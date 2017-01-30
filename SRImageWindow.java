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
 * Class for the image handling
 */

import ij.*;
import ij.gui.*;
import ij.io.*;
import ij.process.ImageStatistics;

import javax.swing.JOptionPane;

public class SRImageWindow extends ImageWindow {

	private static final long serialVersionUID = 1L;
	protected RootModel rm;

	/**
	 * Constructor
	 * @param imp
	 * @param ic
	 */
   public SRImageWindow(ImagePlus imp, ImageCanvas ic) {
      super(imp, ic);
//      ImageStatistics is = imp.getStatistics();
//      if(is.mode < (is.mean - is.min)){
//    	  String dial = "SmartRoot suspects that roots are lighter than the background \n" +
//                       "and will invert the grayscale to allow root tracing. Choose No if \n" +
//    	               "root are truly darker than the background. \n\n" +
//                       "If you choose Yes, you will be prompted to save the inverted image.\n" + 
//                       "If you don't save it, you will simply be asked to invert the image again\n" + 
//                       "the next time you open it, which is not a problem.\n \n";
//    	  int opt = JOptionPane.showConfirmDialog(null, dial,"Invert LUT", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
//          if (opt == JOptionPane.YES_OPTION) {
//             IJ.run("Invert");
//             new FileSaver(imp).save();
//             imp.changes = false;
//          }
//      }

      }
    
   /**
    * Attach the current root model tothe image
    * @param rm
    */
   public void attachRootModel(RootModel rm) {
      this.rm = rm;
      }

   /**
    *  Let SR handle the Save job (both image and datafile)
    *  Then let ImageJ do the rest of the processing
    *  ImageJ will not try to save again because SR resets the imp.changes flag! 
    */
   public boolean close() {
      if (!rm.getNoSave()) {
         if (!imp.changes) rm.saveToRSML();
         else {
            Object[] saveOptions = {"Save the image and datafile", "Save the datafile only", 
                                       "Save the image only", "Don't save"};
            Object selectedOption = JOptionPane.showInputDialog(null, 
                         "The image pixels have been changed. \n Please choose among the following save options.\n \n", 
                         "Save " + imp.getTitle() + " ?",
                         JOptionPane.QUESTION_MESSAGE, null,
                         saveOptions, saveOptions[0]);
            if (selectedOption == saveOptions[0] || selectedOption == saveOptions[2]) {
               FileSaver fs = new FileSaver(imp); 
               if (!fs.save()) return false;
               }
            if (selectedOption == saveOptions[0] || selectedOption == saveOptions[1]) rm.saveToRSML();
            if (selectedOption == null) return false;
            imp.changes = false; // to prevent the parent class to ask the same questions again
            }
         }
      rm = null;   // XD 20100628   Make sure we release memory...
      super.close();
      return true;
      }
}
   
