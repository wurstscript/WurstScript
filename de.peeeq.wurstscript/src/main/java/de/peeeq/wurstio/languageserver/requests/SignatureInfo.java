package de.peeeq.wurstio.languageserver.requests;

import de.peeeq.wurstio.languageserver.BufferManager;
import de.peeeq.wurstio.languageserver.JassDocService;
import de.peeeq.wurstio.languageserver.ModelManager;
import de.peeeq.wurstio.languageserver.WFile;
import de.peeeq.wurstscript.ast.*;
import de.peeeq.wurstscript.attributes.names.FuncLink;
import de.peeeq.wurstscript.types.FunctionSignature;
import de.peeeq.wurstscript.types.WurstType;
import de.peeeq.wurstscript.utils.Utils;
import org.eclipse.lsp4j.ParameterInformation;
import org.eclipse.lsp4j.SignatureHelp;
import org.eclipse.lsp4j.SignatureInformation;
import org.eclipse.lsp4j.TextDocumentPositionParams;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Optional;

public class SignatureInfo extends UserRequest<SignatureHelp> {

	private final WFile filename;
	private final String buffer;
	private final int line;
	private final int column;


	public SignatureInfo(TextDocumentPositionParams position, BufferManager bufferManager) {
		this.filename = WFile.create(position.getTextDocument().getUri());
		this.buffer = bufferManager.getBuffer(position.getTextDocument());
		this.line = position.getPosition().getLine() + 1;
		this.column = position.getPosition().getCharacter() + 1;
	}


	@Override
	public SignatureHelp execute(ModelManager modelManager) {
		CompilationUnit cu = modelManager.replaceCompilationUnitContent(filename, buffer, false);
		if (cu == null) {
			return new SignatureHelp(Collections.emptyList(), 0, 0);
		}
		Optional<Element> e = Utils.getAstElementAtPos(cu, line, column, false);
		if (!e.isPresent()) {
			return new SignatureHelp(Collections.emptyList(), 0, 0);
		}
		if (e.get() instanceof StmtCall) {
			StmtCall call = (StmtCall) e.get();
			// TODO only when we are in parentheses
			return forCall(call);
		}


		while (e.isPresent()) {
			Optional<Element> parent = e.flatMap(el -> Optional.ofNullable(el.getParent()));
			if (parent.isPresent() && parent.get() instanceof Arguments) {
				Arguments args = (Arguments) parent.get();
                if (parent.get().getParent() instanceof StmtCall) {
                    StmtCall call = (StmtCall) parent.get().getParent();
                    SignatureHelp info = forCall(call);
                    info.setActiveParameter(args.indexOf(e.get()));
                    return info;
                }
				break;
			} else if (parent.isPresent() && parent.get() instanceof StmtCall) {
				StmtCall call = (StmtCall) parent.get();
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
		String docs = null;
		if (call instanceof FunctionCall) {
			FunctionCall fc = (FunctionCall) call;
			FuncLink funcLink = fc.attrFuncLink();
			if (funcLink != null) {
				docs = JassDocService.getInstance().documentationForFunction(funcLink.getDef());
			}
		}
		String signatureDoc = "(" + sig.getParameterDescription() + ")";
		info.setDocumentation(docs == null || docs.isEmpty() ? signatureDoc : docs + "\n" + signatureDoc);
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

}
