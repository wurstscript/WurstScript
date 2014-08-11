package de.peeeq.eclipsewurstplugin.editor.reconciling;

import org.eclipse.jface.text.Position;


/** an immutable alternative to eclipse positions. 
 * Why the fuck would they make positions mutable?
 * And then give them hashcode and equals methods?
 * This ist just stupid...*/
/**
 * 
 */
public class WPosition {
	final int offset;
	final int length;
	public WPosition(int offset, int length) {
		this.offset = offset;
		this.length = length;
	}
	public WPosition(Position p) {
		this(p.offset, p.length);
	}
	public int getOffset() {
		return offset;
	}
	public int getLength() {
		return length;
	}
	
	public Position toEclipsePosition() {
		return new Position(offset, length);
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + length;
		result = prime * result + offset;
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
		WPosition other = (WPosition) obj;
		if (length != other.length)
			return false;
		if (offset != other.offset)
			return false;
		return true;
	}
	@Override
	public String toString() {
		return "WPosition(" + offset + ", " + length + ")";
	}
	
	
	

}
