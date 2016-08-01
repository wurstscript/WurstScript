package de.peeeq.wurstio.deps;

import java.util.Arrays;
import java.util.stream.Collectors;

public class DepVersion implements Comparable<DepVersion> {
	private final int[] parts;
	private final String addition;
	
	public DepVersion(int[] parts, String addition) {
		this.parts = parts;
		this.addition = addition;
	}
	
	public DepVersion(String versionString) throws InvalidVersionStringException {
		try {
			int dashPos = versionString.indexOf("-");
			if (dashPos < 0) {
				dashPos = versionString.length()-1;
			}
			this.parts = Arrays.stream(versionString.substring(0, dashPos).split("\\."))
					.mapToInt(Integer::parseInt)
					.toArray();
			this.addition = versionString.substring(dashPos, versionString.length());
		} catch (Exception e) {
			throw new InvalidVersionStringException(e, "Invalid version string.");
		}
	}

	@Override
	public int compareTo(DepVersion o) {
		for (int i = 0; i<parts.length && i <o.parts.length; i++) {
			if (parts[i] < o.parts[i]) {
				return -1;
			} else if (parts[i] > o.parts[i]) {
				return 1;
			}
			
		}
		// when everything else is equal, longer version string is later version
		int res = Integer.compare(parts.length, o.parts.length);
		if (res == 0) {
			return addition.compareTo(o.addition);
		} else {
			return res;
		}
	}
	
	@Override
	public String toString() {
		String result = Arrays.stream(parts).mapToObj(Integer::toString).collect(Collectors.joining("."));
		if (!addition.isEmpty()) {
			result += "-" + addition;
		}
		return result;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((addition == null) ? 0 : addition.hashCode());
		result = prime * result + Arrays.hashCode(parts);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		DepVersion other = (DepVersion) obj;
		if (addition == null) {
			if (other.addition != null)
				return false;
		} else if (!addition.equals(other.addition))
			return false;
		if (!Arrays.equals(parts, other.parts))
			return false;
		return true;
	}
	
	
	

}
