/*
 * Created on Jul 17, 2003 by Straxus
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

import java.awt.Component;

import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JOptionPane;

/** This is a simple wrapper for the About Box which is displayed
 *  either by the GUI or by the Analyzer in response to the
 *  correct set of flags on the command-line (--gui --version).
 *
 * @author Straxus
 */
public class AboutBox extends JDialog {

	// Declare this here so that we create it once, not
	// every time we hit the About button.
	private static final ImageIcon aboutImage = new ImageIcon("images/logo.gif");

	// No instantiation of this class.
	private AboutBox() {
	}

	/** This method displays the actual About box.
	 *
	 * @param parentComponent	The parent frame of this AboutBox.
	 * 							It is safe to make this null if
	 * 							there is no parent.
	 */
	public static void display(Component parentComponent) {

		// Display the About box.
		JOptionPane.showMessageDialog(
			parentComponent,
			Analyzer.VER_STRING + "\nhttp://bayesjunktool.mozdev.org",
			"About",
			JOptionPane.PLAIN_MESSAGE,
			aboutImage
		);
	}
}
