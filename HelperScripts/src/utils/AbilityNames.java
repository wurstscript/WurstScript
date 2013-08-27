package utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

public class AbilityNames {
	private Map<String, String> abilityNames = new HashMap<>();
	private Set<String> usedNames = new HashSet<>();
	
	public AbilityNames(File file) {
		try {
			Scanner s = new Scanner(file);
			s.useDelimiter("[\t\r\n]+");
			while (s.hasNext()) {
				String id = s.next();
				String r = s.next();
				if (abilityNames.containsKey(id)) {
					continue;
				}
				r = r.replace("+", "Plus");
				r = r.replaceAll("[^a-zA-Z0-9]+", "");
				while (!usedNames.add(r)) {
					r += id;
				}
				abilityNames.put(id, r);
				
			}
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public String get(String abilId) {
		String r = abilityNames.get(abilId);
		if (r == null) {
			r = abilId;
		}
		return r == null ? abilId : r;
	}
}
