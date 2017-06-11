package de.peeeq.wurstio.languageserver.requests;

import de.peeeq.wurstio.languageserver.ModelManager;
import de.peeeq.wurstscript.WLogger;
import de.peeeq.wurstscript.ast.Element;
import de.peeeq.wurstscript.ast.CompilationUnit;
import de.peeeq.wurstscript.utils.Utils;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by peter on 24.04.16.
 */
public class HoverInfo extends UserRequest {


	private final String filename;
	private final String buffer;
	private final int line;
	private final int column;

	public HoverInfo(int requestNr, String filename, String buffer, int line, int column) {
		super(requestNr);
		this.filename = filename;
		this.buffer = buffer;
		this.line = line;
		this.column = column;
	}

	@Override
	public Object execute(ModelManager modelManager) {
		CompilationUnit cu = modelManager.replaceCompilationUnitContent(filename, buffer, false);
		Element e = Utils.getAstElementAtPos(cu, line, column, false);
		WLogger.info("hovering over " + Utils.printElement(e));
		Map<String, Object> result = new HashMap<>();
		result.put("documentation", e.descriptionHtml());
		return result;
	}
}
