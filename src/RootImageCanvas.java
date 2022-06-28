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


// I should narrow that list by avoiding the systematic use of .*
import ij.*;
//import ij.process.*;
import ij.gui.*;
//import ij.measure.Measurements;
import ij.measure.ResultsTable;
import ij.plugin.filter.ParticleAnalyzer;
import ij.process.BinaryProcessor;
import ij.process.ByteProcessor;
import ij.process.ImageProcessor;



//import ij.io.*;
//import ij.plugin.frame.*;
//import ij.measure.*;
import java.awt.*;
import java.awt.event.*;

//import java.awt.geom.*;
//import java.awt.image.*;
//import java.awt.font.*;
//import java.awt.datatransfer.*;
//import java.sql.*;
import javax.swing.*;

//XML file support
//import javax.xml.parsers.DocumentBuilder; 
//import javax.xml.parsers.DocumentBuilderFactory;  
//import javax.xml.parsers.FactoryConfigurationError;  
//import javax.xml.parsers.ParserConfigurationException;
//import org.xml.sax.SAXException;  
//import org.xml.sax.SAXParseException; 
import java.util.*;


/** 
 * This is the image canvas, sitting on top of the actual image.
 * Interface between SmartRoot and the image
 * 
 */


// %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

class RootImageCanvas extends ImageCanvas
                      implements ActionListener, KeyEventDispatcher {

   private static final long serialVersionUID = -7813328146240598270L;
   
   public RootModel rm;
   private static Toolbar IJTool = Toolbar.getInstance();
   protected ImagePlus imp;
   protected ImageWindow imw;
   private JPopupMenu popup, nodePopup, currentPopup, markPopup, markPopup2;
   private int drag;
   private boolean tracingTwinMark = false;
   static final int TRACE = 1;
   static final int RULER = 2;
   public static int mode = 1;
   public boolean tracing = false;
   public boolean popupOpen = false;
   public boolean lockMousePosition = false;
   private static int currentTool;
   private int autoMarkType = 0;   // XD 20180811
   private SRWin srWin = SRWin.getInstance();  // XD 20180811 (needed to set showMark when the Mark tool is selected) 

   
   private JMenuItem[] mi = new JMenuItem[55]; // popup
   private static final int TOOL_MARK = 0;
   private static final int TOOL_TRACE = 1;
   private static final int TOOL_ANCHOR = 2;
   private static final int TOOL_MAGNIFIER = 3;
   private static final int TOOL_CROSSHAIR = 4;
   private static final int TOOL_HAND = 5;
   private static final int LOCALS = 6;
   private static final int SET_RULER_ORIGIN = 7;
   private static final int SYNCHRONIZE_ALL_CANVAS = 8;
   private static final int RM_ANCHORS = 9;
   private static final int APPEND_NODES = 10;
   private static final int DELETE_NODE = 11;
   private static final int SPLIT_ROOT = 12;
   private static final int DELETE_END_OF_ROOT = 13;
   private static final int RENAME_ROOT = 14;
   private static final int DELETE_ROOT = 15;
   private static final int REVERSE_ROOT = 16;
   private static final int DELETE_BASE_OF_ROOT = 17;
   private static final int CROP_CANDIDATE_CHILDREN = 18;
   private static final int FILE_RECOVER = 19;
   private static final int FILE_QUIT_WITHOUT_SAVE = 20;
   private static final int FILE_SAVE = 21;
   private static final int FILE_CLEAR = 22;
   private static final int FILE_IMPORT = 23;
   private static final int DELETE_MARK = 24;
   private static final int CHANGE_MARK_VALUE = 25;
   private static final int DELETE_ALL_MARKS = 26;
   private static final int BRING_TO_FRONT = 27;
   private static final int SEND_TO_BACK = 28;
   private static final int ROOTID_ITEM = 29;
   private static final int FIND_LATERALS = 30;
   private static final int FIND_LATERALS_2 = 31;
   private static final int AUTO_DRAW = 32;
   private static final int ATTACHE_PARENT = 33;
   private static final int DETACH_PARENT = 34;
   private static final int PARENTID_ITEM = 35;
   private static final int DETACH_CHILDREN = 36;
   private static final int ORDER_ITEM = 37;
   private static final int RENAME_ALL_WITH = 38;
   private static final int RENAME_ALL_WITHOUT = 39;
   private static final int TOOL_LATERAL = 40;
   private static final int DELETE_SMALL_ROOTS = 41;
   private static final int FILE_IMPORT_MULTIPLE = 42;
   private static final int CROP_TRACING = 43;
   private static final int APPEND_TRACING = 44;
   private static final int RENAME_ALL_LATERALS = 45;
   private static final int CHANGE_ROOT_KEY = 46;
   private static final int FILE_IMPORT_SAME = 47;
   private static final int FILE_SAVE_COMMON = 48;
   private static final int FILE_IMPORT_COMMON = 49;
   private static final int MOVE_CANVAS = 50;
   private static final int TRACE_FOLDER = 51;
   private static final int CROP_SINGLE_ROOT = 52;
   private static final int RECENTER_ALL = 53;
   private static final int MULTIPLY_NODES = 54;

   private static SyncSettings sync = new SyncSettings();
   private static Vector<RootImageCanvas> canvasList = new Vector<RootImageCanvas>();
   private boolean listenerRuler = false;
   static private boolean ctrl_key_pressed = false;
   static private boolean shift_key_pressed = false;
   private float mouseX, mouseY;
   private KeyboardFocusManager kfm = KeyboardFocusManager.getCurrentKeyboardFocusManager();
   protected long mousePressedTime;

   
   /**
    * Constructor. Construct all the menus.
    * @param imp
    */
   public RootImageCanvas(ImagePlus imp) {   
      super(imp);
      this.imp = imp;
      magnification = getMagnification();
      rm = null;
      canvasList.add(this);
      logNInstances();
      IJTool.addMouseListener(this); 
      setMode();
      
      /** By making the canvas a KeyEventDispatcher, I allow it to process KeyEvents before
          other windows */
      kfm.addKeyEventDispatcher(this);
      

      popup = new JPopupMenu();

      JMenu menu = new JMenu("Tools");
      popup.add(menu);

      mi[TOOL_MAGNIFIER] = new JMenuItem("Zoom In/Out (ESC to leave)");
      mi[TOOL_MAGNIFIER].setActionCommand("TOOL_MAGNIFIER");
      mi[TOOL_MAGNIFIER].addActionListener(this);
      menu.add(mi[TOOL_MAGNIFIER]);

      mi[TOOL_HAND] = new JMenuItem("Hand");
      mi[TOOL_HAND].setActionCommand("TOOL_HAND");
      mi[TOOL_HAND].addActionListener(this);
      menu.add(mi[TOOL_HAND]);
      
      mi[TOOL_TRACE] = new JMenuItem("Trace");
      mi[TOOL_TRACE].setActionCommand("TOOL_TRACE");
      mi[TOOL_TRACE].addActionListener(this);
      menu.add(mi[TOOL_TRACE]);
      
      mi[TOOL_LATERAL] = new JMenuItem("Lateral tracing");
      mi[TOOL_LATERAL].setActionCommand("TOOL_LATERAL");
      mi[TOOL_LATERAL].addActionListener(this);
      menu.add(mi[TOOL_LATERAL]);

      mi[TOOL_MARK] = new JMenuItem("Mark");
      mi[TOOL_MARK].setActionCommand("TOOL_MARK");
      mi[TOOL_MARK].addActionListener(this);
      menu.add(mi[TOOL_MARK]);

      mi[TOOL_CROSSHAIR] = new JMenuItem("Ruler");
      mi[TOOL_CROSSHAIR].setActionCommand("TOOL_CROSSHAIR");
      mi[TOOL_CROSSHAIR].addActionListener(this);
      menu.add(mi[TOOL_CROSSHAIR]);

      mi[TOOL_ANCHOR] = new JMenuItem("Registration Anchor");
      mi[TOOL_ANCHOR].setActionCommand("TOOL_ANCHOR");
      mi[TOOL_ANCHOR].addActionListener(this);
      menu.add(mi[TOOL_ANCHOR]);


      mi[LOCALS] = menu = new JMenu("Locals");
      popup.add(menu);

      mi[SET_RULER_ORIGIN] = new JMenuItem("Set Ruler Origin");
      mi[SET_RULER_ORIGIN].setActionCommand("SET_RULER_ORIGIN");
      mi[SET_RULER_ORIGIN].addActionListener(this);
      menu.add(mi[SET_RULER_ORIGIN]);

      mi[SYNCHRONIZE_ALL_CANVAS] = new JMenuItem("Synchronize other windows");
      mi[SYNCHRONIZE_ALL_CANVAS].setActionCommand("SYNCHRONIZE_ALL_CANVAS");
      mi[SYNCHRONIZE_ALL_CANVAS].addActionListener(this);
      menu.add(mi[SYNCHRONIZE_ALL_CANVAS]);

      mi[RM_ANCHORS] = new JMenuItem("Remove All Registration Anchors");
      mi[RM_ANCHORS].setActionCommand("RM_ANCHORS");
      mi[RM_ANCHORS].addActionListener(this);
      menu.add(mi[RM_ANCHORS]);

      mi[DELETE_ALL_MARKS] = new JMenuItem("Delete All Marks");
      mi[DELETE_ALL_MARKS].setActionCommand("DELETE_ALL_MARKS");
      mi[DELETE_ALL_MARKS].addActionListener(this);
      menu.add(mi[DELETE_ALL_MARKS]);


      JMenu filePopup = new JMenu("File");
      
      mi[FILE_IMPORT] = new JMenuItem("Import seed datafile");
      mi[FILE_IMPORT].setActionCommand("FILE_IMPORT");
      mi[FILE_IMPORT].addActionListener(this);
      filePopup.add(mi[FILE_IMPORT]);
      
      mi[FILE_IMPORT_SAME] = new JMenuItem("Import previous datafile");
      mi[FILE_IMPORT_SAME].setActionCommand("FILE_IMPORT_SAME");
      mi[FILE_IMPORT_SAME].addActionListener(this);
      filePopup.add(mi[FILE_IMPORT_SAME]);
      
      mi[FILE_IMPORT_COMMON] = new JMenuItem("Import common datafile");
      mi[FILE_IMPORT_COMMON].setActionCommand("FILE_IMPORT_COMMON");
      mi[FILE_IMPORT_COMMON].addActionListener(this);
      filePopup.add(mi[FILE_IMPORT_COMMON]);

      mi[FILE_IMPORT_MULTIPLE] = new JMenuItem("Import multiple datafile");
      mi[FILE_IMPORT_MULTIPLE].setActionCommand("FILE_IMPORT_MULTIPLE");
      mi[FILE_IMPORT_MULTIPLE].addActionListener(this);
      filePopup.add(mi[FILE_IMPORT_MULTIPLE]);    
      
      filePopup.addSeparator();
      
//      mi[FILE_SAVE] = new JMenuItem("Save datafile");
//      mi[FILE_SAVE].setActionCommand("FILE_SAVE");
//      mi[FILE_SAVE].addActionListener(this);
//      filePopup.add(mi[FILE_SAVE]);
      
      mi[FILE_SAVE_COMMON] = new JMenuItem("Save datafile");
      mi[FILE_SAVE_COMMON].setActionCommand("FILE_SAVE_COMMON");
      mi[FILE_SAVE_COMMON].addActionListener(this);
      filePopup.add(mi[FILE_SAVE_COMMON]);

      mi[FILE_RECOVER] = new JMenuItem("Use backup datafile");
      mi[FILE_RECOVER].setActionCommand("FILE_RECOVER");
      mi[FILE_RECOVER].addActionListener(this);
      filePopup.add(mi[FILE_RECOVER]);

      mi[FILE_CLEAR] = new JMenuItem("Clear datafile");
      mi[FILE_CLEAR].setActionCommand("FILE_CLEAR");
      mi[FILE_CLEAR].addActionListener(this);
      filePopup.add(mi[FILE_CLEAR]);

      mi[FILE_QUIT_WITHOUT_SAVE] = new JMenuItem("Quit without saving datafile");
      mi[FILE_QUIT_WITHOUT_SAVE].setActionCommand("FILE_QUIT_WITHOUT_SAVE");
      mi[FILE_QUIT_WITHOUT_SAVE].addActionListener(this);
      filePopup.add(mi[FILE_QUIT_WITHOUT_SAVE]);
      
      popup.add(filePopup);

//
//      menu = new JMenu("Settings");
//      popup.add(menu);
//
//      JMenu thresholdPopup = new JMenu("Threshold");
//      menu.add(thresholdPopup);
//
//      mi[THRESHOLD_ADAPTIVE] = new JMenuItem("Adaptive Thresholding");
//      mi[THRESHOLD_ADAPTIVE].setActionCommand("THRESHOLD_ADAPTIVE");
//      thresholdPopup.add(mi[THRESHOLD_ADAPTIVE]);
//      mi[THRESHOLD_ADAPTIVE].addActionListener(this);
//      mi[THRESHOLD_ADAPTIVE].setEnabled(false);
//
//      mi[THRESHOLD_20_BELOW_MAX] = new JMenuItem("ImageJ fixed threshold");
//      mi[THRESHOLD_20_BELOW_MAX].setActionCommand("THRESHOLD_20_BELOW_MAX");
//      thresholdPopup.add(mi[THRESHOLD_20_BELOW_MAX]);
//      mi[THRESHOLD_20_BELOW_MAX].addActionListener(this);
      
      
      JMenu utilPopup = new JMenu("Utilities");
      
      JMenu renamePopup = new JMenu("Rename all roots");
      
      mi[RENAME_ALL_WITH] = new JMenuItem("With prefix");
      mi[RENAME_ALL_WITH].setActionCommand("RENAME_ALL_WITH");
      mi[RENAME_ALL_WITH].addActionListener(this);
      renamePopup.add(mi[RENAME_ALL_WITH]);
      
      mi[RENAME_ALL_WITHOUT] = new JMenuItem("Without prefix");
      mi[RENAME_ALL_WITHOUT].setActionCommand("RENAME_ALL_WITHOUT");
      mi[RENAME_ALL_WITHOUT].addActionListener(this);
      renamePopup.add(mi[RENAME_ALL_WITHOUT]);
      
      mi[RENAME_ALL_LATERALS] = new JMenuItem("Rename laterals with lPos");
      mi[RENAME_ALL_LATERALS].setActionCommand("RENAME_ALL_LATERALS");
      mi[RENAME_ALL_LATERALS].addActionListener(this);
      renamePopup.add(mi[RENAME_ALL_LATERALS]);
      
      utilPopup.add(renamePopup);

      mi[MOVE_CANVAS] = new JMenuItem("Move tracing");
      mi[MOVE_CANVAS].setActionCommand("MOVE_CANVAS");
      mi[MOVE_CANVAS].addActionListener(this);
      utilPopup.add(mi[MOVE_CANVAS]);
      
      mi[AUTO_DRAW] = new JMenuItem("Automatic drawing");
      mi[AUTO_DRAW].setActionCommand("AUTO_DRAW");
      mi[AUTO_DRAW].addActionListener(this);
      utilPopup.add(mi[AUTO_DRAW]);
      
      mi[DELETE_SMALL_ROOTS] = new JMenuItem("Delete small roots");
      mi[DELETE_SMALL_ROOTS].setActionCommand("DELETE_SMALL_ROOTS");
      mi[DELETE_SMALL_ROOTS].addActionListener(this);
      utilPopup.add(mi[DELETE_SMALL_ROOTS]);
      
      mi[CROP_TRACING] = new JMenuItem("Crop tracing [*]");
      mi[CROP_TRACING].setActionCommand("CROP_TRACING");
      mi[CROP_TRACING].addActionListener(this);
      utilPopup.add(mi[CROP_TRACING]);  
      
      mi[RECENTER_ALL] = new JMenuItem("Recenter all nodes [*]");
      mi[RECENTER_ALL].setActionCommand("RECENTER_ALL");
      mi[RECENTER_ALL].addActionListener(this);
      utilPopup.add(mi[RECENTER_ALL]);        
      
      mi[TRACE_FOLDER] = new JMenuItem("Crop previous tracing [*]");
      mi[TRACE_FOLDER].setActionCommand("TRACE_FOLDER");
      mi[TRACE_FOLDER].addActionListener(this);
      utilPopup.add(mi[TRACE_FOLDER]);  
      
      mi[APPEND_TRACING] = new JMenuItem("Append tracing [*]");
      mi[APPEND_TRACING].setActionCommand("APPEND_TRACING");
      mi[APPEND_TRACING].addActionListener(this);
      utilPopup.add(mi[APPEND_TRACING]);  
      
      mi[FIND_LATERALS] = new JMenuItem("Find roots [*]");
      mi[FIND_LATERALS].setActionCommand("FIND_LATERALS");
      mi[FIND_LATERALS].addActionListener(this);
      utilPopup.add(mi[FIND_LATERALS]);
      
      popup.add(utilPopup);
      
      
      //popup.add(mi[UNDO]);
            
      nodePopup = new JPopupMenu();
      
      mi[ROOTID_ITEM] = new JMenuItem("rootID");
      mi[ROOTID_ITEM].setFont(mi[ROOTID_ITEM].getFont().deriveFont(Font.BOLD));
      mi[ROOTID_ITEM].setBackground(Color.yellow);
      mi[ROOTID_ITEM].setEnabled(false);
      nodePopup.add(mi[ROOTID_ITEM]);
      
      mi[PARENTID_ITEM] = new JMenuItem("parentID");
      mi[PARENTID_ITEM].setFont(mi[PARENTID_ITEM].getFont().deriveFont(Font.BOLD));
      mi[PARENTID_ITEM].setBackground(Color.lightGray);
      mi[PARENTID_ITEM].setEnabled(false);
      nodePopup.add(mi[PARENTID_ITEM]);
      
      mi[ORDER_ITEM] = new JMenuItem("order");
      mi[ORDER_ITEM].setFont(mi[ORDER_ITEM].getFont().deriveFont(Font.BOLD));
      mi[ORDER_ITEM].setEnabled(false);
      nodePopup.add(mi[ORDER_ITEM]);
      
      nodePopup.addSeparator();

      mi[APPEND_NODES] = new JMenuItem("Append nodes");
      mi[APPEND_NODES].setActionCommand("APPEND_NODES");
      mi[APPEND_NODES].setEnabled(false);
      mi[APPEND_NODES].addActionListener(this);
      nodePopup.add(mi[APPEND_NODES]);

      mi[DELETE_NODE] = new JMenuItem("Remove node");
      mi[DELETE_NODE].setActionCommand("DELETE_NODE");
      mi[DELETE_NODE].addActionListener(this);
      nodePopup.add(mi[DELETE_NODE]);

      mi[SPLIT_ROOT] = new JMenuItem("Split root (after)");
      mi[SPLIT_ROOT].setActionCommand("SPLIT_ROOT");
      mi[SPLIT_ROOT].addActionListener(this);
      nodePopup.add(mi[SPLIT_ROOT]);
      
      mi[CROP_SINGLE_ROOT] = new JMenuItem("Crop root");
      mi[CROP_SINGLE_ROOT].setActionCommand("CROP_SINGLE_ROOT");
      mi[CROP_SINGLE_ROOT].addActionListener(this);
      nodePopup.add(mi[CROP_SINGLE_ROOT]);      

      mi[MULTIPLY_NODES] = new JMenuItem("Multiply nodes");
      mi[MULTIPLY_NODES].setActionCommand("MULTIPLY_NODES");
      mi[MULTIPLY_NODES].addActionListener(this);
      nodePopup.add(mi[MULTIPLY_NODES]); 
      
      mi[DELETE_END_OF_ROOT] = new JMenuItem("Remove all nodes (after)");
      mi[DELETE_END_OF_ROOT].setActionCommand("DELETE_END_OF_ROOT");
      mi[DELETE_END_OF_ROOT].addActionListener(this);
      nodePopup.add(mi[DELETE_END_OF_ROOT]);

      mi[DELETE_BASE_OF_ROOT] = new JMenuItem("Remove all nodes (before)");
      mi[DELETE_BASE_OF_ROOT].setActionCommand("DELETE_BASE_OF_ROOT");
      mi[DELETE_BASE_OF_ROOT].addActionListener(this);
      nodePopup.add(mi[DELETE_BASE_OF_ROOT]);

      nodePopup.addSeparator();

      mi[BRING_TO_FRONT] = new JMenuItem("Bring to front");
      mi[BRING_TO_FRONT].setActionCommand("BRING_TO_FRONT");
      mi[BRING_TO_FRONT].addActionListener(this);
      nodePopup.add(mi[BRING_TO_FRONT]);

      mi[SEND_TO_BACK] = new JMenuItem("Send to back");
      mi[SEND_TO_BACK].setActionCommand("SEND_TO_BACK");
      mi[SEND_TO_BACK].addActionListener(this);
      nodePopup.add(mi[SEND_TO_BACK]);

      nodePopup.addSeparator();
      
      
      mi[FIND_LATERALS_2] = new JMenuItem("Find laterals");
      mi[FIND_LATERALS_2].setActionCommand("FIND_LATERALS_2");
      mi[FIND_LATERALS_2].addActionListener(this);
      nodePopup.add(mi[FIND_LATERALS_2]);
      
      mi[ATTACHE_PARENT] = new JMenuItem("Attach parent root");
      mi[ATTACHE_PARENT].setActionCommand("ATTACHE_PARENT");
      mi[ATTACHE_PARENT].addActionListener(this);
      nodePopup.add(mi[ATTACHE_PARENT]);
      
      mi[DETACH_PARENT] = new JMenuItem("Detach parent root");
      mi[DETACH_PARENT].setEnabled(false);
      mi[DETACH_PARENT].setActionCommand("DETACHE_PARENT");
      mi[DETACH_PARENT].addActionListener(this);
      nodePopup.add(mi[DETACH_PARENT]);
      
      mi[DETACH_CHILDREN] = new JMenuItem("Detach all children roots");
      mi[DETACH_CHILDREN].setEnabled(false);
      mi[DETACH_CHILDREN].setActionCommand("DETACHE_CHILDREN");
      mi[DETACH_CHILDREN].addActionListener(this);
      nodePopup.add(mi[DETACH_CHILDREN]);
      
      nodePopup.addSeparator();
      
       mi[REVERSE_ROOT] = new JMenuItem("Reverse Orientation");
      mi[REVERSE_ROOT].setActionCommand("REVERSE_ROOT");
      mi[REVERSE_ROOT].addActionListener(this);
      nodePopup.add(mi[REVERSE_ROOT]);     
      
      mi[RENAME_ROOT] = new JMenuItem("Rename...");
      mi[RENAME_ROOT].setActionCommand("RENAME_ROOT");
      mi[RENAME_ROOT].addActionListener(this);
      nodePopup.add(mi[RENAME_ROOT]);
      
      mi[CHANGE_ROOT_KEY] = new JMenuItem("Change key...");
      mi[CHANGE_ROOT_KEY].setActionCommand("CHANGE_ROOT_KEY");
      mi[CHANGE_ROOT_KEY].addActionListener(this);
      nodePopup.add(mi[CHANGE_ROOT_KEY]);
      
      nodePopup.addSeparator();
      
      mi[CROP_CANDIDATE_CHILDREN] = new JMenuItem("Crop children...");
      mi[CROP_CANDIDATE_CHILDREN].setActionCommand("CROP_CANDIDATE_CHILDREN");
      mi[CROP_CANDIDATE_CHILDREN].addActionListener(this);
      nodePopup.add(mi[CROP_CANDIDATE_CHILDREN]);

      mi[DELETE_ROOT] = new JMenuItem("Delete Root");
      mi[DELETE_ROOT].setActionCommand("DELETE_ROOT");
      mi[DELETE_ROOT].addActionListener(this);
      nodePopup.add(mi[DELETE_ROOT]);

      markPopup = new JPopupMenu();
      for (int i = 0; i < Mark.getTypeCount(); i++) {
         JMenuItem mi = new JMenuItem(Mark.getName(i), Mark.getIcon(i));
         mi.setActionCommand("ADD_MARK_" + String.valueOf(i));
         mi.addActionListener(this);
         markPopup.add(mi);
         }
         
      markPopup2 = new JPopupMenu();
      mi[DELETE_MARK] = new JMenuItem("Delete mark");
      mi[DELETE_MARK].setActionCommand("DELETE_MARK");
      mi[DELETE_MARK].addActionListener(this);
      markPopup2.add(mi[DELETE_MARK]);

      mi[CHANGE_MARK_VALUE] = new JMenuItem("Change mark value");
      mi[CHANGE_MARK_VALUE].setActionCommand("CHANGE_MARK_VALUE");
      mi[CHANGE_MARK_VALUE].addActionListener(this);
      markPopup2.add(mi[CHANGE_MARK_VALUE]);
      }
   
   /**
    * Set the image window of the canvas
    * @param imw
    */
   public void setImageWindow(ImageWindow imw) {
      this.imw = imw;
   }

   /**
    * Print the number of image canvas instances
    */
   public void logNInstances() {
      if (canvasList.size() == 0)
         SR.write("There are no instances of RootImageCanvas");
      else if (canvasList.size() == 1)
         SR.write("There is one instance of RootImageCanvas");
      else  
         SR.write("There are " + canvasList.size() + " instances of RootImageCanvas");
      }

   /**
    * Attach the current root model
    * @param rm
    */
   public void attachRootModel(RootModel rm) {
      this.rm = rm;
   }
   
   /**
    * Display the graphics
    */
   public void paint(Graphics g) { 
      super.paint(g);
      if (rm != null) rm.paint((Graphics2D) g, false, true);
      }

   /**
    * Set the mouse mode (tracing, mark, ...)
    * @param t
    */
   private void setMode(int t) {
     
      if (t == SR.TRACE_TOOL || t == SR.ANCHOR_TOOL) {
         mode = TRACE;
         }
      else mode = RULER;
      }

   /**
    * Set the mouse mode (tracing, mark, ...)
    */
   private void setMode() {
      setMode(Toolbar.getToolId());
      }

   /**
    * Events triggers when the mouse is pressed
    * This method drives the popmenu events and initialize dragging 
    * (in case this event is followed by a mouseDrag()). 
     * Popup menu are handled here and not in mouseClicked() to be consistent with the parent class behavior
    */
   public void mousePressed(MouseEvent e) {
      // SR.write("MOUSE_PRESSED");

      // ADDED 20180731 XD
      // Java does not fires the MouseClicked if a MouseDragged event occurred between the MousePressed and 
      // MouseReleased events. With sensitive devices (such as a high resolution tablet), a small drag occurs
      // most of the time, even during a simple click, thereby preventing Java to fire the MouseClicked event
      // and all tracing-related work.
      // I therefore propose to supersede Java behavior by detecting mouse clicks programmatically based on the
      // time elapsed between the MousePressed and MouseReleased events
      mousePressedTime = System.currentTimeMillis();

      // This method drives the popmenu events
      // and initialize dragging (in case this event is followed by a mouseDrag())
      // Popup menu are handled here and not in mouseClicked() to be consistent with the parent class behavior
      

      if(currentPopup != null) return;
      
      
      // Do not allow the user to change the tool via the IJtoolbar if tracing or twinmark
      if (e.getSource() == IJTool) {
         if (tracing) IJTool.setTool(SR.TRACE_TOOL);
         else if (tracingTwinMark) IJTool.setTool(SR.MARK_TOOL);
         return;
         }

      // If the canvas is expecting a second click to finalise a TwinMark, do not allow
      // other things to be done. This is a bit rough...
      if (tracingTwinMark) {
         // super.mousePressed() allows the user to hand-move the window.
         // it must be skipped with right-clicks to prevent the parent method to open a popup menu
         if (!(e.isPopupTrigger() == true || e.getButton() == 3)) super.mousePressed(e);
         return;
         }

      // Make sure we are working with the right tool
      int tool = Toolbar.getToolId();
      setMode(tool);

      // Handle right-clicks (popup menus + tracing termination)
      if ((e.isPopupTrigger() == true || e.getButton() == 3) 
                && tool != Toolbar.MAGNIFIER) {
         mouseX = offScreen2DX(e.getX());
         mouseY = offScreen2DY(e.getY());
         if (tool == Toolbar.CROSSHAIR) lockMousePosition = true;
         if (tool == SR.MARK_TOOL) {
            Mark m = rm.selectMark(mouseX, mouseY);
            if (m != null) {
               mi[CHANGE_MARK_VALUE].setEnabled(m.needsValue());
               markPopup2.show(this, e.getX(), e.getY());
               currentPopup = markPopup2;
               lockMousePosition = false;
               return;
               }
            // XD 20180811 Added next block
            if (rm.getSelectedRoot() != null) {
               markPopup.show(this, e.getX(), e.getY());
               currentPopup = markPopup;
               lockMousePosition = false;
               return;
               }
           }
         if (tool == SR.TRACE_TOOL || tool == SR.LATERAL_TOOL) {

            int selection = rm.selectNode(mouseX, mouseY); // mouseClicked() assumes this has been done (connect & termination)
            int root = rm.selectRoot(mouseX, mouseY);
            if (tracing == true) return;  // currently, this drives tracing termination (the job is done by mouseClicked())
                                          // But this is where to handle a popup during tracing
            if (selection != 0) {
               boolean f = (selection == RootModel.NODE);
               mi[APPEND_NODES].setEnabled(f ? rm.isEndOfRoot() : false);
               mi[DELETE_NODE].setEnabled(f);
               mi[SPLIT_ROOT].setEnabled(f);
               mi[DELETE_END_OF_ROOT].setEnabled(f);
               mi[DELETE_BASE_OF_ROOT].setEnabled(f);
               mi[ROOTID_ITEM].setText(rm.getSelectedRootID());
               if (root == RootModel.CHILD) {
                  mi[PARENTID_ITEM].setText(rm.getSelectedRootParentID());
                  mi[ORDER_ITEM].setText("order = "+rm.getSelectedRootOrder());
                  mi[PARENTID_ITEM].setVisible(true);
                  mi[DETACH_PARENT].setEnabled(true);
                  mi[DETACH_CHILDREN].setEnabled(false);
               }
               else if(root == RootModel.PARENT){
                  mi[PARENTID_ITEM].setVisible(false);
                  mi[ORDER_ITEM].setText("order = "+rm.getSelectedRootOrder());
                  mi[DETACH_CHILDREN].setEnabled(true);
                  mi[DETACH_PARENT].setEnabled(false);
               }
               else if(root == RootModel.CHILDPARENT){
                  mi[PARENTID_ITEM].setText(rm.getSelectedRootParentID());
                  mi[ORDER_ITEM].setText("order = "+rm.getSelectedRootOrder());
                  mi[PARENTID_ITEM].setVisible(true);
                  mi[DETACH_PARENT].setEnabled(true);
                  mi[DETACH_CHILDREN].setEnabled(true);
               }
               else if(root == RootModel.ROOT){
                  mi[PARENTID_ITEM].setVisible(false);
                  mi[ORDER_ITEM].setText("order = "+rm.getSelectedRootOrder());
                  mi[DETACH_CHILDREN].setEnabled(false);
                  mi[DETACH_PARENT].setEnabled(false);
               }
               nodePopup.show(this, e.getX(), e.getY());
               currentPopup = nodePopup;
               return;
               }
            }
         // Default popup
         showPopup(e);
         return;
         }
         
      // Handle Left-clicks
      if (tool == Toolbar.CROSSHAIR && IJ.spaceBarDown() == false) {
         // the RootModel knows about the mouse location because we are in ruler
         // mode and the mouseMoved() tells the model.
         rm.sqlPrepare();
         return;
         }
      if (tool == SR.TRACE_TOOL && IJ.spaceBarDown() == false && e.getClickCount() == 1) {
         // mouseClicked() assumes this has been done
         int selection = rm.selectNode(offScreen2DX(e.getX()), offScreen2DY(e.getY()));
         if (tool == SR.TRACE_TOOL && selection == RootModel.NODE && !tracing) {
            drag = 1; // drag = 1 : drag ready; can start drag operation
            lockMousePosition = false;
            }
         return;
         }
      
      // XD 20180811 block commented out 
//      if (tool == SR.MARK_TOOL && IJ.spaceBarDown() == false) {
//         if (rm.getSelectedRoot() != null) {
//            markPopup.show(this, e.getX(), e.getY());
//            currentPopup = markPopup;
//            lockMousePosition = true;
//            }
//         return;
//         }
      
      super.mousePressed(e);
      }


   /**
    * Show the popup menu
    * @param e
    */
   public void showPopup(MouseEvent e) {
      int t = Toolbar.getToolId();
      mi[TOOL_CROSSHAIR].setEnabled(t != Toolbar.CROSSHAIR);
      mi[TOOL_HAND].setEnabled(t != Toolbar.HAND);
      mi[TOOL_MAGNIFIER].setEnabled(t != Toolbar.MAGNIFIER);
      mi[TOOL_MARK].setEnabled(t != SR.MARK_TOOL);
      mi[TOOL_ANCHOR].setEnabled(t != SR.ANCHOR_TOOL);
      mi[TOOL_TRACE].setEnabled(t != SR.TRACE_TOOL);
      mi[TOOL_LATERAL].setEnabled(t != SR.LATERAL_TOOL);
      mi[SET_RULER_ORIGIN].setEnabled(mode == RULER);
      mi[SYNCHRONIZE_ALL_CANVAS].setEnabled(mode == RULER);
      popup.show(this, e.getX(), e.getY());
      currentPopup = popup;
      }

   // XD 20180811
   // The whole method body has been moved to the new method processMouseClickedEvent() 
   public void mouseClicked(MouseEvent e) {
      super.mouseClicked(e);
      // SR.write("MOUSE_CLICKED");
   }

      
   /**
    * 
    * This is where nodes are traced.
    * If right-click + TRACE_TOOL, selectNode() was already called on the RootModel (during MousePressed())
    * Same for left-click + TRACE_TOOL or MARK_TOOL
    */
   // XD 20180811 Method created (body was previously in mouseClicked()
   protected void processMouseClickedEvent(MouseEvent e) {
      // SR.write("SOFT_MOUSE_CLICKED");

      // This is where nodes are traced.
      // If right-click + TRACE_TOOL, selectNode() was already called on the RootModel (during MousePressed())
      // Same for left-click + TRACE_TOOL or MARK_TOOL
      if (e.getSource() == IJTool) {
         // Make sure we are working with the right tool
         setMode(Toolbar.getToolId());
         // XD 201808 block added
         if (Toolbar.getToolId() == SR.MARK_TOOL) {
            srWin.setDisplayMarks(true);    
            IJTool.repaint(); // srWin.setDisplayMarks(true) seems to mess up the painting of IJTool. Not sure why...
         }
         return;
      }

      drag = 0; // Exit drag mode
      if (currentPopup != null){
         if(popupOpen){
            currentPopup = null;
            popupOpen = false;
         }
         popupOpen = true;   // TODO: 20180731 is "else" missing ?
        
        
        return; // do not process this click further if its MOUSE_PRESSED event issued a popup.
      }
      int tool = Toolbar.getToolId();
      if (tool == SR.TRACE_TOOL && IJ.spaceBarDown() == false) {
        
         if (e.getClickCount() == 2) {
            rm.notifyContinueRootEnd(); // WORKS OK, BUT ROOT MODEL MIGHT REPAINT BEFORE ASKING FOR A NAME
            tracing = false;
            }
         else if ((e.isPopupTrigger() == true || e.getButton() == 3) && rm.needConnect()) {
            rm.connect();  // ROOT MODEL SHOULD DELETE THE NODE ADDED DURING THE FIRST CLICK
            tracing = false;
            }
         else {
            tracing = rm.addNode(offScreenX(e.getX()), offScreenY(e.getY()), getModifiers(e), 1);
            if ((e.isPopupTrigger() == true || e.getButton() == 3)) {
               rm.notifyContinueRootEnd(); // allows right-click termination
               tracing = false;
               }
            repaint();
            // Trigger the auto�ated tracing of laterals once the root is traced
            if(getModifiers(e) == RootModel.AUTO_TRACE & SR.prefs.getBoolean("autoFind", false)){
                rm.selectRoot(rm.rootList.get(rm.rootList.size()-1));
               rm.findLaterals2();
            }
         }
         repaint();
         
         return;
         }
      if (tool == SR.ANCHOR_TOOL && IJ.spaceBarDown() == false) {
         rm.addRegistrationAnchor(offScreen2DX(e.getX()) - 0.5f, offScreen2DY(e.getY()) - 0.5f);
         repaint();
         }
      if (tool == SR.MARK_TOOL && IJ.spaceBarDown() == false /* && tracingTwinMark */) {    // XD 20180811 part of expression commented out 
         if ((e.isPopupTrigger() == true || e.getButton() == 3)) return;
         // XD 20180811 (next two lines changed)
         if (tracingTwinMark) tracingTwinMark = rm.setTwinPosition(); // validates the twin mark position (e.g. if the user clicks in a different root)
         else if (rm.getSelectedRoot() != null) addMark(autoMarkType);
         repaint();
         }
      if (tool == SR.LATERAL_TOOL && IJ.spaceBarDown() == false) {
        if(rm.getSelectedRoot() != null) rm.traceLateral(offScreenX(e.getX()), offScreenY(e.getY()));
        else SR.write("Please place your cursor near an already traced root");
        repaint();
          }
      }
   
   /**
    * Events when the moused is dragged. Mainly when a node is moved
    */
   public void mouseDragged(MouseEvent e) {
      // SR.write("MOUSE_DRAGGED");
      if (e.getSource() == IJTool) return;
      if (drag >= 1) {
         drag = 2;  // drag = 2 : dragging
         rm.moveSelectedNode(offScreen2DX(e.getX()), offScreen2DY(e.getY()), getModifiers(e));
         repaint();
         if(rm.getSelectedRoot().parent != null && rm.getSelectedRoot().parent.lastChild == rm.getSelectedRoot()){
          rm.getSelectedRoot().setParentNode();
          rm.getSelectedRoot().parent.setLastChild();
         }
         return;
         }
      super.mouseDragged(e);
      }
   
   /**
   * Action triggered  with the release of the mouse
   * CHANGED XD 20180731 XD
   */
   public void mouseReleased(MouseEvent e) {
      // SR.write("MOUSE_RELEASED");
      if (processMouseReleased(e) == 0) super.mouseReleased(e);

      // ADDED XD 20180731 XD
      if (System.currentTimeMillis() - mousePressedTime <= 200) processMouseClickedEvent(e);
      }      
     
   /* ADDED XD 20180731 XD to allow soft-based processing of mouse clicked */
   protected int processMouseReleased(MouseEvent e) {
      
      if (currentPopup != null) return -1; // do not process this click further if its MOUSE_PRESSED event issued a popup.
      
      if (e.getSource() == IJTool) return -1;
      if (drag == 2) {

         drag = 0; // drag = 0 : leaving drag operation; not dragging 
         if (e.isAltDown()){ rm.rebuildFromSelectedNode(offScreen2DX(e.getX()),
                                                       offScreen2DY(e.getY()), getModifiers(e));
            // Trigger the auto�ated tracing of laterals once the root is traced
            if(getModifiers(e) == RootModel.AUTO_TRACE & SR.prefs.getBoolean("autoFind", false)){
                rm.selectRoot(rm.rootList.get(rm.rootList.size()-1));
               rm.findLaterals2();
               
            }
         }
         else rm.rebuildSelectedNode();
         repaint();
         return -1;
         }
      drag = 0;
      return 0;
      
     

      }
   
   /**
    * 
    */
   public void mouseExited(MouseEvent e) {
      super.mouseExited(e);
      }

   /**
    * 
    */
   public void mouseEntered(MouseEvent e) {
      super.mouseEntered(e);
      }

   /**
    * Actions when the mouse is moved around
    */
   public void mouseMoved(MouseEvent e) {
      if (e.getSource() == IJTool) return;
      super.mouseMoved(e);
      
      if (lockMousePosition) return;
      if (mode == RULER) {
        
         rm.makeRulerLine(offScreen2DX(e.getX()), offScreen2DY(e.getY()));
         Rectangle r = rm.getRulerClipRect();
         repaint(r.x, r.y, r.width, r.height);
         for (int i = 0; i < canvasList.size(); i++) {
            RootImageCanvas ric = (RootImageCanvas) canvasList.get(i);
            if ( ric != this) { ric.traceListenerRuler(rm.getSelectedRootID(),rm.getMarkerPosition());}}
         return;
         }
      if (tracing == true) {
         rm.moveTracingNode(offScreen2DX(e.getX()), offScreen2DY(e.getY()), getModifiers(e)); 
         repaint();
         }
      }

   /**
    * 
    * @return
    */
   public boolean isRulerMode() {return (mode == RULER); }
   
   /**
    * 
    */
   public void tracingDone() {tracing = false;}
   
   /**
    * Get the mouse modifiers (SHIFT, CONTROL and ALT)
    * @param e
    * @return
    */
   private int getModifiers(MouseEvent e) {
      int flag = 0;
      if (e.isShiftDown() && !e.isAltDown()) flag |= RootModel.SNAP_TO_BORDER;
      if (e.isControlDown()) flag |= RootModel.FREEZE_DIAMETER;
      if (e.isAltDown()) flag |= RootModel.AUTO_TRACE;
      return flag;
      }

   /**
    * 
    * @param e
    */
   public void itemStateChanged(ItemEvent e) {
      currentPopup = null;
      lockMousePosition = false;
      repaint();
      }


   /**
    * Trigger the different actions from the contextual menus.
    */
   public void actionPerformed(ActionEvent e) {
      String ac = e.getActionCommand();

      
      int tool = Toolbar.getToolId();
      if (ac == "TOOL_MAGNIFIER") {
         currentTool = tool;
         IJTool.setTool(Toolbar.MAGNIFIER);
         setMode();
         }
      else if (ac == "TOOL_HAND") {
         IJTool.setTool(Toolbar.HAND);
         setMode();
         }
      else if (ac == "TOOL_TRACE") {
         if (tool == SR.MARK_TOOL || tool == Toolbar.CROSSHAIR) repaint(); 
         IJTool.setTool(SR.TRACE_TOOL);
         setMode();
         }
      else if (ac == "TOOL_CROSSHAIR") {
         IJTool.setTool(Toolbar.CROSSHAIR);
         setMode();
         }
      else if (ac == "TOOL_MARK") {
         srWin.setDisplayMarks(true);   // XD 20180811 Line added
         IJTool.setTool(SR.MARK_TOOL);
         setMode();
         }
      else if (ac == "TOOL_ANCHOR") {
         if (tool == SR.MARK_TOOL || tool == Toolbar.CROSSHAIR) repaint(); 
         IJTool.setTool(SR.ANCHOR_TOOL);
         setMode();
         }
      else if (ac == "SET_RULER_ORIGIN") {
         rm.setRulerZero();
         repaint();
         }
      else if (ac == "SYNCHRONIZE_ALL_CANVAS") {
         sync.setRootID(rm.getSelectedRootID());
         sync.setLPos(rm.getRulerPosition());
         sync.setMagnification(getMagnification());
         notifySynchronize();
         }
      else if (ac == "RENAME_ROOT") {
         String rootID = rm.getSelectedRootID();
         rootID = JOptionPane.showInputDialog(imw, "Enter the root identifier: ", rootID);
         if (rootID != null && rootID.length() > 0) rm.setSelectedRootID(rootID);
         }
      else if (ac == "CHANGE_ROOT_KEY") {
          String rootKey = rm.getSelectedRoot().getRootKey();
          rootKey = JOptionPane.showInputDialog(imw, "Enter the root key: ", rootKey);
          if (rootKey != null && rootKey.length() > 0) rm.setSelectedRootKey(rootKey);
          }
      else if (ac == "DELETE_ROOT") {
         rm.deleteSelectedRoot();
         repaint();
         }
      else if (ac == "RENAME_ALL_WITH") {
          rm.setAllRootID(true);
          repaint();
          }
      else if (ac == "RENAME_ALL_LATERALS") {
          rm.setLateralRootID();
          repaint();
          }
      else if (ac == "RENAME_ALL_WITHOUT") {
          rm.setAllRootID(false);
          repaint();
          }
      else if (ac == "DELETE_NODE") {
         rm.deleteSelectedNode();
         repaint();
         }
      else if (ac == "APPEND_NODES") {
         rm.notifyContinueRootStart();
         tracing = true;
         }  
      else if (ac == "FIND_LATERALS") {
          rm.buildNodesFromCoord(getSkeletonTips());
          repaint();
          }
      
      else if (ac == "FIND_LATERALS_2") {
          rm.findLaterals2();
          repaint();
          }
      
      else if (ac == "ATTACHE_PARENT") {
          rm.setParent();
          repaint();
          }
      
      else if (ac == "DETACHE_PARENT") {
          rm.detacheParent();
          repaint();
          }
      else if (ac == "DETACHE_CHILDREN") {
        int opt = JOptionPane.showConfirmDialog(null, "Do you want to delete all the children?",
              "Delete option", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
          if (opt == JOptionPane.YES_OPTION){
           rm.detacheAllChilds();
             repaint();
          }
          }
     
      else if (ac == "AUTO_DRAW") {
        if(imp.getRoi() != null && imp.getRoi().isLine()){
           Line l = (Line) imp.getRoi();
           rm.autoDrawRoot(l.x1, l.y1, l.x2, l.y2, l.getPixels());
              repaint();
        }
        else JOptionPane.showMessageDialog (null, "Please draw a line","ROI error", JOptionPane.ERROR_MESSAGE);
          
      }
      
      else if (ac == "DELETE_SMALL_ROOTS") {
        SR.write("delete small roots");
          rm.deleteSmallRoots();
          repaint();          
      }
      
      else if (ac == "CROP_SINGLE_ROOT") {
          rm.cropSelectedRoot();
          repaint();
          }
      else if (ac == "MULTIPLY_NODES") {
          rm.getSelectedRoot().multiplyNodes();
          repaint();
          }      
      else if (ac == "DELETE_END_OF_ROOT") {
         rm.deleteEndOfSelectedRoot();
         repaint();
         }
      else if (ac == "DELETE_BASE_OF_ROOT") {
         rm.deleteBaseOfSelectedRoot();
         repaint();
         }
      else if (ac == "SPLIT_ROOT") {
         rm.splitSelectedRoot();
         repaint();
         }
      else if (ac == "REVERSE_ROOT") {
         rm.reverseSelectedRoot();
         repaint();
         }
      else if (ac == "BRING_TO_FRONT") {
         rm.bringSelectedRootToFront();
         }
      else if (ac == "SEND_TO_BACK") {
         rm.sendSelectedRootToBack();
         }
      else if (ac == "CROP_CANDIDATE_CHILDREN") {
         rm.cropCandidateChildren();
         repaint();
         }
      else if (ac == "FILE_IMPORT") {
          
        GenericDialog gd = new GenericDialog("Tracing scale");
          gd.addNumericField("Scaling: ", 1, 1);
          gd.showDialog();
          if (gd.wasCanceled()) return;
          float scale = (float) gd.getNextNumber();    
          
        if(rm.readSeedDataFile(false, scale)){
           
           repaint();
           
           int opt = JOptionPane.showConfirmDialog(null, "Is the tracing OK?",
              "Tracing import", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);          
           if (opt == JOptionPane.NO_OPTION){
              new TranslateDialog(rm);
           }
           
        
        }
      }
      
      else if (ac == "FILE_IMPORT_COMMON") {
        rm.readRSML(null);
        repaint();
      }
      
      else if (ac == "FILE_IMPORT_SAME") {
        GenericDialog gd = new GenericDialog("Tracing scale");
          gd.addNumericField("Scaling: ", 1, 1);
          gd.showDialog();
          if (gd.wasCanceled()) return;
          float scale = (float) gd.getNextNumber();    
          
        if(rm.readSeedDataFile(false, scale)){
           repaint();
           int opt = JOptionPane.showConfirmDialog(null, "Is the tracing OK?",
                 "Tracing import", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
           if (opt == JOptionPane.NO_OPTION){
              new TranslateDialog(rm);
           }
          }
      }

      
      else if (ac == "FILE_IMPORT_MULTIPLE") {
          rm.readMultipleDataFile();
          repaint();
          }

      else if (ac == "TRACE_FOLDER") {
        rm.readImagesFromFolder();
      }
      else if (ac == "CROP_TRACING") {
          rm.cropTracing();
          repaint();
          }
      else if (ac == "RECENTER_ALL") {
          rm.reCenterAllNodes();
          repaint();
          }      
      
      else if (ac == "APPEND_TRACING") {
          rm.appendTracing();
          repaint();
          }
      
      else if (ac == "FILE_RECOVER") {
         rm.selectAndRead();
         repaint();
         }
      else if (ac == "FILE_QUIT_WITHOUT_SAVE") {
         rm.setNoSave(true);
         imp.getWindow().close();
         }
      else if (ac == "FILE_SAVE") {
         rm.save();
         }
      else if (ac == "FILE_SAVE_COMMON") {
          rm.saveToRSML();
          }
      else if (ac == "FILE_CLEAR") {
         rm.clearDatafile();
         repaint();
         }
      
      else if (ac == "MOVE_CANVAS") {
          new TranslateDialog(rm);
      }      
      
//      else if (ac == "SETTINGS_DPI") {
//         String value = Float.toString(rm.getDPI());
//         try {
//            value = JOptionPane.showInputDialog(imw, "Enter the DPI value: ", value);
//            double dpi = Double.parseDouble(value);
//            rm.setDPI((float) dpi);
//         } catch(NullPointerException ex) {
//         } catch(Exception ex) {
//            JOptionPane.showMessageDialog(imw, value + " is not a valid number. The current setting has not been changed.",
//                                      "Error", JOptionPane.ERROR_MESSAGE);
//         }
//         repaint();
//         }
      else if (ac == "RM_ANCHORS") {
         rm.rmRegistrationAnchors();
         repaint();
         }
//      else if (ac == "THRESHOLD_20_BELOW_MAX") {
//         mi[THRESHOLD_ADAPTIVE].setEnabled(true);
//         mi[THRESHOLD_20_BELOW_MAX].setEnabled(false);
//         rm.setThresholdMethod(RootModel.THRESHOLD_ADAPTIVE2);
//         }
//      else if (ac == "THRESHOLD_ADAPTIVE") {
//         mi[THRESHOLD_ADAPTIVE].setEnabled(false);
//         mi[THRESHOLD_20_BELOW_MAX].setEnabled(true);
//         rm.setThresholdMethod(RootModel.THRESHOLD_ADAPTIVE1);
//         }
      else if (ac == "DELETE_MARK") {
         rm.removeSelectedMark();
         repaint();
         }
      else if (ac == "DELETE_ALL_MARKS") {
         rm.removeAllMarks();
         repaint();
         }
      else if (ac == "CHANGE_MARK_VALUE") {
         Mark m = rm.getSelectedMark();
         String value = m.value;
         if (m.type == Mark.ANCHOR) {
            try {
               value = JOptionPane.showInputDialog(imw, "Enter the position of the mark relative to the root origin:", value);
               double v = Double.parseDouble(value);
               value = String.valueOf(Math.round(v * 100.0) / 100.0);
            } catch(NullPointerException ex) {
               value = m.value;
            } catch(Exception ex) {
               JOptionPane.showMessageDialog(imw, value + " is not a valid number. The value has not been changed.",
                                         "Error", JOptionPane.ERROR_MESSAGE);
               value = m.value;
            }
         }
         else {
            value = JOptionPane.showInputDialog(imw, "Update the mark value: ", value);
            if (value == null) value = new String();
         }
         rm.changeSelectedMarkValue(value);
         repaint();
         }
      
      else if (ac.startsWith("ADD_MARK")) {
         int type = Integer.parseInt(ac.substring(ac.lastIndexOf('_') + 1));
         autoMarkType = type;   // XD 201811 (line added)
         addMark(type);  // XD 201811 (block that was here has bene encapsulated in new function addMark()
         }
     
      currentPopup = null;
      lockMousePosition = false;

      }

   // XD 20180811 - function created from block moved from actionPerfrmed() (block untouched) 
   private void addMark(int type) {
      String value = Mark.getDefaultValue(type);
      if(type == Mark.NUMBER) value = ""+(int)rm.getSelectedRoot().getNextNumberMarkValue(rm.getMarkerPosition());
      if (type == Mark.ANCHOR) {
         try {
            value = JOptionPane.showInputDialog(imw, "Enter the position of the mark relative to the root origin:", "0.0");
            double v = Double.parseDouble(value);
            value = String.valueOf(Math.round(v * 100.0) / 100.0);
         } catch(NullPointerException ex) {
            value = "0.0";
         } catch(Exception ex) {
            JOptionPane.showMessageDialog(imw, value + " is not a valid number. A value of 0.0 has been set instead.",
                  "Error", JOptionPane.ERROR_MESSAGE);
            value = "0.0";
         }
         }
      else if (Mark.needsValue(type)) {
         value = JOptionPane.showInputDialog(imw, "Enter a mark value: ", value);
         SR.write("Value = "+value);
      }
      if(value != null) tracingTwinMark = rm.addMark(type, value);
      repaint();
   }
   
   
   
   
   /**
    * 
    */
   public boolean dispatchKeyEvent(KeyEvent e) {
      if (e.getID() == KeyEvent.KEY_PRESSED) keyPressed(e);
      else if (e.getID() == KeyEvent.KEY_RELEASED) keyReleased(e);
      return false;
      }

   /**
    * Action trigger by pressing keys
    * @param e
    */
   public void keyPressed (KeyEvent e) {
      int kc = e.getKeyCode();
      // SR.write("KEY_PRESSED " + kc + " (ESC = " + KeyEvent.VK_ESCAPE + "), popup = " + currentPopup);


      if (kc == KeyEvent.VK_SPACE) IJ.setKeyDown(kc);
      if (Toolbar.getToolId() == Toolbar.MAGNIFIER && kc == KeyEvent.VK_ESCAPE) {
         IJ.setTool(currentTool); 
         e.setKeyCode(KeyEvent.VK_UNDEFINED); // Prevents ImageJ to trap this event
         }
      else if (currentPopup != null && kc == KeyEvent.VK_ESCAPE) {
         currentPopup.setVisible(false);
         currentPopup = null;
         lockMousePosition = false;
         e.setKeyCode(KeyEvent.VK_UNDEFINED); // Prevents ImageJ to trap this event
         }
      else if (mode == RULER && kc != KeyEvent.VK_SPACE) {
         if (kc == KeyEvent.VK_CONTROL) ctrl_key_pressed = true;
         else if (ctrl_key_pressed && kc == KeyEvent.VK_W) {
            SR.getSQLServer().write();
            rm.resetSQLSequence();
            e.setKeyCode(KeyEvent.VK_UNDEFINED); // Prevents ImageJ to process this event
            }
         else if (ctrl_key_pressed && kc == KeyEvent.VK_C) {
            SR.getSQLServer().copyToSystemClipboard();
            rm.resetSQLSequence();
            e.setKeyCode(KeyEvent.VK_UNDEFINED); // Prevents ImageJ to process this event
            }
         }
      else if (mode == TRACE && (tracing == true || drag ==2) &&
            ((kc == KeyEvent.VK_CONTROL && !ctrl_key_pressed) ||
             (kc == KeyEvent.VK_SHIFT && !shift_key_pressed))) {
         Point p = getMousePosition();
         int flag = 0;
         if (kc == KeyEvent.VK_CONTROL) {
            flag |= RootModel.FREEZE_DIAMETER;
            ctrl_key_pressed = true;  // Prevent repeated action when the key is sustained
            }
         if (kc == KeyEvent.VK_SHIFT) {
            shift_key_pressed = true;  // Prevent repeated action when the key is sustained
            flag |= RootModel.SNAP_TO_BORDER;
            }
         if (tracing == true) rm.moveTracingNode(offScreen2DX(p.x), offScreen2DY(p.y), flag);
         else rm.moveSelectedNode(offScreen2DX(p.x), offScreen2DY(p.y), flag);
         repaint();
         }
      }

   /**
    *  Action trigger by releasing keys
    * @param e
    */
   public void keyReleased (KeyEvent e) {
      int kc = e.getKeyCode();
      if (kc == KeyEvent.VK_SPACE) IJ.setKeyUp(kc);
      if (kc == KeyEvent.VK_CONTROL) ctrl_key_pressed = false;
      if (kc == KeyEvent.VK_SHIFT) shift_key_pressed = false;
      if (mode == RULER && kc != KeyEvent.VK_SPACE) {
         e.setKeyCode(KeyEvent.VK_UNDEFINED); // Prevents ImageJ to trap key events in RULER mode
         } 
      else if (mode == TRACE && (tracing == true || drag ==2) &&
            (kc == KeyEvent.VK_CONTROL || kc == KeyEvent.VK_SHIFT)) {
         Point p = getMousePosition();
         int flag = 0;
         if (ctrl_key_pressed) flag |= RootModel.FREEZE_DIAMETER;
         if (shift_key_pressed) flag |= RootModel.SNAP_TO_BORDER;
         if (tracing == true) rm.moveTracingNode(offScreen2DX(p.x), offScreen2DY(p.y), flag);
         else rm.moveSelectedNode(offScreen2DX(p.x), offScreen2DY(p.y), flag);
         repaint();
         }
      }

   /**
    * 
    * @param x
    * @return
    */
   public float offScreen2DX(int x) {
      return (float) (srcRect.x + (x / getMagnification()));
      }
   
   /**
    * 
    * @param y
    * @return
    */
   public float offScreen2DY(int y) {
      return (float) (srcRect.y + (y / getMagnification()));
      }

   /**
    * 
    */
   public void kill() {
      rm = null;                             // XD 20100628
      IJTool.removeMouseListener(this);      // XD 20100628
      kfm.removeKeyEventDispatcher(this);    // XD 20100628
      canvasList.removeElement(this); 
      logNInstances();
      }
   
   /**
    * 
    */
   public void notifySynchronize() {
      for (int i = 0; i < canvasList.size(); i++) {
         if (canvasList.get(i) != this) {
            ((RootImageCanvas) canvasList.get(i)).synchronize();
            }
         }
      }

   /**
    * 
    */
   public void synchronize() {
      Point p = rm.getLocation(sync.getRootID(), sync.getLPos());
      if (p == null) return;
      double newMag = sync.getMagnification();
      // the following is based on ImageCanvas.zoomIn(int x, int y)
      int dstWidth = getWidth();
      int dstHeight = getHeight();
      if (imageWidth * newMag > dstWidth) {
         srcRect.width = (int) Math.ceil(dstWidth / newMag);
         srcRect.height = (int) Math.ceil(dstHeight / newMag);
         srcRect.x = p.x - srcRect.width / 2;
         srcRect.y = p.y - srcRect.height / 2;
         if (srcRect.x < 0) srcRect.x = 0;
         if (srcRect.y < 0) srcRect.y = 0;
         if (srcRect.x + srcRect.width > imageWidth) srcRect.x = imageWidth - srcRect.width;
         if (srcRect.y + srcRect.height > imageHeight) srcRect.y = imageHeight - srcRect.height;
         }
      else {
         srcRect.setSize(imageWidth, imageHeight);
         srcRect.setLocation(0, 0);
         setDrawingSize((int) (imageWidth * newMag), (int) (imageHeight * newMag));
         imp.getWindow().pack();
         }
      setMagnification(newMag);
      repaint();
      }
      
   /**
    * 
    * @param rootID
    * @param lp
    */
   public void traceListenerRuler(String rootID, float lp) {
      boolean paint = rm.makeListenerRulerLine(rootID, lp);
      if (paint) {
         listenerRuler = true;
         Rectangle r = rm.getRulerClipRect();
         repaint(r.x, r.y, r.width, r.height);
         }
      }

   /**
    * 
    * @return
    */
   public boolean isTracing() {return tracing; }

   /**
    * 
    * @return
    */
   public boolean isListenerRuler() {return listenerRuler; }

   /**
    * 
    */
   public void listenerRulerDone() {listenerRuler = false; }
   
   /**
    * 
    * @return
    */
   public RootModel getModel() {return rm;}

   /**
    * Compute the image skeleton and find its tips. 
    * Theses tips will be used as starting point to create new roots
    * Not used right now
    */
   private int[][] getSkeletonTips(){
      
      ImageProcessor ip = imp.getProcessor().duplicate();
      ip.autoThreshold();
      BinaryProcessor bp = new BinaryProcessor(new ByteProcessor(ip, true));
      bp.skeletonize();    
      bp.invert();
      ImagePlus im1 = new ImagePlus(); im1.setProcessor(bp);
      ParticleAnalyzer pa = new ParticleAnalyzer(ParticleAnalyzer.SHOW_MASKS, 0, new ResultsTable(), 100, 10e9, 0, 1);
      pa.analyze(im1);
      ImagePlus globalMask = IJ.getImage(); 
      globalMask.hide();
      bp = new BinaryProcessor(new ByteProcessor(globalMask.duplicate().getProcessor(), true));
      
      ArrayList<Integer> x = new ArrayList<Integer>();
      ArrayList<Integer> y = new ArrayList<Integer>();
      for(int w = 0; w < bp.getWidth(); w++){
         for(int h = 0; h < bp.getHeight(); h++){           
            if(bp.get(w, h) > 125){
               int n = nNeighbours(bp, w, h);
               if(n == 1){
                  x.add(w);
                  y.add(h);
               }
            }
         }   
      }
      
      int[][] coord = new int[x.size()][2];
      for(int i = 0; i < x.size(); i++){
         coord[i][0] = x.get(i);
         coord[i][1] = y.get(i);
      }
      IJ.log(coord.length+"");
      return coord;
      
   }
   
   /**
    * Compute the number of black neigbours for a point
    * @param bp
    * @param w
    * @param h
    * @return
    */
   private int nNeighbours(ImageProcessor bp, int w, int h){
      int n = 0;
      for(int i = w-1; i <= w+1; i++){
         for(int j = h-1; j <= h+1; j++){
            if(bp.getPixel(i, j) > 125) n++;
            if(n == 3) return n-1;
         }
      }
      return n-1;
   }
   
   }


