/*
 * Created on 12-Jul-2003 by Straxus
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

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.border.Border;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;

/** This is a simple Dialog box that allows the user to choose
 * 	what kinds of tokens to remove from the list.
 *
 * @author Straxus
 */
public class RemoveTokenWindow extends JDialog {

	/** The default title of this window.
	 */
	private static final String WINDOW_TITLE = "Please Select Range Of Tokens To Remove";

	/** The initial width of this window.
	 */
	private static final int WINDOW_WIDTH =  400;

	/** The initial height of this window.
	 */
	private static final int WINDOW_HEIGHT = 150;

	/** The size of the number-entry JTextFields.
	 */
	private static final int textFieldSize = 5;

	/** The field in which the good token count is entered.
	 */
	private JTextField goodTokenNumField;

	/** The field in which the bad token count is entered.
	 */
	private JTextField badTokenNumField;

	/** The checkbox which determines whether the good token
	 *  count which has been entered is to be used.
	 */
	private JCheckBox goodTokenBox;

	/** The checkbox which determines whether the bad token count
	 *  which has been entered is to be used.
	 */
	private JCheckBox badTokenBox;

	/** This limits a JTextField to integer input. This is borrowed
	 *  from the Java Swing tutorial, and the exact file is found at:
	 *  http://java.sun.com/docs/books/tutorial/uiswing/components/example-swing/LimitedStyledDocument.java
	 */
	private class LimitedDocument extends DefaultStyledDocument {

		public void insertString(int offs, String str, AttributeSet a) throws BadLocationException {

			//This rejects the entire insertion if it would make
			//the contents a non-integer value.
			try {

				// If this isn't an integer, then this will throw
				// an exception and the next line won't execute.
				Integer.parseInt(str);

				super.insertString(offs, str, a);

			} catch (NumberFormatException nfe) {
				// Don't add this, it wasn't a number.
			}
		}
	};

	/** Creates a new RemoveTokenWindow which is modal on the
	 * 	given parent TableWindow.
	 *
	 * @param parent	The parent TableWindow to make this Dialog
	 * 					modal on.
	 */
	public RemoveTokenWindow(TableWindow parent) {

		// Make this a modal window with the given title.
		super(parent, WINDOW_TITLE, true);

		setContentPane(new JScrollPane());

		// Set the preferred size
		setSize(WINDOW_WIDTH, WINDOW_HEIGHT);

		GridBagConstraints gbc = new GridBagConstraints();

		// Keep a reference to our viewport panel for ease of use
		JPanel vp = new JPanel();

		((JScrollPane) getContentPane()).getViewport().add(vp);

		vp.setLayout(new GridBagLayout());

		Border raisedBevel = BorderFactory.createEtchedBorder();

		// Create fields in which the user can enter what the
		// limits on token removal are.
		goodTokenNumField = new JTextField(new LimitedDocument(), "0", textFieldSize);
		goodTokenNumField.setEditable(true);
		goodTokenNumField.setBorder(raisedBevel);

		badTokenNumField = new JTextField(new LimitedDocument(), "0", textFieldSize);
		badTokenNumField.setEditable(true);
		badTokenNumField.setBorder(raisedBevel);

		// Add labels to explain the fields.
		JLabel goodTokenLabel = new JLabel("Good Token Count is strictly less than ");
		JLabel badTokenLabel = new JLabel("Bad Token Count is strictly less than ");

		goodTokenLabel.setForeground(Color.black);
		badTokenLabel.setForeground(Color.black);

		// Set up the properties for the top label.
		gbc.fill = GridBagConstraints.NONE;
		gbc.anchor = GridBagConstraints.CENTER;
		gbc.gridwidth = 3;
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.weightx = 1.0;
		gbc.weighty = 1.0;

		// Add the top label.
		vp.add(new JLabel("Remove if:"), gbc);

		// Set up the properties for the Good Token label.
		gbc.anchor = GridBagConstraints.EAST;
		gbc.gridy = 1;
		gbc.gridwidth = 1;
		gbc.weightx = 3.0;

		// Add the Good Token label.
		vp.add(goodTokenLabel, gbc);

		// Change properties for the Good Token number field.
		gbc.anchor = GridBagConstraints.WEST;
		gbc.weightx = 1.0;
		gbc.gridx = 1;

		// Add the Good Token number field.
		vp.add(goodTokenNumField, gbc);

		// Add in a checkbox for this field, and make it selected
		// by default.
		goodTokenBox = new JCheckBox();
		goodTokenBox.setSelected(true);
		goodTokenBox.setEnabled(true);

		// Change properties for the Bad Token checkbox.
		gbc.anchor = GridBagConstraints.WEST;
		gbc.gridx = 2;

		// Add the Bad Token checkbox.
		vp.add(goodTokenBox, gbc);

		// Set some properties for our spacer label.
		gbc.weightx = 1.0;
		gbc.gridwidth = 3;
		gbc.anchor = GridBagConstraints.CENTER;
		gbc.gridx = 0;
		gbc.gridy = 2;

		// Add a spacer label.
		vp.add(new JLabel("AND"), gbc);

		// Change properties for the Bad Token label.
		gbc.gridwidth = 1;
		gbc.anchor = GridBagConstraints.EAST;
		gbc.weightx = 3.0;
		gbc.gridx = 0;
		gbc.gridy = 3;

		// Add the Bad Token label.
		vp.add(badTokenLabel, gbc);

		// Change properties for the Bad Token number field.
		gbc.anchor = GridBagConstraints.WEST;
		gbc.weightx = 1.0;
		gbc.gridx = 1;

		// Add the Bad Token number field.
		vp.add(badTokenNumField, gbc);

		// Add in a checkbox for this field, and make it selected
		// by default.
		badTokenBox = new JCheckBox();
		badTokenBox.setSelected(true);
		badTokenBox.setEnabled(true);

		// Change properties for the Bad Token checkbox.
		gbc.anchor = GridBagConstraints.WEST;
		gbc.gridx = 2;

		// Add the Bad Token checkbox.
		vp.add(badTokenBox, gbc);

		// Add the buttons to a panel so that they are properly
		// aligned with respect to one another. This panel will
		// then be arranged in the parent window however GridBag
		// wants without breaking the button spacing.
		JPanel buttonPanel = new JPanel();

		JButton okButton = new JButton("   OK   ");

		// Create a final reference to these so that we can use
		// them inside the ActionListener inner class.
		final TrainingData finalTrainer = parent.getTableWindowTrainer();
		final TableWindow finalParent = parent;

		okButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent ae) {

				int goodCount = -1;
				int badCount = -1;

				// Only take the counts if the checkboxes are
				// selected.
				if (goodTokenBox.isSelected()) {
					goodCount = Integer.parseInt(goodTokenNumField.getText());
				}

				if (badTokenBox.isSelected()) {
					badCount = Integer.parseInt(badTokenNumField.getText());
				}

				// Execute the actual removal of the tokens.
				finalTrainer.removeTokens(
					goodCount,
					badCount
				);

				// Reset the data view in the parent.
				finalParent.setNewTrainingData(finalTrainer);

				// Get rid of this window.
				getRemoveTokenWindow().hide();
				getRemoveTokenWindow().dispose();
			}

		});

		JButton cancelButton = new JButton("   Cancel   ");

		cancelButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent ae) {

				// Get rid of this window.
				getRemoveTokenWindow().hide();
				getRemoveTokenWindow().dispose();
			}

		});

		// Add the buttons to pur panel, with a space between
		// them.
		buttonPanel.add(okButton);
		buttonPanel.add(new JLabel("   "));
		buttonPanel.add(cancelButton);

		// Set some properties for our button panel.
		gbc.anchor = GridBagConstraints.CENTER;
		gbc.weightx = 1.0;
		gbc.gridwidth = 3;
		gbc.gridx = 0;
		gbc.gridy = 4;

		// Add the button panel to the dialog.
		vp.add(buttonPanel, gbc);

		addWindowListener(new WindowAdapter() {

			// Ensure that the proper methods are called when this
			// window is closed.
			public void windowClosing (WindowEvent e) {

				// Get rid of this window.
				getRemoveTokenWindow().hide();
				getRemoveTokenWindow().dispose();
			}
		});
	}

	/** Returns a reference to this object. Used for anonymous
	 *  inner classes.
	 *
	 * @return A reference to this object.
	 */
	private RemoveTokenWindow getRemoveTokenWindow() {
		return this;
	}
}
