package de.peeeq.wurstio.languageserver.requests;

import de.peeeq.wurstio.languageserver.BufferManager;
import de.peeeq.wurstio.languageserver.ModelManager;
import de.peeeq.wurstio.languageserver.WFile;
import de.peeeq.wurstscript.ast.*;
import de.peeeq.wurstscript.attributes.ParamTypes;
import de.peeeq.wurstscript.types.FunctionSignature;
import de.peeeq.wurstscript.utils.Utils;
import org.eclipse.lsp4j.ParameterInformation;
import org.eclipse.lsp4j.SignatureHelp;
import org.eclipse.lsp4j.SignatureInformation;
import org.eclipse.lsp4j.TextDocumentPositionParams;

import java.util.ArrayList;
import java.util.Collections;

public class SignatureInfo extends UserRequest<SignatureHelp> {

    private final WFile filename;
    private final int line;
    private final int column;


    public SignatureInfo(TextDocumentPositionParams position, BufferManager bufferManager) {
        this.filename = WFile.create(position.getTextDocument().getUri());
        this.line = position.getPosition().getLine() + 1;
        this.column = position.getPosition().getCharacter() + 1;
    }


    @Override
    public SignatureHelp execute(ModelManager modelManager) {
        CompilationUnit cu = modelManager.getCompilationUnit(filename);
        Element e = Utils.getAstElementAtPos(cu, line, column, false);
        if (e instanceof StmtCall) {
            StmtCall call = (StmtCall) e;
            // TODO only when we are in parentheses
            return forCall(call);
        }


        while (e != null) {
            Element parent = e.getParent();
            if (parent instanceof Argument) {
                Argument arg = (Argument) parent;
                Arguments args = (Arguments) parent.getParent();
                if (parent.getParent() instanceof StmtCall) {
                    StmtCall call = (StmtCall) parent.getParent();
                    SignatureHelp info = forCall(call);
                    info.setActiveParameter(args.indexOf(arg));
                    return info;
                }
                break;
            } else if (parent instanceof StmtCall) {
                StmtCall call = (StmtCall) parent;
                return forCall(call);
            }
            e = parent;
        }
        return new SignatureHelp(Collections.emptyList(), 0, 0);
    }

    private SignatureHelp forCall(StmtCall call) {
        // TODO provide different alternatives
        FunctionSignature sig = call.attrFunctionSignature();
        SignatureHelp help = new SignatureHelp();
        SignatureInformation info = new SignatureInformation();
        info.setDocumentation("(" + sig.getParameterDescription() + ")");
        if (call instanceof FunctionCall) {
            FunctionCall fc = (FunctionCall) call;
            info.setLabel(fc.getFuncName());
        } else if (call instanceof ExprNewObject) {
            ExprNewObject n = (ExprNewObject) call;
            info.setLabel(n.getTypeName());
        }
        info.setParameters(new ArrayList<>());
        for (ParamTypes.ParamInfo t : sig.getParamTypes().getParams()) {
            info.getParameters().add(new ParameterInformation(t.getName(), t.toString()));
        }

        help.getSignatures().add(info);
        return help;
    }

//	private static class SignatureHelp {
//		List<SignatureInformation> signatures = new ArrayList<>();
//		int activeSignature = 0;
//		int activeParameter = 0;
//	}

//	private static class SignatureInformation {
//		String label;
//		String documentation = "";
//		List<ParameterInformation> parameters = new ArrayList<>();
//	}
//
//	private static class ParameterInformation {
//		String label;
//		String documentation = "";
//
//		public ParameterInformation(String label, String documentation) {
//			this.label = label;
//			this.documentation = documentation;
//		}
//	}

}
