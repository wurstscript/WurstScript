package editor;

import java.io.File;
import java.util.List;
import java.util.Map;

import javax.swing.event.ListSelectionEvent;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import editor.PackageTreeModel.PackageTreeElement;

public class PackageTreeModel implements TreeModel {

	Map<File, PackageTreeElement> fileToElem = Maps.newHashMap();
	
	class PackageTreeElement {

		private List<PackageTreeElement> childs = Lists.newArrayList();
		private File file;
		private PackageTreeElement parent;
		
		public PackageTreeElement(PackageTreeElement parent, File child) {
			this.parent = parent;
			fileToElem.put(file, this);
			this.file = file;
		}
		
		public PackageTreeElement getChild(int c) {
			return childs.get(c);
		}

		public int getChildCount() {
			return childs.size();
		}

		public int getIndexOfChild(PackageTreeElement o2) {
			return childs.indexOf(o2);
		}

		public boolean isLeaf() {
			return childs.size() == 0;
		}

		public Object[] getPath() {
			// TODO Auto-generated method stub
			throw new Error("not implemented");
		}
		
	}
	
	
	private List<TreeModelListener> listeners = Lists.newLinkedList();
	private PackageTreeElement root;
	
	@Override
	public void addTreeModelListener(TreeModelListener arg0) {
		listeners.add(arg0);
	}

	@Override
	public Object getChild(Object o, int c) {
		PackageTreeElement elem = (PackageTreeElement) o;
		return elem.getChild(c);
	}

	@Override
	public int getChildCount(Object o) {
		PackageTreeElement elem = (PackageTreeElement) o;
		return elem.getChildCount();
	}

	@Override
	public int getIndexOfChild(Object o1, Object o2) {
		PackageTreeElement elem = (PackageTreeElement) o1;
		return elem.getIndexOfChild((PackageTreeElement) o2);
	}

	@Override
	public Object getRoot() {
		return root;
	}

	@Override
	public boolean isLeaf(Object o) {
		PackageTreeElement elem = (PackageTreeElement) o;
		return elem.isLeaf();
	}

	@Override
	public void removeTreeModelListener(TreeModelListener arg0) {
		listeners.remove(arg0);
	}

	@Override
	public void valueForPathChanged(TreePath arg0, Object arg1) {
		// TODO Auto-generated method stub
		throw new Error("not implemented");
	}
	
	public void addFile(File parent, File child) {
		PackageTreeElement parentElem = fileToElem.get(parent);
		PackageTreeElement inserted = new PackageTreeElement(parentElem, child);
		if (parentElem == null) {
			root = inserted;
		} else {
			
		}
		listeners.get(0).treeNodesInserted(new TreeModelEvent(root, root.getPath()));
	}

}
