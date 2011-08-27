package de.peeeq.wurstscript.attributes;

import katja.common.NE;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

import de.peeeq.wurstscript.ast.ClassDefPos;
import de.peeeq.wurstscript.ast.CompilationUnitPos;
import de.peeeq.wurstscript.ast.FuncDefPos;
import de.peeeq.wurstscript.ast.GlobalVarDefPos;
import de.peeeq.wurstscript.ast.InitBlockPos;
import de.peeeq.wurstscript.ast.NativeFuncPos;
import de.peeeq.wurstscript.ast.NativeTypePos;
import de.peeeq.wurstscript.ast.TopLevelDeclarationPos;
import de.peeeq.wurstscript.ast.WEntityPos;
import de.peeeq.wurstscript.ast.WImportPos;
import de.peeeq.wurstscript.ast.WPackagePos;


/**
 * this attribute calculates all named elements available inside in a package (including imports)
 */
public class AttrPackageElements extends Attribute<WPackagePos, Multimap<String, WEntityPos>> {


	public AttrPackageElements(Attributes attr) {
		super(attr);
	}

	@Override
	protected Multimap<String, WEntityPos> calculate(WPackagePos node) {
		final Multimap<String, WEntityPos> result = ArrayListMultimap.create();
		
		CompilationUnitPos cu = node.root();
		
		addPackage(result, node);
		// add imported stuff
		for (WImportPos imp : node.imports()) {
			String packageName = imp.packagename().term();
			WPackagePos importedPackage = null;
			for (TopLevelDeclarationPos tl : cu) {
				if (tl instanceof WPackagePos) {
					WPackagePos p = (WPackagePos) tl;
					if (p.name().term().equals(packageName)) {
						importedPackage = p;
						break;
					}
				}
			}
			if (importedPackage == null) {
				attr.addError(imp.source(), "Could not resolve import " + packageName);
			} else {
				addPackage(result, importedPackage);
			}
		}
		
		return result;
	}

	private void addPackage(final Multimap<String, WEntityPos> result, 	WPackagePos node) {
		for (WEntityPos e : node.elements()) {
			e.Switch(new WEntityPos.Switch<Void, NE>() {

				@Override
				public Void CaseNativeTypePos(NativeTypePos term) throws NE {
					result.put(term.name().term(), term);
					return null;
				}

				@Override
				public Void CaseClassDefPos(ClassDefPos term) throws NE {
					result.put(term.name().term(), term);
					return null;
				}

				@Override
				public Void CaseFuncDefPos(FuncDefPos term) throws NE {
					result.put(term.signature().name().term(), term);
					return null;
				}

				@Override
				public Void CaseGlobalVarDefPos(GlobalVarDefPos term) throws NE {
					result.put(term.name().term(), term);
					return null;
				}

				@Override
				public Void CaseInitBlockPos(InitBlockPos term) throws NE {
					return null;
				}

				@Override
				public Void CaseNativeFuncPos(NativeFuncPos term) throws NE {
					result.put(term.signature().name().term(), term);
					return null;
				}
			});
		}
		
	}


}
