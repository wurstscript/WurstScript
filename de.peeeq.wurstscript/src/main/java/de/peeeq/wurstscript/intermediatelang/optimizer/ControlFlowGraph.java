package de.peeeq.wurstscript.intermediatelang.optimizer;

import de.peeeq.wurstscript.attributes.CompileError;
import de.peeeq.wurstscript.jassIm.*;
import org.eclipse.jdt.annotation.Nullable;

import java.util.*;
import java.util.stream.Stream;

public class ControlFlowGraph {

    static class Node {
        private @Nullable ImStmt stmt;
        private @Nullable String name = null;
        private final List<Node> predecessors = new ArrayList<>();
        private final List<Node> successors = new ArrayList<>();

        public Node(@Nullable ImStmt stmt) {
            this.stmt = stmt;
        }

        public @Nullable ImStmt getStmt() {
            return stmt;
        }

        public List<Node> getPredecessors() {
            return predecessors;
        }

        public List<Node> getSuccessors() {
            return successors;
        }

        @Override
        public String toString() {
            if (name == null) {
                return "" + stmt;
            } else {
                return name;
            }
        }

        public Node setName(String name) {
            this.name = name;
            return this;
        }

    }

    private final Map<ImStmt, Node> nodes = new HashMap<>();
    private final Map<ImIf, Node> ifEnd = new HashMap<>();
    private final Map<ImLoop, Node> loopEnd = new HashMap<>();
    private final Map<ImVarargLoop, Node> varargLoopEnd = new HashMap<>();
    private final List<Node> nodeList = new ArrayList<>();

    public ControlFlowGraph(ImStmts stmts) {
        buildCfg(stmts);
    }

    private void buildCfg(ImStmts stmts) {
        for (int i = 0; i < stmts.size(); i++) {
            ImStmt s = stmts.get(i);
            Node current = getNode(s);
            nodeList.add(current);

            if (s instanceof ImLoop) {
                ImLoop imLoop = (ImLoop) s;
                ImStmts body = imLoop.getBody();
                buildCfg(body);
                if (!body.isEmpty()) {
                    addSuccessor(current, getNode(body.get(0)));
                }
                Node endloopNode = getEndloopNode(imLoop);
                nodeList.add(endloopNode);
                getSuccessors(imLoop, i).forEach(succ -> addSuccessor(endloopNode, succ));
            } else if(s instanceof ImVarargLoop) {
                ImVarargLoop imVarargLoop = (ImVarargLoop) s;
                ImStmts body = imVarargLoop.getBody();
                buildCfg(body);
                if(!body.isEmpty()) {
                    addSuccessor(current, getNode(body.get(0)));
                }
                Node endloopNode = getEndVarargLoopNode(imVarargLoop);
                addSuccessor(current, endloopNode);
                nodeList.add(endloopNode);
                getSuccessors(imVarargLoop, i).forEach(succ -> addSuccessor(endloopNode, succ));
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
                    if (!thenBlock.isEmpty()) {
                        addSuccessor(current, getEndIfNode(imIf));
                    }
                } else {
                    addSuccessor(current, getNode(elseBlock.get(0)));
                }
                Node endifNode = getEndIfNode(imIf);
                nodeList.add(endifNode);
                getSuccessors(imIf, i).forEach(succ -> addSuccessor(endifNode, succ));
            } else {
                getSuccessors(s, i).forEach(succ -> addSuccessor(current, succ));
            }
        }
    }

    private void addSuccessor(Node current, Node succ) {
        current.successors.add(succ);
        succ.predecessors.add(current);
    }

    private Stream<Node> getSuccessors(ImStmt s, int i) {
        return getSuccessorList(s, i).stream();
    }

    private List<Node> getSuccessorList(ImStmt s, int i) {
        if (s instanceof ImReturn) {
            return Collections.emptyList();
        } else {
            List<Node> result = new ArrayList<>();

            if (s instanceof ImExitwhen) {
                Element e = s;
                for (; ; ) {
                    if (e instanceof ImLoop) {
                        result.add(getEndloopNode((ImLoop) e));
                        break;
                    }
                    e = e.getParent();
                    if (e == null) {
                        throw new CompileError(s, "exitwhen outside of loop");
                    }
                }
            }
            if (s.getParent() instanceof ImStmts) {
                ImStmts stmts = (ImStmts) s.getParent();
                assert stmts != null;
                if (i + 1 < stmts.size()) {
                    result.add(getNode(stmts.get(i + 1)));
                } else if (stmts.getParent() instanceof ImStmt) {
                    ImStmt par = (ImStmt) stmts.getParent();
                    assert par != null;
                    result.addAll(successorsOfBlock(par));
                }
                return result;
            }
            throw new Error("not implemented");
        }
    }

    private List<Node> successorsOfBlock(ImStmt par) throws Error {
        if (par instanceof ImLoop) {
            // successor is beginning of loop
            return Collections.singletonList(getNode(par));
        } else if(par instanceof ImVarargLoop){
            // successor is beginning of loop
            return Collections.singletonList(getNode(par));
        } else if (par instanceof ImIf) {
            // successor is end of if
            return Collections.singletonList(getEndIfNode((ImIf) par));
        } else {
            throw new Error("unhandled case: " + par);
        }
    }

    private Node getNode(ImStmt s) {
        ImStmt stmt = s;
        Node result = getNode(nodes, s, stmt);
        if (stmt instanceof ImIf) {
            ImIf imIf = (ImIf) stmt;
            result.setName("if " + imIf.getCondition());
            // for if statements we only consider the condition as a single
            // statement
            result.stmt = imIf.getCondition();
        } else if (stmt instanceof ImLoop) {
            result.setName("loop");
            result.stmt = null;
        } else if(stmt instanceof ImVarargLoop) {
            result.setName("vararg loop");
            result.stmt = null;
        }
        return result;
    }

    private Node getEndloopNode(ImLoop e) {
        return getNode(loopEnd, e, null).setName("endloop");
    }

    private Node getEndVarargLoopNode(ImVarargLoop e) {
        return getNode(varargLoopEnd, e, null).setName("endvarargloop");
    }

    private Node getEndIfNode(ImIf e) {
        return getNode(ifEnd, e, null).setName("endif");
    }

    private <K> Node getNode(Map<K, Node> cache, K key, @Nullable ImStmt s) {
        return cache.computeIfAbsent(key, k -> new Node(s));
    }

    public List<Node> getNodes() {
        return nodeList;
    }

}
