package de.peeeq.wurstscript.translation.imtranslation;

import de.peeeq.wurstscript.ast.Element;
import de.peeeq.wurstscript.jassIm.*;
import de.peeeq.wurstscript.types.TypesHelper;

import java.util.Collections;

public class ClassManagementVars {
    /**
     * array, nextFree[x] is the element which comes next in the queue
     */
    public final ImVar free;

    /**
     * first element of the queue, from here we take new objects
     */
    public final ImVar freeCount;

    /**
     * the maximal index of current objects
     */
    public final ImVar maxIndex;

    /**
     * array, typeId of each object. used for dispatch
     */
    public final ImVar typeId;

    public ClassManagementVars(ImClass repClass, ImTranslator translator) {
        Element tr = repClass.getTrace();
        ImProg prog = translator.getImProg();
        free = JassIm.ImVar(tr, JassIm.ImArrayType(TypesHelper.imInt()), repClass.getName() + "_nextFree", Collections.emptyList());
        prog.getGlobals().add(free);

        freeCount = JassIm.ImVar(tr, TypesHelper.imInt(), repClass.getName() + "_firstFree", Collections.emptyList());
        translator.addGlobalWithInitializerFront(freeCount, JassIm.ImIntVal(0));

        maxIndex = JassIm.ImVar(tr, TypesHelper.imInt(), repClass.getName() + "_maxIndex", Collections.emptyList());
        translator.addGlobalWithInitializerFront(maxIndex, JassIm.ImIntVal(0));

        typeId = JassIm.ImVar(tr, JassIm.ImArrayType(TypesHelper.imInt()), repClass.getName() + "_typeId", Collections.emptyList());
        prog.getGlobals().add(typeId);
    }


}
