package de.peeeq.wurstscript.translation.imoptimizer;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import de.peeeq.wurstscript.jassIm.*;

import java.util.Collection;

public class VariableUses {

    public static class Uses {
        final Multimap<ImVar, ImVarRead> reads = ArrayListMultimap.create();
        final Multimap<ImVar, ImVarWrite> writes = ArrayListMultimap.create();

        public void addWrite(ImVar v, ImVarWrite w) {
            writes.put(v, w);
        }


        public void addRead(ImVar v, ImVarRead r) {
            reads.put(v, r);
        }
    }

    public static Uses calcVarUses(ImProg imProg) {
        final Uses result = new Uses();
        imProg.accept(new ImProg.DefaultVisitor() {
            @Override
            public void visit(ImSet imSet) {
                Element.DefaultVisitor thiz = this;

                imSet.getRight().accept(this);
                imSet.getLeft().match(new ImLExpr.MatcherVoid() {
                    @Override
                    public void case_ImVarAccess(ImVarAccess e) {
                        result.addWrite(e.getVar(), imSet);
                    }

                    @Override
                    public void case_ImTupleSelection(ImTupleSelection e) {
                        if (e.getTupleExpr() instanceof ImLExpr) {
                            ((ImLExpr) e.getTupleExpr()).match(this);
                        } else {
                            e.getTupleExpr().accept(thiz);
                        }
                    }

                    @Override
                    public void case_ImVarArrayAccess(ImVarArrayAccess e) {
                        result.addWrite(e.getVar(), imSet);
                        e.getIndexes().accept(thiz);
                    }

                    @Override
                    public void case_ImMemberAccess(ImMemberAccess e) {
                        e.getReceiver().accept(thiz);
                        result.addWrite(e.getVar(), imSet);
                    }
                });
            }


            @Override
            public void visit(ImVarAccess r) {
                super.visit(r);
                result.addRead(r.getVar(), r);
            }

            @Override
            public void visit(ImVarArrayAccess r) {
                super.visit(r);
                result.addRead(r.getVar(), r);
            }
        });
        return result;
    }

    public static Collection<ImVarWrite> getVarWrites(ImVar imVar) {
        return imVar.attrProg().attrVariableUses().writes.get(imVar);
    }

    public static Collection<ImVarRead> getVarReads(ImVar imVar) {
        return imVar.attrProg().attrVariableUses().reads.get(imVar);
    }

}
