package de.peeeq.wurstio.languageserver.requests;

import de.peeeq.wurstio.languageserver.ModelManager;
import de.peeeq.wurstscript.WLogger;
import de.peeeq.wurstscript.ast.*;
import de.peeeq.wurstscript.parser.WPos;
import de.peeeq.wurstscript.types.CallSignature;
import de.peeeq.wurstscript.types.FunctionSignature;
import de.peeeq.wurstscript.types.WurstType;
import de.peeeq.wurstscript.utils.Utils;
import org.eclipse.jdt.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

public class SignatureInfo extends UserRequest {

	private final String filename;
	private final int line;
	private final int column;

	public SignatureInfo(int requestNr, String filename, int line, int column) {
		super(requestNr);
		this.filename = filename;
		this.line = line;
		this.column = column;
	}


	@Override
	public SignatureHelp execute(ModelManager modelManager) {
		CompilationUnit cu = modelManager.getCompilationUnit(filename);
		AstElement e = Utils.getAstElementAtPos(cu, line, column, false);
		if (e instanceof StmtCall) {
			StmtCall call = (StmtCall) e;
			// TODO only when we are in parentheses
			return forCall(call);
		}


		while (e != null) {
			AstElement parent = e.getParent();
			if (parent instanceof Arguments) {
				Arguments args = (Arguments) parent;
				if (parent.getParent() instanceof StmtCall) {
					StmtCall call = (StmtCall) parent.getParent();
					SignatureHelp info = forCall(call);
					info.activeParameter = args.indexOf(e);
					return info;
				}
				break;
			} else if (parent instanceof StmtCall) {
				StmtCall call = (StmtCall) parent;
				return forCall(call);
			}
			e = parent;
		}
		return null;
	}

	private SignatureHelp forCall(StmtCall call) {
		// TODO provide different alternatives
		FunctionSignature sig = call.attrFunctionSignature();
		SignatureHelp help = new SignatureHelp();
		SignatureInformation info = new SignatureInformation();
		info.documentation = "(" + sig.getParameterDescription() +")";
		if (call instanceof FunctionCall) {
			FunctionCall fc = (FunctionCall) call;
			info.label = fc.getFuncName();
		} else if (call instanceof ExprNewObject) {
			ExprNewObject n = (ExprNewObject) call;
			info.label = n.getTypeName();
		}
		int i = 0;
		for (WurstType t : sig.getParamTypes()) {
			info.parameters.add(new ParameterInformation(t.toString(), sig.getParamName(i)));
			i++;
		}

		help.signatures.add(info);
		return help;
	}

	private static class SignatureHelp {
		List<SignatureInformation> signatures = new ArrayList<>();
		int activeSignature = 0;
		int activeParameter = 0;
	}

	private static class SignatureInformation {
		String label;
		String documentation = "";
		List<ParameterInformation> parameters = new ArrayList<>();
	}

	private static class ParameterInformation {
		String label;
		String documentation = "";

		public ParameterInformation(String label, String documentation) {
			this.label = label;
			this.documentation = documentation;
		}
	}

}
