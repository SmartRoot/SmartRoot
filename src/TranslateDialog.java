/**
 * Copyright © 2009-2017, Universite catholique de Louvain
 * All rights reserved.
 *
 * Copyright © 2017 Forschungszentrum Juelich GmbH
 * All rights reserved.
 *
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



import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**  
 * Dialog used to translate the imported root system into the image
 */

public class TranslateDialog extends JDialog implements ActionListener{
   

	private static final long serialVersionUID = -2680127157571791747L;
	
	RootModel rm;
    JButton right, left, up, down, done, rLeft, rRight;
    JTextField translation, rotation;

   JPanel p1;
   
   /**
    * Constructor
    * @param rm
    */
   public TranslateDialog(RootModel rm) {
	   this.rm = rm;
	   buildGUI();
   }
   
   /**
    * Build the interface
    */
   public void buildGUI() {
	   
	    this.setTitle("Translate");
	    
	    right = new JButton(">");
	    right.addActionListener(this);
	    right.setActionCommand("RIGHT");
	    right.setPreferredSize(new Dimension(40, 40));
	    
	    rRight = new JButton("-");
	    rRight.addActionListener(this);
	    rRight.setActionCommand("RRIGHT");
	    rRight.setPreferredSize(new Dimension(40, 40));
	    
	    left = new JButton("<");
	    left.addActionListener(this);
	    left.setActionCommand("LEFT");
	    left.setPreferredSize(new Dimension(40, 40));
	    
	    rLeft = new JButton("-");
	    rLeft.addActionListener(this);
	    rLeft.setActionCommand("RLEFT");
	    rLeft.setPreferredSize(new Dimension(40, 40));
	    
	    up = new JButton("|");
	    up.addActionListener(this);
	    up.setActionCommand("UP");
	    up.setPreferredSize(new Dimension(40, 40));
	    
	    down = new JButton("|");
	    down.addActionListener(this);
	    down.setActionCommand("DOWN");
	    down.setPreferredSize(new Dimension(40, 40));
	    
	    done = new JButton("OK");
	    done.addActionListener(this);
	    done.setActionCommand("DONE");
	    done.setPreferredSize(new Dimension(40, 40));
	    
		translation = new JTextField("5", 2);
		rotation = new JTextField("0.02");

	    JPanel panel1 = new JPanel();
	    panel1.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
	    GridBagConstraints gbc = new GridBagConstraints();
	    gbc.anchor = GridBagConstraints.WEST;	    
	    GridBagLayout gbl = new GridBagLayout();
	    panel1.setLayout(gbl);
	    
	    gbc.gridy = 0;
	    gbc.gridx = 0;
	    panel1.add(rLeft, gbc);
	    gbc.gridy = 0;
	    gbc.gridx = 1;
	    panel1.add(up, gbc);
	    gbc.gridy = 0;
	    gbc.gridx = 2;
	    panel1.add(rRight, gbc);
	    
	    gbc.gridy = 1;
	    gbc.gridx = 0;
	    panel1.add(left, gbc);
	    gbc.gridx = 1;
	    panel1.add(done, gbc);
	    gbc.gridx = 2;
	    panel1.add(right, gbc);
	    
	    gbc.gridy = 2;
	    gbc.gridx = 0;
	    panel1.add(translation, gbc);
	    gbc.gridx = 1;
	    panel1.add(down, gbc);
	    gbc.gridx = 2;
	    panel1.add(rotation, gbc);
	    

	    
	    
	    this.add(panel1);
	    
	    setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		setAlwaysOnTop(true);
		setLocationRelativeTo(null);
		setResizable(true);
		pack();
		setVisible(true);
   }
   

   /**
    * Trigger the translations as the suer click the different buttons
    */
   public void actionPerformed(ActionEvent ae) {
	   
      String ac = ae.getActionCommand();
      
      if(ac == "RIGHT") rm.translateTracing(Integer.valueOf(translation.getText()), 0, 0);
      else if(ac == "RRIGHT") rm.translateTracing(0, 0, Float.valueOf(rotation.getText()));
      else if(ac == "LEFT") rm.translateTracing(-Integer.valueOf(translation.getText()), 0, 0);
      else if(ac == "RLEFT") rm.translateTracing(0, 0, -Float.valueOf(rotation.getText()));
      else if(ac == "UP") rm.translateTracing(0, -Integer.valueOf(translation.getText()), 0);
      else if(ac == "DOWN") rm.translateTracing(0, Integer.valueOf(translation.getText()), 0);
      else if (ac == "DONE") setVisible(false);
   }

}