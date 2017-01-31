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


import ij.*;
import ij.plugin.filter.ParticleAnalyzer;
import ij.process.*;
import ij.gui.PolygonRoi;
import ij.gui.Roi;
import ij.io.*;
import ij.measure.*;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
//import java.awt.image.BufferedImage;
//import java.awt.image.RenderedImage;
import java.awt.font.*;
import java.sql.*;

//import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import java.util.*;
import java.util.List;
import java.io.*;

// XML file support
import javax.xml.parsers.DocumentBuilder; 
import javax.xml.parsers.DocumentBuilderFactory;  
import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;  

//import unused.RLDProfile2D;

//import com.sun.image.codec.jpeg.JPEGCodec;
//import com.sun.image.codec.jpeg.JPEGEncodeParam;
//import com.sun.image.codec.jpeg.JPEGImageEncoder;


/** 
 * This is the main class of SR, handling the whole root model.
 * Most of the actions done on the roots transit by this class.
 * 
 */


//%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
/**
 * 
 * Find the assiociated XML datafile
 *
 */
class IdenticalDataFileFilter extends javax.swing.filechooser.FileFilter {

	   String rootFName;
	   
	   public IdenticalDataFileFilter (String rootFName) {
		  int l = rootFName.length();
		  int crop = l-(l/10);
	      this.rootFName = rootFName.substring(0, crop);
	   }

	   public boolean accept(File f) {
		   return (f.getName().toLowerCase().endsWith("xml") && f.getName().startsWith(rootFName));
	   }

	   public String getDescription() {
	      return "SmartRoot Datafiles associated with " + rootFName;
	   }
}

// %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

/**
 * 
 * Find the associated backup files
 *
 */
class BackupFileFilter extends javax.swing.filechooser.FileFilter {

   String rootFName;
   String[] validFile;

   
   public BackupFileFilter (String rootFName) {
      this.rootFName = rootFName.substring(rootFName.lastIndexOf(File.separator) + 1);
      rootFName = rootFName.substring(0, rootFName.lastIndexOf('.') + 1);
      validFile = new String[RootModel.fileSuffix.length];
      for (int i = 0; i < validFile.length; i++) {
         validFile[i] = rootFName + RootModel.fileSuffix[i];
         }
      }

   public boolean accept(File f) {
      String fName = f.getAbsolutePath();
      for (int i = 0; i < validFile.length; i++) 
         if (fName.equals(validFile[i])) return true;
      return false;
      }

   public String getDescription() {
      return "SmartRoot Datafiles associated with " + rootFName;
      }
   }
   

//%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

/**
* 
* Filter for Image files
*
*/
class ImageFileFilter extends javax.swing.filechooser.FileFilter 
                  implements java.io.FileFilter {

public ImageFileFilter () { }

public boolean accept(File f) {
   return f.getName().toLowerCase().endsWith("jpg") ||
		   f.getName().toLowerCase().endsWith("tiff") ||
		   f.getName().toLowerCase().endsWith("jpeg") ||
		   f.getName().toLowerCase().endsWith("png");
   }

public String getDescription() {
   return "SmartRoot Datafiles";
   }
}

// %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

/**
 * 
 * Filter for XML datafiles
 *
 */
class DataFileFilter extends javax.swing.filechooser.FileFilter 
                     implements java.io.FileFilter {

   public DataFileFilter () { }

   public boolean accept(File f) {
      return f.getName().toLowerCase().endsWith("xml");
      }

   public String getDescription() {
      return "SmartRoot Datafiles";
      }
   }

//%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

/**
 * Filter for RSML datafiles
 * @author guillaumelobet
 *
 */
class DataFileFilterRSML extends javax.swing.filechooser.FileFilter 
implements java.io.FileFilter {

	public DataFileFilterRSML () { }

	public boolean accept(File f) {
		return f.getName().toLowerCase().endsWith("rsml") || f.getName().toLowerCase().endsWith("xml");
	}

	public String getDescription() {
		return "Root System Markup Language";
	}
}
   

// %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

class RootModel extends WindowAdapter implements TreeModel{

    private Vector<TreeModelListener> treeModelListeners = new Vector<TreeModelListener>();
	  
    // Parameters for the lateral finding algorithm
	static int nStep;	
	static float dMin;
	static float dMax;
	static int nIt;
	static boolean checkNSize;
	static boolean checkRDir;
	static boolean checkRSize;	
	static boolean doubleDir;
	static double minNodeSize;
	static double maxNodeSize;
	static double minRootSize;
	static double minRootDistance = 2;
	static float maxAngle;
	
//	static int nextRootKey;
	static String version = "4.21";
	static String datafileKey = "default";	

	public GeneralPath convexhullGP = new GeneralPath();
	static AlphaComposite ac2 = AlphaComposite.getInstance(AlphaComposite.SRC_OVER,0.25f);
	static AlphaComposite ac3 = AlphaComposite.getInstance(AlphaComposite.SRC_OVER,0.60f);
	
	private Opener imgOpener = new Opener();

	
   SRImageWindow win;
   ImagePlus img;
   BinaryProcessor bp;
   ImageProcessor ip;
   RootImageCanvas ric;
   double angleStep;
   NodeFitter fit;
   float threshold;
   float dM=0;
   public ArrayList<Root> rootList = new ArrayList<Root>();
   private ArrayList<RegistrationAnchor> anchorList = new ArrayList<RegistrationAnchor>();
   @SuppressWarnings("rawtypes")
   public TreeMap linkedDatafileList = new TreeMap();
   public int nextAutoRootID;
   private Root selectedRoot, tracingRoot, rootOfTwinMark;
   private Node selectedNode, selectedLinkBase, tracingNode, tracingFromNode;
   private Mark selectedMark, twinMark;

   static final int AREA = 0;
   static final int BORDERS = 2;
   static final int DIAMETERS = 3;
   static final int TICKS = 4;
   static final int NODES = 5;
   static final int AXIS = 1;
   
   boolean displayAxis = true;
   boolean displayNodes = true;
   boolean displayBorders = true;
   boolean displayArea = true;
   boolean displayTicks = true;
   boolean displayMarks = true;
   boolean displayTicksP = true;
   boolean displayConvexHull = true;
   
   boolean selected = false;

   /** Return code from selectNode() */
   static final int NODE = 1;
   static final int ROOT = 2;
   static final int CHILD = 3;
   static final int PARENT = 4;
   static final int CHILDPARENT = 5;

   /** autoBuildFromNode() estimates the putative location for a new node in the direction of the 
       line joining the previous and current nodes, using a distance which is the minimum of 
       putative distances 1 & 2 (see AUTOBUILD_STEP_FACTOR_BORDER AUTOBUILD_STEP_FACTOR_DIAMETER)
       but which is at least equal to AUTOBUILD_MIN_STEP  */
   /** Putative distance 1 (from the current node) is equal to the
       distance to the root border (along the predicted direction) multiplied by the AUTOBUILD_STEP_FACTOR_BORDER */
   /** Putative distance 2 (from the current node) is equal to the
       root diameter at the current node multiplied by the AUTOBUILD_STEP_FACTOR_DIAMETER */
   /** Minimum angle step for the automatic recentering of nodes in autoBuildFromNode() */
   /** Angle step for the automatic recentering of nodes in autoBuildFromNode(): the angle step
       is equal to AUTOBUILD_THETA_STEP_FACTOR divided by the root diameter */


   float AUTOBUILD_MIN_STEP = 3.0f; // 3.0
   float AUTOBUILD_STEP_FACTOR_BORDER = 0.5f; // 0.5
   float AUTOBUILD_STEP_FACTOR_DIAMETER = 2.0f; // 2.0f
   float AUTOBUILD_MIN_THETA_STEP = 0.02f; // 0.02
   float AUTOBUILD_THETA_STEP_FACTOR = (float) Math.PI / 2.0f;



   /** Modifier flags for tracing operations */
   static final int AUTO_TRACE = 1;
   static final int FREEZE_DIAMETER = 2;
   static final int SNAP_TO_BORDER = 4;
   
   static final int THRESHOLD_ADAPTIVE1 = 1;
   static final int THRESHOLD_ADAPTIVE2 = 2; 
   
   static final int REGULAR_TRACING = 1;
   static final int LATERAL_TRACING = 2;
   static final int LINE_TRACING = 4;
   static final int LATERAL_TRACING_ONE = 8;

   static private DataFileFilter datafileFilter = new DataFileFilter();
   static private ImageFileFilter imagefileFilter = new ImageFileFilter();
   static private DataFileFilterRSML datafileFilterRSML = new DataFileFilterRSML();

   private boolean tracingBackwards;
   private AlphaComposite ac1;
   private String directory, dataFName, rulerLine1;
   public String imgName;
   private Line2D rulerLine = new Line2D.Float();
   private Point2D.Float rulerPoint = new Point2D.Float();
   private Rectangle2D rulerRect = new Rectangle2D.Float();
   private OrthogonalLine oLine = new OrthogonalLine();
   private float markerPosition, markerDiameter, markerAngle;
   private GeneralPath rulerGP = new GeneralPath();
   private Font font, currentFont;
   private FontRenderContext frc;
   public float previousMagnification = 0.0f;
   public static final String[] fileSuffix = {"xml", "xml01", "xml02", "xml03", "xml04"};
   public static final String[] fileSuffixRSML = {"rsml", "rsml01", "rsml02", "rsml03", "rsml04"};
   private boolean quitWithoutSave;

   private int sqlSequence = 0;
   private SQLServer sqlServer = SR.getSQLServer();
   private SRWin srWin = SRWin.getInstance();
   private String[] sqlVector = new String[8];
   private float dpi;
   public float pixelSize;

   private boolean tracingNewRoot = false;
   private boolean selectedNodeIsMoving = false;

   private int selectedNodePosition = 0;
   private Node selectedNodePrevNode, selectedNodeNextNode;
   private float selectedRootRulerAtOrigin = 0.0f;
   private float selectedNodeBaseLength = 0.0f;   

   public int manThreshold = 0;
   public float autoThreshold;
   
   public ArrayList<float[]> potentialLateral = new ArrayList<float[]>();
   public ArrayList<Node> potentialParentNodes = new ArrayList<Node>();
   
   
   /**
    * Constructor
    * @param win
    * @param directory
    */
   public RootModel(SRImageWindow win, String directory) {
      this(win, directory, null);
      }
   
   
   /**
    * Constructor
    * @param win
    * @param directory
    * @param dataFName
    */ 
   public RootModel(SRImageWindow win, String directory, String dataFName) {	   
	   
	   
      this.directory = directory;
      img = win.getImagePlus();
      Calibration cal = img.getCalibration();
      this.win = win;

      pixelSize = (float) cal.pixelWidth;
      String unit = cal.getUnit().toUpperCase();
      if (unit.startsWith("CM") || unit.startsWith("CEN")) {
         dpi = (float) (2.54f / pixelSize);
         }
      else if (unit.startsWith("IN") || unit.startsWith("in")) {
         dpi = (float) (1.0f / pixelSize);
         pixelSize *= 2.54f;
         }
      else {
         dpi = 300.0f;
         pixelSize = 2.54f / dpi;
         }
      
      dpi = SR.prefs.getFloat("DPI_default", dpi);
      
      ip = img.getProcessor();

      ImageProcessor ip2 = img.getProcessor().duplicate();
      if(SR.prefs.getBoolean("useBinary", false) & ip2.getWidth() < 5000){
    	  	ip2.autoThreshold();
      		bp = new BinaryProcessor(new ByteProcessor(ip2, true));
      		bp.skeletonize();
      }
      else bp = null;

      ric = (RootImageCanvas) win.getCanvas();
      
      
      // This is a trick to place this RootModel instance on the first position in the sequence of listeners
      // Its gives RootModel a chance to do something before the image is closed by ImageJ.
//      WindowListener[] wl = win.getWindowListeners();
//      for (int i = 0; i < wl.length; win.removeWindowListener(wl[i++]));
      win.addWindowListener(this);
//      for (int i = wl.length; i > 0; win.addWindowListener(wl[--i]));
      ((SRImageWindow)win).attachRootModel(this);
      
      fit = new NodeFitter(img);
      ip.setValue(255.0);

      ac1 = AlphaComposite.getInstance(AlphaComposite.SRC_OVER,1.0f);

      int i = img.getTitle().lastIndexOf('.');
      if (i != -1) imgName = img.getTitle().substring(0, i);
      else imgName = img.getTitle();
      
      if (dataFName == null){
    	  dataFName = directory + File.separator + imgName + "." + fileSuffixRSML[0];
    	  // if no RSML ,try to find an xml file
          if (!(new File(dataFName)).exists()) dataFName = directory + File.separator + imgName + "." + fileSuffix[0];    	  
      }
      this.dataFName = dataFName;
      
      read(dataFName, false);
      quitWithoutSave = false;
      
      ric.attachRootModel(this);
      ric.setImageUpdated();
      srWin.setCurrentRootModel(this, true);
      }
   
   /**
    * Constructor. USed for the global export
    * @param dataFName
    * @param globalDPI
    */
   public RootModel(String dataFName, boolean globalDPI) {	 
	      dpi = SR.prefs.getFloat("DPI_default", dpi);
	      pixelSize = 2.54f / dpi;
	      read(dataFName, false, false, true, globalDPI, 1);	      	      
   }
   
   /**
    * Constructor. USed for the global export
    * @param dataFName
    * @param globalDPI
    * @param globalDPIValue
    */
   public RootModel(String dataFName, boolean globalDPI, float globalDPIValue) {	 
	      dpi = globalDPIValue;
	      pixelSize = 2.54f / dpi;
	      read(dataFName, false, false, true, globalDPI, 1);	      	      
   }
   
   
   /**
    * Computed the scale base on an unit and a resolution
    * @param unit
    * @param resolution
    * @return
    */
   public float getDPI(String unit, float resolution){
	   if (unit.startsWith("cm") || unit.startsWith("cen")) {
	          return resolution * 2.54f;
	          }
	   else if (unit.startsWith("mm") || unit.startsWith("mill")) {
		   return (resolution / 10) * 2.54f;
	   }	 
	   else if (unit.startsWith("m") || unit.startsWith("me")) {
		   return (resolution * 100) * 2.54f;
	   }	 	      
	   else if (unit.startsWith("IN") || unit.startsWith("in")) {
		   return resolution;
	   }
	   else {
		   return 0.0f;
	   }	      
   }
   
   /**
    * 
    * @param x
    * @param y
    * @param flag
    * @param type
    * @return
    */
   public boolean addNode(float x, float y, int flag, int type) {
	   return addNode(x, y, flag, type, "", false, null, null);
   }
   
   /**
    * This method is called by RootImageCanvas. It returns true if the canvas should keep tracing the root
    * @param x
    * @param y
    * @param flag
    * @param type is the root a primary root (1) or an auto generated lateral (2) or and auto generated primary (4)
    * @param name if auto generated lateral, this is its name
    * @param lr is auto generated lateral, is the root on the left (true) or right (false) side of the primary root
    * @param parentR
    * @param parentN
    * @return
    */
   public boolean addNode(float x, float y, int flag, int type, String name, boolean lr, Root parentR, Node parentN) {
      boolean isTracingFirstNode = false;
      x -= 0.5f;
      y -= 0.5f;
      if (tracingRoot == null) fit.calcAdaptiveThreshold((int) x, (int) y, 20); // compute the threshold around the point
      fit.checkImageProcessor();

      /* Line tracing */
      if ((type & LINE_TRACING) != 0){
    	  if (fit.getValue(x,y) > autoThreshold) return (tracingRoot != null);
      }
            
      /* Regular tracing */
      else {
      	 if(fit.getValue(x,y) > fit.threshold) { // check if the point enter the threshold
      		 if ((type & REGULAR_TRACING) != 0) SR.write("clicked outside a root");
      		 if ((type & LATERAL_TRACING_ONE) != 0) SR.write("clicked outside a root");
        	 return (tracingRoot != null);
      	 }
      }
      
      /* Strating the new root */
      if (tracingRoot == null) {
         isTracingFirstNode = true;
         tracingNewRoot = true;  
         
         /* if the root is a lateral, tell it it has a father */ 
         if((type & LATERAL_TRACING) != 0 || (type & LATERAL_TRACING_ONE) != 0) 
        	 tracingRoot = new Root(dpi, parentR, getRootKey());
         else tracingRoot = new Root(dpi, getRootKey(), this);
         
         rootList.add(0,tracingRoot);
         tracingBackwards = false;
         tracingNode = tracingRoot.addNode(x, y, tracingBackwards); // could use a tracingCursor ?
         fit.reCenter(tracingNode, 0.05f, 0.5f, true, flag);
         
         /* if the new lateral root is create, check if the first node can be created there */
         if ((type & LATERAL_TRACING) != 0){
        	 if (checkNSize && !checkNodeSize(parentN.diameter, tracingNode, tracingRoot)) return false;    	  
        	 if (!checkNodePosition(tracingNode, tracingRoot)) return false;
         }
         if ((type & LINE_TRACING) != 0){
        	 if (!checkNodePosition(tracingNode, tracingRoot)) return false;
         }
         
         tracingFromNode = tracingNode;
         tracingNode = tracingRoot.addNode(x, y, tracingBackwards);
         tracingRoot.firstNode.calcBorders();
         selectedRoot = null;
         selectedNode = null;
         if ((flag & AUTO_TRACE) != 0 || (type & LATERAL_TRACING_ONE) != 0) {
             if ((type & LATERAL_TRACING) != 0) fit.suggest(tracingNode, tracingFromNode, parentN, 2, lr, maxAngle);
             else if ((type & LATERAL_TRACING_ONE) != 0) fit.suggest(tracingNode, tracingFromNode, parentN, 2, lr, (float) Math.PI);
             else fit.suggest(tracingNode, tracingFromNode);
            }
         else return true;
      }
      fit.reCenter(tracingNode, 0.05f, 0.5f, true, flag);
      
      /* If the new lateral root is create, check if the second node can be created there */ 
      if ((type & LATERAL_TRACING) != 0){
    	  if (checkNSize && !checkNodeSize(parentN.diameter, tracingNode, tracingRoot)) return false;
    	  if (!checkNodePosition(tracingNode, tracingRoot)) return false;
    	  if (checkRDir && !checkRootDirection(tracingFromNode, parentN, tracingRoot, lr)) return false;
      }
      
      if (tracingBackwards) { 
         tracingNode.length = tracingNode.getDistanceTo(tracingNode.child);
         tracingNode.theta = NodeFitter.vectToTheta(tracingNode, tracingNode.child);
         }
      else { 
         tracingNode.parent.length = tracingNode.parent.getDistanceTo(tracingNode);
         tracingNode.parent.theta = NodeFitter.vectToTheta(tracingNode.parent, tracingNode);
         }
      
      /* Automated tracing */
      
      if ((flag & AUTO_TRACE) != 0 || (type & LATERAL_TRACING_ONE) != 0) {
    	  
         if (tracingBackwards) tracingRoot.reverse();
         if ((type & LATERAL_TRACING) != 0 && !doubleDir) doubleDir = false;
         else if ((type & LATERAL_TRACING_ONE) != 0) doubleDir = false;
         else doubleDir = true;
         
         /* Trace the root in the first direction */
         autoBuildFromNode(tracingRoot, tracingNode);
                 
         /* Trace the root backward */
    	 if (tracingNewRoot && isTracingFirstNode && doubleDir) {
            tracingRoot.reverse();
            try{
            	autoBuildFromNode(tracingRoot, tracingRoot.lastNode);
            }catch(Exception e){}
            if ((type & REGULAR_TRACING) != 0 || (type & LINE_TRACING) != 0) tracingRoot.autoFindApex();
            else tracingRoot.reverse();
    	 }
    	 
    	 /* Re draw the roots */
         tracingRoot.needsRefresh();
         ric.repaint();
         
         /* Set the name of the root */
         if (tracingNewRoot) {
        	if ((type & REGULAR_TRACING) != 0 || (type & LATERAL_TRACING_ONE) != 0){ 
//	            String rootID = tracingRoot.getRootID() + Integer.toString(nextAutoRootID);
	            String rootID = SR.prefs.get("root_ID", "root_") + Integer.toString(nextAutoRootID);
	            if((type & LATERAL_TRACING_ONE) != 0) rootID = SR.prefs.get("lateral_ID", "lat_") + Integer.toString(nextAutoRootID);
	            if(SR.prefs.getBoolean("askName", true)) rootID = JOptionPane.showInputDialog(win, "Enter the root identifier: ", rootID);
	            if (rootID == null || rootID.length() == 0) {
	               int i = rootList.indexOf(tracingRoot);
	               rootList.remove(i);
	               ric.repaint();
	               }
	            else {
	               tracingRoot.setRootID(rootID);
	               nextAutoRootID++;
	               }	            	
            }
        	else{
        		tracingRoot.setRootID(name);
        	}
         }
         
         /* Reset the parameters */
         tracingRoot = null;
         tracingNode = null;
         tracingNewRoot = false;
         
         return false;
         }
      else {
         tracingNode = tracingRoot.addNode(x, y, tracingBackwards);
         moveTracingNode(x, y, flag);
         return true;
         }
      }
   
	/**
	 * Automatically continue the tracing of the current root
	 * @param r
	 * @param n
	 */
	public void autoBuildFromNode(Root r, Node n) { 
	      float x, y;
	      int flag = AUTO_TRACE;
	      fit.checkImageProcessor();
	      fit.clearThresholdMemory();
	
	      boolean stop = false;
	      while (!stop) {
	         float maxStep = fit.seekBorder(n.x, n.y, n.parent.theta, 1.0f) * AUTOBUILD_STEP_FACTOR_BORDER;
	         float step = Math.min((float) (AUTOBUILD_STEP_FACTOR_DIAMETER * n.diameter), maxStep);
	         step = Math.max(step, AUTOBUILD_MIN_STEP);
	         
	         float thetaStep = (float) Math.PI * Math.max(AUTOBUILD_MIN_THETA_STEP, AUTOBUILD_THETA_STEP_FACTOR / n.diameter); 
	         
	         x = n.x + step * (float) Math.cos(n.parent.theta) ;
	         y = n.y - step * (float) Math.sin(n.parent.theta) ;
	         
	         if ((x >= ip.getWidth()) || (x < 0) || (y >= ip.getHeight()) || (y < 0)) {
	            SR.write("Getting out of the image domain");
	            break;
	            }
	
	
	         if (fit.getValue(x,y) <= fit.threshold) { 
	// once the first node is drawn, I could estimate the probability of the pixel value at x,y being in the (same) root
	// if OK for threshold but not really good for probability, that would mean I am in a root but not the same one.
	// Actually if I was making that test after reCentering, I would also get the diameter and orientation. That means also
	// I may use a reference "transition" matrix expressing the probability of changing direction / diameter
	// Actually, I may create such transition matrix using a training phase with a number of roots
	// This transition matrix could be stored (or calculated from an existing datafile)
	            n = r.addNode(x, y, false);
	            int rc = fit.reCenter(n, thetaStep, 0.5f, true, flag);
	            double dist = Math.sqrt(Math.pow((double)(n.x - n.parent.x), 2) + Math.pow((double)(n.y - n.parent.y), 2));
	            // rc == 0 : fit was OK
	            // rc == 1 : unable to fit (ex: touching the image border)
	            // rc == 2 : probably crossing
	            if (rc == 1) {
	               r.rmNode();
	               break;
	               }
	            else if (rc == 2) {
	               // Try moving ahead of the cross
	               // If we can't, let us stop the tracing here
	               //    If the new node lays inside another root, let us crop it
	               for (int i = 0; i < rootList.size(); i++) {
	                  Root r1 = (Root) rootList.get(i);
	                  if (r1 != r && r1.contains(n.x, n.y)) {
	                     cropAtIntersection(r1, n);
	                     //SR.write("Cropped at intersection with " + r1.rootID);
	                     break;
	                     }
	                  }
	               break;
	               } 
	            else if (n.diameter > 1.5f * n.parent.diameter) {
	               //SR.write("diameter getting too large"); // The freezediameter will prevent this to happen...
	               r.rmNode();
	               break;
	               }
	            else if (rc == 0 && n.diameter < 0.4f * n.parent.diameter) {
	               //SR.write("diameter getting too small"); // may work if I get the 2 (border - image) diameters...
	               r.rmNode();
	               break;
	               }
	            else if (rc == 0 && n.diameter == 1) {
	                //SR.write("diameter getting too small"); // may work if I get the 2 (border - image) diameters...
	                r.rmNode();
	                break;
	                }
	            else if (rc == 0 && dist < n.parent.diameter){
	            	r.rmNode();
	            	}
	            else if (rc == 0 && n.diameter > dist){
	            	r.rmNode(n.parent);
	            	}    
	            }
	         else { 
	            SR.log("AutobuildFromNode: findEnd");
	            n = r.addNode(0.0f, 0.0f, false);
	            int rc = fit.findEnd(n.parent, n);
	            SR.log("AutobuildFromNode: findEnd -> " + rc);
	            if (rc == NodeFitter.FIND_END_DONE) break;
	            else if (rc == NodeFitter.FIND_END_ABORTED) {
	               r.rmNode();
	               break;
	               }
	            }
	         }
	      if(r.lastNode.diameter == 1){
	    	  r.rmNode();
	      }
	      r.firstNode.calcCLength(0.0f);
	//      r.autoFindApex();
	      return;
	   }

   /**
    * 
    */
	public void bringSelectedRootToFront() {
      int i = rootList.indexOf(selectedRoot);
      rootList.add(0, rootList.remove(i));
      }
 
   /**
    * 
    */
	public void sendSelectedRootToBack() {
      int i = rootList.indexOf(selectedRoot);
      rootList.add(rootList.remove(i));
      }
	
   /**
    * Crop root at intersection with the node n 
    * @param r
    * @param n
    */
	public void cropAtIntersection(Root r, Node n) {
      // get Node n as close as possible to the border of Root r
      Node p = null;
      float length = 0.0f;
      if (n.parent != null && n.child == null) {
         p = n.parent;
         length = p.length;
         }
      else if (n.parent == null && n.child != null) {
         p = n.child;
         length = n.length;
         }
      else return;
      float dx = (n.x - p.x) / length;
      float dy = (n.y - p.y) / length;
      float dir = -1.0f;
      for (float s = length / 2.0f; s > 0.5f; s /= 2.0f) {
         n.x += dir * s * dx;
         n.y += dir * s * dy;
         dir = r.contains(n.x, n.y) ? -1.0f : 1.0f;
         }
      fit.reCenter(n, 0.05f, 0.5f, true, FREEZE_DIAMETER);
      }

   /**
    * 
    */
   public void cropCandidateChildren() {cropCandidateChildren(selectedRoot);}

   /**
    * 
    * @param r
    */
   public void cropCandidateChildren(Root r) {
      for (int i = 0; i < rootList.size(); i++) {
         Root r1 = (Root) rootList.get(i);
         Node n = r1.firstNode;
         if (r1 != r && r.contains(n.x, n.y)) { 
            cropAtIntersection(r, n);
            r1.calcTicks();
            r1.needsRefresh();
            }
         }
      }

   
   /**
    * Selected the node at position x/y
    * @param x
    * @param y
    * @return
    */
   public int selectNode(float x, float y) {
      x -= 0.5f; 
      y -= 0.5f;
      selectedRoot = null;
      selectedNode = null;
      for (int i = 0; i < rootList.size(); i++) {
         Root r = (Root) rootList.get(i);
         if (tracingRoot != null && r == tracingRoot) continue;  // new as of feb2004 because tracingRoot is in rootList
         if (r.contains(x, y)) {
 	  		//SR.write("Select x = "+x+" / y = "+y);
            selectedRoot = r;
            selectedNode = r.getNode(x, y);            
            return (selectedNode == null ? ROOT : NODE);
            }
         }
      return 0;
      }
   
   
   /**
    * Select root at positon x/y
    * @param x
    * @param y
    * @return
    */
   public int selectRoot(float x, float y) {
	      x -= 0.5f; 
	      y -= 0.5f;
	      selectedRoot = null;
	      for (int i = 0; i < rootList.size(); i++) {
	         Root r = (Root) rootList.get(i);
	         if (tracingRoot != null && r == tracingRoot) continue;  // new as of feb2004 because tracingRoot is in rootList
	         if (r.contains(x, y)) {
	            selectedRoot = r;
	            if (r.isChild() !=0 && r.childList.size() > 0) return CHILDPARENT;
	            else if (r.isChild() != 0) return CHILD;
	            else if (r.childList.size() > 0) return PARENT;
	            else return ROOT;
	            }
	         }
	      return 0;
	      }
   
   /**
    * Select MArk at position x/y
    * @param x
    * @param y
    * @return
    */
   public Mark selectMark(float x, float y) {
      x -= 0.5f; 
      y -= 0.5f;
      selectedMark = null;
      for (int i = 0; i < rootList.size(); i++) {
         Root r = (Root) rootList.get(i);
         Mark m = r.getMarkAt(x, y);
         if (m != null) {
            selectedMark = m;
            selectedRoot = r;
            return selectedMark;
            }
         }
      return null;
      }

   /**
    * 
    * @param x
    * @param y
    * @return
    */
   public boolean selectNearestLink(float x, float y) {
      selectedRoot = null;
      selectedNode = null;
      selectedLinkBase = null;
      Node n1, n2;
      double a, b, c, d, minD = 1e4;
      for (int i = 0; i < rootList.size(); i++) {
         Root r = (Root) rootList.get(i);
         n1 = r.firstNode;
         while ((n2 = n1.child) != null) {
            a = sqr(x - n1.x) + sqr(y - n1.y);
            b = sqr(x - n2.x) + sqr(y - n2.y);
            c = sqr(n1.length);
//            if (Math.abs(a - b) > c) d = Math.min(a, b); // closest node
            if (Math.abs(a - b) > c) d = 1e4; // closest node: do not consider 
            else d = a - sqr((a - b + c) / (2 * n1.length)); // closest link
            if (d < minD) {
               selectedRoot = r;
//               selectedNode = a < b ? n1 : n2; // Should not be used by calling methods.
               selectedLinkBase = n1;
               minD = d;
               }
            n1 = n2;
            }
         }
      return (selectedRoot != null);
      }

   /**
    * 
    * @param x
    * @param y
    * @return
    */
   public float getDistNearestLink(float x, float y) {
	      selectedRoot = null;
	      selectedNode = null;
	      selectedLinkBase = null;
	      Node n1, n2;
	      double a, b, c, d, minD = 1e4;
	      for (int i = 0; i < rootList.size(); i++) {
	         Root r = (Root) rootList.get(i);
	         n1 = r.firstNode;
	         while ((n2 = n1.child) != null) {
	            a = sqr(x - n1.x) + sqr(y - n1.y);
	            b = sqr(x - n2.x) + sqr(y - n2.y);
	            c = sqr(n1.length);
	            if (Math.abs(a - b) > c) d = 1e4; // closest node: do not consider 
	            else d = a - sqr((a - b + c) / (2 * n1.length)); // closest link
	            if (d < minD) {
	               selectedRoot = r;
	               selectedLinkBase = n1;
	               minD = d;
	               }
	            n1 = n2;
	            }
	         }
	      return (float) minD;
	      }
   

   /**
    * Square root function
    * @param v
    * @return
    */
   public double sqr(double v) {return v * v; }

   /**
    * Rebuild the root from the selected node.
    * @param x
    * @param y
    * @param flag
    */
   public void rebuildFromSelectedNode(float x, float y, int flag) {
      boolean reverse = false;
      if (selectedRoot.firstNode == selectedNode) {
         selectedRoot.reverse();
         reverse = true;
         } 
      selectedRoot.rmEndOfRoot(selectedNode, this, true);
      selectedNode.x = x - 0.5f;
      selectedNode.y = y - 0.5f;
      fit.reCenter(selectedNode, 0.05f, 0.5f, true, flag);
      autoBuildFromNode(selectedRoot, selectedNode);
      if (reverse) selectedRoot.reverse();
      selectedRoot.needsRefresh();
      selectedNodeIsMoving = false;
      ric.repaint();
      }

   /**
    * Rebuild the selected node.
    */
   public void rebuildSelectedNode() {
      if (isFirstNode()) selectedNode.calcCLength(0.0f);
      else selectedNode.parent.calcCLength();
      selectedNodeIsMoving = false;
      }

   /**
    * Is the selected node the end of the root (first or last)
    * @return
    */
   public boolean isEndOfRoot() {
      return (selectedNode == selectedRoot.firstNode || 
              selectedNode == selectedRoot.lastNode );
      }
   
   /**
    * Is the selected node the end of the root (first)
    * @return
    */
   public boolean isFirstNode() {return selectedRoot.firstNode == selectedNode; }
   
   /**
    * Is the selected node the end of the root (last)
    * @return
    */
   public boolean isLastNode() {return selectedRoot.lastNode == selectedNode; }

   /**
    * Does the node need to be connected
    * @return
    */
   public boolean needConnect() {
      return (selectedRoot != null && selectedRoot != tracingRoot &&
              (selectedNode == selectedRoot.firstNode ||
               selectedNode == selectedRoot.lastNode));
      }

   /**
    * Connect two roots
    */
   public void connect() {
      tracingRoot.rmNode(tracingNode);
      if (tracingBackwards) {
         if (selectedNode == selectedRoot.firstNode) reverseSelectedRoot();
         selectedRoot.append(tracingRoot);
         int i = rootList.indexOf(tracingRoot);
         rootList.remove(i);
         tracingRoot = null;
         }
      else {   // tracing to apex
         if (selectedNode == selectedRoot.lastNode) reverseSelectedRoot();
         tracingRoot.append(selectedRoot);
         int i = rootList.indexOf(selectedRoot);
         rootList.remove(i);
         ric.repaint();
         if (tracingNewRoot == true) {
            String rootID = tracingRoot.getRootID();
            rootID = IJ.getString("Enter the root identifier: ", rootID);
            if (rootID.length() > 0) tracingRoot.setRootID(rootID);
            tracingNewRoot = false;
            }
         tracingRoot = null;
         }
      }

   /**
    * Reverse the ortiention of the selected root
    */
   public void reverseSelectedRoot() {
      selectedRoot.reverse();
      }

   /**
    * 
    */
   public void notifyContinueRootStart() {
      tracingBackwards = (selectedNode == selectedRoot.firstNode);
      tracingRoot = selectedRoot;
      tracingFromNode = selectedNode;
      tracingNode = tracingRoot.addNode(selectedNode.x, selectedNode.y, tracingBackwards);
      selectedRoot = null;
      selectedNode = null;
      }

   
   /**
    * 
    */
   public void notifyContinueRootEnd() {
      tracingRoot.rmNode(tracingNode);
      repaint();
      tracingRoot.firstNode.calcCLength(0.0f);

      if (tracingBackwards)  tracingRoot.setRulerAtOrigin(0.0f); 
      
      tracingRoot.needsRefresh();
      
      if (tracingNewRoot == true) {
//         String rootID = tracingRoot.getRootID() + Integer.toString(nextAutoRootID);
//         rootID = JOptionPane.showInputDialog(win, "Enter the root identifier: ", rootID);
          String rootID = SR.prefs.get("root_ID", "root_") + Integer.toString(nextAutoRootID);
          rootID = JOptionPane.showInputDialog(win, "Enter the root identifier: ", rootID);
         if (rootID == null || rootID.length() == 0) {
            int i = rootList.indexOf(tracingRoot);
            rootList.remove(i);
            }
         else tracingRoot.setRootID(rootID);
         tracingNewRoot = false;
         }

      tracingRoot = null;
      tracingNode = null;
      }

   
   /**
    * Move the currently traced node
    * @param x
    * @param y
    * @param flag
    */
   public void moveTracingNode(float x, float y, int flag) {
      tracingNode.move(x - 0.5f, y - 0.5f);
      fit.reCenter(tracingNode, 0.05f, 0.5f, true, flag);
      if (tracingNode == tracingRoot.firstNode) tracingRoot.setRulerAtOrigin(0.0f);
      tracingRoot.needsRefresh();
      }
   
   
   /**
    * Move the selected node to an x/y position
    * @param x
    * @param y
    * @param flag
    */
   public void moveSelectedNode(float x, float y, int flag) {
      if (!selectedNodeIsMoving) {
         selectedNodePosition = selectedRoot.getNodePositionRelativeToRulerOrigin(selectedNode);
         selectedNodePrevNode = (selectedNode.parent == null) ? selectedNode : selectedNode.parent;
         selectedNodeNextNode = (selectedNode.child == null) ? selectedNode : selectedNode.child;
         selectedNodeBaseLength = selectedNodeNextNode.cLength - selectedNodePrevNode.cLength;
         selectedRootRulerAtOrigin = selectedRoot.getRulerAtOrigin();
         selectedNodeIsMoving = true;
         }      
      selectedNode.move(x - 0.5f, y - 0.5f);
      fit.reCenter(selectedNode, 0.05f, 0.5f, true, flag); 
      if (selectedNodePosition == 0) selectedRoot.setRulerAtOrigin(0.0f);
      else if (selectedNodePosition == -1) 
         selectedRoot.setRulerAtOrigin(selectedRootRulerAtOrigin - 
                                       (selectedNodePrevNode.getDistanceTo(selectedNodeNextNode) - 
                                        selectedNodeBaseLength));
      selectedRoot.needsRefresh();
      }

   /**
    * Set the name  (ID) of the selected root
    * @param ID
    */
   public void setSelectedRootID(String ID) {
      selectedRoot.setRootID(ID);
      }

   /**
    * Set the name  (ID) of all the  roots
    * @param prefix
    */
   public void setAllRootID(boolean prefix) {
	   if(prefix){
	   		int idP = 1;
	   		int idL = 1;
	   		String prP = SR.prefs.get("root_ID", "root_");
	   		String prL = SR.prefs.get("lateral_ID", "lat_");
	   		for(int i = 0 ; i < rootList.size(); i++){
	    	  	Root r = (Root) rootList.get(i);
		   		if(r.isChild() == 0){
		   			r.setRootID(prP+idP);
			   		idP ++;
		   		}
		   		if(r.isChild() > 0){
		   			r.setRootID(prL+idL);
			   		idL ++;
		   		}
	   		}
	   		for(int i = 0 ; i < rootList.size(); i++){
	    	  	Root r = (Root) rootList.get(i);
		   		r.updateRoot();
	   		}
	   		SR.write("All names changed");
	   }
	   else{
	   		int id = 1;
	   		for(int i = 0 ; i < rootList.size(); i++){
	    	  	Root r = (Root) rootList.get(i);
		   		r.setRootID(""+id);
		   		id ++;
	   		}
	   		for(int i = 0 ; i < rootList.size(); i++){
	    	  	Root r = (Root) rootList.get(i);
		   		r.updateRoot();
	   		}
	   		SR.write("All names changed");
	   }
   }

   /**
    * Return the id of the selected root
    * @return
    */
   public String getSelectedRootID() {
	   if(selectedRoot != null){
		   return selectedRoot.getRootID();
	   }
	   else return null;
	   }

   /**
    * Return the selected root
    * @return
    */
   public Root getSelectedRoot() {
      return selectedRoot;
      }

   /**
    * Draw a ruler line along the root axis
    * @param x
    * @param y
    */
   public void makeRulerLine(float x, float y) {  // This may evolve like Root.getAttributesAt()
      x -= 0.5; // added 20090429 - the mouse pointer is offset +0.5 in x and y. Not sure where this comes from.  
      y -= 0.5;
      float dx, dy;
      if (!selectNearestLink(x, y)) {
         rulerLine.setLine(0.0f, 0.0f, 0.0f, 0.0f);         
         rulerRect.setRect(0.0f, 0.0f, 0.0f, 0.0f);         
         rulerLine1 = " ";
         return;
         }

      oLine.setMouseAndNodes(x, y, selectedLinkBase, selectedLinkBase.child);
      rulerLine.setLine(oLine.getRulerLine());
      rulerPoint.setLocation(oLine.getRulerTextLocation());
      dx = selectedLinkBase.x - oLine.getXSect();
      dy = selectedLinkBase.y - oLine.getYSect();
      if (oLine.intersects()) {
         markerPosition = pixelSize * 
                          (selectedLinkBase.cLength + selectedRoot.rulerAtOrigin +
                           (float) Math.sqrt(dx * dx + dy * dy));
         markerDiameter = pixelSize * oLine.getDiameter();
         markerAngle = selectedLinkBase.theta;
         }
      else {
         float sign = (selectedLinkBase == selectedNode) ? -1.0f : 1.0f;
         markerPosition = pixelSize * 
                          (selectedLinkBase.cLength + selectedRoot.rulerAtOrigin +
                           sign * (float) Math.sqrt(dx * dx + dy * dy));
         markerDiameter = Float.NaN;
         markerAngle = Float.NaN;
         }

      float m = (float) ric.getMagnification();
      if(selectedRoot.isChild() == 0)rulerLine1 = selectedRoot.getRootID();
      else rulerLine1 = selectedRoot.getRootID()+" / "+selectedRoot.getParentName();
      TextLayout layout1 = new TextLayout(rulerLine1, font, frc);
      rulerRect = layout1.getBounds();
      rulerPoint.x += 6.0f / m;
      rulerPoint.y -= 6.0f / m;
      rulerRect.setRect(rulerPoint.getX() - 2.0f / m, 
                        rulerPoint.getY() - (rulerRect.getHeight() + 2.0f) / m, 
                        (rulerRect.getWidth() + 4.0f) / m,
                        (rulerRect.getHeight() + 4.4f) / m);
      rulerGP.append(rulerLine, false);
      rulerGP.append(rulerRect, false);
      }
      
      
   /** This is the (light) marker line which shows up in non active windows
    * 
    * @param rootID
    * @param lp
    * @return
    */
   public boolean makeListenerRulerLine(String rootID, float lp) {
      for (int i = 0; i < rootList.size(); i++) {
         Root r = (Root) rootList.get(i);
         if (r.getRootID().equals(rootID)) {
            rulerLine = r.getRulerLine(lp);
            rulerGP.append(rulerLine, false);
            return true;
            }
         }
      return false;
      }
      
   /** 
    * ric uses this method to narrow the clip of application-requested renderings (repaint());
    * @return
    */
   public Rectangle getRulerClipRect() {
      float magnification = (float) ric.getMagnification();
      Rectangle srcRect = ric.getSrcRect();
      Rectangle r = rulerGP.getBounds();
      r.setRect(magnification * (r.x - srcRect.getX()),
                magnification * (r.y - srcRect.getY()),
                magnification * (r.width),
                magnification * (r.height));
      r.grow(2, 2);
      return r;
      }

   /**
    * 
    * @return
    */
   public float getMarkerPosition() {return markerPosition; }

   /**
    * 
    */
   public void resetSQLSequence() {sqlSequence = 0;}

   /**
    * Prepare the SQL connection
    */
   public void sqlPrepare() {
      int sqlSequenceEnd = 1;
      if (sqlSequence == 0) {
         sqlVector[SQLServer.IMAGE] = imgName;
         sqlVector[SQLServer.NAME] = selectedRoot.getRootID();
         sqlVector[SQLServer.LENGTH] = 
            Float.toString(pixelSize * selectedRoot.getRootLength());
         sqlVector[SQLServer.LPOS] = Float.toString(markerPosition);
         sqlVector[SQLServer.DIAMETER] = Float.toString(markerDiameter);
         sqlVector[SQLServer.ANGLE] = Float.toString(markerAngle);
         sqlVector[SQLServer.PARENT] = "";
         sqlVector[SQLServer.LPOS_PAR] = "";
         if (sqlSequence < sqlSequenceEnd) sqlSequence++;
         }
      else {
         sqlVector[SQLServer.PARENT] = selectedRoot.getRootID();
         sqlVector[SQLServer.LPOS_PAR] = Float.toString(markerPosition);
         sqlSequence = 0;
         }
      sqlServer.setValues(sqlVector);

      }

   
   public void sqlSendGrowthRates(String table, boolean create){
	   sqlSendGrowthRates(table, create, imgName);
   }
   
   public void sqlSendGrowthRates(String table, boolean create, String name){
	      Statement sql = sqlServer.getStatement();
	      if (sql == null) return;
	      if (create) {
	         try {
	            sql.executeUpdate("DROP TABLE " + table);
	            }
	         catch (SQLException sqlE) {
	            }
	         try {
	             sql.executeUpdate("CREATE TABLE " + table + " (image CHAR(50), root CHAR(200), root_name CHAR(50));");  // XD 20110629
	             sql.executeUpdate("ALTER TABLE " + table + " ADD COLUMN position FLOAT;");
	             sql.executeUpdate("ALTER TABLE " + table + " ADD COLUMN angle FLOAT;");
	             sql.executeUpdate("ALTER TABLE " + table + " ADD COLUMN posX FLOAT;");
	             sql.executeUpdate("ALTER TABLE " + table + " ADD COLUMN posY FLOAT;");
	             sql.executeUpdate("ALTER TABLE " + table + " ADD COLUMN root_order INTEGER;");
	             sql.executeUpdate("ALTER TABLE " + table + " ADD COLUMN root_ontology CHAR(24);");
	             sql.executeUpdate("ALTER TABLE " + table + " ADD COLUMN date CHAR(24);");
	             sql.executeUpdate("ALTER TABLE " + table + " ADD COLUMN growth FLOAT;");  
	            }
	         catch (SQLException sqlE) {
	            SR.write("The table " + table + " could not be created (SQL CREATE TABLE)");
	            SR.write(sqlE.getMessage());
	            return;
	            }
	         SR.write("The table " + table + " was successfully created");
	         }
	      
	      String stmt = null;
	      try {
	          for (int i = 0; i < rootList.size(); i++) {
	             Root r = (Root) rootList.get(i);
	             if(r.markList.size() > 1){
	            	 List<Mark> l = new ArrayList<Mark>();
	            	 for(int j = 0; j < r.markList.size(); j++){
	            		 Mark v = r.markList.get(j);
	            		 if(v.type == Mark.FREE_TEXT || v.type == Mark.NUMBER){
	            			int k = 0;
	            		 	while(k < l.size() && Float.valueOf(l.get(k).value) < Float.valueOf(v.value)) k++;
		            		l.add(k, v);	  
	            		 	
	            		 }
	            	 }
	            	 Mark mPrev = l.get(0);
	            	 for(int j = 1; j < l.size(); j++){
	            		 Mark m = l.get(j);
		                 Point p = r.getLocation(m.lPos * pixelSize);
	            		 float growth = ((m.lPos * pixelSize) - (mPrev.lPos * pixelSize))/(Float.valueOf(m.value)-Float.valueOf(mPrev.value)); 
	            		 mPrev = l.get(j);
	                     stmt = "INSERT INTO " + table + " VALUES (";
	                     stmt = stmt.concat("'" + name + "', ");
	                     stmt = stmt.concat("'" + r.getRootKey() + "', ");
	                     stmt = stmt.concat("'" + r.getRootID() + "', ");
	                     stmt = stmt.concat(m.lPos * pixelSize + ",  ");
	                     stmt = stmt.concat(m.angle + ",  ");
	                     if (p != null) {
	                         stmt = stmt.concat(p.x * pixelSize + ", ");
	                         stmt = stmt.concat(p.y * pixelSize + ", ");
	                      }
	                     stmt = stmt.concat(r.isChild() + ",  ");
	                     stmt = stmt.concat(r.getPoAccession() + ",  ");
	                     stmt = stmt.concat("'" + m.value + "', ");
	                     stmt = stmt.concat(growth+")");
	                     sql.executeUpdate(stmt);
	            		 
	            	 }
	             }
	          }
	      }
	      catch (SQLException e) {
	          SR.write("Transfer error in SQL statement: " + stmt);
	          SR.write(e.getMessage());
	          }
	       SR.write("SQL data transfer completed for 'GrowthRate'.");
	   
   }
   public void csvSendGrowthRate(PrintWriter pw, boolean header){
	   csvSendGrowthRate(pw, header, imgName, true);
   }
   public void csvSendGrowthRate(PrintWriter pw, boolean header, String name, boolean last){
	   
	   if(header) pw.println("image, root, root_name, position, angle, posX, posY, root_order, root_ontology, date, growth");
	   
      String stmt = null;
      for (int i = 0; i < rootList.size(); i++) {
         Root r = (Root) rootList.get(i);
         if(r.markList.size() > 1){
        	 List<Mark> l = new ArrayList<Mark>();
        	 for(int j = 0; j < r.markList.size(); j++){
        		 Mark v = r.markList.get(j);
        		 if(v.type == Mark.FREE_TEXT || v.type == Mark.NUMBER){
        			int k = 0;
        		 	while(k < l.size() && Float.valueOf(l.get(k).value) < Float.valueOf(v.value)) k++;
            		l.add(k, v);	  
        		 	
        		 }
        	 }
        	 Mark mPrev = l.get(0);
        	 for(int j = 1; j < l.size(); j++){
        		 Mark m = l.get(j);
                 Point p = r.getLocation(m.lPos * pixelSize);
        		 float growth = ((m.lPos * pixelSize) - (mPrev.lPos * pixelSize))/(Float.valueOf(m.value)-Float.valueOf(mPrev.value)); 
        		 mPrev = l.get(j);
                 stmt =  name + ", ";
                 stmt = stmt.concat(r.getRootKey() + ", ");
                 stmt = stmt.concat(r.getRootID() + ", ");
                 stmt = stmt.concat(m.lPos * pixelSize + ", ");
                 stmt = stmt.concat(m.angle + ", ");
                 if (p != null) {
                     stmt = stmt.concat(p.x * pixelSize + ", ");
                     stmt = stmt.concat(p.y * pixelSize + ", ");
                  }
                 stmt = stmt.concat(r.isChild() + ",  ");
                 stmt = stmt.concat(r.getPoAccession() + ",  ");
                 stmt = stmt.concat(m.value + ", ");
                 stmt = stmt.concat(growth+"");
                 pw.println(stmt);
                 if(last) pw.flush();
        	 }
         }
      }
      SR.write("CSV data transfer completed for 'Root Growth'.");     
   }
   

   /**
    * 
    */
   public void sqlSendMarks(String table, boolean create) {
	   sqlSendMarks(table, create, imgName);
   }
   
   /**
    * 
    */
   public void sqlSendMarks(String table, boolean create, String name) {
      Statement sql = sqlServer.getStatement();
      if (sql == null) return;
      if (create) {
         try {
            sql.executeUpdate("DROP TABLE " + table);
            }
         catch (SQLException sqlE) {
            }
         try {
             sql.executeUpdate("CREATE TABLE " + table + " (Img CHAR(50), source CHAR(32), root CHAR(200), root_name CHAR(50));");  // XD 20110629
             sql.executeUpdate("ALTER TABLE " + table + " ADD COLUMN mark_type CHAR(24);");  // XD 20110629
             sql.executeUpdate("ALTER TABLE " + table + " ADD COLUMN position FLOAT;");
             sql.executeUpdate("ALTER TABLE " + table + " ADD COLUMN diameter FLOAT;");
             sql.executeUpdate("ALTER TABLE " + table + " ADD COLUMN angle FLOAT;");
             sql.executeUpdate("ALTER TABLE " + table + " ADD COLUMN x FLOAT;");
             sql.executeUpdate("ALTER TABLE " + table + " ADD COLUMN y FLOAT;");
             sql.executeUpdate("ALTER TABLE " + table + " ADD COLUMN root_order INTEGER;");
             sql.executeUpdate("ALTER TABLE " + table + " ADD COLUMN root_ontology CHAR(50);");
             sql.executeUpdate("ALTER TABLE " + table + " ADD COLUMN value CHAR(24);");

            
            }
         catch (SQLException sqlE) {
            SR.write("The table " + table + " could not be created (SQL CREATE TABLE)");
            SR.write(sqlE.getMessage());
            return;
            }
         SR.write("The table " + table + " was successfully created");
         }
      
      String stmt = null;
      try {
         for (int i = 0; i < rootList.size(); i++) {
            Root r = (Root) rootList.get(i);
            // Root origin information
            stmt = "INSERT INTO " + table + " VALUES (";
            stmt = stmt.concat("'" + name + "', ");
            stmt = stmt.concat("'" + name + "', ");  // XD 20110629
            stmt = stmt.concat("'" + r.getRootKey() + "', ");
            stmt = stmt.concat("'" + r.getRootID() + "', ");
            stmt = stmt.concat("'Origin', ");
            stmt = stmt.concat("0.0, 0.0, 0.0, ");
            stmt = stmt.concat(r.firstNode.x * pixelSize + ", ");
            stmt = stmt.concat(r.firstNode.y * pixelSize + ", ");
            stmt = stmt.concat(r.isChild()+ ", ");
            stmt = stmt.concat("'" + r.getPoAccession() + "', ");
            stmt = stmt.concat("'" + imgName + "');");
            sql.executeUpdate(stmt);
            // Marks information
            for (int j = 0; j < r.markList.size(); j++) {
               Mark m = (Mark)r.markList.get(j);
               Point p = r.getLocation(m.lPos * pixelSize);
               stmt = "INSERT INTO " + table + " VALUES (";
               stmt = stmt.concat("'" + imgName + "', ");
               stmt = stmt.concat("'" + (m.isForeign ? m.foreignImgName : imgName) + "', ");  // XD 20110629
               stmt = stmt.concat("'" + r.getRootKey() + "', ");
               stmt = stmt.concat("'" + r.getRootID() + "', ");
               stmt = stmt.concat("'" + Mark.getName(m.type) + "', ");
               stmt = stmt.concat(r.lPosPixelsToCm(m.lPos) + ", ");
               stmt = stmt.concat(m.diameter * pixelSize + ", ");
               stmt = stmt.concat(m.angle + ", ");
               if (p != null) {
                  stmt = stmt.concat(p.x * pixelSize + ", ");
                  stmt = stmt.concat(p.y * pixelSize + ", ");
               }
               else {
                  SR.write("[WARNING] " + Mark.getName(m.type) + " mark '" + m.value + "' on root '"+ r.getRootID() + "' is past the end of root.");
                  stmt = stmt.concat(" 0.0, 0.0, ");
               }
               stmt = stmt.concat(r.isChild()+ ", ");
               stmt = stmt.concat("'" + r.getPoAccession() + "', ");
               if (m.needsTwinPosition()) 
                  stmt = stmt.concat("'" + ((m.twinLPos - m.lPos) * pixelSize) + "');");
               else stmt = stmt.concat("'" + m.value + "');");
               sql.executeUpdate(stmt);
               }
            // Root end information
            stmt = "INSERT INTO " + table + " VALUES (";
            stmt = stmt.concat("'" + imgName + "', ");
            stmt = stmt.concat("'" + imgName + "', ");  // XD 20110629
            stmt = stmt.concat("'" + r.getRootKey() + "', ");
            stmt = stmt.concat("'" + r.getRootID() + "', ");
            stmt = stmt.concat("'Length', ");
            stmt = stmt.concat(r.lPosPixelsToCm(r.getRootLength()) + ", ");
            stmt = stmt.concat("0.0, 0.0, ");
            stmt = stmt.concat(r.lastNode.x * pixelSize + ", ");
            stmt = stmt.concat(r.lastNode.y * pixelSize + ", ");
            stmt = stmt.concat(r.isChild() + ", ");
            stmt = stmt.concat("'" + r.getPoAccession() + "', ");
            stmt = stmt.concat("'" + imgName + "');");
            sql.executeUpdate(stmt);
            }
         }
      catch (SQLException e) {
         SR.write("Transfer error in SQL statement: " + stmt);
         SR.write(e.getMessage());
         }
      SR.write("SQL data transfer completed for 'Marks'.");
   }
   
   public void csvSendMarks(PrintWriter pw, boolean header){
	   csvSendMarks(pw, header, imgName, true);
   }
   public void csvSendMarks(PrintWriter pw, boolean header, String name, boolean last){
	   
	   if(header)
		   pw.println("image, source, root, root_name, mark_type, position_from_base, diameter, angle, x, y, root_order, root_ontology, value");
	   String stmt;
       for (int i = 0; i < rootList.size(); i++) {
           Root r = (Root) rootList.get(i);
           // Root origin information
           stmt = name + ", ";
           stmt = stmt.concat(imgName + ", ");  // XD 20110629
           stmt = stmt.concat(r.getRootKey() + ", ");
           stmt = stmt.concat(r.getRootID() + ", ");
           stmt = stmt.concat("Origin, ");
           stmt = stmt.concat("0.0, 0.0, 0.0, ");
           stmt = stmt.concat(r.firstNode.x * pixelSize + ", ");
           stmt = stmt.concat(r.firstNode.y * pixelSize + ", ");
           stmt = stmt.concat(r.isChild()+ ", ");
           stmt = stmt.concat(r.getPoAccession()+ ", ");
           stmt = stmt.concat(name);
           pw.println(stmt);
           pw.flush();
           // Marks information
           for (int j = 0; j < r.markList.size(); j++) {
              Mark m = (Mark)r.markList.get(j);
              Point p = r.getLocation(m.lPos * pixelSize);
              stmt = name + ", ";
              stmt = stmt.concat((m.isForeign ? m.foreignImgName : imgName) + ", ");  // XD 20110629
              stmt = stmt.concat(r.getRootKey() + ", ");
              stmt = stmt.concat(r.getRootID() + ", ");
              stmt = stmt.concat(Mark.getName(m.type) + ", ");
              stmt = stmt.concat(r.lPosPixelsToCm(m.lPos) + ", ");
              stmt = stmt.concat(m.diameter * pixelSize + ", ");
              stmt = stmt.concat(m.angle + ", ");
              if (p != null) {
                 stmt = stmt.concat(p.x * pixelSize + ", ");
                 stmt = stmt.concat(p.y * pixelSize + ", ");
              }
              else {
                 SR.write("[WARNING] " + Mark.getName(m.type) + " mark '" + m.value + "' on root '"+ r.getRootID() + "' is past the end of root.");
                 stmt = stmt.concat(" 0.0, 0.0, ");
              }
              stmt = stmt.concat(r.isChild()+ ", ");
              stmt = stmt.concat(r.getPoAccession()+ ", ");
              if (m.needsTwinPosition()) 
                 stmt = stmt.concat(((m.twinLPos - m.lPos) * pixelSize) + "");
              else stmt = stmt.concat(m.value + "");
              pw.println(stmt);
              pw.flush();
              }
           // Root end information
           stmt = name + ", ";
           stmt = stmt.concat(name + ", ");  // XD 20110629
           stmt = stmt.concat(r.getRootKey() + ", ");
           stmt = stmt.concat(r.getRootID() + ", ");
           stmt = stmt.concat("Length, ");
           stmt = stmt.concat(r.lPosPixelsToCm(r.getRootLength()) + ", ");
           stmt = stmt.concat("0.0, 0.0, ");
           stmt = stmt.concat(r.lastNode.x * pixelSize + ", ");
           stmt = stmt.concat(r.lastNode.y * pixelSize + ", ");
           stmt = stmt.concat(r.isChild() + ", ");
           stmt = stmt.concat(r.getPoAccession() + ", ");
           stmt = stmt.concat(name + "");
           pw.println(stmt);
           if(last) pw.flush();           
           }
       SR.write("CSV data transfer completed for 'Marks'.");     
   }

   
   public void sqlLabExport(String table, boolean create){
	   sqlLabExport(table, create, imgName.concat(".tif"), ".tif", false);
   }
   
   /**
    * 
    * @param table
    * @param create
    */
   public void sqlLabExport(String table, boolean create, String name, String ext, boolean perso) {
      	   
	  
	   
      if(rootList.size() == 0) return;
 
	  Statement sql = sqlServer.getStatement();
      if (sql == null){
    	  SR.write("SQL transfer failed. Please connect SmartRoot to the database.");
    	  return;
      }
      
      // Create the table
      if (create) {
    	  SR.write("CREATING TABLE");
         try {
            sql.executeUpdate("DROP TABLE " + table);
            }
         catch (SQLException sqlE) {
            }
         try {
            sql.executeUpdate("CREATE TABLE " + table + " (Img CHAR(50), experiment_id CHAR(24), stock_id CHAR(24), treatment_id CHAR(24), box CHAR(24), plant_id INTEGER, das CHAR(24));");
            sql.executeUpdate("ALTER TABLE " + table + " ADD COLUMN root_id CHAR(70);");
            sql.executeUpdate("ALTER TABLE " + table + " ADD COLUMN root_name CHAR(60);");
            sql.executeUpdate("ALTER TABLE " + table + " ADD COLUMN length FLOAT;");
            sql.executeUpdate("ALTER TABLE " + table + " ADD COLUMN surface FLOAT;");
            sql.executeUpdate("ALTER TABLE " + table + " ADD COLUMN volume FLOAT;");
            sql.executeUpdate("ALTER TABLE " + table + " ADD COLUMN diam FLOAT;");
            sql.executeUpdate("ALTER TABLE " + table + " ADD COLUMN root_order INTEGER;");
            sql.executeUpdate("ALTER TABLE " + table + " ADD COLUMN parent_id CHAR(70);");
            sql.executeUpdate("ALTER TABLE " + table + " ADD COLUMN parent_name CHAR(50);");
            sql.executeUpdate("ALTER TABLE " + table + " ADD COLUMN insertion FLOAT;");
            sql.executeUpdate("ALTER TABLE " + table + " ADD COLUMN insertion_apex FLOAT;");
            sql.executeUpdate("ALTER TABLE " + table + " ADD COLUMN angle FLOAT;");
            sql.executeUpdate("ALTER TABLE " + table + " ADD COLUMN side INTEGER;");
            sql.executeUpdate("ALTER TABLE " + table + " ADD COLUMN laterals INTEGER;");
            sql.executeUpdate("ALTER TABLE " + table + " ADD COLUMN first_lateral FLOAT;");
            sql.executeUpdate("ALTER TABLE " + table + " ADD COLUMN last_lateral FLOAT;");
            sql.executeUpdate("ALTER TABLE " + table + " ADD COLUMN tortuosity FLOAT;");
            sql.executeUpdate("ALTER TABLE " + table + " ADD COLUMN vector_length FLOAT;");
            sql.executeUpdate("ALTER TABLE " + table + " ADD COLUMN children_length FLOAT;");
            sql.executeUpdate("ALTER TABLE " + table + " ADD COLUMN children_surface FLOAT;");
            sql.executeUpdate("ALTER TABLE " + table + " ADD COLUMN children_angle FLOAT;");
            sql.executeUpdate("ALTER TABLE " + table + " ADD COLUMN xmin FLOAT;");
            sql.executeUpdate("ALTER TABLE " + table + " ADD COLUMN xmax FLOAT;");
            sql.executeUpdate("ALTER TABLE " + table + " ADD COLUMN ymin FLOAT;");
            sql.executeUpdate("ALTER TABLE " + table + " ADD COLUMN ymax FLOAT;");
            sql.executeUpdate("ALTER TABLE " + table + " ADD COLUMN xmin_total FLOAT;");
            sql.executeUpdate("ALTER TABLE " + table + " ADD COLUMN xmax_total FLOAT;");
            sql.executeUpdate("ALTER TABLE " + table + " ADD COLUMN ymin_total FLOAT;");
            sql.executeUpdate("ALTER TABLE " + table + " ADD COLUMN ymax_total FLOAT;");            

            }
         catch (SQLException sqlE) {
            SR.write("The table " + table + " could not be created (SQL CREATE TABLE)");
            SR.write(sqlE.getMessage());
            return;
            }
         SR.write("The table " + table + " was successfully created");
         }
      
      // Fill the data table
      
      String stmt = null;
      String[] exp = processImageName(name, ext);
      this.setLateralRootID();
      if(perso){
    	  //renamePrimaryRootsLeftRight();
      }
            
      try {
         for (int i = 0; i < rootList.size(); i++) {
            
        	Root r = (Root) rootList.get(i);
            int plantID = 0;
            //if(perso) plantID = getPlantID(r, exp);
            
            //if (!r.validate() || plantID == 0) continue; // corrupted Root instance
            if (!r.validate()) continue; // corrupted Root instance
            
            stmt = "INSERT INTO " + table + " VALUES (";
            stmt = stmt.concat("'" + name + "', ");
           	stmt = stmt.concat("'" + exp[0] + "', ");
           	stmt = stmt.concat("'" + exp[1] + "', ");
           	stmt = stmt.concat("'" + exp[2] + "', ");
           	stmt = stmt.concat("'" + exp[3] + "', ");
           	stmt = stmt.concat("" + plantID + ", ");
           	stmt = stmt.concat("'" + exp[4] + "', ");
            stmt = stmt.concat("'" + r.getRootKey() + "', ");
            stmt = stmt.concat("'" + r.getRootID() + "', ");
            stmt = stmt.concat(r.lPosPixelsToCm(r.getRootLength()) + ", ");
            stmt = stmt.concat(r.getRootSurface() + ", ");
            stmt = stmt.concat(r.getRootVolume() + ", ");
            stmt = stmt.concat(r.getAVGDiameter() + ", ");
            stmt = stmt.concat(r.isChild() + ", ");
            if(r.getParent() != null ) stmt = stmt.concat("'" + r.getParent().getRootKey() + "', ");
            else stmt = stmt.concat("'-1', ");
            if(r.getParent() != null ) stmt = stmt.concat("'" + r.getParentName() + "', ");
            else stmt = stmt.concat("'-1', ");
            stmt = stmt.concat(r.lPosPixelsToCm(r.getDistanceFromBase()) + ", ");
            stmt = stmt.concat(r.lPosPixelsToCm(r.getDistanceFromApex()) + ", ");
            if(r.getInsertAngl() >= 0) stmt = stmt.concat((r.getInsertAngl() * (180 / Math.PI)) + ", ");
            else stmt = stmt.concat("-1, ");
            stmt = stmt.concat(r.isLeftRight() + ", ");
            stmt = stmt.concat(r.childList.size() + ", ");
            
            if(r.firstChild != null) stmt = stmt.concat(r.lPosPixelsToCm(r.getFirstChild().getDistanceFromBase()) + ", ");
            else stmt = stmt.concat("null" + ", ");
            
            if(r.lastChild != null) stmt = stmt.concat(r.lPosPixelsToCm(r.getLastChild().getDistanceFromBase()) + ", ");
            else stmt = stmt.concat("null" + ", ");
            
            stmt = stmt.concat(r.getTortuosity() + ", ");
            stmt = stmt.concat(r.lPosPixelsToCm(r.getVectorLength()) + ", ");
            stmt = stmt.concat(r.lPosPixelsToCm(r.getChildrenLength()) + ", ");
            stmt = stmt.concat(r.lPosPixelsToCm(r.getChildrenSurface()) + ", ");
            stmt = stmt.concat(r.lPosPixelsToCm(r.getChildrenAngle()) + ", ");
            stmt = stmt.concat(r.lPosPixelsToCm(r.getXMin()) + ", ");
            stmt = stmt.concat(r.lPosPixelsToCm(r.getXMax()) + ", ");
            stmt = stmt.concat(r.lPosPixelsToCm(r.getYMin()) + ", ");
            stmt = stmt.concat(r.lPosPixelsToCm(r.getYMax()) + ", ");
            stmt = stmt.concat(r.lPosPixelsToCm(r.getXMinTotal()) + ", ");
            stmt = stmt.concat(r.lPosPixelsToCm(r.getXMaxTotal()) + ", ");
            stmt = stmt.concat(r.lPosPixelsToCm(r.getYMinTotal()) + ", ");
            stmt = stmt.concat(r.lPosPixelsToCm(r.getYMaxTotal()) + "); ");            
            sql.executeUpdate(stmt);
            }
         }
      catch (SQLException e) {
         SR.write("Transfer error in SQL statement: " + stmt);
         SR.write(e.getMessage());
         }
      if(ext.equals(".tif")) SR.write("SQL data transfer completed for 'Roots'.");
      }

   
   public void csvLabExport(PrintWriter pw, boolean header){
	   csvLabExport(pw, header, imgName, ".tif", true);
   }
   
   public void csvLabExport(PrintWriter pw, boolean header, String name, String ext, boolean last){
      String[] exp = processImageName(name, ext);

	   if(header) 
		   pw.println("Img, experiment_id, genotype_id, treatment_id, box, das, root, root_id, length, surface" +
		   		", Volume, Diam, root_order, path, parent, parent_id, LPosParent" +
		   		", angle, side, laterals, childDensity, firstChild, first_lateral, lastChild, last_lateral, first_x, first_y, last_x, last_y");
	   
	  String stmt = null;
      for (int i = 0; i < rootList.size(); i++) {
         Root r = (Root) rootList.get(i);
         if (!r.validate()) continue; // corrupted Root instance
         stmt = name + ", ";
         stmt = stmt.concat(exp[0] + ", ");
         stmt = stmt.concat(exp[1] + ", ");
         stmt = stmt.concat(exp[2] + ", ");
         stmt = stmt.concat(exp[3] + ", ");
         stmt = stmt.concat(exp[4] + ", ");
         stmt = stmt.concat(r.getRootID() + ", ");
         stmt = stmt.concat(r.getRootKey() + ", ");
         stmt = stmt.concat(r.lPosPixelsToCm(r.getRootLength()) + ", ");
         stmt = stmt.concat(r.getRootSurface() + ", ");
         stmt = stmt.concat(r.getRootVolume() + ", ");
         stmt = stmt.concat(r.getAVGDiameter() + ", ");
         stmt = stmt.concat(r.isChild() + ", ");
         stmt = stmt.concat(r.getRootPath() + ", ");
         stmt = stmt.concat(r.getParentName() + ", ");
         if(r.getParent() != null) stmt = stmt.concat(r.getParent().getRootKey() + ", ");
         else stmt = stmt.concat("-1, ");
         stmt = stmt.concat(r.lPosPixelsToCm(r.getDistanceFromBase()) + ", ");
         stmt = stmt.concat((r.getInsertAngl() * (180 / Math.PI)) + ", ");
         stmt = stmt.concat(r.isLeftRight() + ", ");
         stmt = stmt.concat(r.childList.size() + ", ");
         stmt = stmt.concat(r.getChildDensity() + ", ");
         if(r.firstChild != null){
         	stmt = stmt.concat(r.getFirstChild().getRootID() + ", ");
         	stmt = stmt.concat(r.lPosPixelsToCm(r.getFirstChild().getDistanceFromBase()) + ", ");
         }
         else{ 
         	stmt = stmt.concat("null, ");
         	stmt = stmt.concat("null, ");
         }
         if(r.lastChild != null){
         	stmt = stmt.concat(r.getLastChild().getRootID() + ", ");
         	stmt = stmt.concat(r.lPosPixelsToCm(r.getLastChild().getDistanceFromBase())+",");
         }
         else{ 
         	stmt = stmt.concat("null, ");
         	stmt = stmt.concat("null,");
         }
         stmt = stmt.concat(r.firstNode.x + ", ");
         stmt = stmt.concat(r.firstNode.y + ", ");
         stmt = stmt.concat(r.lastNode.x + ", ");
         stmt = stmt.concat(r.lastNode.y + "");
         pw.println(stmt);
         if(last) pw.flush();
      }
      if(ext.equals(".tif")) SR.write("CSV data transfer completed for 'Roots'.");
   }   
   
   
   
   /**
    * 
    * @param table
    * @param create
    */
   public void sqlSendRoots(String table, boolean create){
	   sqlSendRoots(table, create, imgName);
   }
   
   /**
    * 
    * @param table
    * @param create
    */
   public void sqlSendRoots (String table, boolean create, String name) {
      Statement sql = sqlServer.getStatement();
      if (sql == null){
    	  SR.write("SQL transfer failed. Please connect SmartRoot to the database.");
    	  return;
      }
      if (create) {
         try {
            sql.executeUpdate("DROP TABLE " + table);
            }
         catch (SQLException sqlE) {
            }
         try {
            sql.executeUpdate("CREATE TABLE " + table + " (image CHAR(100));");
            sql.executeUpdate("ALTER TABLE " + table + " ADD COLUMN root_name CHAR(24);");
            sql.executeUpdate("ALTER TABLE " + table + " ADD COLUMN root CHAR(200);");
           sql.executeUpdate("ALTER TABLE " + table + " ADD COLUMN length FLOAT;");
            sql.executeUpdate("ALTER TABLE " + table + " ADD COLUMN surface FLOAT;");
            sql.executeUpdate("ALTER TABLE " + table + " ADD COLUMN volume FLOAT;");
            sql.executeUpdate("ALTER TABLE " + table + " ADD COLUMN convexhull FLOAT;");
            sql.executeUpdate("ALTER TABLE " + table + " ADD COLUMN diameter FLOAT;");
            sql.executeUpdate("ALTER TABLE " + table + " ADD COLUMN root_order INTEGER;");
            sql.executeUpdate("ALTER TABLE " + table + " ADD COLUMN root_ontology CHAR(50);");
            sql.executeUpdate("ALTER TABLE " + table + " ADD COLUMN parent_name CHAR(50);");
            sql.executeUpdate("ALTER TABLE " + table + " ADD COLUMN parent CHAR(200);");
            sql.executeUpdate("ALTER TABLE " + table + " ADD COLUMN insertion_position FLOAT;");
            sql.executeUpdate("ALTER TABLE " + table + " ADD COLUMN insertion_angle FLOAT;");
            sql.executeUpdate("ALTER TABLE " + table + " ADD COLUMN n_child INTEGER;");
            sql.executeUpdate("ALTER TABLE " + table + " ADD COLUMN child_density FLOAT;");
            sql.executeUpdate("ALTER TABLE " + table + " ADD COLUMN first_child CHAR(200);");
            sql.executeUpdate("ALTER TABLE " + table + " ADD COLUMN insertion_first_child FLOAT;");
            sql.executeUpdate("ALTER TABLE " + table + " ADD COLUMN last_child CHAR(200);");
            sql.executeUpdate("ALTER TABLE " + table + " ADD COLUMN insertion_last_child FLOAT;");
            }
         catch (SQLException sqlE) {
            SR.write("The table " + table + " could not be created (SQL CREATE TABLE)");
            SR.write(sqlE.getMessage());
            return;
            }
         SR.write("The table " + table + " was successfully created");
         }
      
      String stmt = null;
      try {
         for (int i = 0; i < rootList.size(); i++) {
            Root r = (Root) rootList.get(i);
            if (!r.validate()) continue; // corrupted Root instance
            stmt = "INSERT INTO " + table + " VALUES (";
            stmt = stmt.concat("'" + name + "', ");
            stmt = stmt.concat("'" + r.getRootID() + "', ");
            stmt = stmt.concat("'" + r.getRootKey() + "', ");
            stmt = stmt.concat(r.lPosPixelsToCm(r.getRootLength()) + ", ");
            stmt = stmt.concat(r.getRootSurface() + ", ");
            stmt = stmt.concat(r.getRootVolume() + ", ");
            stmt = stmt.concat(0+ ", ");
//            stmt = stmt.concat(r.getConvexHullArea() + ", ");
            stmt = stmt.concat(r.getAVGDiameter() + ", ");
            stmt = stmt.concat(r.isChild() + ", ");
            stmt = stmt.concat("'" + r.getPoAccession() + "', ");
            stmt = stmt.concat("'" + r.getParentName() + "', ");
            if(r.getParent() != null ) stmt = stmt.concat("'" + r.getParent().getRootKey() + "', ");
            else stmt = stmt.concat("'-1', ");
            stmt = stmt.concat(r.lPosPixelsToCm(r.getDistanceFromApex()) + ", "); // TODO to change 
            stmt = stmt.concat((r.getInsertAngl() * (180 / Math.PI)) + ", ");
            stmt = stmt.concat(r.childList.size() + ", ");
            stmt = stmt.concat(r.getChildDensity() + ", ");
            if(r.firstChild != null){
            	stmt = stmt.concat("'"+r.getFirstChild().getRootKey() + "', ");
            	stmt = stmt.concat(r.lPosPixelsToCm(r.getFirstChild().getDistanceFromBase()) + ", ");
            }
            else{ 
            	stmt = stmt.concat("null" + ", ");
            	stmt = stmt.concat("null" + ", ");
            }
            if(r.lastChild != null){
            	stmt = stmt.concat("'"+r.getLastChild().getRootKey() + "', ");
            	stmt = stmt.concat(r.lPosPixelsToCm(r.getLastChild().getDistanceFromBase()) + "); ");
            }
            else{ 
            	stmt = stmt.concat("null" + ", ");
            	stmt = stmt.concat("null" + "); ");
            }
            sql.executeUpdate(stmt);
            }
         }
      catch (SQLException e) {
         SR.write("Transfer error in SQL statement: " + stmt);
         SR.write(e.getMessage());
         }
      SR.write("SQL data transfer completed for 'Roots'.");
      }

   public void csvSendRoots(PrintWriter pw, boolean header){
	   csvSendRoots(pw, header, imgName, true);
   }
   public void csvSendRoots(PrintWriter pw, boolean header, String name, boolean last){
	   if(header) 
		   pw.println("image, root_name, root, length, vector_length, surface, volume, direction, diameter, root_order, root_ontology, parent_name, parent, insertion_position" +
		   		", insertion_angle, n_child, child_density, first_child, insertion_first_child, last_child, insertion_last_child");
	   
	  String stmt = null;

      for (int i = 0; i < rootList.size(); i++) {
         Root r = (Root) rootList.get(i);
         if (!r.validate()){
        	 SR.write("Corrupted root instance");
        	 continue; // corrupted Root instance
         }
         stmt = name + ", ";
         stmt = stmt.concat(r.getRootID() + ", ");
         stmt = stmt.concat(r.getRootKey() + ", ");
         stmt = stmt.concat(r.lPosPixelsToCm(r.getRootLength()) + ", ");
         stmt = stmt.concat(r.lPosPixelsToCm(r.getVectorLength()) + ", ");
         stmt = stmt.concat(r.getRootSurface() + ", ");
         stmt = stmt.concat(r.getRootVolume() + ", ");
         stmt = stmt.concat(r.getRootOrientation() * (180 / Math.PI) + ", ");
        // stmt = stmt.concat(r.getConvexHullArea() + ", ");
         stmt = stmt.concat(r.getAVGDiameter() + ", ");
         stmt = stmt.concat(r.isChild() + ", ");
         stmt = stmt.concat(r.getPoAccession() + ", ");
         stmt = stmt.concat(r.getParentName() + ", ");
         if(r.getParent() != null) stmt = stmt.concat(r.getParent().getRootKey() + ", ");
         else stmt = stmt.concat("-1, ");
         stmt = stmt.concat(r.lPosPixelsToCm(r.getDistanceFromBase()) + ", ");
         stmt = stmt.concat((r.getInsertAngl() * (180 / Math.PI)) + ", ");
         stmt = stmt.concat(r.childList.size() + ", ");
         stmt = stmt.concat(r.getChildDensity() + ", ");
         if(r.firstChild != null){
         	stmt = stmt.concat(r.getFirstChild().getRootKey() + ", ");
         	stmt = stmt.concat(r.lPosPixelsToCm(r.getFirstChild().getDistanceFromBase()) + ", ");
         }
         else{ 
         	stmt = stmt.concat("null, ");
         	stmt = stmt.concat("null, ");
         }
         if(r.lastChild != null){
         	stmt = stmt.concat(r.getLastChild().getRootKey() + ", ");
         	stmt = stmt.concat(r.lPosPixelsToCm(r.getLastChild().getDistanceFromBase())+"");
         }
         else{ 
         	stmt = stmt.concat("null, ");
         	stmt = stmt.concat("null");
         }
         pw.println(stmt);
         if(last) pw.flush();
      }
      SR.write("CSV data transfer completed for 'Roots'.");
   }
 
   public void csvSendCoodrinates(PrintWriter pw, boolean header){
	   if(header) 
		   pw.println("Img, Root, x, y");
	   
	  String stmt = null;
	  
	  for(float i = 0; i < img.getHeight(); i++){
		  for(float j = 0; j < img.getWidth(); j++){
			  if(selectRoot(i, j) == ROOT){
				  stmt = imgName + ", ";
			      stmt = stmt.concat(selectedRoot.getRootID() + ", ");
			      stmt = stmt.concat(i + ", ");
			      stmt = stmt.concat(j + ", ");
			      pw.println(stmt);
			      pw.flush();
			  }
		  }
	  }
      SR.write("CSV data transfer completed for 'Coordinates'.");
   }   
   
   /**
    * 
    * @param table
    * @param create
    */
   public void sqlSendNodes(String table, boolean create) {
	  sqlSendNodes(table, create, imgName);
   }
   
   /**
    * 
    * @param table
    * @param create
    */
   public void sqlSendNodes(String table, boolean create, String name) {
      Statement sql = sqlServer.getStatement();
      if (sql == null) return;
      if (create) {
         try {
            sql.executeUpdate("DROP TABLE " + table);
            }
         catch (SQLException sqlE) {
            }
         try {
            sql.executeUpdate("CREATE TABLE " + table + " (image CHAR(100));");
            sql.executeUpdate("ALTER TABLE " + table + " ADD COLUMN root CHAR(200);");
            sql.executeUpdate("ALTER TABLE " + table + " ADD COLUMN root_name CHAR(24);");
            sql.executeUpdate("ALTER TABLE " + table + " ADD COLUMN x FLOAT;");
            sql.executeUpdate("ALTER TABLE " + table + " ADD COLUMN y FLOAT;");
            sql.executeUpdate("ALTER TABLE " + table + " ADD COLUMN theta FLOAT;");
            sql.executeUpdate("ALTER TABLE " + table + " ADD COLUMN diameter FLOAT;");
            sql.executeUpdate("ALTER TABLE " + table + " ADD COLUMN distance_from_base FLOAT;");
            sql.executeUpdate("ALTER TABLE " + table + " ADD COLUMN distance_from_apex FLOAT;");
            sql.executeUpdate("ALTER TABLE " + table + " ADD COLUMN root_order INT;");
            sql.executeUpdate("ALTER TABLE " + table + " ADD COLUMN root_ontology CHAR(24);");
            }
         catch (SQLException sqlE) {
            SR.write("The table " + table + " could not be created (SQL CREATE TABLE)");
            SR.write(sqlE.getMessage());
            return;
            }
         SR.write("The table " + table + " was successfully created");
         }
      
      String stmt = null;
      try {
         for (int i = 0; i < rootList.size(); i++) {
            Root r = (Root) rootList.get(i);
            if (!r.validate()) continue; // corrupted Root instance
            Node n = r.firstNode;
            do {
               stmt = "INSERT INTO " + table + " (image, root, root_name, X, Y, theta, diameter, distance_from_base, distance_from_apex, root_order, root_ontology) VALUES (";
               stmt = stmt.concat("'" + name + "', ");
               stmt = stmt.concat("'" + r.getRootKey() + "', ");
               stmt = stmt.concat("'" + r.getRootID() + "', ");
               stmt = stmt.concat(n.x * pixelSize + ", ");
               stmt = stmt.concat(n.y * pixelSize + ", ");
               stmt = stmt.concat(n.theta + ", ");
               stmt = stmt.concat(n.diameter * pixelSize + ", ");
               stmt = stmt.concat(n.cLength * pixelSize + ", ");
               stmt = stmt.concat((r.getRootLength() - n.cLength ) *pixelSize + ", ");
               stmt = stmt.concat(r.isChild() + ", ");
               stmt = stmt.concat("'" + r.getPoAccession() + "')");
               sql.executeUpdate(stmt);
               } while ((n = n.child) != null);
            }
         }
      catch (SQLException e) {
         SR.write("Transfer error in SQL statement: " + stmt);
         SR.write(e.getMessage());
         }
      SR.write("SQL data transfer completed for 'Nodes'.");
      }

   
   public void csvSendNodes(PrintWriter pw, boolean header){
	   csvSendNodes(pw, header, imgName, true);
   }
   public void csvSendNodes(PrintWriter pw, boolean header, String name, boolean last){
	   
	   if(header)
		   pw.println("image, root, root_name, x, y, theta, diameter, distance_from_base, distance_from_apex, root_order, root_ontology");
	   String stmt;
       for (int i = 0; i < rootList.size(); i++) {
           Root r = (Root) rootList.get(i);
           if (!r.validate()) continue; // corrupted Root instance
           Node n = r.firstNode;
           SR.write(r.pixelSize+" / "+pixelSize);
           do {
              stmt = name + ", ";
              stmt = stmt.concat(r.getRootKey() + ", ");
              stmt = stmt.concat(r.getRootID() + ", ");
              stmt = stmt.concat(n.x * pixelSize + ", ");
              stmt = stmt.concat(n.y * pixelSize + ", ");
              stmt = stmt.concat(n.theta + ", ");
              stmt = stmt.concat(n.diameter * pixelSize + ", ");
              stmt = stmt.concat(n.cLength * pixelSize + ", ");
              stmt = stmt.concat((r.getRootLength() - n.cLength ) *pixelSize + ", ");
              stmt = stmt.concat(r.isChild() + ", ");
              stmt = stmt.concat(r.getPoAccession());
              pw.println(stmt);
              if(last) pw.flush();
              } while ((n = n.child) != null);
        }
       
        SR.write("CSV data transfer completed for 'Nodes'.");
     
   }
   
   /**
    * 
    * @param table
    * @param create
    */
   public void sqlSendStepMorph(String table, boolean create) {
      Statement sql = sqlServer.getStatement();
      if (sql == null) return;
      if (create) {
         try {
            sql.executeUpdate("DROP TABLE " + table);
            }
         catch (SQLException sqlE) {
            }
         try {
            sql.executeUpdate("CREATE TABLE " + table + " (Img CHAR(50), Root CHAR(24), LPos FLOAT);");
            sql.executeUpdate("ALTER TABLE " + table + " ADD COLUMN DirX FLOAT;");
            sql.executeUpdate("ALTER TABLE " + table + " ADD COLUMN DirY FLOAT;");
            sql.executeUpdate("ALTER TABLE " + table + " ADD COLUMN DirA FLOAT;");
            sql.executeUpdate("ALTER TABLE " + table + " ADD COLUMN OriDirX FLOAT;");
            sql.executeUpdate("ALTER TABLE " + table + " ADD COLUMN OriDirY FLOAT;");
            sql.executeUpdate("ALTER TABLE " + table + " ADD COLUMN OriDirA FLOAT;");
            sql.executeUpdate("ALTER TABLE " + table + " ADD COLUMN Diameter FLOAT;");
            }
         catch (SQLException sqlE) {
            SR.write("The table " + table + " could not be created (SQL CREATE TABLE)");
            SR.write(sqlE.getMessage());
            return;
            }
         SR.write("The table " + table + " was successfully created");
         }

      SR.write("Data transfer started...");
      String stmt = null;
      try {
         for (int i = 0; i < rootList.size(); i++) {
            Root r = (Root) rootList.get(i);
            if (!r.validate()) continue; // corrupted Root instance
            float l = r.getRootLength() * pixelSize;
            float[] a0 = r.getAttributesAtLPosCm(0.0f);
            for (float lPos = (float)Math.min(1.0f, l); lPos <= l;) {
               float[] a = r.getAttributesAtLPosCm(lPos);               
               float oriDirX = a[Root.ATTR_X] - a0[Root.ATTR_X];
               float oriDirY = a[Root.ATTR_Y] - a0[Root.ATTR_Y];
               float norm = NodeFitter.norm(oriDirX, oriDirY);
               oriDirX /= norm;
               oriDirY /= norm;
               float oriDirA = (float) (oriDirY <= 0 ? Math.acos(oriDirX / norm) 
                                                     : 2.0 * Math.PI - Math.acos(oriDirX / norm));
               stmt = "INSERT INTO " + table + " VALUES (";
               stmt = stmt.concat("'" + imgName + "', ");
               stmt = stmt.concat("'" + r.getRootID() + "', ");
               stmt = stmt.concat(lPos + ", ");
               stmt = stmt.concat(a[Root.ATTR_DIR_X] + ", ");
               stmt = stmt.concat(a[Root.ATTR_DIR_Y] + ", ");
               stmt = stmt.concat(a[Root.ATTR_ANGLE] + ", ");
               stmt = stmt.concat(oriDirX + ", ");
               stmt = stmt.concat(oriDirY + ", ");
               stmt = stmt.concat(oriDirA + ", ");
               stmt = stmt.concat(a[Root.ATTR_DIAMETER] * pixelSize + ");");
               sql.executeUpdate(stmt);
               if (l == lPos) break;
               else if ((l - lPos) < 1.0f) lPos = l;
               else lPos++; 
               }
            }
         }
      catch (SQLException e) {
         SR.write("Transfer error in SQL statement: " + stmt);
         SR.write(e.getMessage());
         }
      SR.write("SQL data transfer completed.");
      }

   /**
    * 
    * @param table
    * @param create
    */
   public void sqlSendZStepMorph(String table, boolean create) {
      Statement sql = sqlServer.getStatement();
      if (sql == null) return;
      if (create) {
         try {
            sql.executeUpdate("DROP TABLE " + table);
            }
         catch (SQLException sqlE) {
            }
         try {
            sql.executeUpdate("CREATE TABLE " + table + " (Img CHAR(50), Root CHAR(24), LPos_Cm FLOAT);");
            sql.executeUpdate("ALTER TABLE " + table + " ADD COLUMN Y_Cm FLOAT;");
            sql.executeUpdate("ALTER TABLE " + table + " ADD COLUMN DirVectX FLOAT;");
            sql.executeUpdate("ALTER TABLE " + table + " ADD COLUMN DirVectY FLOAT;");
            sql.executeUpdate("ALTER TABLE " + table + " ADD COLUMN DirAngle_Rad FLOAT;");
            sql.executeUpdate("ALTER TABLE " + table + " ADD COLUMN OriDirVectX FLOAT;");
            sql.executeUpdate("ALTER TABLE " + table + " ADD COLUMN OriDirVectY FLOAT;");
            sql.executeUpdate("ALTER TABLE " + table + " ADD COLUMN OriDirAngle_Rad FLOAT;");
            sql.executeUpdate("ALTER TABLE " + table + " ADD COLUMN Diameter_Cm FLOAT;");
            }
         catch (SQLException sqlE) {
            SR.write("The table " + table + " could not be created (SQL CREATE TABLE)");
            SR.write(sqlE.getMessage());
            return;
            }
         SR.write("The table " + table + " was successfully created");
         }

      SR.write("Data transfer started...");
      String stmt = null;
      try {
         for (int i = 0; i < rootList.size(); i++) {
            Root r = (Root) rootList.get(i);
            if (!r.validate()) continue; // corrupted Root instance
            float[] a0 = r.getAttributesAtLPosCm(0.0f);
            Iterator it = r.getAttributesIterator(Root.Y_SEQUENCE);
            while (it.hasNext()) {
               float[] a = (float[])it.next();               
               float oriDirX = a[Root.ATTR_X] - a0[Root.ATTR_X];
               float oriDirY = a[Root.ATTR_Y] - a0[Root.ATTR_Y];
               float norm = NodeFitter.norm(oriDirX, oriDirY);
               oriDirX /= norm;
               oriDirY /= norm;
               float oriDirA = (float) (oriDirY <= 0 ? Math.acos(oriDirX / norm) 
                                                     : 2.0 * Math.PI - Math.acos(oriDirX / norm));
               stmt = "INSERT INTO " + table + " VALUES (";
               stmt = stmt.concat("'" + imgName + "', ");
               stmt = stmt.concat("'" + r.getRootID() + "', ");
               stmt = stmt.concat(((a[Root.ATTR_LPOS] - r.rulerAtOrigin) * pixelSize) + ", ");
               stmt = stmt.concat(Math.rint((a[Root.ATTR_Y] - a0[Root.ATTR_Y]) * pixelSize) + ", ");
               stmt = stmt.concat(a[Root.ATTR_DIR_X] + ", ");
               stmt = stmt.concat(a[Root.ATTR_DIR_Y] + ", ");
               stmt = stmt.concat(a[Root.ATTR_ANGLE] + ", ");
               stmt = stmt.concat(oriDirX + ", ");
               stmt = stmt.concat(oriDirY + ", ");
               stmt = stmt.concat(oriDirA + ", ");
               stmt = stmt.concat(a[Root.ATTR_DIAMETER] * pixelSize + ");");
               sql.executeUpdate(stmt);
               }
            }
         }
      catch (SQLException e) {
         SR.write("Transfer error in SQL statement: " + stmt);
         SR.write(e.getMessage());
         }
      SR.write("SQL data transfer completed.");
      }
   
//   /**
//    * 
//    * @param table
//    * @param rldp2D
//    * @param create
//    */
//   public void sqlSendRLD(String table, RLDProfile2D rldp2D, boolean create) {
//
//	      Statement sql = sqlServer.getStatement();
//	      if (sql == null) return;
//	      if (create) {
//	         try {
//	            sql.executeUpdate("DROP TABLE " + table);
//	            }
//	         catch (SQLException sqlE) {
//	            }
//	         try {
//	            sql.executeUpdate("CREATE TABLE " + table + " (Img CHAR(50), X FLOAT, Y FLOAT, RLD FLOAT);");
//	            }
//	         catch (SQLException sqlE) {
//	            SR.write("The table " + table + " could not be created (SQL CREATE TABLE)");
//	            SR.write(sqlE.getMessage());
//	            return;
//	            }
//	         SR.write("The table " + table + " was successfully created");
//	         }
//	      
//	      SR.write("Data transfer started...");
//	      String stmt = null;
//	      try {
//	         for (int i = 0; i < rldp2D.nx; i++) {
//	            for (int j = 0; j < rldp2D.ny; j++) {
//	               stmt = "INSERT INTO " + table + " VALUES (";
//	               stmt = stmt.concat("'" + imgName + "', ");
//	               stmt = stmt.concat(i * rldp2D.gridsize.gridx + ", ");
//	               stmt = stmt.concat(j * rldp2D.gridsize.gridy + ", ");
//	               stmt = stmt.concat(rldp2D.rld[i][j] + ");");
//	               sql.executeUpdate(stmt);
//	            }
//	         }
//	      }
//	      catch (SQLException e) {
//	         SR.write("Transfer error in SQL statement: " + stmt);
//	         SR.write(e.getMessage());
//	         }
//	      SR.write("SQL data transfer completed for 'RLD'.");
//	      }
//   
//   public void csvSendRLD(PrintWriter pw, RLDProfile2D rldp2D, boolean header){
//	   
//	   if(header)
//		   pw.println("Img, X, Y, RLD");
//	   String stmt;
//       for (int i = 0; i < rldp2D.nx; i++) {
//           for (int j = 0; j < rldp2D.ny; j++) {
//              stmt = imgName + ", ";
//              stmt = stmt.concat(i * rldp2D.gridsize.gridx + ", ");
//              stmt = stmt.concat(j * rldp2D.gridsize.gridy + ", ");
//              stmt = stmt.concat(rldp2D.rld[i][j] + "");
//              pw.println(stmt);
//              pw.flush();
//           }
//        }
//       SR.write("CSV data transfer completed for 'RLD'.");
//   }
//   
   /**
    * 
    */
   public void deleteEndOfSelectedRoot() {
      selectedRoot.rmEndOfRoot(selectedNode, this, false);
      }

   /**
    * 
    */
   public void deleteBaseOfSelectedRoot() {
      selectedRoot.rmBaseOfRoot(selectedNode, this);
      }

   /**
    * 
    */
   public void splitSelectedRoot() {
      Root r = selectedRoot.split(selectedNode, this);
      if (r != null) {
         rootList.add(0,r);
         }
      }

   /**
    * 
    */
   public void setRulerZero() {
      selectedRoot.setRulerAtOrigin(-selectedRoot.lPosCmToPixels(markerPosition));
      }

   /**
    * 
    * @return
    */
   public float getRulerPosition() {
      return markerPosition;
      }

   /**
    * 
    * @param i
    * @return
    */
   public Root getRoot (int i) {
      if (i < getNRoot()) return (Root) rootList.get(i);
      else return null;
      }


   /**
    * 
    * @return
    */
   public int getNRoot() {
      return rootList.size();
      }
      
   /**
    * 
    */
   public void deleteSelectedRoot() {
      int i = rootList.indexOf(selectedRoot);
      int cl = selectedRoot.childList.size();
      ArrayList<Root> l = selectedRoot.childList;
      Root child; 
      Root parent;
      int opt = 0;
      // if the root has children
      if (cl > 0) {
    	  parent = selectedRoot;
    	  opt = JOptionPane.showConfirmDialog(null, "Do you want to delete children?","Delete option", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
	      if (opt == JOptionPane.CANCEL_OPTION) return;
	      else if(opt == JOptionPane.YES_OPTION){
	    	  for (int k=0; k < cl; k++){
	    		  selectedRoot = (Root) l.get(0);
	    		  deleteSelectedRoot();
	    	  }
	      }
	      else if (opt == JOptionPane.NO_OPTION){
	    	  for (int k=0; k < cl; k++){
	    		  child = (Root) l.get(0);
	    		  child.detacheParent();
	    	  }
	      }
	      selectedRoot=parent;
     }
      if (selectedRoot.isChild() != 0){
    	  try{
    		  selectedRoot.detacheParent();
    	  }
    	  catch(Exception e){;}
      }
      i = rootList.indexOf(selectedRoot);
      rootList.remove(i);
      }

   /**
    * 
    */
   public void deleteSelectedNode() {
      if (selectedRoot.rmNode(selectedNode) == true) deleteSelectedRoot();
      }

   /**
    * 
    * @param RootID
    * @param lp
    * @return
    */
   public Point getLocation(String RootID, float lp) {
      for (int i = 0; i < rootList.size(); i++) {
         Root r = (Root) rootList.get(i);
         if (r.getRootID().equals(RootID))
            return r.getLocation(lp);
         }
      return (Point) null;
      }

   /**
    * 
    * @param type
    * @param value
    * @return
    */
   public boolean addMark(int type, String value) {   
	   SR.write("mp = "+markerPosition);
      Mark m = selectedRoot.addMark(type, value, markerPosition);
      if (m == null) return false; // ANCHOR
      if (m.needsTwinPosition()) {
         twinMark = m;
         rootOfTwinMark = selectedRoot;
         return true;
         }
      return false;
      }
   
   /**
    * 
    * @return
    */
   public boolean setTwinPosition() {
      if (selectedRoot != rootOfTwinMark) return true;
      twinMark.setTwinPosition(rootOfTwinMark.lPosCmToPixels(markerPosition));
      return false;
      }

   /**
    * 
    */
   public void removeSelectedMark() {
      selectedRoot.removeMark(selectedMark);
      }

   /**
    * 
    */
   public void removeAllMarks() {
      for (int i = 0; i < rootList.size(); i++) {
         Root r = (Root) rootList.get(i);
         r.removeAllMarks(true);
         }
      }

   /**
    * 
    * @return
    */
   public String getSelectedMarkValue() {
      if (selectedMark != null) return selectedMark.value;
      else return "";
      }
      
   /**
    * 
    * @return
    */
   public Mark getSelectedMark() {
      return selectedMark;
      }
      
   /**
    * 
    * @param value
    */
   public void changeSelectedMarkValue(String value) {
      selectedRoot.changeMarkValue(selectedMark, value);
      }

   /**
    * 
    * @param x
    * @param y
    */
   public void addRegistrationAnchor(float x, float y) {
      anchorList.add(new RegistrationAnchor(x, y, anchorList.size()));
      }

   /**
    * 
    */
   public void rmRegistrationAnchors() {
      anchorList.clear();
      }

   /**
    * 
    * @return
    */
   public float getDPI() {return dpi; }
   
   /**
    * 
    * @param dpi
    */
   public void setDPI(float dpi) {
      this.dpi = dpi;
      pixelSize = (float) (2.54 / dpi);
      for (int i = 0; i < rootList.size(); i++) ((Root) rootList.get(i)).setDPI(dpi);
      }

   /**
    * 
    * @param method
    */
   public void setThresholdMethod(int method) {
      fit.setThresholdMethod(method);
      }

   /**
    * 
    * @param flag
    */
   public void setNoSave(boolean flag) {quitWithoutSave = flag; }
   
   /**
    * 
    * @return
    */
   public boolean getNoSave() {return quitWithoutSave;}

   /**
    * 
    */
   public void windowActivated(WindowEvent e) {
      srWin.setCurrentRootModel(this, false);
      }

   /**
    * 
    */
   public void windowClosing(WindowEvent e) {}

   /**
    * 
    */
   public void windowClosed(WindowEvent e) {
      fit.detach();   // XD 20100628
      win.removeWindowListener(this);   // XD 20100628
      srWin.setCurrentRootModel(null, false);   // XD 20100628
      ric.kill();
      }
      
   /**
    * 
    */
   public void clearDatafile() {
      rootList.clear(); 
      anchorList.clear();
      clearLinkedDatafileList();
      tracingRoot = null;
      tracingNode = null;
      selectedRoot = null;
      selectedNode = null;
//      nextRootKey = 0;
      }

   /**
    * 
    */
   public void selectAndRead() {
      JFileChooser fc = new JFileChooser(new File(directory));
      fc.setFileFilter(new BackupFileFilter(dataFName));
      if (fc.showDialog(null, "Select Datafile") == JFileChooser.APPROVE_OPTION)
         read(fc.getSelectedFile().getAbsolutePath(), false);
      }

   /**
    * 
    * @param f
    */
   private void logReadError(File f) {
      SR.write("An I/O error occured while opening the linked datafile \n"
               + f.getAbsolutePath());
      }

   /**
    * 
    */
   private void logReadError() {
      SR.write("An I/O error occured while attemping to read the datafile.");
      SR.write("A new empty datafile will be created.");
      SR.write("Backup versions of the datafile, if any, can be loaded");
      SR.write("using the File -> Use backup datafile menu item.");
      }

   /**
    * 
    */
   public void read() {
      read(dataFName, false);
      }

   /**
    * 
    */
   public void read(String fName, boolean isUndo) {
      read(fName, isUndo, 1);
      }
   
   /**
    * 
    */
   public void read(String fName, boolean isUndo, float scale) {
      read(fName, isUndo, true, scale);
      }

   
   public void read(String fName, boolean isUndo, boolean isFirst){
	   read(fName, isUndo, isFirst, false, false, 1);
   }
   
   public void read(String fName, boolean isUndo, boolean isFirst, float scale){
	   read(fName, isUndo, isFirst, false, false, scale);
   }
  
   public void read(String fName, boolean isUndo, boolean isFirst, boolean isInvisible, float scale){
	   read(fName, isUndo, isFirst, isInvisible, false, scale);
   }
   
   
   /**
    * Read XML datafile
    * @param fName
    * @param isUndo
    */
   public void read(String fName, boolean isUndo, boolean isFirst, boolean isInvisible, boolean globalDPI, float scale) {
      // redirect the read method to a different version reader if the file suffix
      // indicate an ancestor version (only if the user selected an old datafile)
      // String suffix = fName.substring(fName.lastIndexOf(".") + 1);
      // int i = 0;
      // while (i < fileSuffix.length && !suffix.equals(fileSuffix[i])) i++;
      // if (i == fileSuffix.length) {
      //    v101read(fName);
      //    return;
      //    }
      
      if(isFirst) clearLinkedDatafileList();
      if(isFirst) rootList.clear();
      if(isFirst) anchorList.clear();
      tracingRoot = null;  // This is the default DPI setting (in case a new datafile is created)
      tracingNode = null;
      selectedRoot = null;
      selectedNode = null;
      nextAutoRootID = 0;
         
      if (!(new File(fName)).exists()) {
         SR.write("This image has no datafile.");
         SR.write("A new empty datafile will be created.");
         return;
         }
   
      org.w3c.dom.Document documentDOM = null;
      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

      try {
         DocumentBuilder builder = factory.newDocumentBuilder();
         documentDOM = builder.parse(new File(fName) );
         }
      catch (SAXException sxe) {
         logReadError();
         return;
         }
      catch (ParserConfigurationException pce) {
         logReadError();
         return;
         }
      catch (IOException ioe) {
         logReadError();
         return;
         }

      documentDOM.normalize();
      
      org.w3c.dom.Node nodeDOM = documentDOM.getFirstChild();
      
      // If using a common data structure
      if (nodeDOM.getNodeName().equals("rsml")) {
          readRSML(fName, scale, globalDPI);
          return;
          }
            
      // The root tag of the XML hierarchy must be named "SmartRoot"
      if (!nodeDOM.getNodeName().equals("SmartRoot")) {
         logReadError();
         return;
         }
      
      //float version = Float.valueOf(nodeDOM.getAttributes().getNamedItem("version").getNodeValue());
      String v = nodeDOM.getAttributes().getNamedItem("version").getNodeValue();
      float version;
      if(v.substring(0, 1).equals("v")) version = Float.valueOf(v.substring(1));
      else version = Float.valueOf(v.substring(0));
      
      //dpi = 300;
      if(!globalDPI) dpi = Float.valueOf(nodeDOM.getAttributes().getNamedItem("dpi").getNodeValue()).floatValue();
      //dpi = Float.valueOf(nodeDOM.getAttributes().getNamedItem("dpi").getNodeValue()).floatValue();
      
      IJ.log("DPI = "+dpi);
      
      org.w3c.dom.Node n = nodeDOM.getAttributes().getNamedItem("nextAutoRootID");
      if (n != null) nextAutoRootID = Integer.valueOf(n.getNodeValue()).intValue();
      
      if(!isUndo && !isInvisible) SR.write("Image: " + imgName);
      if(!isUndo & isFirst) SR.write("Version: " + version);
      if(!isUndo & isFirst) SR.write("DPI: " + dpi);
      pixelSize = (float) (2.54 / dpi);
         
      nodeDOM = nodeDOM.getFirstChild();

      while (nodeDOM != null) {
         String nName = nodeDOM.getNodeName();
         // Nodes other than Root & RegistrationAnchor elements are not considered
         if (nName.equals("Root")) {
        	 Root r;
            if(version < 3.2){ 
            	r = new Root(dpi, getRootKey(), this);
            	r.read(nodeDOM);    
            }            
            else r = new Root(dpi, nodeDOM, false, null, this, null);
          	}
         else if (nName.equals("RegistrationAnchor")) {
            RegistrationAnchor ra = new RegistrationAnchor(nodeDOM, anchorList.size());
            anchorList.add(ra);
            }
         else if (nName.equals("LinkedDatafile")) {
            readLinkedDatafile(nodeDOM);
            }
         nodeDOM = nodeDOM.getNextSibling();
       }
      this.deleteSmallRoots();
      if(version < 3.2){
    	  for(int i = 0; i < rootList.size(); i++){
	    	  Root r1 = (Root) rootList.get(i);
	    	  if(r1.parent == null){
	    		  r1.isChild(0);
	    	  }
	      }
      }
      else if( version >= 3.2 && version <= 3.5){
	      for(int i = 0; i < rootList.size(); i++){
	    	  Root r1 = (Root) rootList.get(i);
	    	  if(r1.parentKey.equals("-1")){ 
	    		  r1.isChild(0);
	    	  }
	      }
      }
      else{
	      for(int i = 0; i < rootList.size(); i++){
	    	  Root r1 = (Root) rootList.get(i);
	    	  if(r1.parentKey == null){ //-1
	    		  r1.isChild(0);
	    	  }
	      }
      }


      for (int i = 0; i < rootList.size(); i++){
    	  Root r1 = (Root) rootList.get(i);
    	  for (int j =0; j < rootList.size(); j++){
    		  Root r2 = (Root) rootList.get(j);
    		  
    		  // In version 3.6 and above, the root key is an Unique Identifier and not a number like in previous versions
    		  if(version > 3.5){
    			  if (r1.parentKey.equals(r2.rootKey)) {
;        			  r1.attachParent(r2);
        			  r2.attachChild(r1);
        		  }
    		  }
    		  // In version between 3.2 &nd 3.6, root identifier is just a number
    		  else if(version <= 3.5 && version >= 3.2){
        		  if (r1.parentKey.equals(r2.rootKey)) {
        			  r1.attachParent(r2);
        			  r2.attachChild(r1);
        		  }
    		  }
    		  else if(version < 3.2){
    			  if (r1.getParentName().equals(r2.getRootID())) {
        			  r1.attachParent(r2);
	    			  r2.attachChild(r1);
    			  }
    		  }
    	  }
      }
      
      // Adapt the rootKey for previous versions
      if(version <= 3.5 && version >= 3.2){
    	  for(int i = 0; i < rootList.size(); i++){
    		  Root r1 = (Root) rootList.get(i);
    		  if(r1.childList.size() > 0){
    			  r1.rootKey = getRootKey();
    			  for(int j = 0; j < r1.childList.size(); j++){
    				  Root r2 = (Root) r1.childList.get(j);
    				  r2.parentKey = r1.rootKey;
    			  }
    		  }
    	  }
    	  for(int i = 0; i < rootList.size(); i++){
    		  Root r1 = (Root) rootList.get(i);
    		  if(r1.childList.size() == 0){
    			  r1.rootKey = getRootKey();
    		  }
    	  }
      }
      if(!isInvisible){
    	  if (linkedDatafileList.size() == 0) refreshLinkedDatafileList();
      	else refreshLinkedMarks();
      }
      }
 
   /**
    * Read rsml datafile structure
    * @param f
    */
   public void readRSML(String f) {
	   readRSML(f, 1);
   }
   
   /**
    * Read rsml datafile structure
    * @param f
    */
   public void readRSML(String f, float scale) {
	   readRSML(f, scale, false);
   }
   
   /**
    * Read rsml datafile structure
    * @param f
    */
   public void readRSML(String f, float scale, boolean globalDPI) {

	   // Choose the datafile
	   String fPath = f;

	   if(f == null){
		   	clearDatafile();
	   		JFileChooser fc = new JFileChooser(new File(directory));
	   		fc.setFileFilter(datafileFilterRSML);
	   		if (fc.showDialog(null, "Select Root System Markup Datafile") == JFileChooser.CANCEL_OPTION) return;	 	
	   		fPath = fc.getSelectedFile().getAbsolutePath();
	   }	   

	   
	   
	   tracingRoot = null;  // This is the default DPI setting (in case a new datafile is created)
	   tracingNode = null;
	   selectedRoot = null;
	   selectedNode = null;
	   nextAutoRootID = 0;
	         
	   
	   org.w3c.dom.Document documentDOM = null;
	   DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

	   try {
		   DocumentBuilder builder = factory.newDocumentBuilder();
		   documentDOM = builder.parse(new File(fPath) );
	   }
	   catch (SAXException sxe) {
		   logReadError();
		   return;
	   }
	   catch (ParserConfigurationException pce) {
		   logReadError();
		   return;
	   }
	   catch (IOException ioe) {
		   logReadError();
		   return;
	   }

	   documentDOM.normalize();
	      
	   org.w3c.dom.Node nodeDOM = documentDOM.getFirstChild();
	      	   
	   if (!nodeDOM.getNodeName().equals("rsml")) {
		   logReadError();
		   return;
	   }	
	   
	   String origin = "smartroot";
	   // Navigate the whole document
	   nodeDOM = nodeDOM.getFirstChild();
	   while (nodeDOM != null) {
		   
		   String nName = nodeDOM.getNodeName();
		   
		   // Get and process the metadata
		   if(nName.equals("metadata")){
			   org.w3c.dom.Node nodeMeta = nodeDOM.getFirstChild();
			   String unit = "cm";
			   float res = 1.0f;			   
			   while (nodeMeta != null) {		   
				   	String metaName = nodeMeta.getNodeName();				   	
				   	// Get the image resolution
				   	if(metaName.equals("unit")) unit = nodeMeta.getFirstChild().getNodeValue();	         		         
				   	if(metaName.equals("resolution")) res = Float.valueOf(nodeMeta.getFirstChild().getNodeValue());
				   	if(metaName.equals("file-key")) datafileKey = nodeMeta.getFirstChild().getNodeValue();
				   	if(metaName.equals("software")) origin = nodeMeta.getFirstChild().getNodeValue(); 	
					nodeMeta = nodeMeta.getNextSibling();
			   }
			   if(!globalDPI) dpi = getDPI(unit, res*scale);
			   setDPI(dpi);
		   }
		         
		   // Get the plant
		   if(nName.equals("scene")){
			   org.w3c.dom.Node nodeScene = nodeDOM.getFirstChild();
			   while (nodeScene != null) {		   
				   	String sceneName = nodeScene.getNodeName();
				   	
				   if(sceneName.equals("plant")){
					   org.w3c.dom.Node nodeRoot = nodeScene.getFirstChild();
					   while (nodeRoot != null) {
						   String rootName = nodeRoot.getNodeName();
				   
						   // Get the Roots
						   if(rootName.equals("root")){
							  Root r = new Root(dpi, nodeRoot, true, null, this, origin, scale);
							  if(origin.equals("RootNav")){
								  cropCandidateChildren(r);
							  }
						   }
						   nodeRoot = nodeRoot.getNextSibling();
					   }
				  }
				   nodeScene = nodeScene.getNextSibling();  
			   }	 
		   }
		   nodeDOM = nodeDOM.getNextSibling();  
	   }
   	}
	    
   
   
   
   
   /**
    * 
    * @param redo
    */
   public void undo(boolean redo){
	      FileInfo fileInfo = this.img.getOriginalFileInfo();
	      if(!redo) read(fileInfo.directory + fileInfo.fileName + "_temp1", true);
	      else read(fileInfo.directory + fileInfo.fileName + "_temp2", true);
   }
   
//   /**
//    * 
//    */
//   
//   public void attachParent(Root r){
//	   String pName = r.parentName;
// 	  for (int j =0; j < rootList.size(); j++){
// 		  if
// 	  }
//	   
//   }
   
   /**
    * Tranlate the tracing in x and y
    * @param x
    * @param y
    */
   
   public void translateTracing(int x, int y, float theta){
	   AffineTransform at = AffineTransform.getTranslateInstance(x, y);
	   float[] coord = getCenter();
	   at.rotate(theta, coord[0], coord[1]);
	   for (int i = 0; i < rootList.size(); i++) {
		         ((Root) rootList.get(i)).transform(at);
	   }   
	   repaint();
   }
   

   /**
    * Read a seed datafile to import into the image
    */
   
   public boolean readSeedDataFile(boolean same) {
	   return(readSeedDataFile(same, 1));
   }

	   
   /**
    * Read a seed datafile to import into the image
    */
   
   public boolean readSeedDataFile(boolean same, float scale) {
	      ArrayList<RegistrationAnchor> baseAnchorList = new ArrayList<RegistrationAnchor>(anchorList);
	      clearDatafile();
	      JFileChooser fc = new JFileChooser(new File(directory));
	      if(same){
	    	  IdenticalDataFileFilter datafileFilterDAS = new IdenticalDataFileFilter(imgName);
	    	  fc.setFileFilter(datafileFilterDAS);
	      }
	      else fc.setFileFilter(datafileFilterRSML);
	      
	      if (fc.showDialog(null, "Select Seed Datafile") == JFileChooser.APPROVE_OPTION)
	         read(fc.getSelectedFile().getAbsolutePath(), false, scale);
	      
	      if (baseAnchorList.size() < 2) return true;
	     
	      Point2D.Float baseRA1 = ((RegistrationAnchor) baseAnchorList.get(0)).getPoint();
	      Point2D.Float baseRA2 = ((RegistrationAnchor) baseAnchorList.get(1)).getPoint();
	      Point2D.Float seedRA1 = ((RegistrationAnchor) anchorList.get(0)).getPoint();
	      Point2D.Float seedRA2 = ((RegistrationAnchor) anchorList.get(1)).getPoint();
	      
	      float theta = NodeFitter.vectToTheta(seedRA2.x - seedRA1.x, seedRA2.y - seedRA1.y)
	                    - NodeFitter.vectToTheta(baseRA2.x - baseRA1.x, baseRA2.y - baseRA1.y);
	      AffineTransform at = AffineTransform.getTranslateInstance(baseRA1.x - seedRA1.x, baseRA1.y - seedRA1.y);
	      at.rotate(theta, seedRA1.x, seedRA1.y);
	      
	      for (int i = 0; i < rootList.size(); i++) {
	         ((Root) rootList.get(i)).transform(at);
	      }

	      anchorList = baseAnchorList;
	      return true;
	      }

   /**
    * Read and merge multiple datafiles
    */
   public void readImagesFromFolder() {
	      JFileChooser fc = new JFileChooser(new File(directory));
	      fc.setMultiSelectionEnabled(true);
	      fc.setFileFilter(imagefileFilter);
	      
	      for(int i = 0; i < rootList.size(); i++){
	    	  Root r = (Root) rootList.get(i);
	    	  for(int j = 0; j < 3; j++) r.multiplyNodes();
	      }
	      this.saveToRSML();
	      
	      if (fc.showDialog(null, "Select Multiple Datafiles") == JFileChooser.APPROVE_OPTION)
		      IJ.log(fc.getSelectedFile().length()+" files will be analysed");
	      	 String previousDataFName = dataFName; 
	         for(int i = fc.getSelectedFiles().length-1; i >= 0; i--){
	        	 
                 ImagePlus imp = imgOpener.openImage(fc.getSelectedFiles()[i].getAbsolutePath());
                 if (imp != null) {
                    RootImageCanvas ric = new RootImageCanvas(imp);
                    SRImageWindow imw = new SRImageWindow(imp, ric);
                    RootModel previous = new RootModel(imw, directory, previousDataFName);
                    //previous.reCenterAllNodes();
                    previous.cropTracing();
                    previous.deleteSmallRoots();
                    previous.saveToRSML();
                    
                    String fName = fc.getSelectedFiles()[i].getAbsolutePath();
                    fName = fName.substring(0, fName.lastIndexOf('.')+1);
                    fName = fName + "rsml";                    
                    previousDataFName = fName;
                    IJ.log(previousDataFName);
                    
                    imp.getWindow().close();
                    ric.kill();
                 }
                 
	         }
	      IJ.log("All files were traced");
	      }
   
   /**
    * Read and merge multiple datafiles
    */
   public void readMultipleDataFile() {
	      JFileChooser fc = new JFileChooser(new File(directory));
	      fc.setMultiSelectionEnabled(true);
	      fc.setFileFilter(datafileFilter);
	      if (fc.showDialog(null, "Select Multiple Datafiles") == JFileChooser.APPROVE_OPTION)
	         for(int i = 0; i < fc.getSelectedFiles().length; i++){
	        	 read(fc.getSelectedFiles()[i].getAbsolutePath(), false, i==0);
	         }
	      }
 
   
   /**
    * 
    * @param nodeDOM
    */
   public void readLinkedDatafile(org.w3c.dom.Node nodeDOM) {
      File f = new File(nodeDOM.getAttributes().getNamedItem("filename").getNodeValue());
      if (f.getParent() == null) f = new File(directory, f.getName());
      boolean[] b = new boolean[Mark.getTypeCount()];
      String[] m = nodeDOM.getAttributes().getNamedItem("marks").getNodeValue().split(",");
      for (int i = m.length - 1; i >= 0; i--) {
         int type = Mark.getTypeNum(m[i]);
         if (type >= 0) b[type] = true;
         } 
      if (f != null && f.exists()) linkedDatafileList.put(f, b);
      }

   /**
    * 
    * @param dataOut
    * @throws IOException
    */
   public void saveLinkedDatafileList(FileWriter dataOut) throws IOException {
      String nL = System.getProperty("line.separator");
      Iterator it = linkedDatafileList.keySet().iterator();
      File dir = new File(directory);
      while (it.hasNext()) {
         File f = (File)it.next();
         boolean[] b = (boolean[]) linkedDatafileList.get(f);
         int i = b.length - 1;
         while (i >= 0 && !b[i]) i--;
         if (i < 0) continue;
         dataOut.write(" <LinkedDatafile");
         if (f.getParentFile().equals(dir)) dataOut.write(" filename='" + f.getName() + "'");
         else dataOut.write(" filename='" + f.getAbsolutePath() + "'");
         dataOut.write(" marks='");
         String sep = "";
         while (i >= 0) {
            if (b[i]) {
               dataOut.write(sep + Mark.getName(i));
               sep = ",";
               }
            i--;
            }
         dataOut.write("'>" + nL + " </LinkedDatafile>" + nL);
         }
      }

   /**
    * 
    */
   public void clearLinkedDatafileList() {
      linkedDatafileList.clear();
      }

   /**
    * 
    */
   public void refreshLinkedDatafileList() {
      File current = new File(dataFName);
      File[] list = new File(directory).listFiles(datafileFilterRSML);
      if (list == null) return;
      for (int i = 0; i < list.length; i++) {
    	  System.out.println(list[i]);
         if (linkedDatafileList.containsKey(list[i])) continue;
         if (list[i].compareTo(current) == 0) continue;
         linkedDatafileList.put(list[i], new boolean[Mark.getTypeCount()]);
         }
      }
      
   /**
    * 
    */
   public void refreshLinkedMarks() {
//      Iterator it = rootList.iterator();
//      while (it.hasNext()) ((Root)(it.next())).removeLinkedMarks();
//      it = linkedDatafileList.keySet().iterator();
//      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
//
//      while (it.hasNext()) {
//         // Get file and display request;
//         File f = (File) it.next();
//         boolean[] b = (boolean[]) linkedDatafileList.get(f);
//         // Skip this linkedFile if there is no display request
//         int i = 0;
//         while (i < b.length && b[i] == false) i++;
//         if (i == b.length) continue;
//
//         
////         // Open the datafile
////         org.w3c.dom.Document documentDOM = null;
////         try {
////            DocumentBuilder builder = factory.newDocumentBuilder();
////            documentDOM = builder.parse(f);
////            }
////         catch (SAXException sxe) {
////            logReadError(f);
////            continue;
////            }
////         catch (ParserConfigurationException pce) {
////            logReadError(f);
////            continue;
////            }
////         catch (IOException ioe) {
////            logReadError(f);
////            continue;
////            }
////         documentDOM.normalize();
////         org.w3c.dom.Node nodeDOM = documentDOM.getFirstChild();
////         
////         // Check datafile header
////         if (!nodeDOM.getNodeName().equals("SmartRoot")) {
////            logReadError(f);
////            continue;
////            }
//         
//         
//  	   org.w3c.dom.Document documentDOM = null;
////  	   DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
//
//  	   try {
//  		   DocumentBuilder builder = factory.newDocumentBuilder();
//  		   documentDOM = builder.parse(f);
//  	   }
//  	   catch (SAXException sxe) {
//  		   logReadError();
//  		   return;
//  	   }
//  	   catch (ParserConfigurationException pce) {
//  		   logReadError();
//  		   return;
//  	   }
//  	   catch (IOException ioe) {
//  		   logReadError();
//  		   return;
//  	   }
//
//  	   documentDOM.normalize();
//  	      
//  	   org.w3c.dom.Node nodeDOM = documentDOM.getFirstChild();
//  	      	   
//  	   if (!nodeDOM.getNodeName().equals("rsml")) {
//  		   logReadError();
//  		   return;
//  	   }	         
//         
//  	   
//	   String origin = "smartroot";
//	   // Navigate the whole document
//	   nodeDOM = nodeDOM.getFirstChild();
//	   while (nodeDOM != null) {
//		   
//		   String nName = nodeDOM.getNodeName();
//		   
//		   // Get and process the metadata
//		   if(nName.equals("metadata")){
//			   org.w3c.dom.Node nodeMeta = nodeDOM.getFirstChild();
//			   String unit = "cm";
//			   float res = 1.0f;			   
//			   while (nodeMeta != null) {		   
//				   	String metaName = nodeMeta.getNodeName();				   	
//				   	// Get the image resolution
//				   	if(metaName.equals("unit")) unit = nodeMeta.getFirstChild().getNodeValue();	         		         
//				   	if(metaName.equals("resolution")) res = Float.valueOf(nodeMeta.getFirstChild().getNodeValue());
//					nodeMeta = nodeMeta.getNextSibling();
//			   }
//			   float linkedDPI = getDPI(unit, res);
//		       float dpiRatio = dpi / linkedDPI ;
//		   }
//		         
//		   // Get the plant
//		   if(nName.equals("scene")){
//			   org.w3c.dom.Node nodeScene = nodeDOM.getFirstChild();
//			   while (nodeScene != null) {		   
//				   	String sceneName = nodeScene.getNodeName();
//				   	
//				   if(sceneName.equals("plant")){
//					   org.w3c.dom.Node nodeRoot = nodeScene.getFirstChild();
//					   while (nodeRoot != null) {
//						   String rootName = nodeRoot.getNodeName();
//				   
//						   // Get the Roots
//						   if(rootName.equals("root")){
//							   
//							   String id = null;
//				               Root matchingRoot = null;
//				               float length = 0.0f;
//				               float rulerAtOrigin = 0.0f;
//				               float x = -1.0f;
//				               float y = 0.0f;
//				               Node n = new Node();
//				               
//				         	  org.w3c.dom.Node nn = nodeRoot.getAttributes().getNamedItem("label");
//				         	  nName = nn.getNodeValue();
//				               				            	   
//				              // Get rootID and seek for a match in the datafile of this RootModel
//		                     id = nodeRoot.getFirstChild().getNodeValue();
//		                     for (int j = 0; j < rootList.size(); j++) {
//		                        Root r = (Root)rootList.get(j);
//		                        if (nName.equals(r.getRootID())) {
//		                           matchingRoot = r;
//		                           break;
//		                           }
//		                        }
//		                     if (matchingRoot == null) break; // skip this root if there is no match
//		                     }
//						   
//				                  // Calculate root length (for LENGTH marks)
//				                  if (nName.equals("rulerAtOrigin")) {
//				                     rulerAtOrigin = Float.valueOf(childDOM.getFirstChild().getNodeValue()).floatValue();
//				                     }
//				                  else if (b[Mark.LENGTH] && nName.equals("Node")) {
//				                     n.readXML(childDOM, false);
//				                     if (x != -1.0f) length += NodeFitter.norm(x, y, n.x, n.y) * dpiRatio; // ####
//				                     x = n.x;
//				                     y = n.y;
//				                     }
//				                  // Read marks and filter those to be displayed
//				                  // #################### Need to handle ANCHOR marks (not sure actually)
//				                  else if (nName.equals("Mark")) {
//				                     Mark m = Mark.read(childDOM, matchingRoot, true, fName,  dpiRatio); // ####
//				                     m.move(rulerAtOrigin - matchingRoot.getRulerAtOrigin() * dpiRatio); // ####
//				                     if (m != null && b[m.type]) {
//				                        matchingRoot.addMark(m);
//				                        m.createGraphics();
//				                        }
//				                     }
//				                  childDOM = childDOM.getNextSibling();
//				                  }
//				                  
//				               if (b[Mark.LENGTH] && matchingRoot != null) {
//				                  Mark m = new Mark(Mark.LENGTH, matchingRoot, 
//				                                    length + rulerAtOrigin - matchingRoot.getRulerAtOrigin() * dpiRatio,
//				                                    fName, true, fName);
//				                  if (m != null) {
//				                     matchingRoot.addMark(m);
//				                     m.createGraphics();
//				                     }
//				                  }
//				               } 
//				            nodeDOM = nodeDOM.getNextSibling();
//				            }
//
//						   }
//						   nodeRoot = nodeRoot.getNextSibling();
//					   }
//				  }
//				   nodeScene = nodeScene.getNextSibling();  
//			   }	 
//		   }
//		   nodeDOM = nodeDOM.getNextSibling();  
//	   }  	   
//  	   
//            
////         float linkedDPI = Float.valueOf(nodeDOM.getAttributes().getNamedItem("dpi").getNodeValue()).floatValue();
////         float dpiRatio = dpi / linkedDPI ;
//
//         String fName = f.getName();
//         fName = fName.substring(0, fName.lastIndexOf("."));
//         
//         
//         // Begin scanning the datafile
//         nodeDOM = nodeDOM.getFirstChild();
//
//         while (nodeDOM != null) {
//            // Skip any nodeDOM which is not a Root
//            if (nodeDOM.getNodeName().equals("Root")) {
//               org.w3c.dom.Node childDOM = nodeDOM.getFirstChild();
//               String id = null;
//               Root matchingRoot = null;
//               float length = 0.0f;
//               float rulerAtOrigin = 0.0f;
//               float x = -1.0f;
//               float y = 0.0f;
//               Node n = new Node();
//               while (childDOM != null) {
//                  String nName = childDOM.getNodeName();
//                  // Get rootID and seek for a match in the datafile of this RootModel
//                  if (nName.equals("name")) {
//                     id = childDOM.getFirstChild().getNodeValue();
//                     for (int j = 0; j < rootList.size(); j++) {
//                        Root r = (Root)rootList.get(j);
//                        if (id.equals(r.getRootID())) {
//                           matchingRoot = r;
//                           break;
//                           }
//                        }
//                     if (matchingRoot == null) break; // skip this root if there is no match
//                     }
//                  // Calculate root length (for LENGTH marks)
//                  if (nName.equals("rulerAtOrigin")) {
//                     rulerAtOrigin = Float.valueOf(childDOM.getFirstChild().getNodeValue()).floatValue();
//                     }
//                  else if (b[Mark.LENGTH] && nName.equals("Node")) {
//                     n.readXML(childDOM, false);
//                     if (x != -1.0f) length += NodeFitter.norm(x, y, n.x, n.y) * dpiRatio; // ####
//                     x = n.x;
//                     y = n.y;
//                     }
//                  // Read marks and filter those to be displayed
//                  // #################### Need to handle ANCHOR marks (not sure actually)
//                  else if (nName.equals("Mark")) {
//                     Mark m = Mark.read(childDOM, matchingRoot, true, fName,  dpiRatio); // ####
//                     m.move(rulerAtOrigin - matchingRoot.getRulerAtOrigin() * dpiRatio); // ####
//                     if (m != null && b[m.type]) {
//                        matchingRoot.addMark(m);
//                        m.createGraphics();
//                        }
//                     }
//                  childDOM = childDOM.getNextSibling();
//                  }
//                  
//               if (b[Mark.LENGTH] && matchingRoot != null) {
//                  Mark m = new Mark(Mark.LENGTH, matchingRoot, 
//                                    length + rulerAtOrigin - matchingRoot.getRulerAtOrigin() * dpiRatio,
//                                    fName, true, fName);
//                  if (m != null) {
//                     matchingRoot.addMark(m);
//                     m.createGraphics();
//                     }
//                  }
//               } 
//            nodeDOM = nodeDOM.getNextSibling();
//            }
//         }
	      Iterator it = rootList.iterator();
	      while (it.hasNext()) ((Root)(it.next())).removeLinkedMarks();
	      it = linkedDatafileList.keySet().iterator();
	      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

	      while (it.hasNext()) {
	         // Get file and display request;
	         File f = (File) it.next();
	         boolean[] b = (boolean[]) linkedDatafileList.get(f);
	         // Skip this linkedFile if there is no display request
	         int i = 0;
	         while (i < b.length && b[i] == false) i++;
	         if (i == b.length) continue;
	         // Open the datafile
	         org.w3c.dom.Document documentDOM = null;
	         try {
	            DocumentBuilder builder = factory.newDocumentBuilder();
	            documentDOM = builder.parse(f);
	            }
	         catch (SAXException sxe) {
	            logReadError(f);
	            continue;
	            }
	         catch (ParserConfigurationException pce) {
	            logReadError(f);
	            continue;
	            }
	         catch (IOException ioe) {
	            logReadError(f);
	            continue;
	            }
	         documentDOM.normalize();
	         org.w3c.dom.Node nodeDOM = documentDOM.getFirstChild();
	         
	         // Check datafile header
	         if (!nodeDOM.getNodeName().equals("SmartRoot")) {
	            logReadError(f);
	            continue;
	            }
	            
	         float linkedDPI = Float.valueOf(nodeDOM.getAttributes().getNamedItem("dpi").getNodeValue()).floatValue();
	         float dpiRatio = dpi / linkedDPI ;

	         String fName = f.getName();
	         fName = fName.substring(0, fName.lastIndexOf("."));
	         // Begin scanning the datafile
	         nodeDOM = nodeDOM.getFirstChild();

	         while (nodeDOM != null) {
	            // Skip any nodeDOM which is not a Root
	            if (nodeDOM.getNodeName().equals("Root")) {
	               org.w3c.dom.Node childDOM = nodeDOM.getFirstChild();
	               String id = null;
	               Root matchingRoot = null;
	               float length = 0.0f;
	               float rulerAtOrigin = 0.0f;
	               float x = -1.0f;
	               float y = 0.0f;
	               Node n = new Node();
	               while (childDOM != null) {
	                  String nName = childDOM.getNodeName();
	                  // Get rootID and seek for a match in the datafile of this RootModel
	                  if (nName.equals("name")) {
	                     id = childDOM.getFirstChild().getNodeValue();
	                     for (int j = 0; j < rootList.size(); j++) {
	                        Root r = (Root)rootList.get(j);
	                        if (id.equals(r.getRootID())) {
	                           matchingRoot = r;
	                           break;
	                           }
	                        }
	                     if (matchingRoot == null) break; // skip this root if there is no match
	                     }
	                  // Calculate root length (for LENGTH marks)
	                  if (nName.equals("rulerAtOrigin")) {
	                     rulerAtOrigin = Float.valueOf(childDOM.getFirstChild().getNodeValue()).floatValue();
	                     }
	                  else if (b[Mark.LENGTH] && nName.equals("Node")) {
	                     n.readXML(childDOM, false);
	                     if (x != -1.0f) length += NodeFitter.norm(x, y, n.x, n.y) * dpiRatio; // ####
	                     x = n.x;
	                     y = n.y;
	                     }
	                  // Read marks and filter those to be displayed
	                  // #################### Need to handle ANCHOR marks (not sure actually)
	                  else if (nName.equals("Mark")) {
	                     Mark m = Mark.read(childDOM, matchingRoot, true, fName,  dpiRatio); // ####
	                     m.move(rulerAtOrigin - matchingRoot.getRulerAtOrigin() * dpiRatio); // ####
	                     if (m != null && b[m.type]) {
	                        matchingRoot.addMark(m);
	                        m.createGraphics();
	                        }
	                     }
	                  childDOM = childDOM.getNextSibling();
	                  }
	                  
	               if (b[Mark.LENGTH] && matchingRoot != null) {
	                  Mark m = new Mark(Mark.LENGTH, matchingRoot, 
	                                    length + rulerAtOrigin - matchingRoot.getRulerAtOrigin() * dpiRatio,
	                                    fName, true, fName);
	                  if (m != null) {
	                     matchingRoot.addMark(m);
	                     m.createGraphics();
	                     }
	                  }
	               } 
	            nodeDOM = nodeDOM.getNextSibling();
	            }
	         }	   
      }

//   /**
//    * 
//    */
//   public void deleteAutoSave(){
//	      FileInfo fileInfo = img.getOriginalFileInfo();
//    	  File f = new File(fileInfo.directory + fileInfo.fileName + "_temp1");
//    	  f.delete();
//    	  f = new File(fileInfo.directory + fileInfo.fileName + "_temp2");
//    	  f.delete();
//      }
   
//   /**
//    * 
//    * @param prior
//    */
//   public void autoSave(boolean prior) {
//	      FileInfo fileInfo = img.getOriginalFileInfo();
//	      if(prior) autoSave(fileInfo.directory + fileInfo.fileName + "_temp1");
//	      else autoSave(fileInfo.directory + fileInfo.fileName + "_temp2");
//	      }
//   
//   /**
//    * Auto save the datta structure
//    * @param fName
//    */
//   public void autoSave(String fName) {
//	      FileWriter dataOut;
//	      try {
//	         dataOut = new FileWriter(fName) ;
//	         }
//	      catch (IOException e) {
//	         SR.write("The datafile cannot be created or written to.");
//	         SR.write("Please check you have a WRITE access to the directory and ");
//	         SR.write("there is sufficient space on the disk.");
//	         return;
//	         }
//
//	      try {
//	          String nL = System.getProperty("line.separator");
//	          dataOut.write("<?xml version='1.0' encoding='iso-8859-1'?>" + nL);
//	          dataOut.write("<SmartRoot version='v3.2' dpi='" + Float.toString(dpi) 
//	                         + "' nextAutoRootID='" + Integer.toString(getNPRoot() + 1));
////	                         + "' nextRootKey='" + Integer.toString(nextRootKey+1) + "'>" + nL);
//
//	          saveLinkedDatafileList(dataOut);
//
//	          for (int i = 0; i < anchorList.size(); i ++) {
//	             ((RegistrationAnchor) anchorList.get(i)).save(dataOut);
//	             }
//
//	          for (int i = 0; i < rootList.size(); i ++) {
//	             Root r = (Root)(rootList.get(i));
//	             if (r.validate()) r.save(dataOut);
//	             else SR.write("Corrupted data for root " + r.getRootID() + ". The root was not saved.");
//	             }
//
//	          dataOut.write("</SmartRoot>" + nL);
//	          dataOut.close();
//	          }
//	       catch (IOException ioe) {
//	          SR.write("An I/O error occured while saving the datafile.");
//	          SR.write("The new datafile is thus most probably corrupted.");
//	          SR.write("It is recommended that you re-open the image and");
//	          SR.write("use a backup file before re-saving.");
//	          }
//	      } 
   
   /**
    * 
    */
   public void save() {
      // The following makes sure we are saving the datafile with the image.
      // If the image name has changed or the image has been moved to another location
      // the datafile should follow.
      FileInfo fileInfo = img.getOriginalFileInfo();
      save(fileInfo.directory + fileInfo.fileName);
      //saveCommon();
      }
   
   /**
    * 
    */
   public void saveToRSML() {
      // The following makes sure we are saving the datafile with the image.
      // If the image name has changed or the image has been moved to another location
      // the datafile should follow.
      FileInfo fileInfo = img.getOriginalFileInfo();
      saveToRSML(fileInfo.directory + fileInfo.fileName);
      }

   /**
    * Save function for the common XML structure
    * @param fName
    */
  public void saveToRSML(String fName){
      FileWriter dataOut;
      
      fit.checkImageProcessor();
      
      fName = fName.substring(0, fName.lastIndexOf('.')+1);

      int j = fileSuffixRSML.length - 1;
      File tmpFile = new File(fName + fileSuffixRSML[j]);
      if (tmpFile.exists()) tmpFile.delete();
      while (--j >= 0) {
    	  
         tmpFile = new File(fName + fileSuffixRSML[j]);
         if (tmpFile.exists()){
        	 tmpFile.renameTo(new File(fName + fileSuffixRSML[j + 1]));
         }
      }

      fName = fName.substring(0, fName.lastIndexOf('.'));

      try {
         dataOut = new FileWriter(fName+"." + fileSuffixRSML[0]) ;
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
          dataOut.write("		<resolution>" + Float.toString(dpi) + "</resolution>" + nL);
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
        dataOut.write("		    	<label>pixel</label>" + nL);
        dataOut.write("		        <type>float</type>" + nL);    
        dataOut.write("		        <unit>none</unit>" + nL);
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
        dataOut.write("			<label>" + imgName + "</label>" + nL);            
        dataOut.write("		</image>" + nL);
          
          
        dataOut.write("	</metadata>" + nL);

        // Define the scene  
        dataOut.write("	<scene>" + nL);
        dataOut.write("		<plant>" + nL);        
         
	      for (int i = 0; i < rootList.size(); i ++) {
	           Root r = (Root)(rootList.get(i));
	           if(r.isChild() == 0){
		            if (r.validate()) r.saveRSML(dataOut, fit);
	          }
	      }
          
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
    * Save the root system to an XML datafile
    * @param fName
    */
   public void save(String fName) {
      FileWriter dataOut;

      fName = fName.substring(0, fName.lastIndexOf('.') + 1);
      int i = fileSuffix.length - 1;
      File tmpFile = new File(fName + fileSuffix[i]);
      if (tmpFile.exists()) tmpFile.delete();
      while (--i >= 0) {
         tmpFile = new File(fName + fileSuffix[i]);
         if (tmpFile.exists()) 
            tmpFile.renameTo(new File(fName + fileSuffix[i + 1]));
         }

      try {
         dataOut = new FileWriter(fName + fileSuffix[0]) ;
         }
      catch (IOException e) {
         SR.write("The datafile cannot be created or written to.");
         SR.write("Please check you have a WRITE access to the directory and ");
         SR.write("there is sufficient space on the disk.");
         return;
         }

      try {
         String nL = System.getProperty("line.separator");
         dataOut.write("<?xml version='1.0' encoding='iso-8859-1'?>" + nL);
         dataOut.write("<SmartRoot version='"+version+"' dpi='" + Float.toString(dpi) 
                        + "' nextAutoRootID='" + Integer.toString(getNPRoot() + 1) + "'>" + nL);

         saveLinkedDatafileList(dataOut);

         for (i = 0; i < anchorList.size(); i ++) {
            ((RegistrationAnchor) anchorList.get(i)).save(dataOut);
            }
         
         int maxChild = 0;
         for (i = 0; i < rootList.size(); i ++) {
             Root r = (Root)(rootList.get(i));
             if(r.isChild() > maxChild) maxChild = r.isChild();
         }
         
         for(int j = 0 ; j <= maxChild ; j++){
	         for (i = 0; i < rootList.size(); i ++) {
	             Root r = (Root)(rootList.get(i));
	             if(r.isChild() == j){
		             if (r.validate()) r.save(dataOut);
		             else SR.write("Corrupted data for root " + r.getRootID() + ". The root was not saved.");
	             }
             }
         }
         
//         for (i = 0; i < rootList.size(); i ++) {
//            Root r = (Root)(rootList.get(i));
//            if (r.validate()) r.save(dataOut);
//            else SR.write("Corrupted data for root " + r.getRootID() + ". The root was not saved.");
//            }

         dataOut.write("</SmartRoot>" + nL);
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
    * Get the directory containing the image
    * @return
    */
   public String getDirectory () { return directory; }
 
   /**
    * Refresh the graphics
    */
   public void repaint() {
      ric.repaint();
      }

   /**
    * Display the root system
    * @param g2D
    */
   public void paint(Graphics2D g2D, boolean tips, boolean color) {

      // used by makeRulerMarkerLine
      if (frc == null) {
         frc = g2D.getFontRenderContext();
         font = g2D.getFont();
         }

      Rectangle rect = ric.getSrcRect();
      float magnification = (float) ric.getMagnification();

      g2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

      if (magnification != previousMagnification) {
         currentFont = font.deriveFont(12.0f / magnification);
         previousMagnification = magnification;
         }
      g2D.setFont(currentFont);

      AffineTransform g2dt = g2D.getTransform();
      g2D.transform(new AffineTransform(magnification, 0.0f, 0.0f, magnification,
                                        magnification * (-rect.x + 0.5f), magnification * (-rect.y + 0.5f)));
      
      for (int i = 0; i < rootList.size(); i++) {
         Root r = (Root) rootList.get(i);
         r.paint(g2D, magnification, false, displayAxis, displayNodes, 
                                        displayBorders, displayArea, displayTicks, displayMarks, displayTicksP, tips, color);
         }

      g2D.setComposite(ac1);
      for (int i = 0; i < anchorList.size(); i++) {
         ((RegistrationAnchor) anchorList.get(i)).paint(g2D, magnification);
         }

      rulerGP.reset();

      if (ric.isListenerRuler()) {
         g2D.setComposite(ac1);
         g2D.setColor(Color.green);
         g2D.draw(rulerLine);
         ric.listenerRulerDone();

         rulerGP.append(rulerLine, false);
         rulerLine.setLine(0.0f, 0.0f, 0.0f, 0.0f);
         }

      else if (ric.isRulerMode() && rulerLine1 != null) {
         g2D.setComposite(ac1);
         g2D.setColor(Color.green);
         g2D.draw(rulerLine);
         g2D.setColor(new Color(196, 196, 255));
         g2D.fill(rulerRect);
         g2D.setColor(Color.black);
         g2D.draw(rulerRect);
         g2D.drawString(rulerLine1, rulerPoint.x, rulerPoint.y);

         rulerGP.append(rulerLine, false);
         rulerGP.append(rulerRect, false);
         rulerLine.setLine(0.0f, 0.0f, 0.0f, 0.0f);
         rulerRect.setRect(0.0f, 0.0f, 0.0f, 0.0f);
         rulerLine1 = " ";
         }
      
      if(selected){
    	  createGraphics();
          g2D.setComposite(ac2);
          g2D.setColor(Color.blue);
          g2D.draw(convexhullGP);
          g2D.fill(convexhullGP);
      }
      
      g2D.setTransform(g2dt);
   }

  /**
   * TODO finish this fonction 
   * @param x
   * @param y
   * @return
   */
   public void traceLateral(int x, int y){
	   Root r1 = selectedRoot;
	   Node n = selectedRoot.getClosestNode(markerPosition/pixelSize);
	   int ls = rootList.size();
	   boolean side = getLateralSide(n, x, y);
	   addNode(x, y, 1, LATERAL_TRACING_ONE, "lat_", side, selectedRoot, n);

	   // If the new root was created
	   if(rootList.size() == ls + 1){
		   Root rc = (Root) rootList.get(0);
		   rc.attachParent(r1, false);
		   r1.attachChild(rc);	   
	   }
   }
   
   /**
    * Get the side on which the lateral root is inserted
    * @param parent the parent node
    * @param x the x coordinate of the node
    * @param y the y coordinate of the node
    * @return true if right, false if left
    */
   public boolean getLateralSide(Node parent, int x, int y){
	   
      float angle = NodeFitter.vectToTheta(x - parent.x, y - parent.y);
      float diff = (float) Math.PI - parent.theta;
      if(angle - diff > Math.PI && angle - diff < 2 * Math.PI) return true;
      else return false; 
   }
 
   
   /**
    * Build nodes from a list of [x,y] coordinates
    * @param coord: the [x,y] coordinates for the roots to be drawn
    */
   public void buildNodesFromCoord(int[][]coord){
	   long startD = System.currentTimeMillis();
	   	int ls = rootList.size(); 
		for(int i = 0; i < coord.length; i++){
			SR.write("x = "+coord[i][0]+" / "+coord[i][1]);
		   	addNode(coord[i][0], coord[i][1], 1, REGULAR_TRACING, "lat", true, null, null);
		}
	   
		long endD = System.currentTimeMillis();
		SR.write((rootList.size() - ls)+"laterals created in "+(endD - startD)+" ms");
   }
   /** 
    * This method will check the side of a selected root to see if there is lateral roots around it.
    * The border of the root is scanned at every point, according to the settings in FCSettings.
    * Takes somme time but find a fair amount of laterals .
    * Old method, not used anymore. Kept for historical purposes
    */
   public void findLaterals(){
	long startD = System.currentTimeMillis();
	
   	getFCSettings();
   	int ls = rootList.size();					// root list size
   	Root r = selectedRoot;						// selected root
    int lat = r.childList.size();
    int step = nStep;							// number of steps between two nodes to find laterals
    int cr = 1, dr = 0, signX = 1, signY = 1;
    float sx4, sy4, sx5, sy5, dist, diam = 0.0f;
	float it = (dMax - dMin) / nIt;				// iterative waves
	String name = SR.prefs.get("lateral_ID", "lat_");
		
		Node n = r.firstNode; 
		ArrayList<Node> start = new ArrayList<Node>();			// begin nodes for the algorithm
		ArrayList<Node> end = new ArrayList<Node>();			// end nodes for the algorithm
		start.add(r.firstNode);
		end.add(r.lastNode);
		
		
		/* Check if there is some interval marks associated with the root.
		 * I there is some, the algorithm will only be applied inside the intervals.
		 */
		Mark m;
		boolean mark = false;
		for(int g = 0 ; g < r.markList.size(); g++){
			m = (Mark) r.markList.get(g);
			if(m.needsTwinPosition()){
				if(!mark){
					start.remove(r.firstNode);
					end.remove(r.lastNode);
					mark = true;
				}
				start.add(r.getClosestNode(m.lPos));
			end.add(r.getClosestNode(m.twinLPos));
			}
		}
		
		/* Get the maximum diameter of the primary root */
	if (n != null) {
    	while (n.child != null) {
    		if (n.diameter > diam) diam = n.diameter;
    		n = n.child;
    	} 
	}
	
	for(int h = 0 ; h < start.size(); h++){					// run the algorithm inside the different intervals
   		for(dist = dMin; dist <= dMax; dist += it ){		// different waves of the algorithm
			n = (Node) start.get(h);
   			n.calcParallels(dist);
   			n.calcPoles(dist);
			if (n != null) {
		    	while (n.child != null && n != (Node) end.get(h)) {
		    		n.child.calcParallels(dist);
		    		n.child.calcPoles(dist);
		    		sx4 = Math.abs((n.child.px[4] - n.px[4])) / step;
		    		sy4 = Math.abs((n.child.py[4] - n.py[4])) / step;
		    		sx5 = Math.abs((n.child.px[5] - n.px[5])) / step;
		    		sy5 = Math.abs((n.child.py[5] - n.py[5])) / step;
		    		
					if (n.px[1] < n.child.px[1]) signX = -1; 	// root is going right
					else signX = 1;								// root is going left
						
					if (n.py[1] < n.child.px[1]) signY = -1;	// root is going down
					else signY = 1;								// root is going up
					
						/* scan the parallels between the nodes*/
						for (int k=0; k<=step; k++){ 
							/* check left*/
					    	addNode(n.px[4] + ( signX * k * sx4), n.py[4] + (signY * k * sy4), 1, LATERAL_TRACING, name + (cr + lat), true, r, n);
					    	if (rootList.size() == ls + cr){
					    		Root rc = (Root) rootList.get(0);
					    		if (isInParent(rc) || !distanceFromParent(rc) || checkRSize && checkRootSize(rc, diam, true)) dr++;
					    		else{
					    			rc.attachParent(r, false);
					    			r.attachChild(rc);
					    			cr++;
					    		}					    	
					    	}
					    	/* check right*/
					    	addNode(n.px[5] + (signX * k * sx5), n.py[5] + (signY * k * sy5), 1, LATERAL_TRACING, name + (cr +lat), false, r, n);
					    	if (rootList.size() == ls + cr){
				    			Root rc = (Root) rootList.get(0);
					    		if (isInParent(rc) || !distanceFromParent(rc) || checkRSize && checkRootSize(rc, diam, true)) dr++;
					    		else{
					    			rc.attachParent(r, false);
					    			r.attachChild(rc);
					    			cr++;
					    		}					    	
					    	}			
						}
		            n = n.child;
		    		n.calcParallels(dist);	
		    		n.calcPoles(dist);
		    	}
			}
  		}
	}
	long endD = System.currentTimeMillis();
	SR.write((cr-1)+" laterals created in "+(endD - startD)+" ms");
}

   /**
    * This method will check the side of a selected root to see if there is lateral roots around it.
    * Use the image squeleton to check the presence of the roots. Fast and find most of the roots
    */
   public void findLaterals2(){
	   
	   // If there is no binary processor attached to the rootmodel
	   if(bp == null){
		   findLaterals();
		   return;
	   }
    	long startD = System.currentTimeMillis();	   
    	
	   	getFCSettings();
	   	Root r = selectedRoot;	
	   	int ls = rootList.size();
	    float dist = 0.0f;
		float it = (dMax - dMin) / nIt;				// iterative waves
		
		fit.checkImageProcessor();

   		Node n = r.firstNode; 
   		ArrayList<Node> start = new ArrayList<Node>();			// begin nodes for the algorithm
   		ArrayList<Node> end = new ArrayList<Node>();			// end nodes for the algorithm
   		start.add(r.firstNode);
   		end.add(r.lastNode);
   		
   		
   		/* Check if there is some interval marks associated with the root.
   		 * I there is some, the algorithm will only be applied inside the intervals.
   		 */
   		Mark m;
   		boolean mark = false;
   		for(int g = 0 ; g < r.markList.size(); g++){
   			m = r.markList.get(g);
   			if(m.needsTwinPosition()){
   				if(!mark){
   					start.remove(r.firstNode);
   					end.remove(r.lastNode);
   					mark = true;
   				}
   				start.add(r.getClosestNode(m.lPos));
				end.add(r.getClosestNode(m.twinLPos));
   			}
   		}
		
		for(int h = 0 ; h < start.size(); h++){					// run the algorithm inside the different intervals
	   		for(dist = dMin; dist <= dMax; dist += it ){		// different waves of the algorithm
				n = start.get(h);
	   			n.calcParallels(dist);
	   			n.calcPoles(dist);
				if (n != null) {
					
			    	while (n.child != null && n != (Node) end.get(h)) {
			    		n.child.calcParallels(dist);
			    		n.child.calcPoles(dist);
						
						autoDrawLaterals2(n.px[4], n.py[4],n.child.px[4],n.child.py[4], n, r, true); // Build on right
						autoDrawLaterals2(n.px[5], n.py[5],n.child.px[5],n.child.py[5], n, r, false); // Build on left
						
						repaint();
			            n = n.child;
			    		n.calcParallels(dist);	
			    		n.calcPoles(dist);
			    	}
				}
	  		}
		}
		long endD = System.currentTimeMillis();
		SR.write((rootList.size() - ls)+" lateral(s) created in "+(endD - startD)+" ms");
   }

/**
 * Method to check for the presence of a lateral root based on the image skeleton. 
 * The algorithm walks along a line between to node and if it crosse a black spot (skeleton)
 * it tries to build a new node
 * @param x1
 * @param y1
 * @param x2
 * @param y2
 * @param n
 * @param r
 * @param right
 */
   public void autoDrawLaterals2(float x1, float y1, float x2, float y2, Node n, Root r, boolean right){

	float max=0, min = 255;
	boolean in = false;
	int ls = rootList.size();
	int cr = 1;
	int nLat;
	String name = SR.prefs.get("lateral_ID", "lat_");
	
	float kL = 0;
	float dX = x2 - x1;
	float dY = y2 - y1;
	float l = (float) Math.sqrt(dY * dY + dX * dX);
	float step = 0.01f / l;
	
	// find the roots
	for (float k = 0; k <= 1f; k += step) { 
		float px = bp.get((int) (x1 + k * dX),(int) (y1 + k * dY));
		if (!in && px < 125){
			in = true;
			kL = k;
		}
		else if (in && px > 125){
			in = false;
			nLat = r.childList.size() + 1;
			addNode(x1 + (k+kL) * dX /2, y1 + (k+kL) * dY /2 , 1, LATERAL_TRACING, name + nLat, right, r, n);
	    	if (rootList.size() == ls + cr){
	    		Root rc = (Root) rootList.get(0);
	    		rc.attachParent(r, false);
	    		r.attachChild(rc);
	    		cr++;
	    	}
		}
	}
   }   

   
   /** This method check the size of the newly created root.
    * It might delete the newly created root if it is too small
    * The aim of the method is to detect "noise" when creating new laterals
    * @param r root being tested
    * @param lMin minimal size of the root
    * @return true if the root is too small and deleted, false if the size is OK*/
   
   public boolean checkRootSize(Root r, float lMin, boolean m) {
	   double min = 1;
	   if(m) min = minRootSize;
	   if (r.getRootLength() < (lMin * min)){
		   selectedRoot = r;
		   deleteSelectedRoot();
           ric.repaint();
    	   return true;
	   }
	   return false;
   }
   
   /** This method check if the lateral root is taking the right direction.
    * The method will get the insertion angle between the root and the parent and compare it
    * to the maximal angle defined by the user.
    * @param n first node of the root being created
    * @param nP node of the parent root
    * @param tracingRoot root which is checked
    * @param lr side of the primary root on which the lateral is created (true = left / false = right)
    * @return true if the direction is right, false if the direction is false*/

   public boolean checkRootDirection(Node n, Node nP, Root tracingRoot, boolean lr){
	   
	   if(fit.setInsertAng(nP.theta, n.theta, lr) > maxAngle){// || currentNode.y < previousNode.y){
           selectedRoot = tracingRoot;
	       deleteSelectedRoot();
	       this.tracingRoot = null;
	       return false;
	   }
	   else return true;
   }
   
   /** This method check a the size of a lateral node.
    * It might delete this node and the associated root if the node is either too small or too big.
    * @param parentDiameter diameter of the closest parental node
    * @param tracingNode lateral node which will be checked
    * @param tracingRoot lateral root which will be tested
    * @return true is the node is neither too big or too small, false if the node is either too big or too small*/

   public boolean checkNodeSize(float parentDiameter, Node tracingNode, Root tracingRoot) {
	   
	   // check the diameter of the root
	   // if the node is too small, it will not be created
       if(tracingNode.diameter < parentDiameter * minNodeSize){
           selectedRoot = tracingRoot;
           deleteSelectedRoot();
           this.tracingRoot = null;
           return false;
       }
       // if the root is too big, it will not be created nether.
       else if(tracingNode.diameter > parentDiameter * maxNodeSize){
           selectedRoot = tracingRoot;
           deleteSelectedRoot();
           this.tracingRoot = null;
           return false;
       }
       else return true;
}

   /** This method check a the position of a lateral node.
 *  It might delete this node and the associated root if the node is too close to an other node (from an other root), it will not be created
 * @param tracingNode node checked
 * @param tracingRoot root checked
 * @return true if the node is not too close from an other root, false if the node is too close from and other node*/

   public boolean checkNodePosition(Node tracingNode, Root tracingRoot) {

       // check the position of the root
       // if the node is to close to an other one, it will not be created
	
       int list = rootList.size();
       double dist;
       Node n;
       Root r;
       for(int i=0; i<list; i++){
    	   r = (Root) rootList.get(i);
    	   n = r.firstNode;
		   dist = Point2D.distance((double) n.x, (double) n.y, (double) tracingNode.x, (double) tracingNode.y);
	       if(r.getRootKey() != tracingRoot.getRootKey() && dist < tracingNode.diameter){
	           selectedRoot = tracingRoot;
	           deleteSelectedRoot();
	           this.tracingRoot = null;
	           return false;	
	       }
	       while(n.child != null){
	    	   n = n.child;
			   dist = Point2D.distance((double) n.x, (double) n.y, (double) tracingNode.x, (double) tracingNode.y);
		       if(r.getRootKey() != tracingRoot.getRootKey() && dist < tracingNode.diameter){
		           selectedRoot = tracingRoot;
		           deleteSelectedRoot();
		           this.tracingRoot = null;
		           return false;	
		       }
	       }
       }
	   return true;
	}


	/** This method check if some of the node of the selected root are inside its parent.
	 * @param r the root which will be checked.
	 * @param p the parent root
	 * @return true is the root was in parent and was deleted
	 */

	public boolean isInParent(Root r){		
		Root p = r.getParent();
		Node n = r.lastNode;
		selectedRoot = r;
		if(p.contains(n.x, n.y)) {
			deleteSelectedRoot();	
			return true;
		}
		while( n.parent != null ){
			n = n.parent;
			if(p.contains(n.x, n.y)) {
				r.rmBaseOfRoot(n, this);
				}
		}
		return false;
	}
	
	
	/** This method detect and build root along a strait line defined by the user.
	 * The origin of the line remains the same as the one define by the user.
	 * The x coordinate of the end of the line remains the same while the y coordinate equals the y coordinate of the begining of the line (y2 = y1)
	 * @param x1 x coordinate of the origin of the line drawn on the image
	 * @param y1 y coordinate of the origin of the line drawn on the image
	 * @param x2 x coordinate of the end of the line drawn on the image
	 * @param pix an array containing the values of all the pixel along the line traced by the user
	 * */
	public void autoDrawRoot(int x1, int y1, int x2, int y2, double[] pix){
		
		/* Set the automatic threshold for the root creation*/
		int max=0;
		int min=256;		
		int inc = 0;
		double [] pix1 = new double[pix.length];
		for(int i = 0 ; i < 256 ; i++){
			inc = 0;
			for (int j = 0 ; j < pix.length ; j ++){ 
				if(pix[j] == i){
					pix1[j] = i;
					inc ++;
				}
			}
			if (inc > 10){
				break;
			}
		}
		
		for(int i = 0 ; i < pix1.length ; i ++){
			int p =(int) pix1[i];
			if(p < min && p != 0) min = p;
			if(p > max) max = p;
		}
		autoThreshold = max - ((max - min) / 10) ;

		/* Create the roots*/
		fit.checkImageProcessor();
		boolean in = false;
		int ls = rootList.size();
		int root = 0;
		float kL = 0;
		float dX = x2 - x1;
		float dY = y2 - y1;
		float l = (float) Math.sqrt(dY * dY + dX * dX);
		float step = 1f / l;
		for (float k = 0; k <= 1f; k += step) { 
			float px = fit.getValue(x1 + k * dX, y1 + k * dY);
			if (!in && px < autoThreshold){
				in = true;
				kL = k;
			}
			else if (in && px >= autoThreshold){
				in = false;
				addNode(x1 + (k+kL) * dX /2, y1 + (k+kL) * dY /2 , 1, LINE_TRACING, "", false, null, null);
				if (rootList.size() == ls + root + 1){
					root++;
			}
			}
		}
		
		/* Check the size of the newly created roots */
		int dR = 0;
		float avg = getAvgRLength() / (5 * pixelSize);
		for(int i = 0 ; i < root ; i ++){
			Root r = (Root)rootList.get(i-dR);
			if(checkRootSize(r, avg, false)) dR++;
		}
		
		/* Renaming of roots */
		root = root-dR;
		for(int i = root-1; i >= 0 ; i --){
			Root r = (Root)rootList.get(i);
			String name = SR.prefs.get("root_ID", "root_") + Integer.toString(nextAutoRootID);
			r.setRootID(name);
			nextAutoRootID++;
		}
		
		SR.write("Number of root created = "+(root));
	}
	
	/** Get the manual threshold use for the autoDrawRoot method
	 * @return the manual threshold*/
	
	public int getManualThreshold(){
		return manThreshold;
	}
	
	/** Check the distance between the base node of the root and its parent .
	 * @param r the root of which we want to find the distance with the parent
	 * @return true if root close enough, false if not*/
	
	public boolean distanceFromParent(Root r){
		r.setParentNode();
		Node n1 = r.firstNode;
		Node n2 = r.getParentNode();
		double d = getDistNearestLink(n1.x, n1.y);
		if ( d > minRootDistance * n2.diameter){ 
		    selectedRoot = r;
			deleteSelectedRoot();
	        ric.repaint();
			return false;
		}
		return true;
	}
	
	
	/** Get the closest root from the base of a given root 
	 * @param r the root from which we want the closest root
	 * @return the closest root from the apex of the root r*/
	
	public Root getClosestRoot(Root r){
		Node n = r.firstNode;
		int ls = rootList.size();
		if (ls == 1) return null;
		Root rp;
		Root rpFinal = null;
		float dist;
		float distMin = 1000000.0f;
		
		for (int i = 0; i < ls; i++){
			rp =(Root) rootList.get(i);
			if(rp.getRootKey() == r.getRootKey()) continue;
			Node np = rp.firstNode;
			dist = (float) Point2D.distance((double) n.x, (double) n.y, (double) np.x, (double) np.y);
			if (dist < distMin){
				distMin = dist;
				rpFinal = rp;
			}
			while (np.child != null){
				np = np.child;
				dist = (float) Point2D.distance((double) n.x, (double) n.y, (double) np.x, (double) np.y);
				if (dist < distMin){
					distMin = dist;
					rpFinal = rp;
				}
			}
		}
		return rpFinal;
	}
	
	/** This method allow the user to manually define a parent for a root. 
	 * The first choise givent o the user is the closest root from the base of the selected root. 
	 * If the user is not satisfied with this option, he can choose between all the existing roots of the image.
	 * @return true if one root was set as parent, false if no root were set as parent*/
	
	public boolean setParent(){
		Root r = selectedRoot;
		Root rp = getClosestRoot(r);
		if (rp == null) {
			SR.write("Only one root known");
			return false;
		}	
		int l = rootList.size();
		String[] list = new String[l-1];
		int id = 0;
		Root r0;
		for(int i = 0; i < l; i++){ 
			r0 = (Root) rootList.get(i);
			if (r0.getRootKey() == r.getRootKey()) {
				id = 1;
				continue;
			}
			String n2 = r0.getRootID();
			list[i-id] = n2;
		}
		ParentDialog pD = new ParentDialog(list, r, rp, this);
		pD.setVisible(true);
		return false;
	}
	
	/**
	 * Attach c to p
	 * @param p the parent root
	 * @param c the child root
	 */
	public void setParent(Root p, Root c){
		c.attachParent(p);
		p.attachChild(c);
	}
	
	/**
	 * Attach c to p
	 * @param p the parent root
	 * @param c the child root
	 */
	public void setParent(Root p, Root c, boolean det){
		if(det) c.detacheParent();
		c.attachParent(p);
		p.attachChild(c);
	}	
	
	
	
	/** This method allow the user to manually remove a parent of a selected root*/
	
	public void detacheParent(){
		detacheParent(selectedRoot);
	}
	
	/**
	 * Detach the parent of the selected root
	 * @param selectedRoot the root which parent will be detach
	 */
	public void detacheParent(Root r){
		r.detacheParent();
		SR.write("Parent removed");
	}
	
	/**
	 * Detach all the children of the selected root
	 */
	public void detacheAllChilds(){
		Root r = selectedRoot;
		int ls = r.childList.size();
		for (int i =0; i < ls; i ++){
			Root cr = (Root) r.childList.get(0);
			cr.detacheParent();
		}		
	}
	
	/**
	 * @return the parent's name of the selected root
	 */
	public String getSelectedRootParentID(){
		return selectedRoot.getParent().getRootID();
	}
	
	/**
	 * @return the parent's nmae of the selected root
	 */
	public String getSelectedRootOrder(){
		String t = "0";
		switch(selectedRoot.isChild()){
		case(1): t = "1";
		break;
		case(2): t = "2";
		}
		return t;
	}
	
	
	/**
	 * Get the settings for the findChild method
	 */
	public void getFCSettings(){
		
		nStep = FCSettings.nStep;	
		dMin = FCSettings.dMin;
		dMax = FCSettings.dMax;
		nIt = FCSettings.nIt;	
		checkNSize = FCSettings.checkNSize;
		checkRDir = FCSettings.checkRDir;
		checkRSize = FCSettings.checkRSize;	
		minNodeSize = FCSettings.minNodeSize;
		maxNodeSize = FCSettings.maxNodeSize;	
		minRootSize = FCSettings.minRootSize;
		minRootDistance = FCSettings.minRootDistance;
		doubleDir = FCSettings.doubleDir;
		maxAngle = FCSettings.maxAngle * ((float) Math.PI / 180);
	}
	
	/**
	 * @return the number of primary roots
	 */
	public int getNPRoot(){
		int n = 0;
		Root r;
		for(int i =0 ; i < rootList.size() ; i++){
			r =  (Root) rootList.get(i);
			if (r.isChild() == 0) n++;
		}
		return n;
	}
	
	/**
	 * @return the number of secondary roots
	 */
	public int getNSRoot(){
		int n = 0;
		Root r;
		for(int i =0 ; i < rootList.size() ; i++){
			r =  (Root) rootList.get(i);
			if (r.isChild() !=0) n++;
		}
		return n;
	}
	
	/**
	 * @return the average length of secondary roots
	 */
	public float getAvgSRLength(){
		float n = 0;
		int m = 0;
		Root r;
		for(int i =0 ; i < rootList.size() ; i++){
			r =  (Root) rootList.get(i);
			if (r.isChild() !=0) {
				n += r.getRootLength();
				m++;
			}
		}
		return n/m * pixelSize;
	}
	
	/**
	 * @return the average length of primary roots
	 */
	public float getAvgPRLength(){
		float n = 0;
		int m = 0;
		Root r;
		for(int i =0 ; i < rootList.size() ; i++){
			r =  (Root) rootList.get(i);
			if (r.isChild() == 0 ) {
				n += r.getRootLength();
				m++;
			}
		}
		return n/m * pixelSize;
	}
	
	/**
	 * Get the total root length
	 * @return
	 */
	public float getTotalRLength(){
		float n = 0;
		Root r;
		for(int i =0 ; i < rootList.size() ; i++){
			r =  (Root) rootList.get(i);
			n += r.getRootLength();
		}
		return n * pixelSize;		
		
	}
	
	/**
	 * @return the average length of all roots
	 */
	public float getAvgRLength(){
		float n = 0;
		int m = 0;
		Root r;
		for(int i =0 ; i < rootList.size() ; i++){
			r =  (Root) rootList.get(i);
			n += r.getRootLength();
			m++;
		}
		return n/m * pixelSize;
	}
	
	/**
	 * @return the average diameter of secondary roots
	 */
	public float getAvgSRDiam(){
		float n = 0;
		int m = 0;
		Root r;
		for(int i =0 ; i < rootList.size() ; i++){
			r =  (Root) rootList.get(i);
			if (r.isChild() != 0) {
				Node node = r.firstNode;
				n += node.diameter;
				m++;
				while (node.child != null){
					node = node.child;
					n += node.diameter;
					m++;
				}
			}
		}
		return n/m * pixelSize;
	}
	
	/**
	 * @return the average insertion angle of secondary roots
	 */
	public float getAvgSRInsAng(){
		float n = 0;
		int m = 0;
		Root r;
		for(int i =0 ; i < rootList.size() ; i++){
			r =  (Root) rootList.get(i);
			if (r.isChild() !=0 ) {
				n += r.getInsertAngl() * (180 / Math.PI);
				m++;
			}
		}
		return n/m ;
	}
	
	/**
	 * @return the average diameter of primary roots
	 */
	public float getAvgPRDiam(){
		float n = 0;
		int m = 0;
		Root r;
		for(int i =0 ; i < rootList.size() ; i++){
			r =  (Root) rootList.get(i);
			if (r.isChild() == 0) {
				Node node = r.firstNode;
				n += node.diameter;
				m++;
				while (node.child != null){
					node = node.child;
					n += node.diameter;
					m++;
				}
			}
		}
		return n / m * pixelSize;
	}
	
	/**
	 * @return the average interbranch distance
	 */
	public float getAvgInterBranch(){
		float iB = 0;
		Root r;
		int n = 0;
		for(int i = 0 ; i < rootList.size() ; i++){
			r = (Root) rootList.get(i);
			if(r.getInterBranch() != 0){
				iB += r.getInterBranch();
				n++;
			}
		}
		return (iB / n) * pixelSize;
	}
	
	/**
	 * @return the average interbranch distance
	 */
	public float getSTDevInterBranch(float ib){
		float avg = ib;
		
		float iB = 0;
		float iBMin = 1000000;
		float iBMax = 0;
		Root r;
		int n = 0;
		for(int i = 0 ; i < rootList.size() ; i++){
			r = (Root) rootList.get(i);
			if(r.getInterBranch() != 0){
				if(r.getInterBranch() < iBMin) iBMin = r.getInterBranch();
				if(r.getInterBranch() > iBMax) iBMax = r.getInterBranch();
				iB += Math.pow((r.getInterBranch() * pixelSize) - avg, 2);
				n++;
			}
		}
		return iB / n;
	}
	
	/**
	 * @return the number of nodes of the primary roots
	 */
	public int getNPNode(){
		int m = 0;
		Root r;
		for(int i =0 ; i < rootList.size() ; i++){
			r =  (Root) rootList.get(i);
			if (r.isChild() == 0) {
				Node node = r.firstNode;
				m++;
				while (node.child != null){
					node = node.child;
					m++;
				}
			}
		}
		return m;
	}
	
	/**
	 * @return the number of node of the secondary roots
	 */
	
	public int getNSNode(){
		int m = 0;
		Root r;
		for(int i =0 ; i < rootList.size() ; i++){
			r =  (Root) rootList.get(i);
			if (r.isChild() != 0) {
				Node node = r.firstNode;
				m++;
				while (node.child != null){
					node = node.child;
					m++;
				}
			}
		}
		return m;
	}
	
	
	/**
	 * @return a list of strings containing all the name of roots having children
	 */
	public String[] getParentRootNameList(){
		int ind = 0;
		int c = 0;
		Root r;
		for (int i = 0 ; i < rootList.size(); i++){
			r = (Root) rootList.get(i);
			if(r.childList.size() != 0){
				ind ++;
			}
		}	
		String[] n = new String[ind];
		for (int i = 0 ; i < rootList.size(); i++){
			r = (Root) rootList.get(i);
			if(r.childList.size() != 0){
				n[i-c] = r.getRootID();
			}
			else c++;
		}	
		return n;
	}
	
	/**
	 * @return a list of strings containing all the name of primary
	 */
	public String[] getPrimaryRootNameList(){
		int ind = 0;
		int c = 0;
		Root r;
		for (int i = 0 ; i < rootList.size(); i++){
			r = (Root) rootList.get(i);
			if(r.isChild() == 0){
				ind ++;
			}
		}	
		String[] n = new String[ind];
		for (int i = 0 ; i < rootList.size(); i++){
			r = (Root) rootList.get(i);
			if(r.isChild() == 0){
				n[i-c] = r.getRootID();
			}
			else c++;
		}	
		return n;
	}
	
	/**
	 * @return the average child density of all the parent roots of the image
	 */
	public float getAvgChildDens(){
		float cd = 0;
		int n = 0;
		Root r;
		for (int i = 0 ; i < rootList.size(); i++){
			r = (Root) rootList.get(i);
			if(r.getChildDensity() != 0){
				cd += r.getChildDensity();
				n++;
			}
		}
		return cd / n;
	}
	
	/**
	 * 
	 */
	public void selectRoot(Root r){
		selectedRoot = r;
	}

	public void selectRoot(Root r, boolean flag){
		r.setSelected(flag);
		r.displayConvexHull(displayConvexHull);
	}
	
	/*
	 * Auto generated method to make the root model an implementation of TreeModel
	 * @see javax.swing.tree.TreeModel#getRoot()
	 */
	
	/**
	 * 
	 */
	public Object getRoot() {
		return this;
	}

	/**
	 * 
	 */
	public Object getChild(Object parent, int index) {
		if(parent.equals(this)) {
			ArrayList<Root> rA = new ArrayList<Root>();
			for(int i = 0 ; i < rootList.size(); i++){
				Root r = (Root) rootList.get(i);
				if(r.isChild() == 0){rA.add(r);}
			}		
			return rA.get(index);
		}
		else{
			Root r = (Root) parent;
			//if(r.markList.size() > 0) return r.markList.get(index);
			if(r.childList.size() > 0){
				if(index >= r.childList.size()) index = r.childList.size()-1; 
				return r.childList.get(index);
			}
			else return null;
		}

	}

	/**
	 * 
	 */
	public int getChildCount(Object parent) {
		if(parent.equals(this)){
			ArrayList<Root> rA = new ArrayList<Root>();
			for(int i = 0 ; i < rootList.size(); i++){
				Root r = (Root) rootList.get(i);
				if(r.isChild() == 0){rA.add(r);}
			}
			return rA.size();
		}

		else{
			Root r = (Root) parent;
			return r.childList.size();
		}

	}

	/**
	 * 
	 */
	public boolean isLeaf(Object node) {
		if(node.equals(this)) return false;
		else {
			Root r = (Root) node;
			return r.childList.size() == 0;
		}
	}

	/**
	 * 
	 */
	public void valueForPathChanged(TreePath path, Object newValue) {
		Root r = (Root) path.getLastPathComponent();
		r.setRootID(newValue.toString());
	}

	/**
	 * 
	 */
	public int getIndexOfChild(Object parent, Object child) {
		// TODO Auto-generated method stub
		return ( (Root)child ).isChild();
	}

	/**
	 * 
	 */
	public void addTreeModelListener(TreeModelListener l) { treeModelListeners.addElement(l);}

	/**
	 * 
	 */
	public void removeTreeModelListener(TreeModelListener l) {treeModelListeners.removeElement(l);}
	
	/**
	 * 
	 * @param r
	 */
    protected void fireTreeStructureChanged(Root r) {
        TreeModelEvent e = new TreeModelEvent(this,new Object[] {r});
        for (TreeModelListener tml : treeModelListeners) {tml.treeStructureChanged(e);}
    }
    
    
    
    
	/**
	 * 
	 */
    public String toString(){
    	return this.imgName;
    }
    
    
    /**
     * 
     */
    public void createGraphics() {
           
       // Convexhull
       convexhullGP.reset();
       PolygonRoi ch = getConvexHull();
       int[] xRoi = ch.getXCoordinates();
       int[] yRoi = ch.getYCoordinates();  
       Rectangle rect = ch.getBounds();		
       convexhullGP.moveTo(xRoi[0]+rect.x, yRoi[0]+rect.y);
       for(int k = 1; k < xRoi.length; k++){
     	 convexhullGP.lineTo(xRoi[k]+rect.x, yRoi[k]+rect.y);
       }      
  	 	convexhullGP.lineTo(xRoi[0]+rect.x, yRoi[0]+rect.y);

    }

    /**
     * Set the selection fo the entire root system
     * @param s
     */
    public void setSelected(boolean s){
    	selected = s;
    }
    
    /**
     * Is the entire root system selected
     * @param s
     */
    public boolean isSelected(){
    	return selected;
    }
    
    
    
    

	/**
	 * Methods used by SRWin
	 */
    
    public String displaySummary(){
    	String t = "Image size [cm x cm] = "+img.getWidth() * pixelSize+" x "+img.getHeight() * pixelSize+" \n"+
    		      "Root system convexhull area = "+getConvexHullArea()+" \n\n"+
    		      "------------------------------------------ \n"+
    		      "PRIMARY ROOTS \n"+
			      "------------------------------------------ \n"+
			      "# roots [-] = "+getNPRoot()+" \n"+
			      "# nodes [-] = "+getNPNode()+" \n"+
			      "Mean length [cm] = "+getAvgPRLength()+" \n"+
			      "Mean diameter [cm] = "+getAvgPRDiam()+" \n"+
			      "Mean child density [root / cm] = "+getAvgChildDens()+" \n \n"+
			      "SECONDARY ROOTS \n"+
			      "------------------------------------------ \n"+	      
			      "# roots [-] = "+getNSRoot()+" \n"+
			      "# nodes [-] = "+getNSNode()+" \n"+
			      "Mean length [cm] = "+getAvgSRLength()+" \n"+
			      "Mean diameter [cm] = "+getAvgSRDiam()+" \n"+
			      "Mean insertion angle [degree] = "+getAvgSRInsAng()+" \n"+
			      "Mean interbranch distance [cm] = "+getAvgInterBranch();
    	
    	return t;
    }
    
    public String displayRootInfo(Root r){
    	Root[] pRoot = new Root[1];
    	pRoot[0] = r;
    	return displayRootInfo(pRoot);
    }
    
    
    public String displayRootInfo(Root[] pRoot){
    	String text;
    	if(pRoot.length  == 1){
    		Root r = pRoot[0];
	    	text = "Root = "+r.getRootID()+" \n"	    			
					+"Root Key = "+r.rootKey+" \n"
					+"Root Ontology = "+SR.listPoNames[r.poIndex]+" \n"
				+"--------------------- \n"
				+"Order = "+r.isChild()+" \n"
				+"Parent root = "+r.getParentName()+" \n"
				+"--------------------- \n"
//    			+"Convexhull area [cm3] = "+r.getConvexHullArea()+" \n"
    			+"Network projected surface [cm3] = "+r.getRootNetworkSurface()+" \n"
//    			+"Compactness = "+r.getRootNetworkSurface() / r.getConvexHullArea()+" \n"
				+"--------------------- \n"
    			+"Length [cm] = "+r.lPosPixelsToCm(r.getRootLength())+" \n"
    			+"Projected Surface [cm2] = "+r.getRootProjectedSurface()+" \n"
    			+"Surface [cm2] = "+r.getRootSurface()+" \n"
    			+"Volume [cm3] = "+r.getRootVolume()+" \n"
    			+"Mean diameter [cm] = "+r.getAVGDiameter()+" \n"
    			+"Insertion angle [degree] = "+(r.getInsertAngl() * (180 / Math.PI))+" \n"
    			+"Insertion position [cm] = "+r.lPosPixelsToCm(r.getDistanceFromBase())+" \n"
				+"--------------------- \n"
	    		+"# of laterals = "+r.childList.size()+" \n"
    			+"Lateral density [root/cm] = "+r.getChildDensity()+" \n"
				+"Mean interbranch distance [cm] = "+r.getAVGInterBranchDistance()+" \n"
				+"--------------------- \n"
	    		+"# of marks = "+r.markList.size()+" \n";
    	}
    	else{
    		text = "Number of selected roots = "+pRoot.length+" \n" 
    			+"--------------------- \n" 
    			+"Total root length [cm] = "+getSelectedRootLength(pRoot)+" \n" 
				+"Total root surface [cm2] = "+getSelectedRootSurface(pRoot)+" \n" 
				+"Total root volume [cm3] = "+getSelectedRootVolume(pRoot)+" \n"
				+"--------------------- \n"
    			+"Mean root length [cm] = "+(getSelectedRootLength(pRoot) / pRoot.length)+" \n" 
    			+"Mean root diameter [cm] = "+(getSelectedRootDiameter(pRoot) / pRoot.length)+" \n" 
				+"Mean root surface [cm2] = "+(getSelectedRootSurface(pRoot) / pRoot.length)+" \n" 
				+"Mean root volume [cm3] = "+(getSelectedRootVolume(pRoot) / pRoot.length)+" \n";	    			
    	} 
    	return text;
    }
    public String displayMarkInfo(Mark m){
    	String text;

		text = "Mark type = "+m.type+" \n" 
			+"Mark value = "+m.value+" \n" 
			+"Mark position = "+(m.lPos*pixelSize)+" \n" ;	    			
    	
    	return text;
    }
    
    private float getSelectedRootLength(Root[] pRoot){
    	float l = 0;
    	for(int i = 0 ; i < pRoot.length ; i++){
    		l += pRoot[i].lPosPixelsToCm(pRoot[i].getRootLength());
    	}
    	return l;
    }
    
    private float getSelectedRootSurface(Root[] pRoot){
    	float s = 0;
    	for(int i = 0 ; i < pRoot.length ; i++){
    		s += pRoot[i].getRootSurface();
    	}
    	return s;
    }
    
    private float getSelectedRootVolume(Root[] pRoot){
    	float v = 0;
    	for(int i = 0 ; i < pRoot.length ; i++){
    		v += pRoot[i].getRootVolume();
    	}
    	return v;
    }
    
    private float getSelectedRootDiameter(Root[] pRoot){
    	float d = 0;
    	for(int i = 0 ; i < pRoot.length ; i++){
    		d += pRoot[i].getAVGDiameter();
    	}
    	return d;
    }
    
    public String getRootKey(){
    	return UUID.randomUUID().toString();
    }
    
    
    public void deleteSmallRoots(){
    	Root r;
		for(int i =0 ; i < rootList.size() ; i++){
			r =  (Root) rootList.get(i);
			r.setSelect(false);
		}
		for(int i =0 ; i < rootList.size() ; i++){
			r =  (Root) rootList.get(i);
			if(r.nNodes < 3){
				selectedRoot = r;
				deleteSelectedRoot();
			}
		}
		//SR.write("# of delete roots: "+d);
    }
 
    
    /**
     * Get the convexhull area of all the roots in the image
     * @return
     */
    public float getConvexHullArea(){
    	ImageProcessor maskProcessor = getConvexHull().getMask(); // Get the convex hull from the object stored in the ROI Manager			
    	ImagePlus mask = new ImagePlus();
    	maskProcessor.invert();
    	Calibration cal = new Calibration();
    	cal.setUnit("cm");
    	cal.pixelHeight = pixelSize;
    	cal.pixelWidth = pixelSize;
    	mask.setProcessor(maskProcessor);
    	mask.setCalibration(cal);
    	SR.write("height = "+cal.pixelHeight);
    	
    	ResultsTable rt = new ResultsTable();
    	ParticleAnalyzer pa = new ParticleAnalyzer(ParticleAnalyzer.CLEAR_WORKSHEET, Measurements.AREA , rt, 0, 10e9);
    	pa.analyze(mask);
    	return(float) rt.getValue("Area", 0);
    }
    /**
     * Get the convexhull of all the roots in the image
     * @return
     */
    public PolygonRoi getConvexHull(){
    		
    	List<Integer> xList = new ArrayList<Integer>(); 
    	List<Integer> yList = new ArrayList<Integer>();
    	
    	// Add all the nodes coordinates
    	for(int i = 0; i < rootList.size(); i++){
    		Root r = rootList.get(i);
    		Node n = r.firstNode;
    		while (n.child != null){
    			xList.add((int) n.x);
    			yList.add((int) n.y);
    			n = n.child;
    		}
    		xList.add((int) n.x);
    		yList.add((int) n.y);
    	}
    	
    	int[] xRoiNew = new int[xList.size()];
    	int[] yRoiNew = new int[yList.size()];
    	for(int l = 0; l < yList.size(); l++){
    		xRoiNew[l] = xList.get(l);
    		yRoiNew[l] = yList.get(l);
    	}
    	
    	Roi roi = new PolygonRoi(xRoiNew, yRoiNew, yRoiNew.length, Roi.POLYGON);
    	return new PolygonRoi(roi.getConvexHull(),  Roi.POLYGON);
    }

    
    
    public void cropSelectedRoot(){
    	selectedRoot.cropRoot(this);
    }
    
    
    public void cropTracing(){

    	
    	Root r;
    	for(int k = 0; k < 3; k++){
			for(int i =0 ; i < rootList.size() ; i++){
				r =  (Root) rootList.get(i);
				if(r.isChild() == k) r.cropRoot(this);
			}    	
    	}
		//appendTracing();
    }
    
    public void reCenterAllNodes(){
    	Root r;
    	Node n;
	    fit.checkImageProcessor();
	    
		for(int i =0 ; i < rootList.size() ; i++){
			r =  (Root) rootList.get(i);
			n = r.firstNode;
			while(n.child != null){
				fit.reCenter(n, 0.05f, 0.5f, true, 1);
				n = n.child;
			}
		}    	
		//appendTracing();
    }
    
    
    public void cropTracing2(){
    	Root r;
    	Node n, n1, n2;
	    fit.checkImageProcessor();
		for(int i =0 ; i < rootList.size() ; i++){
			r =  (Root) rootList.get(i);
			n = r.firstNode;
			n2 = n;
			while(n.child != null){
				n1 = n;
				n = n.child;
				if(fit.getValue(n1.x, n1.y) > autoThreshold) r.rmNode(n1);
				IJ.log(fit.getValue(n1.x, n1.y)+ " / "+autoThreshold);
			}
			if(fit.getValue(n2.x, n2.y) > autoThreshold) r.rmNode(n2);
			n = r.lastNode;
			if(fit.getValue(n.x, n.y) > autoThreshold) r.rmNode(n);

		}    	
		//appendTracing();
    }
    
    public void appendTracing(){
    	Root r;
    	Node n, n1;
	    fit.checkImageProcessor();
		for(int i =0 ; i < rootList.size() ; i++){
			r =  (Root) rootList.get(i);
			n = r.lastNode;
			n1 = n.parent;
	    	fit.suggest(n1, n);
		}     	
    }
    
    
    /**
     * Get the center of the tracing
     * @return
     */
    public float[] getCenter(){
    	float[] coord = new float[2];
    	
    	// Get x
    	float min = 1e5f, max = 0; 
    	Root r;
    	Node n;
    	for(int i = 0; i < rootList.size(); i++){
    		r = rootList.get(i);
			n = r.firstNode;
			while(n.child != null){
				if(n.x < min) min = n.x;
				if(n.x > max) max = n.x;
				n = n.child;
			}
    	}
    	coord[0] = min + ((max-min)/2);
    	
    	
    	// Get y
    	min = 1e5f;
    	max = 0; 
    	for(int i = 0; i < rootList.size(); i++){
    		r = rootList.get(i);
			n = r.firstNode;
			while(n.child != null){
				if(n.y < min) min = n.y;
				if(n.y > max) max = n.y;
				n = n.child;
			}
    	}
    	coord[1] = min + ((max-min)/2);

    	
    	
    	return coord;
    }
    
    
    /**
     * Get the widht of the tracing
     * @return
     */
    public int getWidth(){
    	float min = 1e5f, max = 0; 
    	Root r;
    	Node n;
    	for(int i = 0; i < rootList.size(); i++){
    		r = rootList.get(i);
			n = r.firstNode;
			while(n.child != null){
				if(n.x < min) min = n.x;
				if(n.x > max) max = n.x;
				n = n.child;
			}
    	}
    	return (int)(max+min);
    }
    
    /**
     * Get the height of the tracing
     * @return
     */
    public int getHeight(){
    	float min = 1e5f, max = 0; 
    	Root r;
    	Node n;
    	for(int i = 0; i < rootList.size(); i++){
    		r = rootList.get(i);
			n = r.firstNode;
			while(n.child != null){
				if(n.y < min) min = n.y;
				if(n.y > max) max = n.y;
				n = n.child;
			}
    	}
    	return (int)(max+min);
    }
    
    /**
     * Export tracing to an image file
     * @param path
     * @param color
     * @param type
     * @param line
     */
    public void exportImage(String path, boolean color, String type, int line, boolean split){
    	exportImage(path, color, type, line, null, false, split);
    }
    
    /**
     * Create the ImagePlus object for the root tracing
     * @param color
     * @param name
     * @return
     */
    public ImagePlus createImagePlus(boolean color, String name, int w, int h){
    	ImagePlus tracing;
    	
    	if(name != null){
    		if(w < 0 || h < 0 ||w > 6000 || h > 6000){
    			SR.log("Image too back to be printed");
    			return null;
    		}
    		if(color) tracing = IJ.createImage("tracing", "RGB", w, h, 1);
    		else tracing = IJ.createImage("tracing", "8-bit", w, h, 1);
    	}
    	
    	else{
    		if(color) tracing = IJ.createImage("tracing", "RGB", ip.getWidth(), ip.getHeight(), 1);
    		else tracing = IJ.createImage("tracing", "8-bit", ip.getWidth(), ip.getHeight(), 1);
    	}
    	return tracing;
    }
    
    /**
     * Export tracing to an image file. One image / primary root
     * @param path
     * @param color
     * @param type
     * @param line
     * @param name
     * @param real
     */
    public void exportImage(String path, boolean color, String type, int line, String name, boolean real, boolean split){
    	
    	Root r;
    	Node n, n1;
    	int w, h;
    	ImagePlus tracingImage = new ImagePlus();
    	ImageProcessor tracingP = tracingImage.getProcessor();
  	    
    	SR.write(getWidth()+" / "+getHeight());
    	if(!split){
        	if(name != null){
//        		if(getWidth() < 0 || getHeight() < 0 || getWidth() > 6000 || getHeight() > 6000){
//        			SR.write("Image too large to be printed");
//        			return;
//        		}
        		if(color) tracingImage = IJ.createImage("tracing", "RGB", getWidth(), getHeight(), 1);
        		else tracingImage = IJ.createImage("tracing", "8-bit", getWidth(), getHeight(), 1);
        	}
        	
        	else{
        		if(color) tracingImage = IJ.createImage("tracing", "RGB", ip.getWidth(), ip.getHeight(), 1);
        		else tracingImage = IJ.createImage("tracing", "8-bit", ip.getWidth(), ip.getHeight(), 1);
        	}  
    	}
	    if(name == null) fit.checkImageProcessor();
    	for(int i = 0; i < rootList.size(); i++){
			r =  (Root) rootList.get(i);
			if(r.isChild() == 0){
				
				if(split){
					w =(int) (r.getXMaxTotal() - r.getXMinTotal());
					h =(int) (r.getYMaxTotal() - r.getYMinTotal());
		    		tracingImage = createImagePlus(color, name, w+100, h+100);
				}
	    		tracingP = tracingImage.getProcessor();  
				
				n = r.firstNode;		
				if(color) tracingImage.setColor(Color.orange);
				else tracingImage.setColor(Color.black);
				
				while(n.child != null){
					n1 = n;
					n = n.child;
					if(real) tracingP.setLineWidth((int) n.diameter);
					else tracingP.setLineWidth(line);
//			    	tracingP.drawLine((int) (n1.x - (int) r.getXMinTotal())+50, (int) (n1.y - (int) r.getYMinTotal())+50, (int) (n.x - (int) r.getXMinTotal())+50, (int) (n.y - (int) r.getYMinTotal())+50);
			    	tracingP.drawLine((int) n1.x, (int) n1.y , (int) n.x, (int) n.y);
				}
				
				for(int j = 0; j < r.childList.size(); j++){
					Root rr =  (Root) r.childList.get(j);
					n = rr.firstNode;		
					if(color) tracingImage.setColor(Color.green);
					else tracingImage.setColor(Color.black);
					
					while(n.child != null){
						n1 = n;
						n = n.child;
						if(real) tracingP.setLineWidth((int) n.diameter);
						else tracingP.setLineWidth(line);
//				    	tracingP.drawLine((int) (n1.x - (int) r.getXMinTotal())+50, (int) (n1.y - (int) r.getYMinTotal())+50, (int) (n.x - (int) r.getXMinTotal())+50, (int) (n.y - (int) r.getYMinTotal())+50);
				    	tracingP.drawLine((int) n1.x, (int) n1.y , (int) n.x, (int) n.y);
					}
				}
				if(split){
					String p = path;
			    	if(name != null){
					    String[] exp = processImageName(name, ".tiff");
			    		if(!p.endsWith("/")) p = p.concat("/");
			    		p = p.concat(r.getRootKey()+"_das"+exp[4]+"."+type);
			    	}
	//		    	if(!p.endsWith(type)) p = p.substring(0, p.lastIndexOf(".")).concat("."+type);
			    	IJ.save(tracingImage, p);
			    	if( name != null)IJ.log("Image export done");	
				}
			}
    	}
    	if(!split){
			String p = path;
	    	if(name != null){
	    		if(!p.endsWith("/")) p = p.concat("/");
	    		p = p.concat(name+"."+type);
	    	}
	    	if(!p.endsWith(type)) p = p.substring(0, p.lastIndexOf(".")).concat("."+type);
	    	IJ.save(tracingImage, p);
	    	if( name != null)IJ.log("Image export done");	
    	}
    }
    
    
    
    public void exportImageOld(String path, boolean color, String type, int line, String name, boolean real){
    	
    	Root r;
    	Node n, n1;
    	ImagePlus tracing;

    	if(name != null){
    		if(getWidth() < 0 || getHeight() < 0 || getWidth() > 6000 || getHeight() > 6000){
    			SR.log("Image too back to be printed");
    			return;
    		}
    		if(color) tracing = IJ.createImage("tracing", "RGB", getWidth(), getHeight(), 1);
    		else tracing = IJ.createImage("tracing", "8-bit", getWidth(), getHeight(), 1);
    	}
    	
    	else{
    		if(color) tracing = IJ.createImage("tracing", "RGB", ip.getWidth(), ip.getHeight(), 1);
    		else tracing = IJ.createImage("tracing", "8-bit", ip.getWidth(), ip.getHeight(), 1);
    	}
        	
    	ImageProcessor tracingP = tracing.getProcessor();    	
    	
	    if(name == null) fit.checkImageProcessor();
    	for(int i = 0; i < rootList.size(); i++){
			r =  (Root) rootList.get(i);
			n = r.firstNode;
			
			if(color){
				switch(r.isChild()){
					case 0: tracing.setColor(Color.orange); break;
					case 1: tracing.setColor(Color.green); break;
					case 2: tracing.setColor(Color.yellow); break;
				}
			}
			else tracing.setColor(Color.black);
			
			while(n.child != null){
				n1 = n;
				n = n.child;
				if(real) tracingP.setLineWidth((int) n.diameter);
				else tracingP.setLineWidth(line);
		    	tracingP.drawLine((int) n1.x, (int) n1.y, (int) n.x, (int) n.y);
			}
    	}
    	if(name != null){
    		if(!path.endsWith("/")) path = path.concat("/");
    		path = path.concat(name);
    	}
    	if(!path.endsWith(type)) path = path.substring(0, path.lastIndexOf(".")).concat("."+type);
    	IJ.save(tracing, path);
    	if( name != null)IJ.log("Image export done");
    }
    
    
    /**
     * For the export of the Plant Physiology lab's pictures
     * The image name has to be: EXPxx_GENxx_TRxx_BOXxx_DASxx
     * @return the box informations
     */
    private String[] processImageName(String t, String ext){
    	String[] exp = new String[5];
    	exp[0] = extractNameVar(t, "EXP", "_");
    	exp[1] = extractNameVar(t, "GEN", "_");
    	exp[2] = extractNameVar(t, "TR", "_");
    	exp[3] = extractNameVar(t, "BOX", "_");
    	exp[4] = extractNameVar(t, "DAS", "_");
    	
    	return exp;
    }
    
    // Extract string from a string
    private String extractNameVar(String t, String var, String sep){
		String tmp = t.substring(t.indexOf(var) + var.length(), t.length());
    	if(tmp.indexOf(sep) > -1) return tmp.substring(0, tmp.indexOf(sep));
    	else return tmp.substring(0, tmp.indexOf("."));
    }
    
    /**
     * Reset the name of the lateral roots with their position (in pixels) on their parent root.
     */
    public void setLateralRootID() {

    	for(int i = 0 ; i < rootList.size(); i++){
    		Root r = (Root) rootList.get(i);
    		if(r.isChild() > 0) r.setRootID(""+Math.round(r.getDistanceFromBase()));
    	}
    	
    	for(int i = 0 ; i < rootList.size(); i++){
    		Root r = (Root) rootList.get(i);
    		r.updateRoot();
    	}
    } 
    /**
     * 
     * @param ID
     */
    public void setSelectedRootKey(String key) {
       selectedRoot.setRootKey(key);
       }
    
    
    /**
     * Perso option to get the plant id in the mars database
     * @param r
     * @param exp
     * @return
     */
    public int getPlantID(Root r, String[] exp){
    	
    	int plant = 0;
    	if(r.isChild() == 0) plant = Integer.valueOf(r.getRootID());
    	else{
    		Root rP = r.getParent();
    		while(rP.isChild != 0) rP = rP.getParent();
    		plant = Integer.valueOf(rP.getRootID());
    	}
    	if(plant > 5) plant = plant-5;
    		
    	try{
    		Class.forName("com.mysql.jdbc.Driver").newInstance();
    		java.sql.Connection conn = java.sql.DriverManager.getConnection("jdbc:mysql://localhost/mars?user=root&password=");
    		java.sql.Statement stmt=conn.createStatement();
    		java.sql.ResultSet rs=stmt.executeQuery("SELECT plant_id FROM mars.plant "+
    				"WHERE experiment_id = "+exp[0]+" AND stock_id ="+exp[1]+" AND treatment_id ="+exp[2]
    						+" AND pot ="+exp[3]+" AND plant ="+plant);
    		rs.first();
    		return rs.getInt(1);	 	  		 
    	} 
    	catch(Exception e){
    		SR.log("getPlantID failed: "+e);
    	}
    	return 0;
    }
    
    /**
     * Rename the roots based on their position on  the image (from left to right)
     */
    public void renamePrimaryRootsLeftRight(){
    	
    	class compareRoot implements Comparator<Root> {
    	    public int compare(Root r1, Root r2) {
    	    	if(r1.firstNode.x < r2.firstNode.x) return -1;
    	    	else return 1;
    	    }
    	}
    	
    	ArrayList<Root> rl = new ArrayList<Root>();
    	for(int i = 0 ; i < rootList.size(); i++){
    		Root r = (Root) rootList.get(i);
    		if(r.isChild() == 0) rl.add(r);
    	}
    	
		Collections.sort(rl, new compareRoot());
		
//		int j = 0;
    	for(int i = 0; i < rl.size(); i++){
//    		rl.get(i).setRootID(rl.get(i).getRootID().substring(5));
    		rl.get(i).setRootID(""+(i+1));
//    		j++;
    	}
    }   
    
    public int getIndexFromPo(String po){
    	for(int i = 0; i < SR.listPo.length; i++){
    		if(po.equals(SR.listPo[i])) return i;
    	}
    	return 0;
    }
}

