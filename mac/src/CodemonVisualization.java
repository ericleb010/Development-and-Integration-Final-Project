import java.io.*;
import java.util.Scanner;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;

import javax.swing.*;

public class CodemonVisualization extends JDialog {
	
	private static final int MEMORY = 8192;
	private static final int COL_WIDTH = 256;
	private static final int COL_HEIGHT = MEMORY / COL_WIDTH;
	private static final int COL_BUFFER_SIZE = 100;
	private static final int MIN_SPEED = 0;
	private static final int MAX_SPEED = 100;
	private static final int NUM_TICKS = 25;
	
	private static final Color P1_COLOR = Color.GREEN;
	private static final Color P2_COLOR = Color.ORANGE;
	private static final Color P3_COLOR = Color.RED;
	private static final Color P4_COLOR = Color.BLUE;
	
	// JObjects
	JDialog parent = null;
	JButton runButton = null;
	JButton closeButton = null;
	JPanel header = null;
	JLabel image = null;
	BufferedImage mainImage = null;
	
	// Utility values.
	File reportFile = null;
	String s = null;
	
	float curSpeed = 0;
	
	// Other values we'll need.
	int turns = 0;
	int codemonAvailable = 0;
	String p1Name = null;
	String p2Name = null;
	String p3Name = null;
	String p4Name = null;
	JLabel p1Label = null;
	JLabel p2Label = null;
	JLabel p3Label = null;
	JLabel p4Label = null;
	int p1Lines = 0;
	int p2Lines = 0;
	int p3Lines = 0;
	int p4Lines = 0;
	int p1Start = 0;
	int p2Start = 0;
	int p3Start = 0;
	int p4Start = 0;
	
	/**
	 * Constructor for the JFrame.
	 * @param parent
	 * @param reportFile
	 */
	public CodemonVisualization(JDialog thatParent, File reportFile) {
		
		super(thatParent, "Visualisation of Report #" + reportFile.getName());
		this.setModal(true);
		this.setLayout(new BorderLayout());
		this.setResizable(false);
		
		this.parent = this;
		this.reportFile = reportFile;
		
		runButton = new JButton("Run!");
		runButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					runAnimation();
				} catch (FileNotFoundException e1) { /* Won't occur */ }
					catch (InterruptedException e2) { }
			}
		});
		
		header = new JPanel();
		header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
		JPanel labels = new JPanel();
		labels.setLayout(new BoxLayout(labels, BoxLayout.Y_AXIS));
		p1Label = new JLabel("");
		p1Label.setForeground(P1_COLOR);
		p2Label = new JLabel("");
		p2Label.setForeground(P2_COLOR);
		p3Label = new JLabel("");
		p3Label.setForeground(P3_COLOR);
		p4Label = new JLabel("");
		p4Label.setForeground(P4_COLOR);
		labels.add(p1Label);
		labels.add(p2Label);
		labels.add(p3Label);
		labels.add(p4Label);
		JSlider slider = new JSlider(MIN_SPEED, MAX_SPEED, (MIN_SPEED + MAX_SPEED) / 2);
		slider.setMinorTickSpacing((MAX_SPEED - MIN_SPEED) / NUM_TICKS);
		header.add(labels);
		header.add(slider);
		header.add(runButton);
		
		JPanel grid = new JPanel();
		mainImage = new BufferedImage(COL_WIDTH, COL_HEIGHT * 4, BufferedImage.TYPE_INT_RGB);
		image = new JLabel(new ImageIcon(mainImage));
		grid.add(image);
		
				
		closeButton = new JButton("Close");
		closeButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				parent.setModal(false);
				parent.dispose();
			}
		});
		
		this.add(header, BorderLayout.NORTH);
		this.add(closeButton, BorderLayout.SOUTH);
		this.add(grid);
		this.setSize(COL_WIDTH, COL_HEIGHT * 4);
		
		revalidate();
		repaint();
		pack();
		this.setVisible(true);
		
		
		revalidate();
		repaint();
		pack();
		
	}
	
	
	
	
	/**
	 * Initializes the animation.
	 * @throws FileNotFoundException
	 * @throws InterruptedException
	 */
	protected void runAnimation() throws FileNotFoundException, InterruptedException {
		Scanner sc = new Scanner(reportFile);
		StringBuilder build = new StringBuilder();
		while (sc.hasNextLine()) {
			build.append(sc.nextLine());
		}
		s = build.toString();
		sc.close();
		
		int index = 0;
		
		// Get the turn number.
		if ((index = s.indexOf("(turn limit = ") + 14) - 14 != -1) {
			int endIndex = s.indexOf(')', index);
			turns = Integer.parseInt(s.substring(index, endIndex));
		}
		else 
			turns = 0;
		
		
		for (int i = 1; i <= 4; i++) {
			if ((index = s.indexOf(", \"", index + 3)) == -1) 
				break;
			
			codemonAvailable = i;
			
			switch (i) {
			
				case 1:
					p1Name = s.substring(index + 3, s.indexOf(',', index + 3));
					p1Label.setText(p1Name);
					p1Start = Integer.parseInt(s.substring(s.indexOf("address ", index) + 8, s.indexOf("---", index)));
					p1Lines = Integer.parseInt(s.substring(s.indexOf("lines=", index) + 6, s.indexOf(" begin=", index)));
					for (int y = 0; y < p1Lines; y++) {
						for (int z = 0; z < 4; z++) {
							mainImage.setRGB((p1Start + y) % COL_WIDTH, (((p1Start + y) / COL_WIDTH) * 4) + z, P1_COLOR.getRGB());
						}
					}
					
					repaint();
					revalidate();
					break;
				
				case 2:
					p2Name = s.substring(index + 3, s.indexOf(',', index + 3));
					p2Label.setText(p2Name);
					p2Start = Integer.parseInt(s.substring(s.indexOf("address ", index) + 8, s.indexOf("---", index)));
					p2Lines = Integer.parseInt(s.substring(s.indexOf("lines=", index) + 6, s.indexOf(" begin=", index)));
					for (int y = 0; y < p2Lines; y++) {
						for (int z = 0; z < 4; z++) {
							mainImage.setRGB((p2Start + y) % COL_WIDTH, (((p2Start + y) / COL_WIDTH) * 4) + z, P2_COLOR.getRGB());
						}
					}
					repaint();
					revalidate();
					break;
				
				case 3:
					p3Name = s.substring(index + 3, s.indexOf(',', index + 3));
					p3Label.setText(p3Name);
					p3Start = Integer.parseInt(s.substring(s.indexOf("address ", index) + 8, s.indexOf("---", index)));
					p3Lines = Integer.parseInt(s.substring(s.indexOf("lines=", index) + 6, s.indexOf(" begin=", index)));
					for (int y = 0; y < p3Lines; y++) {
						for (int z = 0; z < 4; z++) {
							mainImage.setRGB((p3Start + y) % COL_WIDTH, (((p3Start + y) / COL_WIDTH) * 4) + z, P3_COLOR.getRGB());
						}
					}
					repaint();
					revalidate();
					break;
				
				case 4:
					p4Name = s.substring(index + 3, s.indexOf(',', index + 3));
					p4Label.setText(p4Name);
					p4Start = Integer.parseInt(s.substring(s.indexOf("address ", index) + 8, s.indexOf("---", index)));
					p4Lines = Integer.parseInt(s.substring(s.indexOf("lines=", index) + 6, s.indexOf(" begin=", index)));
					for (int y = 0; y < p4Lines; y++) {
						for (int z = 0; z < 4; z++) {
							mainImage.setRGB((p4Start + y) % COL_WIDTH, (((p4Start + y) / COL_WIDTH) * 4) + z, P4_COLOR.getRGB());
						}
					}
					repaint();
					revalidate();
					break;
			}
		}
		
		index = s.indexOf("commenced...") + 20;
		
		for (int i = 0; i < (codemonAvailable * turns); i++) {
			// Has the game ended prematurely?
			if (s.indexOf('[', index) > s.indexOf("> In the end,", index))
				break;
			
			readAndRefresh(index);
				
			index += 80;
			
		}
		
	}
	
	
	/**
	 * This function reads all values we need for each turn in the report.
	 * @param asOf
	 */
	private void readAndRefresh(int asOf) {
		
		int index = s.indexOf('[', asOf);
		
		// Initialise what we need.
		int turnNum;
		int playerNum;
		int startPC;
		int pc1 = -1;
		int pc2 = -1;
		int pc3 = -1;
		int pc4 = -1;
		
		// Collect all of the data.
		turnNum = Integer.parseInt(s.substring(index + 1, s.indexOf('|', index)).trim());
		playerNum = Integer.parseInt(s.substring(s.indexOf('|', index) + 1, s.indexOf('(', index)));
		startPC = Integer.parseInt(s.substring(s.indexOf("PC=", index) + 3, s.indexOf("PC=", index) + 7).trim());
		
		for (int i = 1; i <= 1; i++) {
			int testIndex = index;
			if ((testIndex = s.indexOf('\'', testIndex)) != -1) {
				switch(i) {
					case 1:
						pc1 = Integer.parseInt(s.substring(testIndex + 1, testIndex + 5).trim());
						break;
					case 2:
						pc2 = Integer.parseInt(s.substring(testIndex + 1, testIndex + 5).trim());
						break;
					case 3:
						pc3 = Integer.parseInt(s.substring(testIndex + 1, testIndex + 5).trim());
						break;
					case 4:
						pc4 = Integer.parseInt(s.substring(testIndex + 1, testIndex + 5).trim());
						break;
				}
			}
			else 
				break;
		}

		
		// Now we can update the image for the user for this move.
		
		switch (playerNum) {
			case 1:
				if (pc1 != -1) {
					for (int i = 0; i < 4; i++) {
						mainImage.setRGB(pc1 % COL_WIDTH, ((pc1 / COL_WIDTH) * 4) + i, P1_COLOR.getRGB());
					}
					
					image.setIcon(new ImageIcon(mainImage));
					repaint();
					revalidate();
					
				}
				
				if (pc2 != -1) {
					for (int i = 0; i < 4; i++) {
						mainImage.setRGB(pc2 % COL_WIDTH, ((pc2 / COL_WIDTH) * 4) + i, P1_COLOR.getRGB());
					}
					repaint();
					revalidate();
				}
				
				if (pc3 != -1) {
					for (int i = 0; i < 4; i++) {
						mainImage.setRGB(pc3 % COL_WIDTH, ((pc3 / COL_WIDTH) * 4) + i, P1_COLOR.getRGB());
					}
					repaint();
					revalidate();
				}
				
				if (pc4 != -1) {
					for (int i = 0; i < 4; i++) {
						mainImage.setRGB(pc4 % COL_WIDTH, ((pc4 / COL_WIDTH) * 4) + i, P1_COLOR.getRGB());
					}
					repaint();
					revalidate();
				}
				
				break;
				
			case 2:
				if (pc1 != -1) {
					for (int i = 0; i < 4; i++) {
						mainImage.setRGB(pc1 % COL_WIDTH, ((pc1 / COL_WIDTH) * 4) + i, P2_COLOR.getRGB());
					}
					repaint();
					revalidate();
					
				}
				
				if (pc2 != -1) {
					for (int i = 0; i < 4; i++) {
						mainImage.setRGB(pc2 % COL_WIDTH, ((pc2 / COL_WIDTH) * 4) + i, P2_COLOR.getRGB());
					}
					repaint();
					revalidate();
				}
				
				if (pc3 != -1) {
					for (int i = 0; i < 4; i++) {
						mainImage.setRGB(pc3 % COL_WIDTH, ((pc3 / COL_WIDTH) * 4) + i, P2_COLOR.getRGB());
					}
					repaint();
					revalidate();
				}
				
				if (pc4 != -1) {
					for (int i = 0; i < 4; i++) {
						mainImage.setRGB(pc4 % COL_WIDTH, ((pc4 / COL_WIDTH) * 4) + i, P2_COLOR.getRGB());
					}
					repaint();
					revalidate();
				}
				
				break;
			
			case 3:
				if (pc1 != -1) {
					for (int i = 0; i < 4; i++) {
						mainImage.setRGB(pc1 % COL_WIDTH, ((pc1 / COL_WIDTH) * 4) + i, P3_COLOR.getRGB());
					}
					repaint();
					revalidate();
				}
				
				if (pc2 != -1) {
					for (int i = 0; i < 4; i++) {
						mainImage.setRGB(pc2 % COL_WIDTH, ((pc2 / COL_WIDTH) * 4) + i, P3_COLOR.getRGB());
					}
					repaint();
					revalidate();
				}
				
				if (pc3 != -1) {
					for (int i = 0; i < 4; i++) {
						mainImage.setRGB(pc3 % COL_WIDTH, ((pc3 / COL_WIDTH) * 4) + i, P3_COLOR.getRGB());
					}
					repaint();
					revalidate();
				}
				
				if (pc4 != -1) {
					for (int i = 0; i < 4; i++) {
						mainImage.setRGB(pc4 % COL_WIDTH, ((pc4 / COL_WIDTH) * 4) + i, P3_COLOR.getRGB());
					}
					repaint();
					revalidate();
				}
				
				break;
			
			case 4:
				if (pc1 != -1) {
					for (int i = 0; i < 4; i++) {
						mainImage.setRGB(pc1 % COL_WIDTH, ((pc1 / COL_WIDTH) * 4) + i, P4_COLOR.getRGB());
					}
					repaint();
					revalidate();
				}
				
				if (pc2 != -1) {
					for (int i = 0; i < 4; i++) {
						mainImage.setRGB(pc2 % COL_WIDTH, ((pc2 / COL_WIDTH) * 4) + i, P4_COLOR.getRGB());
					}
					repaint();
					revalidate();
				}
				
				if (pc3 != -1) {
					for (int i = 0; i < 4; i++) {
						mainImage.setRGB(pc3 % COL_WIDTH, ((pc3 / COL_WIDTH) * 4) + i, P4_COLOR.getRGB());
					}
					repaint();
					revalidate();
				}
				
				if (pc4 != -1) {
					for (int i = 0; i < 4; i++) {
						mainImage.setRGB(pc4 % COL_WIDTH, ((pc4 / COL_WIDTH) * 4) + i, P4_COLOR.getRGB());
					}
					repaint();
					revalidate();
				}
				
				break;
		}
		
	}
	
	
}