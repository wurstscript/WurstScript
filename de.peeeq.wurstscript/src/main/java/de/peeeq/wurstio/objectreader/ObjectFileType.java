package de.peeeq.wurstio.objectreader;


public enum ObjectFileType {
    UNITS("w3u", "Units\\UnitData.slk", "Units\\UnitMetaData.slk", false),
    ITEMS("w3t", "Units\\ItemData.slk", "Units\\UnitMetaData.slk", false),
    DESTRUCTABLES("w3b", "Units\\DestructableData.slk", "Units\\DestructableMetaData.slk", false),
    DOODADS("w3d", "Doodads\\Doodads.slk", "Doodads\\DoodadMetaData.slk", true),
    ABILITIES("w3a", "Units\\AbilityData.slk", "Units\\AbilityMetaData.slk", true),
    BUFFS("w3h", "Units\\AbilityBuffData.slk", "Units\\AbilityBuffMetaData.slk", false),
    UPGRADES("w3q", "Units\\UpgradeData.slk", "Units\\UpgradeMetaData.slk", true);


    private final boolean usesLevels;
    private final String ext;
    private final String objectIDs;
    private final String modIDs;

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
        this.usesLevels = usesLevels;
    }

    public static ObjectFileType fromExt(String fileExtension) {
        for (ObjectFileType t : ObjectFileType.values()) {
            if (t.getExt().equals(fileExtension)) {
                return t;
            }
        }
        throw new Error("Unsupoorted filetype: " + fileExtension);
    }

}
