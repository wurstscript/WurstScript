package de.peeeq.eclipsewurstplugin.editor;

import org.eclipse.jface.text.DefaultInformationControl;
import org.eclipse.jface.text.IInformationControl;
import org.eclipse.jface.text.IInformationControlCreator;
import org.eclipse.swt.widgets.Shell;

public class WurstInformationControlCreator implements
		IInformationControlCreator {

	public WurstInformationControlCreator(WurstEditor editor) {
		// TODO Auto-generated constructor stub
	}

	@Override
	public IInformationControl createInformationControl(Shell parent) {
		// TODO Auto-generated method stub
		DefaultInformationControl ic = new DefaultInformationControl(parent);
		return ic;
	}

}
