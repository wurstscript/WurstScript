package de.peeeq.wurstio;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.lang.reflect.Array;

import de.peeeq.wurstscript.WLogger;

public class UtilsIO {

	public static void sleep(int i) {
		try {
			Thread.sleep(i);
		} catch (InterruptedException e) {
			// ignore
		}
	}

	/**
	 * Get the method name for a depth in call stack. <br />
	 * Utility function
	 * 
	 * @param depth
	 *            depth in the call stack (0 means current method, 1 means call
	 *            method, ...)
	 * @return method name
	 */
	public static String getMethodName(final int depth) {
		StackTraceElement[] ste = Thread.currentThread().getStackTrace();
		int i = 0;
		for (StackTraceElement s : ste) {
			// WLogger.info("Trace " +i+++ " = " + s.getMethodName());
		}

		return ste[depth + 2].getMethodName();
	}

	public static String getMethodNameExt(final int depth) {
		StackTraceElement[] ste = Thread.currentThread().getStackTrace();
		int i = 0;
		for (StackTraceElement s : ste) {
			// WLogger.info("Trace " +i+++ " = " + s.getMethodName());
		}
		StackTraceElement sf = ste[depth + 2];
		return sf.getMethodName() + "" + sf.getLineNumber();
	}

	
	public static void saveToFile(Object object, String filename) {
		FileOutputStream fos = null;
		ObjectOutputStream out = null;
		try {
			fos = new FileOutputStream(filename);
			out = new ObjectOutputStream(fos);
			out.writeObject(object);
			out.close();
		} catch (IOException e) {
			WLogger.info(e);
		}

	}
	
	public static <T> T[] copyArray(T[] ar) {
		@SuppressWarnings("unchecked")
		T[] r = (T[]) Array.newInstance(ar.getClass(), ar.length);
		System.arraycopy(ar, 0, r, 0, ar.length);
		return r;
	}
}
