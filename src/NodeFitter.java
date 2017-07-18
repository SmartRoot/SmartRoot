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

import ij.*;
import ij.process.*;

/** 
 * @author Xavier Draye - Universit� catholique de Louvain
 * @author Guillaume Lobet - Universit� de Li�ge 
 * This class contains the function for the node creattion: centering, diameter estimation, orientation
 */


// %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

class NodeFitter {

   ImagePlus img;
   ImageProcessor ip;
   float dirX, dirY, theta, r, x, y, v, threshold, lastThreshold = 0.0f;
   static final float OUTSIDE_IMAGE = 1e6f;
   static final int FIND_END_SINGLETON = 1;
   static final int FIND_END_DONE = 2;
   static final int FIND_END_ABORTED = 3;
   static final int FIND_END_NOT_END = 4;
   private boolean freezeSet = false;
   private int thresholdMethod = 1;
   static int[] histogram = new int[256];
   private final float PI = (float) Math.PI;
   
   /** When the distance to the border taken orthogonaly is greater that the likelydiameter times than this factor,
       it is considered that the tracing root is crossing another root at a near 90� angle (or a fairly bigger root) */
   public float PROBABLY_CROSSING_FACTOR = 4.0f; 

   /** When the diameter of two successive nodes steps by more than this factor, it is considered that 
       two roots are merging */
   public float FORCE_SNAP_FACTOR = 1.5f; 

   /** When the diameter of two successive nodes steps by more than this factor, it is considered that 
       the size increase is transient and due to some background problem. The correction induced is prevented
       to be used on the next node */
   public float FORCE_FREEZE_FACTOR = 1.2f; 

   /** The adaptive threshold is determined within a distance to the center pixel equal to the 
       node diameter times this factor */
   public float ADAPTIVE_THRESHOLD_RADIUS_FACTOR = 2.0f;   // 1.5f
   
   /** Default threshold radius (used for the first mouseclick of a root */
   public int DEFAULT_ADAPTIVE_THRESHOLD_RADIUS = 20;

   /** Minimum threshold */
   public int MIN_ADAPTIVE_THRESHOLD_RADIUS = 4;

   static final int REGULAR_TRACING = 1;
   static final int LATERAL_TRACING = 2;
   static final int LINE_TRACING = 3;
   
   /**
    * Constructor. Connects the current image
    * @param img
    */
   public NodeFitter(ImagePlus img) {
      this.img = img;
      }
      
   /**
    * Detach the  image
    */
   public void detach() {this.img = null;} 
   
   /** Force the NodeFitter object to get the current ImageProcessor. This method was introduced to 
       allow the execution of ImageJ commands or plugins on RootImageCanvas attached to a RootModel,
       because such commands may replace the ImageProcessor and do not send events when they do.
       This method should be called by any object that intends to call the NodeFitter.getValue(int, int) 
       method to make sure the NodeFitter will use the right ImageProcessor. 
       */
   public void checkImageProcessor() {
      ip = img.getProcessor();
      }

   /**
    * Compute the adaptive threshold to build the next node in the root
    * @param x
    * @param y
    * @param r
    */
   public void calcAdaptiveThreshold(int x, int y, int r) {
      checkImageProcessor();
      
      // Reset the histogram and make sure r is big enough
      for (int i = 0; i < 256; histogram[i++] = 0);
      if (r < MIN_ADAPTIVE_THRESHOLD_RADIUS) 
         r = MIN_ADAPTIVE_THRESHOLD_RADIUS; 

      // Find the intersection between the image and a square of size 2*r centered at x, y
      int xo = (x - r) < 0 ? 0 : x - r;
      int l = ((x + r) > ip.getWidth() ? ip.getWidth() : x + r) - xo;
      int yo = (y - r) < 0 ? 0 : y - r;
      int ye = (y + r) > ip.getHeight() ? ip.getHeight() : y + r;

      // Build the histogram
      int[] p = new int[2 * r + 1];
      for (int j = yo; j < ye; j++) {
         ip.getRow(xo, j, p, l);
         for (int i = 0; i < l; histogram[p[i++]]++);
         }

      // Compute the min, max and some percentile
      int npct = (int) ((l * (ye - yo)) * 0.5f);
      int min, max, pct, count = 0;
      for (min = 0; histogram[min] == 0; min++);
      for (max = 255; histogram[max] == 0; max--);
      for (pct = min; count < npct; count += histogram[pct++]); 
      
      // The idea here is to keep the current threshold if we are in the background or in a root bigger than r
//      if (max - min < 10) return;    

      if (thresholdMethod == 2) {
         threshold = max - 20;
         return;
         }

      // Compute the threshold
      // This function reduces the influence of very dark pixels around the pixel under focus.
      // This happens e.g. when a faint root comes very close to a dark root/dirt.
      threshold = (max + (ip.getPixel(x, y) + min) / 2.0f) / 2.0f;
      SR.log("pre-threshold " + threshold);
      if ((float)(pct - min) / (float)(max - min) < 0.20f) 
         threshold = (1.2f * threshold + 0.8f * pct) / 2f;
         // threshold = min + 3.5f * (pct - min);

      SR.log("threshold at: " + x + " " + y + " radius " + r + " -> value:" + ip.getPixel(x, y) + " min: " + min 
                        + " max: " + max + " pct: " + pct + " threshold: " + threshold);
      }
      
   /**
    *    
    */
   public void clearThresholdMemory() {lastThreshold = 0.0f; }
   
   
   
   /**
 	 * Automatically recenters a root node using the behavior defined by flag. ThetaStep and rStep specify
       respectively the angle and radius step for border search. Narrow = false forces the search
       to be done on a 360 degrees circle around the node, while narrow = true forces the search to be restricted to
       a 180 degrees arc around the direction of the line joining the previous/next node to the node to be recentered.
 	 * @param n
 	 * @param thetaStep
 	 * @param rStep
 	 * @param narrow
 	 * @param flag
 	 * @return
 	 */
   @SuppressWarnings("unused")
 	public int reCenter (Node n, float thetaStep, float rStep, boolean narrow, int flag) {
      boolean snapBorder = ((flag & RootModel.SNAP_TO_BORDER) != 0);
      boolean freezeDiameter = ((flag & RootModel.FREEZE_DIAMETER) != 0);
      boolean autoTrace = ((flag & RootModel.AUTO_TRACE) != 0);
      boolean probablyCrossing = false;
      
      int returnCode = 0;
   
      int nTheta;
      float likelyRadius = 0.0f;
      SR.log("reCenter on " + n.x + " " + n.y);      
      if (n.parent == null && n.child == null) {
         theta = 0;
         nTheta = (int) Math.round(Math.PI / thetaStep);
         narrow = false;
         calcAdaptiveThreshold((int) n.x, (int) n.y, DEFAULT_ADAPTIVE_THRESHOLD_RADIUS); 
         } 
      else {    
         if (n.parent != null) {
            x = n.x - n.parent.x;
            y = n.y - n.parent.y;
            likelyRadius = n.parent.diameter / 2.0f;
            // if (n.parent.threshold == 0.0f) calcAdaptiveThreshold(n.parent);
            calcAdaptiveThreshold((int) n.x, (int) n.y, (int) Math.ceil(ADAPTIVE_THRESHOLD_RADIUS_FACTOR * likelyRadius));
            // if (Math.abs(threshold - n.parent.threshold) > 15.0) threshold = n.parent.threshold;
            }
         else {
            x = n.child.x - n.x;
            y = n.child.y - n.y;
            likelyRadius = n.child.diameter / 2.0f;
            // if (n.child.threshold == 0.0f) calcAdaptiveThreshold(n.child);
            calcAdaptiveThreshold((int) n.x, (int) n.y, (int) Math.ceil(ADAPTIVE_THRESHOLD_RADIUS_FACTOR * likelyRadius));
            // if (Math.abs(threshold - n.child.threshold) > 15.0) threshold = n.child.threshold;
            }
         theta = vectToTheta(x, y) + PI / 4;
         if (theta > 2.0f * PI) theta -= 2.0f * PI;
         nTheta = (int) Math.round(PI / (2 * thetaStep));
         narrow = true;
         if(nTheta == 0) return 1; 

         if (autoTrace) {
            if ((lastThreshold - threshold > 20.0f)) {
    //           SR.write("Freezing threshold " + threshold + " -> " + lastThreshold);
               float t = threshold;
               threshold = lastThreshold;
               lastThreshold = t;
               }
            else lastThreshold = threshold;
            }

         }

      // Find root border and calculate distance to border along nTheta directions (from theta by thetaStep)
      int i, minI = 0, minI1 = 0, minI2 = 0;
      float minT = 0f, minT1 = 0f, minT2 = 0f, orthoL = 0f;
      float[] l = new float[nTheta];
      float[] l1 = new float[nTheta];
      float[] l2 = new float[nTheta];
      float[] x1 = new float[nTheta];
      float[] y1 = new float[nTheta];
      float[] x2 = new float[nTheta];
      float[] y2 = new float[nTheta];
     
      for (i = 0; i<nTheta; i++) {
         l1[i] = seekBorder(n.x, n.y, theta, rStep);
         x1[i] = x; y1[i] = y; 
         l2[i] = seekBorder(n.x, n.y, theta + PI, rStep);
         x2[i] = x; y2[i] = y;
         l[i] = l1[i] + l2[i];
         theta += thetaStep;
         if (l[minI] >= l[i]) {
             minI = i;
             minT = theta;
             }
         if (l1[minI1] >= l1[i]) {
             minI1 = i;
             minT1 = theta;
             }
         if (l2[minI2] >= l2[i]) {
             minI2 = i;
             minT2 = theta + PI;
             }
         }

//      int orth = nTheta / 2;
//      maxL = l1[orth] < l2[orth] ? l1[orth] : l2[orth];
      orthoL = l[nTheta / 2];
         
      // the following is still not properly handled
      if (!narrow && l[minI] >= OUTSIDE_IMAGE ||
          narrow && (l1[minI1] + l2[minI2] >= OUTSIDE_IMAGE)) {
          return 1;
          }  
      
      if (!narrow) minI1 = minI2 = minI;

      SR.log("reCenter l[minI]: " + l[minI] + " likelyradius " + likelyRadius);

      // This block will switch snapBorder & freezediameter on if there is a too large diameter increase
      // ex: when two roots "join" each other
      // The reCentering will then move the node to the closest border, considering the likelyRadius
      // This runs great on SCRI images!!
      // This operation is done only if requested to do so (ex: auto trace)
      // FreezeDiameter alone is expected to be transient (only one)
   
      if (autoTrace && (likelyRadius != 0.0f)) {
//SR.write("orthoL " + orthoL + " likelyRadius " + likelyRadius);
         if (l[minI] > FORCE_SNAP_FACTOR * 2.0f * likelyRadius) 
            snapBorder = true;
         if (l[minI] > FORCE_FREEZE_FACTOR * 2.0f * likelyRadius) {
            freezeDiameter = !freezeSet || snapBorder;
            freezeSet = freezeDiameter && !snapBorder;
            }
         if (orthoL > PROBABLY_CROSSING_FACTOR * 2.0f * likelyRadius) {
           // SR.write("Probably crossing near " + n.x + " " + n.y);
            snapBorder = false;
            freezeDiameter = true;
            probablyCrossing = true;
            freezeSet = false;
            returnCode = 2;
/*
            // Adjust the direction of the segment based on the curvature of the root in the last two segments
            if (n.parent.parent != null && n.parent.parent.parent != null) {
               Node npp = np.parent;
               Node nppp = npp.parent;
               float length = (float) Point2D.distance(n.x, n.y, np.x, np.y);
               n.x += ((np.x - npp.x)/npp.length - (npp.x - nppp.x)/nppp.length) * length;
               n.y += ((np.y - npp.y)/npp.length - (npp.y - nppp.y)/nppp.length) * length;
               }
*/
/*
            // Try moving the node one diameter ahead of the cross
            float dx = n.x - np.x;
            float dy = n.y - np.y;
            float norm = norm(dx, dy);
            dx /= norm;
            dy /= norm;
            theta = vectToTheta(dx, dy);
            int border = (int)seekBorder(np.x, np.y, theta, 2.0f);
            // if (border < norm) we have a problem
            float xTest, yTest;
            theta = theta + PI / 2.0f;
            float thetaSym = theta + PI;
            float pc = PROBABLY_CROSSING_FACTOR * 2.0f * likelyRadius;
            for (float dist = 2.0f; dist < border; dist += 2.0f) {   // seek the cross region
               if (seekBorder(xTest, xTest, theta, 2.0f) +
                   seekBorder(xTest, xTest, thetaSym, 2.0f) > pc) {
                  break;   
                  }
               }
            if (dist < border)
               if 
            if (seekBorder(xTest, xTest, , rStep) > 2.0f * n.parent.diameter) {
               // n.x += 
               theta = vectToTheta(n.x - np.x, n.y - np.y) + PI / 2;
               l1[minI1] = seekBorder(n.x, n.y, theta, rStep);
               x1[minI1] = x; y1[minI1] = y; 
               l2[minI2] = seekBorder(n.x, n.y, theta + PI, rStep);
               x2[minI2] = x; y2[minI2] = y;
               }
            // scan in that direction from the last node (halving step each time) - till when? not easy
            // how can I deal with a small root running parallel to a big root (orthoL would tell is crossing)
*/
            }
         }
      else freezeSet = false;

//      SR.write("SnapBorder -> " + snapBorder);
//      SR.write("FreezeDiameter -> " + freezeDiameter);
      SR.log("AutoTrace -> " + autoTrace);

      // re-center Node
      float b1x, b1y, b2x, b2y;
      if (!snapBorder) {
         if ((autoTrace || !freezeDiameter) && !probablyCrossing) {
            n.x = (x1[minI1] + x2[minI2]) / 2;
            n.y = (y1[minI1] + y2[minI2]) / 2;
            }
         x = x2[minI2] - x1[minI1];
         y = y2[minI2] - y1[minI1];
         float d = (float) Math.sqrt(x * x + y * y);
         if (freezeDiameter && likelyRadius != 0.0f) {
            x /= d;
            y /= d;
            b1x = n.x - x * likelyRadius;
            b1y = n.y - y * likelyRadius;
            b2x = n.x + x * likelyRadius;
            b2y = n.y + y * likelyRadius;
            n.diameter = 2.0f * likelyRadius;
            }
         else {
            n.diameter = d;
            b1x = x1[minI1];
            b1y = y1[minI1];
            b2x = x2[minI2];
            b2y = y2[minI2];
            }
      }
      
      else {    // (snapBorder == true)
         // When possible, take account of the curvature of the root in the last two segments
/*
         if (n.parent != null && n.parent.parent != null && n.parent.parent.parent != null) {
         //SR.write("Correcting " + n.x + " " + n.y + " " + l1[minI1] + " " + l2[minI2]);
            Node np = n.parent;
            Node npp = np.parent;
            Node nppp = npp.parent;
            float length = (float) Point2D.distance(n.x, n.y, np.x, np.y);
            n.x += ((np.x - npp.x)/npp.length - (npp.x - nppp.x)/nppp.length) * length;
            n.y += ((np.y - npp.y)/npp.length - (npp.y - nppp.y)/nppp.length) * length;
            theta = vectToTheta(n.x - np.x, n.y - np.y) + (float) Math.PI / 2;
            l1[minI1] = seekBorder(n.x, n.y, theta, rStep);
            x1[minI1] = x; y1[minI1] = y; 
            l2[minI2] = seekBorder(n.x, n.y, theta + (float) Math.PI, rStep);
            x2[minI2] = x; y2[minI2] = y;
         //SR.write("-> " + n.x + " " + n.y + " " + l1[minI1] + " " + l2[minI2]);
            }
*/
         if (l1[minI1] > l2[minI2]) {
            b2x = x2[minI2];
            b2y = y2[minI2];
            x = n.x - b2x;
            y = n.y - b2y;
            r = (float) Math.sqrt(x * x + y * y);
            n.diameter = (freezeDiameter && likelyRadius != 0.0f)
                            ? 2.0f * likelyRadius
                            : 2.0f * r;
            b1x = b2x + n.diameter * x / r;
            b1y = b2y + n.diameter * y / r;
            n.x = (b1x + b2x) / 2.0f;
            n.y = (b1y + b2y) / 2.0f;
            }
         else {
            b1x = x1[minI1];
            b1y = y1[minI1];
            x = n.x - b1x;
            y = n.y - b1y;
            r = (float) Math.sqrt(x * x + y * y);
            n.diameter = (freezeDiameter && likelyRadius != 0.0f)
                            ? 2.0f * likelyRadius
                            : 2.0f * r;
            b2x = b1x + n.diameter * x / r;
            b2y = b1y + n.diameter * y / r;
            n.x = (b1x + b2x) / 2.0f;
            n.y = (b1y + b2y) / 2.0f;
            }
         }
         
      
      // check the distance between the node and its parent node
      // If the distance is smaller than the radius of the root, it means the node is inside the previous one
      n.buildNode();
      
      
      
      // this method replaces the following block

/*

      if (n.diameter < 1.0f) n.diameter = 1.0f;

      // calculate length and theta where required
      if (n.parent != null) {
         x = n.x - n.parent.x;
         y = n.y - n.parent.y;
         n.parent.theta = vectToTheta(x, y);
         n.parent.length = (float) Math.sqrt(x * x + y * y);
         }
      if (n.child != null) {
         x = n.child.x - n.x;
         y = n.child.y - n.y;
         n.theta = vectToTheta(x, y);
         n.length = (float) Math.sqrt(x * x + y * y);
         }

      
      // adjust the diameter
      // The method is still unsatisfactory when moving the selected node, if parent-node-child are making a sharp angle.
      // see technical note
      if (!snapBorder && !freezeDiameter && !(n.child == null && n.parent == null)) {
         x = y = 0.0f;
         if (n.parent != null) {
            x = -(n.y - n.parent.y) / n.parent.length; 
            y = (n.x - n.parent.x) / n.parent.length;
            }
         if (n.child != null) {
            x += -(n.child.y - n.y) / n.length; 
            y += (n.child.x - n.x) / n.length;
            }
         r = (float) Math.sqrt(x * x + y * y);
         n.diameter = (float) Math.abs((x * (b1x - b2x) + y * (b1y - b2y)) / r); 
         }

      // calculate poles and borders
      n.needsRefresh = true;
      if (n.parent != null) n.parent.calcBorders();
      if (n.child != null) n.calcBorders();

//      n.threshold = threshold;
*/


SR.log("reCenter Done :" + n.x + " " + n.y + " " + n.diameter);


      return returnCode;
      }

   /**
    * Find the end of the root based on the node diameter differences. The end will be defined as the side of root
    * with a decreasing diameter
    * @param n
    * @param endNode
    * @return
    */
   public int findEnd (Node n, Node endNode) {
      // Init
	  SR.log("findEnd 1");
      if (n.parent == null) return FIND_END_SINGLETON;

      calcAdaptiveThreshold((int) n.x, (int) n.y, (int) Math.ceil(ADAPTIVE_THRESHOLD_RADIUS_FACTOR * n.diameter / 2.0f));

      x = n.x - n.parent.x;
      y = n.y - n.parent.y;
      float thetaStep = 0.02f;
      float theta = vectToTheta(x, y) - PI / 3;
      if (theta > 2.0f * (float) Math.PI) theta -= 2.0f * PI;
      int nTheta = (int) Math.round(2 * PI / (3 * thetaStep));

      // Calculate distance to background in all directions
      float maxL = 0.0f, x1 = 0.0f, y1 = 0.0f;
      for (int i = 0; i < nTheta; i++) {
         float l = seekBorder(n.x, n.y, theta, 0.2f);
         if (l > maxL) {
            maxL = l;
            x1 = x;
            y1 = y; 
            }
         theta += thetaStep;
         }

      x = x1 - n.x;
      y = y1 - n.y;
      float d = (float) Math.sqrt(x * x + y * y);
SR.log("maxL: " + maxL + " at " + x1 + " " + y1 + " dist = " + d);

      if (maxL < n.parent.diameter/2) {
         return FIND_END_ABORTED;
         }

      if (maxL <= (2.0f * n.diameter)) {
         // fit endNode; 
         endNode.diameter = 1.0f; // arbitrary, was 0.0f until 20040204.
         endNode.x = x1;
         endNode.y = y1;
         // calculate length and theta where required
         n.theta = vectToTheta(x, y);
         n.length = d;
         n.calcBorders();
         SR.log("findEnd Done");
         return FIND_END_DONE;
         }
      else{
         endNode.x = n.x + x * n.diameter / d;
         endNode.y = n.y + y * n.diameter / d;
         SR.log("FindEnd Try recenter");
         if (reCenter(endNode, 0.1f, 0.5f, true, 0) != 0) 
            return FIND_END_ABORTED;
         SR.log("findEnd not end");
         return FIND_END_NOT_END;
         }      
       }

   /**
    * Suggest a position for the node
    * @param suggestedNode
    * @param n
    */
   public void suggest(Node suggestedNode, Node n){
	   suggest(suggestedNode, n, null, 1, false, 0);
   }
   
   /**
    * Suggest a position for the node
    * @param suggestedNode
    * @param n
    * @param p
    * @param type
    * @param lr
    * @param tmax
    */
   public void suggest(Node suggestedNode, Node n, Node p, int type, boolean lr, float tmax) {
	  calcAdaptiveThreshold((int) n.x, (int) n.y, (int) Math.ceil(ADAPTIVE_THRESHOLD_RADIUS_FACTOR * n.diameter / 2.0f));
      float thetaStep = 0.1f;
      float tIns = 0;
      int nTheta = (int) (2.0f * PI / thetaStep);
      theta = 0;
      // Calculate distance to background in all directions
      float maxL = 0.0f, x1 = 0.0f, y1 = 0.0f;
      for (int i = 0; i < nTheta; i++) {
         float l = seekBorder(n.x, n.y, theta, 0.2f);
         
         if ((type & LATERAL_TRACING) != 0) tIns = setInsertAng(p.theta, theta, lr);
         
         if ((type & REGULAR_TRACING) != 0 && l != OUTSIDE_IMAGE && l > maxL) {
            maxL = l;
            x1 = x;
            y1 = y; 
            }
         if ((type & LATERAL_TRACING) != 0 && l != OUTSIDE_IMAGE && l > maxL && tIns < tmax) {
             maxL = l;
             x1 = x;
             y1 = y; 
             }
         theta += thetaStep;
         }
      float d = norm(n.x, n.y, x1, y1);
      suggestedNode.x = n.x + n.diameter * (x1 - n.x) / d;
      suggestedNode.y = n.y + n.diameter * (y1 - n.y) / d;
      }

   /**
    * Set the insertion angle (in radian) between the two nodes
    * @param t1 parent node
    * @param t2 child node
    * @param lr side of the parent root on which the new root is create: true if left, false if right
    * @return the insertion angle
    */
   public float setInsertAng(float t1, float t2, boolean lr){
	   float tIns = 0;
	   
	   if (lr) {
		   tIns = t1 - t2;
		   if(t1 < t2) tIns = ((float)Math.PI * 2) + tIns;
	   }
	   else{
		   tIns = t2 - t1;
		   if (t1 > t2) tIns = ((float)Math.PI * 2) + tIns;
	   }
	   return tIns;
   }

   /**
    * Get the border of the root at the node location.
    */
   public float seekBorder (float originX, float originY, float theta, float rStep) {
      checkImageProcessor();
      dirX = (float) Math.cos(theta) * rStep;
      dirY = (float) -Math.sin(theta) * rStep;
      x = originX;
      y = originY;
      r = 0;
      while ((v = getValue(x, y)) < threshold) {
         x += dirX;
         y += dirY;
         r += rStep;
         }
// SR.write("Seek Border end: " + r + " -> " + v);
//      if (v == 256.0f) return OUTSIDE_IMAGE;
      if (v == 256.0f) {
         r -= rStep;
         x -= dirX;
         y -= dirY;
         }
      else {
         r -= rStep / 2.0f;
         x -= dirX / 2.0f;
         y -= dirY / 2.0f;
         }
// SR.write("Theta:" + theta + " r:" + r + " v:" + v + " tresh:" + threshold);
      return r;
      }


   /**
    * Suggest a position for the next node
    * @param n1
    * @param length
    * @return
    */
   public float suggestNextNode(Node n1, float length) {
      float theta = n1.parent.theta - PI / 6.0f;
      float thetaStep = (float)(1f / length);
      int nStep = (int)(PI / (3.0 * thetaStep));
      float[] param;
      float minTheta = 0.0f;
      float minSlope = 10.0e4f;
      // Step1: look in front (just a few points, say 3pixels left & right) and estimate slope
      // From slope, choose scan direction (go on left OR right) until reaching a reasonable minimum.
      // Would use node fitter for large roots (more than 5 pixels, or if we can have a reasonable
      // estimate of the presence of a plateau in the valley) where centering is necessary
      // WOuld use a node correction (part of node fitter) to track border if diameter increase
      
      // Use Plot Profile to show (or write data to the Result window to export to excel) the trans-profile

//      SR.write("New node: x:" + n1.x + "   y:" + n1.y + " length:" + length);

      for (int i = 0; i <= nStep; i++, theta += thetaStep) {
         param = getSegmentRegression(n1, (float)Math.cos(theta), (float)-Math.sin(theta), length);
         if (param[1] < minSlope) {
            minTheta = theta;
            minSlope = param[1];
            }
         }
      return minTheta;
      }


   /**
    * 
    * @param n
    * @param dx
    * @param dy
    * @param length
    * @return
    */
   public float[] getSegmentRegression(Node n, float dx, float dy, float length) {
      float norm = norm(dx, dy);
      dx /= norm;
      dy /= norm;
      float x = 0f;
      float y = 0f;
      int nStep = (int)(length);
      float[] param = new float[3];
      float eX = 0f;
      float eY = 0f;
      float eXX = 0f;
      float eYY = 0f;
      float eXY = 0f;
      for (int i = 0; i < nStep; i++, x += dx, y += dy) {
         float v = getValue(n.x + x, n.y + y);
         eX += i;
         eXX += i * i;
         eY += v;
         eYY += v * v;
         eXY += v * i;
         }
      param[2] = (float)(eYY - eY * eY / (float)nStep);
      param[1] = (float)((eXY - eX * eY / (float)nStep) / (eXX - eX * eX / (float)nStep));
      param[0] = (float)((eY - param[1] * eX) / (float)nStep);
//      SR.write("Intercept: " + param[0] + " slope: " + param[1] + " V(Y): " + param[2]);
      return param;
      }  
   
   /**
    * Get the pixel profile ahead of the node. The profile is computed along a circle around the node
    * @param n
    * @param stepFactor
    * @param resolution
    * @return
    */
   public float[] getProfileAhead(Node n, float stepFactor, float resolution) {
      if (n.parent == null) return null;
      float theta = n.parent.theta - PI / 2.0f;
      float r = stepFactor * n.diameter;
      float thetaStep = resolution / r;
      int nStep = (int)(PI / thetaStep);
      float[] profile = new float[nStep];
      for (int i = 0; i < nStep; i++, theta += thetaStep) {
         float dx = (float)Math.cos(theta);
         float dy = (float)Math.sin(theta);
         profile[i] = (getValue(n.x + (r + 1) * dx, n.y - (r + 1) * dy)
                       + getValue(n.x + (r - 1f) * dx, n.y - (r - 1f) * dy)) / 2.0f;
         }
      return profile;
      }  
   
   /**
    * 
    * @param n
    * @param tail
    * @param resolution
    * @return
    */
   public float getExpectedProfile(Node n, float[] tail, float resolution) {
      // It is assumed n is centered!
      float dirX = resolution * (n.parent.y - n.y) / n.parent.length;
      float dirY = resolution * (n.x - n.parent.x) / n.parent.length;
      float x = n.x;
      float y = n.y;
      float dx = dirX;
      float dy = dirY;
      float prevValue = (float) ip.getInterpolatedPixel(x, y);
      float avg = prevValue;
      int nAvg = 1;
      int end = (int) (n.diameter / 2.0f) + 3;
      int inTail = 1;
      for (int i = 0; i < end && inTail < tail.length; i += resolution) {
         float value1 = (float) ip.getInterpolatedPixel(x + dx, y + dy);
         float value2 = (float) ip.getInterpolatedPixel(x - dx, y - dy);
         float value = value1 > value2 ? value1 : value2;
         if (inTail == 1 && Math.abs(value - prevValue) < 10.0 * resolution) {    // in the center of the profile
            avg += value;
            nAvg++;
            }
         else {                 // in the Tail of the profile
            tail[inTail] = value;
            inTail++;
            }
         dx += dirX;
         dy += dirY;
         prevValue = value;
         }
      tail[0] = avg / nAvg;
      return (float) (nAvg * 2 - 1);
      }

   /**
    * Get the pixel value at the x / y position
    * @param x
    * @param y
    * @return
    */
   public float getValue(float x, float y) {
      return (x >= 0 && y >= 0 && x < ip.getWidth() && y < ip.getHeight()) 
             ? (float) ip.getInterpolatedPixel((double) x, (double) y)
             : 256.0f;
      }

   /**
    * 
    * @param from
    * @param to
    * @return
    */
   public static float vectToTheta(Node from, Node to) {
      return vectToTheta(to.x - from.x, to.y - from.y);
      }

   /**
    * 
    * @param dirX
    * @param dirY
    * @return
    */
   public static float vectToTheta (float dirX, float dirY) {
      float norm = (float) Math.sqrt(dirX * dirX + dirY * dirY);
//      float a = (float) (dirY <= 0 ? Math.acos(dirX / norm) 
//              : 2.0 * Math.PI - Math.acos(dirX / norm));
//      SR.write(""+a);
      return (float) (dirY <= 0 ? Math.acos(dirX / norm) 
              : 2.0 * Math.PI - Math.acos(dirX / norm));      

      }

   /**
    * 
    * @param v
    * @return
    */
   public float sqr(float v) {
      return v * v; 
      }
 
   /**
    * 
    * @param dx
    * @param dy
    * @return
    */
   public static float norm(float dx, float dy) {
      return (float) Math.sqrt(dx * dx + dy * dy); 
      }
      
   /**
    * 
    * @param x0
    * @param y0
    * @param x1
    * @param y1
    * @return
    */
   public static float norm(float x0, float y0, float x1, float y1) {
      return norm(x1 - x0, y1 - y0); 
      }
   
   /**
    * 
    * @param method
    */
   public void setThresholdMethod(int method) {
      thresholdMethod = method;
      }

   }


