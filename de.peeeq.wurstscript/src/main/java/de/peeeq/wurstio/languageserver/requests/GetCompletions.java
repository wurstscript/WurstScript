package de.peeeq.wurstio.languageserver.requests;

import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import de.peeeq.wurstio.languageserver.BufferManager;
import de.peeeq.wurstio.languageserver.ModelManager;
import de.peeeq.wurstio.languageserver.WFile;
import de.peeeq.wurstscript.WLogger;
import de.peeeq.wurstscript.WurstKeywords;
import de.peeeq.wurstscript.ast.*;
import de.peeeq.wurstscript.attributes.AttrExprType;
import de.peeeq.wurstscript.attributes.names.NameLink;
import de.peeeq.wurstscript.attributes.names.Visibility;
import de.peeeq.wurstscript.types.WurstType;
import de.peeeq.wurstscript.types.WurstTypeUnknown;
import de.peeeq.wurstscript.types.WurstTypeVoid;
import de.peeeq.wurstscript.utils.Utils;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.lsp4j.*;

import java.io.File;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.*;
import java.util.Map.Entry;

/**
 * Created by peter on 24.04.16.
 */
public class GetCompletions extends UserRequest<CompletionList> {


    private static final int MAX_COMPLETIONS = 100;
    private final WFile filename;
    private final String buffer;
    private final String[] lines;
    private final int line;
    private final int column;
    private String alreadyEntered;
    private String alreadyEnteredLower;
    private SearchMode searchMode;
    private Element elem;
    private WurstType expectedType;
    private ModelManager modelManager;
    private boolean isIncomplete = false;


    public GetCompletions(CompletionParams position, BufferManager bufferManager) {
        this.filename = WFile.create(position.getTextDocument().getUri());
        this.buffer = bufferManager.getBuffer(position.getTextDocument());
        this.line = position.getPosition().getLine() + 1;
        this.column = position.getPosition().getCharacter();
        this.lines = buffer.split("\\n|\\r\\n");
        if (line <= lines.length) {
            WLogger.info("Get completions in line " + line + ": \n" +
                    "" + currentLine().replace('\t', ' ') + "\n" +
                    "" + Utils.repeat(' ', column > 0 ? column : 0) + "^\n" +
                    " at column " + column);
        }
    }

    private String currentLine() {
        return lines[line - 1];
    }

    @Override
    public CompletionList execute(ModelManager modelManager) {
        this.modelManager = modelManager;
        CompilationUnit cu = modelManager.replaceCompilationUnitContent(filename, buffer, false);
        List<CompletionItem> result = computeCompletionProposals(cu);
        // sort: highest rating first, then sort by label
        if (result != null) {
            result.sort(completionItemComparator());
        }
        return new CompletionList(isIncomplete, result);
    }

    private Comparator<CompletionItem> completionItemComparator() {
        return Comparator
                .comparing(CompletionItem::getSortText).reversed()
                .thenComparing(CompletionItem::getLabel);
    }

    private enum SearchMode {
        PREFIX, INFIX, SUBSEQENCE
    }

    /**
     * computes completions at the current position
     */
    public List<CompletionItem> computeCompletionProposals(CompilationUnit cu) {

        if (isEnteringRealNumber()) {
            return null;
        }

        alreadyEntered = getAlreadyEnteredText();
        alreadyEnteredLower = alreadyEntered.toLowerCase();
        WLogger.info("already entered = " + alreadyEntered);

        for (SearchMode mode : SearchMode.values()) {
            searchMode = mode;
            List<CompletionItem> completions = Lists.newArrayList();

            elem = Utils.getAstElementAtPos(cu, line, column + 1, false);
            WLogger.info("get completions at " + Utils.printElement(elem));
            expectedType = null;
            if (elem instanceof Expr) {
                Expr expr = (Expr) elem;
                expectedType = expr.attrExpectedTyp();
                WLogger.info("....expected type = " + expectedType);
            }

            calculateCompletions(completions);

            dropBadCompletions(completions);
            removeDuplicates(completions);

            //		Collections.sort(completions, c)

            if (completions.size() > 0) {
                return completions;
            }
        }
        return null;
    }

    private void calculateCompletions(List<CompletionItem> completions) {
        boolean isMemberAccess = false;
        if (elem instanceof ExprMember) {
            ExprMember e = (ExprMember) elem;

            if (elem instanceof ExprMemberMethod) {
                ExprMemberMethod c = (ExprMemberMethod) elem;
                if (isInParenthesis(c.getLeft().getSource().getEndColumn())) {
                    // cursor inside parenthesis
                    getCompletionsForExistingMemberCall(completions, c);
                    return;
                }
            }

            WurstType leftType = e.getLeft().attrTyp();

            leftType.getMemberMethods(elem).forEach(nameLink -> {
                if (isSuitableCompletion(nameLink.getName())) {
                    CompletionItem completion = makeNameDefCompletion(nameLink.getNameDef());
                    completions.add(completion);
                }
            });

            isMemberAccess = true;
            WScope scope = elem.attrNearestScope();
            // add member vars
            while (scope != null) {
                Multimap<String, NameLink> visibleNames = scope.attrNameLinks();
                completionsAddVisibleNames(alreadyEntered, completions, visibleNames, leftType, isMemberAccess, elem);
                completionsAddVisibleExtensionFunctions(alreadyEntered, completions, visibleNames, leftType);
                scope = scope.attrNextScope();
            }
        } else if (elem instanceof ExprRealVal) {
            // show no hints for reals
        } else if (elem instanceof WPackage) {
            // no hints at package level
        } else if (elem instanceof WImport) {
            //WImport imp = (WImport) elem;
            completeImport(completions);
        } else if (elem instanceof ExprNewObject) {
            ExprNewObject en = (ExprNewObject) elem;
            if (isInParenthesis(en.getSource().getStartColumn())) {
                // cursor inside parameters
                getCompletionsForExistingConstructorCall(completions, en);
                return;
            }
            WScope scope = elem.attrNearestScope();
            while (scope != null) {
                Multimap<String, NameLink> visibleNames = scope.attrNameLinks();
                for (NameLink n : visibleNames.values()) {
                    if (n.getNameDef() instanceof ClassDef && isSuitableCompletion(n.getName())) {
                        ClassDef c = (ClassDef) n.getNameDef();
                        for (ConstructorDef constr : c.getConstructors()) {
                            completions.add(makeConstructorCompletion(c, constr));
                        }
                    }
                }
                scope = scope.attrNextScope();
            }
        } else if (elem instanceof ExprFunctionCall) {
            ExprFunctionCall c = (ExprFunctionCall) elem;
            if (isInParenthesis(c.getSource().getStartColumn())) {
                // cursor is in parameter list
                getCompletionsForExistingCall(completions, c);
            } else {
                addDefaultCompletions(completions, elem, isMemberAccess);
            }
        } else {
            if (elem instanceof ExprEmpty) {
                if (elem.getParent() instanceof Arguments) {
                    Element grandParent = getGrandParent();
                    if (grandParent instanceof ExprFunctionCall) {
                        ExprFunctionCall c = (ExprFunctionCall) grandParent;
                        getCompletionsForExistingCall(completions, c);
                    } else if (grandParent instanceof ExprMemberMethod) {
                        ExprMemberMethod c = (ExprMemberMethod) grandParent;
                        getCompletionsForExistingMemberCall(completions, c);
                    } else if (grandParent instanceof ExprNewObject) {
                        ExprNewObject c = (ExprNewObject) grandParent;
                        getCompletionsForExistingConstructorCall(completions, c);
                    }
                    // TODO add overloaded funcs
                }
            }
            if (completions.isEmpty()) {
                // default completions:
                addDefaultCompletions(completions, elem, isMemberAccess);
            }
        }
        if (!isMemberAccess) {
            addKeywordCompletions(completions);
        }
    }

    private void addKeywordCompletions(List<CompletionItem> completions) {
        for (String keyword : WurstKeywords.KEYWORDS) {
            if (keyword.startsWith(alreadyEntered)) {
                completions.add(makeSimpleNameCompletion(keyword));
            }
        }
        for (String keyword : WurstKeywords.JASS_PRIMITIVE_TYPES) {
            if (keyword.startsWith(alreadyEntered)) {
                completions.add(makeSimpleNameCompletion(keyword));
            }
        }
    }

    private void completeImport(List<CompletionItem> completions) {
        ModelManager mm = modelManager;
        WurstModel model = elem.getModel();
        Set<String> usedPackages = Sets.newHashSet();
        for (WPackage p : model.attrPackages().values()) {
            if (!usedPackages.contains(p.getName()) && isSuitableCompletion(p.getName())) {
                completions.add(makeNameDefCompletion(p));
                usedPackages.add(p.getName());
            }
        }
        for (File dep : mm.getDependencyWurstFiles()) {
            String libName = Utils.getLibName(dep);
            if (!usedPackages.contains(libName) && isSuitableCompletion(libName)) {
                usedPackages.add(libName);
                completions.add(makeSimpleNameCompletion(libName));
            }
        }
        if (isSuitableCompletion("NoWurst")) {
            completions.add(makeSimpleNameCompletion("NoWurst"));
        }
    }

    private @Nullable Element getGrandParent() {
        Element parent = elem.getParent();
        if (parent == null)
            return null;
        return parent.getParent();
    }

    /**
     * Assuming we have a method call like moo.bar(...)
     * <p>
     * checks if the cursor is currently in the parentheses,
     * given the position of the dot
     */
    private boolean isInParenthesis(int start) {
        try {
            String s = currentLine().substring(start, column - start);
            return s.contains("(");
        } catch (IndexOutOfBoundsException e) {
            return false;
        }
    }

    private void addDefaultCompletions(List<CompletionItem> completions, Element elem, boolean isMemberAccess) {
        WurstType leftType;
        leftType = AttrExprType.caclulateThistype(elem, true, null);
        if (leftType instanceof WurstTypeUnknown) {
            leftType = null;
        }
        WScope scope = elem.attrNearestScope();
        while (scope != null) {
            Multimap<String, NameLink> visibleNames = scope.attrNameLinks();
            completionsAddVisibleNames(alreadyEntered, completions, visibleNames, leftType, isMemberAccess, elem);
            scope = scope.attrNextScope();
        }
    }

    private void getCompletionsForExistingConstructorCall(List<CompletionItem> completions, ExprNewObject c) {
        ConstructorDef constructorDef = c.attrConstructorDef();
        if (constructorDef != null) {
            ClassDef classDef = constructorDef.attrNearestClassDef();
            assert classDef != null; // every constructor has a nearest class
            CompletionItem ci = makeConstructorCompletion(classDef, constructorDef);
            ci.setTextEdit(null);
            completions.add(ci);
        }
    }

    private void getCompletionsForExistingMemberCall(List<CompletionItem> completions, ExprMemberMethod c) {
        FunctionDefinition funcDef = c.attrFuncDef();
        if (funcDef != null) {
            CompletionItem ci = makeFunctionCompletion(funcDef);
            ci.setTextEdit(null);
            completions.add(ci);
        }
    }

    private void getCompletionsForExistingCall(List<CompletionItem> completions, ExprFunctionCall c) {
        FunctionDefinition funcDef = c.attrFuncDef();
        if (funcDef != null) {
            alreadyEntered = c.getFuncName();
            CompletionItem ci = makeFunctionCompletion(funcDef);
            ci.setTextEdit(null);
            completions.add(ci);
        }
    }

	/*
    private ICompletionProposal[] toCompletionsArray(List<CompletionItem> completions) {
		Collections.sort(completions);
		ICompletionProposal[] result = new ICompletionProposal[completions.size()];
		for (int i = 0; i < result.length; i++) {
			result[i] = completions.get(i).getProposal();
		}
		return result;
	}
	*/

    private boolean isSuitableCompletion(String name) {
        if(name.endsWith("Tests")) {
            return false;
        }
        switch (searchMode) {
            case PREFIX:
                return name.toLowerCase().startsWith(alreadyEnteredLower);
            case INFIX:
                return name.toLowerCase().contains(alreadyEnteredLower);
            default:
                return Utils.isSubsequenceIgnoreCase(alreadyEntered, name);
        }
    }

    /**
     * checks if we are currently entering a real number
     * (autocomplete might have triggered because of the dot)
     */
    private boolean isEnteringRealNumber() {
        try {
            String currentLine = currentLine();
            String before = currentLine.substring(column - 2, 2);
            return before.matches("[0-9]\\.");
        } catch (IndexOutOfBoundsException e) {
            return false;
        }
    }


    /**
     * checks if cursor is directly before open parenthesis
     */
    private boolean isBeforeParenthesis() {
        try {
            return currentLine().charAt(column) == '(';
        } catch (IndexOutOfBoundsException e) {
            return false;
        }
    }

    private void removeDuplicates(List<CompletionItem> completions) {
        for (int i = 0; i < completions.size() - 1; i++) {
            for (int j = completions.size() - 1; j > i; j--) {
                if (completions.get(i).equals(completions.get(j))) {
                    completions.remove(j);
                }
            }
        }

    }

    private String firstPartOfDisplayString(String s) {
        int p = s.indexOf("-");
        if (p < 0) {
            return s;
        }
        return s.substring(0, p);
    }

    /**
     * get the part of the input which belongs to the identifier being entered currently
     */
    private String getAlreadyEnteredText() {
        try {
            int start = column - 1;
            String currentLine = currentLine();
            while (start >= 0) {
                char c = currentLine.charAt(start);
                if (!Character.isJavaIdentifierPart(c)) {
                    break;
                }
                start--;
            }
            start++;
            return currentLine.substring(start, column);
        } catch (IndexOutOfBoundsException e) {
            return "";
        }
    }

    private void completionsAddVisibleNames(String alreadyEntered, List<CompletionItem> completions, Multimap<String, NameLink> visibleNames,
                                            @Nullable WurstType leftType, boolean isMemberAccess, Element pos) {
        Collection<Entry<String, NameLink>> entries = visibleNames.entries();
        for (Entry<String, NameLink> e : entries) {
            if (!isSuitableCompletion(e.getKey())) {
                continue;
            }

            // remove invisible functions
            if (e.getValue().getVisibility() == Visibility.PRIVATE_OTHER || e.getValue().getVisibility() == Visibility.PROTECTED_OTHER) {
                continue;
            }

            WurstType receiverType = e.getValue().getReceiverType();
            if (leftType == null) {
                if (receiverType != null && !receiverType.isStaticRef()) {
                    // skip extension functions, when not needed
                    continue;
                }
            } else { // leftType != null
                if (receiverType == null) {
                    if (isMemberAccess) {
                        // if this is a member access and receiver type is null, then don't show completion
                        continue;
                    }
                } else {
                    if (!leftType.isSubtypeOf(receiverType, pos)) {
                        // skip elements with wrong receiver type
                        continue;
                    }
                }
            }

            if (e.getValue().getNameDef() instanceof FunctionDefinition) {
                FunctionDefinition f = (FunctionDefinition) e.getValue().getNameDef();

                CompletionItem completion = makeFunctionCompletion(f);
                completions.add(completion);
            } else {
                completions.add(makeNameDefCompletion(e.getValue().getNameDef()));
            }
            if (alreadyEntered.length() <= 3 && completions.size() >= MAX_COMPLETIONS) {
                // got enough completions
                isIncomplete = true;
                return;
            }
        }
    }

    private void dropBadCompletions(List<CompletionItem> completions) {
        completions.sort(completionItemComparator());
        NumberFormat numberFormat = NumberFormat.getNumberInstance(Locale.getDefault());
        for (int i = completions.size() - 1; i >= MAX_COMPLETIONS; i--) {
            try {
                if (numberFormat.parse(completions.get(i).getSortText()).doubleValue() > 0.4) {
                    // good enough
                    return;
                }
            } catch (NumberFormatException | ParseException e) {
                WLogger.severe(e);
            }
            completions.remove(i);
        }
    }

    private CompletionItem makeNameDefCompletion(NameDef n) {
        if (n instanceof FunctionDefinition) {
            return makeFunctionCompletion((FunctionDefinition) n);
        }
        CompletionItem completion = new CompletionItem(n.getName());

        completion.setDetail(HoverInfo.descriptionString(n));
        completion.setDocumentation(n.attrComment());
        double rating = calculateRating(n.getName(), n.attrTyp());
        completion.setSortText(ratingToString(rating));
        String newText = n.getName();
        completion.setInsertText(newText);

        return completion;
    }

    private String ratingToString(double rating) {
        rating = Math.min(10, rating);
        DecimalFormat format = new DecimalFormat("####.000");
        return format.format(10. - rating); // TODO add label?
    }

    private Range range(int line, int startCol, int endCol) {
        return new Range(new Position(line, startCol), new Position(line, endCol));
    }

    private CompletionItem makeSimpleNameCompletion(String name) {
        CompletionItem completion = new CompletionItem(name);

        completion.setDetail("");
        double rating = calculateRating(name, WurstTypeUnknown.instance());
        completion.setSortText(ratingToString(rating));
        completion.setInsertText(name);

        return completion;
    }

    private double calculateRating(String name, WurstType wurstType) {
        double r = calculateNameBasedRating(name);
        if (expectedType != null && wurstType.isSubtypeOf(expectedType, elem)) {
            r += 0.1;
        }
        if (name.contains("BJ") || name.contains("Swapped")) {
            // common.j functions that Frotty does not want to see
            r -= 0.05;
        }
        return r;
    }

    private double calculateNameBasedRating(String name) {
        if (alreadyEntered.isEmpty()) {
            return 0.5;
        }
        if (name.startsWith(alreadyEntered)) {
            // perfect match
            return 1.23;
        }
        String nameLower = name.toLowerCase();
        if (nameLower.startsWith(alreadyEnteredLower)) {
            // close to perfect
            return 0.999;
        }

        int ssLen;
        if (Utils.isSubsequence(alreadyEntered, name)) {
            ssLen = Math.min(Utils.subsequenceLengthes(alreadyEntered, name).size(), Utils.subsequenceLengthes(alreadyEnteredLower, nameLower).size());
        } else {
            ssLen = Utils.subsequenceLengthes(alreadyEnteredLower, nameLower).size();
        }
        return 1 - ssLen * 1. / alreadyEntered.length();
    }

    private static String nearestScopeName(NameDef n) {
        if (n.attrNearestNamedScope() != null) {
            return Utils.printElement(n.attrNearestNamedScope());
        } else {
            return "Global";
        }
    }

    private CompletionItem makeFunctionCompletion(FunctionDefinition f) {
        String replacementString = f.getName();
        WParameters params = f.getParameters();
        if (!isBeforeParenthesis()) {
            if (params.isEmpty()) {
                replacementString += "()";
            }
        }

        CompletionItem completion = new CompletionItem(f.getName());
        completion.setKind(CompletionItemKind.Function);
        completion.setDetail(getFunctionDescriptionShort(f));
        completion.setDocumentation(HoverInfo.descriptionString(f));
        completion.setInsertText(replacementString);
        completion.setSortText(ratingToString(calculateRating(f.getName(), f.getReturnTyp().attrTyp().dynamic())));
        // TODO use call signature instead for generics
//        completion.set

        addParamSnippet(replacementString, params, completion);

        return completion;
    }

    private void addParamSnippet(String replacementString, WParameters params, CompletionItem completion) {
        if (!params.isEmpty()) {
            List<String> paramSnippets = new ArrayList<>();
            for (int i = 0; i < params.size(); i++) {
                WParameter param = params.get(i);
                paramSnippets.add("${" + (i + 1) + ":" + param.getName() + "}");
            }
            replacementString += "(" + String.join(", ", paramSnippets) + ")";
            completion.setInsertText(replacementString);
            completion.setInsertTextFormat(InsertTextFormat.Snippet);
        }
    }

    private String getFunctionDescriptionShort(FunctionDefinition f) {
        String displayString = "(" + Utils.getParameterListText(f) + ")";
        WurstType returnType = f.getReturnTyp().attrTyp();
        if (!(returnType instanceof WurstTypeVoid)) {
            displayString += " returns " + returnType;
        }
        displayString += " [" + nearestScopeName(f) + "]";
        return displayString;
    }

    private CompletionItem makeConstructorCompletion(ClassDef c, ConstructorDef constr) {
        String replacementString = c.getName();
//		if (!isBeforeParenthesis()) {
//			replacementString += "()";
//		}

        CompletionItem completion = new CompletionItem(c.getName());
        completion.setKind(CompletionItemKind.Constructor);
        String params = Utils.getParameterListText(constr);
        completion.setDetail("(" + params + ")");
        completion.setDocumentation(HoverInfo.descriptionString(constr));
        completion.setInsertTextFormat(InsertTextFormat.Snippet);
        completion.setInsertText(replacementString);
        completion.setSortText(ratingToString(calculateRating(c.getName(), c.attrTyp().dynamic())));


        addParamSnippet(replacementString, constr.getParameters(), completion);

        return completion;
    }

    private void completionsAddVisibleExtensionFunctions(String alreadyEntered, List<CompletionItem> completions, Multimap<String, NameLink> visibleNames,
                                                         WurstType leftType) {
        for (Entry<String, NameLink> e : visibleNames.entries()) {
            if (!isSuitableCompletion(e.getKey())) {
                continue;
            }
            if (e.getValue().getNameDef() instanceof ExtensionFuncDef) {
                ExtensionFuncDef ef = (ExtensionFuncDef) e.getValue().getNameDef();
                if (ef.getExtendedType().attrTyp().dynamic().isSupertypeOf(leftType, ef)) {
                    completions.add(makeFunctionCompletion(ef));
                }
            }
        }

    }

//    public static class CompletionItem implements Comparable<CompletionItem> {
//        String label;
//        CompletionItemKind kind;
//        String detail;
//        String documentation;
//        TextEdit textEdit;
//        double rating;
//        List<ParamInfo> parameters;
//
//
//        CompletionItem(String label) {
//            this.label = label;
//        }
//
//        public boolean equalsCompletion(CompletionItem other) {
//            return Objects.equals(other.label, label)
//                    && Objects.equals(other.parameters, parameters);
//        }
//
//        @Override
//        public int compareTo(CompletionItem o) {
//            return Double.compare(rating, o.rating);
//        }
//
//        public CompletionItem withDisableAction() {
//            // TODO remove
//            textEdit = null;
//            return this;
//        }
//
//        public String getDisplayString() {
//            return label;
//        }
//
//        public double getRating() {
//            return rating;
//        }
//    }
//
//    public static class ParamInfo {
//        String type;
//        String name;
//
//        public ParamInfo(String type, String name) {
//            this.type = type;
//            this.name = name;
//        }
//
//        @Override
//        public int hashCode() {
//            return Objects.hash(type, name);
//        }
//
//        @Override
//        public boolean equals(Object obj) {
//            if (obj instanceof ParamInfo) {
//                ParamInfo other = (ParamInfo) obj;
//                return name.equals(other.name) && type.equals(other.type);
//            }
//            return false;
//        }
//    }
//
//    private static class TextEdit {
//        Range range;
//        String newText;
//
//        public TextEdit(Range range, String newText) {
//            this.range = range;
//            this.newText = newText;
//        }
//
//        static TextEdit replace(Range range, String newText) {
//            return new TextEdit(range, newText);
//        }
//
//        static TextEdit insert(TextPos position, String newText) {
//            return new TextEdit(new Range(position, position), newText);
//        }
//
//        static TextEdit delete(Range range) {
//            return new TextEdit(range, "");
//        }
//    }
//
//    private enum CompletionItemKind {
//        Text,
//        Method,
//        Function,
//        Constructor,
//        Field,
//        Variable,
//        Class,
//        Interface,
//        Module,
//        Property,
//        Unit,
//        Value,
//        Enum,
//        Keyword,
//        Snippet,
//        Color,
//        File,
//        Reference;
//    }
}
