

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

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Iterator;


/**
 * This class stores and computes root length density (RLD) profiles along a 2D grid.
 * The size of a grid element is user-provided, as well as the thickness of the rhizotron.
 * The upper left cell of the grid is aligned at the upper left position of the bounding rectangle.
 * The right column and the bottom lines of the grid may extend out of the bounding rectanle if the 
 * rectangle width and height are not integer multiples of the grid cell size.  
 * The RL profile is stored in a public 2D double array ("rld" field). The RL value in a given element
 * of the array rld[i][j] contains the RLD in the soil grid element delimited by:
 * <p>
 * along the x-axis : [xmin + i * xstep, xmin + (i + 1) * xstep[
 * <p>
 * along the y-axis : [ymin + j * ystep, xmin + (j + 1) * ystep[
 * <p>
 *  @author Xavier Draye - Universit� catholique de Louvain (Belgium) */

public class RLDProfile2D {

   public double[][]rld;
   public double xmin, ymin, xmax, ymax;
   public RLDGridSize gridsize;
   public int nx, ny;

   public RLDProfile2D() {
      super();
   }
   
   /** Compute the root length density profile (2D).
    * @param xStep The size of a grid element along the x axis (centimeters)  
    * @param yStep The size of a grid element along the y axis (centimeters)
    * @param thickness The thickness of the rhizotron (use 1.0 to get 2D densities)
    * @param bounds The origin and size of the bounding rectangle (in pixels)
    * @param pixelsize the size (centimeters) of a pixel. Pixels are assumed to be square.
   **/
   
public void computeRLP(ArrayList<Root> rootList, RLDGridSize gs, 
                          Rectangle2D bounds, double pixelSize) {

      gridsize = gs;
      double xStep = gs.gridx / pixelSize;
      double yStep = gs.gridy / pixelSize;
      xmin = bounds.getX();
      ymin = bounds.getY();
      xmax = xmin + Math.ceil(bounds.getWidth() / xStep) * xStep;
      ymax = ymin + Math.ceil(bounds.getHeight() / yStep) * yStep;
      nx = (int)((xmax - xmin) / xStep);
      ny = (int)((ymax - ymin) / yStep);

      // Must have at least one cell!
      if (nx == 0) nx++;
      if (ny == 0) ny++;
      rld = new double[nx][ny];

      Iterator<Root> rootIt = rootList.iterator();
      while(rootIt.hasNext()) {
         Root r = (Root) rootIt.next();
         Node n = r.firstNode;
         Point2D p1 = new Point2D.Double();
         Point2D p2 = new Point2D.Double();
         while(n != r.lastNode) {
            Node nc = n.child;
            p1.setLocation(n.x, n.y);
            p2.setLocation(nc.x, nc.y);
            double dist = p1.distance(p2);
            // The whole thing here is that a segment may cross several cells of the rld table!
            // So we need to detect intersections with the cell borders and split the segment
            // if required.
            // direction of the p1-p2 segment
            double dirX = Math.signum(p2.getX() - p1.getX());
            double dirY = Math.signum(p2.getY() - p1.getY());
            int counter = 0;
            while (dist > 1e-4) {
               if (counter++ == 100) {
                  SR.write("Probably looping");
               }
               // indexes (in rld array) of the cell containing node n 
               int ix = (int)Math.floor((p1.getX() - xmin) / xStep);
               int iy = (int)Math.floor((p1.getY() - ymin) / yStep);
               
               // offset of n position in its cell
               double offsetX = p1.getX() - (xmin + ix * xStep); 
               double offsetY = p1.getY() - (ymin + iy * yStep); 
               
               // (x, y) planes of the next cell border (in x, and y directions) 
               // after n in the direction n -> nc
               double nextX = 0.0, nextY = 0.0;
               if (offsetX < 1e-6) {
                  nextX = xmin + (ix + dirX) * xStep;
                  if (dirX < 0.0) ix--;
               }
               else nextX = xmin + (ix + (dirX >= 0.0 ? 1.0 : 0.0)) * xStep;

               if (offsetY < 1e-6) {
                  nextY = ymin + (iy + dirY) * yStep; 
                  if (dirY < 0.0) iy--;
               }
               else nextY = ymin + (iy + (dirY >= 0.0 ? 1.0 : 0.0)) * yStep;

               // relative position (along [p1,p2]) of the closest intersection of segment [p1,p2] with the next planes
               double lx = (dirX == 0d) ? 2d : (nextX - p1.getX()) / (p2.getX() - p1.getX()); 
               double ly = (dirY == 0d) ? 2d : (nextY - p1.getY()) / (p2.getY() - p1.getY()); 
               double l = Math.min(Math.min(lx, ly), 1d);
               // If the intersection is out of [p1,p2], then l = 1.0.

               // When p1 is very close to (and before) the next plane,
               // some numerical instabilities occur and the program loops. The following moves p1 
               // to the "right" side of the next plane.
               if (l < 1e-4) l += 1e-4; 

               // Add the length of the [p1,intersection] to the rld cell and move p1 to the intersection.
               if (ix >=0 && ix < nx && iy >=0 && iy < ny) rld[ix][iy] += l * dist;
               dist *= (1.0 - l);
               p1.setLocation(p1.getX() + l * (p2.getX() - p1.getX()),
                              p1.getY() + l * (p2.getY() - p1.getY()));
            }
            n = n.child;
         }
      }
      
      // Rescale rld to centimeters and divide by cell volume 
      double volume = gs.gridx * gs.gridy * gs.thickness;
      double f = pixelSize / volume; 
      for (int i = 0; i < nx; i++) {
         for (int j = 0; j < ny; j++) {
            rld[i][j] *= f;
         }
      }
   }
}