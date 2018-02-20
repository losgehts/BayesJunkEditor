/*
 * Created on 20-Jun-2003 by Straxus
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

import javax.swing.JOptionPane;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;

/** This is a data model for the TableWindow's JTable. It keeps
 *  track of the displayed data, and ensures that changes to the
 *  data are handled in a smart manner.
 *
 * @author Straxus
 */
public class TrainingDataTableModel
	extends AbstractTableModel
	implements TableModelListener {

	/** An array of the names of all of the columns in this table.
	 */
	private String colNames[] = {
		"Token",
		"Good",
		"Good %",
		"Bad",
		"Bad %"
	};

	/** A reference to the underling TrainingData from which our
	 *  data is drawn.
	 */
	private TrainingData parentTrainer = null;

	/** A copy of the token set in TrainingData, put into array
	 *  form for speed of access and ease of use.
	 */
	private MozillaSpamToken[] tokenSet = null;

	/** Creates a new TrainingDataTableModel for the given
	 *  TrainingData.
	 *
	 * @param trainer	The TrainingData to draw data from for
	 * 					this table.
	 */
	public TrainingDataTableModel(TrainingData trainer) {

		setNewTrainingData(trainer);
	}

	/** This method retrieves a reference to the TrainingData that
	 *  this TrainingDataTableModel is based on.
	 *
	 * @return The TrainingData associated with this TableModel.
	 */
	public TrainingData getTrainingData() {

		return parentTrainer;
	}

	/** Associates a new TrainingData with this
	 * 	TrainingDataTableModel. The cached data is removed, and
	 *  all associated TableModelListeners are informed of the
	 *  change.
	 *
	 * @param newTrainer The new TrainingData to draw data from.
	 */
	public void setNewTrainingData(TrainingData newTrainer) {

		if (newTrainer == null) {

			// Bail out, someone screwed up.
			return;
		}

		Object[] tempArray = newTrainer.getTokenSet().toArray();
		tokenSet = new MozillaSpamToken[tempArray.length];

		// Can't use System.arraycopy since an Object is not
		// compatible with a MozillaSpamToken via simple
		// assignment - it requires a cast.
		for (int i = 0; i < tokenSet.length; i++) {

			tokenSet[i] = (MozillaSpamToken) tempArray[i];
		}

		parentTrainer = newTrainer;

		// Let everyone know that the table has changed.
		fireTableChanged(new TableModelEvent(this));
	}

	/** Overrides getColumnName(int) in
	 * 	javax.swing.table.TableModel.
	 *
	 * @see javax.swing.table.TableModel#getColumnName(int)
	 */
	public String getColumnName(int col) {
		return colNames[col];
	}

	/** Overrides getRowCount() in javax.swing.table.TableModel.
	 *
	 * @see javax.swing.table.TableModel#getRowCount()
	 */
	public int getRowCount() {
		return tokenSet.length;
	}

	/** Overrides getColumnCount() in javax.swing.table.TableModel.
	 *
	 * @see javax.swing.table.TableModel#getColumnCount()
	 */
	public int getColumnCount() {
		return colNames.length;
	}

	/** Overrides getValueAt(int, int) in
	 *  javax.swing.table.TableModel.
	 *
	 * @see javax.swing.table.TableModel#getValueAt(int, int)
	 */
	public Object getValueAt(int row, int col) {

		// No need for any breaks since we are returning
		// inside ease case statement.
		switch (col) {

			case 0:

				return tokenSet[row].getTokenString();

			case 1:

				return new Integer(tokenSet[row].getGoodTokenCount());

			case 2:
				
				if (parentTrainer.getGoodMessageCount() == 0)
					return new Float(0);				

				float tempGoodFl =
					(float) tokenSet[row].getGoodTokenCount() /
					(float) parentTrainer.getGoodMessageCount();

				// Multiply the float by 100 to convert to a
				// format tha tcan be displayed with %, and
				// by a further 100,000 to keep the last 5
				// digits after the decimal when we take
				// the ceiling of the number.
				tempGoodFl = tempGoodFl * 10000000;
				tempGoodFl = (float) Math.ceil(tempGoodFl);
				tempGoodFl = tempGoodFl / (float) 100000;

				return new Float(tempGoodFl);

			case 3:

				return new Integer(tokenSet[row].getBadTokenCount());

			case 4:
				
				if (parentTrainer.getBadMessageCount() == 0)
					return new Float(0);

				float tempBadFl =
					(float) tokenSet[row].getBadTokenCount() /
					(float) parentTrainer.getBadMessageCount();

				// Multiply the float by 100 to convert to a
				// format tha tcan be displayed with %, and
				// by a further 100,000 to keep the last 5
				// digits after the decimal when we take
				// the ceiling of the number.
				tempBadFl = tempBadFl * 10000000;
				tempBadFl = (float) Math.ceil(tempBadFl);
				tempBadFl = tempBadFl / (float) 100000;

				return new Float(tempBadFl);
		}

		// If we've gotten here, we've passed in a wierd
		// column number.
		return null;
	}

	/** Overrides isCellEditable(int, int) in
	 *  javax.swing.table.TableModel.
	 *
	 * @see javax.swing.table.TableModel#isCellEditable(int, int)
	 */
	public boolean isCellEditable(int row, int col) {

		// Can't edit the probability or token string columns, but
		// can edit the rest of the columns.
		if ((col == 0) || (col == 2) || (col == 4)) {

			return false;

		} else {

			return true;
		}

	}

	/** Overrides getColumnClass(int) in
	 *  javax.swing.table.TableModel.
	 *
	 * @see javax.swing.table.TableModel#getColumnClass(int)
	 */
	public Class getColumnClass(int c) {
		return getValueAt(0, c).getClass();
	}

// Implementation of the TableModelListener interface,

	 // By default forward all events to all the listeners.
	 /** Overrides tableChanged(javax.swing.event.TableModelEvent)
	  *  in javax.swing.event.TableModelListener.
	  *
	  * @see javax.swing.event.TableModelListener#tableChanged(javax.swing.event.TableModelEvent)
	  */
	public void tableChanged(TableModelEvent e) {
		fireTableChanged(e);
	}

	/** Removes a given set of rows from both the displayed data
	 *  and the underlying TrainingData.
	 *
	 * @param rows The rows to be removed.
	 */
	public void removeRows(int[] rows) {

		// Someone screwed up, just ignore the call.
		if (rows == null ||
			rows.length == 0 ||
			rows.length >= getRowCount()) {
			return;
		}

		int highNum = -1;
		int lowNum = -1;

		lowNum = rows[0];
		highNum = rows[rows.length-1];

		// The order is reversed, so compensate.
		if (lowNum > highNum) {

			lowNum = highNum;
			highNum = rows[0];
		}

		MozillaSpamToken[] newTokenSet = new MozillaSpamToken[tokenSet.length - (highNum - lowNum + 1)];

		// Copy the items before and after the selected row.
		System.arraycopy(tokenSet, 0, newTokenSet, 0, lowNum);
		System.arraycopy(tokenSet, highNum+1, newTokenSet, lowNum, tokenSet.length - highNum - 1);

		// Remove these tokens from the parent TrainingData.
		for (int i = 0; i < rows.length; i++) {

			parentTrainer.getTokenSet().remove(tokenSet[rows[i]]);
		}

		// Save the new token set which has the rows removed.
		tokenSet = newTokenSet;

		// Let the container know that these rows are now gone.
		fireTableRowsDeleted(lowNum, highNum);
		fireTableDataChanged();
	}

	/** Creates a new MozillaSpamToken for the given token, and
	 * 	adds it to the TrainingData. Also adds it to the cached
	 *  copy of the data.
	 *
	 * @param newString The new token to be added.
	 */
	public void addRow(String newString) {

		// Create a new token for this string.
		MozillaSpamToken newToken = new MozillaSpamToken(newString, 0, 0);

		// Need to check that the given string does not yet exist
		// in the parent TrainingData.
		if (parentTrainer.getTokenSet().contains(newToken)) {

			// Pop up a dialog box saying that the given token
			// already exists in the set.
			JOptionPane.showMessageDialog(null, "The given token already exists in this set of tokens.");

			return;
		}

		// Create a new array for our n + 1 tokens.
		MozillaSpamToken[] newTokenSet = new MozillaSpamToken[tokenSet.length + 1];

		// Copy the contents of the previous array into the new
		// one.
		System.arraycopy(tokenSet, 0, newTokenSet, 0, tokenSet.length);

		// Since it wasn't already in the set, add it in now.
		parentTrainer.getTokenSet().add(newToken);

		// Add the new token to the end of the array.
		newTokenSet[newTokenSet.length - 1] = newToken;

		// Save the new token set which has the row added.
		tokenSet = newTokenSet;

		// Let the container know that a row has beed added.
		fireTableRowsInserted(tokenSet.length - 1, tokenSet.length - 1);
		fireTableDataChanged();
	}

	/** Overrides setValueAt(java.lang.Object, int, int) in
	 *  javax.swing.table.TableModel
	 * @see javax.swing.table.TableModel#setValueAt(java.lang.Object, int, int)
	 */
	public void setValueAt(Object value, int row, int col) {

		// These are the token string and probability columns and
		// shouldn't be editable!
		if ((col == 0) || (col == 2) || (col == 4)) {
			return;
		}

		// This will retrieve the original MozillaSpamToken that
		// the table's array was copied from.
		MozillaSpamToken parentToken = (MozillaSpamToken)
			parentTrainer.getTokenSet().tailSet(tokenSet[row]).first();

		// If the value of this integer is less than 0, set it to
		// 0. This prevents negative token counts.
		if (((Integer) value).intValue() < 0) {

			value = new Integer(0);
		}

		// Good token count
		if (col == 1) {

			// Set the new Good count both locally and in the
			// originating data structure.
			tokenSet[row].setGoodTokenCount(((Integer) value).intValue());
			parentToken.setGoodTokenCount(((Integer) value).intValue());

		// Bad token count
		} else if (col == 3) {

			// Set the new Bad count both locally and in the
			// originating data structure.
			tokenSet[row].setBadTokenCount(((Integer) value).intValue());
			parentToken.setBadTokenCount(((Integer) value).intValue());

		} else {

			// What the hell?
			System.err.println("TrainingDataTableModel.setValueAt(): "+
			"WARNING -- Invalid column passed in: " + col);
		}

		// Update both the selected cell and the one after it,
		// which contains its probability.
		fireTableCellUpdated(row, col);
		fireTableCellUpdated(row, col+1);
	}
}
