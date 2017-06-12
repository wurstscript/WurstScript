package de.peeeq.wurstscript.translation.imoptimizer;

import java.util.Collection;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

import de.peeeq.wurstscript.jassIm.ImProg;
import de.peeeq.wurstscript.jassIm.ImSet;
import de.peeeq.wurstscript.jassIm.ImSetArray;
import de.peeeq.wurstscript.jassIm.ImSetArrayTuple;
import de.peeeq.wurstscript.jassIm.ImSetTuple;
import de.peeeq.wurstscript.jassIm.ImVar;
import de.peeeq.wurstscript.jassIm.ImVarAccess;
import de.peeeq.wurstscript.jassIm.ImVarArrayAccess;
import de.peeeq.wurstscript.jassIm.ImVarRead;
import de.peeeq.wurstscript.jassIm.ImVarWrite;

public class VariableUses {

	public static class Uses {
		final Multimap<ImVar, ImVarRead> reads = ArrayListMultimap.create();
		final Multimap<ImVar, ImVarWrite> writes = ArrayListMultimap.create();
		
		public void addWrite(ImVar v, ImVarWrite w) {
			writes.put(v, w);
		}
		

		public void addRead(ImVar v, ImVarRead r) {
			reads.put(v, r);
		}
	}

	public static Uses calcVarUses(ImProg imProg) {
		final Uses result = new Uses();
		imProg.accept(new ImProg.DefaultVisitor() {
			@Override
			public void visit(ImSet imSet) {
				result.addWrite(imSet.getLeft(), imSet);
			}
			
			@Override
			public void visit(ImSetArray imSet) {
				result.addWrite(imSet.getLeft(), imSet);
			}
			
			@Override
			public void visit(ImSetArrayTuple imSet) {
				result.addWrite(imSet.getLeft(), imSet);
			}
			
			@Override
			public void visit(ImSetTuple imSet) {
				result.addWrite(imSet.getLeft(), imSet);
			}
			
			@Override
			public void visit(ImVarAccess r) {
				result.addRead(r.getVar(), r);
			}
			
			@Override
			public void visit(ImVarArrayAccess r) {
				result.addRead(r.getVar(), r);
			}
		});
		return result;
	}

	public static Collection<ImVarWrite> getVarWrites(ImVar imVar) {
		return imVar.attrProg().attrVariableUses().writes.get(imVar);
	}

	public static Collection<ImVarRead> getVarReads(ImVar imVar) {
		return imVar.attrProg().attrVariableUses().reads.get(imVar);
	}

}
