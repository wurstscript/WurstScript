package de.peeeq.wurstscript.attributes;

import com.google.common.collect.Lists;
import de.peeeq.wurstscript.ast.*;
import org.eclipse.jdt.annotation.Nullable;

import java.util.Collections;
import java.util.List;

public class Flow {


    public static List<WStatement> getNext(ActionStatement s) {
        List<WStatement> r = getFollowingStatements(s);
        setPrevios(s, r);
        return r;
    }

    public static List<WStatement> getNext(StartFunctionStatement s) {
        List<WStatement> r = getFollowingStatements(s);
        setPrevios(s, r);
        return r;
    }


    public static List<WStatement> getNext(LoopStatement s) {
        // we can go to the statement following the loop
        List<WStatement> r;
        if (s instanceof StmtLoop) {
            r = Lists.newArrayList();
        } else if (s instanceof StmtWhile && isConstantBool(((StmtWhile) s).getCond(), true)) {
            r = Lists.newArrayList();
        } else {
            r = getFollowingStatements(s);
        }

        // we can go to the start of the loop
        if (!s.getBody().isEmpty()) {
            r.add(s.getBody().get(0));
        }

        setPrevios(s, r);
        return r;
    }

    public static List<WStatement> getNext(StmtExitwhen s) {
        LoopStatement loop = getParentLoopStatement(s);
        if (loop == null) {
            s.addError("Break statements must be used inside a loop.");
            return Collections.emptyList();
        }
        List<WStatement> next = getFollowingStatements(loop);
        if (isConstantBool(s.getCond(), true)) {
            // always breaks
        } else {
            next.addAll(getFollowingStatements(s));
        }
        setPrevios(s, next);
        return next;
    }

    private static boolean isConstantBool(Expr cond, boolean value) {
        return cond instanceof ExprBoolVal && ((ExprBoolVal) cond).getValB() == value;
    }


    public static List<WStatement> getNext(StmtIf s) {
        List<WStatement> r;
        if (s.getThenBlock().isEmpty() || s.getElseBlock().isEmpty()) {
            r = getFollowingStatements(s);
        } else {
            r = Lists.newArrayList();
        }

        if (!s.getThenBlock().isEmpty()) {
            r.add(s.getThenBlock().get(0));
        }
        if (!s.getElseBlock().isEmpty()) {
            r.add(s.getElseBlock().get(0));
        }

        setPrevios(s, r);
        return r;
    }

    public static List<WStatement> getNext(WBlock s) {
        if (s.getBody().isEmpty()) {
            return s.attrAfterBodyStatements();
        }
        return Collections.singletonList(s.getBody().get(0));
    }

    public static List<WStatement> getNext(StmtReturn s) {
        WStatement endStmt = findEndStatement(s);
        if (endStmt == null) return Collections.emptyList();
        endStmt.attrPreviousStatements().add(s);
        return Collections.singletonList(endStmt);
    }

    private static @Nullable EndFunctionStatement findEndStatement(@Nullable Element n) {
        if (n == null) {
            return null;
        }
        if (n instanceof WStatements) {
            WStatements stmts = (WStatements) n;
            if (!stmts.isEmpty()) {
                WStatement last = stmts.get(stmts.size() - 1);
                if (last instanceof EndFunctionStatement) {
                    return (EndFunctionStatement) last;
                }
            }
        }
        return findEndStatement(n.getParent());
    }

    public static List<WStatement> getNext(SwitchStmt s) {
        List<WStatement> r = Lists.newArrayList();
        boolean following = false;
        for (SwitchCase c : s.getCases()) {
            if (c.getStmts().isEmpty()) {
                following = true;
            } else {
                r.add(c.getStmts().get(0));
            }
        }
        if (s.getSwitchDefault() instanceof SwitchDefaultCaseStatements) {
            SwitchDefaultCaseStatements defaul = (SwitchDefaultCaseStatements) s.getSwitchDefault();
            if (defaul.getStmts().isEmpty()) {
                following = true;
            } else {
                r.add(defaul.getStmts().get(0));
            }
        } else if (!s.calculateHandlesAllCases()) {
            following = true;
        }

        if (following) {
            r.addAll(getFollowingStatements(s));
        }

        setPrevios(s, r);
        return r;
    }

    private static List<WStatement> getFollowingStatements(WStatement s) {
        if (s.getParent() instanceof WStatements) {
            WStatements parent = (WStatements) s.getParent();
            List<WStatement> r = Lists.newArrayList();

            int index = s.attrListIndex();
            if (index + 1 < parent.size()) {
                r.add(parent.get(index + 1));
            } else {
                // at end of statements, next statement depends on parent statement
                CompoundStatement parentStmt = getParentStatement(parent);
                if (parentStmt != null) {
                    r.addAll(parentStmt.attrAfterBodyStatements());
                }
            }
            return r;
        } else {
            return Collections.emptyList();
        }
    }

    private static @Nullable CompoundStatement getParentStatement(Element node) {
        if (node instanceof CompoundStatement) {
            return (CompoundStatement) node;
        } else if (node instanceof WEntity) {
            return null;
        }
        return getParentStatement(node.getParent());
    }

    private static void setPrevios(WStatement s, List<WStatement> next) {
        for (WStatement n : next) {
            n.attrPreviousStatements().add(s);
        }
    }

    public static List<WStatement> getPrevious(WStatement s) {
        return Lists.newArrayListWithCapacity(1);
    }


    private static int lastUsedIndex = 0;

    public static int getListIndex(WStatement s) {
        WStatements parent = (WStatements) s.getParent();
        int r;
        if (get(parent, lastUsedIndex) == s) {
            r = lastUsedIndex;
        } else if (get(parent, lastUsedIndex + 1) == s) {
            r = lastUsedIndex + 1;
        } else if (get(parent, lastUsedIndex - 1) == s) {
            r = lastUsedIndex - 1;
        } else {
            r = parent.indexOf(s);
        }
        lastUsedIndex = r;
        return r;
    }

    private static @Nullable WStatement get(WStatements parent, int i) {
        if (i <= 0) return null;
        if (i >= parent.size()) return null;
        return parent.get(i);
    }

    public static List<WStatement> getAfterBody(LoopStatement loop) {
        List<WStatement> r = Lists.newArrayList();
        // at the end of an loop we can go to the beginning of the loop body...
        r.add(loop);

        // ... or after the loop
//		int index = loop.attrListIndex();
//		WStatements parent = (WStatements) loop.getParent();
//		if (index + 1 < parent.size()) {
//			r.add(parent.get(index+1));
//		}
        r.addAll(getFollowingStatements(loop));
        return r;
    }

    public static List<WStatement> getAfterBody(StmtIf s) {
        return getFollowingStatements(s);
    }

    public static List<WStatement> getAfterBody(WBlock s) {
        return getFollowingStatements(s);
    }

    public static List<WStatement> getAfterBody(SwitchStmt s) {
        return getFollowingStatements(s);
    }

    private static @Nullable LoopStatement getParentLoopStatement(@Nullable Element node) {
        while (node != null) {
            if (node instanceof LoopStatement) {
                return (LoopStatement) node;
            }
            node = node.getParent();
        }
        return null;
    }

    public static List<WStatement> getNext(EndFunctionStatement endFunctionStatement) {
        return Collections.emptyList();
    }


}
