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



/**
 * Class building the image explorer, used to load the images in SmartRoot
 */

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.text.*;
import java.util.*;

import javax.swing.*;
import javax.swing.tree.*;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;

import ij.*;
import ij.plugin.frame.PlugInFrame;
import ij.io.Opener;
import ij.io.FileSaver;
import ij.process.ImageConverter;
import imageExplorer.JTreeTable;
import imageExplorer.FileSystemModel;


public class SR_Explorer extends PlugInFrame implements ActionListener, TreeExpansionListener {

	private static final long serialVersionUID = 1L;
	private JPopupMenu popup;
	private JTreeTable treeTable;
	private Opener imgOpener = new Opener();
	private File selectedFile;
	public static SR sr;
	public static SRWin srWin;
	private static SR_Explorer instance;
	private static DateFormat df = DateFormat.getDateTimeInstance();
	private JTree tree;
	private static SR_Explorer ie;

	/**
	 * Constructor
	 */
	public SR_Explorer() {
      super("SmartRoot Explorer");

      if (instance != null) {
         IJ.error("SmartRoot is already running");
         return;
         }
         
      (sr = new SR()).initialize();
      srWin = new SRWin();      

      try {
         UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
         }
      catch (Exception e) {};

      FilenameFilter ff = new FilenameFilter() {
         private String[] validTypes = {"tif", "jpg", "gif", "bmp", "png", "tiff"};
         public boolean accept(File dir, String name) {
        	 if (name.charAt(0) == '.') return false;
        	 // Filtering system files
        	 if(IJ.isMacOSX() || IJ.isMacintosh()){
        	 if (name.equals("bin") ||
    			 name.equals("cores") ||
				 name.equals("dev") ||
				 name.equals("etc") ||
				 name.equals("home") ||
				 name.equals("Developer") ||
				 name.equals("Library") ||
				 name.equals("net") ||
				 name.equals("Net") ||
				 name.equals("Network") ||
				 name.equals("opt") ||
				 name.equals("private") ||
				 name.equals("Quarantine") ||
				 name.equals("sbin") ||
				 name.equals("System") ||
				 name.equals("tmp") ||
				 name.equals("usr") ||
				 name.equals("var")) return false;} 
        	 if(IJ.isLinux()){
            	 if (name.equals("bin") ||
        			 name.equals("boot") ||
    				 name.equals("dev") ||
    				 name.equals("etc") ||
    				 name.equals("cdrom") ||
    				 name.equals("lib") ||
    				 name.equals("lost+found") ||
    				 name.equals("mnt") ||
    				 name.equals("proc") ||
    				 name.equals("opt") ||
    				 name.equals("root") ||
    				 name.equals("selinux") ||
    				 name.equals("sbin") ||
    				 name.equals("usr") ||
    				 name.equals("tmp") ||
    				 name.equals("sw") ||
    				 name.equals("sys") ||
    				 name.equals("var")) return false;} 
        	 
            if ((new File(dir, name)).isDirectory()) return true;
            int p = name.lastIndexOf('.');
            if (p < 0) return false;
            String suffix = name.substring(p + 1).toLowerCase();
            for (int i = 0; i < validTypes.length; i++) {
               if (suffix.equals(validTypes[i])) return true;
               }
            return false;
            }
         };
   
      FileSystemModel fsm = new FileSystemModel(ff);
      treeTable = new JTreeTable(fsm);
      treeTable.getColumnModel().getColumn(0).setPreferredWidth(250);
      tree = treeTable.getTree();
      
      popup = new JPopupMenu();
           
      // Handle Mouse events 
      treeTable.addMouseListener(new MouseAdapter() {
         public void mousePressed(MouseEvent e) {
            TreePath selPath = tree.getPathForLocation(e.getX(), e.getY());
            FileSystemModel treeModel = (FileSystemModel)tree.getModel();
            if (selPath != null && treeModel.isLeaf(selPath.getLastPathComponent()) ) {
               selectedFile = (File)(treeModel.getFile(selPath.getLastPathComponent()));
               if (e.getClickCount() == 2) {
                  SR.prefs.put("Explorer.Directory", selectedFile.getParent());
                  IJ.showStatus("Opening: " + selectedFile.getAbsolutePath());
                  ImagePlus imp = imgOpener.openImage(selectedFile.getAbsolutePath());
                  if (imp != null && validateType(imp)) {
                     RootImageCanvas ric = new RootImageCanvas(imp);
                     SRImageWindow imw = new SRImageWindow(imp, ric);
                     ric.setImageWindow(imw);
                     WindowManager.setWindow(imw);
                     new RootModel(imw, selectedFile.getParent());
                     WindowManager.setWindow(imw);
                     }
                  else imp = null;
                  IJ.showStatus(" ");
                  }
               else if (e.isPopupTrigger() || e.getButton() == 3) {
                  int row = treeTable.rowAtPoint(e.getPoint());
                  treeTable.setRowSelectionInterval(row, row);
                  popup.removeAll();
                  // Look for  backup files
                  File[] fileList = selectedFile.getParentFile().listFiles(new BackupFileFilter(selectedFile.getAbsolutePath()));
                  for (int i = 0; i < fileList.length; i++) {
                     JMenuItem mi = new JMenuItem(fileList[i].getName() + " (" + df.format(new Date(fileList[i].lastModified())) + ")");
                     mi.setActionCommand("SR_OPEN:" + fileList[i].getAbsolutePath());
                     mi.addActionListener(SR_Explorer.this);
                     popup.add(mi);
                     }
                  popup.show(treeTable, e.getX(), e.getY());
                  }
               }
            }
         });

      add(new JScrollPane(treeTable));
      pack();

      ImageJ ij = IJ.getInstance();
      Rectangle r = ij.getBounds(null);
      setBounds(SR.prefs.getInt("Explorer.Location.X", r.x), 
                SR.prefs.getInt("Explorer.Location.Y", r.y + r.height + 2),
                SR.prefs.getInt("Explorer.Location.Width", r.width), 
                SR.prefs.getInt("Explorer.Location.Height", getHeight()));

      File f = new File(SR.prefs.get("Explorer.Directory", ""));
      if (f == null || !f.isDirectory()) f = new File("");
      TreePath tp = fsm.getTreePathFromFile(f);
      tree.expandPath(tp);
      treeTable.scrollRectToVisible(new Rectangle(1, tree.getRowForPath(tp) * tree.getRowHeight(), 1, 1));

      setVisible(true);
      tree.addTreeExpansionListener(this);

      }

	
	/**
	 * Trigger the actions
	 */
    public void actionPerformed(ActionEvent ae) {
       String action = ae.getActionCommand();
       if (action.startsWith("SR_OPEN:")) {
          IJ.showStatus("Opening: " + selectedFile.getAbsolutePath());
          ImagePlus imp = imgOpener.openImage(selectedFile.getAbsolutePath());
          if (imp != null && validateType(imp)) {
             RootImageCanvas ric = new RootImageCanvas(imp);
             SRImageWindow imw = new SRImageWindow(imp, ric);
             ric.setImageWindow(imw);
             WindowManager.setWindow(imw);
             new RootModel(imw, selectedFile.getParent(), action.substring(8));
             WindowManager.setWindow(imw);
             }
          else imp = null;
          IJ.showStatus(" ");
          }
       }


    /**
     * Expand the folder system tree
     */
   public void treeExpanded(TreeExpansionEvent tee) {
      Rectangle r = treeTable.getBounds();
      r.setLocation(1, tree.getRowForPath(tee.getPath()) * tree.getRowHeight());
      treeTable.scrollRectToVisible(r);
      }
      
   
   /**
    * 
    */
   public void treeCollapsed(TreeExpansionEvent tee) {
      Rectangle r = treeTable.getBounds();
      r.setLocation(1, tree.getRowForPath(tee.getPath().getParentPath()) * tree.getRowHeight());
      treeTable.scrollRectToVisible(r);
      }
  
  
   /**
    * Validate the image type
    * @param imp
    * @return
    */
   public boolean validateType(ImagePlus imp) {
      if (imp.getType() == ImagePlus.GRAY8) return true;
      if (!IJ.showMessageWithCancel("Wrong image type...", 
                                    "The Trace and Mark tools are only functional on 8-bit grayscale images.\n \n" +
                                    "Do you want SmartRoot to convert this image to grayscale?\n \n" + 
                                    "Notes:\nIf you choose Cancel, you will only be able to see SmartRoot\n" +
                                    "graphics specified in the datafile associated with this image.\n  \n" +
                                    "If you choose Yes, you will be prompted to save the converted image.\n" + 
                                    "If you don't save it, you will simply be asked to convert the image again\n" + 
                                    "the next time you open it, which is not a problem.\n \n")) return true;
      new ImageConverter(imp).convertToGray8();
      new FileSaver(imp).save();
      imp.changes = false;
      return true;
      }

   /**
    * Close SR
    */
   public void dispose() {
      Rectangle r = getBounds();
      SR.prefs.putInt("Explorer.Location.X", r.x);
      SR.prefs.putInt("Explorer.Location.Y", r.y);
      SR.prefs.putInt("Explorer.Location.Width", r.width);
      SR.prefs.putInt("Explorer.Location.Height", r.height);
      srWin.dispose();
      SR.delete();
      instance = null;
      super.dispose();
      }

   /**
    * 
    * @return
    */
   public static SR_Explorer getInstance() {return instance; }

   
   /**
    * 
    * @param args
    */
   public static void main(String args[]) {
      ImageJ ij = new ImageJ();
      ie = new SR_Explorer();
      ij.addWindowListener(new WindowAdapter() {
                              public void windowClosed(WindowEvent e) {
                              ie.dispose();
                              System.exit(0);
                                 }
                              });
      }

   /**
    * Attempt to make an UNDO function. Not really working
    * @author guillaumelobet
    */
   class BackupFileFilter implements FileFilter {
   
      String[] validFile;
   
      /**
       * 
       * @param fName
       */
      public BackupFileFilter (String fName) {
         fName = fName.substring(0, fName.lastIndexOf('.') + 1);
         validFile = new String[RootModel.fileSuffixRSML.length + RootModel.fileSuffix.length];
         int inc = 0;
         // Get RSML files
         for( int i = 0; i < RootModel.fileSuffixRSML.length; i++){
        	 validFile[inc] = fName + RootModel.fileSuffixRSML[i];
        	 inc++;
         }
         // Get XML files
         for (int i = 0; i < RootModel.fileSuffix.length; i++) {
        	 validFile[inc] = fName + RootModel.fileSuffix[i];
        	 inc++;
         }
      }      
         
   
      /**
       * Validate the file
       */
      public boolean accept(File f) {
         String fName = f.getAbsolutePath();
         for (int i = 0; i < validFile.length; i++) 
            if (fName.equals(validFile[i])) return true;
         return false;
      }
   }
   
}

