package de.peeeq.wurstscript.translation.lua.translation;

import de.peeeq.wurstscript.luaAst.*;
import de.peeeq.wurstscript.translation.imoptimizer.NameGenerator;

import java.util.*;
import java.util.regex.Pattern;

/** Assigns final names after Lua lowering has introduced its declarations and resolved virtual slots. */
final class LuaNameCompressor {
    private static final Pattern IDENTIFIER = Pattern.compile("[A-Za-z_][A-Za-z_0-9]*");

    static void compress(LuaCompilationUnit model, Set<String> preserved,
                         Set<LuaVariable> descriptors, Set<String> dispatchSlots,
                         Set<LuaExprFieldAccess> dispatchTargets) {
        Set<String> reserved = LuaReservedNames.all();
        reserved.addAll(preserved);
        reserved.add("...");
        Set<LuaVariable> variables = new LinkedHashSet<>();
        Set<LuaFunction> functions = new LinkedHashSet<>();
        Set<LuaMethod> methods = new LinkedHashSet<>();
        List<LuaExprFieldAccess> slots = new ArrayList<>();

        model.accept(new Element.DefaultVisitor() {
            @Override public void visit(LuaVariable variable) {
                variables.add(variable);
                super.visit(variable);
            }

            @Override public void visit(LuaFunction function) {
                functions.add(function);
                super.visit(function);
            }

            @Override public void visit(LuaMethod method) {
                methods.add(method);
                super.visit(method);
            }

            @Override public void visit(LuaFor loop) {
                variables.add(loop.getLoopVar());
                super.visit(loop);
            }

            @Override public void visit(LuaAssignment assignment) {
                // Lua globals need no declaration; their bootstrap assignment owns the variable.
                if (assignment.getLeft() instanceof LuaExprVarAccess access) {
                    variables.add(access.getVar());
                }
                super.visit(assignment);
            }

            @Override public void visit(LuaExprFieldAccess access) {
                if (!access.getFieldName().equals("__wurst_destroy")
                    && (dispatchTargets.contains(access)
                        || (access.getReceiver() instanceof LuaExprVarAccess receiver
                            && descriptors.contains(receiver.getVar())
                            && dispatchSlots.contains(access.getFieldName())))) {
                    slots.add(access);
                }
                super.visit(access);
            }

            @Override public void visit(LuaLiteral literal) {
                // Raw Lua has no symbol references. Keep every identifier it can refer to, and
                // keep those names out of the generated pool too (including its local bindings).
                // Never rewrite arbitrary Lua text or user strings by name substitution.
                var matcher = IDENTIFIER.matcher(literal.getLuaCode());
                while (matcher.find()) reserved.add(matcher.group());
            }

            @Override public void visit(LuaExprFunctionCallByName call) {
                reserved.add(call.getFuncName());
                super.visit(call);
            }
        });

        model.accept(new Element.DefaultVisitor() {
            @Override public void visit(LuaExprVarAccess access) {
                if (!variables.contains(access.getVar())) reserved.add(access.getVar().getName());
            }

            @Override public void visit(LuaExprFuncRef reference) {
                if (!functions.contains(reference.getFunc())) reserved.add(reference.getFunc().getName());
            }

            @Override public void visit(LuaExprFunctionCall call) {
                if (!functions.contains(call.getFunc())) reserved.add(call.getFunc().getName());
                super.visit(call);
            }
        });

        NameGenerator names = new NameGenerator();
        for (LuaVariable variable : variables) {
            if (!reserved.contains(variable.getName())) variable.setName(next(names, reserved));
        }
        for (LuaFunction function : functions) {
            if (!reserved.contains(function.getName())) function.setName(next(names, reserved));
        }
        for (LuaMethod method : methods) {
            if (!reserved.contains(method.getName())) method.setName(next(names, reserved));
        }
        Map<String, String> slotNames = new LinkedHashMap<>();
        for (LuaExprFieldAccess slot : slots) {
            slot.setFieldName(slotNames.computeIfAbsent(slot.getFieldName(), ignored -> next(names, reserved)));
        }
    }

    private static String next(NameGenerator names, Set<String> reserved) {
        String name;
        do {
            name = names.getUniqueToken();
        } while (reserved.contains(name));
        return name;
    }

    private LuaNameCompressor() {}
}
