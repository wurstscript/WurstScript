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
				Pattern stringPattern = Pattern.compile("\\{([\n\r]|[^}])*\\}");
				String str = sc.findWithinHorizon(stringPattern, 0);
				if (str == null) {
					continue;
				}
				str = str.substring(1, str.length() - 2);
				str = str.trim();
				result.put(id, str);
			}
		}
		return result;
	}

}
