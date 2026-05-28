package tests.wurstscript.tests;

import de.peeeq.wurstio.intermediateLang.interpreter.CompiletimeNatives;
import de.peeeq.wurstio.intermediateLang.interpreter.ProgramStateIO;
import de.peeeq.wurstio.objectreader.ObjectHelper;
import de.peeeq.wurstscript.ast.Ast;
import de.peeeq.wurstscript.ast.Element;
import de.peeeq.wurstscript.gui.WurstGuiLogger;
import de.peeeq.wurstscript.intermediatelang.ILconstInt;
import de.peeeq.wurstscript.intermediatelang.ILconstString;
import de.peeeq.wurstscript.jassIm.ImProg;
import de.peeeq.wurstscript.jassIm.JassIm;
import net.moonlightflower.wc3libs.bin.ObjMod;
import net.moonlightflower.wc3libs.bin.app.objMod.W3A;
import net.moonlightflower.wc3libs.bin.app.objMod.W3U;
import net.moonlightflower.wc3libs.dataTypes.DataType;
import net.moonlightflower.wc3libs.dataTypes.app.War3String;
import net.moonlightflower.wc3libs.dataTypes.app.War3Real;
import net.moonlightflower.wc3libs.misc.MetaFieldId;
import net.moonlightflower.wc3libs.misc.ObjId;
import org.testng.annotations.Test;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

public class CompiletimeNativesTest {

    @Test
    public void modifyObjectKeepsDifferentDataPointers() throws Exception {
        CompiletimeNatives natives = new CompiletimeNatives(null, null, false);
        W3A w3a = new W3A();
        W3A.Obj obj = w3a.addObj(ObjId.valueOf("A001"), ObjId.valueOf("Abas"));

        Method modifyObject = CompiletimeNatives.class.getDeclaredMethod(
                "modifyObject",
                ObjMod.Obj.class,
                ILconstString.class,
                ObjMod.ValType.class,
                int.class,
                int.class,
                DataType.class);
        modifyObject.setAccessible(true);

        MetaFieldId unrealId = MetaFieldId.valueOf("unat");
        modifyObject.invoke(natives, obj, new ILconstString(unrealId.getVal()), ObjMod.ValType.UNREAL, 1, 0, War3Real.valueOf(1.0));
        modifyObject.invoke(natives, obj, new ILconstString(unrealId.getVal()), ObjMod.ValType.UNREAL, 1, 1, War3Real.valueOf(2.0));

        List<ObjMod.Obj.ExtendedMod> unrealMods = obj.getMods().stream()
                .filter(m -> m instanceof ObjMod.Obj.ExtendedMod)
                .map(m -> (ObjMod.Obj.ExtendedMod) m)
                .filter(m -> unrealId.equals(m.getId()) && m.getLevel() == 1)
                .collect(Collectors.toList());

        assertEquals(unrealMods.size(), 2, "Mods with distinct data pointers should both be kept");
        Set<Integer> dataPointers = unrealMods.stream()
                .map(ObjMod.Obj.ExtendedMod::getDataPt)
                .collect(Collectors.toSet());
        assertTrue(dataPointers.contains(0));
        assertTrue(dataPointers.contains(1));
    }

    @Test
    public void sameIdObjectDefinitionUsesOriginalTable() throws Exception {
        CompiletimeNatives natives = new CompiletimeNatives(null, null, false);
        W3U w3u = new W3U();

        Method newDefFromFiletype = CompiletimeNatives.class.getDeclaredMethod(
            "newDefFromFiletype",
            ObjMod.class,
            int.class,
            int.class,
            boolean.class
        );
        newDefFromFiletype.setAccessible(true);

        int hfoo = ObjectHelper.objectIdStringToInt("hfoo");
        ObjMod.Obj obj = (ObjMod.Obj) newDefFromFiletype.invoke(natives, w3u, hfoo, hfoo, true);

        assertEquals(obj.getId().getVal(), "hfoo");
        assertEquals(obj.getBaseId(), null, "Melee overwrite should be written as original-table mod");
        assertEquals(obj.getNewId(), null, "Melee overwrite should not create a custom/new id");
        assertEquals(w3u.getOrigObjs().size(), 1);
        assertEquals(w3u.getCustomObjs().size(), 0);
    }

    @Test
    public void differentIdsObjectDefinitionUsesCustomTable() throws Exception {
        CompiletimeNatives natives = new CompiletimeNatives(null, null, false);
        W3U w3u = new W3U();

        Method newDefFromFiletype = CompiletimeNatives.class.getDeclaredMethod(
            "newDefFromFiletype",
            ObjMod.class,
            int.class,
            int.class,
            boolean.class
        );
        newDefFromFiletype.setAccessible(true);

        int hfoo = ObjectHelper.objectIdStringToInt("hfoo");
        int hf01 = ObjectHelper.objectIdStringToInt("hf01");
        ObjMod.Obj obj = (ObjMod.Obj) newDefFromFiletype.invoke(natives, w3u, hfoo, hf01, false);

        assertEquals(obj.getId().getVal(), "hf01");
        assertEquals(obj.getBaseId().getVal(), "hfoo");
        assertEquals(obj.getNewId().getVal(), "hf01");
        assertEquals(w3u.getOrigObjs().size(), 0);
        assertEquals(w3u.getCustomObjs().size(), 1);
    }

    @Test
    public void differentIdsObjectDefinitionOverwritesExistingMapObject() throws Exception {
        CompiletimeNatives natives = new CompiletimeNatives(null, null, false);
        W3U w3u = new W3U();
        int hfoo = ObjectHelper.objectIdStringToInt("hfoo");
        int hf01 = ObjectHelper.objectIdStringToInt("hf01");
        W3U.Obj existing = w3u.addObj(ObjId.valueOf("hf01"), ObjId.valueOf("hpea"));
        existing.addMod(new ObjMod.Obj.Mod(MetaFieldId.valueOf("unam"), ObjMod.ValType.STRING, War3String.valueOf("old map object")));

        Method newDefFromFiletype = CompiletimeNatives.class.getDeclaredMethod(
            "newDefFromFiletype",
            ObjMod.class,
            int.class,
            int.class,
            boolean.class
        );
        newDefFromFiletype.setAccessible(true);

        ObjMod.Obj obj = (ObjMod.Obj) newDefFromFiletype.invoke(natives, w3u, hfoo, hf01, false);

        assertEquals(w3u.getCustomObjs().size(), 1, "Existing map object should be replaced instead of duplicated");
        assertEquals(obj.getId().getVal(), "hf01");
        assertEquals(obj.getBaseId().getVal(), "hfoo");
        assertEquals(obj.getMods().size(), 0, "Overwritten map object mods should not leak into the Wurst-created object");
    }

    @Test
    public void duplicateCodeObjectDefinitionsReportError() {
        WurstGuiLogger gui = new WurstGuiLogger();
        ProgramStateIO state = new ProgramStateIO(Optional.empty(), null, gui, emptyProg(), true);
        CompiletimeNatives natives = new CompiletimeNatives(state, null, false);
        int hfoo = ObjectHelper.objectIdStringToInt("hfoo");
        int hf01 = ObjectHelper.objectIdStringToInt("hf01");

        natives.createObjectDefinition(new ILconstString("w3u"), new ILconstInt(hf01), new ILconstInt(hfoo));
        natives.createObjectDefinition(new ILconstString("w3u"), new ILconstInt(hf01), new ILconstInt(hfoo));

        assertEquals(gui.getErrorCount(), 1);
        assertTrue(gui.getErrors().contains("Object definition with id hf01 is defined more than once."));
    }

    @Test
    public void createObjectDefinitionDoesNotReportExistingMapObjectAsError() throws Exception {
        WurstGuiLogger gui = new WurstGuiLogger();
        ProgramStateIO state = new ProgramStateIO(Optional.empty(), null, gui, emptyProg(), true);
        CompiletimeNatives natives = new CompiletimeNatives(state, null, false);
        int hfoo = ObjectHelper.objectIdStringToInt("hfoo");
        int hf01 = ObjectHelper.objectIdStringToInt("hf01");

        Method getDataStore = ProgramStateIO.class.getDeclaredMethod("getDataStore", String.class);
        getDataStore.setAccessible(true);
        W3U w3u = (W3U) getDataStore.invoke(state, "w3u");
        W3U.Obj existing = w3u.addObj(ObjId.valueOf("hf01"), ObjId.valueOf("hpea"));
        existing.addMod(new ObjMod.Obj.Mod(MetaFieldId.valueOf("unam"), ObjMod.ValType.STRING, War3String.valueOf("old map object")));

        natives.createObjectDefinition(new ILconstString("w3u"), new ILconstInt(hf01), new ILconstInt(hfoo));

        assertEquals(gui.getErrorCount(), 0);
        assertEquals(w3u.getCustomObjs().size(), 1);
        ObjMod.Obj obj = w3u.getCustomObjs().get(0);
        assertEquals(obj.getId().getVal(), "hf01");
        assertEquals(obj.getBaseId().getVal(), "hfoo");
        assertFalse(obj.getMods().stream().anyMatch(m -> m.getId().getVal().equals("unam")));
    }

    private ImProg emptyProg() {
        Element trace = Ast.NoExpr();
        return JassIm.ImProg(trace, JassIm.ImVars(), JassIm.ImFunctions(), JassIm.ImMethods(), JassIm.ImClasses(), JassIm.ImTypeClassFuncs(), new HashMap<>());
    }
}
