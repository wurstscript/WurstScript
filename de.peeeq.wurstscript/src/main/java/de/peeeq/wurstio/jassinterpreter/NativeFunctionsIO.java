package de.peeeq.wurstio.jassinterpreter;

import com.google.common.collect.Maps;
import de.peeeq.wurstscript.WLogger;
import de.peeeq.wurstscript.intermediatelang.*;
import de.peeeq.wurstscript.intermediatelang.interpreter.NativesProvider;
import de.peeeq.wurstscript.jassinterpreter.TestFailException;
import de.peeeq.wurstscript.jassinterpreter.TestSuccessException;

import java.util.Map;
import java.util.Random;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * provides native functions which are invoked via reflection a function should
 * only take and return ILconsts
 * <p>
 * remember that all functions must be
 */
@SuppressWarnings("ucd") // ignore unused code detector warnings, because this class uses reflection
public class NativeFunctionsIO extends ReflectionBasedNativeProvider implements NativesProvider {

    private Random r = new Random(0);

    @Native
    public void testSuccess() {
        throw TestSuccessException.instance;
    }

    public void testFail(ILconstString msg) {
        throw new TestFailException(msg.getVal());
    }

    public void BJDebugMsg(ILconstString msg) {
        outStream.println(msg.getVal());
    }

    public void println(ILconstString msg) {
        outStream.println(msg.getVal());
    }

    public void $debugPrint(ILconstString msg) {
        outStream.println(msg.getVal());
        throw new DebugPrintError(msg.getVal());
    }

    public ILconstInt ModuloInteger(ILconstInt a, ILconstInt b) {
        return new ILconstInt(a.getVal() % b.getVal());
    }

    public ILconstReal ModuloReal(ILconstReal a, ILconstReal b) {
        return new ILconstReal(a.getVal() % b.getVal());
    }

    @Native
    public ILconstString I2S(ILconstInt i) {
        return new ILconstString("" + i.getVal());
    }

    @Native
    public ILconstInt S2I(ILconstString s) {
        String str = s.getVal();
        Pattern pattern = Pattern.compile("((\\+|-)?[0-9]+).*");
        Matcher matcher = pattern.matcher(str);
        if (matcher.matches()) {
            MatchResult matchResult = matcher.toMatchResult();
            str = matcher.group(1);
            return new ILconstInt(Integer.parseInt(str));
        } else {
            return new ILconstInt(0);
        }
    }

    @Native
    public ILconstString R2S(ILconstReal r) {
        return new ILconstString("" + r.getVal());
    }

    public ILconstInt R2I(ILconstReal i) {
        return new ILconstInt((int) i.getVal());
    }

    public ILconstReal I2R(ILconstInt i) {
        return new ILconstReal(i.getVal());
    }

    @Native
    public ILconstInt StringHash(ILconstString s) {
        // TODO can we use same string hash function as used in wc3?
        WLogger.info("stringhash of <" + s.getVal() + "> = " + s.getVal().hashCode());
        return new ILconstInt(s.getVal().hashCode());
    }

    @Native
    public ILconstNull Player(ILconstInt p) {
        return ILconstNull.instance();
    }

    @Native
    public ILconstInt GetPlayerId(ILconstNull p) {
        return ILconstInt.create(0);
    }

    public IlConstHandle InitHashtable() {
        return new IlConstHandle(getRandomName("ht"), Maps.newLinkedHashMap());
    }

    public void SaveInteger(IlConstHandle ht, ILconstInt key1, ILconstInt key2, ILconstInt value) {
        @SuppressWarnings("unchecked")
        Map<Integer, Map<Integer, Object>> map = (Map<Integer, Map<Integer, Object>>) ht.getObj();
        Map<Integer, Object> map2 = map.computeIfAbsent(key1.getVal(), k -> Maps.newLinkedHashMap());
        map2.put(key2.getVal(), value);
    }

    public ILconstInt LoadInteger(IlConstHandle ht, ILconstInt key1, ILconstInt key2) {
        @SuppressWarnings("unchecked")
        Map<Integer, Map<Integer, Object>> map = (Map<Integer, Map<Integer, Object>>) ht.getObj();
        Map<Integer, Object> map2 = map.get(key1.getVal());
        if (map2 != null) {
            Object value = map2.get(key2.getVal());
            if (value instanceof ILconstInt) {
                return (ILconstInt) value;
            }
        }
        return ILconstInt.create(0);
    }


    public ILconstInt RemoveSavedInteger(IlConstHandle ht, ILconstInt key1, ILconstInt key2) {
        @SuppressWarnings("unchecked")
        Map<Integer, Map<Integer, Object>> map = (Map<Integer, Map<Integer, Object>>) ht.getObj();
        Map<Integer, Object> map2 = map.get(key1.getVal());
        if (map2 != null) {
            map2.remove(key2.getVal());
        }
        return ILconstInt.create(0);
    }


    public void SaveStr(IlConstHandle ht, ILconstInt key1, ILconstInt key2, ILconstString value) {
        @SuppressWarnings("unchecked")
        Map<Integer, Map<Integer, Object>> map = (Map<Integer, Map<Integer, Object>>) ht.getObj();
        Map<Integer, Object> map2 = map.computeIfAbsent(key1.getVal(), k -> Maps.newLinkedHashMap());
        map2.put(key2.getVal(), value);
        WLogger.info("savestr of key1: " + key1.getVal() + ", key2: " + key2.getVal() + ", val: " + value.getVal());
    }

    public ILconstString LoadStr(IlConstHandle ht, ILconstInt key1, ILconstInt key2) {
        @SuppressWarnings("unchecked")
        Map<Integer, Map<Integer, Object>> map = (Map<Integer, Map<Integer, Object>>) ht.getObj();
        Map<Integer, Object> map2 = map.get(key1.getVal());
        if (map2 != null) {
            Object value = map2.get(key2.getVal());
            if (value instanceof ILconstString) {
                return (ILconstString) value;
            }
        }
        return new ILconstString("");
    }

    public ILconstString RemoveSavedString(IlConstHandle ht, ILconstInt key1, ILconstInt key2) {
        @SuppressWarnings("unchecked")
        Map<Integer, Map<Integer, Object>> map = (Map<Integer, Map<Integer, Object>>) ht.getObj();
        Map<Integer, Object> map2 = map.get(key1.getVal());
        if (map2 != null) {
            map2.remove(key2.getVal());
        }
        return new ILconstString("");
    }

    public ILconstBool HaveSavedString(IlConstHandle ht, ILconstInt key1, ILconstInt key2) {
        @SuppressWarnings("unchecked")
        Map<Integer, Map<Integer, Object>> map = (Map<Integer, Map<Integer, Object>>) ht.getObj();
        Map<Integer, Object> map2 = map.get(key1.getVal());
        if (map2 != null) {
            return ILconstBool.instance(map2.containsKey(key2.getVal()));
        }
        return ILconstBool.FALSE;
    }


    public ILconstInt FlushChildHashtable(IlConstHandle ht, ILconstInt parentKey) {
        Map<Integer, Map<Integer, Object>> map = (Map<Integer, Map<Integer, Object>>) ht.getObj();
        map.remove(parentKey.getVal());
        return ILconstInt.create(0);
    }


    public ILconstReal SquareRoot(ILconstReal r) {
        return new ILconstReal(Math.sqrt(r.getVal()));
    }

    public ILconstReal Pow(ILconstReal x, ILconstReal power) {
        return new ILconstReal(Math.pow(x.getVal(), power.getVal()));
    }

    public ILconstReal Sin(ILconstReal r) {
        return new ILconstReal(Math.sin(r.getVal()));
    }

    public ILconstReal Cos(ILconstReal r) {
        return new ILconstReal(Math.cos(r.getVal()));
    }

    public ILconstReal Tan(ILconstReal r) {
        return new ILconstReal(Math.tan(r.getVal()));
    }

    public ILconstReal ATan(ILconstReal r) {
        return new ILconstReal(Math.atan(r.getVal()));
    }


    public ILconstReal Atan2(ILconstReal y, ILconstReal x) {
        return new ILconstReal(Math.atan2(y.getVal(), x.getVal()));
    }


    public IlConstHandle GetLocalPlayer() {
        return new IlConstHandle("Local Player", "local player");
    }

    private String getRandomName(String string) {
        return string + new Random().nextInt(100);
    }

    public void DisplayTimedTextToPlayer(Object player, ILconstReal x, ILconstReal y, ILconstReal duration,
                                         ILconstString msg) {
        outStream.println(msg.getVal());
    }

    public ILconstInt StringLength(ILconstString string) {
        return new ILconstInt(string.getVal().length());
    }

    @Native
    public ILconstString SubString(ILconstString s, ILconstInt start, ILconstInt end) {
        String str = s.getVal();
        if (start.getVal() < 0) {
            // I am not gonna emulate the WC3 bug for negative indexes here ...
            throw new RuntimeException("SubString called with negative start index: " + start) ;
        }
        int e = end.getVal();
        if (e >= str.length()) {
            // Warcraft does no bound checking here:
            e = str.length();
        }
        return new ILconstString(str.substring(start.getVal(), e));
    }

    public ILconstString StringCase(ILconstString string, ILconstBool upperCase) {
        return new ILconstString(
                upperCase.getVal() ?
                        string.getVal().toUpperCase()
                        : string.getVal().toLowerCase());
    }

    public void testPrint(ILconstString msg) {
        outStream.println(msg.getVal());
    }


    public ILconstReal GetRandomReal(ILconstReal a, ILconstReal b) {
        return new ILconstReal(a.getVal() + r.nextFloat() * (b.getVal() - a.getVal()));
    }

    public ILconstInt GetRandomInt(ILconstInt a, ILconstInt b) {
        return new ILconstInt(a.getVal() + r.nextInt(1 + b.getVal() - a.getVal()));
    }
}
