package de.peeeq.wurstio.deps;

import java.io.File;
import java.io.IOException;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.api.errors.TransportException;

public class Test {
	
	public static void main(String[] args) throws InvalidRemoteException, TransportException, IOException, GitAPIException {
		WurstProjectManager wpm = new WurstProjectManager(new File("/home/peter/work/wtest"));
		wpm.loadDepFile();
		System.out.println("done");
	}

}
