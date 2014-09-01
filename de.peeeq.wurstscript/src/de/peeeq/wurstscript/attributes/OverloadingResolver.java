package de.peeeq.wurstscript.attributes;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.Nullable;

import com.google.common.collect.Ordering;

import de.peeeq.wurstscript.ast.AstElement;
import de.peeeq.wurstscript.ast.ConstructorDef;
import de.peeeq.wurstscript.ast.ExprFuncRef;
import de.peeeq.wurstscript.ast.ExprFunctionCall;
import de.peeeq.wurstscript.ast.ExprMemberMethodDot;
import de.peeeq.wurstscript.ast.ExprMemberMethodDotDot;
import de.peeeq.wurstscript.ast.ExprNewObject;
import de.peeeq.wurstscript.ast.FuncRef;
import de.peeeq.wurstscript.ast.FunctionDefinition;
import de.peeeq.wurstscript.types.WurstType;
import de.peeeq.wurstscript.types.WurstTypeTypeParam;
import de.peeeq.wurstscript.types.WurstTypeUnknown;
import de.peeeq.wurstscript.utils.NotNullList;
import de.peeeq.wurstscript.utils.Utils;

public abstract class OverloadingResolver<F extends AstElement,C> {

	abstract int getParameterCount(F f);
	abstract WurstType getParameterType(F f, int i);
	abstract int getArgumentCount(C c);
	abstract WurstType getArgumentType(C c, int i);
	abstract void handleError(List<String> hints);
	
	Optional<F> resolve(Iterable<F> alternativeFunctions, C caller) {
		int size = Utils.size(alternativeFunctions);
		if (size == 0) {
			return Optional.empty();
		} if (size == 1) {
			return Optional.of(Utils.getFirst(alternativeFunctions));
		}
		List<String> hints = new NotNullList<String>();
		
		Map<F,Integer> numMatches = new HashMap<>();
		for (F f : alternativeFunctions) {
			int matches = 0;
			for (int i=0; i<getArgumentCount(caller) && i<getParameterCount(f); i++) {
				if (getArgumentType(caller, i) instanceof WurstTypeTypeParam
						&& getParameterType(f, i) instanceof WurstTypeTypeParam) {
					// should be ok!
				} else if (! getArgumentType(caller, i).isSubtypeOf(getParameterType(f, i), f)) {
					hints.add("Expected " + getParameterType(f, i)
							 + " as parameter " + i + " ,but found " +  getArgumentType(caller, i) + "." );
					continue;
				}
				matches++;
			}
			numMatches.put(f, matches);
		}
		// sort by number of matches
		List<F> funcs = Ordering.natural().reverse()
				.onResultOf(numMatches::get)
				.sortedCopy(alternativeFunctions);
		
		int maxMatches = numMatches.get(funcs.get(0));
		funcs.removeIf(f -> numMatches.get(f) < maxMatches);
		
		if (funcs.size() == 1) {
			// exactly one match
			return Optional.of(funcs.get(0));
		}
		
		// if we have several functions matching a prefix, 
		// we have to check if there is a function with the right number of parameters
		
		List<F> rightNumberOfParams = funcs.stream()
				.filter(f -> getParameterCount(f) == getArgumentCount(caller))
				.collect(Collectors.toList());
		if (rightNumberOfParams.size() == 1) {
			return Optional.of(rightNumberOfParams.get(0));
		} else if (rightNumberOfParams.size() > 1) {
			// if there are many funcs with the right number of arguments
			// take thos as the basis for showing errors
			funcs = rightNumberOfParams;
		}
		// there are many alternatives
		String alts = funcs.stream()
				.map((F f) -> {
					if (f instanceof FunctionDefinition) {
						FunctionDefinition functionDefinition = (FunctionDefinition) f;
						FunctionDefinition func = functionDefinition;
						return "function " + func.getName() + " defined in " + 
							"  line " + func.getSource().getLine();
					}
					return Utils.printElementWithSource(f);
				}).collect(Collectors.joining("\n * "));
		handleError(Utils.list("call is ambiguous, there are several alternatives: \n * " + alts));
		
		// return one with least number of args
		return Optional.of(
				Ordering.natural()
					.onResultOf(this::getParameterCount)
					.max(funcs));
	}
	
	Optional<F> resolveOld(Iterable<F> alternativeFunctions, C caller) {
		List<String> hints = new NotNullList<String>();
		List<F> results = new NotNullList<F>();
		if (Utils.size(alternativeFunctions) == 1) {
			return Optional.of(Utils.getFirst(alternativeFunctions));
		}
		for (F f : alternativeFunctions) {
			if (getParameterCount(f) > getArgumentCount(caller)) {
				hints.add("Expected " + (getParameterCount(f) - getArgumentCount(caller)) + " more argument(s).");
			} else if (getParameterCount(f) < getArgumentCount(caller)) {
				hints.add("Expected " + (getArgumentCount(caller) - getParameterCount(f)) + " less argument(s).");
			} else { // parameter count matches argument count:
				boolean match = true;
				for (int i=0; i<getArgumentCount(caller); i++) {
					if (getArgumentType(caller, i) instanceof WurstTypeTypeParam
							&& getParameterType(f, i) instanceof WurstTypeTypeParam) {
						// should be ok!
					} else if (! getArgumentType(caller, i).isSubtypeOf(getParameterType(f, i), f)) {
						hints.add("Expected " + getParameterType(f, i)
								 + " as parameter " + i + " ,but found " +  getArgumentType(caller, i) + "." );
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
			return Optional.of(results.get(0));
		} else if (results.size() == 0) {
			handleError(hints);
			// no method matches so we just choose the first not matching one:
			for (F f : alternativeFunctions) {
				return Optional.of(f);
			}
			handleError(Utils.list("No constructor found."));
			return Optional.empty();
		} else {
			String alts = results.stream()
					.map((F f) -> {
						if (f instanceof FunctionDefinition) {
							FunctionDefinition functionDefinition = (FunctionDefinition) f;
							FunctionDefinition func = functionDefinition;
							return "function " + func.getName() + " defined in " + 
								"  line " + func.getSource().getLine();
						}
						return Utils.printElementWithSource(f);
					}).collect(Collectors.joining("\n * "));
			handleError(Utils.list("call is ambiguous, there are several alternatives: \n * " + alts));
			// call is ambiguous but we just choose the first method and continue:
			return Optional.of(results.get(0));
		}
	}
	public static @Nullable ConstructorDef resolveExprNew(List<ConstructorDef> constructors, final ExprNewObject node) {
		return new OverloadingResolver<ConstructorDef, ExprNewObject>() {

			@Override
			int getParameterCount(ConstructorDef f) {
				return f.getParameters().size();
			}

			@Override
			WurstType getParameterType(ConstructorDef f, int i) {
				return f.getParameters().get(i).getTyp().attrTyp().dynamic();
			}

			@Override
			int getArgumentCount(ExprNewObject c) {
				return c.getArgs().size();
			}

			@Override
			WurstType getArgumentType(ExprNewObject c, int i) {
				return c.getArgs().get(i).attrTyp();
			}

			@Override
			void handleError(List<String> hints) {
				node.addError("No suitable constructor found. \n" + Utils.join(hints, ", \n"));
			}
		}.resolve(constructors, node).orElse(null);
	}
	
	public static @Nullable ConstructorDef resolveSuperCall(List<ConstructorDef> constructors, final ConstructorDef node) {
		return new OverloadingResolver<ConstructorDef, ConstructorDef>() {

			@Override
			int getParameterCount(ConstructorDef f) {
				return f.getParameters().size();
			}

			@Override
			WurstType getParameterType(ConstructorDef f, int i) {
				return f.getParameters().get(i).getTyp().attrTyp().dynamic();
			}

			@Override
			int getArgumentCount(ConstructorDef c) {
				return c.getSuperArgs().size();
			}

			@Override
			WurstType getArgumentType(ConstructorDef c, int i) {
				return c.getSuperArgs().get(i).attrTyp();
			}

			@Override
			void handleError(List<String> hints) {
				node.addError("No suitable constructor found. \n" + Utils.join(hints, ", \n"));
			}
		}.resolve(constructors, node).orElse(null);
	}
	
	
	public static @Nullable FunctionDefinition resolveFuncCall(final Collection<FunctionDefinition> collection, final FuncRef funcCall) {
		return new OverloadingResolver<FunctionDefinition, FuncRef>() {

			@Override
			int getParameterCount(FunctionDefinition f) {
				return f.getParameters().size();
			}

			@Override
			WurstType getParameterType(FunctionDefinition f, int i) {
				return f.getParameters().get(i).getTyp().attrTyp();
			}

			@Override  
			int getArgumentCount(FuncRef c) {
				return c.match(new FuncRef.Matcher<Integer>() {

					
					@Override
					public Integer case_ExprFuncRef(ExprFuncRef term)  {
						return 0; // CHECK a funcref should be able to specify the parameter types and count
					}

					@Override
					public Integer case_ExprMemberMethodDot(ExprMemberMethodDot term)  {
						return term.getArgs().size(); // we do not count the implicit parameter here
					}
					
					@Override
					public Integer case_ExprMemberMethodDotDot(ExprMemberMethodDotDot term)  {
						return term.getArgs().size(); // we do not count the implicit parameter here
					}

					@Override
					public Integer case_ExprFunctionCall(ExprFunctionCall term)  {
						return term.getArgs().size();
					}
				});
			}

			@Override  
			WurstType getArgumentType(FuncRef c, final int i) {
				return c.match(new FuncRef.Matcher<WurstType>() {

					@Override
					public WurstType case_ExprFuncRef(ExprFuncRef term)  {
						return WurstTypeUnknown.instance(); // CHECK a funcref should be able to specify the parameter types and count
					}

					@Override
					public WurstType case_ExprMemberMethodDot(ExprMemberMethodDot term)  {
						return term.getArgs().get(i).attrTyp(); // the implicit parameter is not necessary for overloading
					}

					@Override
					public WurstType case_ExprMemberMethodDotDot(ExprMemberMethodDotDot term) {
						return term.getArgs().get(i).attrTyp(); // the implicit parameter is not necessary for overloading
					}
					
					@Override
					public WurstType case_ExprFunctionCall(ExprFunctionCall term)  {
						return term.getArgs().get(i).attrTyp();
					}

					
				});
			}

			@Override
			void handleError(List<String> hints) {
				funcCall.addError("Could not find the right method to call: \n" + Utils.join(hints, ", \n"));
			}

		}.resolve(collection, funcCall).orElse(null);
	}
	
}
