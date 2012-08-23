package de.peeeq.wurstscript.translation.imoptimizer;

import com.google.common.collect.Sets;
import static de.peeeq.wurstscript.jassIm.JassIm.ImStatementExpr;
import static de.peeeq.wurstscript.jassIm.JassIm.ImStmts;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import de.peeeq.wurstscript.jassAst.JassProg;
import de.peeeq.wurstscript.jassAst.JassVar;
import de.peeeq.wurstscript.jassAst.JassVars;
import de.peeeq.wurstscript.jassIm.ImExpr;
import de.peeeq.wurstscript.jassIm.ImExprOpt;
import de.peeeq.wurstscript.jassIm.ImFunction;
import de.peeeq.wurstscript.jassIm.ImFunctionCall;
import de.peeeq.wurstscript.jassIm.ImProg;
import de.peeeq.wurstscript.jassIm.ImReturn;
import de.peeeq.wurstscript.jassIm.ImSet;
import de.peeeq.wurstscript.jassIm.ImStatementExpr;
import de.peeeq.wurstscript.jassIm.ImStmt;
import de.peeeq.wurstscript.jassIm.ImStmts;
import de.peeeq.wurstscript.jassIm.ImVar;
import de.peeeq.wurstscript.jassIm.JassIm;
import de.peeeq.wurstscript.jassIm.JassImElement;
import de.peeeq.wurstscript.translation.imtranslation.ImHelper;
import de.peeeq.wurstscript.translation.imtranslation.ImTranslator;
import de.peeeq.wurstscript.utils.Debug;
import de.peeeq.wurstscript.jassIm.*;

public class GlobalsInliner {
	
	private ImTranslator translator;
	private ImProg prog;
	private List<ImVar> globals = Lists.newArrayList();
	private List<ImStmt> setExprs = Lists.newArrayList();
	private Set<ImVar> unused = Sets.newHashSet();
	
	
	ImSet s = null;
	ImSetArray sa = null;
	
	boolean notUsed = true;

	public GlobalsInliner(ImTranslator translator) {
		this.translator = translator;
		this.prog = translator.getImProg();
	}
	
	public void inlineGlobals() {
		collectGlobals();
	}

	private void collectGlobals() {
		for ( final ImVar v : prog.getGlobals() ) {
			prog.getFunctions().accept(new ImProg.DefaultVisitor() {
				
				@Override
				public void visit(ImVarAccess va) {
					String name = va.getVar().getName();
					
					if ( name.equals(v.getName())){
						System.out.println("yes " + v.getName());
						notUsed = false;
					}
				}
				
				@Override
				public void visit(ImSet set) {
					String name = set.getLeft().getName();
					ImFunction f = set.getNearestFunc();
					System.out.println("processing: " + f.getName());
					if ( name.equals(v.getName()) && ! f.getName().substring(0, 5).equals("init_")){
						System.out.println("processing: " + f.getName() + " - not init");
						System.out.println("ye2s " + v.getName());
						notUsed = false;
					}else if ( name.equals(v.getName()) && f.getName().substring(0, 5).equals("init_") ) {
						System.out.println("processing: " + f.getName() + " - init");
						globals.add(v);
						setExprs.add((ImStmt) set);
						notUsed = false;
					}
				}
				
				@Override
				public void visit(ImSetArray set) {
					String name = set.getLeft().getName();
					ImFunction f = set.getNearestFunc();
					System.out.println("processing: " + f.getName());
					if ( name.equals(v.getName()) && f.getName().length() >= 5 && ! f.getName().substring(0, 5).equals("init_")){
						System.out.println("processing: " + f.getName() + " - not init");
						System.out.println("ye2s " + v.getName());
						notUsed = false;
					}else if ( name.equals(v.getName()) && f.getName().length() >= 5 && f.getName().substring(0, 5).equals("init_") ) {
						System.out.println("processing: " + f.getName() + " - init");
						globals.add(v);
						setExprs.add((ImStmt) set);
						notUsed = false;
					}
				}
				
			}); 
			if (notUsed) {
				unused.add(v);
			}else
				notUsed = true;
		}
		prog.getGlobals().removeAll(unused);
		
		for( final ImVar v : unused ) {
			for ( ImFunction func : prog.getFunctions() ) {
				int i = 0;
				ImStmts body = func.getBody();
				next : while ( i < body.size()) {
					JassImElement elem = body.get(i);
					if ( elem instanceof ImSet) {
						ImSet set = (ImSet) elem;
						String name = set.getLeft().getName();
					
						if ( name.equals(v.getName())){
							System.out.println(set);
							body.remove(i);
							System.out.println("removed");
							continue next;
						}
					}
					i++;
				}
				
			}
			

		}

		System.out.println("globals - " + globals.size());
		for ( int i = 0; i < globals.size(); i++) {
			final ImVar v = globals.get(i);
			final ImStmt e = setExprs.get(i);
			if( e instanceof ImSet) {
				s = (ImSet) e;
			} else if ( e instanceof ImSetArray) {
				sa = (ImSetArray) e;
			}
			System.out.println("@: " + v + "  " + s);
			if(!( s == null)) {
				if ( s.getRight() instanceof ImConst ) {
					System.out.println("is constant");
					prog.getFunctions().accept(new ImProg.DefaultVisitor() {
							
						@Override
						public void visit(ImVarAccess va) {
							String name = va.getVar().getName();
								
							if ( name.equals(v.getName() ) ){
								if ( s == null ) {
									replace(va, sa.getRight());
								} else {
									replace(va, s.getRight());	
								}
							}
						}
												
					}); 
				}else {
					globals.remove(i);
					i--;
				}
			}else {
				if ( sa.getRight() instanceof ImConst ) {
					System.out.println("is constant");
					prog.getFunctions().accept(new ImProg.DefaultVisitor() {
							
						@Override
						public void visit(ImVarAccess va) {
							String name = va.getVar().getName();
								
							if ( name.equals(v.getName() ) ){
								if ( s == null ) {
									replace(va, sa.getRight());
								} else {
									replace(va, s.getRight());	
								}
							}
						}
												
					}); 
				}else {
					globals.remove(i);
					i--;
				}
				
			}
			
			
		
		}
		
		
		
		for( final ImVar v : globals ) {
			for ( ImFunction func : prog.getFunctions() ) {
				int i = 0;
				ImStmts body = func.getBody();
				next : while ( i < body.size()) {
					JassImElement elem = body.get(i);
					if ( elem instanceof ImSet) {
						ImSet set = (ImSet) elem;
						String name = set.getLeft().getName();
					
						if ( name.equals(v.getName())){
							System.out.println(set);
							body.remove(i);
							System.out.println("removed");
							continue next;
						}
					}
					i++;
				}
				
			}
			

		}
	}
	
	void replace(JassImElement elem, JassImElement newElem) {
		JassImElement parent = elem.getParent();
		for (int i=0; i<parent.size(); i++) {
			if (parent.get(i) == elem) {
				parent.set(i, ((ImExpr) newElem).copy());
				break;
			}
		}
	}
	
	
}
