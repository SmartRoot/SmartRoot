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

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

/* Created on Jun 16, 2009 */
/** 
 * @author Guillaume Lobet - Universit� catholique de Louvain (Belgium) 
 * Create a dialog promped when the user ask to attach a lateral to a parent root
 * */

public class ParentDialog extends JDialog implements ActionListener, ListSelectionListener {
   
	private static final long serialVersionUID = -2680127157571791747L;
	
	RootModel rm;
	String[] rn;
	Root r;
	Root rp;
	JList root;
    JLabel text1, text2;
    JButton set, cancel;

   JPanel p1;
   
   /**
    * Constructor
    * @param rn list of root names
    * @param r the current root
    * @param rp the parent root
    * @param rm current rootmodel
    */
   public ParentDialog(String[] rn, Root r, Root rp, RootModel rm) {
	   root = new JList(rn);
	   this.rn = rn;
	   this.r = r;
	   this.rm = rm;
	   this.rp = rp;
	   rp.setSelect(true);
	   rm.repaint();
	   buildGUI();
   }
   
   /**
    * Build the interface
    */
   public void buildGUI() {
	   
	    this.setTitle("Choose parent root");

	    text1 = new JLabel("Please choose");
	    text2 = new JLabel("parent root ");
	   
	    root.setLayoutOrientation(JList.VERTICAL);
	    root.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
	    root.setSelectedIndex(indexOf(rp));
	    root.addListSelectionListener(this);
	    root.setVisibleRowCount(5);
        JScrollPane list = new JScrollPane(root);
		
		JPanel p1 = new JPanel();
		p1.setLayout(new BoxLayout(p1, BoxLayout.X_AXIS));	
        p1.setBorder(BorderFactory.createEmptyBorder(10,20,20,10));
		p1.add(list);
		  
		JPanel p5 = new JPanel();
		p5.setLayout(new BoxLayout(p5, BoxLayout.Y_AXIS));	
        //p5.setBorder(BorderFactory.createEmptyBorder(0,0,0,0));
		p5.add(text1, BorderLayout.NORTH);
		p5.add(text2, BorderLayout.SOUTH);
		
		set = new JButton("Set parent");
		set.addActionListener(this) ;
		set.setActionCommand("SET");
		
		cancel = new JButton("Cancel");
		cancel.addActionListener(this) ;
		cancel.setActionCommand("CANCEL");
		
		JPanel p2 = new JPanel();
		p2.setLayout(new BoxLayout(p2, BoxLayout.Y_AXIS));
        p2.setBorder(BorderFactory.createEmptyBorder(10, 0,0, 0));
		p2.add(cancel);
		p2.add(set);
		
		JPanel p6 = new JPanel();
		p6.setLayout(new BoxLayout(p6, BoxLayout.Y_AXIS));
        p6.setBorder(BorderFactory.createEmptyBorder(10,20,20,10));
        p6.add(p5);
        p6.add(p2);

		add(p1, BorderLayout.WEST);
		add(p6, BorderLayout.EAST);
        
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		setAlwaysOnTop(true);
		setLocationRelativeTo(null);
		setResizable(false);
		pack();
		
   }
   
   /**
    * Highlight the currently selected root in the image
    */
   public void valueChanged(ListSelectionEvent e) {
	   
	  int l = rm.rootList.size();
	  int ind = root.getSelectedIndex();
      String rs = rn[ind];
      
	  Root r1;
	  for(int i = 0; i < l; i++){ 
			r1 = (Root) rm.rootList.get(i);
			r1.setSelect(false);
			if (rs == r1.getRootID()) {
				r1.setSelect(true);
			}
	  }   
	  rm.repaint();
   }

   /**
    * Attach the root is required
    */
   public void actionPerformed(ActionEvent ae) {
	   
      String ac = ae.getActionCommand();
      int ind = root.getSelectedIndex();
      String rs = rn[ind];
	  int l = rm.rootList.size();

      if (ac == "SET") {
    	    Root r1;
			for(int i = 0; i < l; i++){ 
				r1 = (Root) rm.rootList.get(i);
				if (rs == r1.getRootID()) {
					rm.setParent(r1, r);
//					r.attachParent(r1);
//					r1.attachChild(r);
					r1.setSelected(false);
					SR.write(r1.getRootID()+" is parent of "+r.getRootID());
					break;
				}
			}
			rm.repaint();
			setVisible(false);
      }
      
      else if (ac == "CANCEL") {
			setVisible(false);
			Root r1;
			for(int i = 0; i < l; i++){ 
				r1 = (Root) rm.rootList.get(i);
				r1.setSelected(false);
			}   
			rm.repaint();
      }
   } 
   
   /**
    * Get the index of the root in the RootModel
    * @param r
    * @return
    */
   public int indexOf(Root r){
	   int i;
	   for(i = 0 ; i < rn.length ; i++){
		   if(r.getRootID() == rn[i]) break;
	   }
	   return i;
   }
}