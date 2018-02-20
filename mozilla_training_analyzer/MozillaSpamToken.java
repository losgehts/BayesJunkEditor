/*
 * Created on 4-Jun-2003
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

/** This class encapsulates a single token which is used in the
 *  classification of emails by the Bayesian Junk Mail Filter.
 *
 * @author Straxus
 */
public class MozillaSpamToken implements Comparable {

	/** The number of times that the given token String occurred
	 *  in good (non-junk) emails.
	 */
	private int goodTokenCount = -1;

	/** The number of times that the given token String occurred
	 *  in bad (junk) emails.
	 */
	private int badTokenCount = -1;

	/** The String upon which the assorted token counts are based.
	 *  This string has been found in an email which the Junk Mail
	 *  filter processed.
	 */
	private String token = null;

	/** Creates a new MozillaSpamToken with the given good and
	 *  bad token counts for the given token String.
	 *
	 * @param newToken The token String which this
	 * 					MozillaSpamToken represents.
	 * @param numGoodTokens 	The number of good (non-spam)
	 * 							occurences of this token.
	 * @param numBadTokens 	The number of bad (spam)
	 * 							occurences of this token.
	 */
	public MozillaSpamToken(String newToken, int numGoodTokens, int numBadTokens) {

		token = newToken;
		goodTokenCount = numGoodTokens;
		badTokenCount = numBadTokens;
	}

	/** Returns the token String which this MozillaSpamToken
	 * 	represents.
	 *
	 * @return The token String which this MozillaSpamToken
	 * 			represents.
	 */
	public String getTokenString() {
		return token;
	}

	/** Returns the number of good (non-spam) occurences of this
	 * 	token.
	 *
	 * @return The number of good (non-spam) occurences of this
	 * 			token.
	 */
	public int getGoodTokenCount() {
		return goodTokenCount;
	}

	/** Sets the number of good (non-spam) occurences of this
	 * 	token.
	 *
	 * @param newCount The new number of good (non-spam)
	 * 					occurences of this token.
	 */
	public void setGoodTokenCount(int newCount) {
		goodTokenCount = newCount;
	}

	/** Returns the number of bad (spam) occurences of this token.
	 *
	 * @return The number of bad (spam) occurences of this token.
	 */
	public int getBadTokenCount() {
		return badTokenCount;
	}

	/** Sets the number of bad (spam) occurences of this token.
	 *
	 * @param newCount The new number of bad (spam) occurences
	 * 					of this token.
	 */
	public void setBadTokenCount(int newCount) {
		badTokenCount = newCount;
	}

	/** Returns a string representation of this token's information.
	 *
	 * @return A string representation of this token's information.
	 *
	 * @see java.lang.Object#toString()
	 */
	public String toString() {

		return "[" +
				token +
				" - " +
				goodTokenCount +
				" good tokens, " +
				badTokenCount +
				" bad tokens]";
	}

	/** Compares the given Object to see if it is equal to, greater
	 *  than, or less than this MozillaSpamToken.
	 *
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	public int compareTo(Object o) throws ClassCastException {

		// compareTo specifies in its contract that it will
		// throw a ClassCastException if the Object passed in
		// is not comparable to this one, so we'll take advantage
		// of that here - if it's not a MozillaSpamToken, it's
		// not comparable.
		MozillaSpamToken mst = (MozillaSpamToken) o;

		// Since Strings have already implemented comparable, and
		// since the token String uniquely identifies and specifies
		// order for a MozillaSpamToken, use it here.
		return this.token.compareTo(mst.token);
	}

	/** Compares the given Object to see if it is equal to this
	 *  MozillaSpamToken.
	 *
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object o) {

		// If it's not a MozillaSpamToken, it's not equal.
		if (!(o instanceof MozillaSpamToken)) {

			return false;
		}

		// If the token strings are equal, these two objects are
		// equal. Otherwise, they are not.
		if (this.getTokenString().equals(((MozillaSpamToken) o).getTokenString())) {

			return true;

		} else {

			return false;
		}
	}
}
