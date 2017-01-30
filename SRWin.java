/**
 * Copyright © 2009-2017, Universite catholique de Louvain
 * All rights reserved.
 *
 * Copyright © 2017 Forschungszentrum Juelich GmbH
 * All rights reserved.
 *
 *  @author Xavier Draye
 *  @author Guillaume Lobet
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted 
 * under the GNU General Public License v3 and provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions 
 * and the following disclaimer in the documentation and/or other materials provided with the distribution.
 * 
 * 3. Neither the name of the copyright holder nor the names of its contributors may be used to endorse or 
 * promote products derived from this software without specific prior written permission.
 *
 * Disclaimer
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, 
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. 
 * IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, 
 * OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; 
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT 
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * You should have received the GNU GENERAL PUBLIC LICENSE v3 with this file in 
 * license.txt but can also be found at http://www.gnu.org/licenses/gpl-3.0.en.html
 *
 * NOTE: The GPL.v3 license requires that all derivative work is distributed under the same license. 
 * That means that if you use this source code in any other program, you can only distribute that 
 * program with the full source code included and licensed under a GPL license.
 * 
 */



/**
 * 
 * This class handles the SmartRoot windows, with the different options, exports, plotting...
 */

import javax.swing.*;

import java.awt.*;
import java.util.List;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.*;

import ij.*;
import ij.io.FileInfo;

import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.table.*;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
//import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
//import javax.swing.tree.DefaultTreeModel;
//import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import javax.swing.ImageIcon;


//import java.text.SimpleDateFormat;
import java.util.*;
import java.io.*;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.StandardXYBarPainter;
import org.jfree.chart.renderer.xy.XYBarRenderer;
import org.jfree.data.xy.IntervalXYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.data.statistics.HistogramDataset;
import org.jfree.data.statistics.HistogramType;

//import unused.RLDGridSize;
//import unused.RLDProfile2D;
//import unused.RLDSettingsDialog;

public class SRWin extends JFrame implements ItemListener, ActionListener {

	private static final long serialVersionUID = 1L;
	private static SRWin instance = null;
	
	private JCheckBox showAxis, showNodes, showBorders, showArea, showTicks, showMarks, showTicksP, insAngHist, diamHist, 
   		diamPHist, diamSHist, diamAllHist, interHist, lengthChart, rePlot, absVal, interChart, angChart,
   		sqlCreate, checkNSizeBox, checkRDirBox, checkRSizeBox, dpiBox, cmBox, autoThresholdBox, IJThresholdBox, 
   		askNameBox, sqlExport, csvExport, csvHeader, imageExport, batchExport, batchSQL, batchCSV, batchImage, splitRootImage,
   		createBatchSQL, batchDPI, imageRealWidth, batchRealWidth , autoFindLat, globalConvexHullBox, displayConvexHull, useBinary;//persoDB
	
	private JTextField sqlTableName, plotTextField1, plotTextField2, plotTextField3, plotTextField4, 
		plotTextField5, plotTextField6, nStepField, minNSizeField, maxNSizeField, minRSizeField, maxAngleField, 
		DPIValue, cmValue, pixValue, rootName1, rootName2, sqlDriverField, sqlUrlField, sqlUserField, sqlPwField,
		csvFolderName, imageFolderName, lineWidth, batchSourceFolder, batchImageFolder, batchCSVFile, batchSQLTable, batchLineWidth, batchDPIValue;
	
	private JComboBox sqlJCB, csvJCB, action, plotComboBox1, plotComboBox2, unitsList, imageJCB, colorJCB, batchJCB, 
		batchColorJCB, batchImageJCB, poListCombo;
	
	private JButton savePrefs, ok, cancel, plot, rPlot, cPlot, savePrefsButton, defaultButton, getLineButton, 
		dpiApplyButton, dpiDefaultButton, namesApplyButton, dpiHelpButton, nameHelpButton, thresholdHelpButton, lateralHelpButton, applyActionList,
		sqlSavePrefsButton, sqlDefaultButton, sqlRestartServerButton, sqlHelpButton, csvChooseFolder, imageChooseFolder, 
		batchSourceButton, batchImageButton, batchCSVButton, batchButton, transfersButton, applyNewPO;
	
	private JTable linkedDatafileTable, marksTable;// rootTable;
	
	private JTabbedPane tp;
	
	private JLabel sep, maxAngleLabel, nStepLabel, minNSizeLabel, maxNSizeLabel, minRSizeLabel, setDPI, setPixel, rootNameLabel1, rootNameLabel2, 
		transfersLabel1, transfersLabel2, transfersLabel4, transfersLabel5, transfersLabel6, transfersLabel7, batchLabel1, batchLabel2, batchLabel3;
	
	private JSplitPane splitPane, splitPane2;// splitPane3;
	private JEditorPane infoPane; 
	private JTextPane aboutPane;
	private JScrollPane infoView, aboutView, layersView;

	private RootModel rm;
	public RootListTree rootListTree;
//	private SummaryTableModel summaryTableModel;
	private LinkedDatafileTableModel linkedDatafileModel;
	private MarksTableModel marksTableModel;
	private Histogram hist1, hist2, hist3, hist4, hist5, hist6;
	private Chart g1, g2, g3;
   
   	Font font = new Font("Dialog", Font.PLAIN, 12);
	
	private boolean b1, b2, b3, b4, b5, b6, b7, b8, b9, reP, absV;
	
	private SQLServer sqlServ;
	
	////
	
	// Get user parameters
	static int nStep = SR.prefs.getInt("nStep", FCSettings.nStep);	
	static float dMin = SR.prefs.getFloat("dMin",FCSettings.dMin);
	static float dMax = SR.prefs.getFloat("nMax",FCSettings.dMax);
	static float maxAngle = SR.prefs.getFloat("maxAngle",FCSettings.maxAngle);
	static int nIt = SR.prefs.getInt("nItField",FCSettings.nIt);	
	static boolean autoFind = SR.prefs.getBoolean("autoFind",FCSettings.autoFind);
	static boolean globalConvex = SR.prefs.getBoolean("globalConvex",FCSettings.globalConvex);
	static boolean checkNSize = SR.prefs.getBoolean("checkNSize",FCSettings.checkNSize);
	static boolean checkRDir = SR.prefs.getBoolean("checkRDir",FCSettings.checkRDir);
	static boolean checkRSize = SR.prefs.getBoolean("checkRSize",FCSettings.checkRSize);	
	static boolean useBinaryImg = SR.prefs.getBoolean("useBinary", FCSettings.useBinaryImg);
	static boolean doubleDir = SR.prefs.getBoolean("doubleDir",FCSettings.doubleDir);	
	static double minNodeSize = SR.prefs.getDouble("minNSize",FCSettings.minNodeSize);
	static double maxNodeSize = SR.prefs.getDouble("maxNSize",FCSettings.maxNodeSize);	
	static double minRootSize = SR.prefs.getDouble("minRSize",FCSettings.minRootSize);
	static double minRootDistance = SR.prefs.getDouble("minRootDistance", FCSettings.minRootDistance);
	static float DPI = 0; 
	static int previousUnit = 0;
   
	/**
	 * This class draw the SmartRoot main window and all its different tabs 
	 * (Layer / Linked datafile / SQL / summary / plot / root list)
	 */
	public SRWin() {
		
      super("SmartRoot");
      instance = this;
      tp = new JTabbedPane();
      tp.setFont(font);
      tp.setSize(600, 600);
      getContentPane().add(tp);
      
      tp.addTab("Layers", getLayerTab());
      tp.addTab("Root List", getRootListTab());
      tp.addTab("Linked files", getLinkedTab());
      tp.addTab("Data transfer", getDataTransfersTab());      
      tp.addTab("Plot", getPlotTab());
      tp.addTab("Settings", getSettingsTab());
      tp.addTab("About", getAboutTab());
      
      ///////////////////
      
      setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
      setIconImage(IJ.getInstance().getIconImage());

      pack();
      int xLoc = SR.prefs.getInt("SR_Win.Location.X", 0);
      int yLoc = SR.prefs.getInt("SR_Win.Location.Y", 0);
      int width = SR.prefs.getInt("SR_Win.Location.Width", 0);
      int height = SR.prefs.getInt("SR_Win.Location.Height", 0);
      if (xLoc < 0) xLoc = 0;
      if (yLoc < 0) yLoc = 0;
      if (width > 0 && height > 0) setBounds(xLoc, yLoc, width, height);
      else setLocation(xLoc, yLoc); 
      setVisible(true);
      }

   /**
    * Get the layer tab
    * @return
    */
   private JPanel getLayerTab(){
	   
      showAxis = new JCheckBox("Display Axis");
      showAxis.setFont(font);
      showAxis.addItemListener(this);
      showAxis.setMnemonic(KeyEvent.VK_A);
      
      showNodes = new JCheckBox("Display Nodes");
      showNodes.setFont(font);
      showNodes.addItemListener(this);
      showNodes.setMnemonic(KeyEvent.VK_N);

      showBorders = new JCheckBox("Display Borders");
      showBorders.setFont(font);
      showBorders.addItemListener(this);
      showBorders.setMnemonic(KeyEvent.VK_B);
      
      showArea = new JCheckBox("Display Area");
      showArea.setFont(font);
      showArea.addItemListener(this);
      showArea.setMnemonic(KeyEvent.VK_R);

      showTicks = new JCheckBox("Display Ticks");
      showTicks.setFont(font);
      showTicks.addItemListener(this);
      showTicks.setMnemonic(KeyEvent.VK_T);

      showTicksP = new JCheckBox("Display Ticks on Primary only");
      showTicksP.setFont(font);
      showTicksP.addItemListener(this);
      showTicksP.setMnemonic(KeyEvent.VK_T);
      
      showMarks = new JCheckBox("Display Marks");
      showMarks.setFont(font);
      showMarks.addItemListener(this);
      showMarks.setMnemonic(KeyEvent.VK_M);
      
      displayConvexHull = new JCheckBox("Display convexhull");
      displayConvexHull.setFont(font);
      displayConvexHull.addItemListener(this);


      sep = new JLabel("----------------");
      JLabel sep1 = new JLabel("----------------");
      
      savePrefs = new JButton("Save in Prefs");
      savePrefs.setFont(font);
      savePrefs.setActionCommand("SAVE_PREFS");
      savePrefs.addActionListener(this);
      savePrefs.setMnemonic(KeyEvent.VK_S);

      JPanel p2 = new JPanel();
      p2.setLayout(new BoxLayout(p2, BoxLayout.Y_AXIS));
      p2.add(showAxis);
      p2.add(showNodes);
      p2.add(showBorders);
      p2.add(showArea);
      p2.add(showTicks);
      p2.add(showMarks);
      p2.add(sep);
      p2.add(showTicksP);
      p2.add(sep1);
      p2.add(displayConvexHull);
      
      layersView = new JScrollPane(p2);
      layersView.setBorder(BorderFactory.createLineBorder(Color.gray));

      JPanel p3 = new JPanel();
      p3.setLayout(new BoxLayout(p3, BoxLayout.X_AXIS));
      p3.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 0));
      p3.add(Box.createHorizontalGlue());
      p3.add(savePrefs);
      
      ImageIcon icon = new ImageIcon(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/images/SmartRoot_logo.gif")));
      JLabel logo = new JLabel("",icon, JLabel.CENTER);
      logo.setPreferredSize(new Dimension(400, 53));
      
      JPanel p1 = new JPanel(new BorderLayout());
      p1.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
      p1.add(logo, BorderLayout.NORTH);
      p1.add(layersView, BorderLayout.CENTER);
      p1.add(p3, BorderLayout.SOUTH);
      return p1;	   
   }
   
   /**
    * Get the root list tab
    * @return
    */
   private JPanel getRootListTab(){
	      
      rootListTree = new RootListTree();

      
      infoPane = new JEditorPane();
      infoPane.setEditable(false);
      infoPane.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
      infoView = new JScrollPane(infoPane);
      infoPane.setText("Please select a root");

//      // Root properties panel
//      marksTableModel = new RootTableModel();
//      rootTable = new JTable(marksTableModel);
//      rootTable.setAutoCreateColumnsFromModel(true);
//      rootTable.setColumnSelectionAllowed(false);
//      rootTable.setRowSelectionAllowed(true);
//      rootTable.setAutoCreateRowSorter(true);
//      rootTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
//      rootTable.setFont(font);      
      
      applyNewPO = new JButton("Set root ontology");
      applyNewPO.setFont(font);
      applyNewPO.setActionCommand("APPLY_PO");
      applyNewPO.addActionListener(this);
      
      poListCombo = new JComboBox(SR.listPoNames);
      poListCombo.setSelectedIndex(0); 
      
      JPanel ppo = new JPanel(new BorderLayout());
      ppo.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 0));
      ppo.add(poListCombo, BorderLayout.WEST);
      ppo.add(applyNewPO, BorderLayout.EAST);
      
      JPanel ppo2 = new JPanel(new BorderLayout());
      ppo2.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 0));
      ppo2.add(ppo, BorderLayout.NORTH);
      ppo2.add(infoView, BorderLayout.CENTER);
      
      
//      splitPane3 = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
//      splitPane3.setTopComponent(ppo);
//      splitPane3.setBottomComponent(infoView);
//      splitPane3.setDividerLocation(30); 

      splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
      splitPane.setLeftComponent(rootListTree);
      splitPane.setRightComponent(ppo2);

      Dimension minimumSize = new Dimension(100, 50);
      infoView.setMinimumSize(minimumSize);
      splitPane.setDividerLocation(200); 
      splitPane.setPreferredSize(new Dimension(500, 300));
      
      String[] list = {"Delete root(s)", "Delete mark(s)", "Delete all marks", "Rename root", "Attach parent", "Detach parent", "Detach child(ren)", "Find laterals"};
      action = new JComboBox(list);
      action.setSelectedIndex(3);      
      
      applyActionList = new JButton("Apply");
      applyActionList.setFont(font);
      applyActionList.setActionCommand("ACTION");
      applyActionList.addActionListener(this);
	  
	  JButton ref = new JButton("Refresh");
	  ref.setFont(font);
	  ref.setActionCommand("ROOT_REFRESH");
	  ref.addActionListener(this);
	  
	  ok = new JButton("OK");
	  ok.setFont(font);
	  ok.setActionCommand("OK_BUTTON");
	  ok.addActionListener(this);
	  ok.setEnabled(false);
	  
	  cancel = new JButton("Cancel");
	  cancel.setFont(font);
	  cancel.setActionCommand("CANCEL_BUTTON");
	  cancel.addActionListener(this);
	  cancel.setEnabled(false);
	  
	  // Marks list panel
      marksTableModel = new MarksTableModel();
      marksTable = new JTable(marksTableModel);
      marksTable.setAutoCreateColumnsFromModel(true);
      marksTable.setColumnSelectionAllowed(false);
      marksTable.setRowSelectionAllowed(true);
      marksTable.setAutoCreateRowSorter(true);
      marksTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
      marksTable.setFont(font);
    
      JScrollPane marksPane = new JScrollPane(marksTable);
	  
      splitPane2 = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
      splitPane2.setTopComponent(splitPane);
      splitPane2.setBottomComponent(marksPane);
      splitPane2.setDividerLocation(500); 
      
	  
      JPanel p2 = new JPanel();
      p2.setLayout(new BoxLayout(p2,BoxLayout.X_AXIS));
      p2.add(ref);
      
      JPanel p3 = new JPanel();
      p3.setLayout(new BoxLayout(p3,BoxLayout.X_AXIS));
      p3.add(action);
      p3.add(applyActionList);
      p3.add(cancel);
      p3.add(ok);
      
      JPanel p4 = new JPanel(new BorderLayout());
      p4.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 0));
      p4.add(p2, BorderLayout.EAST);
      p4.add(p3, BorderLayout.WEST);
            
      JPanel p1 = new JPanel(new BorderLayout());
      p1.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
      p1.add(splitPane2, BorderLayout.CENTER);
      p1.add(p4, BorderLayout.SOUTH);  
      
      return p1;
   }
   
   /**
    * get the Data transfers tab
    * @return
    */
   private JScrollPane getDataTransfersTab(){
	   
	      transfersLabel1 = new JLabel("SmartRoot Dataset:"); transfersLabel1.setFont(font);
	      transfersLabel2 = new JLabel("Table name in DB:"); transfersLabel2.setFont(font); transfersLabel2.setEnabled(true);
	      transfersLabel4 = new JLabel("Save CSV:"); transfersLabel4.setFont(font); transfersLabel4.setEnabled(false);
	      transfersLabel5 = new JLabel("Save image:"); transfersLabel5.setFont(font); transfersLabel5.setEnabled(false);
	      transfersLabel6 = new JLabel("Image type:"); transfersLabel6.setFont(font); transfersLabel6.setEnabled(false);
	      transfersLabel7 = new JLabel("Line width:"); transfersLabel7.setFont(font); transfersLabel7.setEnabled(false);
	      
//	      String[] sqlTables = {"Global Root Data", "All Marks", "Root Nodes", "Root Length Density", "Growth rate", "Lab export"};
	      String[] sqlTables = {"Global Root Data", "All Marks", "Root Nodes", "Growth rate"};
	      sqlJCB = new JComboBox(sqlTables);
	      sqlJCB.setFont(font);
	      sqlJCB.setSelectedIndex(0);
	      sqlJCB.setAlignmentX(Component.LEFT_ALIGNMENT);
	      
	      String[] csvTables = {"Global Root Data", "All Marks", "Root Nodes", "Coordinates", "Growth rate", "Lab export"};
//	      String[] csvTables = {"Global Root Data", "All Marks", "Root Nodes", "Growth rate"};
	      csvJCB = new JComboBox(csvTables);
	      csvJCB.setFont(font);
	      csvJCB.setSelectedIndex(0);
	      csvJCB.setAlignmentX(Component.LEFT_ALIGNMENT);
	      csvJCB.setEnabled(false);
	      
	      String[] type = {"png", "bmp", "jpg", "tif"};
	      imageJCB = new JComboBox(type);
	      imageJCB.setSelectedItem(0);   
	      imageJCB.addActionListener(this);
	      imageJCB.setActionCommand("IMAGE_TYPE");
	      imageJCB.setEnabled(false);
	      
	      String[] color = {"Color image", "Black and white"};
	      colorJCB = new JComboBox(color);
	      colorJCB.setSelectedItem(0);   
	      colorJCB.setEnabled(false);

	      lineWidth = new JTextField("1", 2);
	      lineWidth.setFont(font);
	      lineWidth.setAlignmentX(Component.LEFT_ALIGNMENT);
	      lineWidth.setEnabled(false);
	      
	      sqlTableName = new JTextField("[Database table name]", 24);
	      sqlTableName.setFont(font);
	      sqlTableName.setAlignmentX(Component.LEFT_ALIGNMENT);
	      
	      csvFolderName = new JTextField("[Choose folder]", 20);
	      csvFolderName.setFont(font);
	      csvFolderName.setAlignmentX(Component.LEFT_ALIGNMENT);
	      csvFolderName.setEnabled(false);
	      
	      imageFolderName = new JTextField("[Choose folder]", 16);
	      imageFolderName.setFont(font);
	      imageFolderName.setAlignmentX(Component.LEFT_ALIGNMENT);
	      imageFolderName.setEnabled(false);

	      sqlCreate = new JCheckBox("Create table", true);
	      sqlCreate.setFont(font);
	      sqlCreate.setAlignmentX(Component.LEFT_ALIGNMENT);
	      
	      sqlExport = new JCheckBox("Send to SQL database", true);
	      sqlExport.setFont(font);
	      sqlExport.setAlignmentX(Component.LEFT_ALIGNMENT);
	      sqlExport.addItemListener(this);
	      
	      csvExport = new JCheckBox("Send to CSV file", false);
	      csvExport.setFont(font);	   
	      csvExport.setAlignmentX(Component.LEFT_ALIGNMENT);
	      csvExport.addItemListener(this);
	      
	      imageExport = new JCheckBox("Send tracing to image file", false);
	      imageExport.setFont(font);
	      imageExport.setAlignmentX(Component.LEFT_ALIGNMENT);
	      imageExport.addItemListener(this);
	      
	      imageRealWidth = new JCheckBox("Real width", false);
	      imageRealWidth.setFont(font);
	      imageRealWidth.setAlignmentX(Component.LEFT_ALIGNMENT);
	      imageRealWidth.setEnabled(false);	 
	      
	      batchRealWidth = new JCheckBox("Real width", false);
	      batchRealWidth.setFont(font);
	      batchRealWidth.setAlignmentX(Component.LEFT_ALIGNMENT);
	      batchRealWidth.addItemListener(this);	 	  
	      
	      splitRootImage = new JCheckBox("One primary / image", false);
	      splitRootImage.setFont(font);
	      splitRootImage.setAlignmentX(Component.LEFT_ALIGNMENT);
	      splitRootImage.addItemListener(this);	 	  
	      splitRootImage.setEnabled(false);
	      
	      csvHeader = new JCheckBox("Print headers", true);
	      csvHeader.setFont(font);
	      csvHeader.setAlignmentX(Component.LEFT_ALIGNMENT);
	      csvHeader.setEnabled(false);
	      
	      transfersButton = new JButton("Transfer tracing data");
	      transfersButton.setFont(font);
	      transfersButton.setActionCommand("SQL_TRANSFER");
	      transfersButton.addActionListener(this);
	      
	      csvChooseFolder = new JButton("Choose");
	      csvChooseFolder.setFont(font);
	      csvChooseFolder.setActionCommand("CSV_FOLDER");
	      csvChooseFolder.addActionListener(this);
	      csvChooseFolder.setEnabled(false);
	      
	      imageChooseFolder = new JButton("Choose");
	      imageChooseFolder.setFont(font);
	      imageChooseFolder.setActionCommand("IMAGE_FOLDER");
	      imageChooseFolder.addActionListener(this);
	      imageChooseFolder.setEnabled(false);
	      
	      GridBagLayout trsfGb = new GridBagLayout();

	    
	      //--------------------------

	      // sql subpanel
	      JPanel trsfPanel = new JPanel();
	      trsfPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
	      GridBagConstraints trsfc1 = new GridBagConstraints();
	      trsfc1.anchor = GridBagConstraints.WEST;
	     trsfPanel.setLayout(trsfGb);
	       
	      trsfc1.gridx = 0;
	      trsfc1.gridy = 1;
	      trsfPanel.add(transfersLabel2, trsfc1);
	      trsfc1.gridx = 1;
	      trsfPanel.add(sqlTableName, trsfc1);
	      trsfc1.gridx = 0;
	      trsfc1.gridy = 2;
	      trsfPanel.add(sqlCreate, trsfc1);
	      
	      JPanel trsfPanel2 = new JPanel(new BorderLayout());
	      trsfPanel2.setBorder(BorderFactory.createLineBorder(Color.gray));
	      trsfPanel2.add(trsfPanel, BorderLayout.WEST);
	      
	      JPanel trsfPanelHead1 = new JPanel(new BorderLayout());
	      trsfPanelHead1.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
	      trsfPanelHead1.add(sqlExport, BorderLayout.WEST);
	      trsfPanelHead1.add(sqlJCB, BorderLayout.EAST);
	      
	      JPanel trsfPanel3 = new JPanel(new BorderLayout());
	      trsfPanel3.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
	      trsfPanel3.add(trsfPanelHead1, BorderLayout.NORTH);
	      trsfPanel3.add(trsfPanel2, BorderLayout.SOUTH);
	    
	      //--------------------------
	      
	      // csv subpanel
	      JPanel csvPanel = new JPanel();
	      csvPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
	      GridBagConstraints csv1 = new GridBagConstraints();
	      csv1.anchor = GridBagConstraints.WEST;
	      csvPanel.setLayout(trsfGb);
	      
	      csv1.gridx = 0;
	      csv1.gridy = 1;
	      csvPanel.add(transfersLabel4, csv1);
	      csv1.gridx = 1;
	      csvPanel.add(csvFolderName, csv1);
	      csv1.gridx = 2;
	      csvPanel.add(csvChooseFolder, csv1);
	      csv1.gridx = 0;
	      csv1.gridy = 2;
	      csvPanel.add(csvHeader, csv1);
	      
	      JPanel csvPanel2 = new JPanel(new BorderLayout());
	      csvPanel2.setBorder(BorderFactory.createLineBorder(Color.gray));
	      csvPanel2.add(csvPanel, BorderLayout.WEST);
	      
	      JPanel trsfPanelHead2 = new JPanel(new BorderLayout());
	      trsfPanelHead2.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
	      trsfPanelHead2.add(csvExport, BorderLayout.WEST);
	      trsfPanelHead2.add(csvJCB, BorderLayout.EAST);
	      
	      JPanel csvPanel3 = new JPanel(new BorderLayout());
	      csvPanel3.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
	      csvPanel3.add(trsfPanelHead2, BorderLayout.NORTH);
	      csvPanel3.add(csvPanel2, BorderLayout.SOUTH);
	      
	      //--------------------------

	      // Image subpanel
	      
	      JPanel imagePanel = new JPanel();
	      imagePanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
	      GridBagConstraints image1 = new GridBagConstraints();
	      image1.anchor = GridBagConstraints.WEST;
		  imagePanel.setLayout(trsfGb);
	      
	      image1.gridx = 0;
	      image1.gridy = 0;
	      imagePanel.add(imageExport, image1);
	      image1.gridx = 0;
	      image1.gridy = 1;
	      imagePanel.add(transfersLabel6, image1);
	      image1.gridx = 1;
	      imagePanel.add(colorJCB, image1);
	      image1.gridx = 2;
	      imagePanel.add(imageJCB, image1);
	      image1.gridx = 3;
	      imagePanel.add(transfersLabel7, image1);
	      image1.gridx = 4;
	      imagePanel.add(lineWidth, image1);
	      image1.gridx = 0;
	      image1.gridy = 2;
	      imagePanel.add(transfersLabel5, image1);
	      image1.gridx = 1;
	      imagePanel.add(imageFolderName, image1);
	      image1.gridx = 2;
	      imagePanel.add(imageChooseFolder, image1);
	      image1.gridx = 3;
	      imagePanel.add(imageRealWidth, image1);
	      
	      JPanel imagePanel2 = new JPanel(new BorderLayout());
	      imagePanel2.setBorder(BorderFactory.createLineBorder(Color.gray));
	      imagePanel2.add(imagePanel, BorderLayout.WEST);      
	      imagePanel2.setEnabled(false);
	      
	      JPanel imagePanel3 = new JPanel(new BorderLayout());
	      imagePanel3.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
	      imagePanel3.add(imageExport, BorderLayout.NORTH);
	      imagePanel3.add(imagePanel2, BorderLayout.SOUTH);
	      
	      //--------------------------  

	      JPanel expPanel = new JPanel(new BorderLayout());
	      expPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
	      expPanel.add(trsfPanel3, BorderLayout.NORTH);
	      expPanel.add(csvPanel3, BorderLayout.CENTER);
	      expPanel.add(imagePanel3, BorderLayout.SOUTH);
	      
	      
	      JPanel expPanel1 = new JPanel(new BorderLayout());
	      expPanel1.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
	      expPanel1.add(expPanel, BorderLayout.NORTH);           
	  
	      // button panel
	      
	      JPanel trsfPanel4 = new JPanel(new BorderLayout());
	      trsfPanel4.setBorder(BorderFactory.createEmptyBorder(0, 20, 5, 20));
	      trsfPanel4.add(transfersButton, BorderLayout.EAST); 
	      
	      JPanel trsfPanel6 = new JPanel(new BorderLayout());
	      trsfPanel6.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
	      trsfPanel6.add(expPanel1, BorderLayout.NORTH);
	      trsfPanel6.add(trsfPanel4, BorderLayout.SOUTH);

	      
	      //-----------------------
	      
	      // Batch export
	      
	      
	      batchLabel1 = new JLabel("Choose source folder:"); batchLabel1.setFont(font); batchLabel1.setEnabled(false);
	      batchLabel2 = new JLabel("Save image folder:"); batchLabel2.setFont(font); batchLabel2.setEnabled(false);
	      batchLabel3 = new JLabel("Save csv file:"); batchLabel3.setFont(font); batchLabel3.setEnabled(false);
	      
	      batchExport = new JCheckBox("Batch export", false);
	      batchExport.setFont(font);
	      batchExport.setAlignmentX(Component.LEFT_ALIGNMENT);
	      batchExport.addItemListener(this); 
	      
//	      persoDB = new JCheckBox("Perso", true);
//	      persoDB.setFont(font);
//	      persoDB.setEnabled(false);
	      
	      createBatchSQL = new JCheckBox("Create", true);
	      createBatchSQL.setFont(font);
	      createBatchSQL.setEnabled(false);
	      
	      batchImageJCB = new JComboBox(type);
	      batchImageJCB.setSelectedItem(0);   
	      batchImageJCB.addActionListener(this);
	      batchImageJCB.setActionCommand("IMAGE_TYPE_BATCH");
	      batchImageJCB.setEnabled(false);
	      
	      batchColorJCB = new JComboBox(color);
	      batchColorJCB.setSelectedItem(0);   
	      batchColorJCB.setEnabled(false);
	      
	      String[] batchTables = {"Global Root Data", "All Marks", "Root Nodes", "Growth rate", "Lab export"};
//	      String[] batchTables = {"Global Root Data", "All Marks", "Root Nodes", "Growth rate"};
	      batchJCB = new JComboBox(batchTables);
	      batchJCB.setFont(font);
	      batchJCB.setSelectedIndex(0);
	      batchJCB.setAlignmentX(Component.LEFT_ALIGNMENT);
	      batchJCB.setEnabled(false);
	      batchJCB.addItemListener(this); 

	      batchLineWidth = new JTextField("1", 5);
	      batchLineWidth.setFont(font);
	      batchLineWidth.setAlignmentX(Component.LEFT_ALIGNMENT);
	      batchLineWidth.setEnabled(false);
	      
	      batchSourceFolder = new JTextField("[Choose folder]", 15);
//	      batchSourceFolder = new JTextField("/Users/guillaumelobet/Documents/Agro/Mars-project/EXP20_renamed/xml_files", 15);
	      batchSourceFolder.setFont(font);
	      batchSourceFolder.setAlignmentX(Component.LEFT_ALIGNMENT);
	      batchSourceFolder.setEnabled(false);
	      
	      batchSQLTable = new JTextField("[Table name]", 15);
//	      batchSQLTable = new JTextField("root_architecture_laura", 15);
	      batchSQLTable.setFont(font);
	      batchSQLTable.setAlignmentX(Component.LEFT_ALIGNMENT);
	      batchSQLTable.setEnabled(false);
	      
	      batchCSVFile = new JTextField("[Choose file]", 15);
	      batchCSVFile.setFont(font);
	      batchCSVFile.setAlignmentX(Component.LEFT_ALIGNMENT);
	      batchCSVFile.setEnabled(false);
	      
	      batchImageFolder = new JTextField("[Choose folder]", 15);
	      batchImageFolder.setFont(font);
	      batchImageFolder.setAlignmentX(Component.LEFT_ALIGNMENT);
	      batchImageFolder.setEnabled(false);
	      
	      batchDPIValue = new JTextField("300", 5);
	      batchDPIValue.setFont(font);
	      batchDPIValue.setAlignmentX(Component.LEFT_ALIGNMENT);
	      batchDPIValue.setEnabled(false);
	      
	      batchSQL = new JCheckBox("SQL export", true);
	      batchSQL.setFont(font);
	      batchSQL.setAlignmentX(Component.LEFT_ALIGNMENT);
	      batchSQL.setEnabled(false);
	      batchSQL.addItemListener(this);
	      
	      batchCSV = new JCheckBox("CSV export", false);
	      batchCSV.setFont(font);
	      batchCSV.setAlignmentX(Component.LEFT_ALIGNMENT);
	      batchCSV.setEnabled(false);
	      batchCSV.addItemListener(this);
	      
	      batchDPI = new JCheckBox("Global DPI", false);
	      batchDPI.setFont(font);
	      batchDPI.setAlignmentX(Component.LEFT_ALIGNMENT);
	      batchDPI.setEnabled(false);
	      batchDPI.addItemListener(this);	      
	      
	      batchImage = new JCheckBox("Image export", false);
	      batchImage.setFont(font);
	      batchImage.setAlignmentX(Component.LEFT_ALIGNMENT);
	      batchImage.setEnabled(false);
	      batchImage.addItemListener(this);
	      
	      batchButton = new JButton("Run batch export");
	      batchButton.setFont(font);
	      batchButton.setActionCommand("BATCH_EXPORT");
	      batchButton.addActionListener(this);
	      batchButton.setEnabled(false);
	      
	      batchSourceButton = new JButton("Choose");
	      batchSourceButton.setFont(font);
	      batchSourceButton.setActionCommand("BATCH_SOURCE_FOLDER");
	      batchSourceButton.addActionListener(this);
	      batchSourceButton.setEnabled(false);
	      
	      batchImageButton = new JButton("Choose");
	      batchImageButton.setFont(font);
	      batchImageButton.setActionCommand("BATCH_IMAGE_FOLDER");
	      batchImageButton.addActionListener(this);
	      batchImageButton.setEnabled(false);
	      
	      batchCSVButton = new JButton("Choose");
	      batchCSVButton.setFont(font);
	      batchCSVButton.setActionCommand("BATCH_CSV_FILE");
	      batchCSVButton.addActionListener(this);
	      batchCSVButton.setEnabled(false);
	      
	      JPanel batchPanel = new JPanel();
	      batchPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
	      GridBagConstraints trsfc3 = new GridBagConstraints();
	      trsfc3.anchor = GridBagConstraints.WEST;
	      batchPanel.setLayout(trsfGb);
	      
	      trsfc3.gridy = 1;
	      trsfc3.gridx = 0;
	      batchPanel.add(batchLabel1, trsfc3);
	      trsfc3.gridx = 1;
	      batchPanel.add(batchSourceFolder, trsfc3);
	      trsfc3.gridx = 2;
	      batchPanel.add(batchSourceButton, trsfc3);
	      trsfc3.gridx = 3;
	      batchPanel.add(batchDPI, trsfc3);	   
	      trsfc3.gridx = 4;
	      batchPanel.add(batchDPIValue, trsfc3);	   
	      
	      trsfc3.gridy = 2;
	      trsfc3.gridx = 0;
	      batchPanel.add(batchSQL, trsfc3);
	      trsfc3.gridx = 1;
	      batchPanel.add(batchSQLTable, trsfc3);
	      trsfc3.gridx = 2;
//	      batchPanel.add(persoDB, trsfc3);
//	      trsfc3.gridx = 3;
	      batchPanel.add(createBatchSQL, trsfc3);
	      
	      trsfc3.gridy = 3;
	      trsfc3.gridx = 0;
	      batchPanel.add(batchCSV, trsfc3);
	      trsfc3.gridx = 1;
	      batchPanel.add(batchCSVFile, trsfc3);
	      trsfc3.gridx = 2;
	      batchPanel.add(batchCSVButton, trsfc3);
	      
	      trsfc3.gridy = 4;
	      trsfc3.gridx = 0;
	      batchPanel.add(batchImage, trsfc3);
	      trsfc3.gridx = 1;
	      batchPanel.add(batchImageFolder, trsfc3);
	      trsfc3.gridx = 2;
	      batchPanel.add(batchImageButton, trsfc3);
	      trsfc3.gridx = 3;
	      batchPanel.add(batchRealWidth, trsfc3);
	      
	      trsfc3.gridy = 5;
	      trsfc3.gridx = 1;
	      batchPanel.add(batchColorJCB, trsfc3);
	      trsfc3.gridx = 2;
	      batchPanel.add(batchImageJCB, trsfc3);
	      trsfc3.gridx = 3;
	      batchPanel.add(batchLineWidth, trsfc3);
	      
	      trsfc3.gridy = 6;
	      trsfc3.gridx = 1;
	      batchPanel.add(splitRootImage, trsfc3);
	      
	      JPanel batchPanel2 = new JPanel(new BorderLayout());
	      batchPanel2.setBorder(BorderFactory.createLineBorder(Color.gray));
	      batchPanel2.add(batchPanel, BorderLayout.WEST);
	      
	      JPanel batchPanel3 = new JPanel(new BorderLayout());
	      batchPanel3.setBorder(BorderFactory.createEmptyBorder(0, 20, 5, 20));
	      batchPanel3.add(batchPanel2);
	      
	      JPanel batchChoosePanel = new JPanel(new BorderLayout());
	      batchChoosePanel.setBorder(BorderFactory.createEmptyBorder(5, 20, 5, 20));
	      batchChoosePanel.add(batchExport, BorderLayout.WEST);
	      batchChoosePanel.add(batchJCB, BorderLayout.EAST);
	      
	      JPanel batchButtonPanel = new JPanel(new BorderLayout());
	      batchButtonPanel.setBorder(BorderFactory.createEmptyBorder(5, 20, 5, 20));
	      batchButtonPanel.add(batchButton, BorderLayout.EAST);
	      
	      
	      JPanel batchPanel4 = new JPanel(new BorderLayout());
	      batchPanel4.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
	      batchPanel4.add(batchChoosePanel, BorderLayout.NORTH);
	      batchPanel4.add(batchPanel3, BorderLayout.CENTER);
	      batchPanel4.add(batchButtonPanel, BorderLayout.SOUTH);
	            
	      // Assemble all
	      
	      JPanel finalPanel1 = new JPanel(new BorderLayout());
	      finalPanel1.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
	      finalPanel1.add(trsfPanel6, BorderLayout.NORTH);
	      finalPanel1.add(new JSeparator());
	      finalPanel1.add(batchPanel4, BorderLayout.SOUTH);
	      	      
	      
	      JScrollPane finalPanel = new JScrollPane(finalPanel1);

	      return finalPanel;	   
   }
   
   /**
    * Get plot tab
    * @return
    */
   private JPanel getPlotTab(){

	      hist1 = new Histogram();
	      hist2 = new Histogram();
	      hist3 = new Histogram();
	      hist4 = new Histogram();
	      hist5 = new Histogram();
	      hist6 = new Histogram();

	      g1 = new Chart();
	      g2 = new Chart();
	      g3 = new Chart();
	      
	      JLabel l1 = new JLabel("Bit depth");
	      JLabel l2 = new JLabel("Histograms");
	      JLabel l3 = new JLabel("Charts");
	      JLabel l4 = new JLabel("Options");

	      plotTextField1 = new JTextField("20");
	      plotTextField2 = new JTextField("20");
	      plotTextField3 = new JTextField("20");
	      plotTextField4 = new JTextField("20");
	      plotTextField5 = new JTextField("20");
	      plotTextField6 = new JTextField("20");
	      
	      plotComboBox1 = new JComboBox();
	      plotComboBox1.setPrototypeDisplayValue("XXXXXXXX");
	      plotComboBox1.addActionListener(this);
	      plotComboBox1.setActionCommand("SELECT_ROOT");
	      
	      plotComboBox2 = new JComboBox();
	      plotComboBox2.setPrototypeDisplayValue("XXXXXXXX");
	      plotComboBox2.addActionListener(this);
	      plotComboBox2.setActionCommand("SELECT_ROOT_1");
	      
	      rePlot = new JCheckBox("Create new graphs ");
	      rePlot.setFont(font);
	      rePlot.addItemListener(this);
	      
	      absVal = new JCheckBox("Absolute values");
	      absVal.setFont(font);
	      absVal.addItemListener(this);
	            
	      insAngHist = new JCheckBox("Insertion angle histogram");
	      insAngHist.setFont(font);
	      insAngHist.addItemListener(this);
	      
	      diamHist = new JCheckBox("Root diameter");
	      diamHist.setFont(font);
	      diamHist.addItemListener(this);
	      
	      diamPHist = new JCheckBox("Prim. root diameter");
	      diamPHist.setFont(font);
	      diamPHist.addItemListener(this);
	      
	      diamSHist = new JCheckBox("Sec. root diameter");
	      diamSHist.setFont(font);
	      diamSHist.addItemListener(this);
	      
	      diamAllHist = new JCheckBox("Root diameter (All)");
	      diamAllHist.setFont(font);
	      diamAllHist.addItemListener(this);
	      
	      interHist = new JCheckBox("Root interbranch ");
	      interHist.setFont(font);
	      interHist.addItemListener(this);
	      
	      lengthChart = new JCheckBox("Lateral length ");
	      lengthChart.setFont(font);
	      lengthChart.addItemListener(this);
	      
	      interChart = new JCheckBox("Root interbranch ");
	      interChart.setFont(font);
	      interChart.addItemListener(this);
	      
	      angChart = new JCheckBox("Root direction ");
	      angChart.setFont(font);
	      angChart.addItemListener(this);
	      
	      JPanel p2 = new JPanel();
	      GridBagLayout gridbag1 = new GridBagLayout();
	      p2.setLayout(gridbag1);
	      GridBagConstraints gbc = new GridBagConstraints();
	      gbc.anchor = GridBagConstraints.WEST;
	      
	      
	      gbc.gridx = 0;
	      gbc.gridy = 0;
	      gbc.fill = GridBagConstraints.HORIZONTAL;
	      gbc.insets = new Insets(2, 2, 2, 2);
	      p2.add(l2, gbc);
	      
	      gbc.gridx = 1;
	      p2.add(l1, gbc);
	      
	      gbc.gridx = 0;
	      gbc.gridy = 1;
	      p2.add(insAngHist, gbc);
	      gbc.gridx = 1;
	      p2.add(plotTextField1, gbc);
	      
	      gbc.gridx = 0;
	      gbc.gridy = 2;
	      p2.add(diamHist, gbc);
	      gbc.gridx = 1;
	      p2.add(plotTextField2, gbc);
	      
	      gbc.gridx = 0;
	      gbc.gridy = 3;
	      p2.add(diamSHist, gbc);
	      gbc.gridx = 1;
	      p2.add(plotTextField3, gbc);
	      
	      gbc.gridx = 0;
	      gbc.gridy = 4;
	      p2.add(diamPHist, gbc);
	      gbc.gridx = 1;
	      p2.add(plotTextField4, gbc);
	      
	      gbc.gridx = 0;
	      gbc.gridy = 5;
	      p2.add(diamAllHist, gbc);
	      gbc.gridx = 1;
	      p2.add(plotTextField5, gbc);
	      
	      gbc.gridx = 0;
	      gbc.gridy = 6;
	      p2.add(interHist, gbc);
	      gbc.gridx = 1;
	      p2.add(plotTextField6, gbc);
	 
	      JPanel pP = new JPanel();
	      pP.setLayout(new GridBagLayout());
	      GridBagConstraints gbc1 = new GridBagConstraints();
	      
	      gbc1.gridx = 0;
	      gbc1.gridy = 0;
	      gbc1.fill = GridBagConstraints.HORIZONTAL;
	      gbc1.insets = new Insets(2, 2, 2, 2);
	      pP.add(l3, gbc1);
	      gbc1.gridx = 1;
	      pP.add(l4, gbc1);

	      gbc1.gridx = 0;
	      gbc1.gridy = 1;
	      pP.add(lengthChart, gbc1);
	      gbc1.gridx = 1;
	      pP.add(plotComboBox1, gbc1);
	      
	      gbc1.gridx = 0;
	      gbc1.gridy = 2;
	      pP.add(interChart, gbc1);

	      
	      gbc1.gridx = 0;
	      gbc1.gridy = 3;
	      pP.add(angChart, gbc1);
	      gbc1.gridx = 1;
	      pP.add(plotComboBox2, gbc1);
	      
	      JPanel p4 = new JPanel();
	      p4.setLayout(new BoxLayout(p4, BoxLayout.X_AXIS));
	      //p4.setBorder(BorderFactory.createLineBorder(Color.gray));
	      //p4.add(Box.createHorizontalGlue());
	      p4.add(p2, BorderLayout.LINE_START);
	      p4.add(pP, BorderLayout.LINE_END);
	      
	      plot = new JButton("Plot");
	      plot.setFont(font);
	      plot.setActionCommand("PLOT");
	      plot.addActionListener(this);

	      rPlot = new JButton("Refresh");
	      rPlot.setFont(font);
	      rPlot.setActionCommand("REFRESH_PLOT");
	      rPlot.addActionListener(this);
	      
	      cPlot = new JButton("Close All Plots");
	      cPlot.setFont(font);
	      cPlot.setActionCommand("CLOSE_PLOT");
	      cPlot.addActionListener(this);
	        
	      JPanel p3 = new JPanel();
	      p3.setLayout(new BoxLayout(p3, BoxLayout.X_AXIS));
	      p3.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 0));
	      p3.add(absVal);
	      p3.add(rePlot);
	      p3.add(rPlot);
	      p3.add(cPlot);
	      p3.add(plot);
	      
	      JScrollPane scrollPane1 = new JScrollPane(p4);


	      JPanel p1 = new JPanel(new BorderLayout());
	      p1.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
	      p1.add(scrollPane1, BorderLayout.CENTER);
	      p1.add(p3, BorderLayout.SOUTH);
	      
	   return p1;	   
   }
   
   /**
    * Get the settings tab
    * @return
    */
   private JScrollPane getSettingsTab(){
	   
	   GridBagLayout gridbag = new GridBagLayout();
	      
	      setDPI = new JLabel("DPI (Dots per Inch)");
	      DPIValue = new JTextField(""+0, 6);
	      dpiBox = new JCheckBox();
	      dpiBox.setSelected(true);
	      dpiBox.addActionListener(this);
	      dpiBox.setActionCommand("DPI_SCALE");
	      
	      cmValue = new JTextField(""+0, 6);
	      setPixel = new JLabel("pixels / ");
	      pixValue = new JTextField(""+0, 6);
	      
	      cmBox = new JCheckBox();
	      cmBox.setSelected(false);
	      cmBox.addActionListener(this);
	      cmBox.setActionCommand("CM_SCALE");
	      
	      getLineButton = new JButton("Get line");
	      getLineButton.addActionListener(this);
	      getLineButton.setActionCommand("GET_LINE");
	      
	      dpiApplyButton = new JButton("Apply");
	      dpiApplyButton.addActionListener(this);
	      dpiApplyButton.setActionCommand("APPLY_DPI");
	      
	      dpiDefaultButton = new JButton("Set as default");
	      dpiDefaultButton.addActionListener(this);
	      dpiDefaultButton.setActionCommand("DEFAULT_DPI");
	      
	      dpiHelpButton = new JButton("?");
	      dpiHelpButton.addActionListener(this);
	      dpiHelpButton.setActionCommand("HELP_DPI");
	      dpiHelpButton.setPreferredSize(new Dimension(15, 15));
	      
	      String[] units = {"cm", "mm", "inch"};
	      unitsList = new JComboBox(units);
	      unitsList.setSelectedItem(0); 
	      unitsList.addActionListener(this);
	      unitsList.setActionCommand("UNIT_CHANGE");
	            
	      JPanel panel1 = new JPanel();
	      GridBagConstraints c1 = new GridBagConstraints();
	      panel1.setLayout(gridbag);
	      
	      c1.gridx = 0;
	      c1.gridy = 0;
	      panel1.add(dpiBox, c1);
	      c1.gridx = 1;
	      panel1.add(DPIValue, c1);
	      c1.gridx = 2;
	      panel1.add(setDPI, c1);
	      
	      JPanel panel2 = new JPanel();
	      GridBagConstraints c2 = new GridBagConstraints();
	      panel2.setLayout(gridbag);
	      
	      c2.gridx = 0;
	      c2.gridy = 1;
	      panel2.add(cmBox, c2);
	      c2.gridx = 1;
	      panel2.add(pixValue, c2);
	      c2.gridx = 2;
	      panel2.add(setPixel, c2);
	      c2.gridx = 3;
	      panel2.add(cmValue, c2);
	      c2.gridx = 4;
	      panel2.add(unitsList, c2);
	      
	      JPanel dpiButtonsPanel = new JPanel();
	      dpiButtonsPanel.setLayout(new BoxLayout(dpiButtonsPanel, BoxLayout.X_AXIS));
	      dpiButtonsPanel.add(Box.createHorizontalGlue());
	      dpiButtonsPanel.add(getLineButton);
	      dpiButtonsPanel.add(dpiDefaultButton);
	      dpiButtonsPanel.add(dpiApplyButton);
	      
	      JPanel panel1bis = new JPanel(new BorderLayout());
	      panel1bis.add(panel1, BorderLayout.WEST);
	      
	      JPanel panel3 = new JPanel(new BorderLayout());
	      panel3.add(panel1bis, BorderLayout.NORTH);
	      panel3.add(panel2, BorderLayout.SOUTH);
	      
	      JPanel panel4 = new JPanel(new BorderLayout());
	      panel4.setBorder(BorderFactory.createLineBorder(Color.gray));
	      panel4.add(panel3, BorderLayout.WEST);
	      
	      JLabel text1 = new JLabel("Image resolution");
	      Font f3 = text1.getFont();
	      text1.setFont(f3.deriveFont(f3.getStyle() ^ Font.BOLD));
	      JPanel resolutionTitlePanel = new JPanel(new BorderLayout());
	      resolutionTitlePanel.setBorder(BorderFactory.createEmptyBorder(0, 5, 5, 5));
	      resolutionTitlePanel.add(text1, BorderLayout.WEST);
	      resolutionTitlePanel.add(dpiHelpButton, BorderLayout.EAST);
	      
	      JPanel dpiPanel = new JPanel(new BorderLayout());
	      dpiPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
	      dpiPanel.add(resolutionTitlePanel, BorderLayout.NORTH);
	      dpiPanel.add(panel4, BorderLayout.CENTER);
	      dpiPanel.add(dpiButtonsPanel, BorderLayout.SOUTH);
	      
	      // Names
	      
	      JPanel namesPanel = new JPanel();
	      namesPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
	      GridBagConstraints c4 = new GridBagConstraints();
	      c4.anchor = GridBagConstraints.EAST;
	      namesPanel.setLayout(gridbag);
	      
	      askNameBox = new JCheckBox("Ask for name");
	      askNameBox.setSelected(SR.prefs.getBoolean("askName", true));
	      askNameBox.addActionListener(this);
	      askNameBox.setActionCommand("ASK_NAME");
	      
	      nameHelpButton = new JButton("?");
	      nameHelpButton.addActionListener(this);
	      nameHelpButton.setActionCommand("HELP_NAME");
	      nameHelpButton.setPreferredSize(new Dimension(15, 15));
	      
	      namesApplyButton = new JButton("Apply");
	      namesApplyButton.addActionListener(this);
	      namesApplyButton.setActionCommand("APPLY_NAMES");
	      
	      rootNameLabel1 = new JLabel("Principal root prefix:  ");
	      rootNameLabel2 = new JLabel("Lateral root prefix:  ");
	      
	      rootName1 = new JTextField(SR.prefs.get("root_ID", "root_"), 10);
	      rootName2 = new JTextField(SR.prefs.get("lateral_ID", "lat_"), 10);
	      
	      c4.anchor = GridBagConstraints.WEST;

	      c4.gridx = 0;
	      c4.gridy = 0;
	      namesPanel.add(rootNameLabel1, c4);
	      c4.gridx = 1;
	      namesPanel.add(rootName1, c4);
	      c4.gridx = 0;
	      c4.gridy = 1;
	      namesPanel.add(rootNameLabel2, c4);
	      c4.gridx = 1;
	      namesPanel.add(rootName2, c4);
	      c4.gridx = 0;
	      c4.gridy = 2;
	      namesPanel.add(askNameBox, c4);
	      
	      JPanel namesPanel2 = new JPanel(new BorderLayout());
	      namesPanel2.setBorder(BorderFactory.createLineBorder(Color.gray));
	      namesPanel2.add(namesPanel, BorderLayout.WEST);

	      JLabel text4 = new JLabel("Naming options");
	      Font f2 = text4.getFont();
	      text4.setFont(f2.deriveFont(f2.getStyle() ^ Font.BOLD));      
	      JPanel nameTitlePanel = new JPanel(new BorderLayout());
	      nameTitlePanel.setBorder(BorderFactory.createEmptyBorder(0, 5, 5, 5));
	      nameTitlePanel.add(text4, BorderLayout.WEST);
	      nameTitlePanel.add(nameHelpButton, BorderLayout.EAST);

	      JPanel namesButtonsPanel = new JPanel();
	      namesButtonsPanel.setLayout(new BoxLayout(namesButtonsPanel, BoxLayout.X_AXIS));
	      namesButtonsPanel.add(Box.createHorizontalGlue());
	      namesButtonsPanel.add(namesApplyButton);
	      
	      JPanel namesPanel1 = new JPanel(new BorderLayout());
	      namesPanel1.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
	      namesPanel1.add(nameTitlePanel, BorderLayout.NORTH);
	      namesPanel1.add(namesPanel2, BorderLayout.CENTER);
	      namesPanel1.add(namesButtonsPanel, BorderLayout.SOUTH);
	      
	      // SQL
	      
	      JPanel sqlPanel = new JPanel();
	      sqlPanel.setLayout(new GridBagLayout());
	      GridBagConstraints sqlgbc = new GridBagConstraints();
	      
	      sqlServ = SR.sqlServ;
	      
	      sqlHelpButton = new JButton("?");
	      sqlHelpButton.addActionListener(this);
	      sqlHelpButton.setActionCommand("HELP_SQL");
	      sqlHelpButton.setPreferredSize(new Dimension(15, 15));
	      
	      JLabel driverLabel = new JLabel("Driver class name:");
	      sqlDriverField = new JTextField(sqlServ.getDriver(), 20);
	      JLabel urlLabel = new JLabel("Connection URL:");
	      sqlUrlField = new JTextField(sqlServ.getUrl(), 20);
	      JLabel userLabel = new JLabel("Connection user name:");
	      sqlUserField = new JTextField(sqlServ.getUser(), 20);
	      JLabel pwLabel = new JLabel("Connection password:");
	      sqlPwField = new JTextField(sqlServ.getPassword(), 20);

	      sqlgbc.gridx = 0;
	      sqlgbc.gridy = 0;
	      sqlgbc.fill = GridBagConstraints.HORIZONTAL;
	      sqlgbc.insets = new Insets(2, 2, 2, 2);
	      sqlPanel.add(driverLabel, sqlgbc);
	      
	      sqlgbc.gridx = 1;
	      sqlPanel.add(sqlDriverField, sqlgbc);
	      
	      sqlgbc.gridx = 0;
	      sqlgbc.gridy = 1;
	      sqlPanel.add(urlLabel, sqlgbc);
	      sqlgbc.gridx = 1;
	      sqlPanel.add(sqlUrlField, sqlgbc);
	      
	      sqlgbc.gridx = 0;
	      sqlgbc.gridy = 2;
	      sqlPanel.add(userLabel, sqlgbc);
	      sqlgbc.gridx = 1;
	      sqlPanel.add(sqlUserField, sqlgbc);
	      
	      sqlgbc.gridx = 0;
	      sqlgbc.gridy = 3;
	      sqlPanel.add(pwLabel, sqlgbc);
	      sqlgbc.gridx = 1;
	      sqlPanel.add(sqlPwField, sqlgbc);
	      
	      JPanel sqlPanel1 = new JPanel(new BorderLayout());
	      sqlPanel1.setBorder(BorderFactory.createLineBorder(Color.gray));
	      sqlPanel1.add(sqlPanel, BorderLayout.WEST);
	      
	      JPanel sqlPanel2 = new JPanel();
	      sqlPanel2.setLayout(new BoxLayout(sqlPanel2, BoxLayout.X_AXIS));
	      sqlSavePrefsButton = new JButton("Save Prefs");
	      sqlSavePrefsButton.addActionListener(this);
	      sqlSavePrefsButton.setActionCommand("SQL_SAVE_PREFS");
	      sqlDefaultButton = new JButton("Defaults");
	      sqlDefaultButton.addActionListener(this);
	      sqlDefaultButton.setActionCommand("SQL_DEFAULTS");
	      sqlRestartServerButton = new JButton("Restart server");
	      sqlRestartServerButton.addActionListener(this);
	      sqlRestartServerButton.setActionCommand("SQL_RESTART");

	      sqlPanel2.add(Box.createHorizontalGlue());
	      sqlPanel2.add(sqlDefaultButton);
	      sqlPanel2.add(Box.createRigidArea(new Dimension(5, 5)));
	      sqlPanel2.add(sqlSavePrefsButton);
	      sqlPanel2.add(Box.createRigidArea(new Dimension(5, 5)));
	      sqlPanel2.add(sqlRestartServerButton);
	      sqlPanel2.add(Box.createRigidArea(new Dimension(5, 5)));
	      
	      JLabel sqlText = new JLabel("SQL options");
	      Font f4 = sqlText.getFont();
	      sqlText.setFont(f4.deriveFont(f4.getStyle() ^ Font.BOLD));      
	      JPanel sqlTitlePanel = new JPanel(new BorderLayout());
	      sqlTitlePanel.setBorder(BorderFactory.createEmptyBorder(0, 5, 5, 5));
	      sqlTitlePanel.add(sqlText, BorderLayout.WEST);
	      sqlTitlePanel.add(sqlHelpButton, BorderLayout.EAST);
	      
	      JPanel sqlPanel3 = new JPanel(new BorderLayout());
	      sqlPanel3.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
	      sqlPanel3.add(sqlTitlePanel, BorderLayout.NORTH);
	      sqlPanel3.add(sqlPanel1, BorderLayout.CENTER);
	      sqlPanel3.add(sqlPanel2, BorderLayout.SOUTH);
	      
	      // Threshold
	      
	      JPanel thresholdPanel = new JPanel();
	      GridBagConstraints c3 = new GridBagConstraints();
	      c3.anchor = GridBagConstraints.EAST;
	      thresholdPanel.setLayout(gridbag);
	      
	      thresholdHelpButton = new JButton("?");
	      thresholdHelpButton.addActionListener(this);
	      thresholdHelpButton.setActionCommand("HELP_THRESHOLD");
	      thresholdHelpButton.setPreferredSize(new Dimension(15, 15));
	      
	      autoThresholdBox = new JCheckBox("Adaptive thresholding");
	      autoThresholdBox.setSelected(true);
	      autoThresholdBox.addActionListener(this);
	      autoThresholdBox.setActionCommand("AUTO_THRESHOLD");
	      
	      IJThresholdBox = new JCheckBox("ImageJ fixed threshold");
	      IJThresholdBox.setSelected(false);
	      IJThresholdBox.addActionListener(this);
	      IJThresholdBox.setActionCommand("IJ_THRESHOLD");
	      
	      c3.gridx = 0;
	      c3.gridy = 0;
	      thresholdPanel.add(autoThresholdBox, c3);
	      c3.gridy = 1;
	      thresholdPanel.add(IJThresholdBox, c3);
	      
	      JPanel thresholdPanel2 = new JPanel(new BorderLayout());
	      thresholdPanel2.setBorder(BorderFactory.createLineBorder(Color.gray));
	      thresholdPanel2.add(thresholdPanel, BorderLayout.WEST);

	      JLabel text3 = new JLabel("Thresholding method");
	      Font f = text3.getFont();
	      text3.setFont(f.deriveFont(f.getStyle() ^ Font.BOLD));
	      JPanel thresholdTitlePanel = new JPanel(new BorderLayout());
	      thresholdTitlePanel.setBorder(BorderFactory.createEmptyBorder(0, 5, 5, 5));
	      thresholdTitlePanel.add(text3, BorderLayout.WEST);
	      thresholdTitlePanel.add(thresholdHelpButton, BorderLayout.EAST);

	      JPanel thresholdPanel1 = new JPanel(new BorderLayout());
	      thresholdPanel1.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
	      thresholdPanel1.add(thresholdTitlePanel, BorderLayout.NORTH);
	      thresholdPanel1.add(thresholdPanel2, BorderLayout.SOUTH);
	      
	      // Lateral Setting 
	      
	      JPanel latSettingPanel = new JPanel();
	      latSettingPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
	      GridBagConstraints c = new GridBagConstraints();
	      latSettingPanel.setLayout(gridbag);
	      
	      lateralHelpButton = new JButton("?");
	      lateralHelpButton.addActionListener(this);
	      lateralHelpButton.setActionCommand("HELP_LATERAL");
	      lateralHelpButton.setPreferredSize(new Dimension(15, 15));
	      
	      maxAngleLabel = new JLabel("Maximum insertion angle");
	      maxAngleField = new JTextField(""+maxAngle, 5);
	      
	      nStepLabel = new JLabel("Number of steps along the roots");
	      nStepField = new JTextField(""+nStep, 5);

	      minNSizeLabel = new JLabel("Minimum diameter of a node (% of parental diameter)");
	      minNSizeField = new JTextField(""+minNodeSize, 5);
	      
	      maxNSizeLabel = new JLabel("Maximum diameter of a node (% of parental diameter)");
	      maxNSizeField = new JTextField(""+maxNodeSize, 5);
	      
	      minRSizeLabel = new JLabel("Minimum size of a lateral root (% of parental diameter)");
	      minRSizeField = new JTextField(""+minRootSize, 5);

	      
	      
	      autoFindLat = new JCheckBox("Automatically search for laterals?");
	      autoFindLat.setSelected(autoFind);
	      
	      globalConvexHullBox = new JCheckBox("Convexhull includes laterals");
	      globalConvexHullBox.setSelected(globalConvex);
	      
	      checkNSizeBox = new JCheckBox("Ckeck the size of the nodes?");
	      checkNSizeBox.setSelected(checkNSize);
	      
	      checkRDirBox = new JCheckBox("Check the direction of the nodes?");
	      checkRDirBox.setSelected(checkRDir);

	      checkRSizeBox = new JCheckBox("Check the size of the roots?");
	      checkRSizeBox.setSelected(checkRSize);
	      
	      useBinary = new JCheckBox("Use skeleton (slower for large images)");
	      useBinary.setSelected(useBinaryImg);
	      
	      savePrefsButton = new JButton("Apply");
	      savePrefsButton.addActionListener(this);
	      savePrefsButton.setActionCommand("APPLY_PREF");
	      
	      defaultButton = new JButton("Defaults");
	      defaultButton.addActionListener(this);
	      defaultButton.setActionCommand("DEFAULTS_PREF");
	      
	      c.anchor = GridBagConstraints.WEST;
	      
	      c.gridx = 0;
	      c.gridy = 0;
	      c.fill = GridBagConstraints.VERTICAL;
	      c.insets = new Insets(2, 2, 2, 2);
	      
	      latSettingPanel.add(nStepLabel, c);
	      c.gridx = 1;
	      latSettingPanel.add(nStepField, c);
	      
	      c.gridx = 0;
	      c.gridy = 2;
	      latSettingPanel.add(checkNSizeBox, c);

	      c.gridy = 3;
	      latSettingPanel.add(minNSizeLabel, c);
	      c.gridx = 1;
	      latSettingPanel.add(minNSizeField, c);
	      
	      c.gridx = 0;
	      c.gridy = 4;
	      latSettingPanel.add(maxNSizeLabel, c);
	      c.gridx = 1;
	      latSettingPanel.add(maxNSizeField, c);
	      
	      c.gridx = 0;
	      c.gridy = 5;
	      latSettingPanel.add(checkRSizeBox, c);
	      
	      c.gridy = 6;
	      latSettingPanel.add(minRSizeLabel, c);
	      c.gridx = 1;
	      latSettingPanel.add(minRSizeField, c);
	      
	      c.gridx = 0;
	      c.gridy = 7;
	      latSettingPanel.add(checkRDirBox, c);
	      
	      c.gridy = 8;
	      latSettingPanel.add(maxAngleLabel, c);
	      c.gridx = 1;
	      latSettingPanel.add(maxAngleField, c);
	      
	      c.gridx = 0;
	      c.gridy = 9;
	      latSettingPanel.add(autoFindLat, c);
	      
	      c.gridx = 0;
	      c.gridy = 10;
	      latSettingPanel.add(useBinary, c);
	      
	      c.gridx = 0;
	      c.gridy = 11;
	      latSettingPanel.add(globalConvexHullBox, c);
	      
	      JPanel latSettingPanel1 = new JPanel(new BorderLayout());
	      latSettingPanel1.setBorder(BorderFactory.createLineBorder(Color.gray));
	      latSettingPanel1.add(latSettingPanel, BorderLayout.WEST);
	      
	      JPanel p2 = new JPanel();
	      p2.setLayout(new BoxLayout(p2, BoxLayout.X_AXIS));
	      p2.add(Box.createHorizontalGlue());
	      p2.add(defaultButton);
	      p2.add(savePrefsButton);
	  
	      JLabel text2 = new JLabel("Lateral research parameters");
	      Font f1 = text2.getFont();
	      text2.setFont(f1.deriveFont(f1.getStyle() ^ Font.BOLD));
	      JPanel latSettingTitlePanel = new JPanel(new BorderLayout());
	      latSettingTitlePanel.setBorder(BorderFactory.createEmptyBorder(10, 5, 5, 5));
	      latSettingTitlePanel.add(text2, BorderLayout.WEST);
	      latSettingTitlePanel.add(lateralHelpButton, BorderLayout.EAST);

	      JPanel p5 = new JPanel(new BorderLayout());
	      p5.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
	      p5.add(latSettingTitlePanel, BorderLayout.NORTH);
	      p5.add(latSettingPanel1, BorderLayout.SOUTH);
	      
	      JPanel p3 = new JPanel(new BorderLayout());
	      p3.add(p5, BorderLayout.NORTH);
	      p3.add(p2, BorderLayout.SOUTH);
	      
	      //All
	      
	      JPanel p7 = new JPanel(new BorderLayout());
	      p7.add(dpiPanel, BorderLayout.NORTH);
	      p7.add(namesPanel1, BorderLayout.CENTER);
	      p7.add(sqlPanel3, BorderLayout.SOUTH);

	      JPanel p6 = new JPanel(new BorderLayout());
	      p6.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
	      p6.add(p7, BorderLayout.NORTH);
	      p6.add(p3, BorderLayout.CENTER);
	      p6.add(thresholdPanel1, BorderLayout.SOUTH);

	      
	      JScrollPane scrollPane = new JScrollPane(p6);
	      gridbag.setConstraints(scrollPane,c);
	      
	      return scrollPane;	   
   }
   
   /**
    * 
    * @return
    */
   private JPanel getAboutTab(){
	   
	      ImageIcon icon = new ImageIcon(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/images/SmartRoot_logo.gif")));
	      JLabel logo = new JLabel("",icon, JLabel.CENTER);
	      logo.setPreferredSize(new Dimension(400, 53));
	      
	      JLabel logo1 = new JLabel("",icon, JLabel.CENTER);
	      logo.setPreferredSize(new Dimension(400, 53));
	      
	      aboutPane = new JTextPane();
	      aboutPane.setEditable(false);
	      aboutPane.setText(displayAboutText());
	      SimpleAttributeSet bSet = new SimpleAttributeSet();  
	      StyleConstants.setAlignment(bSet, StyleConstants.ALIGN_CENTER);  
	      StyledDocument doc = aboutPane.getStyledDocument();  
	      doc.setParagraphAttributes(0, doc.getLength(), bSet, false); 
	      
	      aboutView = new JScrollPane(aboutPane); 
	      aboutView.setBorder(BorderFactory.createLineBorder(Color.gray));

	      
	      JPanel p1 = new JPanel(new BorderLayout());
	      p1.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
	      p1.add(logo1, BorderLayout.NORTH);
	      p1.add(aboutView, BorderLayout.CENTER);	   
	      return p1;	   
   }
   
   /**
    * 
    * @return
    */
   private JPanel getLinkedTab(){
	  
	   Font font = new Font("Dialog", Font.PLAIN, 12);

	  linkedDatafileModel = new LinkedDatafileTableModel();
      linkedDatafileTable = new JTable(linkedDatafileModel);
      linkedDatafileTable.setAutoCreateColumnsFromModel(false);
      linkedDatafileTable.setColumnSelectionAllowed(false);
      linkedDatafileTable.setRowSelectionAllowed(false);
      linkedDatafileTable.setFont(font);
      linkedDatafileTable.getTableHeader().addMouseListener(new MouseAdapter() {
         public void mouseClicked(MouseEvent me) {
            if (linkedDatafileTable.columnAtPoint(me.getPoint()) > 0)
               linkedDatafileModel.changeColumnSelection(linkedDatafileTable.columnAtPoint(me.getPoint()));
            }
         });
      linkedDatafileTable.addMouseListener(new MouseAdapter() {
         public void mouseClicked(MouseEvent me) {
            if (linkedDatafileTable.columnAtPoint(me.getPoint()) == 0)
               linkedDatafileModel.changeRowSelection(linkedDatafileTable.rowAtPoint(me.getPoint()));
            }
         });
      TableColumnModel tcm = linkedDatafileTable.getColumnModel();      
      TableCellRenderer ihr = new IconHeaderRenderer();
      tcm.getColumn(0).setPreferredWidth(200);
      for (int i = 0; i < Mark.getTypeCount(); tcm.getColumn(i++).setHeaderRenderer(ihr));
      
      JButton addLinkedDatafile = new JButton("Add File");
      addLinkedDatafile.setFont(font);
      addLinkedDatafile.setActionCommand("LINKED_ADDFILE");
      addLinkedDatafile.addActionListener(this);
      
      JButton refresh = new JButton("Refresh display");
      refresh.setFont(font);
      refresh.setActionCommand("LINKED_REFRESH");
      refresh.addActionListener(this);
      
      JButton reset = new JButton("Reset File list");
      reset.setFont(font);
      reset.setActionCommand("LINKED_RESET");
      reset.addActionListener(this);
          
      JPanel p2 = new JPanel();
      p2.setLayout(new BoxLayout(p2, BoxLayout.X_AXIS));
      p2.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 0));
      p2.add(Box.createHorizontalGlue());
      p2.add(addLinkedDatafile);
      p2.add(Box.createHorizontalStrut(5));
      p2.add(refresh);
      p2.add(Box.createHorizontalStrut(5));
      p2.add(reset);

      JPanel p1 = new JPanel(new BorderLayout());
      p1.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
      JScrollPane linkedDatafilePane = new JScrollPane(linkedDatafileTable);
      p1.add(linkedDatafilePane, BorderLayout.CENTER);
      p1.add(p2, BorderLayout.SOUTH);
      
	  return p1;	   
   }
   
   
   /**
    * Get the current instance
    * @return
    */
   static public SRWin getInstance() {return instance;}   

  
   /**
    * Define the different action possible in the SRWin window
    */
   public void actionPerformed(ActionEvent e) {
	   	      
	   /*
	    * Settings tab actions
	    */
	   
	   	if(e.getActionCommand() == "GET_LINE"){
	   		if(rm.img.getRoi() != null && rm.img.getRoi().isLine()){
	   			pixValue.setText(""+(int)rm.img.getRoi().getLength());
	    	}
	   		else {
	   			pixValue.setText(""+(int)rm.img.getWidth());
	   		}
	   	}
	    if(e.getActionCommand() == "CM_SCALE"){dpiBox.setSelected(!cmBox.isSelected());}
	    if(e.getActionCommand() == "DPI_SCALE"){cmBox.setSelected(!dpiBox.isSelected());}
	   if(e.getActionCommand() == "APPLY_PREF"){changePreferences();}
	   if(e.getActionCommand() == "DEFAULTS_PREF"){resetPreferences();}
	   if(e.getActionCommand() == "APPLY_DPI"){changeDPI(false);}
	   if(e.getActionCommand() == "DEFAULT_DPI"){changeDPI(true);}
	   
	    if(e.getActionCommand() == "AUTO_THRESHOLD"){
	    	IJThresholdBox.setSelected(!autoThresholdBox.isSelected());
	    	try{ rm.setThresholdMethod(RootModel.THRESHOLD_ADAPTIVE1);}
	    	catch(NullPointerException npe){}
	    }
	    if(e.getActionCommand() == "IJ_THRESHOLD"){
	    	autoThresholdBox.setSelected(!IJThresholdBox.isSelected());
	    	try{ rm.setThresholdMethod(RootModel.THRESHOLD_ADAPTIVE2);}
    		catch(NullPointerException npe){}
	    }
	   
	    if(e.getActionCommand() == "APPLY_NAMES"){
	    	SR.prefs.put("root_ID", rootName1.getText());
	    	SR.prefs.put("lateral_ID", rootName2.getText());
	    }
	    
	    if(e.getActionCommand() == "HELP_DPI") {displayHelp(1);}
	    
	    if(e.getActionCommand() == "HELP_NAME"){displayHelp(2);}
	    
	    if(e.getActionCommand() == "HELP_THRESHOLD"){displayHelp(3);}
	    
	    if(e.getActionCommand() == "HELP_LATERAL"){displayHelp(4);}

	    if(e.getActionCommand() == "HELP_SQL"){displayHelp(5);}
	    
	    if(e.getActionCommand() == "UNIT_CHANGE"){refreshScale();}
	    
	   if(e.getActionCommand() == "ASK_NAME"){SR.prefs.putBoolean("askName", askNameBox.isSelected());}

	    
      if (e.getActionCommand() == "SQL_SAVE_PREFS") {
          sqlServ.savePrefs(sqlDriverField.getText(), sqlUrlField.getText(), sqlUserField.getText(), sqlPwField.getText());
       }
       else if (e.getActionCommand() == "SQL_DEFAULTS") {
          sqlDriverField.setText(sqlServ.getDefaultDriver());
          sqlUrlField.setText(sqlServ.getDefaultUrl());
          sqlUserField.setText(sqlServ.getDefaultUser());
          sqlPwField.setText(sqlServ.getDefaultPassword());
       }
       else if (e.getActionCommand() == "SQL_RESTART") {
          sqlServ.start(sqlDriverField.getText(), sqlUrlField.getText(), sqlUserField.getText(), sqlPwField.getText());
       }
	   
	   /*
	    * Root list tab actions
	    */
	   
		if(e.getActionCommand() == "APPLY_PO"){
			if(rootListTree.tree.getSelectionCount() > 0) rootListTree.setPoIndex(poListCombo.getSelectedIndex());
			else infoPane.setText("Please select a root");
			}
		
		if(e.getActionCommand() == "ACTION"){
			if(rootListTree.tree.getSelectionCount() > 0) applyRootListActions();
			else infoPane.setText("Please select a root");
			}
		if(e.getActionCommand() == "OK_BUTTON"){
			rootListTree.attachParent(false);
			rootListTree.refreshNodes();
		} 
		
		if(e.getActionCommand() == "CANCEL_BUTTON"){
            infoPane.setText("Please select a root");
			cancel.setEnabled(false);
			ok.setEnabled(false);
		} 
		
	   if(e.getActionCommand() == "ROOT_REFRESH"){
		   	rootListTree.refreshNodes();	   	
	   } 
	   	  
	   /*
	    * Layers tab actions
	    */
      if (e.getActionCommand() == "SAVE_PREFS") {
         SR.prefs.putBoolean("ShowAxis",showAxis.isSelected());
         SR.prefs.putBoolean("ShowNodes",showNodes.isSelected());
         SR.prefs.putBoolean("ShowBorders",showBorders.isSelected());
         SR.prefs.putBoolean("ShowArea",showArea.isSelected());
         SR.prefs.putBoolean("ShowTicks",showTicks.isSelected());
         SR.prefs.putBoolean("ShowTicksP",showTicksP.isSelected());
         SR.prefs.putBoolean("ShowMarks",showMarks.isSelected());
         SR.prefs.putBoolean("ShowConvexHull",displayConvexHull.isSelected());
         }
 
      /*
       * SQL tab actions
       */
      
      else if (e.getActionCommand() == "SQL_TRANSFER") { 
    	  transfersData(sqlExport.isSelected(), csvExport.isSelected(), imageExport.isSelected());
    	  }
      
      else if (e.getActionCommand() == "CSV_FOLDER") { 
    	  JFileChooser fc = new JFileChooser();
    	  JavaFilter fJavaFilter = new JavaFilter ();

		   fc.setFileFilter(fJavaFilter);
		   fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
		   int returnVal = fc.showDialog(SRWin.this, "Save");
		   
          if (returnVal == JFileChooser.APPROVE_OPTION){ 
        	  String fName = fc.getSelectedFile().toString();
        	  if(!fName.endsWith(".csv")) fName = fName.concat(".csv");
        	  csvFolderName.setText(fName);
          }
          else SR.write("Save command cancelled.");     
      }
      
      else if (e.getActionCommand() == "BATCH_CSV_FILE") { 
    	  JFileChooser fc = new JFileChooser();
    	  JavaFilter fJavaFilter = new JavaFilter ();

		   fc.setFileFilter(fJavaFilter);
		   fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
		   int returnVal = fc.showDialog(SRWin.this, "Save");
		   
          if (returnVal == JFileChooser.APPROVE_OPTION){ 
        	  String fName = fc.getSelectedFile().toString();
        	  if(!fName.endsWith(".csv")) fName = fName.concat(".csv");
        	  batchCSVFile.setText(fName);
          }
          else SR.write("Save command cancelled.");     
      }
 
      else if (e.getActionCommand() == "BATCH_IMAGE_FOLDER") { 
    	  JFileChooser fc = new JFileChooser();
		   fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		   int returnVal = fc.showDialog(SRWin.this, "Choose");
		   
          if (returnVal == JFileChooser.APPROVE_OPTION){ 
        	  String fName = fc.getSelectedFile().toString();
        	  batchImageFolder.setText(fName);
          }
          else SR.write("Choose folder cancelled.");     
      }
      
      else if (e.getActionCommand() == "BATCH_SOURCE_FOLDER") { 
    	  JFileChooser fc = new JFileChooser();

		   fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		   int returnVal = fc.showDialog(SRWin.this, "Choose");
		   
          if (returnVal == JFileChooser.APPROVE_OPTION){ 
        	  String fName = fc.getSelectedFile().toString();
        	  batchSourceFolder.setText(fName);
          }
          else SR.write("Choose folder cancelled.");     
      }
      
      else if(e.getActionCommand() == "BATCH_EXPORT"){
    	  batchExport();
      }
      
      else if (e.getActionCommand() == "IMAGE_FOLDER") { 
    	  JFileChooser fc = new JFileChooser();
    	  ImageFilter fJavaFilter = new ImageFilter();

		   fc.setFileFilter(fJavaFilter);
		   fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
		   int returnVal = fc.showDialog(SRWin.this, "Save");
		   
          if (returnVal == JFileChooser.APPROVE_OPTION){ 
        	  String fName = fc.getSelectedFile().toString();
			  if(!fName.endsWith("."+(String) imageJCB.getSelectedItem())) 
				  fName = changeExtension(fName, (String) imageJCB.getSelectedItem());
        	  imageFolderName.setText(fName);
          }
          else SR.write("Save command cancelled.");     
      }
      
	    else if(e.getActionCommand() == "IMAGE_TYPE"){
		  String fName = imageFolderName.getText();
		  if(!fName.equals("")){
			  if(!fName.endsWith("."+(String) imageJCB.getSelectedItem())) 
				  fName = changeExtension(fName, (String) imageJCB.getSelectedItem());
		  	imageFolderName.setText(fName);
		  }
	  }
  
      
      /*
       * Linked files tab actions
       */
      else if (e.getActionCommand() == "LINKED_REFRESH") {
    	 SR.write("REFRESH");
         rm.refreshLinkedMarks();
         rm.repaint();
         }
      else if (e.getActionCommand() == "LINKED_RESET") {
    	  SR.write("RESET");
    	  rm.refreshLinkedDatafileList();
         linkedDatafileModel.modelChanged();
         }
      
      
      /*
       * Plot tab
       */
      else if (e.getActionCommand() == "REFRESH_PLOT"){
    	  refreshPlot();
      }
      else if (e.getActionCommand() == "SELECT_ROOT"){
    	  if(b7 || b8){
	    	  setSelected((String) plotComboBox1.getSelectedItem(), true);
	    	  rm.repaint();
    	  }
      } 
      else if (e.getActionCommand() == "SELECT_ROOT_1"){
    	  if(b9){
	    	  setSelected((String) plotComboBox2.getSelectedItem(), true);
	    	  rm.repaint();
    	  }
      }
      
      else if (e.getActionCommand() == "PLOT"){ plot();}
      else if (e.getActionCommand() == "CLOSE_PLOT"){ closePlot();}
      }
   
   

/**
    * 
    * Define the actions of the check boxes from the SRWin window
    */
   
   public void itemStateChanged(ItemEvent e) {
      Object item = e.getItem();
      
      // Layer tab
      if (item == showAxis) rm.displayAxis = showAxis.isSelected();
      else if (item == showNodes) rm.displayNodes= showNodes.isSelected();
      else if (item == showBorders) rm.displayBorders = showBorders.isSelected();
      else if (item == showArea) rm.displayArea = showArea.isSelected();
      else if (item == showTicks) rm.displayTicks = showTicks.isSelected();
      else if (item == showTicksP) rm.displayTicksP = showTicksP.isSelected();
      else if (item == showMarks) rm.displayMarks = showMarks.isSelected();
      else if (item == displayConvexHull) rm.displayConvexHull = displayConvexHull.isSelected();
      
      // Transfers tab
      
      else if(item == sqlExport){
    	  this.sqlCreate.setEnabled(sqlExport.isSelected());
    	  this.sqlTableName.setEnabled(sqlExport.isSelected());
    	  this.transfersLabel2.setEnabled(sqlExport.isSelected());
    	  this.transfersButton.setEnabled(sqlExport.isSelected() || imageExport.isSelected() || csvExport.isSelected());
    	  this.sqlJCB.setEnabled(sqlExport.isSelected());
    }
      
      else if(item == imageExport){
    	  colorJCB.setEnabled(imageExport.isSelected());
    	  imageJCB.setEnabled(imageExport.isSelected());
    	  imageChooseFolder.setEnabled(imageExport.isSelected());
    	  imageFolderName.setEnabled(imageExport.isSelected());
    	  lineWidth.setEnabled(imageExport.isSelected());
    	  imageRealWidth.setEnabled(imageExport.isSelected());
    	  transfersLabel5.setEnabled(imageExport.isSelected());
    	  transfersLabel6.setEnabled(imageExport.isSelected());
    	  transfersLabel7.setEnabled(imageExport.isSelected());
    	  this.transfersButton.setEnabled(sqlExport.isSelected() || imageExport.isSelected() || csvExport.isSelected());
      }
      
      else if(item == csvExport){
    	  csvChooseFolder.setEnabled(csvExport.isSelected());
    	  csvFolderName.setEnabled(csvExport.isSelected());
    	  csvHeader.setEnabled(csvExport.isSelected());
    	  transfersLabel4.setEnabled(csvExport.isSelected());
    	  this.transfersButton.setEnabled(sqlExport.isSelected() || imageExport.isSelected() || csvExport.isSelected());
    	  this.csvJCB.setEnabled(csvExport.isSelected());
      }
      else if(item == batchDPI){
    	  this.batchDPIValue.setEnabled(batchDPI.isSelected());
      }
      else if(item == batchExport){
    	  batchButton.setEnabled(batchExport.isSelected());
    	  this.batchCSV.setEnabled(batchExport.isSelected());
    	  this.batchDPI.setEnabled(batchExport.isSelected());
    	  this.batchCSVButton.setEnabled(batchCSV.isSelected() && batchExport.isSelected());
    	  this.batchCSVFile.setEnabled(batchCSV.isSelected() && batchExport.isSelected());
    	  this.batchImage.setEnabled(batchExport.isSelected());
    	  this.batchImageButton.setEnabled(batchImage.isSelected() && batchExport.isSelected());
    	  this.batchImageFolder.setEnabled(batchImage.isSelected() && batchExport.isSelected());
    	  this.batchJCB.setEnabled(batchExport.isSelected());
    	  this.batchLabel1.setEnabled(batchExport.isSelected());
    	  this.batchLabel2.setEnabled(batchExport.isSelected());
    	  this.batchLabel3.setEnabled(batchExport.isSelected());
    	  this.batchSourceButton.setEnabled(batchExport.isSelected());
    	  this.batchSourceFolder.setEnabled(batchExport.isSelected());
    	  this.batchSQL.setEnabled(batchExport.isSelected());
    	  //this.persoDB.setEnabled(batchExport.isSelected() && batchSQL.isSelected());
    	  this.createBatchSQL.setEnabled(batchExport.isSelected() && batchSQL.isSelected());
    	  this.batchSQLTable.setEnabled(batchSQL.isSelected() && batchExport.isSelected());
    	  this.batchColorJCB.setEnabled(batchImage.isSelected() && batchExport.isSelected());
    	  this.batchImageJCB.setEnabled(batchImage.isSelected() && batchExport.isSelected());
    	  this.batchLineWidth.setEnabled(batchImage.isSelected() && batchExport.isSelected());
    	  this.splitRootImage.setEnabled(batchImage.isSelected() && batchExport.isSelected());
    	}
      
      else if(item == batchSQL){
    	  this.batchSQLTable.setEnabled(batchSQL.isSelected() && batchExport.isSelected());
    	  //this.persoDB.setEnabled(batchExport.isSelected() && batchSQL.isSelected());
    	  this.createBatchSQL.setEnabled(batchExport.isSelected() && batchSQL.isSelected());
      }
      
      else if(item == batchCSV) {
    	  this.batchCSVFile.setEnabled(batchCSV.isSelected() && batchExport.isSelected());
    	  this.batchCSVButton.setEnabled(batchCSV.isSelected() && batchExport.isSelected());
      }
      
      else if(item == batchImage) {
    	  this.batchImageFolder.setEnabled(batchImage.isSelected() && batchExport.isSelected());
    	  this.batchImageButton.setEnabled(batchImage.isSelected() && batchExport.isSelected());
    	  this.batchColorJCB.setEnabled(batchImage.isSelected() && batchExport.isSelected());
    	  this.batchImageJCB.setEnabled(batchImage.isSelected() && batchExport.isSelected());
    	  this.batchLineWidth.setEnabled(batchImage.isSelected() && batchExport.isSelected());
    	  this.splitRootImage.setEnabled(batchImage.isSelected() && batchExport.isSelected());
      }
      
      
      
      //Plot tab
      else if (item == insAngHist) b1 = insAngHist.isSelected();
      else if (item == diamHist) b2 = diamHist.isSelected();
      else if (item == diamPHist) b3 = diamPHist.isSelected();
      else if (item == diamSHist) b4 = diamSHist.isSelected();
      else if (item == diamAllHist) b5 = diamAllHist.isSelected();
      else if (item == interHist) b6 = interHist.isSelected();
      else if (item == lengthChart){
    	  b7 = lengthChart.isSelected();
    	  setSelected((String) plotComboBox1.getSelectedItem(), b7); 
      }
      else if (item == interChart){
    	  b8 = interChart.isSelected();
    	  setSelected((String) plotComboBox1.getSelectedItem(), b8);   
      }
      else if (item == angChart){
    	  b9 = angChart.isSelected();
    	  setSelected((String) plotComboBox2.getSelectedItem(), b9);   
      }
      else if (item == rePlot) reP = rePlot.isSelected();
      else if (item == absVal) absV = absVal.isSelected();
      
      if(rm != null) rm.repaint();
      }
   

   /**
    * Attach the current RootModel to SRWin  
    * @param rm
    * @param usePrefs
    */
   public void setCurrentRootModel(RootModel rm, boolean usePrefs) {
      if (rm == this.rm){
          linkedDatafileModel.modelChanged();
          rootListTree.tree.setModel(rm);
          rootListTree.tree.expandRow(0);;
          marksTableModel.modelChanged(null);
          infoPane.setText("Please select a root in the list");
    	  return;
      }
      this.rm = rm;
      
      try {updateScale();}
      catch(Exception e) {}
      
      linkedDatafileModel.modelChanged();
      rootListTree.tree.setModel(rm);
      rootListTree.tree.expandRow(0);
      
      try {
    	  refreshPlot();
      }
      catch(NullPointerException npe){}
      
      if (rm == null) return;
      if (usePrefs) {
         rm.displayAxis = SR.prefs.getBoolean("ShowAxis", true);
         rm.displayNodes = SR.prefs.getBoolean("ShowNodes", true);
         rm.displayBorders = SR.prefs.getBoolean("ShowBorders", false);
         rm.displayArea = SR.prefs.getBoolean("ShowArea", false);
         rm.displayTicks = SR.prefs.getBoolean("ShowTicks", false);
         rm.displayTicksP = SR.prefs.getBoolean("ShowTicksP", false);
         rm.displayMarks = SR.prefs.getBoolean("ShowMarks", false);
         rm.displayConvexHull = SR.prefs.getBoolean("ShowConvexHull", false);
         }
      showAxis.setSelected(rm.displayAxis);
      showNodes.setSelected(rm.displayNodes);
      showBorders.setSelected(rm.displayBorders);
      showArea.setSelected(rm.displayArea);
      showTicks.setSelected(rm.displayTicks);
      showTicksP.setSelected(rm.displayTicksP);
      showMarks.setSelected(rm.displayMarks);
      displayConvexHull.setSelected(rm.displayConvexHull);

      }
    
   /**
    * Close the SR window
    */
   public void dispose() {
      Point p = getLocationOnScreen();
      SR.prefs.putInt("SR_Win.Location.X", p.x);
      SR.prefs.putInt("SR_Win.Location.Y", p.y);
      SR.prefs.putInt("SR_Win.Location.Width", getWidth());
      SR.prefs.putInt("SR_Win.Location.Height", getHeight());
      super.dispose();
      }
   
   /**
    * Create a Tree list to display all the roots traced in the image
    * @author guillaumelobet
    */
   public class RootListTree extends JPanel implements TreeSelectionListener {
	   
	   /**
	    * 
	    */
	   private static final long serialVersionUID = 1L;
		public JTree tree;
	    private Root[] pRoot = null;
	    private ArrayList<Root> cRoot = new ArrayList<Root>();
	    private Root aR1, aR2;
	    boolean attach = false;
	    TreePath[] lastSelectedPath = null;

	    public RootListTree() {

	        super(new GridLayout(1,0));
	        tree = new JTree();
	        tree.getSelectionModel().setSelectionMode(TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);
	        tree.addTreeSelectionListener(this);
	        tree.setEditable(true);
	        tree.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
	        tree.setDragEnabled(true);
	        tree.setDropMode(DropMode.ON_OR_INSERT);
	        tree.setTransferHandler(new TreeTransferHandler());	        
            
	        // New icons for the tree
            tree.setCellRenderer(new MyRenderer());
          
	        JScrollPane treeView = new JScrollPane(tree);
	        add(treeView);
	    }
	    
	    private class MyRenderer extends DefaultTreeCellRenderer {
	    	
			private static final long serialVersionUID = 1L;
			ImageIcon primIcon = new ImageIcon(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/images/primary.gif")));
	        ImageIcon primOpenIcon = new ImageIcon(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/images/primary_open.gif")));
	        ImageIcon latIcon = new ImageIcon(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/images/lateral.gif")));
	        ImageIcon latOpenIcon = new ImageIcon(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/images/lateral_open.gif")));
	        ImageIcon terIcon = new ImageIcon(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/images/tertiary.gif")));
	        ImageIcon terOpenIcon = new ImageIcon(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/images/tertiary_open.gif")));
	        ImageIcon quatIcon = new ImageIcon(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/images/quatuary.gif")));
	        ImageIcon quatOpenIcon = new ImageIcon(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/images/quatuary_open.gif")));
	        ImageIcon markIcon = new ImageIcon(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/images/mark.gif")));
	        ImageIcon rootIcon = new ImageIcon(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/images/root_icon.gif")));

	        public MyRenderer() {}

	        public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded,
	                            boolean leaf, int row, boolean hasFocus) {

	            super.getTreeCellRendererComponent( tree, value, sel, expanded, leaf, row, hasFocus);
	            
	            if (isChild(value, row) == 0 && !expanded) setIcon(primIcon);
	            else if (isChild(value, row) == 0 && expanded) setIcon(primOpenIcon);
	            else if (isChild(value, row) == 1 && !expanded) setIcon(latIcon);
	            else if (isChild(value, row) == 1 && expanded) setIcon(latOpenIcon);
	            else if (isChild(value, row) == 2 && !expanded) setIcon(terIcon);
	            else if (isChild(value, row) == 2 && expanded) setIcon(terOpenIcon);
	            else if (isChild(value, row) >= 3 && isChild(value, row) < 30 && !expanded) setIcon(quatIcon);
	            else if (isChild(value, row) >= 3 && isChild(value, row) < 30 && expanded) setIcon(quatOpenIcon);
	            else if (isChild(value, row) == 50) setIcon(rootIcon);
	            else if (isChild(value, row) == 60) setIcon(markIcon);
	            return this;
	        }

	        protected int isChild(Object value, int row) {
	        	try{
		        	if(row == 0) return 50;
		        	else {
			        	Root root = (Root) value;
			        	return root.isChild();		        	
		        	}
	        	}
	            catch(Exception ex){return 40;}
	        }
	    }

	    public void valueChanged(TreeSelectionEvent e) {
	        
            try{for(int i = 0; i < pRoot.length ; i++){rm.selectRoot(pRoot[i], false);}}
            catch(Exception ex){}
            
            if(tree.getSelectionCount() > 0){
	            if(tree.isRowSelected(0)){
	            	String t = rm.displaySummary();
	            	rm.setSelected(true);
	            	infoPane.setText(t);
	            }
	            else{
	            	rm.setSelected(false);
			    	TreePath[] t = tree.getSelectionPaths();
			    	lastSelectedPath = t;
			    	pRoot = new Root[t.length];
			    	for(int i = 0 ; i < t.length; i++){
			            pRoot[i] = (Root) t[i].getLastPathComponent();
			            rm.selectRoot(pRoot[i], true);
			    	}
		    		infoPane.setText(rm.displayRootInfo(pRoot));
		    		
//		    		int po = pRoot[0].poIndex;
//		    		SR.write(po);
//		    		int j = 0;
//		    		for(int i = 0; i < rm.listPo.length; i++){
//		    			if(rm.listPo[i].equals(po)) j = i;
//		    		}
		    		poListCombo.setSelectedIndex(pRoot[0].poIndex);
		    		
		    		if (t.length == 1) marksTableModel.modelChanged(pRoot[t.length-1]);
		    		else marksTableModel.modelChanged(null);
	            }
            }
            //else infoPane.setText("Please select a root");

            rm.repaint();
	    }
	      
	    public void deleteRoot(){
	    	TreePath[] t = tree.getSelectionPaths();
	    	for(int i = 0 ; i < t.length; i++){
		        rm.selectRoot((Root) t[i].getLastPathComponent());
	            rm.deleteSelectedRoot();
	    	}
            rm.repaint();
            rootListTree.tree.setModel(rm);
            rootListTree.tree.expandRow(0);
	    }
	  	    
	    private void renameRoot(){
	    	rm.selectRoot((Root) tree.getLastSelectedPathComponent());
            String rootID = rm.getSelectedRootID();
            rootID = JOptionPane.showInputDialog("Enter the root identifier: ", rootID);
            if (rootID != null && rootID.length() > 0) rm.setSelectedRootID(rootID);
            rm.repaint();
	    }
	    
	    public void setPoIndex(int index){
	    	
	    	TreePath[] t = tree.getSelectionPaths();
	    	for(int i = 0 ; i < t.length; i++){
		        rm.selectRoot((Root) t[i].getLastPathComponent());
		    	Root r = rm.getSelectedRoot();
		    	r.setPoAccession(index);	            
	    	}
    		infoPane.setText(rm.displayRootInfo(pRoot));
	    	rm.repaint();
	    }
	    
	    private void attachParent(boolean first){
	    	
	    	if (first){
	    		cRoot.clear();
		    	TreePath[] t = tree.getSelectionPaths();
		    	for(int i = 0 ; i < t.length; i++){
			        cRoot.add((Root) t[i].getLastPathComponent());
		    	}
	    		infoPane.setText("Please select the parent root \n"+"and click on the 'OK' button");
	    		ok.setEnabled(true);
	    		cancel.setEnabled(true);
	    	}
	    	else{
	    		aR2 = (Root) tree.getLastSelectedPathComponent();
	    		for(int i = 0 ; i < cRoot.size(); i++){
	    			aR1 = cRoot.get(i);
		    		if(aR1.equals(aR2)) {
		    			SR.write("A root cannot be attached to itself");
		    			break;
		    		}
		    		if(aR2.parent != null){
		    			if(aR2.parent.equals(aR1)){
			    			SR.write("A root cannot be attached to one of its children");
			    			break;
		    			}
		    		}
		    		rm.setParent(aR2, aR1);
	    		}
	    		rm.repaint();
	    		ok.setEnabled(false);
	    		cancel.setEnabled(false);
	    	}
	    }
	    
	    private void detachParent(){
	    	TreePath[] t = tree.getSelectionPaths();
	    	for(int i = 0 ; i < t.length; i++){
//		        Root root = (Root) t[i].getLastPathComponent();
//	            root.detacheParent();
	            rm.detacheParent((Root) t[i].getLastPathComponent());
	    	}
            rm.repaint();
	    }
	    
	    private void findLaterals(){
	    	TreePath[] t = tree.getSelectionPaths();
	    	for(int i = 0 ; i < t.length; i++){
	            rm.selectRoot((Root) t[i].getLastPathComponent());
	            rm.findLaterals2();
	    	}
            rm.repaint();
	    }
	    
	    private void deleteMarks(){
	    	TreePath[] t = tree.getSelectionPaths();
	    	for(int i = 0 ; i < t.length; i++){
	            rm.selectRoot((Root) t[i].getLastPathComponent());
	            rm.getSelectedRoot().removeAllMarks(false);
	    	}
            rm.repaint();
	    }	    
	    
	    private void detachChildren(){
	    	TreePath[] t = tree.getSelectionPaths();
	    	for(int i = 0 ; i < t.length; i++){
		        Root root = (Root) t[i].getLastPathComponent();
		        int count = root.childList.size();
		        for(int j = 0 ; j < count ; j++){
//		        	Root c = root.childList.get(0);
//		        	c.detacheParent();
		        	rm.detacheParent(root.childList.get(0));
		        }
	    	}
            rm.repaint();
	    }
	    
	    public void refreshNodes() {
	    	rootListTree.tree.removeAll();
	    	rootListTree.tree.setModel(null);
	    	rootListTree.tree.setModel(rm);
	    	rootListTree.tree.setSelectionPaths(lastSelectedPath);
	    	if (lastSelectedPath.length == 1) marksTableModel.modelChanged(pRoot[lastSelectedPath.length-1]);
    		else marksTableModel.modelChanged(null);
	    }
	}


   /**
    * Class for the drag and drop functions of the tree
    */
	class TreeTransferHandler extends TransferHandler {
		private static final long serialVersionUID = 1L;
		DataFlavor nodesFlavor;
	    DataFlavor[] flavors = new DataFlavor[1];
	    Root[] nodesToRemove;

	    public TreeTransferHandler() {
	        try {
	            String mimeType = DataFlavor.javaJVMLocalObjectMimeType +";class=\"" + javax.swing.tree.DefaultMutableTreeNode[].class.getName() + "\"";
	            nodesFlavor = new DataFlavor(mimeType);
	            flavors[0] = nodesFlavor;
	        } catch(ClassNotFoundException e) {
	            System.out.println("ClassNotFound: " + e.getMessage());
	        }
	    }

	    public boolean canImport(TransferHandler.TransferSupport support) {
//	        if(!support.isDrop()) {
//	            return false;
//	        }
//	        support.setShowDropLocation(true);
//	        if(!support.isDataFlavorSupported(nodesFlavor)) {
//	            return false;
//	        }
	        // Do not allow a drop on the drag source selections.
	        JTree.DropLocation dl = (JTree.DropLocation)support.getDropLocation();
//	        JTree tree = (JTree)support.getComponent();
//	        int dropRow = tree.getRowForPath(dl.getPath());
//	        int[] selRows = tree.getSelectionRows();
//	        for(int i = 0; i < selRows.length; i++) {
//	            if(selRows[i] == dropRow) {
//	                return false;
//	            }
//	        }
	        // Do not allow MOVE-action drops if a non-leaf node is
	        // selected unless all of its children are also selected.
	        TreePath dest = dl.getPath();
	        Root target = (Root)dest.getLastPathComponent();
	        if(rm.getSelectedRoot() != null) rm.getSelectedRoot().setSelect(false);
	        rm.selectRoot(target);
	        target.setSelect(true);
	        rm.repaint();
	        for(int i=0; i < nodesToRemove.length; i++){
	        	if(nodesToRemove[i].childList.contains(target)) return false;
	        }
	        
//	        int action = support.getDropAction();
//	        if(action == MOVE) {
//	            return haveCompleteNode(tree);
//	        }
//	        // Do not allow a non-leaf node to be copied to a level
//	        // which is less than its source level.
//
//	        TreePath path = tree.getPathForRow(selRows[0]);
//	        Root firstNode = (Root)path.getLastPathComponent();
//	        SR.write("TRANFERS");
//	        if(firstNode.childList.size() > 0 &&
//	               target.getLevel() < firstNode.getLevel()) {
//	            return false;
//	        }
	        return true;
	    }

//	    private boolean haveCompleteNode(JTree tree) {
////	        int[] selRows = tree.getSelectionRows();
////	        TreePath path = tree.getPathForRow(selRows[0]);
////	        Root first = (Root) path.getLastPathComponent();
////	        int childCount = first.childList.size();
////	        // first has children and no children are selected.
////	        if(childCount > 0 && selRows.length == 1)
////	            return false;
////	        // first may have children.
////	        for(int i = 1; i < selRows.length; i++) {
////	            path = tree.getPathForRow(selRows[i]);
////	            Root next = (Root) path.getLastPathComponent();
////	            if(first.parent != null){
////	            	if(first.getParent().equals(next)) {
////	            		// Found a child of first.
////	            		if(childCount > selRows.length-1) {
////	            			// Not all children of first are selected.
////	            			return false;
////	                }
////	            	}
////	            }
////	        }
//	        return true;
//	    }

	    protected Transferable createTransferable(JComponent c) {
	        JTree tree = (JTree) c;
	        TreePath[] paths = tree.getSelectionPaths();
	        if(paths != null) {
	            // Make up a node array of copies for transfer and
	            // another for/of the nodes that will be removed in
	            // exportDone after a successful drop.
	            List<Root> copies = new ArrayList<Root>();
	            List<Root> toRemove = new ArrayList<Root>();
	            Root node = (Root) paths[0].getLastPathComponent();
	            Root copy = node;
	            copies.add(copy);
	            if(node.isChild() > 0) toRemove.add(node);
	            for(int i = 1; i < paths.length; i++) {
	                Root next = (Root) paths[i].getLastPathComponent();
	                next.setSelect(false);
//                	toRemove.add(next);
                // Do not allow higher level nodes to be added to list.
	                if(next.getLevel() < node.getLevel()) {
	                    break;
	                } else if(next.getLevel() > node.getLevel()) {  // child node
	                    copy.attachChild(next);
	                    // node already contains child
	                } else {                                        // sibling
	                    copies.add(next);
	                    if(node.isChild() > 0){
	                    	toRemove.add(next);
	                    }
	                }
	            }
	            Root[] nodes = copies.toArray(new Root[copies.size()]);
	            nodesToRemove = toRemove.toArray(new Root[toRemove.size()]);
	            return new NodesTransferable(nodes);
	        }
	        return null;
	    }

	    protected void exportDone(JComponent source, Transferable data, int action) {
	        nodesToRemove = null;
	    }

	    public int getSourceActions(JComponent c) {
	        return COPY_OR_MOVE;
	    }

	    public boolean importData(TransferHandler.TransferSupport support) {
	        if(!canImport(support)) {
	            return false;
	        }
	        // Extract transfer data.
	        Root[] nodes = null;
	        try {
	            Transferable t = support.getTransferable();
	            nodes = (Root[]) t.getTransferData(nodesFlavor);
	        } catch(UnsupportedFlavorException ufe) {
	            System.out.println("UnsupportedFlavor: " + ufe.getMessage());
	        } catch(java.io.IOException ioe) {
	            System.out.println("I/O error: " + ioe.getMessage());
	        }
	        // Get drop location info.
	        JTree.DropLocation dl = (JTree.DropLocation) support.getDropLocation();
	        TreePath dest = dl.getPath();
	        Root parent = (Root)dest.getLastPathComponent();
	        
	        // Add data to model.
	        for(int i = 0; i < nodes.length; i++) {
	        	rm.setParent(parent, nodes[i]);
	        	rootListTree.refreshNodes();
	        	rm.repaint();
	        }

	        return true;
	    }

	    public String toString() {
	        return getClass().getName();
	    }

	    public class NodesTransferable implements Transferable {
	        Root[] nodes;

	        public NodesTransferable(Root[] nodes) {
	            this.nodes = nodes;
	         }

	        public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException {
	            if(!isDataFlavorSupported(flavor)) throw new UnsupportedFlavorException(flavor);
	            return nodes;
	        }

	        public DataFlavor[] getTransferDataFlavors() {
	            return flavors;
	        }

	        public boolean isDataFlavorSupported(DataFlavor flavor) {
	            return nodesFlavor.equals(flavor);
	        }
	    }
	}
   

   /**
    * Class for the table containgin the linked data
    * @author guillaumelobet
    *
    */
   class LinkedDatafileTableModel extends AbstractTableModel {
      
	private static final long serialVersionUID = 1L;
	ArrayList<File> fileList = new ArrayList<File>();

      public LinkedDatafileTableModel() {}
      
    @SuppressWarnings("unchecked")
	public void modelChanged() {
         fileList.clear();
         if (rm != null) fileList.addAll(rm.linkedDatafileList.keySet());
         fireTableStructureChanged();
         }

      public Object getValueAt(int row, int col) {
         File f = (File) fileList.get(row);
         if (col == 0) return f.getName();
         else {
            boolean[] b = (boolean[]) rm.linkedDatafileList.get(f);
            return new Boolean(b[col - 1]);
            }
         }
      
      public boolean isCellEditable(int row, int col) {return (col > 0);}

      public void setValueAt(Object v, int row, int col) {
         File f = (File) fileList.get(row);
         boolean[] b = (boolean[]) rm.linkedDatafileList.get(f);
         b[col - 1] = ((Boolean)v).booleanValue();
         }

      public int getRowCount() {return fileList.size();}
         
      public int getColumnCount() {return Mark.getTypeCount();}
         
    @SuppressWarnings({ "unchecked", "rawtypes" })
	public Class getColumnClass(int col) {return (col == 0) ? String.class : Boolean.class;}
      
      public String getColumnName(int col) {return (col == 0) ? "Datafile" : "mark";}
      
      public void changeRowSelection(int row) {
         File f = (File) fileList.get(row);
         boolean[] b = (boolean[]) rm.linkedDatafileList.get(f);
         int i = b.length - 2;
         while (i >=0 && b[i]) i--;
         boolean newVal = (i >= 0);
         for (i = 0; i < b.length - 1; b[i++] = newVal);
         fireTableRowsUpdated(row, row);
         }

      public void changeColumnSelection(int col) {
         int row = fileList.size() - 1;
         while (row >=0) {
            File f = (File) fileList.get(row);
            boolean[] b = (boolean[]) rm.linkedDatafileList.get(f);
            if (!b[col - 1]) break;
            row--;
            }
         boolean newVal = (row >= 0);
         for (row = 0; row < fileList.size(); row++) {
            File f = (File) fileList.get(row);
            boolean[] b = (boolean[]) rm.linkedDatafileList.get(f);
            b[col - 1] = newVal;
            }
         fireTableDataChanged();
         }
      }
   

   
   /**
    * Table showing the different marks of a root
    * @author guillaume
    *
    */
   class MarksTableModel extends AbstractTableModel {
		private static final long serialVersionUID = 1L;
		ArrayList<Mark> markList = new ArrayList<Mark>();

	      public MarksTableModel() {
	      }
	      
	      public void modelChanged(Root r) {
	         markList.clear();
	         if(r != null){
	        	 int j = 0;
		         for(int i = 0 ; i < r.markList.size(); i++){
		        	 if(r.markList.get(i).type == Mark.FREE_TEXT || r.markList.get(i).type == Mark.NUMBER 
		        			 || r.markList.get(i).type == Mark.LENGTH || r.markList.get(i).type == Mark.MEASURE){
		        		 markList.add(j, r.markList.get(i));
		        		 j++;
		        	 }
		         }
	         }
	         fireTableStructureChanged();
	         }

	      public Object getValueAt(int row, int col) {
	         Mark m = (Mark) markList.get(row);
	         if (col == 0) return m.isForeign ? m.foreignImgName : rm.imgName;
	         else if(col == 1) return Mark.typeName[m.type];
	         else if (col == 2) return m.value;
	         else return m.lPos * rm.pixelSize;
	      }
	      
	      public Mark getSelectedMark(int row){
	    	  return (Mark) markList.get(row);
	      }
	      
	      public boolean isCellEditable(int row, int col) {return col == 2;}

	      public void setValueAt(Object v, int row, int col) {
	          Mark m = (Mark) markList.get(row);
	          m.value = v.toString();
	          rm.repaint();
	         }
	      
	      public int getRowCount() {return markList.size();}
	         
	      public int getColumnCount() {return 4;}
	         
	      @SuppressWarnings({ "unchecked", "rawtypes" })
		public Class getColumnClass(int col) {
	    	  if (col != 3) return  String.class;
	    	  else return Float.class;
	    	  }
	      
	      public String getColumnName(int col) {
		         if (col == 0) return "Source";
		         else if (col == 1) return "Mark";
		         else if (col == 2) return "Value";
		         else return "LPos";	
	      }
	}
   
   /**
    * Create icons for the tables
    * @author guillaumelobet
    *
    */
   class IconHeaderRenderer extends JLabel implements TableCellRenderer {

	private static final long serialVersionUID = 1L;

	public Component getTableCellRendererComponent(JTable table, Object value, 
                                                     boolean isSelected, boolean hasFocus,
                                                     int row, int col) {
         if (col == 0) {
            setIcon(null);
            setText("Datafile");
            }
         else {
            setIcon(Mark.getIcon(col - 1));
            setText(null);
            }
         setHorizontalAlignment(SwingConstants.CENTER);
         setBorder(BorderFactory.createMatteBorder(0,0,1,0,table.getForeground()));
         return this;
         }
      }
   
   // Plot tools
   
   /**
    * The class used to created charts of the root data
    */
   public class Chart extends JFrame {
	   
	private static final long serialVersionUID = 1L;

	public Chart(String title, XYSeries data, Color color, boolean scatter) {
	       super(title);    
	       org.jfree.chart.renderer.xy.XYBarRenderer.setDefaultShadowsVisible(false);        
	       this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
	       IntervalXYDataset dataset = createDataset(data);
	       JFreeChart chart = createChart(dataset, title, false, scatter, color);
	       ChartPanel chartPanel = new ChartPanel(chart);
	       chartPanel.setPreferredSize(new java.awt.Dimension(500, 270));
	       chartPanel.setMouseZoomable(true, false);
	       setContentPane(chartPanel);
	   }
   
	   public Chart(String[] title, int bit, XYSeries data1, XYSeries data2, XYSeries data3, Color color, boolean scatter ) {
	       super(title[0]);    
	       this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
	       IntervalXYDataset dataset = createDataset(data1, data2, data3);
	       JFreeChart chart = createChart(dataset, title[0], true, scatter, color);
	       ChartPanel chartPanel = new ChartPanel(chart);
	       chartPanel.setPreferredSize(new java.awt.Dimension(500, 270));
	       chartPanel.setMouseZoomable(true, false);
	       setContentPane(chartPanel);
	   }
	   
	   public Chart() {
		   super("empty");
	   }
	   
	   private IntervalXYDataset createDataset(XYSeries data) {
		   
	       XYSeriesCollection dataset = new XYSeriesCollection();
	       dataset.addSeries(data);
	       return dataset;     
	   }
	   
	   private IntervalXYDataset createDataset(XYSeries data1, XYSeries data2, XYSeries data3) {
		   
	       XYSeriesCollection dataset = new XYSeriesCollection();
	       dataset.addSeries(data1);
	       dataset.addSeries(data2);
	       dataset.addSeries(data3);
	       return dataset;     
	   }
   
	   private JFreeChart createChart(IntervalXYDataset dataset, String title, boolean l, boolean scatter, Color color) {
	       XYBarRenderer.setDefaultShadowsVisible(false);
	       JFreeChart chart;
	       if(scatter) {
	    	   chart = ChartFactory.createScatterPlot(
	           title, 
	           null, 
	           null, 
	           dataset, 
	           PlotOrientation.VERTICAL, 
	           l, 
	           false, 
	           false
	    	   );
	       }
	       else{
	    	    chart = ChartFactory.createXYLineChart(
		           title, 
		           null, 
		           null, 
		           dataset, 
		           PlotOrientation.VERTICAL, 
		           l, 
		           false, 
		           false
		    	   );
	       }
	       chart.getXYPlot().setForegroundAlpha(0.75f);
	        chart.setBackgroundPaint(Color.white);   
	        XYPlot plot = chart.getXYPlot();
	        plot.setBackgroundPaint(Color.white);
	        plot.setDomainGridlinePaint(Color.black);
	        plot.setRangeGridlinePaint(Color.black);        
	        plot.getRenderer().setSeriesPaint(0, color);
	        
	       return chart;
	   }
   }
   
   /**
    * The class used to created histograms of the root data
    */
   public class Histogram extends JFrame {
	   
	private static final long serialVersionUID = 1L;

	public Histogram(String title, int bit, double[] data, Color color) {
	       super(title);    
	       org.jfree.chart.renderer.xy.XYBarRenderer.setDefaultShadowsVisible(false);        
	       this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
	       IntervalXYDataset dataset = createDataset(bit, data, title);
	       JFreeChart chart = createChart(dataset, title, false, color);
	       ChartPanel chartPanel = new ChartPanel(chart);
	       chartPanel.setPreferredSize(new java.awt.Dimension(500, 270));
	       chartPanel.setMouseZoomable(true, false);
	       setContentPane(chartPanel);
	   }
   
	   public Histogram(String[] title, int bit, double[] data1, double[] data2, Color color ) {
	       super(title[0]);    
	       this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
	       IntervalXYDataset dataset = createDataset(bit, data1, data2, title);
	       JFreeChart chart = createChart(dataset, title[0], true, color);
	       ChartPanel chartPanel = new ChartPanel(chart);
	       chartPanel.setPreferredSize(new java.awt.Dimension(500, 270));
	       chartPanel.setMouseZoomable(true, false);
	       setContentPane(chartPanel);
	   }
	   
	   public Histogram() {
		   super("empty");
	   }
	   
	   private IntervalXYDataset createDataset(int bit, double[] data, String title) {
		   
	       HistogramDataset dataset = new HistogramDataset();
	       if(!absV) dataset.setType(HistogramType.RELATIVE_FREQUENCY);
	       else dataset.setType(HistogramType.FREQUENCY);
	       dataset.addSeries(title, data, bit);
	       return dataset;     
	   }
	   
	   private IntervalXYDataset createDataset(int bit, double[] data1, double[] data2, String[] title) {
		   
	       HistogramDataset dataset = new HistogramDataset();
	       dataset.setType(HistogramType.RELATIVE_FREQUENCY);
	       dataset.addSeries(title[1], data1, bit);
	       dataset.addSeries(title[2], data2, bit);
	       return dataset;     
	   }
   
	   private JFreeChart createChart(IntervalXYDataset dataset, String title, boolean l, Color color) {
	       XYBarRenderer.setDefaultShadowsVisible(false);        
	       JFreeChart chart = ChartFactory.createHistogram(
	           title, 
	           null, 
	           null, 
	           dataset, 
	           PlotOrientation.VERTICAL, 
	           l, 
	           false, 
	           false
	       );
	       chart.getXYPlot().setForegroundAlpha(0.75f);
	        chart.setBackgroundPaint(Color.white);   
	        XYPlot plot = chart.getXYPlot();
	        plot.setBackgroundPaint(Color.white);
	        plot.setDomainGridlinePaint(Color.black);
	        plot.setRangeGridlinePaint(Color.black);
	        
	        XYBarRenderer renderer = (XYBarRenderer) plot.getRenderer();
	        renderer.setShadowVisible(false);
	        renderer.setBarPainter(new StandardXYBarPainter());
	        renderer.setSeriesPaint(0, color);
	       return chart;
	   }
   }
   
 
   
   /**
    * Get the data for the histogram of the secondary root insertion angles
    * @return an array of secondary root insertion angles
    */
   private double[] getInsHistData(){
	   int n = 0;
	   for(int i =0 ; i < rm.rootList.size(); i ++){
		   Root r = (Root) rm.rootList.get(i);
		   if (r.getInsertAngl() != 0) n++;
	   }
	   double[] data = new double[n];
	   n = 0;
	   for(int i =0 ; i < rm.rootList.size(); i ++){
		   Root r = (Root) rm.rootList.get(i);
		   if(r.getInsertAngl() == 0) n++;
		   else data[i -n] = r.getInsertAngl() * (180 / (float) Math.PI);
	   }
	   return data;
   }
   
   /**
    * Get the data for the histogram of the secondary root inter branch distances
    * @return an array of inter-branch distances
    */
   private double[] getInterHistData(){
	   int n = 0;
	   for(int i =0 ; i < rm.rootList.size(); i ++){
		   Root r = (Root) rm.rootList.get(i);
		   if (r.getInterBranch() != 0) n++;
	   }
	   double[] data = new double[n];
	   n = 0;
	   for(int i =0 ; i < rm.rootList.size(); i ++){
		   Root r = (Root) rm.rootList.get(i);
		   if (r.getInterBranch() == 0) n++;
		   else data[i - n] = r.getInterBranch() * rm.pixelSize;
	   }
	   return data;
   }
   
   /**
    * Get the data for the histogram of all the root diameters
    * @return an array of all nodes diameters
    */
   private double[] getDiamHistData(){
	   int m = 0;
	   Root r;
	   for(int i =0 ; i < rm.rootList.size(); i ++){
		   r = (Root) rm.rootList.get(i);
		   Node n =r.firstNode;
		   m++;
		   while (n.child != null){
			   n = n.child;
			   m++;
		   }
	   }
	   double[] data = new double[m];
	   m = 0;
	   for(int i =0 ; i < rm.rootList.size(); i ++){
		   r = (Root) rm.rootList.get(i);
		   Node n =r.firstNode;
		   data[m] = n.diameter * rm.pixelSize;
		   m++;
		   while (n.child != null){
			   n = n.child;
			   data[m] = n.diameter * rm.pixelSize;
			   m++;
		   }
	   }
	   return data;
   }
   
   /**
    * Get the data for the histogram of the primary root diameters
    * @return an array of primary nodes diameters
    */
   private double[] getDiamPHistData(){
	   int m = 0;
	   for(int i =0 ; i < rm.rootList.size(); i ++){
		   Root r = (Root) rm.rootList.get(i);
		   if (r.isChild() == 0){
			   Node n =r.firstNode;
			   m++;
			   while (n.child != null){
				   n = n.child;
				   m++;
			   }
		   }
	   }
	   double[] data = new double[m];
	   m = 0;
	   for(int i =0 ; i < rm.rootList.size(); i ++){
		   Root r = (Root) rm.rootList.get(i);
		   if (r.isChild() == 0){
			   Node n =r.firstNode;
			   data[m] = n.diameter * rm.pixelSize;
			   m++;
			   while (n.child != null){
				   n = n.child;
				   data[m] = n.diameter * rm.pixelSize;
				   m++;
			   }
		   }
	   }
	   return data;
   }
   
   /**
    * Get the data for the histogram of the secondary root diameters
    * @return an array of secondary nodes diameters
    */
   private double[] getDiamSHistData(){
	   int m = 0;
	   for(int i =0 ; i < rm.rootList.size(); i ++){
		   Root r = (Root) rm.rootList.get(i);
		   if(r.isChild() != 0){
			   Node n =r.firstNode;
			   m++;
			   while (n.child != null){
				   n = n.child;
				   m++;
			   }
		   }
	   }
	   double[] data = new double[m];
	   m = 0;
	   for(int i =0 ; i < rm.rootList.size(); i ++){
		   Root r = (Root) rm.rootList.get(i);
		   if (r.isChild() != 0){
			   Node n =r.firstNode;
			   data[m] = n.diameter * rm.pixelSize;
			   m++;
			   while (n.child != null){
				   n = n.child;
				   data[m] = n.diameter * rm.pixelSize;
				   m++;
			   }
		   }
	   }
	   return data;
   }
   
   /**
    * Get the data for the plot "lateral length vs parent position" 
    * @param r the root name
    * @return XYSeries paramter set
    */
   
   private XYSeries getLatLengthData(String r){
	   XYSeries data = new XYSeries("");
	   Root pr = (Root) rm.rootList.get(0);
	   Root cr;
	   for (int i = 0 ; i < rm.rootList.size(); i++){
		   pr = (Root) rm.rootList.get(i);
		   if (pr.getRootID() == r){
			   break;
		   }
	   }
	   for(int i = 0 ; i < pr.childList.size();  i++){
		   cr = (Root) pr.childList.get(i);
		   data.add(cr.distanceFromBase * rm.pixelSize, cr.getRootLength() * rm.pixelSize);
	   }
	   
	   return data;
   }
   
   
   /**
    * Get the data for the plot "interBranch length vs parent position" 
    * @param r the root name
    * @return XYSeries parameter set
    */
   
   private XYSeries getInterLengthData(String r){
	   XYSeries data = new XYSeries("");
	   Root pr = (Root) rm.rootList.get(0);
	   Root cr;
	   for (int i = 0 ; i < rm.rootList.size(); i++){
		   pr = (Root) rm.rootList.get(i);
		   if (pr.getRootID() == r){
			   break;
		   }
	   }
	   pr.updateChildren();
	   for(int i = 0 ; i < pr.childList.size();  i++){
		   cr = (Root) pr.childList.get(i);
		   data.add(cr.getDistanceFromBase() * rm.pixelSize, cr.getInterBranch() * rm.pixelSize);
	   }
	   
	   return data;
   }
   
   /**
    * Get the data for the plot "angle length vs parent position" 
    * @param r the root name
    * @return XYSeries parameter set
    */
   
   private XYSeries getAngLengthData(String r){
	   XYSeries data = new XYSeries("");
	   Root pr = (Root) rm.rootList.get(0);
	   for (int i = 0 ; i < rm.rootList.size(); i++){
		   pr = (Root) rm.rootList.get(i);
		   if (pr.getRootID() == r){
			   break;
		   }
	   }
	   Node n = pr.firstNode;
	   float der = 0;
	   while (n.child.child != null){
		   n = n.child;
		   der = (n.theta - n.parent.theta) / (n.cLength - n.parent.cLength);
		   data.add(n.cLength * rm.pixelSize, der);
	   }
	   
	   return data;
   }
   /**
    * Set the select status of a root. A selected root will be displayed in red (nodes, axes, area and borders)
    * @param n the name of the root
    * @param t true if selected, false if not
    */
   private void setSelected(String n, boolean t){
 	  Root r;
	  for(int i = 0 ; i< rm.rootList.size(); i++){
		  r = (Root) rm.rootList.get(i);
		  r.setSelect(false);
		  if (n == r.getRootID()){
			  r.setSelect(t);
		  }
	  }   
   }
   
   /**
    * This method allow the user to plot several information about the current traced roots
    */ 
   private void plot(){
	   
 	  if(b1){
		  if(hist1.isVisible() && !reP){
			  SR.prefs.putInt("hist1.X", hist1.getLocation().x);
			  SR.prefs.putInt("hist1.Y", hist1.getLocation().y);
			  hist1.setVisible(false);
		  }
		  hist1 = new Histogram("Insertion angles histogram of "+rm.imgName, Integer.valueOf(plotTextField1.getText()), getInsHistData(), Color.BLUE);
		  hist1.pack();
		  int x = SR.prefs.getInt("hist1.X", 700);
		  int y = SR.prefs.getInt("hist1.Y", 50);
		  hist1.setLocation(x, y);
          hist1.setVisible(true);    		  
	  }
	  else if (!b1 && hist1.isVisible() && !reP){
		  SR.prefs.putInt("hist1.X", hist1.getLocation().x);
		  SR.prefs.putInt("hist1.Y", hist1.getLocation().y);
		  hist1.setVisible(false);
	  }

	  if(b2){
		  if(hist2.isVisible() && !reP){
			  SR.prefs.putInt("hist2.X", hist2.getLocation().x);
			  SR.prefs.putInt("hist2.Y", hist2.getLocation().y);
			  hist2.setVisible(false);
		  }
		  hist2 = new Histogram("Root diameter histogram", 
				  Integer.valueOf(plotTextField2.getText()), getDiamHistData(),Color.RED);
		  hist2.pack();
		  int x = SR.prefs.getInt("hist2.X", 700);
		  int y = SR.prefs.getInt("hist2.Y", 100);
		  hist2.setLocation(x, y);
	      hist2.setVisible(true);
	  }
	  else if (!b2 && hist2.isVisible() && !reP){
		  SR.prefs.putInt("hist2.X", hist2.getLocation().x);
		  SR.prefs.putInt("hist2.Y", hist2.getLocation().y);
		  hist2.setVisible(false);
	  }
	  
	  if(b3){
		  if(hist3.isVisible() && !reP){
			  SR.prefs.putInt("hist3.X", hist3.getLocation().x);
			  SR.prefs.putInt("hist3.Y", hist3.getLocation().y);
			  hist3.setVisible(false);
		  }
		  hist3 = new Histogram("Primary roots diameter histogram of "+rm.imgName, 
				  Integer.valueOf(plotTextField4.getText()), getDiamPHistData(),Color.RED);
		  hist3.pack();
		  int x = SR.prefs.getInt("hist3.X", 700);
		  int y = SR.prefs.getInt("hist3.Y", 150);
		  hist3.setLocation(x, y);
	      hist3.setVisible(true);
	  }
	  else if (!b3 && hist3.isVisible() && !reP){
		  SR.prefs.putInt("hist3.X", hist3.getLocation().x);
		  SR.prefs.putInt("hist3.Y", hist3.getLocation().y);
		  hist3.setVisible(false);
	  }
	  
	  if(b4){
		  if(hist4.isVisible() && !reP){
			  SR.prefs.putInt("hist4.X", hist4.getLocation().x);
			  SR.prefs.putInt("hist4.Y", hist4.getLocation().y);
			  hist4.setVisible(false);
		  }
		  hist4 = new Histogram("Secondary roots diameter histogram of "+rm.imgName, 
				  Integer.valueOf(plotTextField3.getText()), getDiamSHistData(),Color.RED);
		  hist4.pack();
		  int x = SR.prefs.getInt("hist4.X", 700);
		  int y = SR.prefs.getInt("hist4.Y", 200);
		  hist4.setLocation(x, y);
	      hist4.setVisible(true);
	  }
	  else if (!b4 && hist4.isVisible() && !reP){
		  SR.prefs.putInt("hist4.X", hist4.getLocation().x);
		  SR.prefs.putInt("hist4.Y", hist4.getLocation().y);
		  hist4.setVisible(false);
	  }
	  
	  if(b5){
		  if(hist5.isVisible() && !reP){
			  SR.prefs.putInt("hist5.X", hist5.getLocation().x);
			  SR.prefs.putInt("hist5.Y", hist5.getLocation().y);
			  hist5.setVisible(false);
		  }
		  String[] list = {"Root diameter histogram", "Prim", "Sec"};
		  hist5 = new Histogram(list, Integer.valueOf(plotTextField5.getText()),
				  getDiamPHistData(), getDiamSHistData(), null);
		  hist5.pack();
		  int x = SR.prefs.getInt("hist5.X", 700);
		  int y = SR.prefs.getInt("hist5.Y", 250);
		  hist5.setLocation(x, y);
	      hist5.setVisible(true);
	  }
	  else if (!b5 && hist5.isVisible() && !reP){
		  SR.prefs.putInt("hist5.X", hist5.getLocation().x);
		  SR.prefs.putInt("hist5.Y", hist5.getLocation().y);
		  hist5.setVisible(false);
	  }    	  
	  if(b6){
		  if(hist6.isVisible() && !reP){
			  SR.prefs.putInt("hist6.X", hist6.getLocation().x);
			  SR.prefs.putInt("hist6.Y", hist6.getLocation().y);
			  hist6.setVisible(false);
		  }
		  hist6 = new Histogram("Root interbranch histogram of "+rm.imgName,
				  Integer.valueOf(plotTextField6.getText()), getInterHistData(), Color.GREEN);
		  hist6.pack();
		  int x = SR.prefs.getInt("hist6.X", 700);
		  int y = SR.prefs.getInt("hist6.Y", 300);
		  hist6.setLocation(x, y);
	      hist6.setVisible(true);
	  }
	  else if (!b6 && hist6.isVisible() && !reP){
		  SR.prefs.putInt("hist6.X", hist6.getLocation().x);
		  SR.prefs.putInt("hist6.Y", hist6.getLocation().y);
		  hist6.setVisible(false);
	  }    	  
	  if(b7){   		  
          if(g1.isVisible() && !reP){
    		  SR.prefs.putInt("g1.X", g1.getLocation().x);
    		  SR.prefs.putInt("g1.Y", g1.getLocation().y);
        	  g1.setVisible(false);
          }
          g1 = new Chart("Lateral length vs position on parent", 
        		  getLatLengthData((String)plotComboBox1.getSelectedItem()), Color.BLUE, true);
          g1.pack();
		  int x = SR.prefs.getInt("g1.X", 700);
		  int y = SR.prefs.getInt("g1.Y", 350);
		  g1.setLocation(x, y);
	      g1.setVisible(true);
	  }
	  else if (!b7 && g1.isVisible() && !reP){
		  SR.prefs.putInt("g1.X", g1.getLocation().x);
		  SR.prefs.putInt("g1.Y", g1.getLocation().y);
		  g1.setVisible(false);
	  }     
	  if(b8){   		  
          if(g2.isVisible() && !reP){
    		  SR.prefs.putInt("g2.X", g2.getLocation().x);
    		  SR.prefs.putInt("g2.Y", g2.getLocation().y);
        	  g2.setVisible(false);
          }
          g2 = new Chart("Interbranch length vs position on parent", 
        		  getInterLengthData((String)plotComboBox1.getSelectedItem()), Color.GREEN, false);
          g2.pack();
		  int x = SR.prefs.getInt("g2.X", 700);
		  int y = SR.prefs.getInt("g2.Y", 350);
		  g2.setLocation(x, y);
	      g2.setVisible(true);
	  }
	  else if (!b8 && g2.isVisible() && !reP){
		  SR.prefs.putInt("g2.X", g2.getLocation().x);
		  SR.prefs.putInt("g2.Y", g2.getLocation().y);
		  g2.setVisible(false);
	  } 
	  if(b9){   		  
          if(g3.isVisible() && !reP){
    		  SR.prefs.putInt("g3.X", g3.getLocation().x);
    		  SR.prefs.putInt("g3.Y", g3.getLocation().y);
    		  g3.setVisible(false);
          }
          g3 = new Chart("Direction vs position on parent", 
        		  getAngLengthData((String)plotComboBox2.getSelectedItem()), Color.ORANGE, false);
          g3.pack();
		  int x = SR.prefs.getInt("g3.X", 700);
		  int y = SR.prefs.getInt("g3.Y", 350);
		  g3.setLocation(x, y);
		  g3.setVisible(true);
	  }
	  else if (!b9 && g3.isVisible() && !reP){
		  SR.prefs.putInt("g3.X", g3.getLocation().x);
		  SR.prefs.putInt("g3.Y", g3.getLocation().y);
		  g3.setVisible(false);
	  } 
   }
   
   /**
    * Close all the plots
    */
   private void closePlot(){
	   if(hist1.isVisible()){
 		  SR.prefs.putInt("hist1.X", hist1.getLocation().x);
 		  SR.prefs.putInt("hist1.Y", hist1.getLocation().y);
 		  hist1.setVisible(false);
 	  }
 	  if(hist2.isVisible()){
 		  SR.prefs.putInt("hist2.X", hist2.getLocation().x);
 		  SR.prefs.putInt("hist2.Y", hist2.getLocation().y);
 		  hist2.setVisible(false);
 	  }
 	  if(hist3.isVisible()){
 		  SR.prefs.putInt("hist3.X", hist3.getLocation().x);
 		  SR.prefs.putInt("hist3.Y", hist3.getLocation().y);
 		  hist3.setVisible(false);
 	  }
 	  if(hist4.isVisible()){
 		  SR.prefs.putInt("hist4.X", hist4.getLocation().x);
 		  SR.prefs.putInt("hist4.Y", hist4.getLocation().y);
 		  hist4.setVisible(false);
 	  }
 	  if(hist5.isVisible()){
 		  SR.prefs.putInt("hist5.X", hist5.getLocation().x);
 		  SR.prefs.putInt("hist5.Y", hist5.getLocation().y);
 		  hist5.setVisible(false);
 	  }
 	  if(hist6.isVisible()){
 		  SR.prefs.putInt("hist6.X", hist6.getLocation().x);
 		  SR.prefs.putInt("hist6.Y", hist6.getLocation().y);
 		  hist6.setVisible(false);
 	  }
 	  if(g1.isVisible()){
 		  SR.prefs.putInt("g1.X", g1.getLocation().x);
 		  SR.prefs.putInt("g1.Y", g1.getLocation().y);
 		  g1.setVisible(false);
 	  }
 	  if(g2.isVisible()){
 		  SR.prefs.putInt("g2.X", g2.getLocation().x);
 		  SR.prefs.putInt("g2.Y", g2.getLocation().y);
 		  g2.setVisible(false);
 	  }
 	  if(g3.isVisible()){
 		  SR.prefs.putInt("g3.X", g3.getLocation().x);
 		  SR.prefs.putInt("g3.Y", g3.getLocation().y);
 		  g3.setVisible(false);
 	  }
   }
   
   /**
    * Apply the action define in the root list tab
    */
   private void applyRootListActions(){
	   String a = (String) action.getSelectedItem();
		
		if(a == "Attach parent"){
			rootListTree.attachParent(true);
			rootListTree.refreshNodes();
		} 
		if(a == "Delete root(s)"){
			rootListTree.deleteRoot();
			rootListTree.refreshNodes();
			cancel.setEnabled(false);
			ok.setEnabled(false);
		} 
		if(a == "Rename root"){
			rootListTree.renameRoot();
			rootListTree.refreshNodes();
		}
		
		if(a == "Detach parent"){
			rootListTree.detachParent();
			rootListTree.refreshNodes();
		}
		if(a == "Detach child(ren)"){
			rootListTree.detachChildren();
			rootListTree.refreshNodes();
		}
		if(a == "Find laterals"){
			rootListTree.findLaterals();
			rootListTree.refreshNodes();
		}
		if(a == "Delete all marks"){
			rootListTree.deleteMarks();
			rootListTree.refreshNodes();
		}		
		if(a == "Delete mark(s)"){
			int[] rows = marksTable.getSelectedRows();
			Root r = marksTableModel.getSelectedMark(rows[0]).r;
			for (int i  = 0 ; i < rows.length ; i++){
				Mark m = marksTableModel.getSelectedMark(rows[i]);
				r.removeMark(m);
			}
			rm.repaint();
			infoPane.setText(rm.displayRootInfo(r));
			marksTableModel.modelChanged(r);
		}
	} 


   /**
    * Transfers the data to the SQL database
    * @param sql
    * @param csv
    * @param image
    */
   private void transfersData(boolean sql, boolean csv, boolean image){
	   
	   if(image){
           rm.exportImage(imageFolderName.getText(), colorJCB.getSelectedIndex() == 0, 
        		   (String) imageJCB.getSelectedItem(), Integer.valueOf(lineWidth.getText()), null, imageRealWidth.isSelected(), false);
	   }
	   
	   if(csv){       
		   PrintWriter pw = null;
		   String file = csvFolderName.getText();
           try{ pw = new PrintWriter(new FileWriter(file)); }
           catch(IOException e){SR.write("Could not save file "+file);}
    	   int sel = csvJCB.getSelectedIndex();
    	   boolean header = csvHeader.isSelected();
           switch (sel) {
              case 0: rm.csvSendRoots(pw, header); break;
              case 1: rm.csvSendMarks(pw, header); break;
              case 2: rm.csvSendNodes(pw, header); break;
//              case 3:
//                  Rectangle2D.Double bounds = new Rectangle2D.Double(0, 0, rm.ip.getWidth(), rm.ip.getHeight());
//                  RLDGridSize gs = new RLDGridSize(10.0, 10.0, 0.4);
//                  int rc = RLDSettingsDialog.showDialog(this, bounds, gs);
//                  if (rc == RLDSettingsDialog.CANCEL) break;
//                  RLDProfile2D rldp2D = new RLDProfile2D();
//                  SR.write("  Bounding rectangle [pixels] x:" + bounds.getX() + " y:" + bounds.getY() 
//                            + " w:" + bounds.getWidth() + " h:" + bounds.getHeight());
//                  SR.write("  Grid cell size [cm] x:" + gs.gridx + " y:" + gs.gridy);
//                  SR.write("  Thickness [cm]: " + gs.thickness);
//                  rldp2D.computeRLP(rm.rootList, gs, bounds, rm.pixelSize);
//                  rm.csvSendRLD(pw, rldp2D, header);             
//                  break;
              case 3: rm.csvSendCoodrinates(pw, header); break;
              case 4: rm.csvSendGrowthRate(pw, header); break;
              case 5: rm.csvLabExport(pw, header); break;
           }
           
	   } 
	   
	   if(sql){
		   int sel = sqlJCB.getSelectedIndex();
		   boolean create = sqlCreate.isSelected();
		   
	       switch (sel) {
	          case 0: rm.sqlSendRoots(sqlTableName.getText(), create); break;
	          case 1: rm.sqlSendMarks(sqlTableName.getText(), create); break;
	          case 2: rm.sqlSendNodes(sqlTableName.getText(), create); break;
//	          case 3:
//	              Rectangle2D.Double bounds = new Rectangle2D.Double(0, 0, rm.ip.getWidth(), rm.ip.getHeight());
//	              RLDGridSize gs = new RLDGridSize(10.0, 10.0, 0.4);
//	              int rc = RLDSettingsDialog.showDialog(this, bounds, gs);
//	              if (rc == RLDSettingsDialog.CANCEL) break;
//	              RLDProfile2D rldp2D = new RLDProfile2D();
//	              SR.write("  Bounding rectangle [pixels] x:" + bounds.getX() + " y:" + bounds.getY() 
//	                        + " w:" + bounds.getWidth() + " h:" + bounds.getHeight());
//	              SR.write("  Grid cell size [cm] x:" + gs.gridx + " y:" + gs.gridy);
//	              SR.write("  Thickness [cm]: " + gs.thickness);
//	              rldp2D.computeRLP(rm.rootList, gs, bounds, rm.pixelSize);
//	              
//	              if(sql)rm.sqlSendRLD(sqlTableName.getText(), rldp2D, create); break;
	          case 3: rm.sqlSendGrowthRates(sqlTableName.getText(), create); break;
	          case 4: rm.sqlLabExport(sqlTableName.getText(), create); break;
	       }
	   }
   }
   
  
  /**
   * Reset the lateral setting preferences
   */
   private void resetPreferences() {
 	  maxAngleField.setText(Float.toString(FCSettings.MAX_ANGLE));
	  nStepField.setText(Float.toString(FCSettings.N_STEP));	
	  minNSizeField.setText(""+FCSettings.MIN_NODE_SIZE);
	  maxNSizeField.setText(""+FCSettings.MAX_NODE_SIZE);	
	  minRSizeField.setText(""+FCSettings.MIN_ROOT_SIZE);
      checkNSizeBox.setSelected(FCSettings.CHECK_N_SIZE);
      checkRDirBox.setSelected(FCSettings.CHECK_R_DIR);
      checkRSizeBox.setSelected(FCSettings.CHECK_R_SIZE);
	}


   /**
    * Change the lateral settings preferences
    */
	private void changePreferences() {
	  FCSettings.nStep = Integer.parseInt(nStepField.getText());
	  FCSettings.maxAngle = Float.parseFloat(maxAngleField.getText());
	  FCSettings.minNodeSize = Double.parseDouble(minNSizeField.getText());
	  FCSettings.maxNodeSize = Double.parseDouble(maxNSizeField.getText());
	  FCSettings.minRootSize = Double.parseDouble(minRSizeField.getText());
	  FCSettings.checkNSize = checkNSizeBox.isSelected();
	  FCSettings.checkRDir = checkRDirBox.isSelected();
	  FCSettings.checkRSize = checkRSizeBox.isSelected();
	  FCSettings.autoFind = autoFindLat.isSelected();
	  FCSettings.useBinaryImg = useBinary.isSelected();
	  FCSettings.globalConvex = globalConvexHullBox.isSelected();

	  SR.prefs.putInt("nStep", Integer.parseInt(nStepField.getText()));
	  SR.prefs.putFloat("maxAngle", Float.parseFloat(maxAngleField.getText()));
	  SR.prefs.putDouble("minNSize", Double.parseDouble(minNSizeField.getText()));
	  SR.prefs.putDouble("maxNSize", Double.parseDouble(maxNSizeField.getText()));
	  SR.prefs.putDouble("minRSize", Double.parseDouble(minRSizeField.getText()));
	  SR.prefs.putBoolean("checkNSize", checkNSizeBox.isSelected());
	  SR.prefs.putBoolean("checkRDir", checkRDirBox.isSelected());
	  SR.prefs.putBoolean("checkRSize", checkRSizeBox.isSelected());	
	  SR.prefs.putBoolean("autoFind", autoFindLat.isSelected());	
	  SR.prefs.putBoolean("useBinary", useBinary.isSelected());	
	  SR.prefs.putBoolean("globalConvex", globalConvexHullBox.isSelected());	
	  
	}
	 
	/**
	 * Change the DPI value of the root model
	 * @param def
	 */
	private void changeDPI(boolean def){ 
	  if(dpiBox.isSelected()) {
		  float dpi = Float.parseFloat(DPIValue.getText());
		  if(def) SR.prefs.putFloat("DPI_default", dpi);		  
		  if(rm != null) rm.setDPI(dpi);
		  cmValue.setText(""+1);
		  if(unitsList.getSelectedIndex() == 0){
			  pixValue.setText(""+(dpi / 2.54f));
		  }
		  if(unitsList.getSelectedIndex() == 1){
			  pixValue.setText(""+(dpi / 25.4f));
		  }
		  if(unitsList.getSelectedIndex() == 2){
			  pixValue.setText(""+(dpi));
		  }
	  }
	  if(cmBox.isSelected()){
		  float scale = (Float.parseFloat(pixValue.getText()) / Float.parseFloat(cmValue.getText()));
		  if(unitsList.getSelectedIndex() == 0){
			  if(rm != null) rm.setDPI(scale * 2.54f);
			  DPIValue.setText(""+(scale * 2.54f));
			  if(def) SR.prefs.putFloat("DPI_default", (scale * 2.54f));		  			  
		  }
		  if(unitsList.getSelectedIndex() == 1){
			  if(rm != null) rm.setDPI(scale * 25.4f);
			  DPIValue.setText(""+(scale * 25.4f));
			  if(def) SR.prefs.putFloat("DPI_default", (scale * 2.54f));		  			  			  
		  }
		  if(unitsList.getSelectedIndex() == 2){
			  if(rm != null) rm.setDPI(scale);
			  DPIValue.setText(""+(scale));
			  if(def) SR.prefs.putFloat("DPI_default", scale);		  			  			  
		  }
	  }
	}
	
	
	/**
	 * Refresh the scale based on the new DPI values
	 */
	private void refreshScale(){
		  if(dpiBox.isSelected()) {
			  if(unitsList.getSelectedIndex() == 0){
				  pixValue.setText(""+(rm.getDPI() / 2.54f));
			  }
			  if(unitsList.getSelectedIndex() == 1){
				  pixValue.setText(""+(rm.getDPI() / 25.4f));
			  }
			  if(unitsList.getSelectedIndex() == 2){
				  pixValue.setText(""+(rm.getDPI()));
			  }
			  previousUnit = unitsList.getSelectedIndex();
		  }
		  if(cmBox.isSelected()){
			  
			  float scale = Float.parseFloat(pixValue.getText());
			  if(unitsList.getSelectedIndex() == 0){
				  if(previousUnit == 1) pixValue.setText("" + scale * 10);
				  if(previousUnit == 2) pixValue.setText("" + scale / 2.54f);
			  }
			  if(unitsList.getSelectedIndex() == 1){
				  if(previousUnit == 0) pixValue.setText("" + scale / 10);
				  if(previousUnit == 2) pixValue.setText("" + scale / 25.4f);
			  }
			  if(unitsList.getSelectedIndex() == 2){
				  if(previousUnit == 1) pixValue.setText("" + scale * 25.4f);
				  if(previousUnit == 0) pixValue.setText("" + scale * 2.54f);
			  }
			  previousUnit = unitsList.getSelectedIndex();
		  }
	}
	
	/**
	 * Update the scale based on the new DPI values
	 */
	private void updateScale(){
			DPIValue.setText(""+rm.getDPI());
			cmValue.setText(""+1);
			pixValue.setText(""+(int)(rm.getDPI() / 2.54));
	}
	
	/**
	 * Refresh the plots
	 */
	private void refreshPlot(){
  	  plotComboBox1.setModel( new DefaultComboBoxModel(rm.getParentRootNameList()));
	  if(b7 || b8 ){
    	  setSelected((String) plotComboBox1.getSelectedItem(), true);
    	  rm.repaint(); 
	  }
	  plotComboBox2.setModel( new DefaultComboBoxModel(rm.getPrimaryRootNameList()));
	  if(b9 ){
    	  setSelected((String) plotComboBox2.getSelectedItem(), true);
    	  rm.repaint(); 
	  }
	}
	
	
	/**
	 * Display the help text
	 * @param type
	 */
	private void displayHelp(int type){
		String helpText1 =  "SmartRoot allow the user to set the image resolution based on \n"+
							"the DPI value of the image or a random value based on a scale \n"+
							"visible on the image. \n \n"+
							"To use a scale on the image, simply draw a line on the scale, click \n"+
							"the 'Get Line' button and set the physical length of the scale. \n" +
							"The unit can be chosen between cm, mm and inch. \n \n" +
							"Click the 'Apply' button to set the scale on the image. SmartRoot \n" +
							"will use either the DPI or the cm/mm/inch value based on the box you \n" +
							"checked \n \n" +
							"The default value for the scale is the one stored within the image. \n" +
							"This default value will not be used once you saved an image with \n" +
							"SmartRoot. SmartRoot will use the value stored in the .xml file linked \n" +
							"with the image. \n" + 
							"Click the 'Set as default' button to set the current DPI value as \n \n" +
							"the default DPI value for all the newly opened images.";
		String helpText2 = "Once you draw a root SmartRoot choose a new name for it. \n" +
							"The software will use a prefix and a sequential number \n" +
							"based on the number of root already traced in the image. \n \n" +
							"The prefix of the root names can be changed by the user. \n \n" +
							"The 'Principal root prefix' applies for the root traced \n" +
							"manually or with the line-drawing utility. \n \n" +
							"The 'Lateral root prefix' applies for the lateral roots \n" +
							"traced with the 'Find lateral' function. \n \n" +
							"If the 'Ask for name' box is checked, the user will be ask \n" +
							"to confirm the root name every time a new root is traced. \n \n" +
							"The name change will only apply for the root to be traced, \n" +
							"not for the ones already traced";
		String helpText3 = "The thresholding method used by SmartRoot can be chosen \n" +
							"between an 'Adaptive thresholding' method or a fixed threshold \n" +
							"based on ImageJ threshold. \n \n" +
							"It is recommended to used the 'Adaptive thresholding' for optimal \n" +
							"performances.";
		String helpText4 = "The 'Find lateral' function use several test while building the \n" +
							"new laterals. These tests can be disabled or parametrized. \n \n" +
							
							"'Number of steps along the roots'\n" +
							"	The research algorithm scans the border of the parent root to \n" +
							"	find its laterals. The number of step along the root defines the \n" +
							"	the resolution of the search. The bigger the number, the more \n" +
							"	the search will be but the computing time will increase accordingly. \n" +
							"	The 'Fast find lateral' function do not use this parameter. \n \n" +
							
							"'Check the size of the node'\n" +
							"	Test the diameter of the newly created node. \n \n" +
							
							"'Minimum / maximum diameter of a node' \n" +
							"	The minimum / maximum diameter allowed for a newly created node. \n" +
							"	These diameters are expressed as multiples of the parent diameter. \n \n" +
							
							"'Check the size of the root' \n" +
							"	Test if the newly created root is long enough to actually be \n" +
							"	a root and not an artefact in the image. \n \n" +
							
							"'Minimum size of a lateral root' \n" +
							" 	The minimum size allowed for a newly created lateral. \n" +
							" 	Expressed in % of the parent diameter (100% = 1). \n \n" +
							
							"'Check the direction of the root' \n" +
							"	Test the insertion of the newly created root based on the first \n" +
							"	three nodes of this root. \n \n" +
							
							"'Maximum insertion angle' \n" +
							"	The maximum insertion angle allowed for a newly created lateral.";
		
		String helpText5 = "Parameters used by Java to connect to the SQL database:\n" +
		
							"'Driver class name' \n" +
							" 	The driver used to connect to the database. Is OS dependant. \n \n" +
							
							"'Connection URL' \n" +
							" 	The URL used to connect to the database. \n \n" +
							
							"'Connection user name' \n" +
							" 	The user name set in the SQL database. \n \n" +
							
							"'Connection password' \n" +
							" 	The password set in the SQL database."; 
		
		
		String helpTitle1 = "Image resolution settings";
		String helpTitle2 = "Root name settings";
		String helpTitle3 = "Thresholding method settings";
		String helpTitle4 = "Lateral research settings";
		String helpTitle5 = "SQL settings";
		
		switch(type){
			case 1: IJ.showMessage(helpTitle1, helpText1); break;
			case 2: IJ.showMessage(helpTitle2, helpText2); break;
			case 3: IJ.showMessage(helpTitle3, helpText3); break;
			case 4: IJ.showMessage(helpTitle4, helpText4); break;
			case 5: IJ.showMessage(helpTitle5, helpText5); break;
		}
	}
  
	/**
	 * Displauy the about text
	 * @return
	 */
	public String displayAboutText(){

    	String text = "SmartRoot \n"
    		+"version "+RootModel.version+" \n"
    		+"2014-04-11 \n\n"
    		+"Software created by \n"
    		+"Xavier Draye* and Guillaume Lobet** \n"
    		+"*[Universit� catholique de Louvain, Earth and Life Institute] \n"
    		+"**[Universit� de Li�ge, PHYTOSystems] \n \n"
    		+"This sofware is free for use and can be freely distributed \n \n"
    		+"If this software is used for scientific research, please do "
    		+"not forget to cite us as follow: \n \n "
    		+"Lobet G, Pag�s L, Draye X (2011) A Novel Image Analysis Toolbox \n"
    		+"Enabling Quantitative Analyses of Root System Architecture \n " 
    		+"Plant Physiology, Vol. 157, pp 29-39 \n \n"
    		+"More information about the software can be found at the address: \n"
    		+"www.uclouvain.be/en-smartroot \n \n"
    		+"(*) Experimental command. Might not work properly ";
    		

    	return text;
    }
    
    /**
     * File filter for saving the CSV file
     * @author guillaume
     *
     */
    public class ImageFilter extends javax.swing.filechooser.FileFilter{
      public boolean accept (File f) {
//        return f.getName ().toLowerCase ().endsWith ("csv")
//              || f.isDirectory ();
          if (f.isDirectory()) {
              return true;
          }

          String extension = getExtension(f);
          if (extension != null) {
              if (extension.equals("bmp") || extension.equals("png") || extension.equals("jpg") || 
            		  extension.equals("tif") || extension.equals("jpeg") || extension.equals("tiff")) return true;
              else return false;
          }
          return false;
      }
     
      public String getDescription () {
        return "Image file (*.bmp, *.png, *.jpg, *.tiff)";
      }
      
      public String getExtension(File f) {
          String ext = null;
          String s = f.getName();
          int i = s.lastIndexOf('.');

          if (i > 0 &&  i < s.length() - 1) {
              ext = s.substring(i+1).toLowerCase();
          }
          return ext;
      }
    }    
    
    /**
     * File filter for saving the CSV file
     * @author guillaume
     *
     */
    
    public class JavaFilter extends javax.swing.filechooser.FileFilter{
      public boolean accept (File f) {
//        return f.getName ().toLowerCase ().endsWith ("csv")
//              || f.isDirectory ();
          if (f.isDirectory()) {
              return true;
          }

          String extension = getExtension(f);
          if (extension != null) {
              if (extension.equals("csv")) return true;
              else return false;
          }
          return false;
      }
     
      public String getDescription () {
        return "Comma-separated values file (*.csv)";
      }
      
      public String getExtension(File f) {
          String ext = null;
          String s = f.getName();
          int i = s.lastIndexOf('.');

          if (i > 0 &&  i < s.length() - 1) {
              ext = s.substring(i+1).toLowerCase();
          }
          return ext;
      }
    }
	
    /**
     * Change extension of a file
     * @param s
     * @param ext
     * @return
     */
    private String changeExtension(String s, String ext){
    	String init = s;
    	int cut = init.lastIndexOf(".");
    	if(cut > -1){
    		String mid = init.substring(0, cut);    	
    		return mid.concat("."+ext);
    	}
    	else return init.concat("."+ext);
    }
    
    
    /**
     * Batch export function. Export mutliple datafile at once
     */
    private void batchExport(){
    	    	
    	// Retrieve all the xml files
    	File f = new File(batchSourceFolder.getText()); 	
    	File[] rsml = f.listFiles(new FilenameFilter() {
    		public boolean accept(File directory, String fileName) {
    			return fileName.endsWith(".xml") ||  fileName.endsWith(".rsml");
    		}
    	});
    	ArrayList<String> rsmlListShort = new ArrayList<String>();
    	ArrayList<File> rsmlList = new ArrayList<File>();
    	for(int i = 0; i < rsml.length; i++){
    		String n = rsml[i].getName().substring(0, rsml[i].getName().lastIndexOf('.'));
    		if(rsmlListShort.indexOf(n) == -1){
    			rsmlListShort.add(n);
    			rsmlList.add(rsml[i]);
    		}
    	}
  	  	
    	// Create the PrintWriter for the csv export
 	 	PrintWriter pw = null;
    	if(batchCSV.isSelected()){
    		String file = batchCSVFile.getText(); 		   
    		try{pw = new PrintWriter(new FileWriter(file));}
    		catch(IOException e){SR.write("Could not save file "+file);}
    	}
    	
    	SR.write("Batch export started for "+rsml.length+" files");
    	// Open the different XML files and export their data.
  	  	for(int i = 0; i < rsmlList.size(); i++){
  	  		RootModel rmLocal = new RootModel(rsmlList.get(i).getAbsolutePath(), batchDPI.isSelected(), Float.valueOf(batchDPIValue.getText()));
  	  		//rmLocal.setDPI(567f);
//  	  		batchTransfersData((i == 0), (i == xml.length), rmLocal, xml[i].getName(), pw, persoDB.isSelected(), createBatchSQL.isSelected());
  	  		if(batchTransfersData((i == 0), false, rmLocal, rsmlList.get(i).getName(), pw, true, true)) 
  	  			SR.write("Export done for "+rsmlList.get(i).getName());;
  	  	}
  	  	if(pw != null) pw.flush();
  	  	SR.write("Export done for "+ rsmlList.size() +" files"); 
    }
    
    /**
     * Transfers the data to the SQL database / CSV file / image
     */
    private boolean batchTransfersData(boolean first, boolean last, RootModel rm, String name, PrintWriter pw, boolean perso, boolean create){
 	   
 	   if(batchImage.isSelected()){
            rm.exportImage(batchImageFolder.getText(), batchColorJCB.getSelectedIndex() == 0, 
         		   (String) batchImageJCB.getSelectedItem(), Integer.valueOf(batchLineWidth.getText()), 
         		   name, batchRealWidth.isSelected(), splitRootImage.isSelected());
            return true;
 	   }
 	   
 	   if(batchCSV.isSelected()){           
     	   int sel = batchJCB.getSelectedIndex();
     	   switch (sel) {
               case 0: rm.csvSendRoots(pw, first, name, last); break;
               case 1: rm.csvSendMarks(pw, first, name, last); break;
               case 2: rm.csvSendNodes(pw, first, name, last); break;
               case 3: rm.csvSendGrowthRate(pw, first, name, last); break;
               case 4: rm.csvLabExport(pw, first, name, ".xml", last); break;
            }
     	   return true;
 	   } 
 	   
 	   if(batchSQL.isSelected()){
 		   SR.write("SQL Transfer started");
 		   int sel = batchJCB.getSelectedIndex();
 	       switch (sel) {
 	          case 0: rm.sqlSendRoots(batchSQLTable.getText(), (create && first), name); break;
 	          case 1: rm.sqlSendMarks(batchSQLTable.getText(), (create && first), name); break;
 	          case 2: rm.sqlSendNodes(batchSQLTable.getText(), (create && first), name); break;
              case 3: rm.sqlSendGrowthRates(batchSQLTable.getText(), (create && first), name); break;
 	          case 4: rm.sqlLabExport(batchSQLTable.getText(), (create && first), name, ".rsml", perso); break;
 	       }
 	       return true;
 	   }
 	   return false;
    }
    
    
    /**
     * Save function for the common XML structure
     * @param fName
     */
   public void saveDummyRSML(String fName){
       FileWriter dataOut;

       fName = fName.substring(0, fName.lastIndexOf('.'));
       try {
          dataOut = new FileWriter(fName+".xml") ;
          }
       catch (IOException e) {
          SR.write("The datafile cannot be created or written to.");
          SR.write("Please check you have a WRITE access to the directory and ");
          SR.write("there is sufficient space on the disk.");
          return;
          }

       try {
           String nL = System.getProperty("line.separator");
           dataOut.write("<?xml version='1.0' encoding='UTF-8'?>" + nL);
           dataOut.write("<rsml xmlns:po='http://www.plantontology.org/xml-dtd/po.dtd'>" + nL);
           dataOut.write("	<metadata>" + nL);
           
           // Image information
           dataOut.write("		<version>1</version>" + nL);
           dataOut.write("		<unit>inch</unit>" + nL);
           dataOut.write("		<resolution>300</resolution>" + nL);
           dataOut.write("		<last-modified>today</last-modified>" + nL);
           dataOut.write("		<software>smartroot</software>" + nL);
           dataOut.write("		<user>globet</user>" + nL);
           dataOut.write("		<file-key>myimage</file-key>" + nL);  
      

         dataOut.write("		<property-definitions>" + nL);
         dataOut.write("			<property-definition>" + nL);
         dataOut.write("		    	<label>diameter</label>" + nL);
         dataOut.write("		        <type>float</type>" + nL);    
         dataOut.write("		        <unit>cm</unit>" + nL);
         dataOut.write("			</property-definition>" + nL);
         dataOut.write("			<property-definition>" + nL);
         dataOut.write("		    	<label>length</label>" + nL);
         dataOut.write("		        <type>float</type>" + nL);    
         dataOut.write("		        <unit>cm</unit>" + nL);
         dataOut.write("			</property-definition>" + nL);
         dataOut.write("			<property-definition>" + nL);
         dataOut.write("		    	<label>angle</label>" + nL);
         dataOut.write("		        <type>float</type>" + nL);    
         dataOut.write("		        <unit>degree</unit>" + nL);
         dataOut.write("			</property-definition>" + nL);        
         dataOut.write("			<property-definition>" + nL);
         dataOut.write("		    	<label>insertion</label>" + nL);
         dataOut.write("		        <type>float</type>" + nL);    
         dataOut.write("		        <unit>cm</unit>" + nL);
         dataOut.write("			</property-definition>" + nL); 
         dataOut.write("			<property-definition>" + nL);
         dataOut.write("		    	<label>lauz</label>" + nL);
         dataOut.write("		        <type>float</type>" + nL);    
         dataOut.write("		        <unit>cm</unit>" + nL);
         dataOut.write("			</property-definition>" + nL); 
         dataOut.write("			<property-definition>" + nL);
         dataOut.write("		    	<label>lbuz</label>" + nL);
         dataOut.write("		        <type>float</type>" + nL);    
         dataOut.write("		        <unit>cm</unit>" + nL);
         dataOut.write("			</property-definition>" + nL);
         dataOut.write("			<property-definition>" + nL);
         dataOut.write("		    	<label>node-orientation</label>" + nL);
         dataOut.write("		        <type>float</type>" + nL);    
         dataOut.write("		        <unit>radian</unit>" + nL);
         dataOut.write("			</property-definition>" + nL);    
         dataOut.write("		</property-definitions>" + nL);        
           

         dataOut.write("		<image>" + nL);
         dataOut.write("			<captured>today</captured>" + nL);  
         dataOut.write("			<label>xxxx</label>" + nL);            
         dataOut.write("		</image>" + nL);
           
           
         dataOut.write("	</metadata>" + nL);

         // Define the scene  
         dataOut.write("	<scene>" + nL);
         dataOut.write("		<plant>" + nL);           
           dataOut.write("		</plant>" + nL);
           dataOut.write("	</scene>" + nL);
           dataOut.write("</rsml>" + nL);
           dataOut.close();
           }
        catch (IOException ioe) {
           SR.write("An I/O error occured while saving the datafile.");
           SR.write("The new datafile is thus most probably corrupted.");
           SR.write("It is recommended that you re-open the image and");
           SR.write("use a backup file before re-saving.");
        }
 	  
   }
       
    
    
    /**
     * File filter for loading the XML files
     * @author guillaume
     *
     */
    
    public class XMLFilter extends javax.swing.filechooser.FileFilter{
      public boolean accept (File f) {
          if (f.isDirectory()) {
              return true;
          }

          String extension = getExtension(f);
          if (extension != null) {
              if (extension.equals("xml")) return true;
              else return false;
          }
          return false;
      }
     
      public String getDescription () {
        return "XML file (*.xml)";
      }
      
      public String getExtension(File f) {
          String ext = null;
          String s = f.getName();
          int i = s.lastIndexOf('.');
          if (i > 0 &&  i < s.length() - 1) {
              ext = s.substring(i+1).toLowerCase();
          }
          return ext;
      }
    } 
    
    
   }

