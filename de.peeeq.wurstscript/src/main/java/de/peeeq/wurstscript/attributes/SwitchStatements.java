package de.peeeq.wurstscript.attributes;

import de.peeeq.wurstscript.ast.*;
import de.peeeq.wurstscript.types.WurstTypeEnum;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 *
 */
public class SwitchStatements {
    public static boolean handlesAllCases(SwitchStmt switchStmt) {
        if (switchStmt.getSwitchDefault() instanceof SwitchDefaultCaseStatements) {
            return true;
        }
        if (switchStmt.getExpr().attrTyp() instanceof WurstTypeEnum) {
            return !unhandledCasesStream(switchStmt).findAny().isPresent();
        }
        return false;
    }

    public static Stream<String> unhandledCasesStream(SwitchStmt s) {

        if (s.getExpr().attrTyp() instanceof WurstTypeEnum) {
            WurstTypeEnum wurstTypeEnum = (WurstTypeEnum) s.getExpr().attrTyp();
            if (s.getSwitchDefault() instanceof NoDefaultCase) {
                EnumDef def = wurstTypeEnum.getDef();
                if (def == null) {
                    return Stream.empty();
                }
                return def.getMembers()
                        .stream()
                        .filter(e -> s.getCases()
                                .stream()
                                .flatMap(c -> c.getExpressions().stream())
                                .noneMatch(cExpr -> {
                                    if (cExpr instanceof NameRef) {
                                        NameRef exprVarAccess = (NameRef) cExpr;
                                        return exprVarAccess.attrNameDef() == e;
                                    }
                                    return false;
                                }))
                        .map(e -> "Enum member " + e.getName() + " from " + wurstTypeEnum);
            }
        }
        return Stream.empty();
    }

    public static List<String> unhandledCases(SwitchStmt switchStmt) {
        return unhandledCasesStream(switchStmt).collect(Collectors.toList());
    }
}
