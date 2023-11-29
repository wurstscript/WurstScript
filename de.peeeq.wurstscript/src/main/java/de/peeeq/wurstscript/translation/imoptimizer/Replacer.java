package de.peeeq.wurstscript.translation.imoptimizer;

import de.peeeq.wurstscript.attributes.CompileError;
import de.peeeq.wurstscript.jassIm.Element;

// optimization for replaceBy
// remembers last position of elements with the assumption that the next element
// searched in the same parent will be in a similar position
public class Replacer {
    private int lastPos;

    public void hintPosition(int pos) {
        lastPos = pos;
    }

    public void replace(Element oldElement, Element newElement) {
        if (oldElement == newElement) {
            return;
        }
        Element parent = oldElement.getParent();
        if (parent == null) {
            throw new RuntimeException("Node not attached to tree.");
        }
        replaceInParent(parent, oldElement, newElement);
    }

    public void replaceInParent(Element parent, Element oldElement, Element newElement) {
        if (oldElement == newElement) {
            return;
        }
        int pos = -1;
        for (int j = 0; j < parent.size(); j++) {
            int i = (lastPos + j) % parent.size();
            Element element = parent.get(i);
            if (element == oldElement) {
                pos = i;
                break;
            }
        }
        if (pos < 0) {
            throw new CompileError(parent.attrTrace().attrSource(), "Could not find " + oldElement + " in " + parent);
        }
        Element oldParent = oldElement.getParent();
        parent.set(pos, newElement);
        if (oldParent != parent) {
            // was already moved elsewhere, so restore parent
            oldElement.setParent(oldParent);
        }
        lastPos = pos;
    }
}
