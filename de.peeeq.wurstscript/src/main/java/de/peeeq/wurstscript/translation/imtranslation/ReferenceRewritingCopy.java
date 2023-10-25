package de.peeeq.wurstscript.translation.imtranslation;

import com.google.common.base.Preconditions;
import de.peeeq.wurstscript.jassIm.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Copies an IM-Element and corrects references to the new copied elements
 */
public class ReferenceRewritingCopy {

    public static <T extends Element> T copy(T elem) {
        @SuppressWarnings("unchecked")
        T copy = (T) elem.copy();
        Map<Element, Element> oldToNew = calcOldToNew(elem, copy);
        rewriteReferences(copy, oldToNew);
        return copy;
    }

    private static <T extends Element> void rewriteReferences(T copy, Map<Element, Element> oldToNew) {
        copy.accept(new Element.DefaultVisitor() {
            @Override
            public void visit(ImVar e) {
                super.visit(e);
                Element newChild = oldToNew.get(e.getType());
                if (newChild != null) {
                    e.setType((ImType) newChild);
                }
            }

            @Override
            public void visit(ImFunction e) {
                super.visit(e);
                Element newChild = oldToNew.get(e.getReturnType());
                if (newChild != null) {
                    e.setReturnType((ImType) newChild);
                }
            }

            @Override
            public void visit(ImMethod e) {
                super.visit(e);
                Element newChild = oldToNew.get(e.getImplementation());
                if (newChild != null) {
                    e.setImplementation((ImFunction) newChild);
                }
            }


            @Override
            public void visit(ImVarargLoop e) {
                super.visit(e);
                Element newChild = oldToNew.get(e.getLoopVar());
                if (newChild != null) {
                    e.setLoopVar((ImVar) newChild);
                }
            }

            @Override
            public void visit(ImVarAccess e) {
                super.visit(e);
                Element newChild = oldToNew.get(e.getVar());
                if (newChild != null) {
                    e.setVar((ImVar) newChild);
                }
            }

            @Override
            public void visit(ImVarArrayAccess e) {
                super.visit(e);
                Element newChild = oldToNew.get(e.getVar());
                if (newChild != null) {
                    e.setVar((ImVar) newChild);
                }
            }

            @Override
            public void visit(ImMethodCall e) {
                super.visit(e);
                Element newChild = oldToNew.get(e.getMethod());
                if (newChild != null) {
                    e.setMethod((ImMethod) newChild);
                }
            }

            @Override
            public void visit(ImAlloc e) {
                super.visit(e);
                Element newChild = oldToNew.get(e.getClazz());
                if (newChild != null) {
                    e.setClazz((ImClassType) newChild);
                }
            }

            @Override
            public void visit(ImDealloc e) {
                super.visit(e);
                Element newChild = oldToNew.get(e.getClazz());
                if (newChild != null) {
                    e.setClazz((ImClassType) newChild);
                }
            }

            @Override
            public void visit(ImMemberAccess e) {
                super.visit(e);
                Element newChild = oldToNew.get(e.getVar());
                if (newChild != null) {
                    e.setVar((ImVar) newChild);
                }
            }

            @Override
            public void visit(ImInstanceof e) {
                super.visit(e);
                Element newChild = oldToNew.get(e.getClazz());
                if (newChild != null) {
                    e.setClazz((ImClassType) newChild);
                }
            }

            @Override
            public void visit(ImTypeIdOfObj e) {
                super.visit(e);
                Element newChild = oldToNew.get(e.getClazz());
                if (newChild != null) {
                    e.setClazz((ImClassType) newChild);
                }
            }

            @Override
            public void visit(ImTypeIdOfClass e) {
                super.visit(e);
                Element newChild = oldToNew.get(e.getClazz());
                if (newChild != null) {
                    e.setClazz((ImClassType) newChild);
                }
            }

            @Override
            public void visit(ImFunctionCall e) {
                super.visit(e);
                Element newChild = oldToNew.get(e.getFunc());
                if (newChild != null) {
                    e.setFunc((ImFunction) newChild);
                }
            }

            @Override
            public void visit(ImFuncRef e) {
                super.visit(e);
                Element newChild = oldToNew.get(e.getFunc());
                if (newChild != null) {
                    e.setFunc((ImFunction) newChild);
                }
            }

        });
    }

    private static <T extends Element> Map<Element, Element> calcOldToNew(T elem, T copy) {
        HashMap<Element, Element> result = new HashMap<>();
        calcOldToNew(elem, copy, result);
        return result;
    }

    private static <T extends Element> void calcOldToNew(T elem, T copy, HashMap<Element, Element> result) {
        Preconditions.checkArgument(elem.size() == copy.size());
        result.put(elem, copy);
        for (int i = 0; i < elem.size(); i++) {
            calcOldToNew(elem.get(i), copy.get(i), result);
        }
    }

}
