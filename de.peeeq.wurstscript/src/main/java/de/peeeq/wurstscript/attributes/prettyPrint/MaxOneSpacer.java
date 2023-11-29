package de.peeeq.wurstscript.attributes.prettyPrint;

public class MaxOneSpacer implements Spacer {

    public void addSpace(StringBuilder sb) {
        String lastChar = sb.substring(sb.length() - 1);
        if (!lastChar.equals(" ") && !lastChar.equals("(") && !lastChar.equals(".")) {
            sb.append(" ");
        }
    }

}
