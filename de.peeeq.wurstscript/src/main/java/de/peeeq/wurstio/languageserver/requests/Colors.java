package de.peeeq.wurstio.languageserver.requests;

import de.peeeq.wurstio.languageserver.Convert;
import de.peeeq.wurstio.languageserver.ModelManager;
import de.peeeq.wurstio.languageserver.WFile;
import de.peeeq.wurstscript.ast.*;
import de.peeeq.wurstscript.utils.Utils;
import org.eclipse.lsp4j.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 *
 */
public class Colors {

    public static class DocumentColorRequest extends UserRequest<List<ColorInformation>> {

        private final TextDocumentIdentifier textDocument;

        public DocumentColorRequest(DocumentColorParams params) {
            textDocument = params.getTextDocument();
        }


        @Override
        public List<ColorInformation> execute(ModelManager modelManager) throws IOException {
            CompilationUnit cu = modelManager.getCompilationUnit(WFile.create(textDocument));
            List<ColorInformation> result = new ArrayList<>();
            cu.accept(new Element.DefaultVisitor() {
                @Override
                public void visit(ExprFunctionCall call) {
                    Arguments args = call.getArgs();
                    String funcName = call.getFuncName();
                    if (funcName.equals("color")
                            && args.size() == 3) {
                        if (args.stream()
                                .allMatch(a -> a instanceof ExprIntVal)) {
                            double red = ((ExprIntVal) args.get(0)).getValI()/255.;
                            double green = ((ExprIntVal) args.get(1)).getValI()/255.;
                            double blue = ((ExprIntVal) args.get(2)).getValI()/255.;
                            double alpha = 1.;
                            Color color = new Color(red, green, blue, alpha);
                            result.add(new ColorInformation(Convert.range(call), color));
                            return;
                        }
                    } else if (funcName.equals("colorA")
                            && args.size() == 4) {
                        if (args.stream()
                                .allMatch(a -> a instanceof ExprIntVal)) {
                            double red = ((ExprIntVal) args.get(0)).getValI()/255.;
                            double green = ((ExprIntVal) args.get(1)).getValI()/255.;
                            double blue = ((ExprIntVal) args.get(2)).getValI()/255.;
                            double alpha = ((ExprIntVal) args.get(3)).getValI()/255.;
                            Color color = new Color(red, green, blue, alpha);
                            result.add(new ColorInformation(Convert.range(call), color));
                            return;
                        }
                    }
                    super.visit(call);
                }
            });
            return result;
        }
    }

    public static class ColorPresentationRequest extends UserRequest<List<ColorPresentation>> {

        private final Color color;
        private final Range range;
        private final TextDocumentIdentifier textDocument;

        public ColorPresentationRequest(ColorPresentationParams params) {
            color = params.getColor();
            range = params.getRange();
            textDocument = params.getTextDocument();
        }

        @Override
        public List<ColorPresentation> execute(ModelManager modelManager) throws IOException {
            CompilationUnit cu = modelManager.getCompilationUnit(WFile.create(textDocument));
            Element elem = Utils.getAstElementAtPos(cu, range.getStart().getLine(), range.getStart().getCharacter() + 1, false);

            boolean isColorA = false;
            if (elem instanceof ExprFunctionCall) {
                ExprFunctionCall fc = (ExprFunctionCall) elem;
                isColorA = fc.getFuncName().equals("colorA");
            }

            int red = (int) (255*color.getRed());
            int green = (int) (255*color.getGreen());
            int blue = (int) (255*color.getBlue());
            int alpha = (int) (255*color.getAlpha());

            if (alpha != 255) {
                isColorA = true;
            }

            String label;
            if (isColorA) {
                label = "colorA(" + red + ", " + green + ", " + blue + ", " + alpha + ")";
            } else {
                label = "color(" + red + ", " + green + ", " + blue + ")";
            }

            return Collections.singletonList(
                    new ColorPresentation(label)
            );
        }
    }
}
