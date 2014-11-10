package womo;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class Utils {

	final static private File dataDir = new File("/home/f_jostoc/");
	
	/** 
	 * This method returns the contents of a file as bytearray
	 * @param name The name of the file you want to read
	 * @return Bytearray with the contents of this File
	 * @throws IOException
	 */
	public static byte [] filename2bytearray(String name) throws IOException {
		File file = new File(name);
		if (! file.canRead()) {
			throw new java.io.IOException("Kann "+ name + " nicht lesen!");
		}
		byte [] byteArray = new byte[(int)file.length()];
		FileInputStream fis = new FileInputStream(file);
		fis.read(byteArray);
		fis.close();
		return byteArray;
	}
	
	/**
	 * This method saves a bytearray into a file
	 * @param bytearray Contents to write into a file
	 * @param file Path of the file to write the bytearray into
	 * @throws IOException
	 */
	public static void bytearray2file(byte[] bytearray, File file) throws IOException {
		if (! file.canWrite()) {
			throw new java.io.IOException("Kann "+ file.getAbsolutePath() + " nicht beschreiben!");
		}
		FileOutputStream fos = new FileOutputStream(file);
		fos.write(bytearray);
		fos.close();
	}
	public static File bytearray2file(byte[] bytearray, String name) throws IOException {
		if (! dataDir.isDirectory()) {
			if (!dataDir.mkdirs()) {
				throw new IOException(
						"Cannot create data dir: " 
						+ dataDir.getAbsolutePath()
				);
			}
		}
		File dpr = new File(dataDir, name);
		Utils.bytearray2file(bytearray, dpr);
		return dpr;
	}

}
