/*
 * Created on 11-Jun-2003
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

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;

import javax.swing.DefaultListSelectionModel;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.table.TableColumn;

/** This is a fairly simple GUI whose main purpose is to display
 *  the data from a given TrainingData in a nice, tabular form.
 *  It allows access to other, extra functionality in the
 *  underlying Analyzer class.
 *
 * @author Straxus
 */
public class TableWindow extends JFrame {

	/** The default title of this window.
	 */
	private static final String WINDOW_TITLE = "Training Data Token Table";

	/** The initial width of this window.
	 */
	private static final int WINDOW_WIDTH =  600;

	/** The initial height of this window.
	 */
	private static final int WINDOW_HEIGHT = 500;

	/** The TrainingData whose data we wish to display.
	 */
	private TrainingData parentTrainer = null;

	/** Contains the default filename which appears in file
	 *  selection dialog boxes.
	 */
	private String outputFilename = null;

	/** The data model for the enclosed JTable.
	 */
	private TrainingDataTableModel tableModel;

	private JFileChooser fc = new JFileChooser();

	private String lastOpenDirectory = null;

	private String lastSaveDirectory = null;

	private String lastImportDirectory = null;

	private JLabel filenameLabel = null;

	private JLabel selectedTokens = null;

	private JLabel totalTokens = null;

	/** Constructs a new TableWindow, and initializes all
	 *  necessary underlying components. This is the main entry
	 *  point for the self-contained GUI application.
	 */
	public TableWindow(TrainingData trainer, String startFilename) {

		// Set the window title in the call to JFrame's
		// constructor.
		super(WINDOW_TITLE);

		// Determine what directory to start all of the dialogs
		// in.
		String startDirectory = new File(startFilename).getParent();

		// Populate the initial directories for each of these
		// classes of dialogs.
		lastOpenDirectory = startDirectory;
		lastSaveDirectory = startDirectory;
		lastImportDirectory = startDirectory;

		// Keep a reference to the TrainingData object - we'll
		// need it later.
		parentTrainer = trainer;

		// Set the preferred size
		setSize(WINDOW_WIDTH, WINDOW_HEIGHT);

		// Create the actual JFrame here.

		// Create the data model for the JTable. Make this final
		// so that it can be accessed by some of the anonymous
		// inner classes, specifically the ones that open a new
		// file and import and merge data.
		tableModel = new TrainingDataTableModel(trainer);

		// Create a sorter class that allows for sorting by column
		// whenever a column is clicked. Make this final so that
		// it can be accessed by some of the anonymous inner
		// classes, specifically the ones that add and remove
		// rows from the table.
		final TableSorter sorter = new TableSorter(tableModel);

		// Add the sorter as a listener to the tableModel so that
		// any changes to the tableModel are reflected in the
		// TableSorter.
		tableModel.addTableModelListener(sorter);

		// Make this final so that it can be accessed by some of
		// the anonymous inner classes, specifically the ones that
		// add and remove rows from the table.
		final JTable windowTable = new JTable(sorter);

		// Got this from the Java Tutorial that also gave me the
		// TableSorter class.
		sorter.addMouseListenerToHeaderInTable(windowTable);

		// Set the preferred size of our scrollbar viewing area.
		windowTable.setPreferredScrollableViewportSize(new Dimension(WINDOW_WIDTH, WINDOW_HEIGHT));

		// Create a ListSelectionModel for our table.
		ListSelectionModel lsm = new DefaultListSelectionModel();

		// Add a listener that updates the selectedTokens label
		// when the selection changes.
		lsm.addListSelectionListener(new ListSelectionListener() {

			public void valueChanged(ListSelectionEvent lse) {

				// Get a reference to the ListSelectionModel that
				// sent this event.
				DefaultListSelectionModel dlsm = (DefaultListSelectionModel) lse.getSource();

				if (!dlsm.isSelectionEmpty()) {

					// Set the new number of selected tokens in the
					// status bar.
					getTableWindow().selectedTokens.setText(
						String.valueOf(
							dlsm.getMaxSelectionIndex() -
							dlsm.getMinSelectionIndex() +
							1
						)
					);
				} else {

					// There are no rows selected.
					getTableWindow().selectedTokens.setText("0");
				}
			}
		});

		// Set the JTable so that only a single interval of rows
		// can be selected at one time.
		lsm.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);

		// Assign this ListSelectionModel to the JTable.
		windowTable.setSelectionModel(lsm);

		// Do this here to prevent unnecessary object creation
		// inside the loop.
		TableColumn column = null;

		// Set the starting sizes of all of our columns.
		for (int i = 0; i < sorter.getColumnCount(); i++) {

			column = windowTable.getColumnModel().getColumn(i);

			// Set the token column to be the largest
			if (i == 0) {

				column.setPreferredWidth(400);

			} else {

				column.setPreferredWidth(50);
			}
		}

		JScrollPane jsp = new JScrollPane(windowTable);

		// Add the scroll pane to this window.
		getContentPane().add(jsp, BorderLayout.CENTER);

		// Create our initial MenuBar to which we will add all of
		// our menus.
		JMenuBar menuBar = new JMenuBar();
		setJMenuBar(menuBar);

		// Create the File menu.
		JMenu fileMenu = new JMenu("File");

		// Create the Edit menu.
		JMenu editMenu = new JMenu("Edit");

		// Create the Help menu.
		JMenu helpMenu = new JMenu("Help");

		// Add an 'Open' command to the File menu that opens a new
		// token file (either dat or XML) and closes the old one.
		JMenuItem openItem = new JMenuItem("Open...");

		// Add a new action listener that exits when this button
		// is pressed.
		openItem.addActionListener(new ActionListener() {

			// Put this here rather than inside actionPerformed
			// so we aren't recreating the FileFilter every time
			// we open the dialog.
			private FileFilter saveFileFilter = new FileFilter() {

				public boolean accept(File f) {

					if (f.isDirectory()) {
						return true;
					}

					// Show the file if it ends in XML or DAT.
					if (f.getAbsolutePath().toLowerCase().endsWith(".xml") ||
						f.getAbsolutePath().toLowerCase().endsWith(".dat")) {

						return true;

					} else {

						return false;
					}
				}

				public String getDescription() {
					return "XML or Data file (*.xml, *.dat)";
				}
			};

			public void actionPerformed(ActionEvent ae) {

				// Launch a file chooser dialog that
				// gets the filename from the user.
				JFileChooser fc = getTableWindow().fc;
				fc.setDialogTitle("Select Mozilla Bayesian Filter Training File");

				// Set the current directory.
				fc.setCurrentDirectory(new File(getTableWindow().lastOpenDirectory));

				// Reset the list of choosable file filters.
				fc.resetChoosableFileFilters();

				// Add an XML and DAT File Filter to our dialog.
				fc.addChoosableFileFilter(saveFileFilter);

				File inputFile = null;
				String trainingDatPath = null;
				int returnVal = fc.showOpenDialog(null);

				// If they hit "OK", then load the file.
				// Otherwise, just return to the GUI.
				if (returnVal == JFileChooser.APPROVE_OPTION) {

					inputFile = fc.getSelectedFile();
					trainingDatPath = inputFile.getAbsolutePath();

					// Save this directory so that we start there
					// the next time the user opens the dialog.
					getTableWindow().lastOpenDirectory = inputFile.getParent();

					try {

						// Create a new TrainingData from the
						// chosen file.
						TrainingData trainer = Analyzer.parseTrainingFile(trainingDatPath, true);

						// Wasn't binary, try XML.
						if (trainer == null) {

							trainer = Analyzer.parseXMLTrainingFile(trainingDatPath, true);

							if (trainer == null) {

								// If it's still null, there was
								// some sort of error in the file.
								return;
							}
						}

						// Blow away the old TrainingData and
						// assign a new one.
						tableModel.setNewTrainingData(trainer);

						// Set the TableWindow's TrainingData to
						// be the new TrainingData.
						getTableWindow().parentTrainer = trainer;

						// Set the name of the new file.
						getTableWindow().filenameLabel.setText(trainingDatPath);

					} catch (Exception e) {

						// An error occurred, let the user see the
						// stack trace. Normally I wouldn't catch
						// Exception, but I don't really feel like
						// doing the exact same thing for all
						// three types of Exceptions that could be
						// caught here.
						System.out.println(e.getMessage());
					}
				}
			}
		});

		// Add a keyboard accelerator that causes this menu item to
		// be highlighted when O is pressed.
		openItem.setMnemonic(KeyEvent.VK_O);

		// Add a keyboard accelerator that causes this menu item to
		// be executed when Ctrl-O is pressed.
		openItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O,KeyEvent.CTRL_MASK));

		// Add an 'Import and Merge' command that opens a new
		// token file (either dat or XML) and merges it with the
		// old one.
		JMenuItem importItem = new JMenuItem("Import and Merge...");

		// Add a new action listener that exits when this button
		// is pressed.
		importItem.addActionListener(new ActionListener() {

			private FileFilter importFileFilter = new FileFilter() {

				public boolean accept(File f) {

					if (f.isDirectory()) {
						return true;
					}

					if (f.getAbsolutePath().toLowerCase().endsWith(".xml") ||
						f.getAbsolutePath().toLowerCase().endsWith(".dat")) {

						return true;

					} else {

						return false;
					}
				}

				public String getDescription() {
					return "XML or Data file (*.xml, *.dat)";
				}
			};

			public void actionPerformed(ActionEvent ae) {

				// Launch a file chooser dialog that
				// gets the filename from the user.
				JFileChooser fc = getTableWindow().fc;
				fc.setDialogTitle("Select Mozilla Bayesian Filter Training File");

				// Set the current directory.
				fc.setCurrentDirectory(new File(getTableWindow().lastImportDirectory));

				// Reset the list of choosable file filters.
				fc.resetChoosableFileFilters();

				// Add an XML File Filter to our dialog.
				fc.addChoosableFileFilter(importFileFilter);

				File inputFile = null;
				String trainingDatPath = null;
				int returnVal = fc.showOpenDialog(null);

				// If they hit "OK", then load the file.
				// Otherwise, just return to the GUI.
				if (returnVal == JFileChooser.APPROVE_OPTION) {

					inputFile = fc.getSelectedFile();
					trainingDatPath = inputFile.getAbsolutePath();

					// Save this directory so that we start there
					// the next time the user opens the dialog.
					getTableWindow().lastImportDirectory = inputFile.getParent();

					try {

						System.out.println("Checking if " + trainingDatPath + " is a Mozilla token file...");

						// Create a new TrainingData from the
						// chosen file.
						TrainingData trainer = Analyzer.parseTrainingFile(trainingDatPath, true);

						// Wasn't binary, try XML.
						if (trainer == null) {

							System.out.println("Checking if " + trainingDatPath + " is an XML token file...");

							trainer = Analyzer.parseXMLTrainingFile(trainingDatPath, true);
						}

						if (trainer == null) {

							// If it's still null, it wasn't a valid file.
							System.out.println(trainingDatPath + " was not a valid Mozilla token file or XML token file.");
							return;
						}

						// Get a copy of the old TrainingData.
						TrainingData oldTrainer = tableModel.getTrainingData();

						// Set the new good message count.
						trainer.setGoodMessageCount(
							trainer.getGoodMessageCount() +
							oldTrainer.getGoodMessageCount()
						);

						// Set the new bad message count.
						trainer.setBadMessageCount(
							trainer.getBadMessageCount() +
							oldTrainer.getBadMessageCount()
						);

						// Assign the new token set.
						trainer.setTokenSet(
							Analyzer.mergeTokenLists(
								trainer.getTokenSet(),
								oldTrainer.getTokenSet()
							)
						);

						// Blow away the old TrainingData and
						// assign a new one.
						tableModel.setNewTrainingData(trainer);

						// Set the TableWindow's TrainingData to
						// be the new TrainingData.
						getTableWindow().parentTrainer = trainer;

						System.out.println("Import complete!");

					} catch (Exception e) {

						// An error occurred, let the user see the
						// stack trace. Normally I wouldn't catch
						// Exception, but I don't really feel like
						// doing the exact same thing for all
						// three types of Exceptions that could be
						// caught here.
						System.out.println(e.getMessage());
						System.out.println(trainingDatPath + " was not a valid Mozilla token file or XML token file.");
					}
				}
			}
		});

		// Add a keyboard accelerator that causes this menu item to
		// be highlighted when I is pressed.
		importItem.setMnemonic(KeyEvent.VK_I);

		// Add a keyboard accelerator that causes this menu item to
		// be executed when Ctrl-I is pressed.
		importItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_I,KeyEvent.CTRL_MASK));

		// Create the Exit item for the File menu.
		JMenuItem closeItem = new JMenuItem("Close Window");

		// Add a new action listener that exits when this button
		// is pressed.
		closeItem.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent ae) {

				// Get rid of this window.
				wakeAll();
				getTableWindow().hide();
				getTableWindow().dispose();
			}
		});

		// Add a keyboard accelerator that causes this menu item to
		// be highlighted when W is pressed.
		closeItem.setMnemonic(KeyEvent.VK_W);

		// Add a keyboard accelerator that causes this menu item to
		// be executed when Ctrl-W is pressed.
		closeItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_W,KeyEvent.CTRL_MASK));

		// Create the Save item for the File menu.
		JMenuItem saveItem = new JMenuItem("Save As...");

		// Add a new action listener that launches a file save
		// dialog when this button is pressed.
		saveItem.addActionListener(new ActionListener() {

			// We create these file filters here so that they
			// aren't recreated every time we hit the dialog box.
			private TrainingFileFilter xmlFileFilter = new TrainingFileFilter() {

				public boolean accept(File f) {

					if (f.isDirectory()) {
						return true;
					}

					if (f.getAbsolutePath().toLowerCase().endsWith(".xml")) {

						return true;

					} else {

						return false;
					}
				}

				public String getDescription() {
					return "Output as XML (*.xml)";
				}

				public int getFilterOutputType() {
					return Analyzer.OUTPUT_XML;
				}
			};

			private TrainingFileFilter htmlFileFilter = new TrainingFileFilter() {

				public boolean accept(File f) {

					if (f.isDirectory()) {
						return true;
					}

					if (f.getAbsolutePath().toLowerCase().endsWith(".html") ||
						f.getAbsolutePath().toLowerCase().endsWith(".htm")) {

						return true;

					} else {

						return false;
					}
				}

				public String getDescription() {
					return "Output as HTML (*.htm, *.html)";
				}

				public int getFilterOutputType() {
					return Analyzer.OUTPUT_HTML;
				}
			};

			private TrainingFileFilter datFileFilter = new TrainingFileFilter() {

				public boolean accept(File f) {

					if (f.isDirectory()) {
						return true;
					}

					if (f.getAbsolutePath().toLowerCase().endsWith(".dat")) {

						return true;

					} else {

						return false;
					}
				}

				public String getDescription() {
					return "Output as data (*.dat)";
				}

				public int getFilterOutputType() {
					return Analyzer.OUTPUT_DATA;
				}
			};

			private TrainingFileFilter textFileFilter = new TrainingFileFilter() {

				public boolean accept(File f) {

					if (f.isDirectory()) {
						return true;
					}

					if (f.getAbsolutePath().toLowerCase().endsWith(".txt")) {

						return true;

					} else {

						return false;
					}
				}

				public String getDescription() {
					return "Output as Plaintext (*.txt)";
				}

				public int getFilterOutputType() {
					return Analyzer.OUTPUT_TEXT;
				}
			};

			public void actionPerformed(ActionEvent ae) {

				// Launch a file chooser dialog that
				// gets the filename from the user.
				JFileChooser fc = getTableWindow().fc;
				fc.setDialogTitle("Choose Output Format and Filename");

				// Set the current directory.
				fc.setCurrentDirectory(new File(getTableWindow().lastSaveDirectory));

				// Reset the list of choosable file filters.
				fc.resetChoosableFileFilters();

				// Turn off the 'Accept All' file filter for this
				// dialog.
				fc.setAcceptAllFileFilterUsed(false);

				// Add an XML File Filter to our dialog.
				fc.addChoosableFileFilter(xmlFileFilter);

				// Add an HTML File Filter to our dialog.
				fc.addChoosableFileFilter(htmlFileFilter);

				// Add a plaintext File Filter to our dialog.
				fc.addChoosableFileFilter(textFileFilter);

				// Add a data File Filter to our dialog.
				fc.addChoosableFileFilter(datFileFilter);

				// If we passed in an outputFilename on the
				// command-line when we invoked the program, then
				// use it as a starting point now.
				if (outputFilename != null) {

					fc.setSelectedFile(new File(outputFilename));
				}

				int returnVal = fc.showSaveDialog(getTableWindow());

				if (returnVal == JFileChooser.APPROVE_OPTION) {

					File outputFile = fc.getSelectedFile();

					// Convert this string to lower case so we
					// don't have to worry about mixed case when
					// doing our checks for file extensions.
					String filePath = outputFile.getAbsolutePath().toLowerCase();

					// Save this directory so that we start there
					// the next time the user opens the dialog.
					getTableWindow().lastSaveDirectory = outputFile.getParent();

					TrainingFileFilter selFilter = (TrainingFileFilter) fc.getFileFilter();

					switch (selFilter.getFilterOutputType()) {

						case Analyzer.OUTPUT_DATA:

							// Append .dat on the filename if it's
							// not already there.
							if (!filePath.endsWith(".dat")) {
								outputFile =
									new File(
										outputFile.getAbsolutePath() +
										".dat"
									);
							}

							break;

						case Analyzer.OUTPUT_XML:

							// Append .xml on the filename if it's
							// not already there.
							if (!filePath.endsWith(".xml")) {
								outputFile =
									new File(
										outputFile.getAbsolutePath() +
										".xml"
									);
							}

							break;

						case Analyzer.OUTPUT_HTML:

							// Append .html on the filename if
							// int's not already there.
							if (!filePath.endsWith(".htm") ||
								!filePath.endsWith(".html")) {
								outputFile =
									new File(
										outputFile.getAbsolutePath() +
										".html"
									);
							}

							break;

						case Analyzer.OUTPUT_TEXT:

							// Append .txt on the filename if it's
							// not already there.
							if (!filePath.endsWith(".txt")) {
								outputFile =
									new File(
										outputFile.getAbsolutePath() +
										".txt"
									);
							}

							break;

					}

					try {

						Analyzer.writeOutput(
							outputFile,
							getTableWindowTrainer(),
							selFilter.getFilterOutputType()
						);

					} catch (IOException ioe) {
						ioe.printStackTrace(System.err);
					}
				}
			}
		});

		// Add a keyboard accelerator that causes this menu item to
		// be highlighted when S is pressed.
		saveItem.setMnemonic(KeyEvent.VK_S);

		// Add a keyboard accelerator that causes this menu item to
		// be executed when Ctrl-S is pressed.
		saveItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S,KeyEvent.CTRL_MASK));

		// Add in an item for adding a new row to the table
		JMenuItem addRowItem = new JMenuItem("Add New Row");

		// Add a new action listener that creates a new token when
		// this button is pressed.
		addRowItem.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent ae) {

				String newToken = JOptionPane.showInputDialog(getTableWindow(), "Please enter a Token string");

				if (newToken != null) {
					// Add a new row to the table.
					tableModel.addRow(newToken);
				}
			}
		});

		// Add a keyboard accelerator that causes this menu item to
		// be highlighted when N is pressed.
		addRowItem.setMnemonic(KeyEvent.VK_N);

		// Add a keyboard accelerator that causes this menu item to
		// be executed when Ctrl-N is pressed.
		addRowItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N,KeyEvent.CTRL_MASK));

		// Add in an item for removing a row from the table
		JMenuItem removeRowItem = new JMenuItem("Remove Selected Rows");

		// Add a new action listener that removes the selected
		// rows when this button is pressed.
		removeRowItem.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent ae) {

				// If a row is selected, delete it.
				if (windowTable.getSelectedRowCount() != 0) {

					// Remove the rows in the selection set from
					// the table.
					tableModel.removeRows(sorter.lookupRows(windowTable.getSelectedRows()));

				} else {

					JOptionPane.showMessageDialog(getTableWindow(), "No rows were selected.");
				}
			}
		});

		// Add a keyboard accelerator that causes this menu item to
		// be highlighted when R is pressed.
		removeRowItem.setMnemonic(KeyEvent.VK_R);

		// Add a keyboard accelerator that causes this menu item to
		// be executed when Ctrl-R is pressed.
		removeRowItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R,KeyEvent.CTRL_MASK));

		// Add in an item for removing a row from the table
		JMenuItem removeTokensItem = new JMenuItem("Remove Tokens by Count...");

		// Add a new action listener that exits when this button
		// is pressed.
		removeTokensItem.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent ae) {

				// Pop up the dialog, and have it get the numbers
				// from the user and execute the token removal.
				new RemoveTokenWindow(getTableWindow()).show();
			}
		});

		// Add a keyboard accelerator that causes this menu item to
		// be highlighted when T is pressed.
		removeTokensItem.setMnemonic(KeyEvent.VK_T);

		// Add a keyboard accelerator that causes this menu item to
		// be executed when Ctrl-T is pressed.
		removeTokensItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_T,KeyEvent.CTRL_MASK));

		// Add in an item for removing a row from the table
		JMenuItem aboutItem = new JMenuItem("About");

		// Add a new action listener that displayes the About
		// dialog when this button is pressed.
		aboutItem.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent ae) {

				// Display the About box.
				AboutBox.display(getTableWindow());
			}
		});

		// Add a keyboard accelerator that causes this menu item to
		// be highlighted when B is pressed.
		aboutItem.setMnemonic(KeyEvent.VK_B);

		// Add a keyboard accelerator that causes this menu item to
		// be executed when Ctrl-B is pressed.
		aboutItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_B,KeyEvent.CTRL_MASK));

		// Add these items to the menubar.
		fileMenu.add(openItem);
		fileMenu.add(importItem);
		fileMenu.add(saveItem);

		// Put a space between Save and Close to lower chance that
		// an unexpected click will close the GUI and lose any
		// saved changes.
		fileMenu.addSeparator();
		fileMenu.add(closeItem);
		menuBar.add(fileMenu);

		editMenu.add(addRowItem);
		editMenu.add(removeRowItem);

		// Put a separator between these groups because they are
		// logically distinct operations.
		editMenu.addSeparator();
		editMenu.add(removeTokensItem);
		menuBar.add(editMenu);

		// Add the Help menu.
		helpMenu.add(aboutItem);
		menuBar.add(helpMenu);

		// Add a keyboard accelerator that causes this menu item
		// to open when Alt-F is pressed.
		fileMenu.setMnemonic(KeyEvent.VK_F);

		// Add a keyboard accelerator that causes this menu item
		// to open when Alt-E is pressed.
		editMenu.setMnemonic(KeyEvent.VK_E);

		// Add a keyboard accelerator that causes this menu item
		// to open when Alt-H is pressed.
		helpMenu.setMnemonic(KeyEvent.VK_H);

		JPanel statusPanel = new JPanel(new GridBagLayout());

		GridBagConstraints gbc = new GridBagConstraints();

		// Set up the properties for the filename label.
		gbc.fill = GridBagConstraints.NONE;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.gridwidth = 1;
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.weightx = 4.0;
		gbc.weighty = 1.0;

		filenameLabel = new JLabel(startFilename);

		// Add the filename label.
		statusPanel.add(filenameLabel, gbc);

		// Set up the properties for the Total number of tokens
		// label.
		gbc.anchor = GridBagConstraints.EAST;
		gbc.gridx = 1;
		gbc.weightx = 0.1;

		// Create our selected tokens label.
		selectedTokens = new JLabel("0");

		// Add the total tokens label.
		statusPanel.add(selectedTokens, gbc);

		// Set up the properties for the number of selected
		// tokens label.
		gbc.anchor = GridBagConstraints.CENTER;
		gbc.gridx = 2;

		// Add the selected tokens label.
		statusPanel.add(new JLabel("/"), gbc);

		// Set up the properties for the number of selected
		// tokens label.
		gbc.gridx = 3;

		// Create our total tokens label.
		totalTokens = new JLabel(String.valueOf(parentTrainer.getTokenSet().size()));

		// Add the selected tokens label.
		statusPanel.add(totalTokens, gbc);

		// Set up the properties for the number of selected
		// tokens label.
		gbc.gridx = 4;

		// Add the selected tokens label.
		statusPanel.add(new JLabel(" tokens selected"), gbc);

		tableModel.addTableModelListener(new TableModelListener() {

			public void tableChanged(TableModelEvent tme) {

				// Get the new number of rows from the
				// TableModel.
				getTableWindow().totalTokens.setText(String.valueOf(tableModel.getRowCount()));
			}
		});

		// Add the status bar to this window.
		getContentPane().add(statusPanel, BorderLayout.SOUTH);

		// Add in a listener that will wake up the calling program
		// when this window is closed.
		this.addWindowListener(new WindowAdapter() {

			public void windowClosing(WindowEvent we) {

				wakeAll();
			}
		});
	}

	/** Returns a reference to this object. Used for anonymous
	 *  inner classes.
	 *
	 * @return A reference to this object.
	 */
	private TableWindow getTableWindow() {
		return this;
	}

	/** Returns a reference to this object's underlying
	 * 	TrainingData. Used for anonymous inner classes.
	 *
	 * @return A reference to this object's underlying
	 *			TrainingData.
	 */
	public TrainingData getTableWindowTrainer() {
		return this.parentTrainer;
	}

	/** Sets a default output filename which will appear in any
	 *  save dialogs that are created.
	 *
	 * @param string The new default output filename.
	 */
	public void setOutputFilename(String string) {
		outputFilename = string;
	}

	/** Changes the displayed TrainingData to be the new, given
	 * 	TrainingData.
	 *
	 * @param newTrainer	The new TrainingData to display in
	 * 						this TableWindow.
	 */
	public void setNewTrainingData(TrainingData newTrainer) {

		// Get a reference to the new TrainingData
		parentTrainer = newTrainer;

		// Change the JTable's data model to reflect the new
		// TrainingData.
		tableModel.setNewTrainingData(newTrainer);
	}

	/** Wake up any programs that are waiting for this window to
	 * 	close.
	 */
	protected synchronized void wakeAll() {

		// Wake up any programs that are waiting for this
		// window to close.
		this.notifyAll();
	}

	/** Any thread calling this method will block until the window
	 *  is closed.
	 */
	public synchronized void waitForWindow() {

		// Block until we are awakened.
		try {

			this.wait();

		} catch (InterruptedException e) {
			// Continue on, we're awake now.
		}
	}

	/** This class adds a simple method onto a normal FileFilter.
	 *  This method will be used to determine what format our
	 * 	output should take based on what filter was selected.
	 *
	 * @author Straxus
	 */
	private abstract class TrainingFileFilter extends FileFilter{

		abstract public int getFilterOutputType();
	}
}
