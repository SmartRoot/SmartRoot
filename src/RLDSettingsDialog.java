/**
 * Copyright © 2009-2017, Université catholique de Louvain
 * All rights reserved.
 *
 * Copyright © 2017 Forschungszentrum Jülich GmbH
 * All rights reserved.
 *
 *  @author Xavier Draye
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

import java.awt.BorderLayout;
import java.awt.Dimension;
//import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.Point;
//import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Rectangle2D;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

/* Created on Jul 27, 2009 */



public class RLDSettingsDialog extends JDialog implements ActionListener {


	private static final long serialVersionUID = 1L;
	JTextField roixField, roiyField, roiwField, roihField, gridxField, gridyField, thickField;
   JButton cancelButton, okButton;
   Frame owner;

   static int rc;
   static final int CANCEL = 0; 
   static final int OK = 1; 
   static RLDSettingsDialog instance = null;

   public RLDSettingsDialog(Frame owner) {
      super(owner, true);
      this.owner = owner;
      buildGUI();
   }

   
   static public int showDialog(Frame owner, Rectangle2D.Double bounds, RLDGridSize gs) {
      if (instance == null) instance = new RLDSettingsDialog(owner);
      instance.roixField.setText(Double.toString(bounds.x));
      instance.roiyField.setText(Double.toString(bounds.y));
      instance.roihField.setText(Double.toString(bounds.height));
      instance.roiwField.setText(Double.toString(bounds.width));
      instance.gridxField.setText(Double.toString(gs.gridx));
      instance.gridyField.setText(Double.toString(gs.gridy));
      instance.thickField.setText(Double.toString(gs.thickness));
      instance.setVisible(true);
      
      if (rc == OK) {
         bounds.x = Double.parseDouble(instance.roixField.getText());
         bounds.y = Double.parseDouble(instance.roiyField.getText());
         bounds.height = Double.parseDouble(instance.roihField.getText());
         bounds.width = Double.parseDouble(instance.roiwField.getText());
         gs.gridx = Double.parseDouble(instance.gridxField.getText());
         gs.gridy = Double.parseDouble(instance.gridyField.getText());
         gs.thickness = Double.parseDouble(instance.thickField.getText());
      }
      return rc;
   }
   
   private void buildGUI() {
      JPanel p1 = new JPanel();
      p1.setLayout(new GridLayout(7, 2, 5, 5));
      p1.add(new JLabel("Roi [pixels] - x"));
      p1.add(roixField = new JTextField());
      p1.add(new JLabel("Roi [pixels] - y"));
      p1.add(roiyField = new JTextField());
      p1.add(new JLabel("Roi [pixels] - w"));
      p1.add(roiwField = new JTextField());
      p1.add(new JLabel("Roi [pixels] - h"));
      p1.add(roihField = new JTextField());
      p1.add(new JLabel("Grid size [cm] - x"));
      p1.add(gridxField = new JTextField());
      p1.add(new JLabel("Grid size [cm] - y"));
      p1.add(gridyField = new JTextField());
      p1.add(new JLabel("Thnickness [cm]"));
      p1.add(thickField = new JTextField());
      
      JPanel p2 = new JPanel();
      p2.setLayout(new BoxLayout(p2, BoxLayout.Y_AXIS));
      p2.add(cancelButton = new JButton("Cancel"));
      cancelButton.addActionListener(this);
      cancelButton.setActionCommand("RLD_SETTINGS_CANCEL");
      p2.add(Box.createRigidArea(new Dimension(0, 5)));
      p2.add(okButton = new JButton("OK"));
      okButton.addActionListener(this);
      okButton.setActionCommand("RLD_SETTINGS_OK");
      
      this.setLayout(new BorderLayout(5, 5));
      this.add(p1, BorderLayout.LINE_START);
      this.add(p2, BorderLayout.LINE_END);
      
      Point p = owner.getLocation();
      p.x += 20;
      p.y += 20;
      this.setLocation(p);
      
      this.setTitle("RLD Settings");
      
      pack();
   }

   /** @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent) */
   public void actionPerformed(ActionEvent ae) {
      String ac = ae.getActionCommand();
      if (ac.equals("RLD_SETTINGS_CANCEL")) {
         rc = CANCEL;
         setVisible(false);
      }
      else if (ac.equals("RLD_SETTINGS_OK")) {
         rc = OK;
         setVisible(false);
      }
   }
}
