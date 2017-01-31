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
 * Main SR class  
 */

import ij.*;
import ij.gui.*;

import java.util.prefs.Preferences;
import java.io.*;


// %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

class SR {

   private static FileOutputStream fos;
   private static PrintStream ps;
   public static SQLServer sqlServ;
   public static Preferences prefs;
   public static int TRACE_TOOL, MARK_TOOL, ANCHOR_TOOL, LATERAL_TOOL;
   
   public static String[] listPo = {"PO:0009005", "PO:0020127", "PO:0020121", "PO:0025002", "PO:0003005", "PO:0000043"};
   public static String[] listPoNames = {"Root", "Primary root", "Lateral root", "Basal root", "Nodal root", "Crown root"};
	
   
   /**
    * Constructor
    * I should add something here to make sure this remains a singleton (lecon614.cpp)
    */
   public SR() {
      try {
         fos = new FileOutputStream(System.getProperty("user.dir") + 
                                 File.separator + "SmartRoot.log");
         ps = new PrintStream(fos, true);
         } 
      catch (FileNotFoundException e1) {write("Cannot create LOG file");}
      IJ.register(this.getClass());
      }      

   /**
    * Initialize SR
    */
   public static void initialize() {
      prefs = Preferences.userRoot().node("/ImageJ/SmartRoot");
      sqlServ = new SQLServer();
      sqlServ.start();
      Toolbar IJTool = Toolbar.getInstance();
      if (TRACE_TOOL == 0) {
         // By using addMacroTool( , ,0), any installed tools are removed from the toolbar
         // which leaves space for the SmartRoot tools (required as from ImageJ 1.37)
         IJTool.addMacroTool("Trace Root-Cfa4L0ff0Lcc33Pe151151a5eaeeae100", null, 0);
         TRACE_TOOL = IJTool.getToolId("Trace Root");
         LATERAL_TOOL = IJTool.addTool("Trace Lateral-C4f4L0ff0Lcc33Pe151151a5eaeeae100");
         MARK_TOOL = IJTool.addTool("Mark-C4f4P414e5eb8b7514100Cff4P525d6c63747b8a859699a8a7");
         ANCHOR_TOOL = IJTool.addTool("Registration Anchor-C44fL737bL37b7");
         IJTool.repaint();
         }
      IJTool.setTool(TRACE_TOOL);

      }

   /**
    * 
    * @param s
    */
   public static void write(String s) {
      IJ.log(s);
      if (ps != null) ps.println(s);
      }

   /**
    * 
    * @param s
    */
   public static void log(String s) {
      if(ps != null) ps.println(s);
      }

   
   /**
    * 
    * @return
    */
   public static SQLServer getSQLServer() {return sqlServ; }
      
   
   /**
    * 
    */
   public static void delete() {
      sqlServ.close();
      if (ps != null) {
         try {
            ps.close();
            fos.close();
            }
         catch (IOException ioe) { }
         }
      }
   
   
}


