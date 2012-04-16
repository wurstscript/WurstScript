package de.peeeq.wurstscript.compilationserver;

import java.io.File;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import de.peeeq.wurstscript.ast.CompilationUnit;
import de.peeeq.wurstscript.attributes.CompileError;
import de.peeeq.wurstscript.utils.Pair;

public class CompilationServerImpl implements CompilationServer {

	
	private static class CacheEntry {
		final long lastModified;
		final CompilationUnit compilationUnit;
		public CacheEntry(long lastModified, CompilationUnit compilationUnit) {
			super();
			this.lastModified = lastModified;
			this.compilationUnit = compilationUnit;
		}
	}
	
	private Map<String, CacheEntry> cuCache = Maps.newHashMap();
	
	@Override
	public List<CompileError> compile(List<WInput> inputs, File outFile) {
		List<ResourceInput> resources = Lists.newArrayList();
		for (WInput input : inputs) {
			resources.addAll(input.getCompilationUnits());
		}
		
		List<CompilationUnit> cus = getCompilationUnits(resources);
		
		List<CompileError> r = Lists.newArrayList();
		// TODO
		return r;
	}

	private List<CompilationUnit> getCompilationUnits(	List<ResourceInput> resources) {
		List<CompilationUnit> result = Lists.newArrayList();
		for (ResourceInput r : resources) {
			result.add(getCompilationUnit(r));
		}
		return result ;
	}

	private CompilationUnit getCompilationUnit(ResourceInput r) {
		// check cache:
		CacheEntry cached = cuCache.get(r.getName());
		if (cached != null) {
			// is in cache
			if (r.getLastModified() > cached.lastModified) {
				// cache not up to date
				return reparse(r);
			} else {
				return cached.compilationUnit;
			}
		} else {
			// not in cache
			return reparse(r);
		}
	}

	private CompilationUnit reparse(ResourceInput r) {
		// TODO Auto-generated method stub
		return null;
	}

}
