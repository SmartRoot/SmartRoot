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

import java.awt.*;
import java.awt.geom.*;
import java.io.*;

/** @author Xavier Draye - Universit� catholique de Louvain 
 * Registration anchors are used to register successive images.
 * Should be place on the same object in every image
 * */


// %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

class RegistrationAnchor {
   private Point2D.Float p = new Point2D.Float();
   private GeneralPath gp = new GeneralPath();
   private float size = 10.0f;
   private int number;
   static Color[] color = new Color[4];
   
   /**
    * Constructor
    * @param x
    * @param y
    * @param n
    */
   public RegistrationAnchor(float x, float y, int n) {
      p.setLocation(x, y);
      createGraphics();
      number = n;
      init();
      }
      
   /**
    * Constructor
    * @param parentDOM
    * @param n
    */
   public RegistrationAnchor(org.w3c.dom.Node parentDOM, int n) {
      read(parentDOM);
      number = n;
      init();
      }
      
   /**
    * Create the graphcis
    */
   private void createGraphics() {
      gp.reset();
      gp.moveTo(p.x - size, p.y);
      gp.lineTo(p.x + size, p.y);
      gp.moveTo(p.x, p.y - size);
      gp.lineTo(p.x, p.y + size);
      }
   
   /**
    * 
    * @return
    */
   public Point2D.Float getPoint() {return p; }

   /**
    * 
    * @return
    */
   public float getX() {return p.x; }

   /**
    * 
    * @return
    */
   public float getY() {return p.y; }

   /**
    * Initialise the anchors
    */
   private void init() {
      if (color[0] != null) return;
      color[0] = Color.magenta;
      color[1] = Color.blue;
      color[2] = Color.cyan;
      color[3] = Color.gray;
      }

   /**
    * Display the anchors on the image
    */
   public void paint(Graphics2D g2D, float magnification) {
      g2D.setColor(color[number]);
      Stroke bs1 = new BasicStroke(1.0f / magnification);
      g2D.setStroke(bs1);
      g2D.draw(gp);
      }
      
   /**
    * Read the anchors from the xml file
    * @param parentDOM
    */
   public void read(org.w3c.dom.Node parentDOM) {
      org.w3c.dom.Node nodeDOM = parentDOM.getFirstChild();
      while (nodeDOM != null) {
         String nName = nodeDOM.getNodeName();
         // Nodes that are not x, y are not considered
         if (nName.equals("x")) p.x = Float.valueOf(nodeDOM.getFirstChild().getNodeValue()).floatValue();
         else if (nName.equals("y")) p.y = Float.valueOf(nodeDOM.getFirstChild().getNodeValue()).floatValue();
         nodeDOM = nodeDOM.getNextSibling();
         } 
      createGraphics();
      }

   /**
    * Save the registration anchors
    * @param dataOut
    * @throws IOException
    */
   public void save(FileWriter dataOut) throws IOException {
      String nL = System.getProperty("line.separator");
      dataOut.write(" <RegistrationAnchor>" + nL);
      dataOut.write("  <x>" + Float.toString(p.x) + "</x>" + nL);
      dataOut.write("  <y>" + Float.toString(p.y) + "</y>" + nL);
      dataOut.write(" </RegistrationAnchor>" + nL);
      }
   }

