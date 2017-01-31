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


import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

/* Created on Jun 16, 2009 */


public class SQLSettingsDialog extends JDialog implements ActionListener {
   

	private static final long serialVersionUID = 1L;
	JTextField sqlDriverField;
   JTextField sqlUrlField;
   JTextField sqlUserField;
   JTextField sqlPwField;
   JButton sqlSavePrefsButton;
   JButton sqlDefaultButton;
   JButton sqlRestartServerButton;
   JButton sqlCloseButton;
   
   private SQLServer sqlServ;
   
   public SQLSettingsDialog(SQLServer server) {
      sqlServ = server;
      buildGUI();
   }
   
   public void buildGUI() {

      this.setTitle("SQL server");
      
      JPanel p1 = new JPanel();
      p1.setLayout(new GridBagLayout());
      GridBagConstraints gbc = new GridBagConstraints();
      
      JLabel driverLabel = new JLabel("Driver class name:");
      sqlDriverField = new JTextField(sqlServ.getDriver(), 40);
      JLabel urlLabel = new JLabel("Connection URL:");
      sqlUrlField = new JTextField(sqlServ.getUrl(), 40);
      JLabel userLabel = new JLabel("Connection user name:");
      sqlUserField = new JTextField(sqlServ.getUser(), 40);
      JLabel pwLabel = new JLabel("Connection password:");
      sqlPwField = new JTextField(sqlServ.getPassword(), 40);

      gbc.gridx = 0;
      gbc.gridy = 0;
      gbc.fill = GridBagConstraints.HORIZONTAL;
      gbc.insets = new Insets(2, 2, 2, 2);
      p1.add(driverLabel, gbc);
      
      gbc.gridx = 1;
      p1.add(sqlDriverField, gbc);
      
      gbc.gridx = 0;
      gbc.gridy = 1;
      p1.add(urlLabel, gbc);
      gbc.gridx = 1;
      p1.add(sqlUrlField, gbc);
      
      gbc.gridx = 0;
      gbc.gridy = 2;
      p1.add(userLabel, gbc);
      gbc.gridx = 1;
      p1.add(sqlUserField, gbc);
      
      gbc.gridx = 0;
      gbc.gridy = 3;
      p1.add(pwLabel, gbc);
      gbc.gridx = 1;
      p1.add(sqlPwField, gbc);
      
      this.add(p1, BorderLayout.CENTER);

      JPanel p5 = new JPanel();
      p5.setLayout(new BoxLayout(p5, BoxLayout.X_AXIS));
      sqlSavePrefsButton = new JButton("Save Prefs");
      sqlSavePrefsButton.addActionListener(this);
      sqlSavePrefsButton.setActionCommand("SAVE_PREFS");
      sqlDefaultButton = new JButton("Defaults");
      sqlDefaultButton.addActionListener(this);
      sqlDefaultButton.setActionCommand("DEFAULTS");
      sqlRestartServerButton = new JButton("Restart server");
      sqlRestartServerButton.addActionListener(this);
      sqlRestartServerButton.setActionCommand("RESTART");
      sqlCloseButton = new JButton("Close");
      sqlCloseButton.addActionListener(this);
      sqlCloseButton.setActionCommand("CLOSE");
      p5.add(Box.createHorizontalGlue());
      p5.add(sqlDefaultButton);
      p5.add(Box.createRigidArea(new Dimension(5, 5)));
      p5.add(sqlSavePrefsButton);
      p5.add(Box.createRigidArea(new Dimension(5, 5)));
      p5.add(sqlRestartServerButton);
      p5.add(Box.createRigidArea(new Dimension(5, 5)));
      p5.add(sqlCloseButton);
      p5.add(Box.createRigidArea(new Dimension(5, 5)));
      this.add(p5, BorderLayout.SOUTH);
   
      this.pack();
   }

   /** @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent) */
   public void actionPerformed(ActionEvent ae) {
      String ac = ae.getActionCommand();
      if (ac == "SAVE_PREFS") {
         sqlServ.savePrefs(sqlDriverField.getText(), sqlUrlField.getText(), sqlUserField.getText(), sqlPwField.getText());
      }
      else if (ac == "DEFAULTS") {
         sqlDriverField.setText(sqlServ.getDefaultDriver());
         sqlUrlField.setText(sqlServ.getDefaultUrl());
         sqlUserField.setText(sqlServ.getDefaultUser());
         sqlPwField.setText(sqlServ.getDefaultPassword());
      }
      else if (ac == "RESTART") {
         sqlServ.start(sqlDriverField.getText(), sqlUrlField.getText(), sqlUserField.getText(), sqlPwField.getText());
      }
      else if (ac == "CLOSE") {
         setVisible(false);
      }
   }
}

