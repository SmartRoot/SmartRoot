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
  
/** @author Xavier Draye - Universit� catholique de Louvain */

// %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

class OrthogonalLine {
   public float x0, y0, xSect, ySect, a0, a1, a2, dx, dy;
   public Node n1, n2;
   public Line2D.Float line;
   public Point2D.Float point;
   private boolean intersectionComputed = false;

   /**
    * Constructor
    */
   public OrthogonalLine() {
      line = new Line2D.Float();
      point = new Point2D.Float();
      }

   /**
    * Constructor
    * @param x
    * @param y
    * @param n1
    * @param n2
    */
   public OrthogonalLine(float x, float y, Node n1, Node n2) {
      line = new Line2D.Float();
      point = new Point2D.Float();
      setMouseAndNodes(x, y, n1, n2);
      }

   /**
    * 
    * @param x0
    * @param y0
    * @param n1
    * @param n2
    */
   public void setMouseAndNodes(float x0, float y0, Node n1, Node n2) {
      this.x0 = x0;
      this.y0 = y0;
      this.n1 = n1;
      this.n2 = n2;
      
      // (dx, dy) is the direction orthogonal to the searched line
      dx = n2.x - n1.x;
      dy = n2.y - n1.y;
      // the orthogonal line is the set of points whose scalar product with 
      // the vector (dx, dy) equals zero
      a0 = dx;
      a1 = dy;
      a2 = - (dx * x0 + dy * y0);
      intersectionComputed = false;
      };

   /**
    * 
    */
   private void calcIntersection() {
      // the line containing n1 and n2 is the set of points whose scalar product with 
      // the vector (dy, -dx) equals zero
      float b0 = dy;
      float b1 = -dx;
      float b2 = -(dy * n1.x - dx * n1.y);
      // the intersection is the solution to a system of 2 eqns
      float d = a0 * b1 - b0 * a1;
      xSect = (-a2 * b1 + b2 * a1) / d;
      ySect = (-a0 * b2 + b0 * a2) / d;
      intersectionComputed = true;
      }
   
   /**
    * 
    * @return
    */
   public float getXSect() {
      if (!intersectionComputed) calcIntersection();
      return xSect;
      }

   /**
    * 
    * @return
    */
   public float getYSect() {
      if (!intersectionComputed) calcIntersection();
      return ySect;
      }

   /**
    * 
    * @return
    */
   public boolean intersects() {
      float f1 = a0 * n1.x + a1 * n1.y + a2;
      float f2 = a0 * n2.x + a1 * n2.y + a2;
      return ((f1 * f2) <= 0.0f);
      }

   /**
    * 
    * @return
    */
   public float getDiameter() {
      // eqn of line joining borders 1 of n1 and n2
      float b0 = -(n2.by[0] - n1.by[1]);
      float b1 = n2.bx[0] - n1.bx[1];
      float b2 = n1.bx[1] * (-b0) - n1.by[1] * b1;
      // intersection with orthLine
      float d = a0 * b1 - b0 * a1;
      float bx = (-a2 * b1 + b2 * a1) / d;
      float by = (-a0 * b2 + b0 * a2) / d;

      // eqn of line joining borders 2 of n1 and n2
      b0 = -(n2.by[2] - n1.by[3]);
      b1 = n2.bx[2] - n1.bx[3];
      b2 = n1.bx[3] * (-b0) - n1.by[3] * b1;
      // intersection with orthLine
      d = a0 * b1 - b0 * a1;
      bx -= (-a2 * b1 + b2 * a1) / d;
      by -= (-a0 * b2 + b0 * a2) / d;

      return (float) Math.sqrt(bx * bx + by * by);
      }

   /**
    * 
    * @return
    */
   public Line2D.Float getRulerLine() {
      if (!intersectionComputed) calcIntersection();
      float d = getDiameter();
      float s = (float) Math.sqrt(dx * dx + dy * dy);
      line.setLine(xSect - (d * dy / s), ySect + (d * dx / s),
                   xSect + (d * dy / s), ySect - (d * dx / s));
      return line;
      }

   /**
    * 
    * @param dummy
    * @return
    */
   public Point2D.Float getRulerTextLocation(int dummy) {
      if (!intersectionComputed) calcIntersection();
      if ((dy * (x0 - xSect) + dx * (y0 - ySect)) > 0) 
         point.setLocation(xSect + dy, ySect - dx);      
      else
         point.setLocation(xSect - dy, ySect + dx);
      return point;
      }      

   /**
    * 
    * @return
    */
   public Point2D.Float getRulerTextLocation() {
      point.setLocation(x0, y0);      
      return point;
      }      

   /**
    * 
    * @return
    */
   public Line2D.Float getLine() {
      return line; 
      }

   /**
    * 
    * @return
    */
   public Point2D.Float getPoint() {
      return point; 
      }
   }
