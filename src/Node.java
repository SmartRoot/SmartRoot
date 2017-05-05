/**
 * Copyright © 2009-2017, Université catholique de Louvain
 * All rights reserved.
 *
 * Copyright © 2017 Forschungszentrum Jülich GmbH
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


import java.awt.geom.*;
import java.io.*;

import ij.IJ;


/** 
 * @author Xavier Draye - Universit� catholique de Louvain
 * @author Guillaume Lobet - Universit� de Li�ge 
 * 
 * Class for the node objects. Nodes are the basic untis of the root
 * 
 * */

// %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%


class Node {
   float x, y, theta, length, cLength, diameter, birthTime, pixValue, prevPixValue; // length and cLength are in pixels
   float[] bx = new float[6]; // border
   float[] by = new float[6]; // border
   Node child;
   Node parent;
   boolean needsRefresh;
   boolean bCross01 = false, bCross23 = false;
   boolean pCross01 = false, pCross23 = false;   
   float[] px = new float[6]; // parallels (used for the lateral finding algorithm)
   float[] py = new float[6]; // parallels
   
  
   /**
    * Constructor
    */
   public Node() {
      parent = null;
      child = null;
      needsRefresh = true;
      };


      /**
       * Constructor
       * @param x position of the node
       * @param y position of the node
       * @param d diameter of the node
       */
       public Node(float x, float y, float d) {
          this.x = x;
          this.y = y;
          this.diameter = d;
          needsRefresh = false;
          }      
   
      
   /**
   * Constructor
   * @param x position of the node
   * @param y position of the node
   * @param d diameter of the node
   * @param n previous node
   * @param after: is this node before (parent) or after (child) compared to the n Node  
   */
   public Node(float x, float y, float d, Node n, boolean after) {
      this.x = x;
      this.y = y;
      this.diameter = d;
      if (after) {
         parent = n;
         if (parent != null) parent.child = this;
         child = null;
         }
      else {
         child = n;
         if (child != null) child.parent = this;
         parent = null;
         }
      needsRefresh = true;
      }

   /**
    * Constructor
    * @param x
    * @param y
    * @param n
    * @param after
    */
   public Node(float x, float y, Node n, boolean after) {
      this(x, y, 0f, n, after);
      }

   /** 
    * Transform a node based on an AffineTransform (used for Registration) 
    * @param the affine tranformation 
    * */
   public void transform (AffineTransform at) {
      Point2D.Float p = new Point2D.Float();
      p.setLocation(x, y);
      at.transform(p, p);
      x = p.x;
      y = p.y;
      diameter *= (float) (at.getScaleX() + at.getScaleY()) / 2.0f;
      if (parent != null) {
          float dx = x - parent.x;
          float dy = y - parent.y;
          parent.theta = NodeFitter.vectToTheta(dx, dy);
          parent.length = (float) Math.sqrt(dx * dx + dy * dy);
          parent.calcBorders();
         }
      needsRefresh = true;
      }

   
   /**
    * Build the node
    */
   public void buildNode() {
      if (diameter < 1.0f) diameter = 1.0f;

      // calculate length and theta where required
      if (parent != null) {
         float dx = x - parent.x;
         float dy = y - parent.y;
         parent.theta = NodeFitter.vectToTheta(dx, dy);
         parent.length = NodeFitter.norm(dx, dy);
         }
      if (child != null) {
         float dx = child.x - x;
         float dy = child.y - y;
         theta = NodeFitter.vectToTheta(dx, dy);
         length = NodeFitter.norm(dx, dy);
         }

      // calculate poles and borders
      needsRefresh = true;
      if (parent != null){ 
    	  parent.calcBorders();  
    	  parent.calcPoles(0);
      }
      if (child != null){
    	  calcBorders();
    	  calcPoles(0);
      }
      }


   /**
    * Get the node borders
    */
   public void calcBorders() {
	   calcBorders(0.5f, bx, by, child.bx, child.by);
   }
   
   /**
    * Get the node parralels at a given distance
    * @param dist
    */
   public void calcParallels(float dist){
	   if (child != null) calcBorders(dist, px, py, child.px, child.py);
   }

   /**
    * Evaluate the position of border anchors between nodes this and this.child
    * @param dist
    * @param bx
    * @param by
    * @param cbx
    * @param cby
    */
   public void calcBorders(float dist, float[] bx, float[] by, float[] cbx, float[] cby) {
      if (length == 0.0f) ; // Don't do the job
      float nr, cr, ncx, ncy, nax, nay, acnSin, acnCos;
      nr = diameter * dist;
      cr = child.diameter * dist;

      ncx = (child.x - x) / length; 
      ncy = (child.y - y) / length;
      acnSin = (cr - nr) / length;
      acnCos = (float) Math.sqrt(1 - acnSin * acnSin);

      nax = (-ncy) * acnCos - ncx * acnSin;
      nay = (-ncy) * acnSin + ncx * acnCos;
      bx[1] = x + nax * nr;
      by[1] = y + nay * nr;
      cbx[0] = child.x + nax * cr;
      cby[0] = child.y + nay * cr;

      nax = ncy * acnCos - (-ncx) * (-acnSin);
      nay = ncy * (-acnSin) + (-ncx) * acnCos;
      bx[3] = x + nax * nr;
      by[3] = y + nay * nr;
      cbx[2] = child.x + nax * cr;
      cby[2] = child.y + nay * cr;
      
      needsRefresh = true;
      child.needsRefresh = true;
      }

   // Determine a radius of a node oriented on the bissecting angle relative to parent and children
   // This is only for display purpose and as such is called by Root.createGraphics() during paint()
   // It can be assumed that calcBorders() has already been called
   // see tech. note
   
   /**
    * Determine a radius of a node oriented on the bissecting angle relative to parent and children
   	* This is only for display purpose and as such is called by Root.createGraphics() during paint()
    * It can be assumed that calcBorders() has already been called
    * see tech. note
    * @param dist
    */
   public void calcPoles(float dist){
	   if(dist == 0){
		   calcPoles(bx, by, bCross01, bCross23, 2.0f);
	      // Trick to detect borders intersections (see Tech Notes)
	      if (parent != null && child != null) {
	         bCross01 = (bx[1] * (y - by[0]) + by[1] * (bx[0] - x) + x * by[0] - y * bx[0]) >= 0.0f ? true : false;
	         bCross23 = (bx[3] * (y - by[2]) + by[3] * (bx[2] - x) + x * by[2] - y * bx[2]) <= 0.0f ? true : false;
	         //SR.write("pole = "+this.bCross01+" / "+bCross01);
	      }
	      else {
	         bCross01 = false;
	         bCross23 = false;
	         }
	   }
	   else calcPoles(px, py, pCross01, pCross23, 1 / dist);
   }
   
   
   /**
    * Determine a radius of a node oriented on the bissecting angle relative to parent and children
   	* This is only for display purpose and as such is called by Root.createGraphics() during paint()
    * It can be assumed that calcBorders() has already been called
    * see tech. note
    * @param bx
    * @param by
    * @param bCross01
    * @param bCross23
    * @param dist
    */
   public void calcPoles(float[] bx, float[] by, boolean bCross01, boolean bCross23, float dist) {
      if (!needsRefresh) return;
      float dx, dy, dxc = 0.0f, dyc = 0.0f, dxp = 0.0f, dyp = 0.0f, norm;
      if (parent == null && child == null) return;
      if (parent != null) {
         dxp = x - parent.x;
         dyp = y - parent.y;
         norm = (float) Math.sqrt(dxp * dxp + dyp * dyp);
         dxp /= norm;
         dyp /= norm;
         }
      if (child != null) {
         dxc = child.x - x;
         dyc = child.y - y;
         norm = (float) Math.sqrt(dxc * dxc + dyc * dyc);
         dxc /= norm;
         dyc /= norm;
         }
      if (parent == null) {
         dxp = dxc;
         dyp = dyc;
         }
      if (child == null) {
         dxc = dxp;
         dyc = dyp;
         }
      dx = dyp + dyc;
      dy = - (dxp + dxc);
      norm = (float) Math.sqrt(dx * dx + dy * dy);
      dx *= (diameter / (dist * norm));
      dy *= (diameter / (dist * norm));
      bx[4] = x - dx;
      by[4] = y - dy;
      bx[5] = x + dx;
      by[5] = y + dy;
      
      needsRefresh = false;
      }


   /**
    * Does the node contains the x/y coordinates
    * @param x
    * @param y
    * @return true if contains
    */
   public boolean contains(float x, float y) {
      return (Point2D.distance(this.x, this.y, x, y) < (diameter / 2.0f));
      }

   /**
    * Make a copy of the node
    * @param n
    */
   public void copy(Node n) {
      x = n.x;
      y = n.y;
      for (int i = 0; i < bx.length; i++) {
         bx[i] = n.bx[i];
         by[i] = n.by[i];
         }
      for (int i = 0; i < px.length; i++) {
    	  px[i] = n.px[i];
    	  py[i] = n.py[i];
          }
      theta = n.theta;
      length = n.length;
      cLength = n.cLength;
      diameter = n.diameter;
      if (parent != null) parent.needsRefresh = true;
      if (child != null) child.needsRefresh = true;
      needsRefresh = true;
      }

   /**
    * Move a node to a x/y position
    * @param x
    * @param y
    */
   public void move(float x, float y) {  
      this.x = x;
      this.y = y;
      needsRefresh = true;
      if (parent != null) parent.needsRefresh = true;
      if (child != null) child.needsRefresh = true;
      }
   
   /**
    * Move the node of a given x and y deviation
    * @param dx
    * @param dy
    */
   public void translate(float dx, float dy) {
      x += dx;
      y += dy;
      for (int i = 0; i < bx.length; i++) {
         bx[i] += dx;
         by[i] += dy;
         }   
      for (int i = 0; i < px.length; i++) {
          px[i] += dx;
          py[i] += dy;
          }     
      needsRefresh = true;
      }

   /**
    * Invert the direction of the node
    */
   public void invert() {
      float b = bx[0];
      float p = px[0];

      bx[0] = bx[3];
      bx[3] = b;
      b = by[0];
      by[0] = by[3];
      by[3] = b;
      b = bx[1];
      bx[1] = bx[2];
      bx[2] = b;
      b = by[1];
      by[1] = by[2];
      by[2] = b;
      b = bx[4];
      bx[4] = bx[5];
      bx[5] = b;
      b = by[4];
      by[4] = by[5];
      by[5] = b;
      
      px[0] = px[3];
      px[3] = p;
      p = py[0];
      py[0] = py[3];
      py[3] = p;
      p = px[1];
      px[1] = px[2];
      px[2] = p;
      p = py[1];
      py[1] = py[2];
      py[2] = p;
      p = px[4];
      px[4] = px[5];
      px[5] = p;
      p = py[4];
      py[4] = py[5];
      py[5] = p;
      
      if (parent != null) parent.needsRefresh = true;
      if (child != null) child.needsRefresh = true;
      needsRefresh = true;
      }

   /**
    * Get the distance to a node n from the same root
    * @param n
    * @return
    */
   public float getDistanceTo (Node n) {
      float d = 0.0f;
      for (Node n1 = this; n1 != n; n1 = n1.child) { 
         if (n1 == null) return 0.0f;
         d += (float) Point2D.distance((double) n1.x, (double) n1.y,
                                       (double) n1.child.x, (double) n1.child.y);
         }
      return d;
      }

   /**
    * Compute the longitinal pisition of the node
    */
   public void calcCLength() {
      calcCLength(this.cLength);
      }

	/**
	 * Compute the longitinal pisition of the node
	 * @param startValue
	 */
   public void calcCLength(float startValue) {
      this.cLength = startValue;
      Node n = this;
      while (n.child != null) {
         n.child.cLength = n.cLength + n.length;
         n = n.child;
         }
      }
     
   /**
    * 
    */
   public void needsRefresh() {needsRefresh = true; }
   

   /**
    * Read the node information from the datafile
    * @param parentDOM xml element containing the node
    * @param common
    */
   public void readXML(org.w3c.dom.Node parentDOM, boolean common) {
//	   if(common) readCommon(parentDOM, null);
//	   else{
	      org.w3c.dom.Node nodeDOM = parentDOM.getFirstChild();
	      while (nodeDOM != null) {
	         String nName = nodeDOM.getNodeName();
	         // Nodes that are not required elements are not considered
	         if (nName.equals("x")) x = Float.valueOf(nodeDOM.getFirstChild().getNodeValue()).floatValue();
	         if (nName.equals("y")) y = Float.valueOf(nodeDOM.getFirstChild().getNodeValue()).floatValue();
	         if (nName.equals("diameter")) diameter = Float.valueOf(nodeDOM.getFirstChild().getNodeValue()).floatValue();
	         if (nName.equals("birthTime")) birthTime = Float.valueOf(nodeDOM.getFirstChild().getNodeValue()).floatValue();
	         nodeDOM = nodeDOM.getNextSibling();
	         }
	      if (parent != null) {
	          float dx = x - parent.x;
	          float dy = y - parent.y;
	          parent.theta = NodeFitter.vectToTheta(dx, dy);
	          parent.length = (float) Math.sqrt(dx * dx + dy * dy);
	          parent.calcBorders();
	          //parent.calcPoles(0);
	         }
	      needsRefresh = true;
//	   }
      }
   
   /**
    * Read the node information from and RSML file
    * @param parentDOM the xml elemnt containg the x/y coordinates
    * @param diamDOM the xml element contining the diameter elements
    * @param dpi
    */
   public void readRSML(org.w3c.dom.Node parentDOM, org.w3c.dom.Node diamDOM, org.w3c.dom.Node pixDOM, float dpi, float scale) {
	   
		  org.w3c.dom.Node nn = parentDOM.getAttributes().getNamedItem("x");
		  if (nn != null) x = Float.valueOf(nn.getNodeValue()).floatValue() * dpi * scale;
		  nn = parentDOM.getAttributes().getNamedItem("y");
		  if (nn != null) y = Float.valueOf(nn.getNodeValue()).floatValue() * dpi * scale;
		  diameter = 0;
		  if(diamDOM != null){
			  // if node is stored as <sample value='xx'/>
			  nn = diamDOM.getAttributes().getNamedItem("value");
			  if(nn != null) diameter = Float.valueOf(nn.getNodeValue()).floatValue() * dpi;		  
			  // if node is stored as <sample>xx</sample>
			  else{
				  try{
					  diameter = Float.valueOf(diamDOM.getFirstChild().getNodeValue()).floatValue();
				  }
				  catch(Exception e){}
						  
			  }
		  }
		  if(pixDOM != null){
			  // if node is stored as <sample value='xx'/>
			  nn = pixDOM.getAttributes().getNamedItem("value");
			  if(nn != null) prevPixValue = Float.valueOf(nn.getNodeValue()).floatValue();		  
			  // if node is stored as <sample>xx</sample>
			  else{
				  try{
					  prevPixValue = Float.valueOf(pixDOM.getFirstChild().getNodeValue()).floatValue();
				  }
				  catch(Exception e){}
						  
			  }
		  }		  
	      if (parent != null) {
	          float dx = x - parent.x;
	          float dy = y - parent.y;
	          parent.theta = NodeFitter.vectToTheta(dx, dy);
	          parent.length = (float) Math.sqrt(dx * dx + dy * dy);
	          parent.calcBorders();
//	          parent.calcPoles(0);
	         }
	      needsRefresh = true;
	      }

   /**
    * Save the node coordinates to an RSML datafile
    * @param dataOut
    * @throws IOException
    */
   public void saveCoordinatesToRSML(FileWriter dataOut) throws IOException {
	      String nL = System.getProperty("line.separator");
          dataOut.write("						<point x='" + Float.toString(x) + "' y='" + Float.toString(y) + "'/>" + nL);      
	      }
  
   /**
    * Save the diameter to the RSML datafile
    * @param dataOut
    * @throws IOException
    */
   public void saveDiameterToRSML(FileWriter dataOut) throws IOException {
	      String nL = System.getProperty("line.separator");
	      dataOut.write("						<sample>" + Float.toString(diameter) + "</sample>" + nL);
	      }
   
   /**
    * Save the node orientation to the RSML file
    * @param dataOut
    * @throws IOException
    */
   public void saveOrientationToRSML(FileWriter dataOut) throws IOException {
	      String nL = System.getProperty("line.separator");
	      dataOut.write("						<sample>" + Float.toString(theta) + "</sample>" + nL);
	      } 
   
   
   /**
    * Save the node pixel value to the RSML file
    * @param dataOut
    * @throws IOException
    */
   public void savePixelToRSML(FileWriter dataOut, NodeFitter fit) throws IOException {
	      String nL = System.getProperty("line.separator");
	      dataOut.write("						<sample>" + Float.toString(fit.getValue(x, y)) + "</sample>" + nL);
	      }    
   
   /**
    * Save the anotation to the RSML file
    * @param dataOut
    * @throws IOException
    */
   public void saveAnnotationToRSML(FileWriter dataOut) throws IOException {
	      String nL = System.getProperty("line.separator");
          dataOut.write("					<annotation name='diameter'>" + nL);      
          dataOut.write("						<point x='" + Float.toString(x) + "' y='" + Float.toString(y) + "'/>" + nL);      
	      dataOut.write("						<value>" + Float.toString(diameter) + "</value>" + nL);
	      dataOut.write("						<software>smartroot</software>" + nL);
          dataOut.write("					</annotation>" + nL);      
	      }
   
   /**
    * Save the node data to an xml file
    * @param dataOut
    * @throws IOException
    */
   public void saveToXML(FileWriter dataOut) throws IOException {
      String nL = System.getProperty("line.separator");
      dataOut.write("  <Node>" + nL);
      dataOut.write("   <x>" + Float.toString(x) + "</x>" + nL);
      dataOut.write("   <y>" + Float.toString(y) + "</y>" + nL);
      dataOut.write("   <diameter>" + Float.toString(diameter) + "</diameter>" + nL);
      dataOut.write("   <birthTime>" + Float.toString(birthTime) + "</birthTime>" + nL);
      dataOut.write("  </Node>" + nL);
      }
   }


