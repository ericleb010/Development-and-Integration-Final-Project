import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import javax.swing.filechooser.*;
import javax.swing.filechooser.FileFilter;

import java.awt.*;
import java.awt.event.*;
import java.util.Scanner;
import java.util.Vector;
import java.io.*;

/**
 * 
 * @author liknox
 *
 */
public class CodemonGUI {
	
	private static final int H_GAP = 10;
	private static final int V_GAP = 4;
	private static final int BORDER_WIDTH = 5;
	private static final Insets BORDER_DEFINE = new Insets(BORDER_WIDTH, BORDER_WIDTH, BORDER_WIDTH, BORDER_WIDTH);
	private static final Font SECOND_FONT = new Font((new JLabel()).getFont().getName(), Font.BOLD, 14);
	private static final int REPORTS_WIDTH = 200;
	private static final int MAXTURNS = 50000;
	
	// For all of GUI.
	protected JFrame mainWindow = null;
	protected JDialog displayTrainingWindow = null;
	protected JDialog displayFightingWindow = null;
	private static Dimension mainSize = new Dimension(500, 300);
	private static Dimension trainSize = new Dimension(500, 600);
	private static Dimension fightSize = new Dimension(600, 500);
	private static Dimension reportDisplaySize = new Dimension(400, 700);
	
	// For training window.
	private boolean trainingHasName = false;
	private File trainingCurrentFile = null;
	private File cmDirectory = new File("Source");
	private File codemonDirectory = new File("Codemon");
	private File reportsDirectory = new File("Reports");
	private boolean trainingModified = false;
	private JLabel modifiedInfo = null;
	private CodemonEditAreaListener editAreaListener = this.new CodemonEditAreaListener();
	private boolean editAreaListenerOn = false;
	
	// Other variables.
	private JComboBox<Codemon> codemon1Select = null;
	private JComboBox<Codemon> codemon2Select = null;
	private JList<Codemon> codemonList = null;
	private JList<File> reportsList = null;
	private int iterationNum = 0;
	
	// Native code
	static { System.loadLibrary("CodemonGUI"); }
	private native String sendTest(String filenameJ, int numTurns);
	private native String sendTestBattle(String filenameJ, String filename2J, int numTurns);
	private native String sendBattle(String filenameJ, int numPlayers);
	private native String parseTextFile(String filenameJ, String destFileJ);
	private native String getReport(String reportNumJ, String outFilenameJ);
	
	// Class variable declarations for proper access across methods.
	private JTextArea editArea = null;
	private JMenuItem saveItem = null;
	private JButton saveButton = null;
	private JMenuItem saveAsItem = null;
	private JButton saveAsButton = null;
	private JMenuItem closeItem = null;
	private JMenuItem assembleItem = null;
	private JButton assembleButton = null;
	private JMenuItem assembleLaunchItem = null;
	
	/**
	 * Constructor for the intro JFrame.
	 */
	public CodemonGUI() {
		// Before we even do anything, create the default directories if they aren't present already.
		cmDirectory.mkdir();
		codemonDirectory.mkdir();
		reportsDirectory.mkdir();
				
		// Set up the JFrame and the layout.
		JFrame mainFrame = new JFrame("Codemon Fighting Simulator - by Eric Leblanc");
		this.mainWindow = mainFrame;
		mainFrame.setVisible(true);
		mainFrame.setResizable(false);
		mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		mainFrame.setLayout(new BoxLayout(mainFrame.getContentPane(), BoxLayout.Y_AXIS));
		mainFrame.add(new JLabel(new ImageIcon("src/codemon-main.jpg"), JLabel.CENTER));
		JPanel mainFrameButtons = new JPanel();
		mainFrameButtons.setBorder(new EmptyBorder(BORDER_DEFINE));
		mainFrameButtons.setLayout(new GridLayout(2, 2, H_GAP, V_GAP));
		
		// Start adding buttons and functionality.
		JButton trainButton = new JButton("Train");
		trainButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				displayTrainingDialog();
			}
		});
		JButton fightButton = new JButton("Fight");
		fightButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				displayFightingDialog(null);
			}
		});
		JButton aboutButton = new JButton("About");
		aboutButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				displayAboutDialog(mainFrame);
			}
		});
		JButton exitButton = new JButton("Exit");
		exitButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				closeGUI();
			}
		});
		mainFrameButtons.add(trainButton);
		mainFrameButtons.add(fightButton);
		mainFrameButtons.add(aboutButton);
		mainFrameButtons.add(exitButton);
		mainFrame.add(mainFrameButtons);
		
		
		// Label at the bottom.
		JPanel authorInfoCont = new JPanel();
		authorInfoCont.setLayout(new BorderLayout());
		authorInfoCont.setBorder(BorderFactory.createLoweredBevelBorder());
		JLabel authorInfo = new JLabel("CIS*2750F15 - by Eric Leblanc", SwingConstants.CENTER);
		authorInfo.setFont(SECOND_FONT);
		authorInfoCont.add(authorInfo);
		mainFrame.add(authorInfoCont);
		
		// Reload!
		mainFrame.revalidate();
		mainFrame.repaint();
		mainFrame.pack();
	}
	
	
	/**
	 * A helper function to set up the training window!
	 */
	protected void displayTrainingDialog() {
			
		// Set up the JDialog.
		JDialog trainingFrame = new JDialog(mainWindow, "Training Center!");
		this.displayTrainingWindow = trainingFrame;
		trainingFrame.setModal(true);
		trainingFrame.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		trainingFrame.setLocation(mainWindow.getLocation());
		trainingFrame.setSize(trainSize);
		trainingFrame.setPreferredSize(trainSize);
		
		// We want full control over the closing event. 
		trainingFrame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				
				if (!promptTrainSave(false, false))
					return;
				trainingFrame.setModal(false);
				trainingFrame.dispose();
			}
		});
		
		// Set up the main panel.
		JPanel mainPanel = new JPanel();
		mainPanel.setBorder(new EmptyBorder(BORDER_DEFINE));
		mainPanel.setLayout(new BorderLayout());
		trainingFrame.add(mainPanel);
		
		// Adding our menu bar.
		JMenuBar bar = new JMenuBar();
		JMenu fileList = new JMenu("File");
		fileList.setMnemonic(KeyEvent.getExtendedKeyCodeForChar('f'));
		fileList.setDisplayedMnemonicIndex(0);
		JMenu buildList = new JMenu("Build");
		buildList.setMnemonic(KeyEvent.getExtendedKeyCodeForChar('b'));
		buildList.setDisplayedMnemonicIndex(0);
		JMenu configList = new JMenu("Config");
		configList.setMnemonic(KeyEvent.getExtendedKeyCodeForChar('c'));
		configList.setDisplayedMnemonicIndex(0);
		JMenu helpList = new JMenu("Help");
		helpList.setMnemonic(KeyEvent.getExtendedKeyCodeForChar('h'));
		helpList.setDisplayedMnemonicIndex(0);
		
		
		// ...and the various buttons!
		JMenuItem newItem = new JMenuItem("New");
		newItem.setMnemonic(KeyEvent.getExtendedKeyCodeForChar('n'));
		newItem.setDisplayedMnemonicIndex(0);
		newItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				
				// Prompt the user to save if necessary. If the save cannot be completed, abort.
				if (!promptTrainSave(false, false))
					return;
				
				// Housekeeping
				editArea.setText("");
				handleOpenClose(true);
				toggleTrainingSaveButtons(false);
				trainingHasName = false;
				trainingCurrentFile = null;
				editArea.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLoweredBevelBorder(), ""));
			}
		});
		
		
		JMenuItem openItem = new JMenuItem("Open...");
		openItem.setMnemonic(KeyEvent.getExtendedKeyCodeForChar('o'));
		openItem.setDisplayedMnemonicIndex(0);
		openItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				
				// Prompt the user to save if necessary. If the save cannot be completed, abort.
				if (!promptTrainSave(false, false))
					return;
				
				// Attempt to open the file and read in the contents.
				JFileChooser openDialog = new JFileChooser(cmDirectory);
				openDialog.setFileFilter(new FileNameExtensionFilter("Codemon source files", "cm"));
				openDialog.showOpenDialog(displayTrainingWindow);
				if (openDialog.getSelectedFile() != null) {
					// Placing in a string since there are a maximum of 50 instructions in a .cm file, so 
					// shouldn't be too worried to hit the max value of 2^31 bytes.
					String fileContents = "";
					FileReader sc = null;
					try {
						sc = new FileReader(openDialog.getSelectedFile());
						for (int i = 0; i < openDialog.getSelectedFile().length(); i++) {
							fileContents += (char) sc.read();
						}
						sc.close();
						editArea.setText(fileContents);
					} catch (IOException e3) { /* Never occurs */ return; }
					
					
					// Housekeeping
					handleOpenClose(true);
					toggleTrainingSaveButtons(false);
					trainingHasName = true;
					trainingCurrentFile = openDialog.getSelectedFile();
					editArea.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLoweredBevelBorder(), trainingCurrentFile.getName()));
				}
			}
		});
		
		
		saveItem = new JMenuItem("Save");
		saveItem.setMnemonic(KeyEvent.getExtendedKeyCodeForChar('v'));
		saveItem.setDisplayedMnemonicIndex(2);
		saveItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (!promptTrainSave(false, true))
					return;
			}
		});
		
		
		// Same as save for the most part.
		saveAsItem = new JMenuItem("Save As...");
		saveAsItem.setMnemonic(KeyEvent.getExtendedKeyCodeForChar('s'));
		saveAsItem.setDisplayedMnemonicIndex(0);
		saveAsItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (!promptTrainSave(true, true))
					return;
			}
		});
		
		
		closeItem = new JMenuItem("Close");
		closeItem.setMnemonic(KeyEvent.getExtendedKeyCodeForChar('c'));
		closeItem.setDisplayedMnemonicIndex(0);
		closeItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (!promptTrainSave(false, false))
					return;
				
				editArea.setText("");
				trainingHasName = false;
				trainingCurrentFile = null;
				toggleTrainingSaveButtons(false);
				handleOpenClose(false);
				editArea.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLoweredBevelBorder(), ""));
			}
		});
		
		
		// Sets off the WindowListener defines above.
		JMenuItem quitItem = new JMenuItem("Quit");
		quitItem.setMnemonic(KeyEvent.getExtendedKeyCodeForChar('q'));
		quitItem.setDisplayedMnemonicIndex(0);
		quitItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				displayTrainingWindow.getWindowListeners()[0].windowClosing(new WindowEvent(displayTrainingWindow, WindowEvent.WINDOW_CLOSING));
			}
		});
		
		
		// Uses native code to accomplish the parsing.
		assembleItem = new JMenuItem("Assemble");
		assembleItem.setMnemonic(KeyEvent.getExtendedKeyCodeForChar('a'));
		assembleItem.setDisplayedMnemonicIndex(0);
		assembleItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (trainingCurrentFile == null || !promptTrainSave(false, true))
					return;
				String fileToPass = codemonDirectory.getPath() + "/" + trainingCurrentFile.getName();
				File targFile = new File(fileToPass.substring(0, fileToPass.length() - 3) + ".codemon");
				// Check to see if the assembled codemon exists already.
				if (targFile.exists())
					if (JOptionPane.showConfirmDialog(displayTrainingWindow, "The assembled codemon already exists. Are you sure you wish to over?", "Overwrite?", JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE) == JOptionPane.NO_OPTION)
						return;
				
				String status = parseTextFile(trainingCurrentFile.getAbsolutePath(), targFile.getAbsolutePath());
				if (status != null) 
					JOptionPane.showMessageDialog(displayTrainingWindow, "An error occurred when assembling: " + status, "Error", JOptionPane.ERROR_MESSAGE);
				else
					JOptionPane.showMessageDialog(displayTrainingWindow, "Assembled!", "", JOptionPane.INFORMATION_MESSAGE);
			}
		});
		
		// Same as above, but this one launches straight into the fighting window.
		assembleLaunchItem = new JMenuItem("Assemble and Launch...");
		assembleLaunchItem.setMnemonic(KeyEvent.getExtendedKeyCodeForChar('l'));
		assembleLaunchItem.setDisplayedMnemonicIndex(13);
		assembleLaunchItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (trainingCurrentFile == null || !promptTrainSave(false, true))
					return;
				String fileToPass = codemonDirectory.getPath() + "/" + trainingCurrentFile.getName();
				File targFile = new File(fileToPass.substring(0, fileToPass.length() - 3) + ".codemon");
				if (targFile.exists())
					if (JOptionPane.showConfirmDialog(displayTrainingWindow, "The assembled codemon already exists. Are you sure you wish to overwrite?", "Overwrite?", JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE) == JOptionPane.NO_OPTION)
						return;

				String status = parseTextFile(trainingCurrentFile.getAbsolutePath(), targFile.getAbsolutePath());
				if (status != null) {
					JOptionPane.showMessageDialog(displayTrainingWindow, "An error occurred when assembling: " + status, "Error", JOptionPane.ERROR_MESSAGE);
					return;
				}
				
				editArea.setText("");
				trainingHasName = false;
				trainingCurrentFile = null;
				toggleTrainingSaveButtons(false);
				handleOpenClose(false);
				editArea.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLoweredBevelBorder(), ""));
				displayFightingDialog(targFile);
			}
		});
		
		// These buttons have listeners defined further below.
		JMenuItem sourceDirItem = new JMenuItem("Source directory...");
		sourceDirItem.setMnemonic(KeyEvent.getExtendedKeyCodeForChar('s'));
		sourceDirItem.setDisplayedMnemonicIndex(0);
		sourceDirItem.addActionListener(new SourceDirActionListener(false));
		JMenuItem codemonDirItem = new JMenuItem("Codemon directory...");
		codemonDirItem.setMnemonic(KeyEvent.getExtendedKeyCodeForChar('d'));
		codemonDirItem.setDisplayedMnemonicIndex(2);
		codemonDirItem.addActionListener(new CodemonDirActionListener(false));
		
		// For specific help on this window.
		JMenuItem helpItem = new JMenuItem("Help...");
		helpItem.setMnemonic(KeyEvent.getExtendedKeyCodeForChar('h'));
		helpItem.setDisplayedMnemonicIndex(0);
		helpItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				displayTrainingHelp();
			}
		});
		
		// Displays the contents of the README.
		JMenuItem aboutItem = new JMenuItem("About this program...");
		aboutItem.setMnemonic(KeyEvent.getExtendedKeyCodeForChar('a'));
		aboutItem.setDisplayedMnemonicIndex(0);
		aboutItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				displayAboutDialog(displayTrainingWindow);
			}
		});
		
		fileList.add(newItem);
		fileList.add(openItem);
		fileList.add(saveItem);
		fileList.add(saveAsItem);
		fileList.addSeparator();
		fileList.add(closeItem);
		fileList.add(quitItem);
		buildList.add(assembleItem);
		buildList.add(assembleLaunchItem);
		configList.add(sourceDirItem);
		configList.add(codemonDirItem);
		helpList.add(helpItem);
		helpList.addSeparator();
		helpList.add(aboutItem);
		bar.add(fileList);
		bar.add(buildList);
		bar.add(configList);
		bar.add(helpList);
		trainingFrame.setJMenuBar(bar);
		
		// Quick access bar! Nothing new here, reusing the listeners from above.
		JToolBar quickAccess = new JToolBar("Quick Access");
		JButton newButton = new JButton(new ImageIcon("src/toolbarButtonGraphics/general/New24.gif"));
		newButton.setToolTipText("New");
		newButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				newItem.getActionListeners()[0].actionPerformed(e);
			}
		});
		JButton openButton = new JButton(new ImageIcon("src/toolbarButtonGraphics/general/Open24.gif"));
		openButton.setToolTipText("Open");
		openButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				openItem.getActionListeners()[0].actionPerformed(e);	
			}
		});
		saveButton = new JButton(new ImageIcon("src/toolbarButtonGraphics/general/Save24.gif"));
		saveButton.setToolTipText("Save");
		saveButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				saveItem.getActionListeners()[0].actionPerformed(e);	
			}
		});
		saveAsButton = new JButton(new ImageIcon("src/toolbarButtonGraphics/general/SaveAs24.gif"));
		saveAsButton.setToolTipText("Save as...");
		saveAsButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				saveAsItem.getActionListeners()[0].actionPerformed(e);	
			}
		});
		assembleButton = new JButton(new ImageIcon("src/toolbarButtonGraphics/development/ApplicationDeploy24.gif"));
		assembleButton.setToolTipText("Assemble");
		assembleButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				assembleItem.getActionListeners()[0].actionPerformed(e);
			}
		});
		quickAccess.add(newButton);
		quickAccess.add(openButton);
		quickAccess.add(saveButton);
		quickAccess.add(saveAsButton);
		quickAccess.add(assembleButton);
		mainPanel.add(quickAccess, BorderLayout.NORTH);
		
		// Set up the editing screen.
		editArea = new JTextArea();
		editArea.setEditable(true);
		editArea.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLoweredBevelBorder(), ""));
		editArea.setEnabled(false);
		JScrollPane scrollable = new JScrollPane(editArea, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		mainPanel.add(scrollable);
		
		// Label at the bottom for the save state.
		modifiedInfo = new JLabel("\t", SwingConstants.CENTER);
		mainPanel.add(modifiedInfo, BorderLayout.SOUTH);
		
		// Refresh!
		toggleTrainingSaveButtons(false);
		handleOpenClose(false);
		trainingFrame.setVisible(true);
		trainingFrame.revalidate();
		trainingFrame.repaint();
		trainingFrame.pack();
	}
	
//------------------------------------------------------------------------------------------------
	
	/**
	 * Helper method for setting up the fighting window!
	 * @param runFileImmediately a File object pointing to the assembled codemon, or null if not applicable.
	 */
	protected void displayFightingDialog(File runFileImmediately) {
		
		// Set up the JFrame.
		JDialog fightingFrame = new JDialog(mainWindow, "Fighting Center!");
		this.displayFightingWindow = fightingFrame;
		fightingFrame.setModal(true);
		fightingFrame.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		fightingFrame.setLocation(mainWindow.getLocation());
		fightingFrame.setResizable(false);
		fightingFrame.setSize(fightSize);
		fightingFrame.setPreferredSize(fightSize);
		
		// We want full control over the closing event.
		fightingFrame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				fightingFrame.setModal(false);
				fightingFrame.dispose();
			}
		});
		
		JPanel mainPanel = new JPanel();
		mainPanel.setBorder(new EmptyBorder(BORDER_DEFINE));
		mainPanel.setLayout(new BorderLayout());
		fightingFrame.add(mainPanel);
		
		// Set up the menu bar.
		JMenuBar bar = new JMenuBar();
		JMenu fileList = new JMenu("File");
		fileList.setMnemonic(KeyEvent.getExtendedKeyCodeForChar('f'));
		fileList.setDisplayedMnemonicIndex(0);
		JMenu configList = new JMenu("Config");
		configList.setMnemonic(KeyEvent.getExtendedKeyCodeForChar('c'));
		configList.setDisplayedMnemonicIndex(0);
		JMenu reportList = new JMenu("Reports");
		reportList.setMnemonic(KeyEvent.getExtendedKeyCodeForChar('r'));
		reportList.setDisplayedMnemonicIndex(0);
		JMenu helpList = new JMenu("Help");
		helpList.setMnemonic(KeyEvent.getExtendedKeyCodeForChar('h'));
		helpList.setDisplayedMnemonicIndex(0);
		
		// Needed in some look and feel styles for JDialogs.
		JMenuItem closeItem = new JMenuItem("Close");
		closeItem.setMnemonic(KeyEvent.getExtendedKeyCodeForChar('c'));
		closeItem.setDisplayedMnemonicIndex(0);
		closeItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				fightingFrame.setModal(false);
				fightingFrame.dispose();
			}
		});
		
		// Configurations for the default folders used.
		JMenuItem sourceDirItem = new JMenuItem("Source directory...");
		sourceDirItem.setMnemonic(KeyEvent.getExtendedKeyCodeForChar('s'));
		sourceDirItem.setDisplayedMnemonicIndex(0);
		sourceDirItem.addActionListener(new SourceDirActionListener(true));
		JMenuItem codemonDirItem = new JMenuItem("Codemon directory...");
		codemonDirItem.setMnemonic(KeyEvent.getExtendedKeyCodeForChar('c'));
		codemonDirItem.setDisplayedMnemonicIndex(0);
		codemonDirItem.addActionListener(new CodemonDirActionListener(true));
		JMenuItem reportItem = new JMenuItem("Reports directory...");
		reportItem.setMnemonic(KeyEvent.getExtendedKeyCodeForChar('r'));
		reportItem.setDisplayedMnemonicIndex(0);
		reportItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				int choice = JOptionPane.showConfirmDialog((Component) e.getSource(), "Would you like to type in the path manually?", "", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
				// Manual entry.
				if (choice == JOptionPane.YES_OPTION) {
					boolean done = false;
					while (!done) {
						String result;
						if ((result = JOptionPane.showInputDialog(displayTrainingWindow, "Please enter the path of the directory you wish to set as default.", reportsDirectory.getAbsolutePath())) == null)
								return;
						File chosenFile = new File(result);
						// Check if the path even exists!
						if (chosenFile.exists() && chosenFile.isDirectory()) {
							reportsDirectory = chosenFile;
							refreshReportsAndCodemon();
							done = true;
						}
						else if (!chosenFile.exists())
							JOptionPane.showMessageDialog(displayTrainingWindow, "That's not a valid path. Please try again.", "Error", JOptionPane.ERROR_MESSAGE);
						else 
							JOptionPane.showMessageDialog(displayTrainingWindow, "That's not a valid directory. Please try again.", "Error", JOptionPane.ERROR_MESSAGE);
					}
				}
				// File chooser!
				else {
					JFileChooser chooseFolder = new JFileChooser(reportsDirectory);
					chooseFolder.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
					chooseFolder.setFileFilter(new FileFilter() {
						@Override
						public boolean accept(File file) {
							return file.isDirectory();
						}
						@Override
						public String getDescription() {
							return "Folders";
						}
					});
					// If they cancel.
					if (chooseFolder.showOpenDialog(displayTrainingWindow) != JFileChooser.APPROVE_OPTION)
						return;
					reportsDirectory = chooseFolder.getSelectedFile();
					refreshReportsAndCodemon();
				}
			}
		});
		
		// Sets the iteration limit for any tests run.
		JMenuItem iterationItem = new JMenuItem("Iteration limit...");
		iterationItem.setMnemonic(KeyEvent.getExtendedKeyCodeForChar('i'));
		iterationItem.setDisplayedMnemonicIndex(0);
		iterationItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				boolean good = false;
				while (!good) {
					// String validation mainly.
					String input = null;
					if ((input = JOptionPane.showInputDialog(fightingFrame, "Please select the number of iterations for your tests. (Max: " + MAXTURNS + ")", iterationNum)) == null) 
						return;
				
					if (!input.matches("[0-9]+")) {
						JOptionPane.showMessageDialog(fightingFrame, "That's not a number. Please try again.", "Error", JOptionPane.ERROR_MESSAGE);
					}
					else if (Integer.valueOf(input) > MAXTURNS)
						JOptionPane.showMessageDialog(fightingFrame, "Maximum is " + MAXTURNS + ".", "Error", JOptionPane.ERROR_MESSAGE);
					else {
						iterationNum = Integer.valueOf(input);
						good = true;
					}
				}
			}
		});
		
		// Setting up the radio box in our menu.
		JMenu versusList = new JMenu("Versus");
		versusList.setMnemonic(KeyEvent.getExtendedKeyCodeForChar('v'));
		versusList.setDisplayedMnemonicIndex(0);
		ButtonGroup versusGroup = new ButtonGroup();
		JMenuItem vs1Item = new JRadioButtonMenuItem("vs. 1");
		vs1Item.setMnemonic(KeyEvent.getExtendedKeyCodeForChar('1'));
		vs1Item.setDisplayedMnemonicIndex(4);
		JMenuItem vs2Item = new JRadioButtonMenuItem("vs. 2");
		vs2Item.setMnemonic(KeyEvent.getExtendedKeyCodeForChar('2'));
		vs2Item.setDisplayedMnemonicIndex(4);
		JMenuItem vs3Item = new JRadioButtonMenuItem("vs. 3", true);
		vs3Item.setMnemonic(KeyEvent.getExtendedKeyCodeForChar('3'));
		vs3Item.setDisplayedMnemonicIndex(4);
		
		// Allows the user to view a report in the program.
		JMenuItem viewItem = new JMenuItem("View");
		viewItem.setMnemonic(KeyEvent.getExtendedKeyCodeForChar('v'));
		viewItem.setDisplayedMnemonicIndex(0);
		viewItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// For when there is no selection.
				if (reportsList.getSelectedIndex() == -1)
					return;
				displayReport(reportsList.getSelectedIndex());
			}
		});
		// To delete a report.
		JMenuItem deleteItem = new JMenuItem("Delete");
		deleteItem.setMnemonic(KeyEvent.getExtendedKeyCodeForChar('d'));
		deleteItem.setDisplayedMnemonicIndex(0);
		deleteItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// When there are no reports to select.
				if (reportsList.getSelectedIndex() == -1)
					return;
				// Give them a second chance.
				int choice = JOptionPane.showConfirmDialog(displayFightingWindow, "Are you sure you wish to delete this report?", "Confirm?", JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
				if (choice == JOptionPane.OK_OPTION) {
					reportsList.getModel().getElementAt(reportsList.getSelectedIndex()).delete();
					refreshReportsAndCodemon();
				}
			}
		});
		// To fetch any previously unfetched reports from the server.
		JMenuItem fetchItem = new JMenuItem("Fetch all");
		fetchItem.setMnemonic(KeyEvent.getExtendedKeyCodeForChar('f'));
		fetchItem.setDisplayedMnemonicIndex(0);
		fetchItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				for (int i = 0; i < reportsList.getModel().getSize(); i++) {
					if (reportsList.getModel().getElementAt(i).length() == 0)
						fetchReport(reportsList.getModel().getElementAt(i));
				}
			}
		});
		JMenuItem visualItem = new JMenuItem("Visualize");
		visualItem.setMnemonic(KeyEvent.getExtendedKeyCodeForChar('v'));
		visualItem.setDisplayedMnemonicIndex(0);
		visualItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// When there are no reports to select.
				if (reportsList.getSelectedIndex() == -1)
					return;
				
				// If the report isn't fetched yet, we'd like to fetch it.
				if (reportsList.getModel().getElementAt(reportsList.getSelectedIndex()).length() == 0) {
					JOptionPane.showMessageDialog(fightingFrame, "Before you can visualize a report, you must retrieve it first.", "Fetching...", JOptionPane.INFORMATION_MESSAGE);
					fetchReport(reportsList.getModel().getElementAt(reportsList.getSelectedIndex()));
				}		
				else {
					File reportFile = reportsList.getModel().getElementAt(reportsList.getSelectedIndex());
					JDialog visualWindow = new CodemonVisualization(fightingFrame, reportFile);
				}
				
			}
		});
		// Help on this window.
		JMenuItem helpItem = new JMenuItem("Help");
		helpItem.setMnemonic(KeyEvent.getExtendedKeyCodeForChar('h'));
		helpItem.setDisplayedMnemonicIndex(0);
		helpItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				displayFightingHelp();
			}
		});
		// About the program.
		JMenuItem aboutItem = new JMenuItem("About");
		aboutItem.setMnemonic(KeyEvent.getExtendedKeyCodeForChar('a'));
		aboutItem.setDisplayedMnemonicIndex(0);
		aboutItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				displayAboutDialog(displayFightingWindow);
			}
		});
		
		fileList.add(closeItem);
		configList.add(sourceDirItem);
		configList.add(codemonDirItem);
		configList.add(reportItem);
		configList.add(iterationItem);
		configList.add(versusList);
		versusGroup.add(vs1Item);
		versusGroup.add(vs2Item);
		versusGroup.add(vs3Item);
		versusList.add(vs1Item);
		versusList.add(vs2Item);
		versusList.add(vs3Item);
		configList.add(versusList);
		reportList.add(viewItem);
		reportList.add(deleteItem);
		reportList.add(fetchItem);
		reportList.addSeparator();
		reportList.add(visualItem);
		helpList.add(helpItem);
		helpList.add(aboutItem);
		bar.add(fileList);
		bar.add(configList);
		bar.add(reportList);
		bar.add(helpList);
		fightingFrame.setJMenuBar(bar);
		
		// The quick access bar!
		JToolBar quickAccess = new JToolBar("Quick Access");
		JButton runTestButton = new JButton(new ImageIcon("src/toolbarButtonGraphics/general/Redo24.gif"));
		runTestButton.setToolTipText("Run test");
		// Single codemon test. Mostly error handling for the native code within.
		runTestButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Codemon firstCodemon = (Codemon) codemon1Select.getSelectedItem();
				
				if (firstCodemon.getFilePath() == null) {
					JOptionPane.showMessageDialog(displayFightingWindow, "Codemon #1 must be selected for a test!", "Error", JOptionPane.ERROR_MESSAGE);
					return;
				}
				String status = sendTest(firstCodemon.getFilePath().getPath(), iterationNum);
				try {
					Integer.parseInt(status);
				} catch (NumberFormatException e1) {
					JOptionPane.showMessageDialog(displayFightingWindow, "An error occurred when sending to the server: " + status, "Error", JOptionPane.ERROR_MESSAGE);
					return;
				}
				
				// A-OK. Summarize to the user and refresh reports.
				JOptionPane.showMessageDialog(displayFightingWindow, "Success! Report #" + status + " is now reserved.", "", JOptionPane.INFORMATION_MESSAGE);
				File tempReport = null;
				try {
					tempReport = new File(reportsDirectory.getPath() + "/" + status);
					tempReport.createNewFile();
				} catch (IOException e1) {
					JOptionPane.showMessageDialog(displayFightingWindow, "The report number is already on disk. Either remove the file (" + tempReport.getAbsolutePath() + "), or else fetching this report will overwrite it.", "Error", JOptionPane.ERROR_MESSAGE);
				}
				refreshReportsAndCodemon();		
			}
		});
		// For PvP2 against themselves.
		JButton runSelfButton = new JButton(new ImageIcon("src/toolbarButtonGraphics/general/Refresh24.gif"));
		runSelfButton.setToolTipText("Run self test");
		runSelfButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Codemon firstCodemon = (Codemon) codemon1Select.getSelectedItem();
				Codemon secondCodemon = (Codemon) codemon2Select.getSelectedItem();
				
				if (firstCodemon.getFilePath() == null || secondCodemon.getFilePath() == null) {
					JOptionPane.showMessageDialog(displayFightingWindow, "Two Codemons must be selected for a test!", "Error", JOptionPane.ERROR_MESSAGE);
					return;
				}
				String status = sendTestBattle(firstCodemon.getFilePath().getPath(), secondCodemon.getFilePath().getPath(), iterationNum);
				try {
					Integer.parseInt(status);
				} catch (NumberFormatException e1) {
					JOptionPane.showMessageDialog(displayFightingWindow, "An error occurred when sending to the server: " + status, "Error", JOptionPane.ERROR_MESSAGE);
					return;
				}
				
				// A-OK. Summarize to the user and refresh the reports.
				JOptionPane.showMessageDialog(displayFightingWindow, "Success! Report #" + status + " is now reserved.", "", JOptionPane.INFORMATION_MESSAGE);
				File tempReport = null;
				try {
					tempReport = new File(reportsDirectory.getPath() + "/" + status);
					tempReport.createNewFile();
				} catch (IOException e1) {
					JOptionPane.showMessageDialog(displayFightingWindow, "The report number is already on disk. Either remove the file (" + tempReport.getAbsolutePath() + "), or else fetching this report will overwrite it.", "Error", JOptionPane.ERROR_MESSAGE);
				}
				refreshReportsAndCodemon();	
			}
		});
		// For actual PvP battles.
		JButton runPVPButton = new JButton(new ImageIcon("src/toolbarButtonGraphics/general/SendMail24.gif"));
		runPVPButton.setToolTipText("Run PVP");
		runPVPButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Codemon firstCodemon = (Codemon) codemon1Select.getSelectedItem();
				int pvpNum = 4;
				if (vs1Item.isSelected()) {
					pvpNum = 2;
				}
				else if (vs2Item.isSelected()) {
					pvpNum = 3;
				}
				
				if (firstCodemon.getFilePath() == null) {
					JOptionPane.showMessageDialog(displayFightingWindow, "Codemon #1 must be selected for a test!", "Error", JOptionPane.ERROR_MESSAGE);
					return;
				}
				String status = sendBattle(firstCodemon.getFilePath().getPath(), pvpNum);
				try {
					Integer.parseInt(status);
				} catch (NumberFormatException e1) {
					JOptionPane.showMessageDialog(displayFightingWindow, "An error occurred when sending to the server: " + status, "Error", JOptionPane.ERROR_MESSAGE);
					return;
				}
				
				// A-OK. Summarize to the user and refresh the reports.
				JOptionPane.showMessageDialog(displayFightingWindow, "Success! Report #" + status + " is now reserved.", "", JOptionPane.INFORMATION_MESSAGE);
				File tempReport = null;
				try {
					tempReport = new File(reportsDirectory.getPath() + "/" + status);
					tempReport.createNewFile();
				} catch (IOException e1) {
					JOptionPane.showMessageDialog(displayFightingWindow, "The report number is already on disk. Either remove the file (" + tempReport.getAbsolutePath() + "), or else fetching this report will overwrite it.", "Error", JOptionPane.ERROR_MESSAGE);
				}
				refreshReportsAndCodemon();	
			}
		});
		
		// For setting off the visualization of a report.
		JButton visualButton = new JButton(new ImageIcon("src/toolbarButtonGraphics/media/Movie24.gif"));
		visualButton.setToolTipText("Visualize");
		visualButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				visualItem.getActionListeners()[0].actionPerformed(e);
			}
		});
		
		// For deleting a report from the list. Already defined above.
		JButton deleteButton = new JButton(new ImageIcon("src/toolbarButtonGraphics/general/Delete24.gif"));
		deleteButton.setToolTipText("Delete report");
		deleteButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				deleteItem.getActionListeners()[0].actionPerformed(e);
			}
		});
		
		quickAccess.add(runTestButton);
		quickAccess.add(runSelfButton);
		quickAccess.add(runPVPButton);
		quickAccess.add(visualButton);
		quickAccess.add(deleteButton);
		mainPanel.add(quickAccess, BorderLayout.NORTH);
		
		// Setting up the various layouts on this window.
		JPanel displayPanel = new JPanel();
		displayPanel.setLayout(new BorderLayout());
		JPanel codemonQuickViewPanel = new JPanel();
		codemonQuickViewPanel.setLayout(new BorderLayout());
		JPanel codemonPanel = new JPanel();
		codemonPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLoweredBevelBorder(), "Choose your Codemon!"));
		codemonPanel.setLayout(new GridLayout(2, 2));
		JPanel quickViewPanel = new JPanel();
		quickViewPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLoweredBevelBorder(), "Quick view"));
		quickViewPanel.setLayout(new BorderLayout());
		JPanel reportsPanel = new JPanel();
		reportsPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLoweredBevelBorder(), "Reports"));
		reportsPanel.setLayout(new BorderLayout());
		
		// The interior features of said panels!
		codemon1Select = new JComboBox<Codemon>();
		codemon1Select.addItem(new Codemon("-- None --", null));
		codemon2Select = new JComboBox<Codemon>();
		codemon2Select.addItem(new Codemon("-- None --", null));
		codemonList = new JList<Codemon>();
		codemonList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		codemonList.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				// On double-click:
				if (e.getClickCount() == 2) {
									
					// Get location of the click and match to an assembled codemon.
					int index = codemonList.locationToIndex(e.getPoint());
					if (index < 0)
						return;
					File tempFile = codemonList.getModel().getElementAt(index).getFilePath();
					// We know the name of the source file. Try and locate it.
					tempFile = new File(cmDirectory.getPath() + "/" + tempFile.getName().substring(0, tempFile.getName().length() - 7) + "cm");

					// It's possible it doesn't exist though.
					if (!tempFile.exists()) {
						JOptionPane.showMessageDialog(displayFightingWindow, "No source file for that codemon in the current source directory!", "Error", JOptionPane.ERROR_MESSAGE);
						return;
					}
					// Display the source code to the user.
					displaySource(tempFile);
				}
			}
		});
		JScrollPane codemonListScroller = new JScrollPane(codemonList, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		codemonListScroller.setAlignmentX(JScrollPane.LEFT_ALIGNMENT);
		
		
		// Now for the reports list!
		reportsList = new JList<File>();
		reportsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		// Set up the style and highlighting.
		reportsList.setCellRenderer(new ListCellRenderer<File>() {
			@Override
			public Component getListCellRendererComponent(JList<? extends File> list, File value, int index, boolean isSelected, boolean cellHasFocus) {
				try {
					JLabel returnLabel = new JLabel(value.getName());
					Scanner sc = new Scanner(value);
					if (!sc.hasNext())
						returnLabel.setForeground(Color.RED);
					sc.close();
					if (cellHasFocus) 
						returnLabel.setBackground(new Color(30, 255, 75));
					returnLabel.setOpaque(true);
					return returnLabel;
				} catch (FileNotFoundException e) { /* Won't occur */ }
				return null;
			}
		});
		reportsList.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				// If there is a double click:
				if (e.getClickCount() == 2) {
									
					File tempFile;
					// Get the index clicked.
					int index = reportsList.locationToIndex(e.getPoint());

					// Either fetch the report or display it if it's already local.
					if (index >= 0 && (tempFile = reportsList.getModel().getElementAt(index)).length() == 0) {
						fetchReport(tempFile);
					}
					else {
						displayReport(index);
					}
				}
			}
		});
		
		JScrollPane reportListScroller = new JScrollPane(reportsList, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		reportListScroller.setAlignmentX(JScrollPane.LEFT_ALIGNMENT);
		
		codemonPanel.add(new JLabel("Codemon 1"));
		codemonPanel.add(codemon1Select);
		codemonPanel.add(new JLabel("Codemon 2"));
		codemonPanel.add(codemon2Select);
		quickViewPanel.add(codemonListScroller);
		reportsPanel.add(reportListScroller);
		reportsPanel.add(Box.createHorizontalStrut(REPORTS_WIDTH), BorderLayout.NORTH);
		codemonQuickViewPanel.add(codemonPanel, BorderLayout.NORTH);
		codemonQuickViewPanel.add(quickViewPanel);
		displayPanel.add(codemonQuickViewPanel);
		displayPanel.add(reportsPanel, BorderLayout.EAST);
		mainPanel.add(displayPanel);
		
		// Refresh!
		refreshReportsAndCodemon();		
		fightingFrame.setVisible(true);
		fightingFrame.revalidate();
		fightingFrame.repaint();
		fightingFrame.pack();
		
		if (runFileImmediately != null)
			refreshReportsAndCodemon();		

	}
	
//------------------------------------------------------------------------------------------------
	
	protected void displayAboutDialog(Window frame) {
		JTextArea aboutText = new JTextArea();
		aboutText.setEditable(false);
		
		// Include the README file.
		File readme = new File("README.txt");
		try {
			StringBuilder sb = new StringBuilder();
			FileReader sc = new FileReader(readme);
			for (int i = 0; i < readme.length(); i++) {
				sb.append((char) sc.read());
			}
			sc.close();
			String tempText = "CODEMON FIGHTING SIMULATOR - GUI\nCIS*2750\n\n By: Eric Leblanc";
			aboutText.setText(tempText + sb.toString());
		} catch (IOException e) { /* Will never occur */ }
		
		JDialog aboutDisplay = new JDialog(frame, "ABOUT");
		aboutDisplay.setResizable(true);
		aboutDisplay.setSize(reportDisplaySize);
		aboutDisplay.setPreferredSize(new Dimension(700, 700));
		aboutDisplay.setLayout(new BorderLayout());
		JScrollPane scrollAbout = new JScrollPane(aboutText, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		JButton closeAbout = new JButton("Close");
		closeAbout.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				aboutDisplay.dispose();
			}
		});
		aboutDisplay.add(scrollAbout);
		aboutDisplay.add(closeAbout, BorderLayout.SOUTH);
		aboutDisplay.setVisible(true);
		aboutDisplay.revalidate();
		aboutDisplay.repaint();
		aboutDisplay.pack();
	}
	
	protected void displayTrainingHelp() {
		JTextArea aboutText = new JTextArea();
		aboutText.setEditable(false);
		aboutText.setText("CODEMON TRAINING CENTER\n\nWelcome to the Codemon Training Center!\n\nThe main feature of this window is to write and assemble (compile) your source code.\nJust type your commands, save and assemble! You can configure your save directories and launch the fighting window from here too.");
	
		JDialog aboutDisplay = new JDialog(displayTrainingWindow, "HELP - TRAINING CENTER");
		aboutDisplay.setModal(true);
		aboutDisplay.setResizable(true);
		aboutDisplay.setSize(new Dimension(800, 300));
		aboutDisplay.setPreferredSize(new Dimension(800, 300));
		aboutDisplay.setLayout(new BorderLayout());
		JScrollPane scrollAbout = new JScrollPane(aboutText, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		JButton closeAbout = new JButton("Close");
		closeAbout.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				aboutDisplay.setModal(false);
				aboutDisplay.dispose();
			}
		});
		aboutDisplay.add(scrollAbout);
		aboutDisplay.add(closeAbout, BorderLayout.SOUTH);
		aboutDisplay.setVisible(true);
		aboutDisplay.revalidate();
		aboutDisplay.repaint();
		aboutDisplay.pack();
	}
	
	protected void displayFightingHelp() {
		JTextArea aboutText = new JTextArea();
		aboutText.setEditable(false);
		aboutText.setText("CODEMON FIGHTING CENTER\n\nWelcome to the Codemon Fighting Center!\n\nHere you can set up your assembled codemon to fight others!\nJust choose the Codemon you've assembled from the drop-down menus, configure your settings in the menu bar, and select a battle mode!\n\nYou can also view the results of these and past fights here too.\nClicking a red link will fetch the report from the server and save it to your disk (which you can delete if you wish).\nYou may also review your source code for your Codemon here too!");
	
		JDialog aboutDisplay = new JDialog(displayFightingWindow, "HELP - FIGHTING CENTER");
		aboutDisplay.setModal(true);
		aboutDisplay.setResizable(true);
		aboutDisplay.setSize(new Dimension(800, 300));
		aboutDisplay.setPreferredSize(new Dimension(800, 300));
		aboutDisplay.setLayout(new BorderLayout());
		JScrollPane scrollAbout = new JScrollPane(aboutText, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		JButton closeAbout = new JButton("Close");
		closeAbout.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				aboutDisplay.setModal(false);
				aboutDisplay.dispose();
			}
		});
		aboutDisplay.add(scrollAbout);
		aboutDisplay.add(closeAbout, BorderLayout.SOUTH);
		aboutDisplay.setVisible(true);
		aboutDisplay.revalidate();
		aboutDisplay.repaint();
		aboutDisplay.pack();
	}
	
//-------------------------------------------------------------------------------------------------
	
	/**
	 * A helper function managing all of the processing during a save in the training area.
	 * @param explicitySaveAs a flag which determines whether or not we always show a filechooser.
	 * @param assumeSave a flag which foregoes (re-)asking the user if they wish to save.
	 */
	private boolean promptTrainSave(boolean explicitlySaveAs, boolean assumeSave) {
		if (trainingModified || assumeSave) {
			// If there have been modifications made to the text, prompt the user to save first.
			int choice = JOptionPane.YES_OPTION;
			if (!assumeSave)
				choice = JOptionPane.showConfirmDialog(displayTrainingWindow, "You have unsaved changes to this Codemon. Would you like to save them?", "Save?", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
			if (choice == JOptionPane.YES_OPTION) {
				
				// Is the file on disk already? Just save.
				if (trainingHasName && !explicitlySaveAs) {
					
					int status = 0;
					String fileText = editArea.getText();
					try {
						FileWriter fr = new FileWriter(trainingCurrentFile);
						fr.write(fileText);
						fr.close();
					} catch (IOException e) {
						status = JOptionPane.showConfirmDialog(displayTrainingWindow, "Could not write changes. Choose another path to save to?", "Error", JOptionPane.YES_NO_OPTION, JOptionPane.ERROR_MESSAGE);
						if (status == JOptionPane.NO_OPTION)
							return false;
						status = -1;
					}
					if (status != -1) {
						toggleTrainingSaveButtons(false);
						displayTrainingWindow.revalidate();
						displayTrainingWindow.repaint();
						displayTrainingWindow.pack();
						return true;
					}
				}
					
				// Offer a save as dialog if we get to here.
				JFileChooser filePrompt = new JFileChooser(cmDirectory);
				filePrompt.setFileFilter(new FileNameExtensionFilter("Codemon source files (.cm)", "cm"));
				if (trainingCurrentFile != null) 
					filePrompt.setSelectedFile(trainingCurrentFile);
				
				// If there was an issue when saving:
				if (filePrompt.showSaveDialog(displayTrainingWindow) != JFileChooser.APPROVE_OPTION) {
					JOptionPane.showMessageDialog(displayTrainingWindow, "Not saving changes to your Codemon. Aborting.", "Error", JOptionPane.ERROR_MESSAGE);
				}
				// Otherwise:
				else {
					boolean safe = false;
					while (!safe) {
						String fileNameChosen = filePrompt.getSelectedFile().getPath();
						if (!fileNameChosen.endsWith(".cm")) 
							fileNameChosen += ".cm";
						
						// Set the new current training file reference. And check if it even exists.
						trainingCurrentFile = new File(fileNameChosen);
						if (trainingCurrentFile.exists()) {
							if (JOptionPane.showConfirmDialog(displayTrainingWindow, "This file exists already. Are you sure you wish to overwrite?", "Overwrite file?", JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE) == JOptionPane.NO_OPTION) {
								if (filePrompt.showSaveDialog(displayTrainingWindow) != JFileChooser.APPROVE_OPTION) {
									JOptionPane.showMessageDialog(displayTrainingWindow, "Not saving changes to your Codemon. Aborting.", "Error", JOptionPane.ERROR_MESSAGE);
									return false;
								}
								continue;
							}
							else 
								safe = true;
						}
						else 
							safe = true;
					}
						
					// Save the contents of the edit area to disk.
					try {
						FileWriter fr = new FileWriter(trainingCurrentFile);
						fr.write(editArea.getText());
						fr.close();
					} catch (IOException e) {
						JOptionPane.showMessageDialog(displayTrainingWindow, "Could not save changes to your Codemon: \"" + e.getMessage() + "\" Aborting.", "Error", JOptionPane.ERROR_MESSAGE);
						return false;
					}
					
					// Housekeeping.
					toggleTrainingSaveButtons(false);
					trainingHasName = true;
					editArea.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLoweredBevelBorder(), trainingCurrentFile.getName()));
					displayTrainingWindow.revalidate();
					displayTrainingWindow.repaint();
					displayTrainingWindow.pack();
					return true;
				}
			}
			
			// If the user says no, just don't save. But continue going as if it was saved.
			else if (choice == JOptionPane.NO_OPTION) 
				return true;
			
			// If we get to here, something went wrong when saving. Abort.
			return false;
		}
		// If we get to here, there was nothing to save anyway.
		return true;
	}
	
	/**
	 * A helper function to set the button functionality based on the save state.
	 * @param modified a flag telling us if there are unsaved modifications made to the edit area.
	 */
	private void toggleTrainingSaveButtons(boolean modified) {
		
		trainingModified = modified;
		synchronizeEditAreaListener();
		saveItem.setEnabled(modified);
		saveButton.setEnabled(modified);
		saveAsItem.setEnabled(true);
		saveAsButton.setEnabled(true);
		// Set the label.
		if (!modified)
			modifiedInfo.setText("\t");
		else
			modifiedInfo.setText("MODIFIED");
		displayTrainingWindow.revalidate();
		displayTrainingWindow.repaint();
		displayTrainingWindow.pack();
	}
	
	/**
	 * Another similar helper function, this time handling button functionality when opening or closing.
	 * @param open a flag determining whether or not we were opening or closing a source file.
	 */
	private void handleOpenClose(boolean open) {
		toggleTrainingSaveButtons(false);
		editArea.setEnabled(open);
		saveItem.setEnabled(false);
		saveButton.setEnabled(false);
		saveAsItem.setEnabled(false);
		saveAsButton.setEnabled(false);
		closeItem.setEnabled(open);
		assembleItem.setEnabled(open);
		assembleButton.setEnabled(open);
		assembleLaunchItem.setEnabled(open);
		displayTrainingWindow.revalidate();
		displayTrainingWindow.repaint();
		displayTrainingWindow.pack();
	}
	
	/**
	 * A helper function for refreshing the state of the listener on the edit area.
	 * Essentially, we don't want to slow down the program by listening for keystrokes all the time.
	 */
	private void synchronizeEditAreaListener() {
		if (!trainingModified && !editAreaListenerOn) {
			editArea.getDocument().addDocumentListener(editAreaListener);
			editAreaListenerOn = true;
		}
		else if (trainingModified && editAreaListenerOn) {
			editArea.getDocument().removeDocumentListener(editAreaListener);
			editAreaListenerOn = false;
		}
		displayTrainingWindow.revalidate();
		displayTrainingWindow.repaint();
		displayTrainingWindow.pack();
	}
	
	/**
	 * A helper function to refresh the state of the two JLists in the training area.
	 */
	private void refreshReportsAndCodemon() {
		
		// Create our vector holding the various Codemon objects.
		Vector<Codemon> codemonVector = new Vector<Codemon>();
		Vector<Codemon> codemonSelectVector = new Vector<Codemon>();
		for (File curFile : codemonDirectory.listFiles()) {
			// Scan the directory; skip any files not ending in ".codemon" and directories.
			if (curFile.getPath().endsWith(".codemon") && !curFile.isDirectory()) {
				codemonVector.add(new Codemon(curFile.getName().substring(0, curFile.getName().length() - 8), curFile));
				codemonSelectVector.add(new Codemon(curFile.getName().substring(0, curFile.getName().length() - 8), curFile));
			}
		}
		// Send the Vector along to the JList for styling.
		// Note that we have an extra entry for "None". Just a null file pointer.
		codemonSelectVector.add(0, new Codemon("-- None --", null));
		codemonList.setListData(codemonVector);
		codemon1Select.setModel(new DefaultComboBoxModel<Codemon>(codemonSelectVector));
		codemon2Select.setModel(new DefaultComboBoxModel<Codemon>(codemonSelectVector));
		
		
		
		// Now for the reports. Same deal.
		Vector<File> reportVector = new Vector<File>();
		for (File curFile : reportsDirectory.listFiles()) {
			// Since we aren't sorting by file extension, skip all directories and hidden files.
			// Everything else is fair game. User shouldn't be mucking with these anyway!
			if (!curFile.isDirectory() && curFile.getName().charAt(0) != '.')
				reportVector.add(curFile);
		}
		// Send the Vector along.
		reportsList.setListData(reportVector);
		
		// Refresh the screen.
		displayFightingWindow.revalidate();
		displayFightingWindow.repaint();
		displayFightingWindow.pack();
		
	}
	
//-------------------------------------------------------------------------------------------------
	
	/**
	 * Displays a screen for viewing reports.
	 * @param index the JList index corresponding to the report wanted by the user.
	 */
	private void displayReport(int index) {
		// When the list is empty.
		if (index < 0)
			return;
		JTextArea reportText = new JTextArea();
		reportText.setEditable(false);
		
		String name = reportsList.getModel().getElementAt(index).getName();
		try {
			// Read in the file and build the report.
			StringBuilder sb = new StringBuilder();
			FileReader sc = new FileReader(reportsList.getModel().getElementAt(index));
			for (int i = 0; i < reportsList.getModel().getElementAt(index).length(); i++) {
				sb.append((char) sc.read());
			}
			sc.close();
			reportText.setText(sb.toString());
		} catch (IOException e) { /* Will never occur */ }
	
		// Layout the window.
		JDialog reportDisplay = new JDialog(displayFightingWindow, "Report #" + name);
		reportDisplay.setModal(true);
		reportDisplay.setResizable(true);
		reportDisplay.setSize(reportDisplaySize);
		reportDisplay.setPreferredSize(reportDisplaySize);
		reportDisplay.setLayout(new BorderLayout());
		JScrollPane scrollReport = new JScrollPane(reportText, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		JButton closeReport = new JButton("Close");
		closeReport.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				reportDisplay.setModal(false);
				reportDisplay.dispose();
			}
		});
		reportDisplay.add(scrollReport);
		reportDisplay.add(closeReport, BorderLayout.SOUTH);
		reportDisplay.setVisible(true);
		
		// Refresh!
		refreshReportsAndCodemon();
		reportDisplay.revalidate();
		reportDisplay.repaint();
		reportDisplay.pack();
		
	}
	
	/**
	 * Displays a window for reading source code at a glance.
	 * @param sourceFile the file to read.
	 */
	private void displaySource(File sourceFile) {
		JTextArea sourceText = new JTextArea();
		sourceText.setEditable(false);
		
		try {
			// Read in the file and build the source code.
			StringBuilder sb = new StringBuilder();
			FileReader sc = new FileReader(sourceFile);
			for (int i = 0; i < sourceFile.length(); i++) {
				sb.append((char) sc.read());
			}
			sc.close();
			sourceText.setText(sb.toString());
		} catch (IOException e) { /* Will never occur */ }
	
		// Layout the window.
		JDialog sourceDisplay = new JDialog(displayFightingWindow, sourceFile.getName());
		sourceDisplay.setModal(true);
		sourceDisplay.setResizable(true);
		sourceDisplay.setSize(reportDisplaySize);
		sourceDisplay.setPreferredSize(reportDisplaySize);
		sourceDisplay.setLayout(new BorderLayout());
		JScrollPane scrollSource = new JScrollPane(sourceText, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		JButton closeSource = new JButton("Close");
		closeSource.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				sourceDisplay.setModal(false);
				sourceDisplay.dispose();
			}
		});
		sourceDisplay.add(scrollSource);
		sourceDisplay.add(closeSource, BorderLayout.SOUTH);
		sourceDisplay.setVisible(true);
		
		// Refresh!
		refreshReportsAndCodemon();
		sourceDisplay.revalidate();
		sourceDisplay.repaint();
		sourceDisplay.pack();
	}
	
	/**
	 * Sends a request to the server using native code for the contents of a report.
	 * @param reportFile the local storage for the existing report (empty or not).
	 * @return a flag if the retrieval was successful.
	 */
	private boolean fetchReport(File reportFile) {
		
		String status = getReport(reportFile.getName(), reportFile.getPath());
		
		// Couldn't get the report yet.
		if (status != null) {
			JOptionPane.showMessageDialog(displayFightingWindow, status, "Error", JOptionPane.ERROR_MESSAGE);
			return false;
		}
		
		// Refresh!
		int index = 0;
		refreshReportsAndCodemon();
		for (int i = 0; i < reportsList.getModel().getSize(); i++) {
			if (reportsList.getModel().getElementAt(i).getName() == reportFile.getName()) {
				index = i;
				break;
			}
		}
		// Display the report.
		displayReport(index);
		return true;
	}
	
//-------------------------------------------------------------------------------------------------
	
	/**
	 * Helper class to define the training window's DocumentListener.
	 */
	private class CodemonEditAreaListener implements DocumentListener {
		@Override
		public void changedUpdate(DocumentEvent e) {
			toggleTrainingSaveButtons(true);
		}
		public void insertUpdate(DocumentEvent e) {
			toggleTrainingSaveButtons(true);
		}
		public void removeUpdate(DocumentEvent e) {
			toggleTrainingSaveButtons(true);
		}
	}
	
	/**
	 * Helper class to define the source config buttons.
	 */
	private class SourceDirActionListener implements ActionListener {
		
		private boolean fightingWindow = false;
		
		public SourceDirActionListener(boolean fightingWindow) {
			if (fightingWindow)
				this.fightingWindow = fightingWindow;
			
		}
			@Override
			public void actionPerformed(ActionEvent e) {
				int choice = JOptionPane.showConfirmDialog((Component) e.getSource(), "Would you like to type in the path manually?", "", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
				if (choice == JOptionPane.YES_OPTION) {
					boolean done = false;
					while (!done) {
						String result;
						if ((result = JOptionPane.showInputDialog((Component) e.getSource(), "Please enter the path of the directory you wish to set as default.", cmDirectory.getAbsolutePath())) == null)
								return;
						File chosenFile = new File(result);
						if (chosenFile.exists() && chosenFile.isDirectory()) {
							cmDirectory = chosenFile;
							done = true;
						}
						else if (!chosenFile.exists())
							JOptionPane.showMessageDialog((Component) e.getSource(), "That's not a valid path. Please try again.", "Error", JOptionPane.ERROR_MESSAGE);
						else 
							JOptionPane.showMessageDialog((Component) e.getSource(), "That's not a valid directory. Please try again.", "Error", JOptionPane.ERROR_MESSAGE);
					}
				}
				else {
					JFileChooser chooseFolder = new JFileChooser(cmDirectory);
					chooseFolder.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
					chooseFolder.setFileFilter(new FileFilter() {
						@Override
						public boolean accept(File file) {
							return file.isDirectory();
						}
						@Override
						public String getDescription() {
							return "Folders";
						}
					});
					if (chooseFolder.showOpenDialog((Component) e.getSource()) != JFileChooser.APPROVE_OPTION)
						return;
					cmDirectory = chooseFolder.getSelectedFile();
				}
				if (fightingWindow)
					refreshReportsAndCodemon();
			}
	}
	
	/**
	 * Helper class to define the Codemon directory config buttons.
	 */
	private class CodemonDirActionListener implements ActionListener {
		
			private boolean fightingWindow = false;
		
			public CodemonDirActionListener(boolean fightingWindow) {
				if (fightingWindow)
					this.fightingWindow = fightingWindow;
				
			}

			@Override
			public void actionPerformed(ActionEvent e) {
				int choice = JOptionPane.showConfirmDialog((Component) e.getSource(), "Would you like to type in the path manually?", "", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
				if (choice == JOptionPane.YES_OPTION) {
					boolean done = false;
					while (!done) {
						String result;
						if ((result = JOptionPane.showInputDialog((Component) e.getSource(), "Please enter the path of the directory you wish to set as default.", codemonDirectory.getAbsolutePath())) == null)
								return;
						File chosenFile = new File(result);
						if (chosenFile.exists() && chosenFile.isDirectory()) {
							codemonDirectory = chosenFile;
							done = true;
						}
						else if (!chosenFile.exists())
							JOptionPane.showMessageDialog((Component) e.getSource(), "That's not a valid path. Please try again.", "Error", JOptionPane.ERROR_MESSAGE);
						else 
							JOptionPane.showMessageDialog((Component) e.getSource(), "That's not a valid directory. Please try again.", "Error", JOptionPane.ERROR_MESSAGE);
					}
				}
				else {
					JFileChooser chooseFolder = new JFileChooser(codemonDirectory);
					chooseFolder.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
					chooseFolder.setFileFilter(new FileFilter() {
						@Override
						public boolean accept(File file) {
							return file.isDirectory();
						}
						@Override
						public String getDescription() {
							return "Folders";
						}
					});
					if (chooseFolder.showOpenDialog((Component) e.getSource()) != JFileChooser.APPROVE_OPTION)
						return;
					codemonDirectory = chooseFolder.getSelectedFile();
					
				}
				if (fightingWindow)
					refreshReportsAndCodemon();
			}
	}
	
	/**
	 * Helper class to hold information about an assembled Codemon.
	 */
	protected class Codemon {
		String name = null;
		File path = null;
		
		public Codemon(String name, File path) {
			this.name = name;
			this.path = path;
		}
		
		public File getFilePath() {
			return path;
		}
		
		@Override
		public String toString() {
			return this.name;
		}
	}
	
//--------------------------------------------------------------------------------------------------------	

	/**
	 * Closes the GUI.
	 */
	protected void closeGUI() {
		System.exit(0);
	}
	
}
