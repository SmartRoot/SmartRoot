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


import java.io.File;
import ij.ImagePlus;
import ij.ImageStack;
import ij.WindowManager;
import ij.gui.StackWindow;
import ij.io.FileSaver;
import ij.io.Opener;
import ij.plugin.PlugIn;
import ij.process.ByteProcessor;
import ij.process.ImageProcessor;

/* Created on May 19, 2008 */
/** @author Xavier Draye - Universit� catholique de Louvain (Belgium) */

public class Registration implements PlugIn {

   /** @see ij.plugin.PlugIn#run(java.lang.String) */
   public void run(String arg0) {
      
      try {
      
         String directory = "D:\\Home\\Pictures\\Arabido\\Gerrit\\CKX\\work1";
         File dir = new File(directory);
         String[] fileList = dir.list();
         
         String dirOut = "D:\\Home\\Pictures\\Arabido\\Gerrit\\CKX\\work2";
         
         final Opener opener = new Opener();
         
         ImagePlus mask = WindowManager.getCurrentImage();
         int width = mask.getWidth();
         int height = mask.getHeight();
         
         ImagePlus tmp = opener.openImage(directory, fileList[0]);
         tmp.setProcessor(null, getByteProcessorFromRedChannel(tmp.getProcessor()));
         
         FileSaver fs = new FileSaver(tmp);
         fs.saveAsTiff(dirOut + File.separator + "source000.tif");
         
         ImageStack targetStack = new ImageStack(width, height);
         targetStack.addSlice("target", tmp.getProcessor());
         targetStack.addSlice("mask", mask.getProcessor());
         ImagePlus targetImage = new ImagePlus("Target", targetStack);
         new StackWindow(targetImage);
         
         ImageStack result = new ImageStack(width, height);
         result.addSlice(tmp.getTitle(), tmp.getProcessor());
         
         for (int i = 1; i < fileList.length; i+=1) {
            tmp = opener.openImage(directory, fileList[i]);
            tmp.setProcessor(null, getByteProcessorFromRedChannel(tmp.getProcessor()));
            fs = new FileSaver(tmp);
            String sourcePath = dirOut + File.separator + "tmp.tif";
//            fs.saveAsTiff(sourcePath);
//            IJ.write("Processing slice " + i);
//            Object turboReg = IJ.runPlugIn("TurboReg_", 
//                  " -align " + 
//                  " -file " + sourcePath +
//                  " 0 0 " + (width - 1) + " " + (height - 1) +
//                  " -window Target " +
//                  " 0 0 " + (width - 1) + " " + (height - 1) +
//                  " -translation " +
//                  " " + (width / 2) + " " + (height / 2) +
//                  " " + (width / 2) + " " + (height / 2) +
//                  " -rigidBody " +
//                  " " + (width / 2) + " " + (height / 2) +
//                  " " + (width / 2) + " " + (height / 2) +
//                  " " + (width / 2) + " " + (height / 6) +
//                  " " + (width / 2) + " " + (height / 6) +
//                  " " + (width / 2) + " " + ((5 * height) / 6) +
//                  " " + (width / 2) + " " + ((5 * height) / 6) +

                  //                  " -hideOutput"
//                );
//            Method method = turboReg.getClass().getMethod("getTransformedImage", (Class[]) null);
//            ImagePlus transformedSource = (ImagePlus)method.invoke(turboReg, (Object[]) null);
//            transformedSource.getStack().deleteLastSlice();
//            transformedSource.getProcessor().setMinAndMax(0.0, 255.0);
//            ImageConverter converter = new ImageConverter(transformedSource);
//            converter.convertToGray8();
//            fs = new FileSaver(transformedSource);
            sourcePath = dirOut + File.separator + "source" + pad(i) + ".tif";
            fs.saveAsTiff(sourcePath);
         }
         
//         new StackWindow(new ImagePlus("Result", result));
         
      } catch (Exception e) {
         e.printStackTrace();
      }
      
   }

   
   public ByteProcessor getByteProcessorFromRedChannel(ImageProcessor ip) {
      int[] c = (int[])ip.getPixels();
      byte[] r = new byte[c.length];
      for (int i=0; i < c.length; i++) {
         r[i] = (byte)((c[i]&0xff0000)>>16);
      }
      return new ByteProcessor(ip.getWidth(), ip.getHeight(), r, null);
   }

   public String pad(int i) {
      String s = Integer.toString(i);
      while (s.length() < 3) s = "0" + s;
      return s;
   }
   

}
