package de.peeeq.wurstio.languageserver.requests;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import de.peeeq.wurstio.languageserver.BufferManager;
import de.peeeq.wurstio.languageserver.JassDocService;
import de.peeeq.wurstio.languageserver.ModelManager;
import de.peeeq.wurstio.languageserver.WFile;
import de.peeeq.wurstscript.WLogger;
import de.peeeq.wurstscript.WurstOperator;
import de.peeeq.wurstscript.WurstKeywords;
import de.peeeq.wurstscript.ast.*;
import de.peeeq.wurstscript.attributes.AttrExprType;
import de.peeeq.wurstscript.attributes.names.*;
import de.peeeq.wurstscript.types.*;
import de.peeeq.wurstscript.utils.Utils;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.lsp4j.*;
import org.eclipse.lsp4j.jsonrpc.messages.Either;

import java.io.File;
import java.text.DecimalFormat;
import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;

/**
 * Created by peter on 24.04.16.
 */
public class GetCompletions extends UserRequest<CompletionList> {


    private static final int MAX_COMPLETIONS = 100;
    private static final int MAX_CANDIDATES = 2000;
    private final WFile filename;
    private final String buffer;
    private final String[] lines;
    private final int line;
    private final int column;
    private String alreadyEntered;
    private String alreadyEnteredLower;
    private Element elem;
    private WurstType expectedType;
    private boolean hasExpectedType;
    private ModelManager modelManager;
    private boolean isIncomplete = false;
    private CompilationUnit cu;
    private final IdentityHashMap<CompletionItem, AstElementWithSource> docTargets = new IdentityHashMap<>();
    private final IdentityHashMap<CompletionItem, WurstType> completionTypes = new IdentityHashMap<>();


    public GetCompletions(CompletionParams position, BufferManager bufferManager) {
        this.filename = WFile.create(position.getTextDocument().getUri());
        this.buffer = bufferManager.getBuffer(position.getTextDocument());
        this.line = position.getPosition().getLine() + 1;
        this.column = position.getPosition().getCharacter();
        this.lines = buffer.split("\\n|\\r\\n");
        if (line <= lines.length) {
            WLogger.debug("Get completions in line " + line + ": \n" + currentLine().replace('\t', ' ') + "\n" + Utils.repeat(' ', column > 0 ? column : 0) + "^\n" +
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
                .comparing(CompletionItem::getSortText)
                .thenComparing(CompletionItem::getLabel);
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
        WLogger.debug("already entered = " + alreadyEntered);

        List<CompletionItem> completions = Lists.newArrayList();
        elem = Utils.getAstElementAtPos(cu, line, column + 1, false).get();
        WLogger.debug("get completions at " + Utils.printElement(elem));
        expectedType = null;
        hasExpectedType = false;
        Element typeElem = elem;
        if (!(typeElem instanceof Expr)) {
            Optional<Element> leafElem = Utils.getAstElementAtPos(cu, line, column + 1, true);
            if (leafElem.isPresent()) {
                Expr nearestExpr = nearestEnclosingExpr(leafElem.get());
                if (nearestExpr != null) {
                    typeElem = nearestExpr;
                }
            }
        }
        if (typeElem instanceof Expr) {
            Expr expr = (Expr) typeElem;
            expectedType = expr.attrExpectedTyp();
            if (expectedType instanceof WurstTypeUnknown) {
                WurstType inferred = inferExpectedTypeFromContext(expr);
                if (!(inferred instanceof WurstTypeUnknown)) {
                    expectedType = inferred;
                }
            }
        } else {
            expectedType = inferExpectedTypeFromStatementContext(typeElem);
        }
        hasExpectedType = expectedType != null && !(expectedType instanceof WurstTypeUnknown);
        WLogger.info("....expected type = " + expectedType);

        calculateCompletions(completions);
        removeDuplicates(completions);
        preferBestMatchTier(completions);
        preferExpectedTypeForShortQueries(completions);
        dropBadCompletions(completions);
        enrichTopDocumentation(completions);
        return completions;
    }

    private void preferExpectedTypeForShortQueries(List<CompletionItem> completions) {
        if (!hasExpectedType || alreadyEntered.length() > 2 || completions.isEmpty()) {
            return;
        }
        List<CompletionItem> expectedTypeMatches = new ArrayList<>();
        List<CompletionItem> others = new ArrayList<>();
        for (CompletionItem item : completions) {
            WurstType t = completionTypes.get(item);
            if (t != null && t.isSubtypeOf(expectedType, elem)) {
                expectedTypeMatches.add(item);
            } else {
                others.add(item);
            }
        }
        if (!expectedTypeMatches.isEmpty()) {
            completions.clear();
            completions.addAll(expectedTypeMatches);
            completions.addAll(others);
        }
    }

    private void enrichTopDocumentation(List<CompletionItem> completions) {
        // Keep completion responsive: enrich only visible/top results.
        int limit = Math.min(completions.size(), 24);
        for (int i = 0; i < limit; i++) {
            CompletionItem ci = completions.get(i);
            AstElementWithSource target = docTargets.get(ci);
            if (target == null) {
                continue;
            }
            String comment = null;
            boolean jassDoc = false;
            if (target instanceof FunctionDefinition) {
                FunctionDefinition f = (FunctionDefinition) target;
                comment = f.attrComment();
                if (comment == null || comment.isEmpty()) {
                    comment = JassDocService.getInstance().documentationForFunctionQuick(f);
                    jassDoc = comment != null && !comment.isEmpty();
                }
            } else if (target instanceof NameDef) {
                NameDef n = (NameDef) target;
                comment = n.attrComment();
                if (comment == null || comment.isEmpty()) {
                    comment = JassDocService.getInstance().documentationForVariableQuick(n);
                    jassDoc = comment != null && !comment.isEmpty();
                }
            }
            if (comment == null || comment.isEmpty()) {
                continue;
            }
            if (jassDoc) {
                MarkupContent mc = new MarkupContent();
                mc.setKind("markdown");
                mc.setValue("*JassDoc*\n\n" + comment);
                ci.setDocumentation(Either.forRight(mc));
            } else {
                ci.setDocumentation(comment);
            }
        }
    }

    private void preferBestMatchTier(List<CompletionItem> completions) {
        if (alreadyEntered.length() < 2 || completions.isEmpty()) {
            return;
        }
        int bestTier = Integer.MAX_VALUE;
        for (CompletionItem item : completions) {
            bestTier = Math.min(bestTier, matchTier(item.getLabel()));
        }
        // If we have a strong prefix-like match, keep the strong tier.
        // For case-insensitive prefix matching we keep both exact/case-sensitive and case-insensitive tiers
        // so typing lowercase still keeps uppercase constants (e.g. "t" -> "TEXT_*").
        if (bestTier <= 2) {
            int minTier = Math.max(0, bestTier - 1);
            int maxTier = 2;
            completions.removeIf(c -> {
                if (alreadyEntered.length() <= 2 && isExpectedTypeMatch(c)) {
                    return false;
                }
                if (alreadyEntered.length() >= 3 && shouldKeepAsExpectedTypeFallback(c)) {
                    return false;
                }
                int t = matchTier(c.getLabel());
                return t < minTier || t > maxTier;
            });
        }
    }

    private boolean isExpectedTypeMatch(CompletionItem item) {
        if (!hasExpectedType) {
            return false;
        }
        WurstType t = completionTypes.get(item);
        return t != null && t.isSubtypeOf(expectedType, elem);
    }

    private boolean shouldKeepAsExpectedTypeFallback(CompletionItem item) {
        if (!isExpectedTypeMatch(item)) {
            return false;
        }
        int tier = matchTier(item.getLabel());
        if (tier < 3 || tier > 6) {
            return false;
        }
        String normalizedQuery = normalizedIdentifier(alreadyEntered);
        if (normalizedQuery.isEmpty()) {
            return false;
        }
        String normalizedName = normalizedIdentifier(item.getLabel());
        int span = subsequenceSpan(normalizedQuery, normalizedName);
        if (span < 0) {
            return false;
        }
        // Keep compact expected-type subsequence matches (e.g. TEXTU -> TEXT_JUSTIFY_*),
        // but reject very gappy matches that mostly add noise.
        return span <= normalizedQuery.length() + 3;
    }

    private WurstType inferExpectedTypeFromContext(Expr expr) {
        Element parent = expr.getParent();
        if (parent instanceof ExprBinary) {
            ExprBinary b = (ExprBinary) parent;
            Expr other = b.getLeft() == expr ? b.getRight() : b.getLeft();
            WurstType inferred = inferFromBinaryComparison(b, other);
            if (!(inferred instanceof WurstTypeUnknown)) {
                return inferred;
            }
        }
        return WurstTypeUnknown.instance();
    }

    private @Nullable Expr nearestEnclosingExpr(Element e) {
        Element current = e;
        while (current != null) {
            if (current instanceof Expr) {
                return (Expr) current;
            }
            current = current.getParent();
        }
        return null;
    }

    private WurstType inferExpectedTypeFromStatementContext(Element contextElem) {
        if (contextElem instanceof StmtIf) {
            return inferFromConditionalExpr(((StmtIf) contextElem).getCond());
        }
        if (contextElem instanceof StmtWhile) {
            return inferFromConditionalExpr(((StmtWhile) contextElem).getCond());
        }
        return WurstTypeUnknown.instance();
    }

    private WurstType inferFromConditionalExpr(Expr cond) {
        if (cond instanceof ExprBinary) {
            ExprBinary b = (ExprBinary) cond;
            if (b.getRight() instanceof ExprEmpty) {
                WurstType inferred = inferFromBinaryComparison(b, b.getLeft());
                if (!(inferred instanceof WurstTypeUnknown)) {
                    return inferred;
                }
            }
            if (b.getLeft() instanceof ExprEmpty) {
                WurstType inferred = inferFromBinaryComparison(b, b.getRight());
                if (!(inferred instanceof WurstTypeUnknown)) {
                    return inferred;
                }
            }
        }
        return WurstTypeUnknown.instance();
    }

    private WurstType inferFromBinaryComparison(ExprBinary b, Expr otherSide) {
        // For comparisons, mirror the opposite side type (e.g. p == | -> player).
        if (b.getOp() == WurstOperator.EQ
            || b.getOp() == WurstOperator.NOTEQ
            || b.getOp() == WurstOperator.LESS
            || b.getOp() == WurstOperator.LESS_EQ
            || b.getOp() == WurstOperator.GREATER
            || b.getOp() == WurstOperator.GREATER_EQ) {
            WurstType otherType = otherSide.attrTyp();
            if (!(otherType instanceof WurstTypeUnknown)) {
                return otherType;
            }
        }
        return WurstTypeUnknown.instance();
    }

    private void calculateCompletions(List<CompletionItem> completions) {
        boolean isMemberAccess = false;
        if (elem instanceof ExprMember) {
            ExprMember e = (ExprMember) elem;

            if (elem instanceof ExprMemberMethod) {
                ExprMemberMethod c = (ExprMemberMethod) elem;
                if (isInParenthesis(c.getLeft().getSource().getEndColumn()) || isInsideCallArguments(c.getFuncName(), c.getLeft().getSource().getEndColumn())) {
                    // cursor inside parenthesis
                    getCompletionsForExistingMemberCall(completions, c);
                    return;
                }
            }

            WurstType leftType = e.getLeft().attrTyp();

            addArrayLengthCompletion(completions, e, leftType);

            if (leftType instanceof WurstTypeNamedScope) {
                WurstTypeNamedScope ct = (WurstTypeNamedScope) leftType;
                for (DefLink nameLink : ct.nameLinks().values()) {
                    if (isSuitableCompletion(nameLink.getName())
                            && (nameLink.getReceiverType() != null || nameLink instanceof TypeDefLink)
                            && nameLink.getVisibility() == Visibility.PUBLIC) {
                        CompletionItem completion = makeNameDefCompletion(nameLink);
                        if (!addCompletionCandidate(completions, completion)) {
                            isIncomplete = true;
                            if (!hasExpectedType) {
                                break;
                            }
                        }
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
                    // Also provide value suggestions for the current argument context.
                    addDefaultCompletions(completions, elem, false);
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

    private void addArrayLengthCompletion(List<CompletionItem> completions, ExprMember member, WurstType leftType) {
        boolean hasConstArrayInitializer = false;
        if (member.getLeft() instanceof NameRef) {
            NameDef nameDef = ((NameRef) member.getLeft()).tryGetNameDef();
            if (nameDef instanceof GlobalOrLocalVarDef) {
                GlobalOrLocalVarDef varDef = (GlobalOrLocalVarDef) nameDef;
                hasConstArrayInitializer = varDef.getInitialExpr() instanceof ArrayInitializer;
            }
        }

        if (!hasConstArrayInitializer || !isSuitableCompletion("length")) {
            return;
        }

        CompletionItem completion = new CompletionItem("length");
        completion.setKind(CompletionItemKind.Property);
        completion.setDetail("int length");
        completion.setInsertText("length");
        completion.setSortText(ratingToString(calculateRating("length", WurstTypeInt.instance())));
        completions.add(completion);
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
            preferExpectedTypeFromCall(funcDef, currentArgumentIndex(c.getFuncName(), c.getLeft().getSource().getEndColumn()));
            // Also provide argument value suggestions at this call site.
            addDefaultCompletions(completions, c, false);
            return;
        }
        // Fallback for incomplete/unresolved calls: infer expected parameter type from visible overloads.
        inferExpectedTypeFromUnresolvedMemberCall(c);
        addDefaultCompletions(completions, c, false);
    }

    private void getCompletionsForExistingCall(List<CompletionItem> completions, ExprFunctionCall c) {
        FuncLink funcDef = c.attrFuncLink();
        if (funcDef != null) {
            alreadyEntered = c.getFuncName();
            CompletionItem ci = makeFunctionCompletion(funcDef);
            ci.setTextEdit(null);
            completions.add(ci);
            preferExpectedTypeFromCall(funcDef, currentArgumentIndex(c.getFuncName(), c.getSource().getStartColumn()));
            // Also provide argument value suggestions at this call site.
            addDefaultCompletions(completions, c, false);
        }
    }

    private void preferExpectedTypeFromCall(FuncLink funcDef, int argIndex) {
        if (argIndex < 0 || argIndex >= funcDef.getParameterTypes().size()) {
            return;
        }
        WurstType paramType = funcDef.getParameterType(argIndex);
        if (paramType instanceof WurstTypeUnknown) {
            return;
        }
        if (!hasExpectedType || expectedType instanceof WurstTypeUnknown) {
            expectedType = paramType;
            hasExpectedType = true;
        }
    }

    private int currentArgumentIndex(String funcName, int searchStartColumn) {
        String lineText = currentLine();
        int searchStart = Math.max(0, Math.min(lineText.length(), searchStartColumn));
        int paren = lineText.indexOf(funcName + "(", searchStart);
        if (paren >= 0) {
            paren = paren + funcName.length();
        } else {
            paren = lineText.indexOf('(', searchStart);
        }
        return argumentIndexFromParen(lineText, paren);
    }

    private boolean isInsideCallArguments(String funcName, int searchStartColumn) {
        String lineText = currentLine();
        int searchStart = Math.max(0, Math.min(lineText.length(), searchStartColumn));
        int funcPos = lineText.indexOf(funcName + "(", searchStart);
        if (funcPos < 0) {
            return false;
        }
        int paren = funcPos + funcName.length();
        if (paren >= column) {
            return false;
        }
        int depth = 0;
        for (int i = paren; i < column && i < lineText.length(); i++) {
            char ch = lineText.charAt(i);
            if (ch == '(') {
                depth++;
            } else if (ch == ')') {
                depth--;
            }
        }
        return depth > 0;
    }

    private int argumentIndexFromParen(String lineText, int paren) {
        if (paren < 0 || paren >= column) {
            return 0;
        }
        int depth = 0;
        int commas = 0;
        for (int i = paren + 1; i < column && i < lineText.length(); i++) {
            char ch = lineText.charAt(i);
            if (ch == '(') {
                depth++;
            } else if (ch == ')') {
                if (depth > 0) {
                    depth--;
                }
            } else if (ch == ',' && depth == 0) {
                commas++;
            }
        }
        return commas;
    }

    private void inferExpectedTypeFromUnresolvedMemberCall(ExprMemberMethod c) {
        int argIndex = currentArgumentIndex(c.getFuncName(), c.getLeft().getSource().getEndColumn());
        WurstType candidate = WurstTypeUnknown.instance();
        Collection<FunctionSignature> sigs = c.attrPossibleFunctionSignatures();
        for (FunctionSignature sig : sigs) {
            if (argIndex < sig.getMaxNumParams()) {
                candidate = candidate.typeUnion(sig.getParamType(argIndex), c);
            }
        }
        if (!(candidate instanceof WurstTypeUnknown)) {
            expectedType = candidate;
            hasExpectedType = true;
            return;
        }

        WurstType leftType = c.getLeft().attrTyp();
        if (leftType instanceof WurstTypeUnknown) {
            return;
        }
        WScope scope = c.attrNearestScope();
        while (scope != null) {
            Collection<DefLink> defs = scope.attrNameLinks().get(c.getFuncName());
            for (DefLink d : defs) {
                if (!(d instanceof FuncLink)) {
                    continue;
                }
                FuncLink f = (FuncLink) d;
                FuncLink bound = f.adaptToReceiverType(leftType);
                if (bound == null || argIndex >= bound.getParameterTypes().size()) {
                    continue;
                }
                candidate = candidate.typeUnion(bound.getParameterType(argIndex), c);
            }
            scope = scope.attrNextScope();
        }
        if (!(candidate instanceof WurstTypeUnknown)) {
            expectedType = candidate;
            hasExpectedType = true;
        }
    }

    private boolean addCompletionCandidate(List<CompletionItem> completions, CompletionItem candidate) {
        if (completions.size() < MAX_CANDIDATES) {
            completions.add(candidate);
            return true;
        }
        if (!hasExpectedType || !isExpectedTypeMatch(candidate)) {
            return false;
        }
        // Keep expected-type candidates even when the cap is reached by replacing
        // a non-expected candidate if possible.
        for (int i = completions.size() - 1; i >= 0; i--) {
            CompletionItem existing = completions.get(i);
            if (!isExpectedTypeMatch(existing)) {
                completions.remove(i);
                docTargets.remove(existing);
                completionTypes.remove(existing);
                completions.add(candidate);
                return true;
            }
        }
        return false;
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

    /**
     * checks if we are currently entering a real number
     * (autocomplete might have triggered because of the dot)
     */
    private boolean isEnteringRealNumber() {
        String line = currentLine();
        if (column < 2 || column > line.length()) {
            return false;
        }
        // cursor is expected directly after '.'
        if (line.charAt(column - 1) != '.') {
            return false;
        }
        int i = column - 2;
        if (!Character.isDigit(line.charAt(i))) {
            return false;
        }
        while (i >= 0 && Character.isDigit(line.charAt(i))) {
            i--;
        }
        // avoid suppressing member access on identifiers ending with digits (e.g. foo2.)
        return i < 0 || !Character.isJavaIdentifierPart(line.charAt(i));
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
                if (!addCompletionCandidate(completions, completion)) {
                    isIncomplete = true;
                    if (!hasExpectedType) {
                        return;
                    }
                    continue;
                }
            } else {
                CompletionItem completion = makeNameDefCompletion(defLink);
                if (!addCompletionCandidate(completions, completion)) {
                    isIncomplete = true;
                    if (!hasExpectedType) {
                        return;
                    }
                }
            }
        }
    }

    private void dropBadCompletions(List<CompletionItem> completions) {
        completions.sort(completionItemComparator());
        if (completions.size() > MAX_COMPLETIONS) {
            completions.subList(MAX_COMPLETIONS, completions.size()).clear();
            isIncomplete = true;
        }
    }

    private CompletionItem makeNameDefCompletion(NameLink n) {
        if (n instanceof FuncLink) {
            return makeFunctionCompletion((FuncLink) n);
        }
        CompletionItem completion = new CompletionItem(n.getName());

        completion.setDetail(n.getTyp() + " [" + nearestScopeName(n.getDef()) + "]");
        completion.setDocumentation(n.getDef().attrComment());
        docTargets.put(completion, n.getDef());
        double rating = calculateRating(n.getName(), n.getTyp());
        completion.setSortText(ratingToString(rating));
        String newText = n.getName();
        completion.setInsertText(newText);
        completionTypes.put(completion, n.getTyp());

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
        completionTypes.put(completion, WurstTypeUnknown.instance());

        return completion;
    }

    private double calculateRating(String name, WurstType wurstType) {
        double r = calculateNameBasedRating(name);
        if (hasExpectedType) {
            if (wurstType.isSubtypeOf(expectedType, elem)) {
                // Strongly prefer candidates that satisfy the expected type at cursor.
                r += 1.0;
            } else {
                r -= 0.12;
            }
        }
        if (name.contains("BJ") || name.contains("Swapped")) {
            // common.j functions that Frotty does not want to see
            r -= 0.05;
        }
        return r;
    }

    private double calculateNameBasedRating(String name) {
        if (alreadyEntered.isEmpty()) {
            return 0.6;
        }
        String nameLower = name.toLowerCase();
        switch (matchTier(name)) {
            case 0:
                return 1.8;
            case 1:
                return 1.65;
            case 2:
                return 1.55;
            case 3:
                return 1.45;
            case 4:
                return 1.35;
            case 5:
                return 1.25;
            case 6:
                int span = subsequenceSpan(alreadyEnteredLower, nameLower);
                if (span > 0) {
                    double compactness = 1.0 - ((double) span / Math.max(1, name.length()));
                    return 0.75 + 0.4 * compactness;
                }
                return 0.75;
            default:
                return -1.0;
        }
    }

    private boolean isCamelSubsequence(String query, String candidate) {
        if (query.isEmpty()) {
            return false;
        }
        StringBuilder caps = new StringBuilder();
        for (int i = 0; i < candidate.length(); i++) {
            char c = candidate.charAt(i);
            if (Character.isUpperCase(c) || i == 0 || !Character.isLetterOrDigit(candidate.charAt(i - 1))) {
                caps.append(Character.toLowerCase(c));
            }
        }
        return Utils.isSubsequenceIgnoreCase(query, caps.toString());
    }

    private int subsequenceSpan(String queryLower, String candidateLower) {
        int qi = 0;
        int start = -1;
        int end = -1;
        for (int i = 0; i < candidateLower.length() && qi < queryLower.length(); i++) {
            if (candidateLower.charAt(i) == queryLower.charAt(qi)) {
                if (start < 0) {
                    start = i;
                }
                end = i;
                qi++;
            }
        }
        if (qi < queryLower.length()) {
            return -1;
        }
        return end - start + 1;
    }

    private boolean isPotentialMatch(String name) {
        return matchTier(name) < 7;
    }

    private int matchTier(String name) {
        if (alreadyEntered.isEmpty()) {
            return 0;
        }
        int qLen = alreadyEntered.length();
        String nameLower = name.toLowerCase();
        String normalizedQuery = normalizedIdentifier(alreadyEntered);
        String normalizedName = normalizedIdentifier(name);
        if (name.equals(alreadyEntered)) {
            return 0;
        }
        if (name.startsWith(alreadyEntered)) {
            return 1;
        }
        if (nameLower.startsWith(alreadyEnteredLower)) {
            return 2;
        }
        // treat '_' and other non-identifier separators as optional for matching
        if (!normalizedQuery.isEmpty() && normalizedName.startsWith(normalizedQuery)) {
            return 2;
        }
        // For short prefixes, keep matching intentionally tight to avoid noisy "dead-end" lists.
        if (qLen == 1) {
            return 7;
        }
        if (isCamelSubsequence(alreadyEntered, name)) {
            return 3;
        }
        if (qLen == 2) {
            // Avoid broad contains/subsequence for 2-char queries.
            return 7;
        }
        if (name.contains(alreadyEntered)) {
            return 4;
        }
        if (nameLower.contains(alreadyEnteredLower)) {
            return 5;
        }
        if (!normalizedQuery.isEmpty() && normalizedName.contains(normalizedQuery)) {
            return 5;
        }
        if (Utils.isSubsequenceIgnoreCase(alreadyEntered, name)) {
            return 6;
        }
        return 7;
    }

    private String normalizedIdentifier(String s) {
        StringBuilder b = new StringBuilder(s.length());
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (Character.isLetterOrDigit(c)) {
                b.append(Character.toLowerCase(c));
            }
        }
        return b.toString();
    }

    private boolean isSuitableCompletion(String name) {
        if (name.endsWith("Tests")) {
            return false;
        }
        return isPotentialMatch(name);
    }

    private static String nearestScopeName(NameDef n) {
        if (n.attrNearestNamedScope() != null) {
            return Utils.printElement(n.attrNearestNamedScope());
        } else {
            return "Global";
        }
    }

    private static String nearestScopeName(Element e) {
        if (e.attrNearestNamedScope() != null) {
            return Utils.printElement(e.attrNearestNamedScope());
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
        completion.setDocumentation(f.getDef().attrComment());
        docTargets.put(completion, f.getDef());
        completion.setInsertText(replacementString);
		double rating = calculateRating(f.getName(), f.getReturnType());
		if (f.getDef().attrHasAnnotation("deprecated")) {
			rating -= 0.05;
		}
		completion.setSortText(ratingToString(rating));
        completionTypes.put(completion, f.getReturnType());
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
        completion.setDocumentation(constr.attrComment());
        docTargets.put(completion, constr);
        completion.setInsertTextFormat(InsertTextFormat.Snippet);
        completion.setInsertText(replacementString);
        completion.setSortText(ratingToString(calculateRating(c.getName(), c.attrTyp().dynamic())));
        completionTypes.put(completion, c.attrTyp().dynamic());


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
                    if (!addCompletionCandidate(completions, makeFunctionCompletion(ef2))) {
                        isIncomplete = true;
                        if (!hasExpectedType) {
                            return;
                        }
                    }
                }
            }
        }

    }

}
