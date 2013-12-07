package de.peeeq.eclipsewurstplugin.launch;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

public class LaunchConfigMainTab extends AbstractLaunchConfigurationTab {

	private Text mapFile;
	private Text project;

	@Override
	public void createControl(Composite parent) {
		Composite comp = new Composite(parent, SWT.NONE);
		setControl(comp);
		comp.setLayout(new GridLayout(2, true));
		comp.setFont(parent.getFont());
		
		GridData gridData = new GridData();
		gridData.horizontalAlignment = SWT.FILL;
		gridData.horizontalSpan = 2;
		gridData.grabExcessHorizontalSpace = true;
		gridData.verticalAlignment = SWT.FILL;
		
		Group g = new Group(comp, SWT.NONE);
		g.setLayoutData(gridData);
		GridLayout layout = new GridLayout(2, false);
		g.setLayout(layout);
		
		g.setText("Wurst Launch Config");

		Label t = new Label(g, SWT.WRAP);
		t.setText("General launch options...");
		t.setLayoutData(gridData);
		
		gridData = new GridData();
		gridData.horizontalAlignment = SWT.FILL;
		gridData.horizontalSpan = 1;
		gridData.grabExcessHorizontalSpace = true;
		gridData.verticalAlignment = SWT.FILL;
		{
			Label l = new Label(g, SWT.NONE);
			l.setText("Mapfile:");
//			l.setLayoutData(gridData);
			mapFile = new Text(g, SWT.NONE);
			mapFile.setLayoutData(gridData);
		}
		{
			Label l = new Label(g, SWT.NONE);
			l.setText("Project:");
//			l.setLayoutData(gridData);
			project = new Text(g, SWT.NONE);
			project.setLayoutData(gridData);
		}
		
	}

	@Override
	public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {
		
	}

	@Override
	public void initializeFrom(ILaunchConfiguration c) {
		try {
			mapFile.setText(c.getAttribute(LaunchConstants.MAP_FILE, ""));
			project.setText(c.getAttribute(LaunchConstants.PROJECT_NAME, ""));
		} catch (CoreException e) {
			e.printStackTrace();
		}
		
	}

	@Override
	public void performApply(ILaunchConfigurationWorkingCopy c) {
		c.setAttribute(LaunchConstants.MAP_FILE, mapFile.getText());
		c.setAttribute(LaunchConstants.PROJECT_NAME, project.getText());
	}

	@Override
	public String getName() {
		return "Wurst";
	}


}
