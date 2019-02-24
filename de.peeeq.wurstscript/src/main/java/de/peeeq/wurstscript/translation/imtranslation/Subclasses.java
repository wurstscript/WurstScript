package de.peeeq.wurstscript.translation.imtranslation;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import de.peeeq.wurstscript.attributes.CompileError;
import de.peeeq.wurstscript.jassIm.ImClass;
import de.peeeq.wurstscript.jassIm.ImClassType;
import de.peeeq.wurstscript.jassIm.ImProg;
import de.peeeq.wurstscript.utils.Utils;

import java.util.List;

public class Subclasses {

    public static Multimap<ImClass, ImClass> calculate(ImProg prog) {
        Multimap<ImClass, ImClass> result = ArrayListMultimap.create();

        for (ImClass c : prog.getClasses()) {
            for (ImClassType sct : c.getSuperClasses()) {
                ImClass sc = sct.getClassDef();
                if (sc == c) {
                    throw new CompileError(c.attrTrace().attrSource(),
                            Utils.printElement(c.attrTrace()) + " depends on itself.");
                }
                result.put(sc, c);
            }
        }

        return result;
    }

    public static List<ImClass> get(ImClass c) {
        return Lists.newArrayList(c.attrProg().attrSubclasses().get(c));
    }

}
