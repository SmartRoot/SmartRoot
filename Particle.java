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


import ij.gui.PolygonRoi;
import ij.gui.Roi;
import ij.gui.Wand;
import ij.measure.Measurements;
import ij.process.ByteStatistics;
import ij.process.ImageProcessor;
import ij.process.ImageStatistics;
import ij.process.PolygonFiller;

import java.awt.Rectangle;

/* Created on Apr 28, 2008 */
/** @author Xavier Draye - Universit� catholique de Louvain (Belgium) */

public class Particle {

   double xcm, ycm, area, diameter, perimeter;
   Root root;
   int gap, match, x, y;
   Rectangle bounds;
   PolygonRoi roi;
   ImageProcessor mask;
   ImageStatistics stat;
   static ImageProcessor ip;
   static Wand wand = null;
   static PolygonFiller pf = new PolygonFiller();

   
   public Particle(int x, int y, int level1, int level2, ImageProcessor ipr) {
      if (ipr != ip) {
         ip = ipr;
         wand = new Wand(ip);
      }
      wand.autoOutline(x, y, level1, level2);    
      pf.setPolygon(wand.xpoints, wand.ypoints, wand.npoints);
      roi = new PolygonRoi(wand.xpoints, wand.ypoints, wand.npoints, Roi.TRACED_ROI);
      perimeter = roi.getLength();
      diameter = roi.getFeretsDiameter();
      bounds = roi.getBounds();
      mask = pf.getMask(bounds.width, bounds.height);
      ip.setMask(mask);
      ip.setRoi(bounds);
      stat = new ByteStatistics(ip, Measurements.AREA + Measurements.CENTER_OF_MASS /* + Measurements.PERIMETER */, null);
      xcm = stat.xCenterOfMass;
      ycm = stat.yCenterOfMass;
      area = stat.area;
   }
   
   public ImageProcessor getMask() {return mask;}
   
   public ImageStatistics getStatistics() {return stat;}
   
   public Rectangle getBounds() {return bounds;}
   
}
