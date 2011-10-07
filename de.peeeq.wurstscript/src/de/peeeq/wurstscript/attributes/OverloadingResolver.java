package de.peeeq.wurstscript.attributes;

import java.util.Collection;
import java.util.List;

import katja.common.NE;
import de.peeeq.wurstscript.ast.ConstructorDefPos;
import de.peeeq.wurstscript.ast.ExprFuncRefPos;
import de.peeeq.wurstscript.ast.ExprFunctionCallPos;
import de.peeeq.wurstscript.ast.ExprMemberMethodPos;
import de.peeeq.wurstscript.ast.ExprNewObjectPos;
import de.peeeq.wurstscript.ast.FuncRefPos;
import de.peeeq.wurstscript.ast.FunctionDefinitionPos;
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
			return null;
		} else {
			handleError(Utils.list("call is ambigious"));
			// call is ambigious but we just choose the first method and continue:
			return results.get(0);
		}
	}
	public static ConstructorDefPos resolveExprNew(final Attributes attr, List<ConstructorDefPos> constructors, final ExprNewObjectPos node) {
		return new OverloadingResolver<ConstructorDefPos, ExprNewObjectPos>() {

			@Override
			int getParameterCount(ConstructorDefPos f) {
				return f.params().size();
			}

			@Override
			PscriptType getParameterType(ConstructorDefPos f, int i) {
				return attr.typeExprType.get(f.params().get(i).typ());
			}

			@Override
			int getArgumentCount(ExprNewObjectPos c) {
				return c.args().size();
			}

			@Override
			PscriptType getArgumentType(ExprNewObjectPos c, int i) {
				return attr.exprType.get(c.args().get(i));
			}

			@Override
			void handleError(List<String> hints) {
				attr.addError(node.source(), "No suitable constructor found. \n" + Utils.join(hints, ", \n"));
			}
		}.resolve(constructors, node);
	}
	
	public static FunctionDefinitionPos resolveFuncCall(final Attributes attr, final Collection<FunctionDefinitionPos> functions, final FuncRefPos funcCall) {
		return new OverloadingResolver<FunctionDefinitionPos, FuncRefPos>() {

			@Override
			int getParameterCount(FunctionDefinitionPos f) {
				return f.signature().parameters().size();
			}

			@Override
			PscriptType getParameterType(FunctionDefinitionPos f, int i) {
				return attr.typeExprType.get(f.signature().parameters().get(i).typ());
			}

			@Override
			int getArgumentCount(FuncRefPos c) {
				return c.Switch(new FuncRefPos.Switch<Integer, NE>() {

					@Override
					public Integer CaseExprFuncRefPos(ExprFuncRefPos term) throws NE {
						return 0; // CHECK a funcref should be able to specify the parameter types and count
					}

					@Override
					public Integer CaseExprMemberMethodPos(ExprMemberMethodPos term) throws NE {
						return term.args().size(); // we do not count the implicit parameter here
					}

					@Override
					public Integer CaseExprFunctionCallPos(ExprFunctionCallPos term) throws NE {
						return term.args().size();
					}
				});
			}

			@Override
			PscriptType getArgumentType(FuncRefPos c, final int i) {
				return c.Switch(new FuncRefPos.Switch<PscriptType, NE>() {

					@Override
					public PscriptType CaseExprFuncRefPos(ExprFuncRefPos term) throws NE {
						return PScriptTypeUnknown.instance(); // CHECK a funcref should be able to specify the parameter types and count
					}

					@Override
					public PscriptType CaseExprMemberMethodPos(ExprMemberMethodPos term) throws NE {
						return attr.exprType.get(term.args().get(i)); // the implicit parameter is not necessary for overloading
					}

					@Override
					public PscriptType CaseExprFunctionCallPos(ExprFunctionCallPos term) throws NE {
						return attr.exprType.get(term.args().get(i));
					}
				});
			}

			@Override
			void handleError(List<String> hints) {
				attr.addError(funcCall.source(), "Could not find the right method to call: \n" + Utils.join(hints, ", \n"));
			}

		}.resolve(functions, funcCall);
	}
	
}
