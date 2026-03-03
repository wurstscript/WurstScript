package de.peeeq.wurstio.languageserver.requests;

import de.peeeq.wurstio.languageserver.BufferManager;
import de.peeeq.wurstio.languageserver.Convert;
import de.peeeq.wurstio.languageserver.ModelManager;
import de.peeeq.wurstio.languageserver.WFile;
import de.peeeq.wurstscript.ast.ClassDef;
import de.peeeq.wurstscript.ast.CompilationUnit;
import de.peeeq.wurstscript.ast.ConstructorDef;
import de.peeeq.wurstscript.ast.Element;
import de.peeeq.wurstscript.ast.EnumDef;
import de.peeeq.wurstscript.ast.EnumMember;
import de.peeeq.wurstscript.ast.ExprIntVal;
import de.peeeq.wurstscript.ast.ExprRealVal;
import de.peeeq.wurstscript.ast.ExprStringVal;
import de.peeeq.wurstscript.ast.FuncRef;
import de.peeeq.wurstscript.ast.FunctionDefinition;
import de.peeeq.wurstscript.ast.GlobalVarDef;
import de.peeeq.wurstscript.ast.InterfaceDef;
import de.peeeq.wurstscript.ast.LocalVarDef;
import de.peeeq.wurstscript.ast.ModuleDef;
import de.peeeq.wurstscript.ast.NameDef;
import de.peeeq.wurstscript.ast.NameRef;
import de.peeeq.wurstscript.ast.NativeType;
import de.peeeq.wurstscript.ast.TupleDef;
import de.peeeq.wurstscript.ast.TypeDef;
import de.peeeq.wurstscript.ast.TypeExpr;
import de.peeeq.wurstscript.ast.TypeParamDef;
import de.peeeq.wurstscript.ast.WImport;
import de.peeeq.wurstscript.ast.WPackage;
import de.peeeq.wurstscript.ast.WParameter;
import de.peeeq.wurstscript.attributes.names.FuncLink;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.SemanticTokenModifiers;
import org.eclipse.lsp4j.SemanticTokenTypes;
import org.eclipse.lsp4j.SemanticTokens;
import org.eclipse.lsp4j.SemanticTokensParams;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SemanticTokensRequest extends UserRequest<SemanticTokens> {

    public static final List<String> TOKEN_TYPES = List.of(
            SemanticTokenTypes.Namespace,
            SemanticTokenTypes.Class,
            SemanticTokenTypes.Interface,
            SemanticTokenTypes.Enum,
            SemanticTokenTypes.Type,
            SemanticTokenTypes.TypeParameter,
            SemanticTokenTypes.Function,
            SemanticTokenTypes.Method,
            SemanticTokenTypes.Parameter,
            SemanticTokenTypes.Variable,
            SemanticTokenTypes.Property,
            SemanticTokenTypes.EnumMember,
            SemanticTokenTypes.String,
            SemanticTokenTypes.Number
    );

    public static final List<String> TOKEN_MODIFIERS = List.of(
            SemanticTokenModifiers.Declaration,
            SemanticTokenModifiers.Readonly
    );

    private static final int DECLARATION_MOD = modifierBit(SemanticTokenModifiers.Declaration);
    private static final int READONLY_MOD = modifierBit(SemanticTokenModifiers.Readonly);

    private final WFile filename;
    private final String buffer;

    public SemanticTokensRequest(SemanticTokensParams params, BufferManager bufferManager) {
        this.filename = WFile.create(params.getTextDocument().getUri());
        this.buffer = bufferManager.getBuffer(params.getTextDocument());
    }

    @Override
    public SemanticTokens execute(ModelManager modelManager) {
        CompilationUnit cu = modelManager.replaceCompilationUnitContent(filename, buffer, false);
        if (cu == null) {
            return new SemanticTokens(Collections.emptyList());
        }

        String[] lines = buffer.split("\\r?\\n", -1);
        TokenCollector collector = new TokenCollector(lines);
        ArrayDeque<Element> todo = new ArrayDeque<>();
        todo.push(cu);
        while (!todo.isEmpty()) {
            Element e = todo.pop();
            classify(collector, e);
            for (int i = e.size() - 1; i >= 0; i--) {
                todo.push(e.get(i));
            }
        }

        return new SemanticTokens(collector.encode());
    }

    private void classify(TokenCollector collector, Element e) {
        if (e instanceof WPackage) {
            collector.add(((WPackage) e).getNameId(), tokenTypeIndex(SemanticTokenTypes.Namespace), DECLARATION_MOD);
            return;
        }
        if (e instanceof ClassDef || e instanceof ModuleDef || e instanceof TupleDef || e instanceof NativeType) {
            collector.add(e, tokenTypeIndex(SemanticTokenTypes.Class), DECLARATION_MOD);
            return;
        }
        if (e instanceof InterfaceDef) {
            collector.add(e, tokenTypeIndex(SemanticTokenTypes.Interface), DECLARATION_MOD);
            return;
        }
        if (e instanceof EnumDef) {
            collector.add(e, tokenTypeIndex(SemanticTokenTypes.Enum), DECLARATION_MOD);
            return;
        }
        if (e instanceof TypeParamDef) {
            collector.add(e, tokenTypeIndex(SemanticTokenTypes.TypeParameter), DECLARATION_MOD);
            return;
        }
        if (e instanceof FunctionDefinition) {
            FunctionDefinition f = (FunctionDefinition) e;
            int type = f.attrIsDynamicClassMember()
                    ? tokenTypeIndex(SemanticTokenTypes.Method)
                    : tokenTypeIndex(SemanticTokenTypes.Function);
            collector.add(f, type, DECLARATION_MOD);
            return;
        }
        if (e instanceof ConstructorDef) {
            collector.add(e, tokenTypeIndex(SemanticTokenTypes.Method), DECLARATION_MOD);
            return;
        }
        if (e instanceof WParameter) {
            collector.add(e, tokenTypeIndex(SemanticTokenTypes.Parameter), DECLARATION_MOD);
            return;
        }
        if (e instanceof GlobalVarDef) {
            GlobalVarDef v = (GlobalVarDef) e;
            int type = v.attrIsDynamicClassMember()
                    ? tokenTypeIndex(SemanticTokenTypes.Property)
                    : tokenTypeIndex(SemanticTokenTypes.Variable);
            int mods = DECLARATION_MOD | (v.attrIsConstant() ? READONLY_MOD : 0);
            collector.add(v, type, mods);
            return;
        }
        if (e instanceof LocalVarDef) {
            LocalVarDef v = (LocalVarDef) e;
            int mods = DECLARATION_MOD | (v.attrIsConstant() ? READONLY_MOD : 0);
            collector.add(v, tokenTypeIndex(SemanticTokenTypes.Variable), mods);
            return;
        }
        if (e instanceof EnumMember) {
            collector.add(e, tokenTypeIndex(SemanticTokenTypes.EnumMember), DECLARATION_MOD);
            return;
        }
        if (e instanceof FuncRef) {
            FuncLink f = ((FuncRef) e).attrFuncLink();
            if (f != null) {
                boolean isMethod = f.getDef() != null && f.getDef().attrIsDynamicClassMember();
                collector.add(e, tokenTypeIndex(isMethod ? SemanticTokenTypes.Method : SemanticTokenTypes.Function), 0);
            }
            return;
        }
        if (e instanceof NameRef) {
            NameDef def = ((NameRef) e).tryGetNameDef();
            if (def != null) {
                collector.add(e, tokenTypeForDefinition(def), def.attrIsConstant() ? READONLY_MOD : 0);
            }
            return;
        }
        if (e instanceof TypeExpr) {
            TypeDef def = ((TypeExpr) e).attrTypeDef();
            if (def != null) {
                collector.add(e, tokenTypeForTypeDef(def), 0);
            }
            return;
        }
        if (e instanceof WImport) {
            collector.add(((WImport) e).getPackagenameId(), tokenTypeIndex(SemanticTokenTypes.Namespace), 0);
            return;
        }
        if (e instanceof ExprStringVal) {
            collector.add(e, tokenTypeIndex(SemanticTokenTypes.String), 0);
            return;
        }
        if (e instanceof ExprIntVal || e instanceof ExprRealVal) {
            collector.add(e, tokenTypeIndex(SemanticTokenTypes.Number), 0);
        }
    }

    private int tokenTypeForDefinition(NameDef def) {
        if (def instanceof WParameter) {
            return tokenTypeIndex(SemanticTokenTypes.Parameter);
        }
        if (def instanceof FunctionDefinition) {
            FunctionDefinition f = (FunctionDefinition) def;
            return tokenTypeIndex(f.attrIsDynamicClassMember() ? SemanticTokenTypes.Method : SemanticTokenTypes.Function);
        }
        if (def instanceof TypeDef) {
            return tokenTypeForTypeDef((TypeDef) def);
        }
        if (def instanceof EnumMember) {
            return tokenTypeIndex(SemanticTokenTypes.EnumMember);
        }
        if (def instanceof GlobalVarDef) {
            GlobalVarDef gv = (GlobalVarDef) def;
            return tokenTypeIndex(gv.attrIsDynamicClassMember() ? SemanticTokenTypes.Property : SemanticTokenTypes.Variable);
        }
        if (def instanceof LocalVarDef) {
            return tokenTypeIndex(SemanticTokenTypes.Variable);
        }
        return tokenTypeIndex(SemanticTokenTypes.Variable);
    }

    private int tokenTypeForTypeDef(TypeDef def) {
        if (def instanceof InterfaceDef) {
            return tokenTypeIndex(SemanticTokenTypes.Interface);
        }
        if (def instanceof EnumDef) {
            return tokenTypeIndex(SemanticTokenTypes.Enum);
        }
        return tokenTypeIndex(SemanticTokenTypes.Class);
    }

    private static int tokenTypeIndex(String type) {
        int index = TOKEN_TYPES.indexOf(type);
        return Math.max(index, 0);
    }

    private static int modifierBit(String modifierName) {
        int index = TOKEN_MODIFIERS.indexOf(modifierName);
        if (index < 0) {
            return 0;
        }
        return 1 << index;
    }

    private static class TokenCollector {
        private static class Token {
            int line;
            int startChar;
            int length;
            int type;
            int modifiers;
        }

        private final String[] lines;
        private final List<Token> tokens = new ArrayList<>();
        private final Set<String> dedupe = new LinkedHashSet<>();

        TokenCollector(String[] lines) {
            this.lines = lines;
        }

        void add(Element e, int type, int modifiers) {
            if (e == null || e.attrErrorPos().isArtificial()) {
                return;
            }
            addRange(Convert.errorRange(e), type, modifiers);
        }

        private void addRange(Range range, int type, int modifiers) {
            int startLine = Math.max(0, range.getStart().getLine());
            int endLine = Math.max(startLine, range.getEnd().getLine());
            for (int line = startLine; line <= endLine; line++) {
                if (line >= lines.length) {
                    break;
                }
                int lineLength = lines[line].length();
                int start = line == startLine ? Math.max(0, range.getStart().getCharacter()) : 0;
                int end = line == endLine ? Math.max(0, range.getEnd().getCharacter()) : lineLength;
                start = Math.min(start, lineLength);
                end = Math.min(end, lineLength);
                int length = end - start;
                if (length <= 0) {
                    continue;
                }
                String key = line + ":" + start + ":" + length + ":" + type + ":" + modifiers;
                if (!dedupe.add(key)) {
                    continue;
                }
                Token t = new Token();
                t.line = line;
                t.startChar = start;
                t.length = length;
                t.type = type;
                t.modifiers = modifiers;
                tokens.add(t);
            }
        }

        List<Integer> encode() {
            tokens.sort(Comparator.comparingInt((Token t) -> t.line)
                    .thenComparingInt(t -> t.startChar)
                    .thenComparingInt(t -> t.length));

            List<Integer> encoded = new ArrayList<>(tokens.size() * 5);
            int lastLine = 0;
            int lastStartChar = 0;
            boolean first = true;
            for (Token t : tokens) {
                int deltaLine = first ? t.line : t.line - lastLine;
                int deltaStart = first
                        ? t.startChar
                        : (deltaLine == 0 ? t.startChar - lastStartChar : t.startChar);
                encoded.add(deltaLine);
                encoded.add(deltaStart);
                encoded.add(t.length);
                encoded.add(t.type);
                encoded.add(t.modifiers);
                first = false;
                lastLine = t.line;
                lastStartChar = t.startChar;
            }
            return encoded;
        }
    }
}

