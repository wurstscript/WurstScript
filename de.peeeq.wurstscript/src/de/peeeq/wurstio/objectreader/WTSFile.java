package de.peeeq.wurstio.objectreader;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Pattern;

import com.google.common.base.Charsets;
import com.google.common.collect.Maps;
import com.google.common.io.Files;

import de.peeeq.wurstscript.WLogger;

public class WTSFile {

	public static Map<Integer, String> parse(File wts) {
		try {
			return parse(Files.toString(wts, Charsets.UTF_8));
		} catch (IOException e) {
			WLogger.warning("Could not read wts file");
			throw new Error(e);
		}
	}

	public static Map<Integer, String> parse(String wts) {
		Map<Integer, String> result = Maps.newLinkedHashMap();
		try (Scanner sc = new Scanner(wts)) {
			while (sc.findWithinHorizon("STRING", 0) != null) {
				int id = sc.nextInt();
				// TODO this pattern is not correct, but it should cover 99% of the cases M;D
				// the handling of curly braces and comments is not really clear to me at the moment
				String str = extractNextWTSString(sc);
				result.put(id, str);
				WLogger.info("found: " + id + " -> " + str);
			}
		}
		return result;
	}

	/** extracts the next string in curly braces*/
	private static String extractNextWTSString(Scanner sc) {
		// temoporary change delimiter to read single chars
		Pattern delimiter = sc.delimiter();
		sc.useDelimiter("");
		try {
			StringBuilder result = new StringBuilder();
			
			for (;;) {
				String n = sc.next();
				if (n == null) {
					return null;
				}
				if (n.equals("{")) {
					break;
				}
			}
			
			for (;;) {
				String n = sc.next();
				if (n == null) {
					return null;
				}
				if (n.equals("}")) {
					break;
				}
				result.append(n);
			}
			// use substring to remove \r\n at beginning and end
			return result.substring(2, result.length()-2);
		} finally {
			// revert to old delimiter
			sc.useDelimiter(delimiter);
		}
	}

}
