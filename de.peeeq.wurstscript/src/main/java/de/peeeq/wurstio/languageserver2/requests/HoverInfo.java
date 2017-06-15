package de.peeeq.wurstio.languageserver2.requests;

import de.peeeq.wurstio.languageserver.ModelManager;
import de.peeeq.wurstio.languageserver2.BufferManager;
import de.peeeq.wurstscript.WLogger;
import de.peeeq.wurstscript.ast.CompilationUnit;
import de.peeeq.wurstscript.ast.Element;
import de.peeeq.wurstscript.utils.Utils;
import org.eclipse.lsp4j.Hover;
import org.eclipse.lsp4j.TextDocumentPositionParams;
import org.eclipse.lsp4j.jsonrpc.messages.Either;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by peter on 24.04.16.
 */
public class HoverInfo extends UserRequest<Hover> {


	private final String filename;
	private final String buffer;
	private final int line;
	private final int column;

	public HoverInfo(TextDocumentPositionParams position, BufferManager bufferManager) {
		this.filename = position.getTextDocument().getUri();
		this.buffer = bufferManager.getBuffer(position.getTextDocument());
		this.line = position.getPosition().getLine();
		this.column = position.getPosition().getCharacter();
	}

    @Override
	public Hover execute(ModelManager modelManager) {
		CompilationUnit cu = modelManager.replaceCompilationUnitContent(filename, buffer, false);
		Element e = Utils.getAstElementAtPos(cu, line, column, false);
		WLogger.info("hovering over " + Utils.printElement(e));
		return new Hover(Collections.singletonList(Either.forLeft(e.descriptionHtml())));
	}
}
