package de.peeeq.wurstscript.objectreader;

import java.io.IOException;
import java.util.List;

import com.google.common.collect.Lists;

public class ObjectDefinition {

	private String origObjectId;
	private String newObjectId;
	private List<ObjectModification> modifications = Lists.newArrayList();

	public ObjectDefinition(String origObjectId, String newObjectId) {
		this.origObjectId =  origObjectId;
		this.newObjectId = newObjectId;
	}

	public void add(ObjectModification mod) {
		modifications.add(mod);
	}
	
	static ObjectDefinition readFromStream(BinaryDataInputStream in, ObjectFileType fileType) throws IOException {
		String origObjectId = in.readString(4);
		String newObjectId = in.readString(4);
		ObjectDefinition def = new ObjectDefinition(origObjectId, newObjectId);
		int numberOfModifications = in.readInt();
		for (int i = 0; i < numberOfModifications; i++) {
			ObjectModification mod = ObjectModification.readFromStream(in, fileType, def);
			def.add(mod);
		}
		return def;
	}
	
	public void writeToStream(BinaryDataOutputStream out, ObjectFileType fileType) throws IOException {
		out.writeString(origObjectId, 4);
		out.writeString(newObjectId, 4);
		
		// write number of modifications.
		out.writeInt(modifications.size());
		for (ObjectModification m : modifications) {
			m.writeToStream(out, fileType);
		}
		
	}

	public String getOrigObjectId() {
		return origObjectId;
	}

	public String getNewObjectId() {
		return newObjectId;
	}

	public List<ObjectModification> getModifications() {
		return modifications;
	}

	public void prettyPrint(StringBuilder sb) {
		sb.append("Object " + newObjectId + " <: " + origObjectId+ "[\n");
		
		for (ObjectModification m : modifications) {
			sb.append("    " + m.toString() + ";\n");
		}
		
		sb.append("]\n\n");
	}

	public void exportToWurst(Appendable out) throws IOException {
		out.append("@compiletime function createUnit"+newObjectId+"()\n");
		out.append("	let u = createUnitType(\"");
		out.append(newObjectId);
		out.append("\", \"");
		out.append(origObjectId);
		out.append("\")\n");
		for (ObjectModification m : modifications) {
			m.exportToWurst(out);
		}
		out.append("\n\n");
	}

	

}
