package editor;

import javax.swing.JTree;
import javax.swing.tree.TreeModel;

public class PackageTree extends JTree {
	
	private PackageTreeModel packageTreeModel;

	public PackageTreeModel getPackageTreeModel() {
		return packageTreeModel;
	}

	public void setPackageTreeModel(PackageTreeModel packageTreeModel) {
		this.packageTreeModel = packageTreeModel;
	}

	public PackageTree() {
		this.packageTreeModel = new PackageTreeModel();
		setModel(packageTreeModel);
	}

}
