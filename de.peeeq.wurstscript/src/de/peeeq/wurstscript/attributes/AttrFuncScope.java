package de.peeeq.wurstscript.attributes;

import katja.common.NE;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

import de.peeeq.wurstscript.ast.ClassDefPos;
import de.peeeq.wurstscript.ast.ClassSlotPos;
import de.peeeq.wurstscript.ast.CompilationUnitPos;
import de.peeeq.wurstscript.ast.FuncDefPos;
import de.peeeq.wurstscript.ast.FunctionDefinitionPos;
import de.peeeq.wurstscript.ast.TopLevelDeclarationPos;
import de.peeeq.wurstscript.ast.WEntityPos;
import de.peeeq.wurstscript.ast.WPackagePos;
import de.peeeq.wurstscript.ast.WScopePos;


/**
 * this attribute calculates the available functions for a given Scope
 * 
 *  note that there can be more than one function of the same name inside one scope
 */
public class AttrFuncScope extends Attribute<WScopePos, Multimap<String, FunctionDefinitionPos>> {


	public AttrFuncScope(Attributes attr) {
		super(attr);
	}

	@Override
	protected Multimap<String, FunctionDefinitionPos> calculate(WScopePos node) {
		final Multimap<String, FunctionDefinitionPos> result = ArrayListMultimap.create();
		return node.Switch(new WScopePos.Switch<Multimap<String, FunctionDefinitionPos>, NE>() {

			@Override
			public Multimap<String, FunctionDefinitionPos> CaseWPackagePos(WPackagePos term)
					throws NE {
				for (WEntityPos e : term.elements()) {
					if (e instanceof FunctionDefinitionPos) {
						FunctionDefinitionPos f = (FunctionDefinitionPos) e;
						result.put(f.signature().name().term(), f);
					}
				}
				return result;
			}

			@Override
			public Multimap<String, FunctionDefinitionPos> CaseClassDefPos(ClassDefPos term)
					throws NE {
				for (ClassSlotPos e : term.slots()) {
					if (e instanceof FunctionDefinitionPos) {
						FunctionDefinitionPos f = (FunctionDefinitionPos) e;
						result.put(f.signature().name().term(), f);
					}
				}
				return result;
			}

			@Override
			public Multimap<String, FunctionDefinitionPos> CaseFuncDefPos(FuncDefPos term)
					throws NE {
				// functions cannot include other functions (not yet?)
				return result;
			}

			@Override
			public Multimap<String, FunctionDefinitionPos> CaseCompilationUnitPos(CompilationUnitPos term) throws NE {
				for (TopLevelDeclarationPos e : term) {
					if (e instanceof FunctionDefinitionPos) {
						FunctionDefinitionPos f = (FunctionDefinitionPos) e;
						result.put(f.signature().name().term(), f);
					}
				}
				return result;
			}
		});
	}


}
