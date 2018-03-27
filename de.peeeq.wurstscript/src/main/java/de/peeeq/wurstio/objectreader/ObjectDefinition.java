package de.peeeq.wurstio.objectreader;

import com.google.common.collect.Lists;

import java.io.IOException;
import java.util.List;

public class ObjectDefinition {

    private int origObjectId;
    private int newObjectId;
    private List<ObjectModification<?>> modifications = Lists.newArrayList();
    private final ObjectTable parent;

    public ObjectDefinition(ObjectTable parent, int origObjectId, int newObjectId) {
        this.origObjectId = origObjectId;
        this.newObjectId = newObjectId;
        this.parent = parent;
    }

    public void add(ObjectModification<?> mod) {
        modifications.add(mod);
    }

    static ObjectDefinition readFromStream(BinaryDataInputStream in, ObjectTable parent) throws IOException {
        ObjectFileType fileType = parent.getFileType();
        int origObjectId = in.readIntReverse();
        int newObjectId = in.readIntReverse();
        ObjectDefinition def = new ObjectDefinition(parent, origObjectId, newObjectId);
        int numberOfModifications = in.readInt();
        for (int i = 0; i < numberOfModifications; i++) {
            ObjectModification<?> mod = ObjectModification.readFromStream(in, fileType, def);
            def.add(mod);
        }
        return def;
    }

    public void writeToStream(BinaryDataOutputStream out, ObjectFileType fileType) throws IOException {
        out.writeIntReverse(origObjectId);
        out.writeIntReverse(newObjectId);

        // write number of modifications.
        out.writeInt(modifications.size());
        for (ObjectModification<?> m : modifications) {
            m.writeToStream(out, fileType);
        }

    }

    public int getOrigObjectId() {
        return origObjectId;
    }

    public int getNewObjectId() {
        return newObjectId;
    }

    public List<ObjectModification<?>> getModifications() {
        return modifications;
    }

    public void prettyPrint(StringBuilder sb) {


        for (ObjectModification<?> m : modifications) {
            sb.append("    ").append(m.toString()).append(";\n");
        }

        sb.append("]\n\n");
    }

    public void exportToWurst(Appendable out) throws IOException {
        String oldId = ObjectHelper.objectIdIntToString(origObjectId);
        String newId = ObjectHelper.objectIdIntToString(newObjectId);
        if(oldId.equals(newId)) {
            newId = "xxxx";
        }
        out.append("@compiletime function create_").append(parent.getFileType().getExt()).append("_").append(newId)
                .append("()\n");
        out.append("	let def = createObjectDefinition(\"").append(parent.getFileType().getExt()).append("\", '");
        out.append(newId);
        out.append("', '");
        out.append(oldId);
        out.append("')\n");
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
