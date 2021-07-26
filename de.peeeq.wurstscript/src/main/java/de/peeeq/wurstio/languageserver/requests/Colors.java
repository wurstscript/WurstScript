package de.peeeq.wurstio.languageserver.requests;

import de.peeeq.wurstio.languageserver.Convert;
import de.peeeq.wurstio.languageserver.ModelManager;
import de.peeeq.wurstio.languageserver.WFile;
import de.peeeq.wurstscript.ast.*;
import de.peeeq.wurstscript.utils.Utils;
import org.eclipse.lsp4j.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 */
public class Colors {

    private static final Pattern colorPattern = Pattern.compile("\\|c([0-9a-fA-F]{2})([0-9a-fA-F]{2})([0-9a-fA-F]{2})([0-9a-fA-F]{2})");


    public static class DocumentColorRequest extends UserRequest<List<ColorInformation>> {

        private final TextDocumentIdentifier textDocument;

        public DocumentColorRequest(DocumentColorParams params) {
            textDocument = params.getTextDocument();
        }


        @Override
        public List<ColorInformation> execute(ModelManager modelManager) throws IOException {
            Optional<CompilationUnit> cu = Optional.ofNullable(
                modelManager.getCompilationUnit(WFile.create(textDocument)));
            if (!cu.isPresent()) {
                return Collections.emptyList();
            }

            List<ColorInformation> result = new ArrayList<>();
            cu.get().accept(new Element.DefaultVisitor() {
                @Override
                public void visit(ExprFunctionCall call) {
                    Arguments args = call.getArgs();
                    String funcName = call.getFuncName();
                    if (funcName.equals("color")
                            && args.size() == 3) {
                        if (args.stream()
                                .allMatch(a -> a instanceof ExprIntVal)) {
                            double red = ((ExprIntVal) args.get(0)).getValI() / 255.;
                            double green = ((ExprIntVal) args.get(1)).getValI() / 255.;
                            double blue = ((ExprIntVal) args.get(2)).getValI() / 255.;
                            double alpha = 1.;
                            Color color = new Color(red, green, blue, alpha);
                            result.add(new ColorInformation(Convert.range(call), color));
                            return;
                        }
                    } else if (funcName.equals("colorA")
                            && args.size() == 4) {
                        if (args.stream()
                                .allMatch(a -> a instanceof ExprIntVal)) {
                            double red = ((ExprIntVal) args.get(0)).getValI() / 255.;
                            double green = ((ExprIntVal) args.get(1)).getValI() / 255.;
                            double blue = ((ExprIntVal) args.get(2)).getValI() / 255.;
                            double alpha = ((ExprIntVal) args.get(3)).getValI() / 255.;
                            Color color = new Color(red, green, blue, alpha);
                            result.add(new ColorInformation(Convert.range(call), color));
                            return;
                        }
                    }
                    super.visit(call);
                }

                @Override
                public void visit(ExprStringVal e) {
                    String s = e.getValS();
                    Range loc = Convert.range(e);
                    int line = loc.getStart().getLine();
                    int col = loc.getStart().getCharacter() + 1;
                    collectStringColors(s, result, line, col);
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
        public List<ColorPresentation> execute(ModelManager modelManager) {
            Optional<CompilationUnit> cu = Optional.ofNullable(
                modelManager.getCompilationUnit(WFile.create(textDocument)));
            if (!cu.isPresent()) {
                return Collections.emptyList();
            }
            Optional<Element> elemAtPos = Utils.getAstElementAtPos(cu.get(), range.getStart().getLine() + 1,
                range.getStart().getCharacter() + 2, false);
            if (!elemAtPos.isPresent()) {
                return Collections.emptyList();
            }
            Element elem = elemAtPos.get();

            if (elem instanceof ExprIntVal
                    && Optional.ofNullable(elem.getParent()).isPresent()) {
                elem = elem.getParent();
            }
            if (elem instanceof Arguments
                    && Optional.ofNullable(elem.getParent()).isPresent()) {
                elem = elem.getParent();
            }


            if (elem instanceof ExprFunctionCall) {
                ExprFunctionCall fc = (ExprFunctionCall) elem;
                boolean isColorA = fc.getFuncName().equals("colorA");

                int red = (int) (255 * color.getRed());
                int green = (int) (255 * color.getGreen());
                int blue = (int) (255 * color.getBlue());
                int alpha = (int) (255 * color.getAlpha());

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
            } else if (elem instanceof ExprStringVal) {
                return Collections.singletonList(
                        new ColorPresentation("|c" + toHex(color))
                );
            }
            return Collections.emptyList();
        }
    }

    public static void collectStringColors(String s, List<ColorInformation> result, int line, int col) {
        // PERFORMANCE would probably be better to directly store strings the same way they appear in the source code ...
        s = Utils.escapeStringWithoutQuotes(s);
        Matcher matcher = colorPattern.matcher(s);

        for ( ; matcher.find(); ) {
            double red = Integer.parseInt(matcher.group(2), 16) / 255.;
            double green = Integer.parseInt(matcher.group(3), 16) / 255.;
            double blue = Integer.parseInt(matcher.group(4), 16) / 255.;
            double alpha = Integer.parseInt(matcher.group(1), 16) / 255.;
            Color color = new Color(red, green, blue, alpha);
            Range range = new Range(
                    new Position(line, col + matcher.start(0)),
                    new Position(line, col + matcher.end(0))
            );
            result.add(new ColorInformation(range, color));
        }
    }

    public static String toHex(Color color) {
        String r = String.format("%02x", (int) (color.getRed() * 255));
        String g = String.format("%02x", (int) (color.getGreen() * 255));
        String b = String.format("%02x", (int) (color.getBlue() * 255));
        String a = String.format("%02x", (int) (color.getAlpha() * 255));
        return a + r + g + b;
    }
}
