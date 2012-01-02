package wursteditor.projectcontrol;

import java.util.List;

import com.google.common.collect.Lists;

public class EditorProperties {
	private boolean lineNumbers = true;
        private boolean tabbedLines = true;    
	
		
	public boolean getLineNumbers() {
		return lineNumbers;
	}
	public void setLineNumbers(boolean flag) {
		this.lineNumbers = flag;
	}
        
        public boolean getTabbedLines() {
		return tabbedLines;
	}
	public void setTabbedLines(boolean flag) {
		this.tabbedLines = flag;
	}
	
	
}
