package de.peeeq.wurstscript.hotdoc;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;

import com.google.common.base.Charsets;
import com.google.common.collect.Lists;
import com.google.common.io.Files;

import de.peeeq.wurstscript.RunArgs;
import de.peeeq.wurstscript.WurstCompilerJassImpl;
import de.peeeq.wurstscript.WurstConfig;
import de.peeeq.wurstscript.ast.AstElementWithName;
import de.peeeq.wurstscript.ast.ExtensionFuncDef;
import de.peeeq.wurstscript.ast.FuncDef;
import de.peeeq.wurstscript.ast.FunctionDefinition;
import de.peeeq.wurstscript.ast.StructureDef;
import de.peeeq.wurstscript.ast.VarDef;
import de.peeeq.wurstscript.ast.WEntity;
import de.peeeq.wurstscript.ast.WPackage;
import de.peeeq.wurstscript.ast.WParameter;
import de.peeeq.wurstscript.ast.WurstModel;
import de.peeeq.wurstscript.gui.WurstGui;
import de.peeeq.wurstscript.gui.WurstGuiCliImpl;
import de.peeeq.wurstscript.types.WurstTypeVoid;
import de.peeeq.wurstscript.utils.Utils;

public class HotdocGenerator {

	private List<String> files;
	private File outputfolder;
	private String navbar;
	private WurstModel model;
	private ArrayList<WPackage> packages;
	private VelocityEngine ve;
	private Template variableTemplate;
	private Template navbarTemplate;
	private Template structureTemplate;

	public HotdocGenerator(List<String> files) {
		this.files = Lists.newArrayList(files);
		this.outputfolder = new File(this.files.remove(files.size()-1));
	}

	public void generateDoc() {
		try {
			setupVelocity();
			
			System.out.println("Generating hotdoc into " + outputfolder.getAbsolutePath());
			for (String f : files) {
				System.out.println("	input: " + f);
			}
			if (outputfolder.exists()) {
				// clean folder
				for (File f : outputfolder.listFiles()) {
					if (f.getName().endsWith(".html")) {
						f.delete();
					}
				}
			} else {
				if (!outputfolder.mkdirs()) {
					throw new Error("could not create output directory");
				}
			}
			
			RunArgs runArgs = new RunArgs(new String[] {});
			WurstGui gui = new WurstGuiCliImpl();
			WurstCompilerJassImpl compiler = new WurstCompilerJassImpl(gui, runArgs);
			WurstConfig.get().setSetting("lib", "");
			compiler.loadFiles("resources/common.j");
			compiler.loadFiles("resources/blizzard.j");
			for (String file: files) {
				File f = new File(file);
				if (!f.exists()) {
					System.out.println("Folder " + f + " does not exist");
					continue;
				}
				if (f.isDirectory()) {
					compiler.loadWurstFilesInDir(f);
				} else {
					compiler.loadFiles(f);
				}
			}
			model = compiler.parseFiles();
			
			packages = Lists.newArrayList(model.attrPackages().values());
			Collections.sort(packages, new Comparator<WPackage>() {
				@Override
				public int compare(WPackage o1, WPackage o2) {
					return o1.getName().compareTo(o2.getName());
				}
			});
			
			createIndex();
			for (WPackage p : packages) {
				createPackageDoc(p);
			}
		} catch (Throwable t) {
			System.err.println("Error in creating documentation: ");
			t.printStackTrace();
		}
		
	}

	private void createIndex() {
		Template t = ve.getTemplate("resources/hotdoc/document.html");
		
		VelocityContext context = new VelocityContext();
		context.put("title", "HotDoc Wurst Documentation");
		context.put("navbar", getNavbarWithHighlight(null));
		context.put("content", "");
		String s = render(t, context);
		System.out.println( s );     
		// TODO
		try {
			Files.write(render(t, context), new File(outputfolder + "/index.html"), Charsets.UTF_8);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	private void setupVelocity() {
		ve = new VelocityEngine();
		Properties p = new Properties();
		p.setProperty("eventhandler.include.class", "org.apache.velocity.app.event.implement.IncludeRelativePath");
		p.setProperty("runtime.references.strict", "true");
		ve.init(p);
		variableTemplate = ve.getTemplate("resources/hotdoc/var.html");
		navbarTemplate = ve.getTemplate("resources/hotdoc/navbar.html");
		structureTemplate = ve.getTemplate("resources/hotdoc/structure.html");
	}

	private void createPackageDoc(WPackage pack) {
		
        Template t = ve.getTemplate("resources/hotdoc/document.html");
	
		VelocityContext context = new VelocityContext();
		context.put("title", pack.getName() + " HotDoc Wurst Documentation");
		context.put("navbar", getNavbarWithHighlight(pack));
		context.put("content", getPackageContent(pack));
		String s = render(t, context);
		System.out.println( s );     
		// TODO
		try {
			Files.write(render(t, context), new File(outputfolder + "/" + pack.getName() + ".html"), Charsets.UTF_8);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	private String getPackageContent(WPackage pack) {
		Template t = ve.getTemplate("resources/hotdoc/package.html");
		VelocityContext context = new VelocityContext();
		context.put("currentPackage", pack);
		StringWriter writer = new StringWriter();
		t.merge(context, writer);
		
		documentVars(getElements(pack, VarDef.class), writer, false);
		documentFuncs(getElements(pack, FunctionDefinition.class), writer, false);
		documentStructures(pack,writer);
		
		return writer.toString();
	}

	private void documentStructures(WPackage pack, StringWriter writer) {
		for (StructureDef v : sortedByName(getElements(pack, StructureDef.class))) {
			if (!v.attrIsPublic()) {
				continue;
			}
			
			VelocityContext context = new VelocityContext();
			context.put("name", Utils.printElement(v));
			context.put("type", "");
			context.put("comment", v.attrComment());
			
			structureTemplate.merge(context, writer);
			
			documentVars(v.getVars(), writer, true);
			documentFuncs(v.getMethods(), writer, true);
		}
		
	}

	private void documentFuncs(List<? extends FunctionDefinition> funcs, StringWriter writer, boolean includeNonPublic) {
		funcs = sortedByName(funcs);
		for (FunctionDefinition f : funcs) {
			if (!f.attrIsPublic()) {
				if (!includeNonPublic || f.attrIsPrivate() ) {
					continue;
				}
			}
			
			VelocityContext context = new VelocityContext();
			
			StringBuilder descr = new StringBuilder();
			descr.append("function ");
			if (f instanceof ExtensionFuncDef) {
				ExtensionFuncDef ex = (ExtensionFuncDef) f;
				descr.append(ex.getExtendedType().attrTyp());
				descr.append(".");
			}
			descr.append(f.getName());
			descr.append("(");
			boolean first = true;
			for (WParameter p : f.getParameters()) {
				if (!first) {
					descr.append(", ");
				}
				descr.append(p.attrTyp() + " " + p.getName());
				first = false;
			}
			descr.append(")");
			if (f.getReturnTyp().attrTyp() instanceof WurstTypeVoid) {
				
			} else {
				descr.append(" returns ");
				descr.append(f.getReturnTyp().attrTyp());
			}
			
			context.put("name", descr);
			context.put("type", "");
			context.put("comment", f.attrComment());
			
			variableTemplate.merge(context, writer);
		}
	}

	private <T extends AstElementWithName> List<T> sortedByName(List<T> funcs) {
		List<T> result = Lists.newArrayList(funcs);
		Collections.sort(result, new Comparator<T>() {

			@Override
			public int compare(T o1, T o2) {
				return o1.getName().compareTo(o2.getName());
			}
		});
		return result;
	}

	private void documentVars(List<? extends VarDef> vardefs, StringWriter writer, boolean includeNonPublic) {
		for (VarDef v : sortedByName(vardefs)) {
			if (!v.attrIsPublic()) {
				if (!includeNonPublic || v.attrIsPrivate() ) {
					continue;
				}
			}
			
			VelocityContext context = new VelocityContext();
			context.put("name", v.getName());
			context.put("type", v.attrTyp());
			context.put("comment", v.attrComment());
			
			variableTemplate.merge(context, writer);
		}
	}

	private <T> List<T> getElements(WPackage pack, Class<T> clazz) {
		List<T> result = Lists.newArrayList();
		for (WEntity e : pack.getElements()) {
			System.out.println(Utils.printElement(e));
			if (clazz.isAssignableFrom(e.getClass())) {
				@SuppressWarnings("unchecked")
				T t = (T) e;
				result.add(t);
			}
		}
		return result;
	}

	private String render(Template t, VelocityContext context) {
		StringWriter writer = new StringWriter();
        t.merge( context, writer );
        String s = writer.toString();
		return s;
	}

	private String getNavbarWithHighlight(WPackage pack) {
		VelocityContext context = new VelocityContext();
		context.put("packages", packages);
		context.put("currentPackage", pack);
		return render(navbarTemplate, context);
	}


}
