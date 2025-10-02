package de.peeeq.wurstscript.intermediatelang.optimizer;

import de.peeeq.wurstscript.attributes.CompileError;
import de.peeeq.wurstscript.jassIm.*;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import org.eclipse.jdt.annotation.Nullable;

import java.util.List;

public class ControlFlowGraph {

    public static final class Node {
        private @Nullable ImStmt stmt;
        private @Nullable String name = null;
        // Use fastutil lists; far less overhead than ArrayList for small lists.
        private final ObjectArrayList<Node> predecessors = new ObjectArrayList<>(2);
        private final ObjectArrayList<Node> successors   = new ObjectArrayList<>(2);

        Node(@Nullable ImStmt stmt) { this.stmt = stmt; }

        public @Nullable ImStmt getStmt() { return stmt; }

        public ObjectArrayList<Node> getPredecessors() { return predecessors; }

        public ObjectArrayList<Node> getSuccessors() { return successors; }

        @Override public String toString() { return name != null ? name : String.valueOf(stmt); }

        Node setName(String name) { this.name = name; return this; }
    }

    // Identity maps: ImStmt/ImIf/ImLoop are AST nodes; identity semantics are correct & faster.
    private final Reference2ObjectOpenHashMap<ImStmt, Node> nodes      = new Reference2ObjectOpenHashMap<>();
    private final Reference2ObjectOpenHashMap<ImIf, Node> ifEnd        = new Reference2ObjectOpenHashMap<>();
    private final Reference2ObjectOpenHashMap<ImLoop, Node> loopEnd    = new Reference2ObjectOpenHashMap<>();
    private final Reference2ObjectOpenHashMap<ImVarargLoop, Node> varargLoopEnd = new Reference2ObjectOpenHashMap<>();
    private final ObjectArrayList<Node> nodeList = new ObjectArrayList<>();

    public ControlFlowGraph(ImStmts stmts) {
        // a light hint helps the first growth step avoid rehash
        nodes.trim(0);
        buildCfg(stmts);
    }

    private void buildCfg(ImStmts stmts) {
        final int n = stmts.size();
        for (int i = 0; i < n; i++) {
            ImStmt s = stmts.get(i);
            Node current = getNode(s);
            nodeList.add(current);

            if (s instanceof ImLoop) {
                ImLoop imLoop = (ImLoop) s;
                ImStmts body = imLoop.getBody();
                buildCfg(body);
                if (!body.isEmpty()) addSuccessor(current, getNode(body.get(0)));
                Node endloopNode = getEndloopNode(imLoop);
                nodeList.add(endloopNode);
                addAllSuccessors(endloopNode, getSuccessorList(imLoop, i));
            } else if (s instanceof ImVarargLoop) {
                ImVarargLoop l = (ImVarargLoop) s;
                ImStmts body = l.getBody();
                buildCfg(body);
                if (!body.isEmpty()) addSuccessor(current, getNode(body.get(0)));
                Node end = getEndVarargLoopNode(l);
                addSuccessor(current, end);
                nodeList.add(end);
                addAllSuccessors(end, getSuccessorList(l, i));
            } else if (s instanceof ImIf) {
                ImIf imIf = (ImIf) s;
                ImStmts thenBlock = imIf.getThenBlock();
                ImStmts elseBlock = imIf.getElseBlock();
                buildCfg(thenBlock);
                buildCfg(elseBlock);

                if (thenBlock.isEmpty()) {
                    addSuccessor(current, getEndIfNode(imIf));
                } else {
                    addSuccessor(current, getNode(thenBlock.get(0)));
                }
                if (elseBlock.isEmpty()) {
                    if (!thenBlock.isEmpty()) addSuccessor(current, getEndIfNode(imIf));
                } else {
                    addSuccessor(current, getNode(elseBlock.get(0)));
                }

                Node endifNode = getEndIfNode(imIf);
                nodeList.add(endifNode);
                addAllSuccessors(endifNode, getSuccessorList(imIf, i));
            } else {
                addAllSuccessors(current, getSuccessorList(s, i));
            }
        }
    }

    private static void addAllSuccessors(Node from, List<Node> succs) {
        // small, tight loop avoids Stream allocs
        for (int j = 0, m = succs.size(); j < m; j++) {
            Node succ = succs.get(j);
            from.successors.add(succ);
            succ.predecessors.add(from);
        }
    }

    private void addSuccessor(Node current, Node succ) {
        current.successors.add(succ);
        succ.predecessors.add(current);
    }

    private List<Node> getSuccessorList(ImStmt s, int i) {
        // Reuse an ObjectArrayList with tiny expected size (0-2)
        final ObjectArrayList<Node> result = new ObjectArrayList<>(2);

        if (s instanceof ImReturn) {
            return result; // empty
        }

        if (s instanceof ImExitwhen) {
            Element e = s;
            while (true) {
                if (e instanceof ImLoop) {
                    result.add(getEndloopNode((ImLoop) e));
                    break;
                }
                e = e.getParent();
                if (e == null) throw new CompileError(s, "exitwhen outside of loop");
            }
        }

        if (s.getParent() instanceof ImStmts) {
            ImStmts stmts = (ImStmts) s.getParent();
            if (i + 1 < stmts.size()) {
                result.add(getNode(stmts.get(i + 1)));
            } else if (stmts.getParent() instanceof ImStmt) {
                ImStmt par = (ImStmt) stmts.getParent();
                // Successor depends on block container:
                if (par instanceof ImLoop || par instanceof ImVarargLoop) {
                    result.add(getNode(par)); // back-edge to loop header
                } else if (par instanceof ImIf) {
                    result.add(getEndIfNode((ImIf) par));
                } else {
                    throw new Error("unhandled parent block: " + par);
                }
            }
            return result;
        }
        throw new Error("unexpected CFG shape");
    }

    private Node getNode(ImStmt s) {
        Node result = nodes.get(s);
        if (result == null) {
            result = new Node(s);
            nodes.put(s, result);
            // assign display / stmt view for compound statements
            if (s instanceof ImIf) {
                ImIf imIf = (ImIf) s;
                result.setName("if " + imIf.getCondition());
                result.stmt = imIf.getCondition(); // condition is the node "stmt"
            } else if (s instanceof ImLoop) {
                result.setName("loop");
                result.stmt = null;
            } else if (s instanceof ImVarargLoop) {
                result.setName("vararg loop");
                result.stmt = null;
            }
        }
        return result;
    }

    private Node getEndloopNode(ImLoop e) {
        Node n = loopEnd.get(e);
        if (n == null) {
            n = new Node(null).setName("endloop");
            loopEnd.put(e, n);
        }
        return n;
    }

    private Node getEndVarargLoopNode(ImVarargLoop e) {
        Node n = varargLoopEnd.get(e);
        if (n == null) {
            n = new Node(null).setName("endvarargloop");
            varargLoopEnd.put(e, n);
        }
        return n;
    }

    private Node getEndIfNode(ImIf e) {
        Node n = ifEnd.get(e);
        if (n == null) {
            n = new Node(null).setName("endif");
            ifEnd.put(e, n);
        }
        return n;
    }

    public List<Node> getNodes() {
        return nodeList;
    }
}
