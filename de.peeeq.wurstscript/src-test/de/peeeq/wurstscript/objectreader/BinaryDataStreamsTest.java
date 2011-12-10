package de.peeeq.wurstscript.objectreader;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Random;

import org.junit.Test;

public class BinaryDataStreamsTest {


	@Test
	public void testWriteInt() throws IOException {
		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		BinaryDataOutputStream out = new BinaryDataOutputStream(outStream, true);
		int[] numbers =  {1, 2, 3, 0, Integer.MAX_VALUE, Integer.MIN_VALUE, 1 << 8, 1 << 16, 1 << 24};  // getRandomInts();
		for (int i=0; i < numbers.length; i++) {
			out.writeInt(numbers[i]);
		}

		out.flush();
		
		System.out.println("bytes = " + outStream);
		byte[] bytes = outStream.toByteArray();
		
		
		
		BinaryDataInputStream in = new BinaryDataInputStream(new ByteArrayInputStream(bytes), true);
		for (int i=0; i < numbers.length; i++) {
			int read = in.readInt();
			System.out.println("read = " + read);
			assertEquals(numbers[i], read);
		}
		
	}

	private int[] getRandomInts() {
		Random r = new Random();
		int[] result = new int[r.nextInt(100)];
		for (int i=0; i<result.length; i++) {
			result[i] = r.nextInt();
		}
		return result;
	}

	@Test
	public void testWriteString() {
		fail("Not yet implemented");
	}

	@Test
	public void testWriteStringNullTerminated() {
		fail("Not yet implemented");
	}

	@Test
	public void testWriteFloat() {
		fail("Not yet implemented");
	}

}
