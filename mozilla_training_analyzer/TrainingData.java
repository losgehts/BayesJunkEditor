/*
 * Created on 9-Jun-2003
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

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.TreeSet;

/** A class which represents all of the information that is found
 *  within a Mozilla Bayesian Filter Training file.
 *
 * @author Straxus
 */
public class TrainingData {

	/** The first expected byte of the training.dat header.
	 */
	public static final int firstHdrByte = 0xFE;

	/** The second expected byte of the training.dat header.
	 */
	public static final int secondHdrByte = 0xED;

	/** The third expected byte of the training.dat header.
	 */
	public static final int thirdHdrByte = 0xFA;

	/** The fourth expected byte of the training.dat header.
	 */
	public static final int fourthHdrByte = 0xCE;

	/** The size of buffer to use when writing output via a
	 *  BufferedOutputStream.
	 */
	public static final int OUTPUT_BUFFER_SIZE = 65536;

	/** The default filename for the Mozilla training file.
	 */
	public static final String outputFileName = "training.dat";

	/** The number of good messages processed by this file.
	 */
	private int goodMessageCount = -1;

	/** The number of bad messages processed by this file.
	 */
	private int badMessageCount = -1;

	/** The number of good tokens found within this data
	 * 	structure.
	 */
	private int numGoodTokens = -1;

	/** The number of bad tokens found within this data structure.
	 */
	private int numBadTokens = -1;

	/** The list of tokens found within this data structure.
	 */
	private TreeSet tokenSet = null;

	/** Creates a new, empty TrainingData.
	 */
	public TrainingData() {
		// No need to do anything, this class will be populated later.

	}

	/** Creates a new TrainingData which is populated with the
	 *  specified values.
	 *
	 * @param goodMsgCount The number of good messages processed
	 * 						by this set of tokens.
	 * @param badMsgCount	The number of bad messages processed
	 * 						by this set of tokens.
	 * @param newTokenSet	The set of tokens found within the
	 * 						represented training file.
	 */
	public TrainingData(
		int goodMsgCount,
		int badMsgCount,
		TreeSet newTokenSet) {

		goodMessageCount = goodMsgCount;
		badMessageCount = badMsgCount;
		tokenSet = newTokenSet;
	}

	/** This method searches through the set of tokens in this
	 * 	TrainingData and validates the good and bad token counts.
	 *  Please note that this method should be called after any
	 * 	changes to this TrainingData's token set, otherwise the
	 * 	token counts will be out-of-date.
	 */
	public void validateTokenCount() {

		int goodTokenCount = 0;
		int badTokenCount = 0;

		// Get an iterator for our set of tokens.
		Iterator tokenIter = tokenSet.iterator();

		// Create this here to avoid unnecessary object creation
		// inside the loop.
		MozillaSpamToken tempToken = null;

		// Iterate through the entire TreeSet and count up both of
		// the token counts.
		while (tokenIter.hasNext()) {

			// Get a reference to the next token.
			tempToken = (MozillaSpamToken) tokenIter.next();

			if (tempToken.getGoodTokenCount() > 0) {
				goodTokenCount++;
			}

			if (tempToken.getBadTokenCount() > 0) {
				badTokenCount++;
			}
		}

		// Set the new, validated token counts.
		numGoodTokens = goodTokenCount;
		numBadTokens = badTokenCount;
	}

	/** This method removes all tokens which have a good count
	 *  which is less than goodCount, and a bad count which is
	 *  less than badCount. It must match both conditions, or it
	 * 	will not be removed. To disable good or bad checking,
	 *  just pass in a parameter which is &lt;= 0. The given
	 *  parameter will then be ignored, and only the other
	 *  parameter will be compared when removing tokens.
	 *
	 * @param goodCount	Tokens with a good count &gt;=
	 * 						goodCount will be kept, and those
	 * 						below will be removed from the set
	 * 						assuming they also satisfy badCount.
	 * 						To disable comparing the good count,
	 * 						pass in a parameter which is &lt;= 0.
	 * @param badCount		Tokens with a bad count &gt;=
	 * 						badCount will be kept, and those
	 * 						below will be removed from the set
	 * 						assuming they also satisfy goodCount.
	 * 						To disable comparing the bad count,
	 * 						pass in a parameter which is &lt;= 0.
	 */
	public void removeTokens(int goodCount, int badCount) {

		boolean goodFlag = true;
		boolean badFlag = true;

		// Check if the good flag is disabled.
		if (goodCount <= 0) {

			goodFlag = false;
		}

		// Check if the bad flag is disabled.
		if (badCount <= 0) {

			badFlag = false;
		}

		if (!goodFlag && !badFlag) {

			// We've disabled both, so just exit.
			return;
		}

		// Get an iterator for our set of tokens.
		Iterator tokenIter = tokenSet.iterator();

		// Create this here to avoid unnecessary object creation
		// inside the loop.
		MozillaSpamToken tempToken = null;

		// Iterate through the entire TreeSet and remove all of
		// the matching tokens.
		while (tokenIter.hasNext()) {

			// Get a reference to the next token.
			tempToken = (MozillaSpamToken) tokenIter.next();

			// Check if the token's goodCount is too large for it
			// to be removed.
			if (goodFlag &&
				(tempToken.getGoodTokenCount() >= goodCount)
			) {

				// This token is a keeper, so just go to the next
				// iteration of the loop.
				continue;

			// Check if the token's badCount is too large for it
			// to be removed.
			} else if (badFlag &&
						(tempToken.getBadTokenCount() >= badCount)) {

				// This token is a keeper, so just go to the next
				// iteration of the loop.
				continue;

			} else {

				// If we've gotten here, then the token is too
				// small. Remove it from the list.
				tokenIter.remove();
			}
		}

		// Revalidate the token count now that we're done.
		validateTokenCount();
	}

	/** Returns the number of bad messages processed by this set
	 * 	of tokens.
	 *
	 * @return The number of bad messages processed by this set
	 * 			of tokens.
	 */
	public int getBadMessageCount() {
		return badMessageCount;
	}

	/** Returns the number of good messages processed by this set
	 * 	of tokens.
	 *
	 * @return The number of good messages processed by this set
	 * 			of tokens.
	 */
	public int getGoodMessageCount() {
		return goodMessageCount;
	}

	/** Returns the number of bad tokens contained within this set
	 * 	of tokens.
	 *
	 * @return The number of bad tokens contained within this set
	 * 			of tokens.
	 */
	public int getNumBadTokens() {
		return numBadTokens;
	}

	/** Returns the number of good tokens contained within this
	 * 	set of tokens.
	 *
	 * @return The number of good tokens contained within this
	 * 			set of tokens.
	 */
	public int getNumGoodTokens() {
		return numGoodTokens;
	}

	/** Returns the set of tokens that this class represents.
	 *
	 * @return The set of tokens that this class represents.
	 */
	public TreeSet getTokenSet() {
		return tokenSet;
	}

	/** Sets the number of bad messages processed by this set of
	 * 	tokens.
	 *
	 * @param i 	The number of bad messages processed by this
	 * 				set of tokens.
	 */
	public void setBadMessageCount(int i) {
		badMessageCount = i;
	}

	/** Sets the number of good messages processed by this set of
	 * 	tokens.
	 *
	 * @param i 	The number of good messages processed by this
	 * 				set of tokens.
	 */
	public void setGoodMessageCount(int i) {
		goodMessageCount = i;
	}

	/** Changes the set of tokens that this class represents.
	 * 	After calling this method, you should also call
	 * 	validateTokenCount().
	 *
	 * @param set	The new set of tokens that this class
	 * 				represents.
	 */
	public void setTokenSet(TreeSet set) {
		tokenSet = set;
	}

	/** Writes output to the given BufferedOutputStream which
	 *  contains all of this TrainingData's information as a
	 *  well-formatted Mozilla Bayesian Filter Training file.
	 *
	 * @param outStream	The BufferedOutputStream to write the
	 * 						data to.
	 *
	 * @throws IOException If an error was encountered while
	 * 						generating the output data.
	 */
	public void outputTrainingDataFileContents(BufferedOutputStream outStream) throws IOException {

		// This is a good idea so that we don't write bad data to
		// the training data file. It's not strictly necessary
		// if the class has been used right, however I prefer
		// to lose a few cycles to have added robustness.
		validateTokenCount();

		// Write the necessary file header to the output file.
		outStream.write(TrainingData.firstHdrByte);
		outStream.write(TrainingData.secondHdrByte);
		outStream.write(TrainingData.thirdHdrByte);
		outStream.write(TrainingData.fourthHdrByte);

		// Write out the number of good messages processed
		outStream.write(Globals.makeBytes(goodMessageCount), 0, 4);

		// Write out the number of bad messages processed
		outStream.write(Globals.makeBytes(badMessageCount), 0, 4);

		// Write out the number of good tokens to be written
		outStream.write(Globals.makeBytes(numGoodTokens), 0, 4);

		// Get an iterator for our set of tokens.
		Iterator tokenIter = tokenSet.iterator();

		// Create this here to avoid unnecessary object creation
		// inside the loop.
		MozillaSpamToken tempToken = null;
		String tempString = null;

		// Iterate through the entire TreeSet and write out all
		// tokens with a GoodTokenCount > 0
		while (tokenIter.hasNext()) {

			// Get a reference to the next token.
			tempToken = (MozillaSpamToken) tokenIter.next();

			if (tempToken.getGoodTokenCount() > 0) {

				// First, write out the number of good occurences of this token
				outStream.write(
					Globals.makeBytes(tempToken.getGoodTokenCount()),
					0,
					4);

				// Get a reference to this string for ease of use
				tempString = tempToken.getTokenString();

				// Then, write out the length of the token string
				outStream.write(Globals.makeBytes(tempString.getBytes().length), 
						0, 4);
				
				// Finally, write out the token itself.
				outStream.write(tempString.getBytes(), 
						0, 
						tempString.getBytes().length);
			}

		}

		// Write out the number of bad tokens to be written
		outStream.write(Globals.makeBytes(numBadTokens));

		// Recreate our iterator for the second loop-through.
		tokenIter = tokenSet.iterator();

		// Iterate through the entire TreeSet and write out all
		// tokens with a BadTokenCount > 0
		while (tokenIter.hasNext()) {

			// Get a reference to the next token.
			tempToken = (MozillaSpamToken) tokenIter.next();

			if (tempToken.getBadTokenCount() > 0) {

				// First, write out the number of bad occurences of this token
				outStream.write(
					Globals.makeBytes(tempToken.getBadTokenCount()),
					0,
					4);

				// Get a reference to this string for ease of use
				tempString = tempToken.getTokenString();

				// Then, write out the length of the token string
				outStream.write(Globals.makeBytes(tempString.getBytes().length),
						0, 4);

				// Finally, write out the token itself.
				outStream.write(tempString.getBytes(), 
						0, 
						tempString.getBytes().length);
			}
		}
	}

	/** Outputs the data represented by this TrainingData as a
	 * 	human-readable plaintext file.
	 *
	 * @param outStream	The BufferedOutputStream to write the
	 * 						data to.
	 *
	 * @throws IOException If an error was encountered while
	 * 						generating the output data.
	 */
	public void toTextDocument(BufferedOutputStream outStream) throws IOException {

		// Print a header to the text file.
		outStream.write("Good messages: ".getBytes());
		outStream.write(Integer.toString(goodMessageCount).getBytes());
		outStream.write("\nBad messages: ".getBytes());
		outStream.write(Integer.toString(badMessageCount).getBytes());
		outStream.write("\n\nList of tokens\n--------------\n".getBytes());

		// Create this outside the loop to avoid unnecessary
		// creation of a bunch of objects inside the loop.
		Object tempObj = null;

		// Get an iterator for our set of tokens.
		Iterator tokenIter = tokenSet.iterator();

		// Keep going while elements remain in the list.
		while (tokenIter.hasNext()) {

			// Get a reference to the next token.
			tempObj = tokenIter.next();

			// Write that token's info to the output stream.
			outStream.write(tempObj.toString().getBytes());
			outStream.write('\n');
		}
	}

	/** Outputs the data represented by this TrainingData as a
	 * 	valid HTML 4.01 Transitional page.
	 *
	 * @param outStream	The BufferedOutputStream to write the
	 * 						data to.
	 *
	 * @throws IOException If an error was encountered while
	 * 						generating the output data.
	 */
	public void toHTMLDocument(BufferedOutputStream outStream) throws IOException {

		// Print a header to the HTML file.
		// Please note - the generated HTML is valid HTML 4.01
		// transitional - validated at http://walidator.w3c.org
		outStream.write("<!DOCTYPE html PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\">\n".getBytes());
		outStream.write("<html lang=\"en\">\n".getBytes());
		outStream.write("	<head>\n".getBytes());
		outStream.write("		<meta http-equiv=\"Content-Type\" content=\"text/html; charset=ISO-8859-1\">\n".getBytes());
		outStream.write("		<title>\n".getBytes());
		outStream.write("			Mozilla Bayesian Filter Tokens\n".getBytes());
		outStream.write("		</title>\n".getBytes());
		outStream.write("	</head>\n\n".getBytes());
		outStream.write("	<body>\n".getBytes());
		outStream.write("		<h1>\n".getBytes());
		outStream.write("			<div align=\"center\">\n".getBytes());
		outStream.write("				Mozilla Bayesian Filter Tokens\n".getBytes());
		outStream.write("			</div>\n".getBytes());
		outStream.write("		</h1>\n".getBytes());
		outStream.write("		<dl>\n".getBytes());
		outStream.write("			<dt>\n".getBytes());
		outStream.write("				Token\n".getBytes());
		outStream.write("			</dt>\n".getBytes());
		outStream.write("			<dd>\n".getBytes());
		outStream.write("				The string which has been detected\n".getBytes());
		outStream.write("				and tracked by the Bayesian filter.\n".getBytes());
		outStream.write("			</dd>\n".getBytes());
		outStream.write("			<dt>\n".getBytes());
		outStream.write("				Good\n".getBytes());
		outStream.write("			</dt>\n".getBytes());
		outStream.write("			<dd>\n".getBytes());
		outStream.write("				The number of occurences of this\n".getBytes());
		outStream.write("				token in non-junk (good) emails.\n".getBytes());
		outStream.write("			</dd>\n".getBytes());
		outStream.write("			<dt>\n".getBytes());
		outStream.write("				Bad\n".getBytes());
		outStream.write("			</dt>\n".getBytes());
		outStream.write("			<dd>\n".getBytes());
		outStream.write("				The number of occurences of this\n".getBytes());
		outStream.write("				token in junk (bad) emails.\n".getBytes());
		outStream.write("			</dd>\n".getBytes());
		outStream.write("		</dl>\n".getBytes());
		outStream.write("		<table align=\"center\" rules=\"all\">\n".getBytes());
		outStream.write("			<tr><th><strong>Token</strong></th>".getBytes());
		outStream.write("<th><strong>Good</strong></th>".getBytes());
		outStream.write("<th><strong>Bad</strong></th></tr>\n".getBytes());

		// Create this outside the loop to avoid unnecessary
		// creation of a bunch of objects inside the loop.
		MozillaSpamToken tempToken = null;

		// Get an iterator for our set of tokens.
		Iterator tokenIter = tokenSet.iterator();

		// Keep going while elements remain in the list.
		while (tokenIter.hasNext()) {

			// Get a reference to the next token.
			tempToken = (MozillaSpamToken) tokenIter.next();

			// Write that token's info to the output stream.
			outStream.write("			<tr><td>".getBytes());
			outStream.write(tempToken.getTokenString().getBytes());
			outStream.write("</td><td>".getBytes());
			outStream.write(Integer.toString(tempToken.getGoodTokenCount()).getBytes());
			outStream.write("</td><td>".getBytes());
			outStream.write(Integer.toString(tempToken.getBadTokenCount()).getBytes());
			outStream.write("</td></tr>\n".getBytes());
		}

		// Add a footer to the text file.
		outStream.write("		</table>\n".getBytes());
		outStream.write("	</body>\n".getBytes());
		outStream.write("</html>\n".getBytes());
	}

	/** Outputs the data represented by this TrainingData as a
	 * 	well-formed XML document. This XML document will conform
	 *  to the DTD generated by the writeXMLDTD(File) method.
	 *
	 * @param outStream	The BufferedOutputStream to write the
	 * 						data to.
	 *
	 * @throws IOException If an error was encountered while
	 * 						generating the output data.
	 */
	public void toXMLDocument(BufferedOutputStream outStream) throws IOException {

		// First, a quick discussion about my chosen method of
		// implementation. I'm doing this with Java 1.3.1, which
		// does not come with a built-in XML parser as 1.4 does.
		// In addition, even if I had an XML parser available,
		// I believe that this method is far faster than adding
		// all of these items to a DOM tree and writing out that
		// DOM tree to the file. So, in the interest of simplicity
		// and speed, I have elected to write out the XML file as
		// text rather than doing the whole DOM thing. As I will
		// not be doing any manipulation of the DOM tree, I see
		// no benefit to its usage here. If you should happen to
		// disagree, implementing this using DOM should be quite
		// straightforward.

		// Print a header to the XML file.
		outStream.write("<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>\n".getBytes());
		outStream.write("<!DOCTYPE tokenfile SYSTEM \"trainer_xml.dtd\">".getBytes());
		outStream.write("<tokenfile>\n".getBytes());
		outStream.write("	<good_msgs>".getBytes());
		outStream.write(Integer.toString(goodMessageCount).getBytes());
		outStream.write("</good_msgs>\n".getBytes());
		outStream.write("	<bad_msgs>".getBytes());
		outStream.write(Integer.toString(badMessageCount).getBytes());
		outStream.write("</bad_msgs>\n".getBytes());

		// Create this outside the loop to avoid unnecessary
		// creation of a bunch of objects inside the loop.
		MozillaSpamToken tempToken = null;

		// Get an iterator for our set of tokens.
		Iterator tokenIter = tokenSet.iterator();

		// Keep going while elements remain in the list.
		while (tokenIter.hasNext()) {

			// Get a reference to the next token.
			tempToken = (MozillaSpamToken) tokenIter.next();

			// Write that token's info to the output stream.
			outStream.write("	<token>\n".getBytes());
			outStream.write("		<name>".getBytes());
			outStream.write(tempToken.getTokenString().getBytes());
			outStream.write("</name>\n".getBytes());
			outStream.write("		<good>".getBytes());
			outStream.write(Integer.toString(tempToken.getGoodTokenCount()).getBytes());
			outStream.write("</good>\n".getBytes());
			outStream.write("		<bad>".getBytes());
			outStream.write(Integer.toString(tempToken.getBadTokenCount()).getBytes());
			outStream.write("</bad>\n".getBytes());
			outStream.write("	</token>\n".getBytes());
		}

		// Add a footer to the text file.
		outStream.write("</tokenfile>\n".getBytes());
	}

	/** This method generates a DTD to which the XML output of the
	 * 	toXMLDocument() method conforms.
	 *
	 * @param fileToWrite The File to write the DTD to.
	 *
	 * @throws IOException If an error was encountered while
	 * 						writing to the output file.
	 */
	public void writeXMLDTD(File fileToWrite) throws IOException {

		// Create a StringBuffer to hold our output as we
		// generate it.
		StringBuffer bufferOut = new StringBuffer();

		bufferOut
			.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n\n")
			.append("<!-- The root element of this Mozilla training data XML file. -->\n")
			.append("<!ELEMENT tokenfile (good_msgs, bad_msgs, token*)>\n\n")
			.append("<!-- Represents the number of good (non-junk) messages processed by this token file. -->\n")
			.append("<!ELEMENT good_msgs (#PCDATA)>\n\n")
			.append("<!-- Represents the number of bad (junk) messages processed by this token file. -->\n")
			.append("<!ELEMENT bad_msgs (#PCDATA)>\n\n")
			.append("<!-- Represents a single token in the training file. -->\n")
			.append("<!ELEMENT token (name, good, bad)>\n\n")
			.append("<!-- Represents the string associated with this token. -->\n")
			.append("<!ELEMENT name (#PCDATA)>\n\n")
			.append("<!-- Represents the number of times this token has appeared in good (non-junk) emails. -->\n")
			.append("<!ELEMENT good (#PCDATA)>\n\n")
			.append("<!-- Represents the number of times this token has appeared in bad (junk) emails. -->\n")
			.append("<!ELEMENT bad (#PCDATA)>\n");

		// Erase the output file if it exists
		if (fileToWrite.exists()) {

			fileToWrite.delete();
		}

		// Create a new output file.
		fileToWrite.createNewFile();

		// Create a stream for that output file.
		FileOutputStream outStream = new FileOutputStream(fileToWrite);

		// Write out the DTD.
		outStream.write(bufferOut.toString().getBytes());

		// Flush the output and close the file stream.
		outStream.flush();
		outStream.close();
	}
}
