package de.peeeq.wurstio.languageserver.requests;

import com.google.common.collect.ImmutableMultimap;
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
import de.peeeq.wurstscript.attributes.names.*;
import de.peeeq.wurstscript.types.*;
import de.peeeq.wurstscript.utils.Utils;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.lsp4j.*;

import java.io.File;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;

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
    private CompilationUnit cu;


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
        cu = modelManager.replaceCompilationUnitContent(filename, buffer, false);
        if (cu == null) {
            return new CompletionList(Collections.emptyList());
        }
        List<CompletionItem> result = computeCompletionProposals(cu);
        // sort: highest rating first, then sort by label
        if (result != null) {
            result.sort(completionItemComparator());
            return new CompletionList(isIncomplete, result);
        } else {
            return new CompletionList(isIncomplete, Collections.emptyList());
        }
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

            elem = Utils.getAstElementAtPos(cu, line, column + 1, false).get();
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

            if (leftType instanceof WurstTypeNamedScope) {
                WurstTypeNamedScope ct = (WurstTypeNamedScope) leftType;
                for (DefLink nameLink : ct.nameLinks().values()) {
                    if (isSuitableCompletion(nameLink.getName())
                            && (nameLink.getReceiverType() != null || nameLink instanceof TypeDefLink)
                            && nameLink.getVisibility() == Visibility.PUBLIC) {
                        CompletionItem completion = makeNameDefCompletion(nameLink);
                        completions.add(completion);
                    }
                }
            }

            isMemberAccess = true;
            WScope scope = elem.attrNearestScope();
            // add member vars
            while (scope != null) {
                ImmutableMultimap<String, DefLink> visibleNames = scope.attrNameLinks();
                completionsAddVisibleNames(alreadyEntered, completions, visibleNames, leftType, isMemberAccess, elem);
                completionsAddVisibleExtensionFunctions(completions, visibleNames, leftType);
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
                Multimap<String, DefLink> visibleNames = scope.attrNameLinks();
                for (NameLink n : visibleNames.values()) {
                    if (n.getDef() instanceof ClassDef && isSuitableCompletion(n.getName())) {
                        ClassDef c = (ClassDef) n.getDef();
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
                completions.add(makeNameDefCompletion(PackageLink.create(p, p.attrNearestScope())));
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
            Multimap<String, DefLink> visibleNames = scope.attrNameLinks();
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
        FuncLink funcDef = c.attrFuncLink();
        if (funcDef != null) {
            CompletionItem ci = makeFunctionCompletion(funcDef);
            ci.setTextEdit(null);
            completions.add(ci);
        }
    }

    private void getCompletionsForExistingCall(List<CompletionItem> completions, ExprFunctionCall c) {
        FuncLink funcDef = c.attrFuncLink();
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
        if (name.endsWith("Tests")) {
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

    private boolean isAtEndOfLine() {
        String line = currentLine();
        for (int i = column + 1; i < line.length(); i++) {
            if (!Character.isWhitespace(line.charAt(i))) {
                return false;
            }
        }
        return true;
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

    private void completionsAddVisibleNames(String alreadyEntered, List<CompletionItem> completions, Multimap<String, DefLink> visibleNames,
                                            @Nullable WurstType leftType, boolean isMemberAccess, Element pos) {
        Collection<Entry<String, DefLink>> entries = visibleNames.entries();
        for (Entry<String, DefLink> e : entries) {
            if (!isSuitableCompletion(e.getKey())) {
                continue;
            }
            DefLink defLink = e.getValue();

            // remove invisible functions
            if (defLink.getVisibility() == Visibility.PRIVATE_OTHER || defLink.getVisibility() == Visibility.PROTECTED_OTHER) {
                continue;
            }

            WurstType receiverType = defLink.getReceiverType();
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

            if (defLink instanceof FuncLink) {
                FuncLink funcLink = (FuncLink) defLink;
                CompletionItem completion = makeFunctionCompletion(funcLink);
                completions.add(completion);
            } else {
                completions.add(makeNameDefCompletion(defLink));
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

    private CompletionItem makeNameDefCompletion(NameLink n) {
        if (n instanceof FuncLink) {
            return makeFunctionCompletion((FuncLink) n);
        }
        CompletionItem completion = new CompletionItem(n.getName());

        completion.setDetail(HoverInfo.descriptionString(n.getDef()));
        completion.setDocumentation(n.getDef().attrComment());
        double rating = calculateRating(n.getName(), n.getTyp());
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

    private CompletionItem makeFunctionCompletion(FuncLink f) {
        String replacementString = f.getName();
        List<WurstType> params = f.getParameterTypes();


        CompletionItem completion = new CompletionItem(f.getName());
        completion.setKind(CompletionItemKind.Function);
        completion.setDetail(getFunctionDescriptionShort(f.getDef()));
        completion.setDocumentation(HoverInfo.descriptionString(f.getDef()));
        completion.setInsertText(replacementString);
		double rating = calculateRating(f.getName(), f.getReturnType());
		if (f.getDef().attrHasAnnotation("deprecated")) {
			rating -= 0.05;
		}
		completion.setSortText(ratingToString(rating));
        // TODO use call signature instead for generics
//        completion.set

        if (!isBeforeParenthesis()) {
            addParamSnippet(replacementString, f, completion);
        }


        return completion;
    }

    private void addParamSnippet(String replacementString, FuncLink f, CompletionItem completion) {
        List<String> paramNames = f.getParameterNames();
        StringBuilder lambdaReplacement = null;
        List<WurstType> parameterTypes = f.getParameterTypes();
        if (isAtEndOfLine() && !parameterTypes.isEmpty()) {
            WurstType lastParamType = Utils.getLast(parameterTypes);
            if (lastParamType instanceof WurstTypeClassOrInterface) {
                WurstTypeClassOrInterface it = (WurstTypeClassOrInterface) lastParamType;
                FuncLink singleAbstractMethod = it.findSingleAbstractMethod(elem);
                if (singleAbstractMethod != null) {
                    paramNames = Utils.init(paramNames);

                    if (singleAbstractMethod.getParameterTypes().size() == 0) {
                        lambdaReplacement = new StringBuilder(" -> \n");
                        cu.getCuInfo().getIndentationMode().appendIndent(lambdaReplacement, 1);
                    } else {
                        lambdaReplacement = new StringBuilder(" (");
                        for (int i = 0; i < singleAbstractMethod.getParameterTypes().size(); i++) {
                            if (i > 0) {
                                lambdaReplacement.append(", ");
                            }
                            lambdaReplacement.append(singleAbstractMethod.getParameterType(i));
                            lambdaReplacement.append(" ");
                            lambdaReplacement.append(singleAbstractMethod.getParameterName(i));
                        }
                        lambdaReplacement.append(") ->\n");
                        // only need to add one indent here, because \n already indents to the same line as before
                        cu.getCuInfo().getIndentationMode().appendIndent(lambdaReplacement, 1);
                    }
                }
            }
        }


        addParamSnippet(replacementString, paramNames, completion, lambdaReplacement);
    }

    private void addParamSnippet(String replacementString, List<String> paramNames, CompletionItem completion, StringBuilder lambdaReplacement) {
        if (paramNames.isEmpty()) {
            replacementString += "()";
        } else {
            List<String> paramSnippets = new ArrayList<>();
            for (int i = 0; i < paramNames.size(); i++) {
                String paramName = paramNames.get(i);
                paramSnippets.add("${" + (i + 1) + ":" + paramName + "}");
            }
            replacementString += "(" + String.join(", ", paramSnippets) + ")";
            completion.setInsertTextFormat(InsertTextFormat.Snippet);
        }
        if (lambdaReplacement != null) {
            replacementString += lambdaReplacement;
            completion.setInsertTextFormat(InsertTextFormat.Snippet);
        }
        completion.setInsertText(replacementString);
    }

    private String getFunctionDescriptionShort(FunctionDefinition f) {
        String displayString = "(" + Utils.getParameterListText(f) + ")";
        WurstType returnType = f.attrReturnTyp();
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


        List<String> parameterNames = constr.getParameters().stream().map(WParameter::getName).collect(Collectors.toList());
        addParamSnippet(replacementString, parameterNames, completion, null);

        return completion;
    }

    private void completionsAddVisibleExtensionFunctions(List<CompletionItem> completions, Multimap<String, DefLink> visibleNames,
                                                         WurstType leftType) {
        for (Entry<String, DefLink> e : visibleNames.entries()) {
            if (!isSuitableCompletion(e.getKey())) {
                continue;
            }
            if (e.getValue() instanceof FuncLink && e.getValue().getVisibility().isPublic()) {
                FuncLink ef = (FuncLink) e.getValue();
                FuncLink ef2 = ef.adaptToReceiverType(leftType);
                if (ef2 != null) {
                    completions.add(makeFunctionCompletion(ef2));
                }
            }
        }

    }

}
