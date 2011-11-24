package de.peeeq.wurstscript;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

/** 
 * a helper class to run pjass 
 *
 */
public class Pjass {

	public static class Result {

		private boolean ok;
		private String message;

		public Result(boolean ok, String message) {
			this.ok = ok;
			this.message = message;
		}

		public boolean isOk() {
			return ok;
		}

		public String getMessage() {
			return message;
		}
		
		
		
	}
	
	
	public static Result runPjass(File outputFile) {
		try {
			Process p;
			System.out.println("Starting pjass");
			if (System.getProperty("os.name").contains("windows")) {
				p = Runtime.getRuntime().exec("lib/pjass.exe lib/common.j lib/debugnatives.j lib/blizzard.j " + outputFile.getPath());
			} else {
				System.out.println("Operation system " + System.getProperty("os.name") + " detected.");
				System.out.println("Trying to run with wine ...");
				// try to run with wine
				p = Runtime.getRuntime().exec("wine lib/pjass.exe lib/common.j lib/debugnatives.j lib/blizzard.j " + outputFile.getPath());
			}
			BufferedReader input =
					new BufferedReader
					(new InputStreamReader(p.getInputStream()));
			
			StringBuilder output = new StringBuilder();
			String line;
			while ((line = input.readLine()) != null) {
				System.out.println(line);
				output.append(line + "\n");
			}
			input.close();

			int exitValue = p.waitFor();
			if (exitValue != 0) {
				return new Result(false, "pjass errors: \n" + output.toString());
			} else {
				return new Result(true, output.toString());
			}
		} catch (IOException e) {
			System.err.println("Could not run pjass:");
			e.printStackTrace();
			return new Result(false, "IO Exception");
		} catch (InterruptedException e) {
			return new Result(false, "Interrupted");
		}
		
	}
}
