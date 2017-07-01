package de.peeeq.wurstio.languageserver2.requests;

import de.peeeq.wurstio.languageserver2.ModelManager;
import de.peeeq.wurstio.languageserver2.BufferManager;
import de.peeeq.wurstio.languageserver2.WFile;
import de.peeeq.wurstscript.ast.*;
import de.peeeq.wurstscript.types.FunctionSignature;
import de.peeeq.wurstscript.types.WurstType;
import de.peeeq.wurstscript.utils.Utils;
import org.eclipse.lsp4j.ParameterInformation;
import org.eclipse.lsp4j.SignatureHelp;
import org.eclipse.lsp4j.SignatureInformation;
import org.eclipse.lsp4j.TextDocumentPositionParams;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
			if (parent instanceof Arguments) {
				Arguments args = (Arguments) parent;
				if (parent.getParent() instanceof StmtCall) {
					StmtCall call = (StmtCall) parent.getParent();
					SignatureHelp info = forCall(call);
					info.setActiveParameter(args.indexOf(e));
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
		int i = 0;
		info.setParameters(new ArrayList<>());
		for (WurstType t : sig.getParamTypes()) {
			String paramName = sig.getParamName(i);
			info.getParameters().add(new ParameterInformation(paramName, t + " " + paramName));
			i++;
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
