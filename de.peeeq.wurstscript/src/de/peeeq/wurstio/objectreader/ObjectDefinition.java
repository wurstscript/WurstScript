package de.peeeq.wurstio.objectreader;

import java.io.IOException;
import java.util.List;

import com.google.common.collect.Lists;

public class ObjectDefinition {

	private String origObjectId;
	private String newObjectId;
	private List<ObjectModification<?>> modifications = Lists.newArrayList();
	private final ObjectTable parent;
	
	public ObjectDefinition(ObjectTable parent, String origObjectId, String newObjectId) {
		this.origObjectId =  origObjectId;
		this.newObjectId = newObjectId;
		this.parent = parent;
	}

	public void add(ObjectModification<?> mod) {
		modifications.add(mod);
	}
	
	static ObjectDefinition readFromStream(BinaryDataInputStream in, ObjectTable parent) throws IOException {
		ObjectFileType fileType = parent.getFileType();
		String origObjectId = in.readString(4);
		String newObjectId = in.readString(4);
		ObjectDefinition def = new ObjectDefinition(parent, origObjectId, newObjectId);
		int numberOfModifications = in.readInt();
		for (int i = 0; i < numberOfModifications; i++) {
			ObjectModification<?> mod = ObjectModification.readFromStream(in, fileType, def);
			def.add(mod);
		}
		return def;
	}
	
	public void writeToStream(BinaryDataOutputStream out, ObjectFileType fileType) throws IOException {
		out.writeString(origObjectId, 4);
		out.writeString(newObjectId, 4);
		
		// write number of modifications.
		out.writeInt(modifications.size());
		for (ObjectModification<?> m : modifications) {
			m.writeToStream(out, fileType);
		}
		
	}

	public String getOrigObjectId() {
		return origObjectId;
	}

	public String getNewObjectId() {
		return newObjectId;
	}

	public List<ObjectModification<?>> getModifications() {
		return modifications;
	}

	public void prettyPrint(StringBuilder sb) {
		sb.append("Object " + newObjectId + " <: " + origObjectId+ "[\n");
		
		for (ObjectModification<?> m : modifications) {
			sb.append("    " + m.toString() + ";\n");
		}
		
		sb.append("]\n\n");
	}

	public void exportToWurst(Appendable out) throws IOException {
		out.append("@compiletime function create_" + parent.getFileType().getExt() + "_"+newObjectId+"()\n");
		out.append("	let u = createObjectDefinition(\""+parent.getFileType().getExt()+ "\", \"");
		out.append(newObjectId);
		out.append("\", \"");
		out.append(origObjectId);
		out.append("\")\n");
		for (ObjectModification<?> m : modifications) {
			m.exportToWurst(out);
		}
		out.append("\n\n");
	}

	public ObjectTable getParent() {
		return parent;
	}
	
	public ObjectFileType getFileType() {
		return parent.getFileType();
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		prettyPrint(sb);
		return sb.toString();
	}

	

}
