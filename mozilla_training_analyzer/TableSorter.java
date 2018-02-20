/*
 * Borrowed from the Sun Java Tutorial - Originally found on
 * http://java.sun.com/docs/books/tutorial/uiswing/components/table.html
 * adapted with slight modifications. I have removed methods which
 * are not used by the Mozilla traning file analyzer. I have also
 * optimized several methods and added JavaDoc comments which
 * weren't there before. There's definitely a lot of room for
 * improvement in this class, but for now it works so I'm happy
 * with it. Maybe in the future I'll pretty it up.
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

import java.awt.Cursor;
import java.awt.event.InputEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Vector;

import javax.swing.JTable;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

/**
 * A sorter for TableModels. The sorter has a model (conforming to TableModel)
 * and itself implements TableModel. TableSorter does not store or copy
 * the data in the TableModel, instead it maintains an array of
 * integers which it keeps the same size as the number of rows in its
 * model. When the model changes it notifies the sorter that something
 * has changed eg. "rowsAdded" so that its internal array of integers
 * can be reallocated. As requests are made of the sorter (like
 * getValueAt(row, col) it redirects them to its model via the mapping
 * array. That way the TableSorter appears to hold another copy of the table
 * with the rows in a different order. The sorting algorthm used is stable
 * which means that it does not move around rows when its comparison
 * function returns 0 to denote that they are equivalent.
 *
 * @version 1.5 12/17/97
 * @author Philip Milne
 */
public class TableSorter
	extends AbstractTableModel
	implements TableModelListener {

	private int indexes[] = null;
	private Vector sortingColumns = new Vector();
	private boolean ascending = true;
	private TableModel model = null;

    /** Constructs a new TableSorter which will sort the data
     *  found in the given TableModel.
     *
	 * @param newModel The TableModel to sort data in.
	 */
	public TableSorter(TableModel newModel) {

        setModel(newModel);
    }

    /** Associate a new TableModel with this Sorter.
     *
	 * @param newModel The new TableModel to be associated with
	 * 					this Sorter.
	 */
	public void setModel(TableModel newModel) {

		// Keep a reference to the associated TableModel.
        model = newModel;

		// Set our data to match that in the underlying
		// TableModel.
        reallocateIndexes();
    }

	/** This method will return the TableModel's row number for a
	 *  currently displayed row. For example, if the table is
	 *  sorted on one of the columns, the row which is displayed
	 *  5th may be the 23rd row in the underlying TableModel.
	 *
	 * @param row The displayed row to look up.
	 *
	 * @return The row number of the given row in the TableModel.
	 */
	public int lookupRowNum(int row) {

		// Someone screwed up, just bail.
		if (row < 0) {
			return -1;
		}

		// Return the real TableModel row number for the given
		// displayed row.
		return indexes[row];
	}

	/** This method will return the TableModel's row numbers for
	 *  a currently displayed set of rows. For example, if the
	 *  table is sorted on one of the columns, the row which is
	 *  displayed 5th may be the 23rd row in the underlying
	 *  TableModel.
	 *
	 * @param rows The displayed rows to look up.
	 *
	 * @return The array of row numbers of the given rows in the
	 * 			TableModel.
	 */
	public int[] lookupRows(int[] rows) {

		// Someone screwed up, just bail.
		if (rows == null) {
			return null;
		}

		// Create a new array for our return set.
		int[] retRows = new int[rows.length];

		// Loop through the entire array and look up each row.
		for (int i = 0; i < rows.length; i++) {

			retRows[i] = lookupRowNum(rows[i]);
		}

		// Return the new, converted array.
		return retRows;
	}

    /** Compares the given column of two rows to see which is
     * 	greater.
     *
	 * @param row1 The first row to compare.
	 * @param row2 The second row to compare.
	 * @param column	The column of the given rows to base the
	 * 					comparison on.
	 *
	 * @return 0 if the rows are the same, &gt; 0 if row1 is
	 * 			greater than row2, or &lt; 0 if row1 is less than
	 * 			row2.
	 */
	public int compareRowsByColumn(int row1, int row2, int column) {

        Class type = model.getColumnClass(column);
        TableModel data = model;

        // Check for nulls.
        Object o1 = data.getValueAt(row1, column);
        Object o2 = data.getValueAt(row2, column);

        if (o1 == null) { // Define null as less than everything.

			if (o2 == null) {

				return 0;

			} else {

				return -1;
			}

        } else if (o2 == null) {

            return 1;
        }

		// Keeper for our result
		double result = 0.0;

        /*
         * We copy all returned values from the getValue call in case
         * an optimised model is reusing one object to return many
         * values.  The Number subclasses in the JDK are immutable and
         * so will not be used in this way but other subclasses of
         * Number might want to do this to save space and avoid
         * unnecessary heap allocation.
         */

        if (Number.class.isAssignableFrom(type)) {

            double d1 = ((Number) data.getValueAt(row1, column)).doubleValue();
            double d2 = ((Number) data.getValueAt(row2, column)).doubleValue();

			result = d1 - d2;

		} else {

            String s1 = o1.toString();
            String s2 = o2.toString();

			result = s1.compareTo(s2);
		}

		// Interpret the result, and return a number as
		// appropriate.
		if (result < 0.0) {

			return -1;

		} else if (result > 0.0) {

			return 1;

		} else {

			return 0;
		}
    }

    /** Compares two rows to see which is greater.
     *
	 * @param row1 The first row to compare.
	 * @param row2 The second row to compare.
	 *
	 * @return 0 if the rows are the same, &gt; 0 if row1 is
	 * 			greater than row2, or &lt; 0 if row1 is less than
	 * 			row2.
	 */
	public int compare(int row1, int row2) {

        for (int level = 0; level < sortingColumns.size(); level++) {

            Integer column = (Integer) sortingColumns.elementAt(level);

            int result = compareRowsByColumn(row1, row2, column.intValue());

            if (result != 0) {

                return ascending ? result : -result;
            }
        }

        return 0;
    }

	/** A convenience method, this acts the same as
	 * 	reallocateIndexes(null).
	 */
	public void reallocateIndexes() {

		reallocateIndexes(null);
	}

    /** This method handles the updating of the TableSorter's
     * 	data to match changes in the underlying TableModel.
     *
	 * @param e	The TableModelEvent which is causing the
	 *				change in the underlying data. It is OK for
	 *				this parameter to be null if this call is not
	 *				being done as the result of a TableModelEvent,
	 *				for example when this class is first
	 *				initialized.
	 */
	public void reallocateIndexes(TableModelEvent e) {
        int rowCount = model.getRowCount();

		// Added to eliminate an annoying bug with the sorter -
		// any type of TableChange event causes the sorted index
		// to revert back to its original ordering.
		if ((indexes == null) || (rowCount != indexes.length)) {

			// This handles adding of new rows
			if ((e != null) &&
				(e.getType() == TableModelEvent.INSERT) &&
				(rowCount == (indexes.length + 1))) {

				// We're adding a single new row, so keep all of
				// the rest of the rows the same.
				int[] newIndexes = new int[rowCount];

				// Copy the contents of the old array into the new one.
				System.arraycopy(indexes, 0, newIndexes, 0, indexes.length);

				// Add in the new, last row
				newIndexes[rowCount - 1] = rowCount - 1;

				indexes = newIndexes;

				return;
			}

			// This handles removing of rows.
			if ((e != null) &&
				(e.getType() == TableModelEvent.DELETE) &&
				(rowCount == (indexes.length - 1))) {

				// We're removing a single new row, so keep all of
				// the rest of the rows the same
				int[] newIndexes = new int[rowCount];

				// Figure out which row was removed.
				int removedNum = e.getFirstRow();

				// This is used to automatically adjust the index
				// after the bad number has been found.
				int jumpNum = 0;

				// Do the arraycopy manually since we don't know
				// which element has to be removed.
				for (int i = 0; i < indexes.length; i++) {

					if (indexes[i] != removedNum) {

						// This wasn't the bad number, add it.
						// Since everything after removedNum will
						// be decremented by 1, handle that with
						// a simple shorthand if statement.
						newIndexes[i-jumpNum] =
							(indexes[i] > removedNum ? indexes[i] - 1 : indexes[i]);

					} else {

						// This was the bad number, turn on
						// jumpNum.
						jumpNum = 1;
					}
				}

				indexes = newIndexes;

				return;
			}

	        // Set up a new array of indexes with the right number of elements
	        // for the new data model.
	        indexes = new int[rowCount];

	        // Initialise with the identity mapping.
	        for (int row = 0; row < rowCount; row++) {
	            indexes[row] = row;
	        }
		}
    }

    /** Overrides TableChanged(javax.swing.event.TableModelEvent)
     *  in javax.swing.event.TableModelListener.
     *
	 * @see javax.swing.event.TableModelListener#tableChanged(javax.swing.event.TableModelEvent)
	 */
	public void tableChanged(TableModelEvent e) {

        // Reset our data to match that in the underlying
        // TableModel.
        reallocateIndexes(e);

		// Pass this event to any TableModelListeners.
		fireTableChanged(e);
    }

	/** This method confirms that the Sorter's data is still
	 *  consistent with the underlying TableModel's data. If it is
	 * 	not, an error message is written to System.err and the
	 *  method returns false.
	 *
	 * @return true if the Sorter's data is consistent with the
	 * 			underlying TableModel's data, false otherwise.
	 */
    public boolean checkModel() {

    	// If the arrays have different length, then our model is
    	// stale. Return false.
        if (indexes.length != model.getRowCount()) {

            System.err.println("Sorter not informed of a change " +
				"in model. Perhaps the Sorter was not added as a " +
				"TableModelListener to the underlying TableModel.");

			return false;

        } else {

			// Looks fine, return true.
			return true;
		}
    }

    /** This method does the actual sorting of the data.
	 */
	public void sort() {

		// Validate the data model before doing anything. If it's
		// busted, bail out and display a warning.
        if (!checkModel()) {

        	System.err.println("checkModel() returned false, aborting sort().");
			return;
        }

		// Perform the sorting using the shuttlesort algorithm.
        shuttlesort((int[])indexes.clone(), indexes, 0, indexes.length);
    }

    /** This is a home-grown implementation which we have not had time
     *  to research - it may perform poorly in some circumstances. It
     *  requires twice the space of an in-place algorithm and makes
     *  NlogN assigments shuttling the values between the two
     *  arrays. The number of compares appears to vary between N-1 and
     *  NlogN depending on the initial order but the main reason for
     *  using it here is that, unlike qsort, it is stable.
     *
	 * @param from The array to be sorted.
	 * @param to	The location in which to store the new, sorted
	 * 				array.
	 * @param low	The beginning of the range to be sorted,
	 * 				inclusive.
	 * @param high The end of the range to be sorted, exclusive.
	 */
    public void shuttlesort(int from[], int to[], int low, int high) {

        if (high - low < 2) {

            return;
        }

        int middle = (low + high)/2;
        shuttlesort(to, from, low, middle);
        shuttlesort(to, from, middle, high);

        int p = low;
        int q = middle;

        /* This is an optional short-cut; at each recursive call,
        check to see if the elements in this subset are already
        ordered.  If so, no further comparisons are needed; the
        sub-array can just be copied.  The array must be copied rather
        than assigned otherwise sister calls in the recursion might
        get out of sinc.  When the number of elements is three they
        are partitioned so that the first set, [low, mid), has one
        element and and the second, [mid, high), has two. We skip the
        optimisation when the number of elements is three or less as
        the first compare in the normal merge will produce the same
        sequence of steps. This optimisation seems to be worthwhile
        for partially ordered lists but some analysis is needed to
        find out how the performance drops to Nlog(N) as the initial
        order diminishes - it may drop very quickly.  */

        if (high - low >= 4 && compare(from[middle-1], from[middle]) <= 0) {
            for (int i = low; i < high; i++) {
                to[i] = from[i];
            }
            return;
        }

        // A normal merge.
        for (int i = low; i < high; i++) {
            if (q >= high || (p < middle && compare(from[p], from[q]) <= 0)) {
                to[i] = from[p++];
            }
            else {
                to[i] = from[q++];
            }
        }
    }

	// The mapping only affects the contents of the data rows.
	// Pass all requests to these rows through the mapping array: "indexes".

    /** Overrides getValueAt(int, int) in
     *  javax.swing.table.TableModel.
     *
	 * @see javax.swing.table.TableModel#getValueAt(int, int)
	 */
    public Object getValueAt(int aRow, int aColumn) {

		// Validate the data model before doing anything. If it's
		// busted, display a warning. I elect not to bail out here
		// because viewing the broken data will not make things
		// worse, whereas if I'm trying to change something (like
		// in sort() or setValueAt()) I could make things worse.
		if (!checkModel()) {

			System.err.println("checkModel() returned false, getValueAt() may return invalid data.");
		}

		// Return what the underlying TableModel returns.
        return model.getValueAt(indexes[aRow], aColumn);
    }

    /** Overrides setValueAt(java.lang.Object, int, int) in
     *  javax.swing.table.TableModel.
     *
	 * @see javax.swing.table.TableModel#setValueAt(java.lang.Object, int, int)
	 */
	public void setValueAt(Object aValue, int aRow, int aColumn) {

		// Validate the data model before doing anything. If it's
		// busted, bail out and display a warning.
		if (!checkModel()) {

			System.err.println("checkModel() returned false, aborting satValueAt().");
			return;
		}

		// Pass this to the underlying TableModel.
        model.setValueAt(aValue, indexes[aRow], aColumn);
    }

    /** This is a convenience method for
     * 	sortByColumn(int, boolean) and acts identically to
     *  sortByColumn(int, true).
     *
	 * @param column The column to sort the table by.
	 */
	public void sortByColumn(int column) {

        sortByColumn(column, true);
    }

    /** This method sorts the table on the given column and in
     *  the given order.
     *
	 * @param column	The column number to sort the table on.
	 * @param ascending	true if the table should be sorted in
	 * 						ascending order, e.g. a above b, false
	 * 						otherwise.
	 */
	public void sortByColumn(int column, boolean ascending) {

		// Change the sorting order.
        this.ascending = ascending;

		// Erase the old sorting information and place in fresh
		// data.
        sortingColumns.removeAllElements();
        sortingColumns.addElement(new Integer(column));

		// Sort the table.
        sort();

		// Let any listeners know that the table has changed.
		fireTableChanged(new TableModelEvent(this));
    }

	// By default, implement TableModel by forwarding all messages
	// to the model.

	/** Overrides getRowCount() in javax.swing.table.TableModel.
	 *
	 * @see javax.swing.table.TableModel#getRowCount()
	 */
	public int getRowCount() {
		return (model == null) ? 0 : model.getRowCount();
	}

	/** Overrides getColumnCount() in javax.swing.table.TableModel.
	 * @see javax.swing.table.TableModel#getColumnCount()
	 */
	public int getColumnCount() {
		return (model == null) ? 0 : model.getColumnCount();
	}

	/** Overrides getColumnName(int) in
	 *  javax.swing.table.TableModel.
	 *
	 * @see javax.swing.table.TableModel#getColumnName(int)
	 */
	public String getColumnName(int aColumn) {
		return model.getColumnName(aColumn);
	}

	/** Overrides getColumnClass(int) in
	 *  javax.swing.table.TableModel.
	 *
	 * @see javax.swing.table.TableModel#getColumnClass(int)
	 */
	public Class getColumnClass(int aColumn) {
		return model.getColumnClass(aColumn);
	}

	/** Overrides isCellEditable(int, int) in
	 *  javax.swing.table.TableModel.
	 *
	 * @see javax.swing.table.TableModel#isCellEditable(int, int)
	 */
	public boolean isCellEditable(int row, int column) {
		 return model.isCellEditable(row, column);
	}

    /** Add a mouse listener to the Table to trigger a table sort
     *  when a column heading is clicked in the JTable. This
     * 	method has been placed here because, in the words of the
     *  class's author, "There is no-where else to put this".
     *
	 * @param table The table to add sorting functionality to.
	 */
    public void addMouseListenerToHeaderInTable(JTable table) {

		// Make these final so that they can be accessed from
		// the MouseAdapter anonymous inner class.
        final TableSorter sorter = this;
        final JTable tableView = table;

		// Do not allow column selection on this table.
        tableView.setColumnSelectionAllowed(false);

        MouseAdapter listMouseListener = new MouseAdapter() {

            public void mouseClicked(MouseEvent e) {

				// Save the old cursor, and turn on the ol'
				// hourglass.
				// BUG: I'm not sure why, but this cursor code is
				// being very flaky on my machine. I originally
				// used Cursor.getPredefinedCursor to prevent
				// unnecessary new'ing of objects, however the
				// cursor changes far more frequently when I do it
				// this way, so it's like this until I figure out
				// what's acting up.
				Cursor waitCursor = new Cursor(Cursor.WAIT_CURSOR);
				Cursor oldCursor = tableView.getRootPane().getCursor();
				tableView.getRootPane().setCursor(waitCursor);

                TableColumnModel columnModel = tableView.getColumnModel();
                int viewColumn = columnModel.getColumnIndexAtX(e.getX());
                int column = tableView.convertColumnIndexToModel(viewColumn);

                if (e.getClickCount() == 1 && column != -1) {

                    int shiftPressed = e.getModifiers()&InputEvent.SHIFT_MASK;

					// If we're sorting on the same column, change
					// the ascending status.
					if ((sortingColumns.size() > 0) &&
						sortingColumns.contains(new Integer(column))) {

						ascending = !ascending;

					} else {

						// Otherwise, it's a new column. Start it
						// in ascending order.
						ascending = true;
					}

                    sorter.sortByColumn(column, ascending);
                }

				// Restore the previous cursor since sorting is
				// completed.
				tableView.getRootPane().setCursor(oldCursor);
            }
        };

        JTableHeader th = tableView.getTableHeader();
        th.addMouseListener(listMouseListener);
    }
}
