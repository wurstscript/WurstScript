package tests.wurstscript.tests;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import de.peeeq.wurstio.WurstCompilerJassImpl;
import de.peeeq.wurstscript.RunArgs;
import de.peeeq.wurstscript.ast.*;
import de.peeeq.wurstscript.gui.WurstGuiCliImpl;
import de.peeeq.wurstscript.types.FunctionSignature;
import de.peeeq.wurstscript.types.WurstType;
import de.peeeq.wurstscript.types.WurstTypeBoundTypeParam;
import de.peeeq.wurstscript.utils.Pair;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * generates data for the type search in leps bot
 * <p>
 * (21:26:01) lep: einmal ne liste von (Type, Type) (21:26:10) lep: also die
 * typhirachie (21:26:18) lep: und dann ne liste von signaturen (21:26:38) lep:
 * Sig Name [Type] Type
 */
public class GenerateBotData {

    static class Sig {
        private String name;
        private List<WurstType> paramTypes;
        private WurstType returnType;

        public Sig(String name, List<WurstType> paramTypes, WurstType returnType) {
            super();
            this.name = name;
            this.paramTypes = paramTypes;
            this.returnType = returnType;
        }

        public Sig(String name, FunctionSignature sig) {
            this.name = name;
            this.paramTypes = new ArrayList<>(sig.getParamTypes());
            if (sig.getReceiverType() != null) {
                paramTypes.add(0, sig.getReceiverType());
                this.name = sig.getReceiverType() + "." + name;
            }
            this.returnType = sig.getReturnType();
        }

        @Override
        public String toString() {
            return "Sig \"" + name + "\"" + " ["
                    + paramTypes.stream().map(t -> "\"" + t + "\"").collect(Collectors.joining(", ")) + "] \""
                    + returnType + "\"";
        }

        public Stream<WurstType> usedTypes() {
            return Stream.concat(paramTypes.stream(), Stream.of(returnType));
        }

    }

    public static void main(String... args) throws FileNotFoundException {
        System.setOut(new PrintStream(new File("sigs.hs")));

        List<Sig> sigs = new ArrayList<>();
        // List<WurstType> types = new ArrayList<>();
        // Multimap<WurstType, WurstType> superTypes =
        // ArrayListMultimap.create();

        // TODO set config
        RunArgs runArgs = RunArgs.defaults();
        runArgs.addLibs(Sets.newHashSet(StdLib.getLib()));
        WurstCompilerJassImpl comp = new WurstCompilerJassImpl(null, new WurstGuiCliImpl(), null, runArgs);

        comp.loadFiles("common.j");
        comp.loadFiles("blizzard.j");
        comp.loadWurstFilesInDir(new File(StdLib.getLib()));
        WurstModel model = comp.parseFiles();

        model.accept(new WurstModel.DefaultVisitor() {


            @Override
            public void visit(ConstructorDef f) {
                super.visit(f);
                StructureDef struct = f.attrNearestStructureDef();
                assert struct != null; // because constructors can only appear
                // inside a StructureDef

                WurstType returnType = struct.attrTyp().dynamic();
                fj.data.TreeMap<TypeParamDef, WurstTypeBoundTypeParam> binding2 = WurstType.emptyMapping(); // TODO get mapping?
                List<WurstType> paramTypes = Lists.newArrayList();
                for (WParameter p : f.getParameters()) {
                    paramTypes.add(p.attrTyp().setTypeArgs(binding2));
                }
                returnType = returnType.setTypeArgs(binding2);
                // List<String> pNames =
                // FunctionSignature.getParamNames(f.getParameters());
                sigs.add(new Sig("new " + f.attrNearestClassOrModule().getName(), paramTypes, returnType));
            }

            @Override
            public void visit(ExtensionFuncDef f) {
                super.visit(f);
                sigs.add(new Sig(f.getName(), FunctionSignature.forFunctionDefinition(f)));
            }

            @Override
            public void visit(FuncDef f) {
                super.visit(f);
                sigs.add(new Sig(f.getName(), FunctionSignature.forFunctionDefinition(f)));
            }

            @Override
            public void visit(TupleDef f) {
                super.visit(f);
                sigs.add(new Sig(f.getName(), FunctionSignature.forFunctionDefinition(f)));
            }

            @Override
            public void visit(NativeFunc f) {
                super.visit(f);
                sigs.add(new Sig(f.getName(), FunctionSignature.forFunctionDefinition(f)));
            }

        });


        System.out.println("sigs = [");
        for (Sig sig : sigs) {
            System.out.print("    ");
            System.out.print(sig);
            if (sigs != sigs.get(sigs.size() - 1)) {
                System.out.println(",");
            }
        }
        System.out.println("]");

        List<WurstType> types = new ArrayList<>();
        // collect all types used in signatures
        sigs.stream().flatMap(Sig::usedTypes).forEach(t -> {
            if (!types.stream().anyMatch(t2 -> t.equalsType(t2, model))) {
                types.add(t);
            }
        });

        types.sort(Comparator.comparing(Object::toString));

//		for (WurstType wurstType : types) {
//			System.out.println("type " + wurstType);			
//		}

        List<Pair<String, String>> superTypes = new ArrayList<>();
        for (WurstType t1 : types) {
            for (WurstType t2 : types) {
                if (t1 != t2 && t1.isSubtypeOf(t2, model)) {
                    superTypes.add(Pair.create(t1.toString(), t2.toString()));
                }
            }

        }
        System.out.println("typeRelation = [");
        boolean first = true;
        for (Pair<String, String> st : superTypes) {
            if (!first) {
                System.out.println(",");
            }
            System.out.print("    ");
            System.out.print("(\"" + st.getA() + "\", \"" + st.getB() + "\")");
            first = false;
        }
        System.out.println("]");

    }

}
