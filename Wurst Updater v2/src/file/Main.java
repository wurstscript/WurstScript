package file;

import java.io.IOException;

import ui.Init;

public class Main {

	public static void main(String[] args) throws IOException {
		if(args.length > 0) {
			if(args[0].equals("silent")) {
				Init.initSilent();
			}
		} else {
			Init.initNormal();
		}
	}
}
