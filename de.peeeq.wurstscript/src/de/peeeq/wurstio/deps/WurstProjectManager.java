package de.peeeq.wurstio.deps;

import java.io.File;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Deque;
import java.util.List;

import org.eclipse.jgit.api.CheckoutCommand;
import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.api.errors.TransportException;
import org.eclipse.jgit.lib.ProgressMonitor;

import com.google.common.base.Charsets;
import com.google.common.io.Files;

public class WurstProjectManager {
	
	
	private File workspace;
	private File depFolder;

	public WurstProjectManager(File workspace) {
		this.workspace = workspace;
		this.depFolder = new File(workspace, "deps");
	}
	
	
	
	public void loadDepFile() throws IOException, InvalidRemoteException, TransportException, GitAPIException {
		WurstProjectFile projectFileInfo = new WurstProjectFile(Files.toString(getProjectFile(workspace), Charsets.UTF_8));
		
		
		depFolder.mkdirs();
		
		resolveDependencies(projectFileInfo.getDependencies());
		
		
		
		
	}

	private void resolveDependencies(List<DependencyInfo> dependencies) throws InvalidRemoteException, TransportException, GitAPIException, IOException {
		Deque<DependencyInfo> todo = new ArrayDeque<>(sortedByName(dependencies));
		while (!todo.isEmpty()) {
			DependencyInfo depInfo = todo.removeFirst();
			processDependency(depInfo, todo);
		}
	}



	private List<DependencyInfo> sortedByName(List<DependencyInfo> dependencies) {
		// sort by library name
		ArrayList<DependencyInfo> res = new ArrayList<>(dependencies);
		Collections.sort(res, Comparator.comparing(DependencyInfo::getName));
		return res;
	}

	private void processDependency(DependencyInfo depInfo, Deque<DependencyInfo> todo) throws InvalidRemoteException, TransportException, GitAPIException, IOException {
		File targetFolder = new File(depFolder, depInfo.getName());
		if (targetFolder.exists()) {
			System.out.println("Dependency " + depInfo.getName() + " already exists.");
			return;
		}
		
		CloneCommand cloneCommand = Git.cloneRepository()
				.setURI(depInfo.getGitRepository())
				.setDirectory(targetFolder);
		if (depInfo.getGitBranch() != null) {
			cloneCommand.setBranch(depInfo.getGitBranch());
		}

		System.out.println("start cloning " + depInfo.getName());
		Git repo = cloneCommand.call();
		System.out.println("Cloned " + depInfo.getName());
		if (depInfo.getGitRevision() != null) {
			repo.checkout()
				.setName(depInfo.getGitRevision())
				.call();
		} else if (depInfo.getGitTag() != null) {
			repo.checkout()
			.setName(depInfo.getGitTag())
			.call();
		}
		
		// add transitive dependencies:
		File projectFile = getProjectFile(targetFolder);
		if (projectFile.exists()) {
			WurstProjectFile projectFileInfo = new WurstProjectFile(Files.toString(projectFile, Charsets.UTF_8));
			todo.addAll(sortedByName(projectFileInfo.getDependencies()));
		} else {
			System.out.println("Warning " + depInfo.getName() + " has no project file.");
		}
		
	}

	private File getProjectFile(File workspace) {
		return new File(workspace, "wurst.json");
	}

	
	

}
