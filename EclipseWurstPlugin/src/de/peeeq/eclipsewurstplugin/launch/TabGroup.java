package de.peeeq.eclipsewurstplugin.launch;

import org.eclipse.debug.ui.AbstractLaunchConfigurationTabGroup;
import org.eclipse.debug.ui.CommonTab;
import org.eclipse.debug.ui.ILaunchConfigurationDialog;
import org.eclipse.debug.ui.ILaunchConfigurationTab;
import org.eclipse.jdt.annotation.Nullable;

public class TabGroup extends AbstractLaunchConfigurationTabGroup {

	@Override
	public void createTabs(@Nullable ILaunchConfigurationDialog dialog, @Nullable String mode) {
		ILaunchConfigurationTab[] tabs = new ILaunchConfigurationTab[] {
				new LaunchConfigMainTab(),
				new CommonTab()
		};
		setTabs(tabs);
		
	}


}
