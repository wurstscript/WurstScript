package de.peeeq.eclipsewurstplugin.ui;

import java.io.File;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.ISelectionService;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;

import com.google.common.base.Charsets;
import com.google.common.io.Files;

import de.peeeq.wurstio.mpq.JmpqBasedEditor;
import de.peeeq.wurstio.mpq.MpqEditor;
import de.peeeq.wurstio.objectreader.BinaryDataInputStream;
import de.peeeq.wurstio.objectreader.WCTFile;
import de.peeeq.wurstio.objectreader.WCTFile.CustomTextTrigger;


public class ExtractJurstCommand extends AbstractHandler implements IHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindowChecked(event);
		//get selection service 
		ISelectionService service = window.getSelectionService();
		//get selection
		IStructuredSelection structured = (IStructuredSelection) service
		        .getSelection();
		//get selected file
		IFile file = (IFile) structured.getFirstElement();
		
		BinaryDataInputStream in;
		try {
			MpqEditor mpqEd = new JmpqBasedEditor();
			IPath mpqPath = file.getRawLocation();
			File mpq = mpqPath.toFile();
			System.out.println("mpq = " + mpq);
			System.out.println(mpq.exists());
			File tempWct = mpqEd.extractFile(mpq, "war3map.wct");
			System.out.println("tempwct =  " + tempWct);
			
			in = new BinaryDataInputStream(tempWct, true);
			WCTFile f = WCTFile.fromStream(in);
			
			File wurstFolder = new File(mpq.getParent(), "wurst");
			
			File jurstOut = new File(wurstFolder, "exported");
			
			jurstOut.mkdirs();
			
			
			
			for (CustomTextTrigger tr : f.getTriggers()) {
				String contents = tr.getContents();
				if (contents.isEmpty()) {
					continue;
				}
				
				String packageName = extractPackageName(contents);
				File out = new File(jurstOut, packageName + ".jurst");
				for (int i=1; out.exists(); i++) {
					out = new File(jurstOut, packageName + "_" + i + ".jurst");
				}
				Files.write(contents, out, Charsets.UTF_8);
				System.out.println(tr.getContents());
			}
			
			
		} catch (CoreException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new Error(e);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new Error(e);
		}
		
		return null;
	}

	private static String extractPackageName(String contents) {
		Pattern p = Pattern.compile("(scope|library|package) ([a-zA-Z0-9\\_]+)");
		Matcher m = p.matcher(contents);

		if (m.find()) {
		    return m.group(2);
		}
		
		return "_";
	}

}
