package de.peeeq.eclipsewurstplugin;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UTFDataFormatException;
import java.util.Scanner;

import com.google.common.base.Charsets;
import com.google.common.io.Files;

public class FileKeywordScanner {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Scanner sc;
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
					sb.append(" \"" + sc.next() + "\", ");
					if ( i > 4 ) {
						i = 0;
						sb.append("\n");
					}
					i++;
				}
				
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
