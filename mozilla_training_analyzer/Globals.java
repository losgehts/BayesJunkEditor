/*
 * Created on 10-Jun-2003
 *
 * The terms for using this software are as follows:
 *
 * USE AT YOUR OWN RISK - if this program goes insane and takes
 * out several bystanders, don't come knocking on my door with
 * lawyers.
 *
 * If you want to extend or use this software for some sort of
 * commercial (read: money-making) software, tell me about it
 * first. I probably won't ask for a cut because the software
 * isn't that complicated, but I do want to know where my little
 * baby heads after it leaves my machine.
 *
 * This project has become an official Mozdev project, and the
 * website for it is http://bayesjunktool.mozdev.org. That is
 * the best place to look for updates and information about this
 * application. Updates are coming fast and furious right now,
 * so it's a good idea to check it frequently.
 *
 * If you have any questions about this program, feel free to
 * email any questions to bayesjunktool@mozdev.org. I'd love to
 * hear how this program worked for you, or any suggestions or
 * bugfixes that you believe this software should use. I believe
 * that software should evolve and become better, so there's an
 * extremely good chance your suggestion will make it into the
 * next version.
 *
 * Oh, and for those of you curious about the author's (my) name,
 * just email straxus@baynet.net and ask. :)
 */
package mozilla_training_analyzer;

/** A class with convenience methods which are used by several
 * 	other classes and which did not really belong in any of those
 * 	other classes.
 *
 * @author Straxus
 */
public class Globals {

	// No instantiation of this class.
	private Globals() {
	}

	/** This method converts a 4 byte array to an equivalent int
	 * 	representation. This is a convenience method used during
	 *  parsing of the Mozilla Bayesian Filter Training file.
	 *
	 * @param inBytes	The bytes to be converted to an int. This
	 * 					array must be of size 4, or an
	 * 					IllegalArgumentException will be thrown.
	 *
	 * @return An int which represents the given bytes.
	 */
	public static int makeInt(byte[] inBytes) {

		if (inBytes.length != 4) {
			throw new IllegalArgumentException("inBytes must have a length of 4!");
		}

		int retVal = (inBytes[3] + 256) % 256;
		retVal += ((inBytes[2] + 256) % 256) * 256; 	// 2^8
		retVal += ((inBytes[1] + 256) % 256) * 65536;	// 2^16
		retVal += ((inBytes[0] + 256) % 256) * 16777216;// 2^24

		return retVal;
	}

	/** This method converts an int to an equivalent 4 byte array
	 * 	representation. This is a convenience method used during
	 *  creation of the Mozilla Bayesian Filter Training file.
	 *
	 * @param inNum	The number to be represented as an array of
	 * 					bytes.
	 *
	 * @return A byte[] of length 4 which represents the given
	 * 			int.
	 */
	public static byte[] makeBytes(int inNum) {

		byte[] retArray = new byte[4];

		for (int i = 3; i >= 0; i--) {

			retArray[i] = (byte) ((inNum >> (8*(3-i))) % 256);
		}

		return retArray;
	}
}
