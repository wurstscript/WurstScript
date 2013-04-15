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

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Scanner sc;
		try {
			
		} catch (Exception e) {
			// TODO: handle exception
		}
		try {
			sc = new Scanner(new File("./resources/common.j"));
			File f = new File("./resources/helper.j");
			StringBuilder sb = new StringBuilder();
			//sc.useDelimiter(" *");
			if (sc.hasNext()) {
				System.out.println("yo");
			}
			int i = 0;
			while(sc.hasNext()) {
				String s = sc.next();
				if (s.equals("type")) {
					System.out.println(s);
					s = sc.next();
					String s2 = s.substring(0,1);
					s2 = s2.toUpperCase() + s.substring(1, s.length());
					sb.append("function " + s + "FromIndex( int index ) returns " + s);
					sb.append("\n");
					sb.append("\t" + "data.saveFogState(0,ConvertFogState(index))");
					sb.append("\n");
					sb.append("\t" + "return data.load" + s2 + "(0)");
					sb.append("\n");sb.append("\n");
					sb.append("function " + s + "ToIndex(" + s + " object ) returns int");
					sb.append("\n");
					sb.append("\t" + "return object.getHandleId()");
					sb.append("\n");sb.append("\n");
					i++;
				}
//				function getInt( int parentKey ) returns int
//	            return ht.loadInt(this castTo int, parentKey)
//	        
//	        function setInt( int parentKey, int value )
//	            ht.saveInt(this castTo int, parentKey, value)
			}
			System.out.println("yo2");
			System.out.println(sb);
			try {
				Files.write(sb.toString(), f, Charsets.UTF_8);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

}
