package de.peeeq.wurstscript.objectreader;

public enum ObjectFileType {
	UNITS("w3u", "Units\\UnitData.slk", "Units\\UnitMetaData.slk", false),
	ITEMS("w3t", "Units\\ItemData.slk", "Units\\UnitMetaData.slk", false),
	DESTRUCTABLES("w3b", "Units\\DestructableData.slk", "Units\\DestructableMetaData.slk", false),
	DOODADS("w3d", "Doodads\\Doodads.slk", "Doodads\\DoodadMetaData.slk", true),
	ABILITIES("w3a", "Units\\AbilityData.slk", "Units\\AbilityMetaData.slk", true),
	BUFFS("w3b", "Units\\AbilityBuffData.slk", "Units\\AbilityBuffMetaData.slk", false),
	UPGRADES("w3u", "Units\\UpgradeData.slk", "Units\\UpgradeMetaData.slk", true);
	
	private boolean usesLevels;
	private String ext;
	private String objectIDs;
	private String modIDs;

	public boolean usesLevels() {
		return usesLevels;
	}

	public String getExt() {
		return ext;
	}

	public String getObjectIDs() {
		return objectIDs;
	}

	public String getModIDs() {
		return modIDs;
	}

	ObjectFileType(String ext, String objectIDs, String modIDs, boolean usesLevels) {
		this.ext = ext;
		this.objectIDs = objectIDs;
		this.modIDs = modIDs;
		this.usesLevels = true;
	}
	
}
