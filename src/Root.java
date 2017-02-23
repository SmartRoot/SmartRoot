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
//import ij.*;
//import ij.process.*;
//import ij.gui.*;
//import ij.io.*;
//import ij.plugin.frame.*;
//import ij.measure.*;
import ij.IJ;
import ij.ImagePlus;
import ij.gui.PolygonRoi;
import ij.gui.Roi;
import ij.measure.Calibration;
import ij.measure.Measurements;
import ij.measure.ResultsTable;
import ij.plugin.filter.ParticleAnalyzer;
import ij.process.ImageProcessor;

import java.awt.*;
//import java.awt.event.*;
import java.awt.geom.*;
//import java.awt.image.*;
//import java.awt.font.*;
//import java.awt.datatransfer.*;
//import java.sql.*;
//import javax.swing.*;
//import javax.swing.filechooser.*;
//import javax.swing.event.*;
//import javax.swing.table.*;
import java.util.*;
import java.util.List;
//XML file support
//import javax.xml.parsers.DocumentBuilder; 
//import javax.xml.parsers.DocumentBuilderFactory;  
//import javax.xml.parsers.FactoryConfigurationError;  
//import javax.xml.parsers.ParserConfigurationException;
//import org.xml.sax.SAXException;  
//import org.xml.sax.SAXParseException;
import java.io.*;
import java.util.UUID;

import javax.swing.JOptionPane;

//import sun.security.provider.SystemSigner;

//import org.w3c.dom.NamedNodeMap;
//import org.w3c.dom.NodeList;
  
/**
 * 
 * Root class. Used to create individual roots, themselves composed of individual nodes 
 * 
 */

// %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

class Root{
   Node firstNode, lastNode;
   int nNodes;
   public ArrayList<Root> childList = new ArrayList<Root>();
   
   String rootKey = "";
   String parentKey = null;
   int poIndex = 1;
   
   Node parentNode;
   Root parent;
   Root firstChild;
   Root lastChild;
   float distanceFromApex = 0;
   float distanceFromBase = 0;
   float childDensity = 0;
   float insertAngl = 0;
   float interBranch = 0;
   int isChild = 0;
   String parentName;
   int gM = 0;
   
   ArrayList<Tick> tickList;
   ArrayList<Root> deletedRoot, keepRoot;
   public Vector<Mark> markList;
   public Mark anchor, MDL;
   float rulerAtOrigin = 0.0f;  // in pixel units
   private String rootID = "";
   public float dpi, pixelSize;
   private boolean needsRefresh = false, select = false, displayConvexHull = true;
   
   // Rendering parameters
   public GeneralPath bordersGP = new GeneralPath();
   public GeneralPath axisGP = new GeneralPath();
   public GeneralPath nodesGP = new GeneralPath();
   public GeneralPath ticksGP = new GeneralPath();
   public GeneralPath tipsGP = new GeneralPath();
   public GeneralPath convexhullGP = new GeneralPath();
   private Line2D.Float rulerLine = new Line2D.Float();
   public PolygonRoi convexHull;
   public GeneralPath parallelsGP = new GeneralPath();
   static String noName = SR.prefs.get("root_ID", "root_");
   static AlphaComposite ac1 = AlphaComposite.getInstance(AlphaComposite.SRC_OVER,1.0f);
   static AlphaComposite ac2 = AlphaComposite.getInstance(AlphaComposite.SRC_OVER,0.25f);
   static AlphaComposite ac3 = AlphaComposite.getInstance(AlphaComposite.SRC_OVER,0.60f);
   static AlphaComposite ac4 = AlphaComposite.getInstance(AlphaComposite.SRC_OVER,0f);
   static Color axisColor = Color.green;
   static Color nodesColor = Color.orange;
   static Color bordersColor = Color.red;
   static Color areaColor = Color.yellow;
   static Color ticksColor = Color.yellow;
   static Color tipsColor = Color.yellow;
   static Color childTipsColor = Color.green;
   static Color childNodesColor = Color.green;
   static Color childAxisColor = Color.yellow;
   static Color childBordersColor = Color.orange;
   
   static public final int NUM_ATTRIBUTES = 7;
   static public final int ATTR_DIAMETER = 0;
   static public final int ATTR_X = 1;
   static public final int ATTR_Y = 2;
   static public final int ATTR_DIR_X = 3;
   static public final int ATTR_DIR_Y = 4;
   static public final int ATTR_ANGLE = 5;
   static public final int ATTR_LPOS = 6;
   static public final int Y_SEQUENCE = 1;
   
   static int nextRootKey = 1;

   /**
    * Constructor
    */
   public Root(){
   }

   /**
    * Constructor
    * @param dpi
    * @param parentDOM
    */
   public Root(float dpi, org.w3c.dom.Node parentDOM) {
	   new Root(dpi, parentDOM, false, null, null, null);
   }
   /**
    * Used when opening a xml file
    * @param dpi
    */
   public Root(float dpi, org.w3c.dom.Node parentDOM, boolean common, Root parentRoot, RootModel rm, String origin) {
	   new Root(dpi, parentDOM, common, parentRoot, rm, origin, 1);
   }

   /**
    * Used when opening a xml file
    * @param dpi
    */
   public Root(float dpi, org.w3c.dom.Node parentDOM, boolean common, Root parentRoot, RootModel rm, String origin, float scale) {
	  this.dpi = dpi;
      pixelSize = ((float) 2.54 / dpi);
      nNodes = 0;
      firstNode = lastNode = null;
      //SR.write("new root");
      tickList = new ArrayList<Tick>();
      markList = new Vector<Mark>();
      rootID = noName;
      nextRootKey++;     

      if(!common){
    	  read(parentDOM);
    	  if(validate()) rm.rootList.add(this);
      }
      else readRSML(parentDOM, rm, parentRoot, origin, scale);
      
    }
   
   /**
    * Constructor
    * @param dpi
    */
   public Root(float dpi, String rK, RootModel rm) {
      this.dpi = dpi;
      pixelSize = ((float) 2.54 / dpi);
      nNodes = 0;
      firstNode = lastNode = null;
      tickList = new ArrayList<Tick>();
      markList = new Vector<Mark>();
      rootID = noName;
      rootKey = rK;
      poIndex = 1;
      }
   
   /**
    * Constructor
    * @param dpi
    * @param r
    */
   public Root(float dpi, Root r, String rK) {
      this.dpi = dpi;
      pixelSize = ((float) 2.54 / dpi);
      nNodes = 0;
      firstNode = lastNode = null;
      tickList = new ArrayList<Tick>();
      markList = new Vector<Mark>();
      rootID = noName;
      isChild = r.isChild + 1;
      parent = r;
      rootKey = rK;
      poIndex = 2;  
      }

   /**
    * Add a node to the existing root
    * @param x
    * @param y
    * @param addToBase
    * @return
    */
   public Node addNode(float x, float y, boolean addToBase) {
      needsRefresh();
      if (!addToBase) {
         lastNode = new Node(x, y, lastNode, true);
         if (nNodes == 0) firstNode = lastNode;
         nNodes++;
         return lastNode;
         }
      else {
         firstNode = new Node(x, y, firstNode, false);
         if (nNodes == 0) lastNode = firstNode;
         nNodes++;
         return firstNode; // user must reactualize cLength
         }
      }
  
   /**
    * Add a mark to the existing root
    * @param type
    * @param value
    * @param markerPosition
    * @return
    */
   public Mark addMark(int type, String value, float markerPosition) {
      float lPos = lPosCmToPixels(markerPosition);
      if (type == Mark.ANCHOR) {
         float v = Float.parseFloat(value);
         if (v != 0.0f) {   // reset rulerOrigin relative to the mark;
            setRulerAtOrigin(-(lPos - v / pixelSize));
            }
         else value = String.valueOf(Math.round(markerPosition * 100.0) / 100.0);
         anchor = new Mark(type, this, lPos, value);
         return null;
         }
      else if (type == Mark.MDL){
    	  for (int i = 0 ;  i< markList.size(); i++){
    		  Mark ma= (Mark) markList.get(i);
    		  if (ma.type == type && !ma.isForeign) markList.remove(i);
    	  }
          Mark m = new Mark(type, this, lPos, value);
          markList.add(m);
          MDL = m;
          return m;
      }
      else {
         Mark m = new Mark(type, this, lPos, value);
         markList.add(m);
         // m.createGraphics();
         return m;
         }
      }

   /**
    * Add a mark to the root
    * @param m
    */
   public void addMark(Mark m) {
      if (m.type == Mark.ANCHOR) {
         anchor = m;
         }
      else markList.add(m);
      }

   /**
    * Append two roots togethers
    * @param r
    */
   public void append(Root r) {
      Node ln = lastNode;
      lastNode.child = r.firstNode;
      r.firstNode.parent = lastNode;
      lastNode = r.lastNode;
      nNodes += r.nNodes;
      ln.length = ln.getDistanceTo(ln.child);
      ln.theta = NodeFitter.vectToTheta(ln.child.x - ln.x, ln.child.y - ln.y);
      ln.calcCLength(ln.cLength);
      ln.calcBorders();
      needsRefresh();
      }

   /**
    * Find the apex of the root
    */
   public void autoFindApex() {
      // Should return directly if the root base is connected to another root
      if (firstNode.diameter < lastNode.diameter) reverse();
      }

   /**
    * Compute the number of nodes in the root
    */
   public void calcNNodes() {
      if (firstNode == null) {
         nNodes = 0;
         return;
         }
      nNodes = 1;
      for (Node n = firstNode; (n = n.child) != null; nNodes++);
      }

   /**
    * Comptue the ticks positions along the root
    */
   public void calcTicks() {
      // The following parameters may be stored in a Prefs table of the RootModel.
      float minorStep = (float) (dpi / 25.4);
      float minorSize = 3.0f;
      float medianSize = 7.0f;
      float majorSize = 7.0f;
      int nStepBwMedian = 5;
      int nStepBwMajor = 10;
      
      ticksGP.reset();

      Node n = firstNode;
      float tickSize = 0, curLen = 0;
      tickList.clear();
      if (n == null) return;
      float offset = minorStep - 
                     ((float) Math.ceil(rulerAtOrigin / minorStep) * minorStep -
                       rulerAtOrigin);
      int stepCount = (int) Math.ceil(rulerAtOrigin / minorStep);
      float totLen = (stepCount - 1) * minorStep;
      
      Tick t = new Tick();
      while (n != null && n.child != null) {
         float dx = (n.child.x - n.x) / n.length;
         float dy = (n.child.y - n.y) / n.length;
         curLen = minorStep - offset;
         offset = 0;

         while (curLen <= n.length) {

            t.x = n.x + curLen * dx;
            t.y = n.y + curLen * dy;

            if ((stepCount % nStepBwMajor) == 0) {
               tickSize = majorSize;
               t.b2x = t.x - tickSize * dy;
               t.b2y = t.y + tickSize * dx;
               t.hasLabel = true;
               }
            else if ((stepCount % nStepBwMedian) == 0) {
               t.b2x = t.x;
               t.b2y = t.y;
               tickSize = medianSize;
               }
            else {
               t.b2x = t.x;
               t.b2y = t.y;
               tickSize = minorSize;
               }
            t.b1x = t.x + tickSize * dy;
            t.b1y = t.y - tickSize * dx;

            ticksGP.moveTo(t.b1x, t.b1y);
            ticksGP.lineTo(t.b2x, t.b2y);

            stepCount++;
            totLen += minorStep;
            t.length = Math.round(totLen * pixelSize);
            curLen += minorStep;

            if (t.hasLabel) {
               t.lblOffsetX = (t.b1x >= t.b2x) ? -15 : 5;
               t.lblOffsetY = (t.b1y >= t.b2y) ? -3 : 10;
               tickList.add(t);
               t = new Tick();
               }
            }
         offset = n.length - (curLen - minorStep);
         n = n.child;
         }
      }

   /**
    * Change a mark value
    * @param m the mark
    * @param v the new value
    */
   public void changeMarkValue(Mark m, String v) {
      if (m == anchor) {
         rulerAtOrigin = - (anchor.lPos - Float.parseFloat(v) / pixelSize);
         needsRefresh();
         }
      m.value = v;
      m.needsRefresh();
      }

   /**
    * Get the node position
    * @param n
    * @return
    */
   public int getNodePositionRelativeToRulerOrigin(Node n) {
      Node prev = (n.parent == null) ? n : n.parent;
      Node next = (n.child == null) ? n : n.child;
      if (next.cLength + rulerAtOrigin < 0.0f) return -1;
      else if (prev.cLength + rulerAtOrigin > 0.0f) return 1;
      else return 0;
      }

   /**
    * Clear all the root data
    */
   public void clear() {
      Node n = firstNode;
      while (n != null) {
         Node n1 = n.child;
         n.child = null;
         n.parent = null;
         n = n1;      
         }
      firstNode = null;
      lastNode = null;
      nNodes = 0;
      tickList.clear();
      rulerAtOrigin = 0.0f;
      rootID = noName;
      needsRefresh();
      }


   /**
    * Does the root contain the x/y position?
    * @param x
    * @param y
    * @return
    */
   public boolean contains(float x, float y) {
	   createGraphics();
      return bordersGP.contains(x, y);
      }
   
   /**
    * Create the root graphics. For display purpose
    */
   public void createGraphics() {
      calcTicks();
      Arc2D arc = new Arc2D.Float(Arc2D.OPEN);
      
      axisGP.reset();
      Node n = firstNode;
      if (n != null) {
         axisGP.moveTo(n.x, n.y);
         while ((n = n.child) != null) axisGP.lineTo(n.x, n.y);
         }
      
      nodesGP.reset();
      n = firstNode;
      while (n != null) {
         float r = n.diameter / 2.0f;
         if (n.child != null) {
            nodesGP.moveTo(n.x + 1.4142f * r * (n.child.x - n.x) / n.length,
                           n.y + 1.4142f * r * (n.child.y - n.y) / n.length);
            arc.setArcByCenter(n.x, n.y, r, n.theta * 180.0f / Math.PI + 45.0f, 270.0f, Arc2D.OPEN);
            nodesGP.append(arc, true);
            nodesGP.closePath();
            }
         else {
            arc.setArcByCenter(n.x, n.y, r, 0.0f, 360.0f, Arc2D.OPEN);
            nodesGP.append(arc, false);
            nodesGP.closePath();
            }
         n.calcPoles(0);
         nodesGP.moveTo(n.bx[4], n.by[4]);
         nodesGP.lineTo(n.bx[5], n.by[5]);
         n = n.child;
         }

      tipsGP.reset();
      if (lastNode != null) {
         arc.setArcByCenter(lastNode.x, lastNode.y, lastNode.diameter / 2.0f, 0.0f, 360.0f, Arc2D.OPEN);
         tipsGP.append(arc, false);
         }
      
      // Convexhull
      convexhullGP.reset();
      PolygonRoi ch = getConvexHull();
      if(ch != null){
	      int[] xRoi = ch.getXCoordinates();
	      int[] yRoi = ch.getYCoordinates();  
	      Rectangle rect = ch.getBounds();		
	      convexhullGP.moveTo(xRoi[0]+rect.x, yRoi[0]+rect.y);
	      for(int k = 1; k < xRoi.length; k++){
	    	 convexhullGP.lineTo(xRoi[k]+rect.x, yRoi[k]+rect.y);
	      }      
	 	 convexhullGP.lineTo(xRoi[0]+rect.x, yRoi[0]+rect.y);
      }
      
      
      bordersGP.reset();
      n = firstNode;
      if (n != null) {
         bordersGP.moveTo(n.bx[1], n.by[1]);
         
         while (n.child != null) {
            n = n.child;
            if (n.bCross01) bordersGP.lineTo(n.bx[4], n.by[4]);
            else {
               if (n.child == null) bordersGP.lineTo(n.bx[0], n.by[0]);
               else {
                  arc.setFrame(n.x - n.diameter / 2.0, n.y - n.diameter / 2.0, n.diameter, n.diameter);
                  arc.setAngles(n.bx[0], n.by[0], n.bx[1], n.by[1]);
                  bordersGP.append(arc, true);
                  }
               }
            }
         
         arc.setFrame(n.x - n.diameter / 2.0, n.y - n.diameter / 2.0, n.diameter, n.diameter);
         arc.setAngles(n.bx[0], n.by[0], n.bx[2], n.by[2]);
         bordersGP.append(arc, true);
         
         while (n.parent != null) {
            n = n.parent;
            if (n.bCross23) bordersGP.lineTo(n.bx[5], n.by[5]);
            else {
               if (n.parent == null) bordersGP.lineTo(n.bx[3], n.by[3]);
               else {
                  arc.setFrame(n.x - n.diameter / 2.0, n.y - n.diameter / 2.0, n.diameter, n.diameter);
                  arc.setAngles(n.bx[3], n.by[3], n.bx[2], n.by[2]);
                  bordersGP.append(arc, true);
                  }
               }
            }
         
         arc.setFrame(n.x - n.diameter / 2.0, n.y - n.diameter / 2.0, n.diameter, n.diameter);
         arc.setAngles(n.bx[5], n.by[5], n.bx[1], n.by[1]);
         bordersGP.append(arc, true);
         bordersGP.closePath();
         }  
      needsRefresh = false;
      }


   /**
    * Get the location of a longitudinal position along the root
    * @param lp position along the root. Is in cm
    * @return
    */
   public Point getLocation(float lp) {
      lp = lp / pixelSize - rulerAtOrigin;
      Node n = firstNode;
      while (n != lastNode && n.child.cLength < lp) n = n.child;
      if (n == lastNode) return (Point) null;
      float r = (lp - n.cLength) / n.length;
      return new Point((int) (n.x + r * (n.child.x - n.x)),
                       (int) (n.y + r * (n.child.y - n.y)));
      }

   /**
    *  Computes and returns geometrical features of the root at a position specified
    *  by a segment and a relative offset in the segment
    * @param lp
    * @return
    */
   public float[] getAttributesAtLPosCm(float lp) {
      return getAttributesAtLPosPixels(lp / pixelSize - rulerAtOrigin);
      }

   /**
    *  Computes and returns geometrical features of the root at a position specified
    *  by a segment and a relative offset in the segment
    * @param lp
    * @return
    */
   public float[] getAttributesAtLPosPixels(float lp) {
      Node n = firstNode;
      while (n != lastNode && n.child.cLength < (lp - 0.001f)) n = n.child;
      if (n == lastNode) return new float[NUM_ATTRIBUTES]; // if (lp > rootLength) 
      float offset = (lp - n.cLength) / n.length;
      return getAttributesInSegment(n, offset);
      }
   
   /** Computes and returns geometrical features of the root at a position specified
    *  by a segment and a relative offset in the segment
   /** @param n The base node of the segment in which the selected point resides
   /** @param offset The relative offset [0,1] of the selected point in this segment
   /** @return A float[] of size NUM_ATTRIBUTES with the attributes at the selected point
    */
   public float[] getAttributesInSegment(Node n, float offset) {
      if (n == lastNode) return null;
      float[] attr = new float[NUM_ATTRIBUTES];
      attr[ATTR_DIAMETER] = (1.0f - offset) * n.diameter + offset * n.child.diameter;
      attr[ATTR_X] = n.x + offset * (n.child.x - n.x);
      attr[ATTR_Y] = n.y + offset * (n.child.y - n.y);
      attr[ATTR_DIR_X] = (n.child.x - n.x) / n.length;
      attr[ATTR_DIR_Y] = (n.child.y - n.y) / n.length;
      attr[ATTR_ANGLE] = NodeFitter.vectToTheta(attr[ATTR_DIR_X], attr[ATTR_DIR_Y]);
      attr[ATTR_LPOS] = n.cLength + offset * n.length; 
      return attr;
   }
   
   /**
    * 
    * @param sequence
    * @return
    */
   @SuppressWarnings("rawtypes")
   public Iterator getAttributesIterator(int sequence) {
      if (sequence == Y_SEQUENCE)
         return new Iterator() {
            Node n = Root.this.firstNode; // base node of the current segment
            double offset = 0.0; // offset [0,1] of the current position in the current segment
            double zeroY = 0.0; // Y of the root basis 
            {
               n = firstNode;
               while (n != lastNode && n.child.cLength < -rulerAtOrigin) n = n.child;
               if (n == lastNode) n = null;
               else {
                  offset = (-rulerAtOrigin - n.cLength) / n.length;
                  zeroY = (1 - offset) * n.y + offset * n.child.y;
                  prepareNext();
               }
            }
            
            public boolean hasNext() {
               return (n != null);
            }

            public Object next() {
               if (n == null) return null;
               float[] a = getAttributesInSegment(n, (float)offset);
               prepareNext();
               return a;
            }
            
            public void prepareNext() {
               // y of the last location returned (should be a mathematical integer)
               double y = Math.rint(((1.0 - offset) * n.y + offset * n.child.y - zeroY) * pixelSize);
               // skip nodes until crossing an integer Y value
               double delta = Math.signum((n.child.y - zeroY) * pixelSize - y) * 1e-6;
               while (n != Root.this.lastNode && Math.floor(y + delta) == Math.floor((n.child.y - zeroY) * pixelSize)) {
                  offset = 0.0f;
                  y = (n.child.y - zeroY) * pixelSize;
                  n = n.child;
               }
               // compute the offset
               if (n != Root.this.lastNode) {
                  double length = (n.child.y - n.y) * pixelSize;
                  if (length > 0.0f) offset = (Math.floor(y) + 1.0 - (n.y - zeroY) * pixelSize) / length;
                  else if (length < 0.0f) offset = -((n.y - zeroY) * pixelSize - (Math.ceil(y) - 1.0)) / length;
                  else SR.write("Something wrong in Root.getAttributesIterator().");
               }
               else n = null;
            }
            
            public void remove() {}
         };
      return null;
   }

   /**
    * Get the closest mark at the x/y position 
    * @param x
    * @param y
    * @return
    */
   public Mark getMarkAt(float x, float y) {
      for (int i = 0; i < markList.size(); i++) {
         Mark m = (Mark) markList.get(i);
         if (m.contains(x, y)) return m;
         }
      if (anchor != null && anchor.contains(x, y)) return anchor;
      return null;
      }

   /**
    * Get the ruler line along the root
    * @param lp
    * @return
    */
   public Line2D.Float getRulerLine(float lp) {
      lp = lp / pixelSize - rulerAtOrigin;
      Node n1 = firstNode;
      while (n1 != lastNode && n1.child.cLength < lp) n1 = n1.child;
      if (n1 == lastNode) rulerLine.setLine(0.0f, 0.0f, 0.0f, 0.0f);
      else {
         float dx = n1.child.x - n1.x;
         float dy = n1.child.y - n1.y;
         float r = (lp - n1.cLength) / n1.length;
         float x = n1.x + r * dx;
         float y = n1.y + r * dy;
         rulerLine.setLine(x + dy, y - dx, x - dy, y + dx);
         }
      return rulerLine;
      }

   /**
    * Get the node at the x/y position
    * @param x
    * @param y
    * @return the closest node
    */
   public Node getNode(float x, float y) {
      if (needsRefresh) createGraphics();
      if (tipsGP.contains(x, y)) return lastNode;
      if (nodesGP.contains(x, y)) {
         for (Node n = firstNode; n.child != null; n = n.child) {
            if (n.contains(x, y)) return n;
            }
         }
      return null;
      }

   /**
    * Get the root id
    * @return the root ide
    */
   public String getRootID() {return rootID; }
 
   /**
    * Get the root length
    * @return the root length
    */
   public float getRootLength() {
	   return lastNode.cLength + rulerAtOrigin; 
	   }

   /**
    * Get the ruler at origin
    * @return The ruler at origin
    */
   public float getRulerAtOrigin() {return rulerAtOrigin; }
   
   /**
    * Convert a position from cm to pixels
    * @param cm the position in cm
    * @return the position in px
    */
   public float lPosCmToPixels(float cm) {
      return cm / pixelSize - this.rulerAtOrigin;
      }

   /**
    * Convert a position from pixels to cm
    * @param pixels the position in pixels
    * @return the position in cm
    */
   public float lPosPixelsToCm(float pixels) {
      return pixelSize * (pixels + this.rulerAtOrigin);
      }

   /**
    * Does the root need to be refreshed
    */
   public void needsRefresh(){
	   needsRefresh = true;
	   for(int i = 0; i < this.markList.size(); i++){
		   markList.get(i).needsRefresh();
	   }
	 }
   
   /**
    * display the root
    * @param g2D
    * @param magnification
    * @param isSelected
    * @param displayAxis
    * @param displayNodes
    * @param displayBorders
    * @param displayArea
    * @param displayTicks
    * @param displayMarks
    * @param displayTicksP
    */
   public void paint(Graphics2D g2D, float magnification, boolean isSelected, boolean displayAxis,
                     boolean displayNodes, boolean displayBorders, boolean displayArea, 
                     boolean displayTicks, boolean displayMarks, boolean displayTicksP, boolean print, boolean color) {
      if (needsRefresh){
    	  createGraphics();
    	  for(int i = 0; i < markList.size(); i++) markList.get(i).createGraphics();
      }
      // Should try to set the current affine transform of G2D once and reset it after ?
   
      g2D.setComposite(ac1);
      Stroke bs1 = new BasicStroke(1.0f / magnification);
      Stroke bs2 = new BasicStroke(2.0f / magnification);
      g2D.setStroke(isSelected ? bs2 : bs1);
      if (displayAxis) {
    	  switch(isChild){
    		  case(0):
    			g2D.setColor(tipsColor);
    		  	if(!print) g2D.fill(tipsGP);
   		  		g2D.setColor(axisColor);
   		  		if(!color) g2D.setColor(Color.black);
   		  		//g2D.setColor(Color.blue);
   		  		g2D.draw(axisGP);
    		  	break;
    		  case(1):
    			g2D.setColor(childTipsColor);
    		  	if(!print) g2D.fill(tipsGP);
  		  		g2D.setColor(childAxisColor);
   		  		if(!color) g2D.setColor(Color.black);
  		  		//g2D.setColor(Color.orange);
  		  		g2D.draw(axisGP);
  		  		break;
    		  case(2):
    			g2D.setColor(Color.CYAN);
    		  	if(!print) g2D.fill(tipsGP);
  		  		g2D.setColor(Color.green);//Color.MAGENTA);
   		  		if(!color) g2D.setColor(Color.black);
  	  		  	g2D.draw(axisGP);
 		  		break;
    		  case(3):
    			g2D.setColor(Color.MAGENTA);
    		  	if(!print) g2D.fill(tipsGP);
  		  		g2D.setColor(Color.blue);//Color.CYAN);
   		  		if(!color) g2D.setColor(Color.black);
  	  		  	g2D.draw(axisGP);
 		  		break;
    	  }
    	  if(isChild > 3){
  			g2D.setColor(Color.MAGENTA);
		  	if(!print) g2D.fill(tipsGP);
		  	g2D.setColor(Color.blue);//Color.CYAN);
		  	if(!color) g2D.setColor(Color.black);
	  		g2D.draw(axisGP);
    	  }
         }
      if (displayNodes) {
    	  switch(isChild){
		  case(0):
			    g2D.setColor(nodesColor);
		  		if(!color) g2D.setColor(Color.black);
		  		g2D.draw(nodesGP);
		  		break;
		  case(1):
			    g2D.setColor(childNodesColor);
	  			if(!color) g2D.setColor(Color.black);
		  		g2D.draw(nodesGP);
		  		break;
		  case(2):
			    g2D.setColor(Color.CYAN);
		  		if(!color) g2D.setColor(Color.black);
		  		g2D.draw(nodesGP);
		  		break;
		  case(3):
			    g2D.setColor(Color.MAGENTA);
	  			if(!color) g2D.setColor(Color.black);
		  		g2D.draw(nodesGP);
		  		break;
		  }
    	  if(isChild > 3){
			    g2D.setColor(Color.MAGENTA);
	  			if(!color) g2D.setColor(Color.black);
		  		g2D.draw(nodesGP);
    	  }    	  
	  }

      if (displayBorders) {
          	if (isChild != 0) g2D.setColor(childBordersColor);
          	else g2D.setColor(bordersColor);         
	  		if(!color) g2D.setColor(Color.black);
	  		g2D.draw(bordersGP);    
         }
      if (displayTicks && !displayTicksP) {
    	  g2D.setStroke(bs1);
    	  g2D.setColor(ticksColor);
    	  if(!color) g2D.setColor(Color.black);
    	  g2D.draw(ticksGP);
    	  g2D.setColor(Color.magenta);
    	  for (int j = 0; j < tickList.size(); j++) {
    		  Tick t = (Tick) tickList.get(j);
    		  if (t.hasLabel) {
    			  g2D.drawString(Integer.toString(t.length),
    					  (float) (t.b2x + t.lblOffsetX),
    					  (float) (t.b2y + t.lblOffsetY));
    		  }
    	  }
      }
      if (displayTicks && displayTicksP && isChild==0) {
    	  	g2D.setStroke(bs1);
          	g2D.setColor(ticksColor);
	  		if(!color) g2D.setColor(Color.black);
	  		g2D.draw(ticksGP);
	  		if(!color) g2D.setColor(Color.black);
	  		g2D.setColor(Color.magenta);
	  		for (int j = 0; j < tickList.size(); j++) {
             Tick t = (Tick) tickList.get(j);
             if (t.hasLabel) {
                g2D.drawString(Integer.toString(t.length),
                               (float) (t.b2x + t.lblOffsetX),
                               (float) (t.b2y + t.lblOffsetY));
                }
             }
          }
      if (displayArea) {
    	  g2D.setComposite(isSelected ? ac3 : ac2);
    	  g2D.setColor(areaColor);
    	  if(!color) g2D.setColor(Color.black);
    	  g2D.fill(bordersGP);
         }
      if (displayMarks) {
         for (int i = 0; i < markList.size(); i++) {
            ((Mark) markList.get(i)).paint(g2D);
            }
         if (anchor != null) anchor.paint(g2D);
         }
      if (select){
    	  g2D.setComposite(isSelected ? ac3 : ac2);
    	  
    	  if(displayConvexHull){
    		  Stroke bs3 = new BasicStroke(4.0f / magnification);  
              g2D.setStroke(bs3);
              g2D.setColor(Color.blue);
              g2D.draw(convexhullGP);
              g2D.fill(convexhullGP);
          }
          g2D.setStroke(bs2);
          g2D.setColor(Color.red);
          g2D.fill(bordersGP);
          g2D.draw(bordersGP);   
          g2D.draw(nodesGP);
          g2D.draw(axisGP);

      }
      }


   /**
    * Read the root daa from the RSML file
    * @param parentDOM
    */
   public void readRSML(org.w3c.dom.Node parentDOM, RootModel rm, Root parentRoot, String origin, float scale) {
	  
	  int counter = 1, clock = 1; // The counter is used to select only one node in x (x = counter)
	  if(origin.equals("Root System Analyzer")) {
		  counter = 5;
		  clock = 5;
	  }
//	  SR.write("origin = "+origin);
//	  SR.write("counter = "+counter);
	   
	  org.w3c.dom.Node nn = parentDOM.getAttributes().getNamedItem("label");
	  if (nn != null) rootID = nn.getNodeValue();
	 
	  //SR.write("rootkey = "+rootKey);
	  nn = parentDOM.getAttributes().getNamedItem("ID");
	  if (nn != null){
		  rootKey = nn.getNodeValue();
		  //SR.write("rootkey = "+rootKey);
	  }
	 
	  nn = parentDOM.getAttributes().getNamedItem("po:accession");
	  if (nn != null) poIndex = rm.getIndexFromPo(nn.getNodeValue());
	 
	  
	  // Get the diameter nodes
	  org.w3c.dom.Node nodeDiameters = null; 
	  org.w3c.dom.Node nodeDiam = null;	  
      org.w3c.dom.Node nodeDOM = parentDOM.getFirstChild();
      while (nodeDOM != null) {
          String nName = nodeDOM.getNodeName();
          if(nName.equals("functions")){
			   org.w3c.dom.Node nodeFunctions = nodeDOM.getFirstChild();
			   while(nodeFunctions != null){
			      String fName = nodeFunctions.getNodeName();
		          if(fName.equals("function")){
				      String fAtt1 = nodeFunctions.getAttributes().getNamedItem("name").getNodeValue();
				      String fAtt2 = nodeFunctions.getAttributes().getNamedItem("domain").getNodeValue();
			          if(fAtt1.equals("diameter") & fAtt2.equals("polyline")){
			        	  nodeDiameters = nodeFunctions;
			        	  break;
			          }
		          }
		          nodeFunctions = nodeFunctions.getNextSibling();
			   }
          }
          nodeDOM = nodeDOM.getNextSibling(); 
      }

	  // Get the diameter nodes
	  org.w3c.dom.Node nodePixels = null; 
	  org.w3c.dom.Node nodePix = null;	  
      nodeDOM = parentDOM.getFirstChild();
      while (nodeDOM != null) {
          String nName = nodeDOM.getNodeName();
          if(nName.equals("functions")){
			   org.w3c.dom.Node nodeFunctions = nodeDOM.getFirstChild();
			   while(nodeFunctions != null){
			      String fName = nodeFunctions.getNodeName();
		          if(fName.equals("function")){
				      String fAtt1 = nodeFunctions.getAttributes().getNamedItem("name").getNodeValue();
				      String fAtt2 = nodeFunctions.getAttributes().getNamedItem("domain").getNodeValue();
			          if(fAtt1.equals("pixel") & fAtt2.equals("polyline")){
			        	  nodePixels = nodeFunctions;
			        	  break;
			          }
		          }
		          nodeFunctions = nodeFunctions.getNextSibling();
			   }
          }
          nodeDOM = nodeDOM.getNextSibling(); 
      }      
      
	  nodeDOM = parentDOM.getFirstChild();
      while (nodeDOM != null) {
         String nName = nodeDOM.getNodeName();
         // Nodes that are neither name, rulerAtOrigin nor Node elemnts are not considered
         // Read the geometry
         if (nName.equals("geometry")) {
			   org.w3c.dom.Node nodeGeom = nodeDOM.getFirstChild();
			   while (nodeGeom != null) {
				   	String geomName = nodeGeom.getNodeName();
				   if (geomName.equals("polyline")) {
					   org.w3c.dom.Node nodePoint = nodeGeom.getFirstChild();
					   if(nodeDiameters != null) nodeDiam = nodeDiameters.getFirstChild();		   
					   if(nodePixels != null) nodePix = nodePixels.getFirstChild();		   
					   while (nodePoint != null) {
						   	String pointName = nodePoint.getNodeName();
						   if (pointName.equals("point")) {
							   	if(counter == clock){
							   		Node no = addNode(0.0f, 0.0f, false);
			        			 	if(nodeDiam != null){
				        			 	if(nodeDiam != null){
				        			 		no.readRSML(nodePoint, nodeDiam, nodePix, 1, scale);
				        			 	}
				        			 	else no.readRSML(nodePoint, nodeDiam, null, 1, scale);
			        			 	}
			        			 	else{
			        			 		no.readRSML(nodePoint, null, null, 1, scale);		        			 	
			        			 	}
			        			 	counter = 0;
							   	}
							   	counter++;
						   }
						   nodePoint = nodePoint.getNextSibling();
						   if(nodeDiam != null) nodeDiam = nodeDiam.getNextSibling();
						   if(nodePix != null) nodePix = nodePix.getNextSibling();
					   }
					   this.firstNode.calcCLength(0.0f);
					   if(validate()){
						   rm.rootList.add(this);
					   }
					   if(parentRoot != null) {
						   attachParent(parentRoot);
						   parentRoot.attachChild(this);
					   }
				   }
				   nodeGeom = nodeGeom.getNextSibling();
			   }
         }
         // Read child roots
         else if (nName.equals("root")){
        	  new Root(dpi, nodeDOM, true, this, rm, origin);
         }
         nodeDOM = nodeDOM.getNextSibling();
      } 

      if(rootKey.equals("")) rootKey = this.getNewRootKey();
      if(rootID.equals(noName)){
    	  rootID = "root_"+rm.nextAutoRootID;
    	  rm.nextAutoRootID++;
      }
      if(rootID.equals("")){
    	  rootID = "root_"+rm.nextAutoRootID;
    	  rm.nextAutoRootID++;
      }
      
      // Compute the diameter of the nodes with a diameter = 0
      Node n = firstNode;
      while(n != null){
    	  if(n.diameter == 0 || origin.equals("RootNav")){
    		  n.diameter = 3;
    		  rm.fit.reCenter(n, 0.05f, 0.5f, false, 1);
    	  }
    	  n = n.child;
      }
      
	  if(origin.equals("RootNav")){
		  n = firstNode;
		  boolean del = false;
		  Node n1 = null;
		  while (n != null) {	
			  if(n.length < 5){
				  del = true;
				  n1 = n;
			  }
			  n = n.child; 
			  if(del){
				  if(n1 != null) rmNode(n1);
				  del = false;
				  n1 = null;
			  }
			  
		  }
	  }
      
      
      // Add the annotations
      nodeDOM = parentDOM.getFirstChild();
      while (nodeDOM != null) {
          String nName = nodeDOM.getNodeName();
          if(nName.equals("annotations")){
			   org.w3c.dom.Node nodeAnnotations = nodeDOM.getFirstChild();
			   while(nodeAnnotations != null){
			      String fName = nodeAnnotations.getNodeName();
		          if(fName.equals("annotation")){
		              Mark m = Mark.readRSML(nodeAnnotations, this);
		              if (m != null) this.addMark(m);
		          }
		          nodeAnnotations = nodeAnnotations.getNextSibling();
			   }
          }
          nodeDOM = nodeDOM.getNextSibling(); 
      }      
      needsRefresh();
      }
   
  
   /**
    * Read the root data from an xml datafile
    * @param parentDOM
    */
   public void read(org.w3c.dom.Node parentDOM) {
      org.w3c.dom.Node nodeDOM = parentDOM.getFirstChild();
      Node n = null;
      while (nodeDOM != null) {

         String nName = nodeDOM.getNodeName();
         // Nodes that are neither name, rulerAtOrigin nor Node elemnts are not considered
         if (nName.equals("name")) rootID = nodeDOM.getFirstChild().getNodeValue();
         else if (nName.equals("rootKey")) rootKey = nodeDOM.getFirstChild().getNodeValue();//Integer.valueOf(nodeDOM.getFirstChild().getNodeValue());
         else if (nName.equals("isChild")) isChild(Integer.valueOf(nodeDOM.getFirstChild().getNodeValue()));
         else if (nName.equals("rulerAtOrigin")) rulerAtOrigin = Float.valueOf(nodeDOM.getFirstChild().getNodeValue()).floatValue();
         else if (nName.equals("parent")) parentName = nodeDOM.getFirstChild().getNodeValue();
         else if (nName.equals("parentKey")) parentKey = nodeDOM.getFirstChild().getNodeValue();//Integer.valueOf(nodeDOM.getFirstChild().getNodeValue());
         
         else if (nName.equals("Node")) {
            n = addNode(0.0f, 0.0f, false);
            n.readXML(nodeDOM, false);
            }
         else if (nName.equals("Mark")) {
            Mark m = Mark.read(nodeDOM, this);
            if (m != null) this.addMark(m);
            }
         
         nodeDOM = nodeDOM.getNextSibling();
         }

      this.firstNode.calcCLength(0.0f);
      for (int i = markList.size() - 1; i >= 0; i--) {
         Mark m = (Mark)markList.get(i);
         if (m.lPos > lastNode.cLength || m.twinLPos > lastNode.cLength) markList.removeElementAt(i);
         else m.createGraphics();
         }
      needsRefresh();
      }
      
   /**
    * remove a mark
    * @param m the mark
    */
   public void removeMark(Mark m) {
      if (m == anchor) {
         anchor = null;
         }
      else markList.removeElement(m);
      }
   
   /**
    * Remove all marks
    * @param removeAnchor
    */
   public void removeAllMarks(boolean removeAnchor) {
      for (int i = markList.size() - 1; i >= 0; i--) {
         if (!((Mark)markList.get(i)).isForeign) markList.remove(i);
         }
      if (removeAnchor) anchor = null;
      }
      
   /**
    * Remove all the linked marks
    */
   public void removeLinkedMarks() {
      for (int i = markList.size() - 1; i >= 0; i--) {
         if (((Mark)markList.get(i)).isForeign) markList.remove(i);
         }
      }

   /**
    * Reverse the orientation of the root
    */
   public void reverse() {
      if (rulerAtOrigin != 0.0f) {
         if (anchor != null) {
            anchor.lPos = lastNode.cLength - anchor.lPos;
            rulerAtOrigin = - (anchor.lPos - Float.parseFloat(anchor.value) / pixelSize);
            anchor.needsRefresh();
            }
         else rulerAtOrigin = -(lastNode.cLength + rulerAtOrigin);
         }
      Node n;
      for (n = lastNode; n != null;) {
         n.invert();
         Node c = n.child;
         n.child = n.parent;
         n.parent = c;
         if (n.child != null) {
            float dx = n.child.x - n.x;
            float dy = n.child.y - n.y;
            n.theta = NodeFitter.vectToTheta(dx, dy);
            n.length = n.getDistanceTo(n.child);
            }
         n = n.child;
         }
      n = firstNode;
      firstNode = lastNode;
      lastNode = n;
      firstNode.calcCLength(0.0f);
      if(childList.size() != 0) updateChildren();
      needsRefresh();
      }

   /**
    * Remove the base of the root
    * @param beforeNode
    * @param rm
    */
   public void rmBaseOfRoot(Node beforeNode, RootModel rm) {
	  Node n = firstNode;
      int cl = childList.size();
      deletedRoot = new ArrayList<Root>();
      keepRoot = new ArrayList<Root>();
      int opt = 0;
      Root child;
      if(cl > 0){
		  opt = JOptionPane.showConfirmDialog(null, "Do you want to delete children?","Delete option", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
	      if (opt == JOptionPane.CANCEL_OPTION) return;
	      else if(opt == JOptionPane.YES_OPTION){
	    	  for (int k=0; k < cl; k++){
	    		  child = (Root) childList.get(k);
	    		  if(child.distanceFromBase < beforeNode.cLength) deletedRoot.add(child);
	    		  else keepRoot.add(child);
	    	  }
	    	  for (int k=0; k < deletedRoot.size(); k++){
	    		  child = (Root) deletedRoot.get(k);
	    		  child.delete(rm);
	    	  }
	      }
	      else if (opt == JOptionPane.NO_OPTION){
	    	  for (int k=0; k < cl; k++){
	    		  child = (Root) childList.get(k);
	    		  if(child.distanceFromBase < beforeNode.cLength) deletedRoot.add(child);
	    		  else keepRoot.add(child);
	    	  }
	    	  for (int k=0; k < deletedRoot.size(); k++){
	    		  child = (Root) deletedRoot.get(k);
	    		  child.detacheParent();
	    	  }
	      }
      }
      deletedRoot.clear();
      while (n != beforeNode) {
         Node n1 = n.child;
         n.child = null;
         n.parent = null;
         nNodes--;
         n = n1;    
         }
      beforeNode.parent = null;
      firstNode = beforeNode;
      firstNode.needsRefresh();
      if (beforeNode.cLength + getRulerAtOrigin() > 0.0f) setRulerAtOrigin(0.0f);
      else setRulerAtOrigin(getRulerAtOrigin() + beforeNode.cLength);
      firstNode.calcCLength(0.0f);
	  for (int k=0; k < keepRoot.size(); k++){
		  child = (Root) keepRoot.get(k);
		  child.updateRoot();
	  }  
      needsRefresh();
      }
      
   /**
    * Remove the end of the root
    * @param afterNode
    * @param rm
    * @param add
    */
   public void rmEndOfRoot(Node afterNode, RootModel rm, boolean add) {
      Node n = afterNode.child;
      int cl = childList.size();
      deletedRoot = new ArrayList<Root>();
      keepRoot = new ArrayList<Root>();
      int opt = 0;
      Root child;
      if(cl > 0){
		  if (!add) opt = JOptionPane.showConfirmDialog(null, "Do you want to delete children?","Delete option", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
		  else opt = JOptionPane.YES_OPTION; 
		  if (opt == JOptionPane.CANCEL_OPTION) return;
	      else if(opt == JOptionPane.YES_OPTION && !add){
	    	  for (int k=0; k < cl; k++){
	    		  child = (Root) childList.get(k);
	    		  if(child.distanceFromBase > afterNode.cLength) deletedRoot.add(child);
	    		  else keepRoot.add(child);
	    	  }
	    	  for (int k=0; k < deletedRoot.size(); k++){
	    		  child = (Root) deletedRoot.get(k);
	    		  child.delete(rm);
	    	  }
	      }
	      else if(opt == JOptionPane.YES_OPTION && add){
	    	  for (int k=0; k < cl; k++){
	    		  child = (Root) childList.get(k);
	    		  keepRoot.add(child);
	    	  }
	      }
	      else if (opt == JOptionPane.NO_OPTION){
	    	  for (int k=0; k < cl; k++){
	    		  child = (Root) childList.get(k);
	    		  if(child.distanceFromBase > afterNode.cLength) deletedRoot.add(child);
	    		  else keepRoot.add(child);
	    	  }
	    	  for (int k=0; k < deletedRoot.size(); k++){
	    		  child = (Root) deletedRoot.get(k);
	    		  child.detacheParent();
	    	  }
	      }
      }
      while (n != null) {
         Node n1 = n.child;
         n.child = null;
         n.parent = null;
         nNodes--;
         n = n1;      
         }
      afterNode.child = null;
      lastNode = afterNode;
      lastNode.needsRefresh();
      if (lastNode.cLength + getRulerAtOrigin() < 0.0f) setRulerAtOrigin(0.0f);
	  for (int k=0; k < keepRoot.size(); k++){
		  child = (Root) keepRoot.get(k);
		  child.updateRoot();
	  }  
      needsRefresh();
      }
      
   /**
    * Remove a the last node from the root
    * @return
    */
   public Node rmNode() {             //removes last node
      needsRefresh();
      lastNode = lastNode.parent;
      if (lastNode == null) firstNode = null;
      else lastNode.child = null;
      nNodes--;
      lastNode.needsRefresh();
      return lastNode;
      }

   /**
    * Remove a given node from the root 
    * @param n
    * @return
    */
   public boolean rmNode(Node n) {
      // this implementation assumes Node n is a member of this root
      if (nNodes == 1) {
         return true;
         }
      int rp = getNodePositionRelativeToRulerOrigin(n);
      Node prev = (n.parent == null) ? n : n.parent;
      Node next = (n.child == null) ? n : n.child;
      float base = next.cLength - prev.cLength;
      if (n == lastNode) {
         rmNode();
         }
      else if (n == firstNode) {
         firstNode = n.child;
         firstNode.parent = null;
         firstNode.needsRefresh();         
         firstNode.calcCLength(0.0f);
         }
      else {
         n.parent.child = n.child;
         n.child.parent = n.parent;
         n.parent.theta = NodeFitter.vectToTheta(n.parent, n.child);
         n.parent.length = n.parent.getDistanceTo(n.child);
         n.parent.calcBorders();
         n.child.needsRefresh();
         n.parent.calcCLength();
         }
      if (rp == 0) setRulerAtOrigin(0.0f);
      else if (rp == -1) setRulerAtOrigin(getRulerAtOrigin() - (next.cLength - prev.cLength - base));
      nNodes--;
      needsRefresh();
      return false;
      }

   /**
    * Set the DPI value for the root
    * @param dpi
    */
   public void setDPI(float dpi) {
      this.dpi = dpi;
      pixelSize = ((float) 2.54 / dpi);
      needsRefresh();
      }

   
   /**
    * Save the data to a RSML file
    * @param dataOut
    * @throws IOException
    */
   public void saveRSML(FileWriter dataOut, NodeFitter fit, boolean storePixels) throws IOException {
      Node n = firstNode;
      if (n == null) return;
      
      
      String nL = System.getProperty("line.separator");
      dataOut.write("			<root ID='" + rootKey + "' label='" + (rootID.equals("") ? noName : rootID) + "' po:accession='PO:0009005'>" + nL);
      dataOut.write("				<properties>" + nL);
      dataOut.write("  <rulerAtOrigin>" + Float.toString(rulerAtOrigin) + "</rulerAtOrigin>" + nL);      
      dataOut.write("					<length>" + lPosPixelsToCm(getRootLength()) + "</length>" + nL);
      dataOut.write("					<orientation>" + getRootOrientation() + "</orientation>" + nL);
      if(isChild() > 0) {
    	  dataOut.write("					<insertion>" + (lPosPixelsToCm(getDistanceFromBase())) + "</insertion>" + nL);
    	  dataOut.write("					<angle>" + (getInsertAngl() * (180 / Math.PI)) + "</angle>" + nL);
      }
      if(firstChild != null) dataOut.write("					<lbuz>" + (lPosPixelsToCm(getFirstChild().getDistanceFromBase())) + "</lbuz>" + nL);
      if(lastChild != null) dataOut.write("					<lauz>" + (lPosPixelsToCm(getLastChild().getDistanceFromBase())) + "</lauz>" + nL);
      dataOut.write("				</properties>" + nL);
      
      // Root gemeotry
      dataOut.write("				<geometry>" + nL);      
      dataOut.write("					<polyline>" + nL);      
      while (n != null) {
         n.saveCoordinatesToRSML(dataOut);
         n = n.child;
         }
      dataOut.write("					</polyline>" + nL);      
      dataOut.write("				</geometry>" + nL); 
      
      // Root functions
      dataOut.write("				<functions>" + nL);      
      dataOut.write("					<function name='diameter' domain='polyline'>" + nL);      
      n = firstNode;      
      while (n != null) {
         n.saveDiameterToRSML(dataOut);
         n = n.child;
         }
      dataOut.write("					</function>" + nL);  
      dataOut.write("					<function name='orientation' domain='polyline'>" + nL);      
      n = firstNode;      
      while (n != null) {
         n.saveOrientationToRSML(dataOut);
         n = n.child;
         }
      dataOut.write("					</function>" + nL); 
      
      if(storePixels){
	      dataOut.write("					<function name='pixel' domain='polyline'>" + nL);      
	      n = firstNode;      
	      while (n != null) {
	         n.savePixelToRSML(dataOut, fit);
	         n = n.child;
	         }
	      dataOut.write("					</function>" + nL);
      }
      
      dataOut.write("				</functions>" + nL);       
      
      
      // Root annotations
      dataOut.write("				<annotations>" + nL);   
      
      for (int i = 0; i < markList.size(); ((Mark)markList.get(i++)).saveToRSML(dataOut));

      dataOut.write("				</annotations>" + nL);            
      
      
      // Save the children
      for(int i = 0; i < childList.size(); i++){
    	Root r = childList.get(i);
    	r.saveRSML(dataOut, fit, storePixels);
      }
      
//      dataOut.write(" <measurments>" + nL);      
//      dataOut.write(" <length>" + lPosPixelsToCm(getRootLength())+ "</length>" + nL);      
//      dataOut.write(" </measurments>" + nL);      
      
      dataOut.write(" </root>" + nL);
      }
   
   
   /**
    * Save the root data to an XML file
    * @param dataOut
    * @throws IOException
    */
   public void save(FileWriter dataOut) throws IOException {
      Node n = firstNode;
      if (n == null) return;

      String nL = System.getProperty("line.separator");
      dataOut.write(" <Root>" + nL);
      dataOut.write("  <name>" + (rootID.equals("") ? noName : rootID) + "</name>" + nL);
      dataOut.write("  <rootKey>" + rootKey + "</rootKey>" + nL);
      dataOut.write("  <rulerAtOrigin>" + Float.toString(rulerAtOrigin) + "</rulerAtOrigin>" + nL);
      dataOut.write("  <isChild>" + Integer.toString(isChild()) + "</isChild>" + nL);
      if (parent != null){
    	  //dataOut.write("  <parent>" + parent.getRootID() + "</parent>" + nL);
    	  dataOut.write("  <parentKey>" + parent.getRootKey() + "</parentKey>" + nL);
      }
      else{
    	  //dataOut.write("  <parent>null</parent>" + nL);
    	  dataOut.write("  <parentKey>" + (-1) + "</parentKey>" + nL);
      }

      while (n != null) {
         n.saveToXML(dataOut);
         n = n.child;
         }

      if (anchor != null) anchor.save(dataOut);
      for (int i = 0; i < markList.size(); ((Mark)markList.get(i++)).save(dataOut));

      dataOut.write(" </Root>" + nL);
      }
 
   /**
    * Set the Plant Ontology accession of the root
    * @param po
    */
   public void setPoAccession(int po) {
      this.poIndex = (rootID.length() == 0) ? 0 : po;
      }   
   
   /**
    * Set the root id of the root (the name)
    * @param rootID
    */
   public void setRootID(String rootID) {
      this.rootID = (rootID.length() == 0) ? noName : rootID;
      updateChildren();
      }
   
   /**
    * Set the root key (unique identifier) of the root
    * @param rootKey
    */
   public void setRootKey(String rootKey) {
      this.rootKey = (rootKey.length() == 0) ? noName : rootKey;
      updateChildren();
      }

   /**
    * Set the ruler at origin for the root
    * @param rulerAtOrigin
    */
   public void setRulerAtOrigin(float rulerAtOrigin) {
      this.rulerAtOrigin = rulerAtOrigin;
      needsRefresh();
      if (anchor != null) {
         float v = lPosPixelsToCm(anchor.lPos);
         anchor.value = String.valueOf(Math.round(v * 100.0) / 100.0);
         anchor.needsRefresh();
         }
      }
      
   /**
    * Split the root in half
    * @param afterNode
    * @return
    */
   public Root split(Node afterNode, RootModel rm) {
      if (afterNode == firstNode || afterNode == lastNode) return null;

      Root r = new Root(dpi, getNewRootKey(), rm);
      r.firstNode = afterNode.child;
      r.firstNode.parent = null;
      r.lastNode = lastNode;
      r.calcNNodes();
      r.firstNode.calcCLength(0.0f);
      r.needsRefresh();

      lastNode = afterNode;
      afterNode.child = null;
      calcNNodes();
      needsRefresh();
      return r;
      }

   /**
    * Transform (deform) the root
    * @param at
    */
   public void transform(AffineTransform at) {
      Node n = firstNode;
      while (n != null) {
         n.transform(at);
         n = n.child;
         }
      this.firstNode.calcCLength(0.0f);
      this.calcTicks();
      for(int i = 0 ; i < markList.size() ; i++ ){
    	  markList.get(i).needsRefresh();
      }
      needsRefresh();
      }
      
   /**
    * Validate the root
    * @return
    */
   public boolean validate() {
      if (lastNode == null) {
         //IJ.showMessage("Internal data for root " + rootID + " appears to be corrupted." + 
         //      " They will not be written to the datafile and you will have to retrace the root.");
         return false;
      }
      return (!Float.isNaN(lastNode.cLength));
      }

/*
 *  All the methods hereafter are involved in the parent / child relationship
 */

   /**
    * Attach the selected root to the parent and set child informations
    * @param r the root to be attach
    */
   public void attachChild(Root r){
	   childList.add(r);
	   setFirstChild();
	   setLastChild();
	   setChildDensity();
   }
   
   /**
    * Update the children information
    */
   public void updateChildren(){
	   for (int i = 0 ; i < childList.size() ; i++){
		   Root c = (Root) childList.get(i);
		   c.updateRoot();
	   }
	   if(childList.size() > 0){
		   setFirstChild();
		   setLastChild();
		   setChildDensity();
	   }
   }

   /**
    * Detach the selected child from the parent and remove it from the child list
    * @param r child to be removed
    */
   public void detacheChild(Root r){
	   int index = childList.indexOf(r);
	   childList.remove(index);
	   if (childList.size() != 0 ){
		   setFirstChild();
		   setLastChild();
		   setChildDensity();
	   }
	   else{
		   removeMark(MDL);
		   setFirstChild();
		   setLastChild();
	   }
   }
 
   /**
    * Detach the selected child from the parent and remove it from the child list
    * @param index position of the child in the child list
    */
   public void detacheChild(int index){
	   childList.remove(index);
	   if (childList.size() != 0 ){
		   setFirstChild();
		   setLastChild();
	   }
	   else{
		   removeMark(MDL);
		   setFirstChild();
		   setLastChild();
	   }
   }
      
   /**
    * Set if the root is a child
    * @param l true if child
    */
   public void isChild(int l){ isChild = l; }
   
  
   /**
    * 
    * @return the child level (0 = primary, 1 = level 1, ...)
    */
   public int isChild(){ return isChild; }
      
   
   /**
    * Attach the selected root as parent
    * @param r parent root
    */
   
   public void attachParent(Root r){
	   attachParent(r, true);
   }
   
   /**
    * Attach the selected root as parent
    * @param r parent root
    * @param is their a need to detach the previous parent?
    */
   public void attachParent(Root r, boolean detach){
	   
	   if(detach && parent != null){
		   parent.detacheChild(this);
	   }
	   r.needsRefresh();
	   parent = r;
	   isChild(parent.isChild() + 1);
	   setParentNode();
	   setDistanceFromBase();
	   setDistanceFromApex();
	   setInsertAngl();
	   setInterBranch();
	   setParentName(parent.getRootID());
	   updateChildren();
	   poIndex = 2;
   }
   
   /**
    * Update the root information relative to his parent
    */
   public void updateRoot(){
	   if(parent != null){
	   setDistanceFromBase();
	   setDistanceFromApex();
	   setInsertAngl();
	   setInterBranch();
	   isChild(parent.isChild() + 1);
	   setParentName(parent.getRootID());
	   setParentKey(parent.getRootKey());
	   }
	   if(childList.size() > 0) updateChildren();
   }
   
   /**
    * Detach the parent of the root and update information relative to this parent
    */
   public void detacheParent(){
	   parent.detacheChild(this);
	   parent = null;
	   isChild(0);
	   setInsertAngl(0);
	   setInterBranch(0);
	   setParentNode();
	   setDistanceFromApex(0); 
	   setDistanceFromBase(0);
	   updateChildren();
   }
   
   /**
    * set the distance from the parent apex
    * @param d distance from apex
    */
   public void setDistanceFromApex(float d){ distanceFromApex = d;}
   
   
   /**
    * Automatically set distance from parent apex 
    */
   public void setDistanceFromApex(){
	   if(parent != null) distanceFromApex = parent.getRootLength() - distanceFromBase;
	   if(distanceFromApex < 0) distanceFromApex = 0;
   }
   
   /**
    * set the distance from the parent base
    * @param d distance from base
    */
   public void setDistanceFromBase(float d){ distanceFromBase = d;}
   
   /**
    * Automatically set distance from parent base 
    */
   public void setDistanceFromBase(){

		float dx;
		float dy;
		boolean up;
		Node n = firstNode;
		Node n1 = parentNode;
		Node n2;
		if (n1 != null && n1.parent != null && n1.child != null){
			
			dx = n.x - n1.child.x;
			dy = n.y - n1.child.y;
			float dChild = (float) Math.sqrt( dx * dx + dy * dy);
			
			dx = n.x - n1.parent.x;
			dy = n.y - n1.parent.y;
			float dParent = (float) Math.sqrt( dx * dx + dy * dy);
			
			if(dParent < dChild){
				up = false;
				n2 = n1.parent;
			}
			else{
				up = true;
				n2 = n1.child;
			}

			int inc = 20;
			float stepX = (n2.x - n1.x) / inc;
			float stepY = (n2.y - n1.y) / inc;
			float minDist = 1000;
			float dist = 0;; float x; float y;
			for(int i = 0; i <= inc; i++){
				x = (n2.x + (stepX*i)) - n.x;
				y = (n2.y + (stepY*i)) - n.y;
				dist = (float) Math.sqrt( x * x + y * y);
				if(dist < minDist){
					minDist = dist;
				}
			}
			
			float dl1 = minDist ;
			
//			dx = n.x - n1.x;
//			dy = n.y - n1.y;
//			float d1 = (float) Math.sqrt( dx * dx + dy * dy);
//			
//			dx = n.x - n2.x;
//			dy = n.y - n2.y;
//			float d2 = (float) Math.sqrt( dx * dx + dy * dy);
//			
//			dx = n1.x - n2.x;
//			dy = n1.y - n2.y;
//			float d3 = (float) Math.sqrt( dx * dx + dy * dy);
//			
//			float dl1 = (d1 - d2  + (d3 * d3)) / (2*d3);					
//			
			if(up) distanceFromBase = n2.cLength - dl1;
			else distanceFromBase = n2.cLength + dl1;
		}
//		distanceFromBase = parentNode.cLength;
		if(distanceFromBase < 0) distanceFromBase = 0;
   }
   
   /**
    * Set which child is the first one on the root (closest from the base)
    * @return true if there is at least one child, false if not.
    */
   public boolean setFirstChild(){
	   if (childList.size() == 0){
		   firstChild = null;
		   return false;
	   }
	   Root fc = (Root) childList.get(0);
	   for (int i = 0; i < childList.size(); i++){
		   Root c = (Root) childList.get(i);
		   if (c.getDistanceFromApex() > fc.getDistanceFromApex()) fc = c;
	   }
	   firstChild = fc;
	   return true;   
   }
   
   /**
    * Set which child is the last one on the root (closest from the apex)
    * @return true if there is at least one child, false if not.
    */
   public boolean setLastChild(){
	   if (childList.size() == 0){
		   lastChild = null;
		   return false;
	   }
	   Root fc = (Root) childList.get(0);
	   for (int i = 0; i < childList.size(); i++){
		   Root c = (Root) childList.get(i);
		   if (c.getDistanceFromApex() < fc.getDistanceFromApex()) fc = c;
	   }
	   lastChild = fc;
	   lastChild.setDistanceFromBase();
	   addMark(Mark.MDL, "0", lPosPixelsToCm(lastChild.distanceFromBase));
	   return true; 
   }
   
   /**
    * Set the child density of the root inside the ramified region.
    */
   public void setChildDensity(){
	   float dist = lPosPixelsToCm(lastChild.getDistanceFromBase() - firstChild.getDistanceFromBase());
	   if (dist != 0) childDensity = childList.size() / dist;
   }
   
   /**
    * set the insertion angle of the root in its parent
    * @param ins the insertion angle
    */
   public void setInsertAngl(float ins){insertAngl = ins;}  
   
   /**
    * Automatically set the insertion angle of the root on its parent
    */
   public void setInsertAngl(){
	   Node n = firstNode;
	   int count = 3;
	   float ang = 0;
	   
	   int i = 0;
	   while(n.child != null && i < count){
		   ang += n.theta;
		   n = n.child;
		   i++;
	   }
	   ang = ang / i;

	   if (ang > parentNode.theta) insertAngl = ang - parentNode.theta;
	   else insertAngl = parentNode.theta - ang;
	   if (insertAngl > (float) Math.PI ) insertAngl = (2 *(float) Math.PI) - insertAngl;
   }
         
   /**
    * Set the parentNode, which is the closest node in the parent from the base node of root
    */
   public void setParentNode(){
	   
		Node n = firstNode;
		Root p = (Root) getParent();
		if( p == null){
			parentNode = null;
			return;
		}
		Node np = p.firstNode;
		Node npFinal = p.firstNode;
		double dMin = Point2D.distance((double) n.x, (double) n.y, (double) np.x, (double) np.y);
		double d;
		while (np.child != null){
			np = np.child;
			d = Point2D.distance((double) n.x, (double) n.y, (double) np.x, (double) np.y);
			if (d < dMin){
				dMin = d;
				npFinal = np;
			}
		}	   
		parentNode = npFinal;
	}
      
   /**
    * Set the interbranch distance between this root and the previous child on the parent
    * @param iB the interbranch
    */
   public void setInterBranch(float iB) {interBranch = iB;}
   
   
   /**
    * Automatically set the interbranch distance between this root and the previous child on the parent
    */
   public void setInterBranch(){
	   if (isChild() == 0){ return; }
	   if (this == parent.firstChild){
		   interBranch = 0;
		   return; }
	   float dist = 0;
	   Root r;
	   for( int i = 0 ; i < parent.childList.size(); i++){
		   r =(Root) parent.childList.get(i);
		   if (this == r) continue;
		   float d = r.getDistanceFromApex() - this.getDistanceFromApex();
		   if (i == 0) dist = Math.abs(d);
		   if (d > 0 && d < dist ){
			   dist = d;
		   }
	   }
	   interBranch = dist;
   }
   
   /**
    * Set the root as selected or not. A selected root will be displayed in red (node, axis, border and area)
    * @param s true if selected
    */
   public void setSelect(boolean s) {select = s;}

   /**
    * get the closest node in the root from a given position on the root
    * @param lPos the position on the root
    * @return the closest node
    */
   public Node getClosestNode (float lPos){
	   Node n0 = firstNode;
	   Node n1 = firstNode;
	   float d0 = Math.abs(n0.cLength - lPos);
	   float d1 = 0;
	   while (n0.child != null){
		   n0 = n0.child;
		   d1 = Math.abs(n0.cLength - lPos);
		   if(d1 < d0){
			   d0 = d1;
			   n1 = n0;
		   }
	   }
	   return n1;
   }
   
   /**
    * Set the parent name
    * @param n
    */
   public void setParentName(String n){
	   parentName = n;
   }
   
   /**
    * Set the parent key
    * @param n
    */
   public void setParentKey(String n){
	   parentKey = n;
   }
   
   /**
    * @return parent the parent root
    */
   public Root getParent(){ return parent; }
   
   /**
    * @return parentNode the parent's closest node from the base of this root
    */
   public Node getParentNode(){ return parentNode;}
   
   /**
    * @return distanceFromApex the distance of the the insertion point from the parent's apex
    */
   public float getDistanceFromApex(){ return distanceFromApex;}

   /**
    * @return distanceFromBase the distance of the the insertion point from the parent's base
    */
   public float getDistanceFromBase(){ 
	  if(isChild() > 0) return distanceFromBase;
	  else return -1;
   }
   
   /**
    * @return parentName the name of the parent
    */
   public String getParentName(){ 
	   if(parentName != null) return parentName;
	   else return "-1";
	   }
   
   /**
    * @return interBranch the inter branch distance between this root and the previous one on the parent
    */
   public float getInterBranch() {return interBranch;}
   
   /**
    * @return childDensity the child density inside the ramified region
    */
   public float getChildDensity() {
	   if(childList.size() > 0){
		   float dist = lPosPixelsToCm(lastChild.getDistanceFromBase() - firstChild.getDistanceFromBase());
		   if (dist != 0) return childList.size() / dist;
		   else return 0;
	   }
	   else return 0;
   }
   
   /**
    * @return childDensity the child density inside the ramified region
    */
   public float getChildDensity1() {
	   
	   if(childList.size() > 0){
		   float dist = lPosPixelsToCm(lastChild.getDistanceFromBase() - firstChild.getDistanceFromBase());
		   if (dist != 0) return childList.size() / dist;
		   else return 0;
	   }
	   else return 0;
	}
   
   /**
    * @return firstChild the first lateral root (the closest from the base)
    */
   public Root getFirstChild(){return firstChild;}

   /**
    * @return lastChild the last lateral root (the closest from the apex)
    */
   public Root getLastChild(){return lastChild;}
   
   /**
    * @return insertAngl the insertion angle on the parent root
    */
   public float getInsertAngl(){
	   if(isChild() > 0) return insertAngl;
	   else return -1;
	   }
   
   /**
    * delete the root
    * @param rm
    */
   public void delete(RootModel rm){
	   rm.selectRoot(this);
	   rm.deleteSelectedRoot();
   }
   
   /**
    * Get the root path to the collar (list of parent,  great-parent, ...)
    * @return
    */
   public String getRootPath(){
	   String path;
	   if(isChild > 0) path = this.getParentName();
	   else path = "-1";
	   Root r = this.getParent();
	   for(int i = 1 ; i < isChild ; i++){
		   path = r.getParentName().concat(":" + path);
		   r = r.getParent();
	   }
	   return path;
   }
   
   /**
    * Get the average diameter of the root
    * @return
    */
   public float getAVGDiameter(){
	   float n = 0;
	   int m = 0;
	   Node node = this.firstNode;
		n += node.diameter;
		m++;
		while (node.child != null){
			node = node.child;
			n += node.diameter;
			m++;
		}
		return (n / m) * pixelSize;   
   }
   
   /**
    * Get the averag interbranch distance of the root
    * @return
    */
   public float getAVGInterBranchDistance(){
	   float n = 0;
	   int m = 0;
	   for(int i = 0 ; i < childList.size() ; i++){
		   Root r = (Root) childList.get(i);
		   n += r.getInterBranch();
		   m++;
	   }
	   return (n / m ) * pixelSize;   
   }
   
   /**
    * 
    */
   public String toString(){
	   return rootID;
   }

   /**
    * Set the selection of the root
    * @param b
    */
   public void setSelected(boolean b) {
	select = b;
   }
   
   /**
    * Displayt the convex hull
    * @param b
    */
   public void displayConvexHull(boolean b){
	   displayConvexHull = b;
   }
   
   /**
    * Get the total root volumes. 
    * The root segments are considered as truncated pyramids with a circular base
    * @return
    */
   public float getRootVolume(){
	   float vol = 0;
	   Node n = this.firstNode;
		while (n.child != null){
			n = n.child;
			double r1 = (n.parent.diameter * pixelSize) / 2;
			double r2 = (n.diameter * pixelSize) / 2;
			double h = (n.parent.length * pixelSize);
			vol += (Math.PI * (Math.pow(r1, 2) + Math.pow(r2, 2) + (r1*r2) ) * h) / 3;
//			vol += Math.PI * r1 * r1 * h;
		}	   
		return vol;
   }

   /**
    * Get the total root surface
    * The root segments are considered as truncated pyramids with a circular base
    * @return
    */
   public float getRootProjectedSurface(){
	   float surf = 0;
	   Node n = this.firstNode;
		while (n.child != null){
			n = n.child;
			double r1 = (n.parent.diameter * pixelSize);
			double r2 = (n.diameter * pixelSize);
			double rMax = Math.max(r1, r2);
			double rMin = Math.min(r1, r2);
			double h = (n.parent.length * pixelSize);			
			surf += (h * (rMax + ((rMax-rMin)/2)));
		}	   
		return surf;
   }   
   
   /**
    * Get the total root surface
    * The root segments are considered as truncated pyramids with a circular base
    * @return
    */
   public float getRootSurface(){
	   float surf = 0;
	   Node n = this.firstNode;
		while (n.child != null){
			n = n.child;
//			double B = n.parent.diameter * pixelSize * Math.PI;
//			double b = n.diameter * pixelSize * Math.PI;
//			surf += (n.length * pixelSize) * ( (B + b) / 2);
			double r1 = (n.parent.diameter * pixelSize) / 2;
			double r2 = (n.diameter * pixelSize) / 2;
			double rMax = Math.max(r1, r2);
			double rMin = Math.min(r1, r2);
			double h = (n.parent.length * pixelSize);
			surf += Math.PI * (rMax+rMin) * Math.sqrt( Math.pow((rMax-rMin), 2) + Math.pow(h, 2));
//			surf += 2 * Math.PI * r1 * h;
		}	   
		return surf;
   }
   
   /**
    * Get the root network (root + laterals) surface
    * @return
    */
   public float getRootNetworkSurface(){
	   float surf = this.getRootProjectedSurface();
	   for(int i = 0; i < childList.size(); i++){
		   surf += childList.get(i).getRootProjectedSurface();
	   }
	   return surf;
   }   
   
   /**
    * Get the surface of all the laterals
    * @return
    */
   public float getChildrenSurface(){
	   float surf = 0;
	   for(int i = 0; i < childList.size(); i++){
		   surf += childList.get(i).getRootSurface();
//		   Node n = childList.get(i).firstNode;
//			while (n.child != null){
//				n = n.child;
//				double B = n.parent.diameter * pixelSize * Math.PI;
//				double b = n.diameter * pixelSize * Math.PI;
//				surf += (n.length * pixelSize) * ( (B + b) / 2);
//			}	   
	   }
		return surf;
   }   
  
   
   /**
    * Get the root identifier
    * @return
    */
   public String getRootKey(){
	   return rootKey;
   }
   
   /**
    * Get the next value for the "Number" mark.
    * Increment the previous value along the root axis.
    * @param markerPosition
    * @return
    */
   public float getNextNumberMarkValue(float markerPosition){
	   float lPos = lPosCmToPixels(markerPosition);
	   List<Mark> numList = new ArrayList<Mark>();
	   for(int i = 0; i < markList.size(); i++){
		   if(markList.get(i).type == Mark.NUMBER){
			   int j = 0;
			   while(j < numList.size() && numList.get(j).lPos < markList.get(i).lPos) j++;
			   numList.add(j, markList.get(i));
		   }
	   }
	   int i = 0;
	   if(numList.size() > 0){
		   if(lPos < numList.get(i).lPos) return Float.valueOf(numList.get(i).getValue()) - 1;
		   while(i < numList.size() && lPos > numList.get(i).lPos) i++;
		   return Float.valueOf(numList.get(i-1).getValue()) + 1;
	   }
	   else return 1;
   }
   
   /**
    * Get a new, randomly generated, root key
    * @return
    */
   public String getNewRootKey(){
   	return UUID.randomUUID().toString();
   }
   
   
   /**
    * Return an integer indicating if the root is on the right or the left side of its parent (if it is a lateral)
    * @return 0 if not a lateral; 1 if on the left, 2 if on the right
    */
   public int isLeftRight(){
	   
	   if(isChild() == 0) return 0;
	   else{
		   if(this.firstNode != null && this.parentNode != null){
			   if(this.firstNode.x < this.parentNode.x) return 1;
		   		else return 2;
		   }
		   else return 0;
	   }
	   
   }

	/**
	 * Returns the tortuosity of the root
	 * @return
	 */
	public float getTortuosity() {
		float tort = 0;
		Node n = this.firstNode;
		Node nPrev = this.firstNode;
		int inc = 0;
		while (n.child != null){
			n = n.child;
			tort += Math.abs(n.theta - nPrev.theta);
			nPrev = n;
			inc++;
		}	   
		return tort / inc;	
	}
	
	/**
	 * Return the vector lenght of the root (as defined in Armengaud 2009)
	 * @return
	 */
	public float getVectorLength() {
		Node n1 = this.firstNode;
		Node n2 = this.lastNode;   
		return (float) Math.sqrt(Math.pow((n1.x-n2.x),2) + Math.pow((n1.y-n2.y),2));
	}
	
	/**
	 * Return the total length of all the children
	 * @return
	 */
	public float getChildrenLength() {
		float cl = 0;   
		for(int i = 0; i < childList.size(); i++){
			cl = cl + childList.get(i).getRootLength();
		}
		return cl;
	}
	
	/**
	 * Return the mean children angle 
	 */
	public float getChildrenAngle(){
		if(childList.size() > 0){
		   float ang = 0;
		   for(int i = 0; i < childList.size(); i++){
			   ang += childList.get(i).getInsertAngl();	   
		   }
			return ang / childList.size();
		}
		else return 0; 
	} 
	
	/**
	 * Return the min X coordinate of the root
	 * @return
	 */
	public float getXMin() {
		float min = 100000;
		Node n = this.firstNode;
		while (n.child != null){
			if(n.x < min) min = n.x;
			n = n.child;
		}	   
		return min;	
	}
	
	
	/**
	 * Return the max X coordinate of the root
	 * @return
	 */
	public float getXMax() {
		float max = 0;
		Node n = this.firstNode;
		while (n.child != null){
			if(n.x > max) max = n.x;
			n = n.child;
		}	   
		return max;	
	}
	
	
	/**
	 * Return the min Y coordinate of the root
	 * @return
	 */
	public float getYMin() {
		float min = 100000;
		Node n = this.firstNode;
		while (n.child != null){
			if(n.y < min) min = n.y;
			n = n.child;
		}	   
		return min;	
	}
	
	/**
	 * Return the max Y coordinate of the root
	 * @return
	 */
	public float getYMax() {
		float max = 0;
		Node n = this.firstNode;
		while (n.child != null){
			if(n.y > max) max = n.y;
			n = n.child;
		}	   
		return max;	
	}
	
	/**
	 * Return the min X coordinate of the root and its children
	 * @return
	 */
	public float getXMinTotal() {
		float min = 100000;
		Node n = this.firstNode;
		while (n.child != null){
			if(n.x < min) min = n.x;
			n = n.child;
		}	
		for(int i = 0; i < childList.size(); i++){
			n = childList.get(i).firstNode;		
			while (n.child != null){
				if(n.x < min) min = n.x;
				n = n.child;
			}	
		}	
		return min;	
	}
	
	
	/**
	 * Return the max X coordinate of the root and its children
	 * @return
	 */
	public float getXMaxTotal() {
		float max = 0;
		Node n = this.firstNode;
		while (n.child != null){
			if(n.x > max) max = n.x;
			n = n.child;
		}	   
		for(int i = 0; i < childList.size(); i++){
			n = childList.get(i).firstNode;		
			while (n.child != null){
				if(n.x > max) max = n.x;
				n = n.child;
			}	
		}		
		return max;	
	}
	
	
	/**
	 * Return the min Y coordinate of the root and its children
	 * @return
	 */
	public float getYMinTotal() {
		float min = 100000;
		Node n = this.firstNode;
		while (n.child != null){
			if(n.y < min) min = n.y;
			n = n.child;
		}	   
		for(int i = 0; i < childList.size(); i++){
			n = childList.get(i).firstNode;		
			while (n.child != null){
				if(n.y < min) min = n.y;
				n = n.child;
			}	
		}	
		return min;	
	}
	
	/**
	 * Return the max Y coordinate of the root and its children
	 * @return
	 */
	public float getYMaxTotal() {
		float max = 0;
		Node n = this.firstNode;
		while (n.child != null){
			if(n.y > max) max = n.y;
			n = n.child;
		}	   
		for(int i = 0; i < childList.size(); i++){
			n = childList.get(i).firstNode;		
			while (n.child != null){
				if(n.y > max) max = n.y;
				n = n.child;
			}	
		}
		return max;	
	}
	
	/**
	 * Return the average orientation of the root
	 * @return
	 */
	public float getRootOrientation() {
		float angle = 0;
		int count = 0;
		Node n = this.firstNode;
		while (n.child != null){
			angle += n.theta;
			count ++;
			n = n.child;
		}	   	
		return (angle / count);	
	}

	/**
	 * Fonction used for the table display ofthe root system
	 * @return
	 */
	public int getLevel() {
		// TODO Auto-generated method stub
		return this.isChild();
	}
	   
		  
	/**
	 * Get the root convexhull
	 * @return
	 */
	public PolygonRoi getConvexHull(){
			
		List<Integer> xList = new ArrayList<Integer>(); 
		List<Integer> yList = new ArrayList<Integer>();
		
		// Add all the nodes coordinates
		Node n = this.firstNode;
		while (n.child != null){
//			xList.add((int) n.x);
//			yList.add((int) n.y);
//			for(int j = 0; j < n.by.length; j++) if(n.by[j] > 0) yList.add((int) n.by[j]);
//			for(int j = 0; j < n.bx.length; j++) if(n.bx[j] > 0) xList.add((int) n.bx[j]);	
			xList.add((int) n.bx[4]);
			xList.add((int) n.bx[5]);
			yList.add((int) n.by[4]);
			yList.add((int) n.by[5]);
			n = n.child;
		}
		xList.add((int) n.x);
		yList.add((int) n.y);
		if(SR.prefs.getBoolean("globalConvex", true)){
			for(int i = 0; i < childList.size(); i++){
				Root r = childList.get(i);
				n = r.firstNode;
				while (n.child != null){
//					xList.add((int) n.x);
//					yList.add((int) n.y);
//					for(int j = 0; j < n.by.length; j++) if(n.by[j] > 0) yList.add((int) n.by[j]);
//					for(int j = 0; j < n.bx.length; j++) if(n.bx[j] > 0) xList.add((int) n.bx[j]);		
					xList.add((int) n.bx[4]);
					xList.add((int) n.bx[5]);
					yList.add((int) n.by[4]);
					yList.add((int) n.by[5]);					
					n = n.child;
				}
				xList.add((int) n.x);
				yList.add((int) n.y);
			}
		}
		
		int[] xRoiNew = new int[xList.size()];
		int[] yRoiNew = new int[yList.size()];
		for(int l = 0; l < yList.size(); l++){
			xRoiNew[l] = xList.get(l);
			yRoiNew[l] = yList.get(l);
		}
		Roi roi = new PolygonRoi(xRoiNew, yRoiNew, yRoiNew.length, Roi.POLYGON);
		if(roi.getConvexHull() == null) return null;
		else return new PolygonRoi(roi.getConvexHull(),  Roi.POLYGON);
	}
	
//	/**
//	 * Get the Area of the convexhull
//	 * @return
//	 */
//	public float getConvexHullArea(){
//		ImageProcessor maskProcessor = getConvexHull().getMask(); // Get the convex hull from the object stored in the ROI Manager			
//		ImagePlus mask = new ImagePlus();
//		maskProcessor.invert();
//		Calibration cal = new Calibration();
//		cal.setUnit("cm");
//		cal.pixelHeight = pixelSize;
//		cal.pixelWidth = pixelSize;
//		mask.setProcessor(maskProcessor);
//		mask.setCalibration(cal);
//		SR.write("height = "+cal.pixelHeight);
//		
//		ResultsTable rt = new ResultsTable();
//		ParticleAnalyzer pa = new ParticleAnalyzer(ParticleAnalyzer.CLEAR_WORKSHEET, Measurements.AREA , rt, 0, 10e9);
//		pa.analyze(mask);
//		SR.write("CHArea = " + rt.getValue("Area", 0));
//		mask.show();
//		return(float) rt.getValue("Area", 0);
//	}

	
	public String getPoAccession(){
		
		return SR.listPoNames[poIndex];
	}
	
	
	
	// Create additional nodes within the root
	public void multiplyNodes(){
		Node n = firstNode;
		while(n.child != null){
			
			float newX = 0;
			if(n.x  < n.child.x) newX = n.x + ((n.child.x- n.x) / 2);
			else newX = n.x - ((n.x - n.child.x) / 2);
			
			float newY = 0;
			if(n.y < n.child.y) newY = n.y + ((n.child.y - n.y) / 2);
			else newY = n.y - (n.y - n.child.y) / 2;			
			
			float newD = (n.diameter + n.child.diameter)/2;
			
			n = n.child;
			
			Node newN = new Node(newX, newY, newD);
			
			Node n1 = n.parent; 			
			n1.child = newN;
			n.parent = newN;
			newN.parent = n1;
			newN.child = n;		
			
			newN.buildNode();
			n.buildNode();
			n1.buildNode();
			
			newN.needsRefresh = true;

		}
		needsRefresh = true;
	}
	
	public void cropRoot(RootModel rm){
		Node n = lastNode;
		rm.fit.checkImageProcessor();
		float corr = getMeanPixelValue(rm)-getMeanPixelValuePrev(rm);
		double thr = (getMaxPixelValuePrev(rm) - getMinPixelValuePrev(rm))/4;
		if(thr < 3) thr = 3;
		IJ.log("The threshold is" + thr + "The correction is " + corr);
		int count = 0;
		
		if(this != null){
			while(n != firstNode){
				if(n != null){
				float prev = n.prevPixValue + corr;
				float pix = rm.fit.getValue(n.x, n.y);
				float diff = pix-prev;
				n = n.parent;
				if(diff > thr){
					rmEndOfRoot(n, rm, true);
					count =0;
				}
				if(diff < thr/2) count = count+1;
				if(count > 10) break;
				} 
			}
			}
	
		if(nNodes < 3) delete(rm);
		
		//If the difference between the root and the parent node is too big, delete the whole root
		//IJ.log("The child level is" + this.isChild());
		//if(this.isChild() != 0){
		//float diff2 = getMaxPixelValue(rm) - getMeanPixelValue(rm);
		//IJ.log("The removal diff is" + diff2);
		//if(diff2 < thr/4){
		//		delete(rm);
		//}
		//}
			
		
//		if(fit.getValue(n2.x, n2.y) > autoThreshold) r.rmNode(n2);
//		n = r.lastNode;
//		if(fit.getValue(n.x, n.y) > autoThreshold) r.rmNode(n);
	}
	
	public float getMaxPixelValue(RootModel rm){
		Node n = firstNode;		
		Float max = 0f;
		rm.fit.checkImageProcessor();
		while(n.child != null){
			if(rm.fit.getValue(n.x, n.y) > max) max = rm.fit.getValue(n.x, n.y);
			n = n.child;				
		}
		return max;
	}
	
	
	public float getMinPixelValue(RootModel rm){
		Node n = firstNode;
		Float min = 1.e9f;
		rm.fit.checkImageProcessor();
		while(n.child != null){
			if(rm.fit.getValue(n.x, n.y) < min) min = rm.fit.getValue(n.x, n.y);
			n = n.child;
				
		}
		return min;
	}
	public float getMeanPixelValue(RootModel rm){
		Node n = firstNode;
		Float sum = 0f;
		int count = 0;
		rm.fit.checkImageProcessor();
		while(n.child != null){
			count ++;
			sum += rm.fit.getValue(n.x, n.y);
			n = n.child;
				
		}
		return sum/count;
	}
	public float getMaxPixelValuePrev(RootModel rm){
		Node n = firstNode;		
		Float max = 0f;
		rm.fit.checkImageProcessor();
		while(n.child != null){
			if(n.prevPixValue > max) max = n.prevPixValue;
			n = n.child;				
		}
		return max;
	}
	
	public float getMinPixelValuePrev(RootModel rm){
		Node n = firstNode;
		Float min = 1.e9f;
		rm.fit.checkImageProcessor();
		while(n.child != null){
			if(n.prevPixValue < min) min = n.prevPixValue;
			n = n.child;
				
		}
		return min;
	}
	public float getMeanPixelValuePrev(RootModel rm){
		Node n = firstNode;
		Float sum = 0f;
		int count = 0;
		rm.fit.checkImageProcessor();
		while(n.child != null){
			count ++;
			sum += n.prevPixValue;
			n = n.child;
				
		}
		return sum/count;
	}	
	
	
  }
