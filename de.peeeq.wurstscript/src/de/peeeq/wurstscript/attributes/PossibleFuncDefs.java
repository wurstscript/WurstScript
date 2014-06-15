package de.peeeq.wurstscript.attributes;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.eclipse.jdt.annotation.Nullable;

import com.google.common.base.Function;
import com.google.common.collect.Lists;

import de.peeeq.wurstscript.WurstOperator;
import de.peeeq.wurstscript.ast.AstElement;
import de.peeeq.wurstscript.ast.Expr;
import de.peeeq.wurstscript.ast.ExprBinary;
import de.peeeq.wurstscript.ast.ExprFuncRef;
import de.peeeq.wurstscript.ast.ExprFunctionCall;
import de.peeeq.wurstscript.ast.ExprMemberMethod;
import de.peeeq.wurstscript.ast.FuncRef;
import de.peeeq.wurstscript.ast.FunctionDefinition;
import de.peeeq.wurstscript.ast.TypeDef;
import de.peeeq.wurstscript.attributes.names.NameLink;
import de.peeeq.wurstscript.attributes.names.Visibility;
import de.peeeq.wurstscript.types.WurstType;
import de.peeeq.wurstscript.types.WurstTypeInt;
import de.peeeq.wurstscript.types.WurstTypeReal;
import de.peeeq.wurstscript.types.WurstTypeString;
import de.peeeq.wurstscript.utils.Utils;

public class PossibleFuncDefs {
	public final static String overloadingPlus = "op_plus";
	public final static String overloadingMinus = "op_minus";
	public final static String overloadingMult = "op_mult";
	public final static String overloadingDiv = "op_divReal";

	public static Collection<FunctionDefinition> calculate(final ExprFuncRef node) {

		Collection<NameLink> funcs;
		if (node.getScopeName().length() > 0) {
			TypeDef typeDef = node.lookupType(node.getScopeName());
			if (typeDef == null) {
				node.addError("Could not find type " + node.getScopeName() + ".");
				return Collections.emptyList();
			}
			WurstType receiverType = typeDef.attrTyp();
			funcs = node.lookupMemberFuncs(receiverType, node.getFuncName());
		} else {
			funcs = node.lookupFuncs(node.getFuncName());
		}
		try {
			funcs = filterInvisible(node.getFuncName(), node, funcs);
		} catch (EarlyReturn e) {
			return Collections.singleton(e.getFunc());
		}
		Collection<FunctionDefinition> result = new ArrayList<>();
		for (NameLink nameLink : funcs) {
			result.add((FunctionDefinition)nameLink.getNameDef());
		}
		return result;
	}


	public static Collection<FunctionDefinition> calculate(ExprBinary node) {
		return getExtensionFunction(node.getLeft(), node.getRight(), node.getOp());
	}

	public static Collection<FunctionDefinition> calculate(final ExprMemberMethod node) {

		Expr left = node.getLeft();
		WurstType leftType = left.attrTyp();
		String funcName = node.getFuncName();

		return searchMemberFunc(node, leftType, funcName);
	}

	public static Collection<FunctionDefinition> calculate(final ExprFunctionCall node) {
		Collection<FunctionDefinition> result = searchFunction(node.getFuncName(), node);
		return result;
	}

	private static Collection<FunctionDefinition> getExtensionFunction(Expr left, Expr right, WurstOperator op) {
		String funcName = op.getOverloadingFuncName();
		if (funcName == null || nativeOperator(left.attrTyp(), right.attrTyp(), left)) {
			return Collections.emptyList();
		}
		return searchMemberFunc(left, left.attrTyp(), funcName);
	}



	/**
	 * checks if operator is a native operator like for 1+2
	 * TODO also check which operator is used?
	 * @param term 
	 */
	private static boolean nativeOperator(WurstType leftType, WurstType rightType, AstElement term) {
		return
			// numeric
			((leftType.isSubtypeOf(WurstTypeInt.instance(), term) || leftType.isSubtypeOf(WurstTypeReal.instance(), term)) 
					&& (rightType.isSubtypeOf(WurstTypeInt.instance(), term) || rightType.isSubtypeOf(WurstTypeReal.instance(), term)))
			// strings
			|| (leftType instanceof WurstTypeString && rightType instanceof WurstTypeString);
	}



	private static Collection<FunctionDefinition> searchFunction(String funcName, @Nullable FuncRef node) {
		if (node == null) {
			return Collections.emptyList();
		}
		Collection<NameLink> funcs1 = node.lookupFuncs(funcName);
		if (funcs1.size() == 0) {
			if (funcName.startsWith("InitTrig_")) {
				// ignore error
				return Collections.emptyList();
			}
			node.addError("Reference to function " + funcName + " could not be resolved.");
			return Collections.emptyList();
		}
		try {
			// filter out the methods which are private somewhere else
			List<NameLink> funcs = filterInvisible(funcName, node, funcs1);
			
			funcs = filterByReceiverType(node, funcName, funcs);
	
			return nameLinksToFunctionDefinition(funcs);
		} catch (EarlyReturn e) {
			return Collections.singleton(e.getFunc());
		}
	}
	
	
	private static Collection<FunctionDefinition> searchMemberFunc(Expr node, WurstType leftType, String funcName) {
		Collection<NameLink> funcs1 = node.lookupMemberFuncs(leftType, funcName);
		if (funcs1.size() == 0) {
			return Collections.emptyList();
		}
		try {
			// filter out the methods which are private somewhere else
			List<NameLink> funcs = filterInvisible(funcName, node, funcs1);

			// chose method with most specific receiver type
			funcs = filterByReceiverType(node, funcName, funcs);
			return nameLinksToFunctionDefinition(funcs);
		} catch (EarlyReturn e) {
			return Collections.singleton(e.getFunc());
		}
	}


	private static List<FunctionDefinition> nameLinksToFunctionDefinition(
			List<NameLink> funcs) {
		return Utils.map(funcs, new Function<NameLink, FunctionDefinition>() {
			@Override
			public FunctionDefinition apply(@SuppressWarnings("null") NameLink nl) {
				return (FunctionDefinition) nl.getNameDef();
			}
			
		});
	}


	private static List<NameLink> filterInvisible(String funcName, AstElement node, Collection<NameLink> funcs1) throws EarlyReturn {
		if (node.attrSource().getFile().equals("<REPL>")) {
			// no filtering of invisible names in repl:
			return Lists.newArrayList(funcs1);
		}
		List<NameLink> funcs2 = Lists.newArrayListWithCapacity(funcs1.size());
		for (NameLink nl : funcs1) {
			if (!(nl.getVisibility() == Visibility.PRIVATE_OTHER
					|| nl.getVisibility() == Visibility.PROTECTED_OTHER)
					&& nl.getNameDef() instanceof FunctionDefinition) {
				funcs2.add(nl);
			}
		}
		
		funcs2 = Utils.removedDuplicates(funcs2);

		if (funcs2.size() == 0) {
			node.addError("Function " + funcName + " is not visible here.");
			throw new EarlyReturn(firstFunc(funcs1));
		} else if (funcs2.size() == 1) {
			throw new EarlyReturn(firstFunc(funcs2));
		}
		return funcs2;
	}


	


	private static List<NameLink> filterByReceiverType(AstElement node,
			String funcName, List<NameLink> funcs2) throws EarlyReturn {
		List<NameLink> funcs3 = Lists.newArrayListWithCapacity(funcs2.size());
		for (NameLink f : funcs2) {
			boolean existsMoreSpecific = false;
			WurstType f_receiverType = f.getReceiverType();
			if (f_receiverType != null) {
				for (NameLink g : funcs2) {
					if (f != g) {
						WurstType g_receiverType = g.getReceiverType();
						if (g_receiverType != null
							&&  g_receiverType.isSubtypeOf(f_receiverType, node)
							&& !g_receiverType.equalsType(f_receiverType, node)) {
							existsMoreSpecific = true;
							break;
						}
					}
				}
			}
			if (!existsMoreSpecific) {
				funcs3.add(f);
			}
		}
		
		if (funcs3.size() == 0) {
			node.addError("Function " + funcName + " dfopsdfmpso.");
			throw new EarlyReturn( firstFunc(funcs2));
		} else if (funcs2.size() == 1) {
			throw new EarlyReturn(firstFunc(funcs3));
		}
		return funcs3;
	}

	private static FunctionDefinition firstFunc(Collection<NameLink> funcs1) {
		NameLink nl = Utils.getFirst(funcs1);
		if (nl.getNameDef() instanceof FunctionDefinition) {
			return (FunctionDefinition) nl.getNameDef();
		}
		throw new Error("Collection of funcs was empty");
	}

}
