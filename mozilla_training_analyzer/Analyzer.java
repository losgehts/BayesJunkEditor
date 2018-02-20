/*
 * Created on 3-Jun-2003
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
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Iterator;
import java.util.TreeSet;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/** A class which allows analysis of a given Mozilla Bayesian
 * 	Filter Training file.
 *
 * @author Straxus
 */
public class Analyzer {

	/** The number of tokens to process before printing out a
	 * 	display character. This is used to indicate to the user
	 * 	that the program is still alive and well while it is
	 *  processing the token file, as users like some indication
	 *  that the program is not dead if it sits there for an
	 *  extended period of time.
	 */
	private static final int numTokensBeforeTick = 250;

	/** Indicates that the data will be output in basic text
	 * 	format.
	 */
	public static final int OUTPUT_TEXT = 1;

	/** Indicates that the data will be output in HTML format.
	 */
	public static final int OUTPUT_HTML = 2;

	/** Indicates that the data will be output in XML format.
	 */
	public static final int OUTPUT_XML = 3;

	/** Indicates that the data will be output in well-formed
	 *  Mozilla Bayesian Filter Training file format.
	 */
	public static final int OUTPUT_DATA = 4;

	/** The error code returned when an invalid argument was
	 *  passed into the program.
	 */
	public static final int INVALID_ARG = -3;

	/** The error code returned when a general, unknown error is
	 *  encountered.
	 */
	public static final int GEN_ERROR = -1;

	/** The error code returned when the program completes
	 *  successfully.
	 */
	public static final int SUCCESS = 0;

	/** A String which describes the version and name of this
	 *  application.
	 */
	public static final String VER_STRING = "Bayes Junk Tool Ver. 0.2";

	/** A String which displays valid command-line flags for this
	 *  program.
	 */
	public static final String PROGRAM_USAGE =
		VER_STRING + "\n\n" +
		"Valid command-line arguments for this program are:\n\n" +
		"-q, --quiet == silent execution of program\n" +
		"-g, --gui == start up GUI version of program\n" +
		"-h, -?, --help == display program usage (this message)\n" +
		"-v, --version == display program version\n" +
		"-f, --format [ xml | html | text | data ] == program output format\n" +
		"-rg, --remove-good [number] == Remove all tokens with a good or bad count\n" +
		"-rb, --remove-bad [number]  == less than the given number. If both are\n" +
		"specified, those tokens which satisfy either one OR the other will be kept.\n" +
		"-o, --outputfile [filename] == path to program output file\n" +
		"-m, --merge [filename] == path to XML or .dat file to merge with inputfile\n" +
		"-i, --inputfile [filename] == path to Mozilla training.dat\n\n" +
		"Please note that the input file must include the training.dat\n" +
		"filename, e.g. [path-to-profile]/xxxxxxxx.slt/training.dat\n";

	// No instantiation of this class, everything is static.
	private Analyzer() {
	}

	/** Checks the header of the given file to see if it is a
	 *  valid Mozilla Bayesian Filter training file. If it was
	 *  not, a warning message is output to the console.
	 *
	 * @param headerBytes 	The first four bytes that were read
	 * 						in from the file.
	 *
	 * @return true if the header is valid, false otherwise.
	 */
	public static boolean checkHeader(byte[] headerBytes) {

		// Java reads in a byte as a signed number, so we need
		// to do this modulo trick to make the byte an unsigned
		// number for comparison purposes.
		if ((((headerBytes[0] + 256) % 256) == TrainingData.firstHdrByte) &&
			(((headerBytes[1] + 256) % 256) == TrainingData.secondHdrByte) &&
			(((headerBytes[2] + 256) % 256) == TrainingData.thirdHdrByte) &&
			(((headerBytes[3] + 256) % 256) == TrainingData.fourthHdrByte)) {

			return true;

		} else {

			return false;
		}
	}

	/** Reads in token data from the given InputStream, and
	 * 	generates a MozillaSpamToken which represents that data.
	 *
	 * @param inFile	The InputStream to read data from.
	 * @param inBytes 	A 4-byte array used as a buffer for
	 * 					reading from the InputStream. This is
	 * 					done to prevent loads of unnecessary
	 * 					object creation.
	 * @param isGood 	true if the token is a good token, false
	 * 					if it is a bad one.
	 *
	 * @return A MozillaSpamToken which represents the token that
	 * 			was just read in from the training file.
	 *
	 * @throws IOException If an error was encountered while
	 * 						reading from the InputStream.
	 * @throws IllegalArgumentException If an illegal argument
	 * 									 was passed to this
	 * 									 method.
	 */
	public static MozillaSpamToken readNextToken(
			InputStream inFile,
			byte[] inBytes,
			boolean isGood
	) throws IOException, IllegalArgumentException {

		// Throw an exception if the InputStream is null.
		if (inFile == null) {
			throw new IllegalArgumentException("inFile cannot be null!");
		}

		// Throw an exception if the buffer is the wrong size.
		if (inBytes.length != 4) {
			throw new IllegalArgumentException("inBytes must have a length of 4!");
		}

		// Read in the number of tokens
		inFile.read(inBytes);
		int tokenCount = Globals.makeInt(inBytes);

		// Read in the length of the token
		inFile.read(inBytes);
		int tokenLength = Globals.makeInt(inBytes);
		
		// Read in the token
		byte[] bytes = new byte[tokenLength];
		int a = inFile.read(bytes);
		String tokenString = new String(bytes, "UTF-8");

		// Pass the new MozillaSpamToken back to the caller.
		return new MozillaSpamToken(tokenString,
				(isGood? tokenCount : 0),
				(isGood? 0 : tokenCount)
		);
	}

	/** Reads in token data from the given file and places it in
	 * 	a TrainingData for easy access.
	 *
	 * @param trainingDatPath	The path to the Mozilla Bayesian
	 * 							Filter training file.
	 * @param displayOutput	true if output should be displayed
	 * 							to the console while parsing,
	 * 							false otherwise.
	 *
	 * @return A TrainingData which encapsulates the training
	 * 			file's data, or null if an error was encountered
	 * 			while parsing the file.
	 *
	 * @throws IOException If an error was encountered while
	 * 						reading from the training file.
	 */
	public static TrainingData parseTrainingFile(
			String trainingDatPath,
			boolean displayOutput
	) throws IOException {

		// If there's no path, return null.
		if (trainingDatPath == null) {
			return null;
		}

		File inputFile = new File(trainingDatPath);
		FileInputStream inStream = null;

		// Check that the input file exists.
		if (inputFile.exists()) {

			inStream = new FileInputStream(inputFile);

		} else {

			// Can't read from a file that doesn't exist.
			return null;
		}

		// Create our reading buffer.
		byte[] inBytes = new byte[4];

		// Read in the standard header from the file.
		inStream.read(inBytes);

		if (!checkHeader(inBytes)) {

			if (displayOutput) {

				System.err.println(trainingDatPath +
					" is either not a Mozilla Bayesian filter" +
					" token file or is corrupt.");
			}

			return null;
		}

		TrainingData retTrainer = new TrainingData();

		// Read in the number of good emails processed.
		inStream.read(inBytes);
		int numGoodMsgs = Globals.makeInt(inBytes);

		// Read in the number of bad emails processed.
		inStream.read(inBytes);
		int numBadMsgs = Globals.makeInt(inBytes);

		// Read in the number of Good tokens from the file.
		inStream.read(inBytes);
		int numGoodTokens = Globals.makeInt(inBytes);

		retTrainer.setGoodMessageCount(numGoodMsgs);
		retTrainer.setBadMessageCount(numBadMsgs);

		if (displayOutput) {

			// Console status messages
			System.out.println("The number of good messages processed is " + numGoodMsgs);
			System.out.println("The number of bad messages processed is " + numBadMsgs);
			System.out.print("Now processing " + numGoodTokens + " good tokens");
		}

		// TreeSet automatically sorts all of the elements,
		// so take advantage of that here.
		TreeSet goodTokens = new TreeSet();

		// Go through and add all of the good tokens to a
		// "Good Token" set
		for (int i = 0; i < numGoodTokens; i++) {
			goodTokens.add(readNextToken(inStream, inBytes, true));

			// Every numTokensBeforeTick tokens, print out a .
			// so that the user knows that the program is
			// doing something.
			if (displayOutput && ((i % numTokensBeforeTick) == 0)) {
				System.out.print('.');
			}
		}

		// Read in the number of Bad tokens from the file.
		inStream.read(inBytes);
		int numBadTokens = Globals.makeInt(inBytes);

		if (displayOutput) {

			// Terminate the line, and print a console status message.
			System.out.print("\nNow processing " + numBadTokens + " bad tokens");
		}

		// TreeSet automatically sorts all of the elements,
		// so take advantage of that here.
		TreeSet badTokens = new TreeSet();

		// Go through and add all of the bad tokens to a
		// "Bad Token" vector
		for (int i = 0; i < numBadTokens; i++) {
			badTokens.add(readNextToken(inStream, inBytes, false));

			// Every numTokensBeforeTick tokens, print out a .
			// so that the user knows that the program is
			// doing something.
			if (displayOutput && ((i % numTokensBeforeTick) == 0)) {
				System.out.print('.');
			}
		}

		// Close the input stream.
		inStream.close();

		if (displayOutput) {

			// Terminate the line, and print a console status message.
			System.out.println("\nMerging token lists...");
		}

		// Merge the tokens into a single list.
		TreeSet mergedTokens = mergeTokenLists(goodTokens, badTokens);

		// Add the new, merged token set to the return class.
		retTrainer.setTokenSet(mergedTokens);

		// Set the token counts for this new list.
		retTrainer.validateTokenCount();

		return retTrainer;
	}

	/** Reads in token data from the given XML file and places it
	 * 	in a TrainingData for easy access.
	 *
	 * @param trainingXMLPath	The path to the Mozilla Bayesian
	 * 							Filter XML training file.
	 * @param displayOutput	true if output should be displayed
	 * 							to the console while parsing,
	 * 							false otherwise.
	 *
	 * @return A TrainingData which encapsulates the training
	 * 			file's data, or null if an error was encountered
	 * 			while parsing the file.
	 *
	 * @throws IOException If an error was encountered while
	 * 						reading from the XML file.
	 * @throws SAXException	If an error was encountered while
	 * 							parsing the given XML file.
	 * @throws ParserConfigurationException If the XML parser was
	 * 										 misconfigured.
	 */
	public static TrainingData parseXMLTrainingFile(
			String trainingXMLPath,
			boolean displayOutput
	) throws ParserConfigurationException, SAXException, IOException {

		// Create a new factory and a new document builder.
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

		// Turn on validating of parsed XML file so that we know
		// that the XML we read in conforms to the DTD it's
		// supposed to.
		factory.setValidating(true);

		// Generate a new DocumentBuilder for this file.
		DocumentBuilder builder = factory.newDocumentBuilder();

		File xmlTrainingFile;

		if (trainingXMLPath != null) {

			xmlTrainingFile = new File(trainingXMLPath);

			if (!xmlTrainingFile.exists()) {

				// The given file doesn't exist, so return null.
				return null;
			}

		} else {

			// The path is null, so return null.
			return null;
		}

		if (displayOutput) {

			// Console status messages
			System.out.println("Now generating XML DOM document for " + trainingXMLPath);

		}

		// Generate a new DOM Document.
		Document trainingDoc = builder.parse(xmlTrainingFile);

		if (displayOutput) {

			// Console status messages
			System.out.println("Generation complete. Now parsing DOM tree...");

		}

		TrainingData retTrainer;

		// Make sure that the valid document we just read in is
		// the right kind of document.
		if (trainingDoc.getDoctype().getNodeName().equals("tokenfile")) {

			// Create a new encapsulating class.
			retTrainer = new TrainingData();

			// Get the good count from the document and set it.
			// Note: Since there's only ever one good_msgs (or
			// bad_msgs) tag in a well-formed tokenfile XML file,
			// I can just get by name and select the first in the
			// list with no fear. As well, those of you that are
			// familiar with DOM will know that I need to do the
			// second getChildNodes to grab the Text node of this
			// Element.
			String goodCount = trainingDoc.getElementsByTagName("good_msgs").item(0).getChildNodes().item(0).getNodeValue();
			retTrainer.setGoodMessageCount(Integer.parseInt(goodCount));

			// Get the good count from the document and set it.
			String badCount = trainingDoc.getElementsByTagName("bad_msgs").item(0).getChildNodes().item(0).getNodeValue();
			retTrainer.setBadMessageCount(Integer.parseInt(badCount));

			// Get the set of tokens from the XML document.
			NodeList tokenList = trainingDoc.getElementsByTagName("token");

			// Create a new MozillaSpamToken list.
			TreeSet tokenSet = new TreeSet();

			// Create these here to avoid unnecessary object creation
			// inside the loop.
			String tokenString = null;
			int goodTokens = -1;
			int badTokens = -1;
			MozillaSpamToken newToken = null;
			Node tokenNode = null;
			Node tokenSubNode = null;
			NodeList childNodes = null;


			if (displayOutput) {

				// Console status messages
				System.out.println("The number of good messages processed is " + retTrainer.getGoodMessageCount());
				System.out.println("The number of bad messages processed is " + retTrainer.getBadMessageCount());
				System.out.print("Now processing "+ tokenList.getLength() + " tokens");

			}

			// This for loop is slow slow slow slow slow. I think
			// these DOM operations are just this slow, so I'm
			// not sure if there's any space for improvement other
			// than from Sun.
			// NOTE: The speed of the DOM parsing seems to drop
			// exponentially as the document gets larger. For
			// instance, an XML file with 200 or so tokens is
			// done very rapidly, whereas one with 49,000 tokens
			// takes about 4 minutes to get to the 200 token mark.
			for (int i = 0; i < tokenList.getLength(); i++) {

				// Every numTokensBeforeTick tokens, print out a .
				// so that the user knows that the program is
				// doing something.
				if (displayOutput && ((i % numTokensBeforeTick) == 0)) {
					System.out.print('.');
				}

				// Get the next XML token.
				tokenNode = tokenList.item(i);

				// Look at the set of child nodes for this Token.
				childNodes = tokenNode.getChildNodes();

				// During testing, the length of childNodes was 7,
				// so I don't think it's this second for loop that
				// is killing things. as it can be considered a
				// constant for Order runtime.
				for (int j = 0; j < childNodes.getLength(); j++) {

					tokenSubNode = childNodes.item(j);

					// If this is an Element node, it'll be one of
					// "good", "bad", or "name". Again, we do the
					// getChildNodes.item(0) thing because of the
					// way the DOM tree is structured, with the
					// Text node under the Element node.
					if (tokenSubNode.getNodeType() == Node.ELEMENT_NODE) {

						if (tokenSubNode.getNodeName().equals("good")) {

							goodTokens = Integer.parseInt(tokenSubNode.getChildNodes().item(0).getNodeValue());

						} else if (tokenSubNode.getNodeName().equals("bad")) {

							badTokens = Integer.parseInt(tokenSubNode.getChildNodes().item(0).getNodeValue());

						} else if (tokenSubNode.getNodeName().equals("name")) {

							tokenString = tokenSubNode.getChildNodes().item(0).getNodeValue();
						}
					}
				}

				// Create a new MozillaSpamToken for the given
				// token data.
				newToken = new MozillaSpamToken(tokenString, goodTokens, badTokens);

				// Add the token to the TrainingData's set.
				tokenSet.add(newToken);
			}

			if (displayOutput) {

				// Terminate the line of ...s that the loop
				// created on the console.
				System.out.println();
			}

			// Assign the newly constructed token set to the
			// TrainingData to be returned.
			retTrainer.setTokenSet(tokenSet);

			// Set up the token counts inside the TrainingData.
			retTrainer.validateTokenCount();

		} else {

			System.err.println(trainingXMLPath +
				" is not a Mozilla Bayesian filter" +
				" XML file - it has DOCTYPE [" +
				trainingDoc.getDoctype().getNodeName() +
				"], and DOCTYPE [tokenfile] was expected.");

			retTrainer = null;
		}

		return retTrainer;
	}

	/** Merges two token lists into a third, new TreeSet. This
	 *  method will intelligently merge tokens, e.g. if the token
	 * 	'foo' appears in goodTokens with goodCount 5 and badCount
	 *  0 and also appears in badTokens with goodCount 0 and
	 * 	badCount 23, then the returned list will contain a single
	 * 	token 'foo' with goodCount 5 and badCount 23.
	 *
	 * 	The names of the variables reflect the most common use of
	 * 	this method, which is merging the good-only tokens with
	 * 	the bad-only tokens that were just read in from a Mozilla
	 * 	training file, however it will work properly for two
	 * 	complete token lists that contain both good and bad
	 * 	token values.
	 *
	 * @param goodTokens A TreeSet of good tokens to be merged.
	 * @param badTokens A TreeSet of bad tokens to be merged.
	 *
	 * @return A TreeSet which contains the combined values of
	 */
	public static TreeSet mergeTokenLists(
			TreeSet goodTokens,
			TreeSet badTokens
	) {

		// Use goodTokens as the basis for the new, merged set.
		TreeSet mergedSet = new TreeSet(goodTokens);

		// Get an iterator for our set of bad tokens.
		Iterator badSetIter = badTokens.iterator();

		// Create these here to avoid unnecessary object creation
		// inside the loop.
		MozillaSpamToken tempToken = null;
		MozillaSpamToken goodToken = null;

		// Loop through the entire list.
		while (badSetIter.hasNext()) {

			// Get a reference to the next token.
			tempToken = (MozillaSpamToken) badSetIter.next();

			// Due to the nature of MozillaSpamToken.compareTo(),
			// two tokens will be considered equal if they have
			// the same tokenString. Use that here to determine
			// if a given bad token matches a given good token.
			if (mergedSet.contains(tempToken)) {

				// This depends upon 2 things from the contract
				// for the method tailSet():
				// 1. The original set backs this one up, i.e.
				// changes here take effect in the parent set
				// 2. These elements will be greater than or equal
				// to tempToken, which means tempToken will be the
				// first element in the set.
				goodToken = (MozillaSpamToken) mergedSet.tailSet(tempToken).first();

				// Add the bad token count in the old list to the
				// bad token count in the merged list.
				goodToken.setBadTokenCount(
					goodToken.getBadTokenCount() +
					tempToken.getBadTokenCount()
				);

				// Add the good token count in the old list to the
				// god token count in the merged list.
				goodToken.setGoodTokenCount(
					goodToken.getGoodTokenCount() +
					tempToken.getGoodTokenCount()
				);

			} else {

				// If it's not in the set, add it in.
				mergedSet.add(tempToken);
			}
		}

		// Blank these out to help the garbage collector realize
		// that references no longer exist to these objects.
		tempToken = null;
		goodToken = null;

		return mergedSet;
	}

	/** Write the given token data to the given output file in
	 * 	the given output format.
	 *
	 * @param outputFile	The file to which we will write the
	 * 						text output.
	 * @param trainer	The TrainingData containing the token
	 * 					data which is to be output as text.
	 * @param outputType	The format of the output. This can be
	 * 						one of OUTPUT_TEXT, OUTPUT_HTML,
	 * 						OUTPUT_XML, or OUTPUT_DATA.
	 *
	 * @throws IOException If an error was encountered while
	 * 						writing to the output file.
	 */
	public static void writeOutput(
			File outputFile,
			TrainingData trainer,
			int outputType
	) throws IOException {

		// Erase the output file if it exists
		if (outputFile.exists()) {

			outputFile.delete();
		}

		// Create a new output file.
		outputFile.createNewFile();

		// Create a stream for that output file. This is a
		// BufferedOutputStream so that we can gain the
		// advantages of doing large writes while at the same
		// time preventing the errors that arose on some clients
		// where an OutOfMemory error was encountered due to
		// large token files resulting in large output sets
		// stored in a StringBuffer. Fix recommended by Jeffrey
		// Siegal, see http://mozdev.org/bugs/show_bug.cgi?id=3943
		BufferedOutputStream outStream = new BufferedOutputStream(new FileOutputStream(outputFile), TrainingData.OUTPUT_BUFFER_SIZE);

		switch (outputType) {

			case OUTPUT_TEXT:

				// Write the token data to a plaintext output file
				trainer.toTextDocument(outStream);

				break;

			case OUTPUT_HTML:

				// Write the token data to an HTML output file
				trainer.toHTMLDocument(outStream);

				break;

			case OUTPUT_XML:

				// Write the token data to an XML output file
				trainer.toXMLDocument(outStream);

				// Write out the DTD for this XML file.
				trainer.writeXMLDTD(new File(outputFile.getParentFile(), "trainer_xml.dtd"));

				break;

			case OUTPUT_DATA:

				// Write the token data to a well-formed data file
				trainer.outputTrainingDataFileContents(outStream);

				break;

			default:

				// What the? Unknown format output type.
				System.err.println("Unknown format output type: " + outputType);
		}

		// Flush the output and close the file stream.
		outStream.flush();
		outStream.close();
	}

	/** This is the main entry point of this class. This is a
	 *  program which interprets a bunch of command-line
	 *  parameters and launches assorted portions of the program
	 *  depending upon what is requested.
	 *
	 * @param args The command-line arguments which were passed
	 * 				to this program.
	 */
	public static void main(String[] args) {

		// Declare assorted commandline-accessible variables
		String trainingDatPath = null;
		String outputFilename = null;
		String mergeFilename = null;
		boolean displayOutput = true;
		int outputType = -1;
		boolean launchGUI = false;
		boolean verFlag = false;
		boolean helpFlag = false;
		int removeGood = -1;
		int removeBad = -1;

		// A useful flag for determining if an error has occurred
		// during parsing of the command-line.
		boolean dupFlag = false;

		// Catch any ArrayIndexOutOfBoundsExceptions that occur
		// so that we can recover gracefully from them.
		try {

			// Parse through the passed-in commandline and see if
			// The arguments passed in were valid.
			for (int i = 0; i < args.length; i++) {

				// I wish there was a more elegant method than a very
				// large group of if/else statements, but switch will
				// only work on primitives, not on Strings.

				// Check for the 'silent' argument
				if (args[i].equalsIgnoreCase("-q") ||
					args[i].equalsIgnoreCase("--quiet")) {

					// If displayOutput is already false, we've seen
					// this argument before. Set the dupFlag.
					if (displayOutput) {

						displayOutput = false;

					} else {

						dupFlag = true;
					}

				// Check for the 'help' argument
				} else if (args[i].equalsIgnoreCase("-h") ||
							args[i].equalsIgnoreCase("-?") ||
							args[i].equalsIgnoreCase("--help")) {

					if (helpFlag) {

						dupFlag = true;

					} else {

						helpFlag = true;
					}

				// Check for the 'version' argument
				} else if (args[i].equalsIgnoreCase("-v") ||
							args[i].equalsIgnoreCase("--version")) {


					if (verFlag) {

						dupFlag = true;

					} else {

						verFlag = true;
					}

				// Check for the 'GUI' argument
				} else if (args[i].equalsIgnoreCase("-g") ||
							args[i].equalsIgnoreCase("--gui")) {

					// If launchGUI is already false, we've seen
					// this argument before. Set the dupFlag.
					if (launchGUI) {

						dupFlag = true;

					} else {

						launchGUI = true;
					}

				// Check for the 'output type' argument.
				} else if (args[i].equalsIgnoreCase("-f") ||
							args[i].equalsIgnoreCase("--format")) {

					if (args[i+1].equalsIgnoreCase("text")) {

						outputType = OUTPUT_TEXT;

					} else if (args[i+1].equalsIgnoreCase("html")) {

						outputType = OUTPUT_HTML;

					} else if (args[i+1].equalsIgnoreCase("xml")) {

						outputType = OUTPUT_XML;

					} else if (args[i+1].equalsIgnoreCase("data")) {

						outputType = OUTPUT_DATA;

					} else {

						System.err.println("An invalid output type was specified: " + args[i+1]);
						System.err.println("Acceptable values are xml, html, text, and data.");
						System.err.println(PROGRAM_USAGE);
						System.exit(INVALID_ARG);
					}

					// Increment i since we already handled the
					// next argument during the handling of this
					// one.
					i++;

				// Check for the 'output file' argument.
				} else if (args[i].equalsIgnoreCase("-o") ||
							args[i].equalsIgnoreCase("--outputfile")) {

					// If outputFilename is not null, we've
					// seen this argument before. Set the dupFlag.
					if (outputFilename == null) {

						// Set the output filename.
						outputFilename = args[i+1];

					} else {

						dupFlag = true;
					}

					// Increment i since we already handled the
					// next argument during the handling of this
					// one.
					i++;

				// Check for the 'merge' argument.
				} else if (args[i].equalsIgnoreCase("-m") ||
							args[i].equalsIgnoreCase("--merge")) {

					// If mergeFilename is not null, we've seen
					// this argument before. Set the dupFlag.
					if (mergeFilename == null) {

						// Set the merge filename.
						mergeFilename = args[i+1];

					} else {

						dupFlag = true;
					}

					// Increment i since we already handled the
					// next argument during the handling of this
					// one.
					i++;

				// Check for the 'input file' argument.
				} else if (args[i].equalsIgnoreCase("-i") ||
							args[i].equalsIgnoreCase("--inputfile")) {

					// If trainingDatPath is not null, we've
					// seen this argument before. Set the dupFlag.
					if (trainingDatPath == null) {

						// Set the threshhold for the good tokens.
						trainingDatPath = args[i+1];

					} else {

						dupFlag = true;
					}

					// Increment i since we already handled the
					// next argument during the handling of this
					// one.
					i++;

				// Check for the 'remove good' argument.
				} else if (args[i].equalsIgnoreCase("-rg") ||
							args[i].equalsIgnoreCase("--remove-good")) {

					// If removeGood is not -1, we've seen this
					// argument before. Set the dupFlag.
					if (removeGood == -1) {

						// Set the threshhold for the good tokens.
						removeGood = Integer.parseInt(args[i+1]);

					} else {

						dupFlag = true;
					}

					// Increment i since we already handled the
					// next argument during the handling of this
					// one.
					i++;

				// Check for the 'remove bad' argument.
				} else if (args[i].equalsIgnoreCase("-rb") ||
							args[i].equalsIgnoreCase("--remove-bad")) {

					// If removeBad is not -1, we've seen this
					// argument before. Set the dupFlag.
					if (removeBad == -1) {

						// Set the threshhold for the bad tokens.
						removeBad = Integer.parseInt(args[i+1]);

					} else {

						dupFlag = true;
					}

					// Increment i since we already handled the
					// next argument during the handling of this
					// one.
					i++;

				// If we've gotten here, we have an invalid argument.
				} else {

					System.err.println("An invalid argument was specified: " + args[i]);
					System.err.println(PROGRAM_USAGE);
					System.exit(INVALID_ARG);
				}

				if (dupFlag) {

					System.err.println("An duplicate argument was encountered: " + args[i]);
					System.err.println(PROGRAM_USAGE);
					System.exit(INVALID_ARG);
				}
			}

		// We'll get this if we try to find a secont part of an
		// argument that wasn't given, e.g. if the user enters
		// 'programName -f xml -o' and does not specify the
		// output file.
		} catch (ArrayIndexOutOfBoundsException aioobe) {

			System.err.println("An argument was not specified for: " + args[args.length-1]);
			System.err.println(PROGRAM_USAGE);
			System.exit(INVALID_ARG);

		// We'll get this if some specified -rg or -rb and then
		// didn't follow it with an integer.
		} catch (NumberFormatException nfe) {

			System.err.println(nfe.getMessage());
			System.err.println(PROGRAM_USAGE);
			System.exit(INVALID_ARG);
		}

		if (verFlag) {

			if (launchGUI) {

				// Display the About box without a parent frame.
				AboutBox.display(null);

			} else {

				// Write out the version string.
				System.out.println(VER_STRING);
			}
		}

		if (helpFlag) {

			if (launchGUI) {

				// Display the help text in a Dialog box.
				JOptionPane.showMessageDialog(null, PROGRAM_USAGE, "Bayes Junk Tool Help", JOptionPane.PLAIN_MESSAGE);

			} else {

				// Write out the help text.
				System.out.println(PROGRAM_USAGE);
			}
		}

		// If we had one of these flags, then exit now.
		if (helpFlag || verFlag) {

			System.exit(SUCCESS);
		}

		// If we haven't specified an output file, launch the GUI.
		if (outputFilename == null) {

			launchGUI = true;
		}

		// If we didn't specify an outputType, default to Text.
		if (outputType == -1) {

			outputType = OUTPUT_TEXT;
		}

		try {

			File inputFile = null;

			// If we don't have an input filename, get it now.
			if (trainingDatPath == null) {

				if (launchGUI) {

					// Launch a file chooser dialog that
					// gets the filename from the user.
					JFileChooser fc = new JFileChooser();
					fc.setDialogTitle("Select Mozilla Bayesian Filter training.dat");

					int returnVal = fc.showOpenDialog(null);

					if (returnVal == JFileChooser.APPROVE_OPTION) {

						inputFile = fc.getSelectedFile();
						trainingDatPath = inputFile.getAbsolutePath();

					} else {

						// Can't read from a file that doesn't exist.
						System.err.println("An input file must be specified.");
						System.exit(GEN_ERROR);

					}

				} else {

					// Prompt the user for an input filename, and
					// read it in from System.in
					System.out.println("Please specify the location of the Mozilla Bayesian Filter training file:");
					trainingDatPath = new BufferedReader(new InputStreamReader(System.in)).readLine();
					inputFile = new File(trainingDatPath);
				}

			} else {

				inputFile = new File(trainingDatPath);
			}

			FileInputStream inStream = null;

			// Check that the input file exists.
			if (!inputFile.exists()) {

				// Can't read from a file that doesn't exist.
				System.err.println(trainingDatPath + " does not exist!");
				System.exit(GEN_ERROR);

			}

			if (displayOutput) {

				System.out.println("Checking if " + trainingDatPath + " is a Mozilla token file...");
			}

			// Convert the training data file to a TrainingData
			// for easier manipulation.
			TrainingData trainer = parseTrainingFile(trainingDatPath, displayOutput);

			// Wasn't binary, try XML.
			if (trainer == null) {

				if (displayOutput) {

					System.out.println("Checking if " + trainingDatPath + " is an XML token file...");
				}

				try {

					trainer = parseXMLTrainingFile(trainingDatPath, displayOutput);

				} catch (ParserConfigurationException pce) {

					// Do nothing, it'll be handled after this
					// try/catch block.

				} catch (SAXException se) {

					// Do nothing, it'll be handled after this
					// try/catch block.
				}

				// If it's still null,then it was an invalid file.
				if (trainer == null) {

					System.err.println("An error was encountered while reading " + trainingDatPath);
					System.exit(GEN_ERROR);
				}
			}

			if (mergeFilename != null) {

				// If we passed in a merge filename, then merge
				// it with the training.dat now.
				File mergeFile = new File(mergeFilename);

				String mergeFilePath = mergeFile.getAbsolutePath();

				try {

					if (displayOutput) {

						System.out.println("Checking if " + mergeFilePath + " is a Mozilla token file...");
					}

					// Create a new TrainingData from the chosen
					// file.
					TrainingData newTrainer = Analyzer.parseTrainingFile(mergeFilePath, displayOutput);

					// Wasn't binary, try XML.
					if (newTrainer == null) {

						if (displayOutput) {

							System.out.println("Checking if " + mergeFilePath + " is an XML token file...");
						}

						newTrainer = Analyzer.parseXMLTrainingFile(mergeFilePath, displayOutput);
					}

					if (newTrainer == null) {

						if (displayOutput) {

							// If it's still null, it wasn't a
							// valid file.
							System.out.println(mergeFilePath + " was not a valid Mozilla token file or XML token file.");
						}

					} else {

						// Set the new good message count.
						newTrainer.setGoodMessageCount(
							newTrainer.getGoodMessageCount() +
							trainer.getGoodMessageCount()
						);

						// Set the new bad message count.
						newTrainer.setBadMessageCount(
							newTrainer.getBadMessageCount() +
							trainer.getBadMessageCount()
						);

						// Assign the new token set.
						newTrainer.setTokenSet(
							mergeTokenLists(
								newTrainer.getTokenSet(),
								trainer.getTokenSet()
							)
						);

						// Blow away the old TrainingData and
						// assign a new one.
						trainer = newTrainer;

						if (displayOutput) {

							System.out.println("Merge complete!");
						}
					}
				} catch (Exception e) {

					// An error occurred, let the user see the
					// message. Normally I wouldn't catch
					// Exception, but I don't really feel like
					// doing the exact same thing for all
					// three types of Exceptions that could be
					// caught here.

					if (displayOutput) {

						System.out.println(e.getMessage());
						System.out.println(mergeFilePath + " was not a valid Mozilla token file or XML token file.");
					}
				}
			}

			// This will trim the token list according to the
			// user's wishes. Since the method ignores negative
			// values and we set these two values to be negative
			// by default, if they aren't set then this will do
			// nothing.
			trainer.removeTokens(removeGood, removeBad);

			if (launchGUI) {

				if (displayOutput) {

					// Console status message
					System.out.println("Launching GUI...");
				}

				// Create and show the GUI.
				TableWindow t = new TableWindow(trainer, trainingDatPath);
				t.show();

				// Set the outputFilename with the TableWindow if
				// one was passed in via the command-line.
				t.setOutputFilename(outputFilename);

				// This will block until the window closes.
				t.waitForWindow();

			} else {

				File outputFile = new File(outputFilename);

				if (displayOutput) {

					// Console status message
					System.out.println("Writing tokens to " + outputFilename);
				}

				// Write the token data to an output file with the
				// specified format.
				writeOutput(outputFile, trainer, outputType);

				if (displayOutput) {

					// Console status message
					System.out.println("Program complete. Output has been written to " + outputFilename);
				}
			}

		} catch (IOException ioe) {
			ioe.printStackTrace();
			System.exit(GEN_ERROR);
		}

		// Do this so we know for sure what error code it returns.
		// Also allows one to specify a different error code to
		// represent a successful execution.
		System.exit(SUCCESS);
	}
}
