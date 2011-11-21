package de.peeeq.wurstscript.attributes;

import java.util.Collection;
import java.util.List;

import de.peeeq.wurstscript.ast.ConstructorDef;
import de.peeeq.wurstscript.ast.ExprFuncRef;
import de.peeeq.wurstscript.ast.ExprFunctionCall;
import de.peeeq.wurstscript.ast.ExprMemberMethod;
import de.peeeq.wurstscript.ast.ExprNewObject;
import de.peeeq.wurstscript.ast.FuncRef;
import de.peeeq.wurstscript.ast.FunctionDefinition;
import de.peeeq.wurstscript.types.PScriptTypeUnknown;
import de.peeeq.wurstscript.types.PscriptType;
import de.peeeq.wurstscript.utils.NotNullList;
import de.peeeq.wurstscript.utils.Utils;

public abstract class OverloadingResolver<F,C> {

	abstract int getParameterCount(F f);
	abstract PscriptType getParameterType(F f, int i);
	abstract int getArgumentCount(C c);
	abstract PscriptType getArgumentType(C c, int i);
	abstract void handleError(List<String> hints);
	
	F resolve(Iterable<F> alternativeFunctions, C caller) {
		List<String> hints = new NotNullList<String>();
		List<F> results = new NotNullList<F>();
		for (F f : alternativeFunctions) {
			if (getParameterCount(f) < getArgumentCount(caller)) {
				hints.add("Expected " + (getParameterCount(f) - getArgumentCount(caller)) + " more argument(s).");
			} else if (getParameterCount(f) > getArgumentCount(caller)) {
				hints.add("Expected " + (getArgumentCount(caller) - getParameterCount(f)) + " less argument(s).");
			} else { // parameter count matches argument count:
				boolean match = true;
				for (int i=0; i<getArgumentCount(caller); i++) {
					if (! getArgumentType(caller, i).isSubtypeOf(getParameterType(f, i))) {
						hints.add("Expected " + getParameterType(f, i)
								 + " as parameter " + i + " but found " +  getArgumentType(caller, i) + "." );
						match = false;
						break;
					}
				}
				if (match) {
					results.add(f);
				}
			}
		}
		if (results.size() == 1) {
			return results.get(0);
		} else if (results.size() == 0) {
			handleError(hints);
			// no method matches so we just choose the first not matching one:
			for (F f : alternativeFunctions) {
				return f;
			}
			handleError(Utils.list("No constructor found."));
			return null;
		} else {
			handleError(Utils.list("call is ambigious"));
			// call is ambigious but we just choose the first method and continue:
			return results.get(0);
		}
	}
	public static ConstructorDef resolveExprNew(List<ConstructorDef> constructors, final ExprNewObject node) {
		return new OverloadingResolver<ConstructorDef, ExprNewObject>() {

			@Override
			int getParameterCount(ConstructorDef f) {
				return f.getParams().size();
			}

			@Override
			PscriptType getParameterType(ConstructorDef f, int i) {
				return f.getParams().get(i).getTyp().attrTyp();
			}

			@Override
			int getArgumentCount(ExprNewObject c) {
				return c.getArgs().size();
			}

			@Override
			PscriptType getArgumentType(ExprNewObject c, int i) {
				return c.getArgs().get(i).attrTyp();
			}

			@Override
			void handleError(List<String> hints) {
				attr.addError(node.getSource(), "No suitable constructor found. \n" + Utils.join(hints, ", \n"));
			}
		}.resolve(constructors, node);
	}
	
	public static FuncDefInstance resolveFuncCall(final Collection<FuncDefInstance> collection, final FuncRef funcCall) {
		return new OverloadingResolver<FuncDefInstance, FuncRef>() {

			@Override
			int getParameterCount(FuncDefInstance f) {
				return f.getDef().getSignature().getParameters().size();
			}

			@Override
			PscriptType getParameterType(FuncDefInstance f, int i) {
				return f.getDef().getSignature().getParameters().get(i).getTyp().attrTyp();
			}

			@Override
			int getArgumentCount(FuncRef c) {
				return c.match(new FuncRef.Matcher<Integer>() {

					@Override
					public Integer case_ExprFuncRef(ExprFuncRef term)  {
						return 0; // CHECK a funcref should be able to specify the parameter types and count
					}

					@Override
					public Integer case_ExprMemberMethod(ExprMemberMethod term)  {
						return term.getArgs().size(); // we do not count the implicit parameter here
					}

					@Override
					public Integer case_ExprFunctionCall(ExprFunctionCall term)  {
						return term.getArgs().size();
					}
				});
			}

			@Override
			PscriptType getArgumentType(FuncRef c, final int i) {
				return c.match(new FuncRef.Matcher<PscriptType>() {

					@Override
					public PscriptType case_ExprFuncRef(ExprFuncRef term)  {
						return PScriptTypeUnknown.instance(); // CHECK a funcref should be able to specify the parameter types and count
					}

					@Override
					public PscriptType case_ExprMemberMethod(ExprMemberMethod term)  {
						return term.getArgs().get(i).attrTyp(); // the implicit parameter is not necessary for overloading
					}

					@Override
					public PscriptType case_ExprFunctionCall(ExprFunctionCall term)  {
						return term.getArgs().get(i).attrTyp();
					}
				});
			}

			@Override
			void handleError(List<String> hints) {
				attr.addError(funcCall.getSource(), "Could not find the right method to call: \n" + Utils.join(hints, ", \n"));
			}

		}.resolve(collection, funcCall);
	}
	
}
