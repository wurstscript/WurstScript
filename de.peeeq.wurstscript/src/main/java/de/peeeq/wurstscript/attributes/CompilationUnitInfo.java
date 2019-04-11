package de.peeeq.wurstscript.attributes;

import de.peeeq.wurstscript.utils.Utils;

/**
 *
 */
public class CompilationUnitInfo {
    private String file = "";
    private de.peeeq.wurstscript.attributes.ErrorHandler cuErrorHandler;
    private IndentationMode indentationMode = IndentationMode.spaces(4);

    public CompilationUnitInfo(ErrorHandler cuErrorHandler) {
        this.cuErrorHandler = cuErrorHandler;
    }

    public String getFile() {
        return file;
    }

    public void setFile(String file) {
        this.file = file;
    }

    public ErrorHandler getCuErrorHandler() {
        return cuErrorHandler;
    }

    public void setCuErrorHandler(ErrorHandler errorHandler) {
        this.cuErrorHandler = errorHandler;
    }

    public IndentationMode getIndentationMode() {
        return indentationMode;
    }

    public void setIndentationMode(IndentationMode indentationMode) {
        this.indentationMode = indentationMode;
    }

    static public interface IndentationMode {

        static IndentationMode tabs() {
            return new IndentationMode() {
                @Override
                public String getIndent() {
                    return "\t";
                }

                @Override
                public int countIndents(int startColumn) {
                    return startColumn;
                }
            };
        }

        static IndentationMode spaces(int num) {
            String indent = Utils.repeat(' ', num);
            return new IndentationMode() {
                @Override
                public String getIndent() {

                    return indent;
                }

                @Override
                public int countIndents(int startColumn) {
                    return startColumn / num;
                }
            };
        }
        String getIndent();

        int countIndents(int startColumn);

        default void appendIndent(StringBuilder sb, int counts) {
            for (int i = 0; i < counts; i++) {
                sb.append(getIndent());
            }

        }
    }


}
