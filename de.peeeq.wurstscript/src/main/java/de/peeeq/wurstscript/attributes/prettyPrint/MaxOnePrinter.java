package de.peeeq.wurstscript.attributes.prettyPrint;

public class MaxOnePrinter extends Printer {

    @Override
    public void addSpace() {
        if (sb.length() == 0) {
            return;
        }
        String lastChar = sb.substring(sb.length() - 1);
        if (!lastChar.equals(" ") && !lastChar.equals("(") && !lastChar.equals(".")) {
            sb.append(" ");
        }
    }

}
