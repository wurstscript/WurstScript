package de.peeeq.eclipsewurstplugin;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Scanner;

import com.google.common.base.Charsets;
import com.google.common.io.Files;

/**
 * Temporary Class for parsing the common.j in order 
 * to extract the jasstype names.
 * Simple java Scanner app.
 * @author Frotty
 *
 */
public class FileKeywordScanner {

	public static String charset(){
		return "0aUb1GLwR2kHvP3CQIM4npFs5iSW6tzAr7TNxo8JXVjy9OcdEfBKgqeuYDhmlZ";
	}

	public static int charsetlen(){
		return (charset()).length();
	}
	
	public static int chartoi( String c ) {
		int i = 0;
		String cs = charset();
		int len = charsetlen();
		while ( i < len && ! c.equals(cs.substring(i,i+1)))
			i = i + 1;
		return i;
	}
			
	public static int fhash( String ps ) {
		int hash = 7;
		for( int i = 0; i<= ps.length()-1; i++)
			hash = hash*31+chartoi(String.valueOf(ps.charAt(i)))+11;
		if (hash < 0)
			hash = -hash;
		return hash;
	}
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		StringBuilder sb = new StringBuilder();
		//sc.useDelimiter(" *");
		int frotty = fhash("Frotty");
		NameGenerator ng = new NameGenerator();
		String t = ng.getToken();
		int length = t.length();
		int ol = 0;
		System.out.println(String.valueOf(args));
//		while(length < 15) {
//			int s = fhash(t);
//			if (s == frotty && ! t.equals("Frotty"))  {
//				System.out.println("found");
//				sb.append(t);
//				sb.append("\n");
//				break;
//			}
//			if (length > ol) {
//				ol = length;
//				System.out.println(length);
//			}
//			t = ng.getToken();
//			length = t.length();
//		}
//		System.out.println("FOUND!!!!");
//		System.out.println(sb);
		
	}
}

