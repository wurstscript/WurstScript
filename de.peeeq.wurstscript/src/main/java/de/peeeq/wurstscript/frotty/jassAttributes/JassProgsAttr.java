package de.peeeq.wurstscript.frotty.jassAttributes;

import com.google.common.collect.Maps;
import de.peeeq.wurstscript.jassAst.*;

import java.util.Map;

public class JassProgsAttr {

    private static final Map<String, JassFunction> functionsMap = Maps.newLinkedHashMap();
    private static final Map<String, JassNative> nativesMap = Maps.newLinkedHashMap();
    private static final Map<String, JassVar> globalsMap = Maps.newLinkedHashMap();
    private static final Map<String, JassTypeDef> typeDefsMap = Maps.newLinkedHashMap();
    private static final Map<String, String> extendsMap = Maps.newLinkedHashMap();


    public static void addFunction(JassProgs jassProgs, JassFunction f) {
        functionsMap.put(f.getName(), f);

    }

    public static void addGlobal(JassProgs jassProgs, JassVar v) {
        globalsMap.put(v.getName(), v);

    }

    public static void addNative(JassProgs jassProgs, JassNative n) {
        nativesMap.put(n.getName(), n);

    }

    public static void addTypeDef(JassProgs jassProgs, JassTypeDef t) {
        typeDefsMap.put(t.getName(), t);

    }

    public static JassFunction getFunction(JassProgs jassProgsImpl, String name) {
        return functionsMap.get(name);
    }

    public static JassVar getGlobal(JassProgs jassProgsImpl, String name) {
        // TODO Auto-generated method stub
        return globalsMap.get(name);
    }

    public static JassNative getNative(JassProgs jassProgsImpl, String name) {
        // TODO Auto-generated method stub
        return nativesMap.get(name);
    }

    public static JassTypeDef getTypeDef(JassProgs jassProgsImpl, String name) {
        // TODO Auto-generated method stub
        return typeDefsMap.get(name);
    }

    public static Map<String, String> getExtendsMap(JassProgs jassProgs) {
        for (JassProg prog : jassProgs) {
            for (JassTypeDef f : prog.getDefs()) {

                extendsMap.put(f.getName(), f.getExt());
            }
        }
        return extendsMap;
    }


}
