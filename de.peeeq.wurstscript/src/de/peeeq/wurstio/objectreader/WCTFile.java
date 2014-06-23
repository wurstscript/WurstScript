package de.peeeq.wurstio.objectreader;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

import com.google.common.base.Charsets;
import com.google.common.collect.Lists;

import de.peeeq.wurstio.mpq.MpqEditor;
import de.peeeq.wurstio.mpq.MpqEditorFactory;

public class WCTFile {
	
	
	public static class CustomTextTrigger {

		private String contents;

		public CustomTextTrigger(String c) {
			this.contents = c;
		}

		public String getContents() {
			return contents;
		}
		
		
	}


	private int numberOfTriggers;
	private int version;
	private List<CustomTextTrigger> triggers = Lists.newArrayList();
	private String customScriptComment;
	
	
	public static WCTFile fromStream(BinaryDataInputStream in) throws IOException {
		WCTFile result = new WCTFile();
		result.version = in.readInt();
		System.out.println("version = " + result.version);
		result.customScriptComment = in.readNullTerminatedString(Charsets.UTF_8);
		result.triggers.add(readCustomTextTrigger(in));
		System.out.println("Comment: " + result.customScriptComment);
		result.numberOfTriggers = in.readInt();
		
		
		System.out.println("numberOfTriggers: " + result.numberOfTriggers);
		
		for (int i=0; i<result.numberOfTriggers; i++) {
			result.triggers.add(readCustomTextTrigger(in));
		}
		return result;
	}


	private static CustomTextTrigger readCustomTextTrigger(	BinaryDataInputStream in) throws IOException {
		int size = in.readInt();
		return new CustomTextTrigger(in.readString(size));
	}


	public int getNumberOfTriggers() {
		return numberOfTriggers;
	}


	public int getVersion() {
		return version;
	}


	public List<CustomTextTrigger> getTriggers() {
		return Collections.unmodifiableList(triggers);
	}


	public String getCustomScriptComment() {
		return customScriptComment;
	}
	
	public static void main(String[] args) throws Exception {
		
		File mpq = new File("/home/peter/work/dvs/dvs.w3x");
		try (MpqEditor ed = MpqEditorFactory.getEditor(mpq)) {
			byte[] wtc = ed.extractFile("war3map.wct");
			try (BinaryDataInputStream bdin = new BinaryDataInputStream(new ByteArrayInputStream(wtc), true)) {
				WCTFile tr = fromStream(bdin);
			
				for (CustomTextTrigger t : tr.triggers) {
					if (!t.getContents().isEmpty()) {
						System.out.println("####################### ");
						System.out.println(t.getContents());
					}
				}
			}
		}
	}
	
	
	

}
