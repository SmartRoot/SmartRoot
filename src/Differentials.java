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

/* The following code is a light version of the Differentials_ plugin
   from P. Thevenaz. 
   The getHorizontalGradient and getVerticalGradient compute, respectively,
   horizontal and vertical gradients of the ImageProcessor (float) passed as argument.

   All methods are static, to avoid the user to instantiate the class before using it.

   The original code from P. Thevenaz contained a number of checks throwing
   exceptions in situations that can be avoided if the calling objects makes sure that
   the ImageProcessor to be processed is a FloatProcessor, and if the algorithm or implementation
   of the methods of Differentials are not modified. All of these checks were suppressed to 
   improve the speed of execution. Therefore, mmodifying this class code
   should be done carefully and is not advised.
*/ 

import ij.process.*;

class Differentials {

   private static final double FLT_EPSILON = (double)Float.intBitsToFloat((int)0x33FFFFFF);

   public static void getMagnitudeGradient(ByteProcessor ip) {
      int width = ip.getWidth();
      int height = ip.getHeight();
   
      ImageProcessor ip1 = ip.crop().convertToFloat();
      ImageProcessor h = ip1.duplicate();
      ImageProcessor v = ip1.duplicate();
      float[] floatPixels = (float[])ip1.getPixels();
      float[] floatPixelsH = (float[])h.getPixels();
      float[] floatPixelsV = (float[])v.getPixels();
   
      getHorizontalGradient(h);
      getVerticalGradient(v);
      for (int y = 0, k = 0; (y < height); y++) {
         for (int x = 0; (x < width); x++, k++) {
            floatPixels[k] = (float)Math.sqrt(floatPixelsH[k] * floatPixelsH[k]
               + floatPixelsV[k] * floatPixelsV[k]);
         }
      }
   
      ip1.resetMinAndMax();
      ip.setPixels((byte[]) ip1.convertToByte(true).getPixels());
   }


   public static void getHorizontalGradient (ImageProcessor ip) {

      int width = ip.getWidth();
      int height = ip.getHeight();
      double line[] = new double[width];

      for (int y = 0; (y < height); y++) {
         getRow(ip, y, line);
         getSplineInterpolationCoefficients(line, FLT_EPSILON);
         getGradient(line);
         putRow(ip, y, line);
      }
   }


   public static void getVerticalGradient (ImageProcessor ip) {
   
      int width = ip.getWidth();
      int height = ip.getHeight();
      double line[] = new double[height];
   
      for (int x = 0; (x < width); x++) {
         getColumn(ip, x, line);
         getSplineInterpolationCoefficients(line, FLT_EPSILON);
         getGradient(line);
         putColumn(ip, x, line);
      }
   }


   private static void getColumn (ImageProcessor ip, int x, double[] column) {
      int width = ip.getWidth();
      int height = ip.getHeight();
      float[] floatPixels = (float[]) ip.getPixels();

      for (int i = 0; (i < height); i++, x += width) {
         column[i] = (double) (floatPixels[x]);
      }
   }


   private static void putColumn (ImageProcessor ip, int x, double[] column) {
      int width = ip.getWidth();
      int height = ip.getHeight();
   
      float[] floatPixels = (float[]) ip.getPixels();
      for (int i = 0; (i < height); i++, x += width) {
         floatPixels[x] = (float) column[i];
      }
   } 
   
   
   private static void getRow (ImageProcessor ip, int y, double[] row) {
      int width = ip.getWidth();
      float[] floatPixels = (float[]) ip.getPixels();

      y *= width;
      for (int i = 0; (i < width); i++) {
         row[i] = (double) (floatPixels[y++]);
      }
   }

   
   private static void putRow (ImageProcessor ip, int y, double[] row) {
      int width = ip.getWidth();
      float[] floatPixels = (float[]) ip.getPixels();
   
      y *= width;
      for (int i = 0; (i < width); i++) {
         floatPixels[y++] = (float) row[i];
      }
   } 


   private static void getGradient (double[] c) {
      double h[] = {0.0, -1.0 / 2.0   };
      double s[] = new double[c.length];
   
      antiSymmetricFirMirrorOnBounds(h, c, s);
      System.arraycopy(s, 0, c, 0, s.length);
   }
   
   
   private static void getSplineInterpolationCoefficients (double[] c, double tolerance) {
      double z[] = {Math.sqrt(3.0) - 2.0   };
      double lambda = 1.0;
   
      if (c.length == 1) {
         return;
      }
      for (int k = 0; (k < z.length); k++) {
         lambda = lambda * (1.0 - z[k]) * (1.0 - 1.0 / z[k]);
      }
      for (int n = 0; (n < c.length); n++) {
         c[n] = c[n] * lambda;
      }
      for (int k = 0; (k < z.length); k++) {
         c[0] = getInitialCausalCoefficientMirrorOnBounds(c, z[k], tolerance);
         for (int n = 1; (n < c.length); n++) {
            c[n] = c[n] + z[k] * c[n - 1];
         }
         c[c.length - 1] = getInitialAntiCausalCoefficientMirrorOnBounds(c, z[k],
            tolerance);
         for (int n = c.length - 2; (0 <= n); n--) {
            c[n] = z[k] * (c[n+1] - c[n]);
         }
       }
   } 


   private static void antiSymmetricFirMirrorOnBounds (double[] h, double[] c, double[] s) {
      if (2 <= c.length) {
         s[0] = 0.0;
         for (int i = 1; (i < (s.length - 1)); i++) {
            s[i] = h[1] * (c[i + 1] - c[i - 1]);
         }
         s[s.length - 1] = 0.0;
      }
      else s[0] = 0.0;
   }


 @SuppressWarnings("unused")
private static void symmetricFirMirrorOnBounds (double[] h, double[] c, double[] s) {
      if (2 <= c.length) {
         s[0] = h[0] * c[0] + 2.0 * h[1] * c[1];
         for (int i = 1; (i < (s.length - 1)); i++) {
            s[i] = h[0] * c[i] + h[1] * (c[i - 1] + c[i + 1]);
         }
         s[s.length - 1] = h[0] * c[c.length - 1] + 2.0 * h[1] * c[c.length - 2];
      }
      else s[0] = (h[0] + 2.0 * h[1]) * c[0];
   } 


   private static double getInitialAntiCausalCoefficientMirrorOnBounds (double[] c, double z, double tolerance) {
      return((z * c[c.length - 2] + c[c.length - 1]) * z / (z * z - 1.0));
   }
   
   
   private static double getInitialCausalCoefficientMirrorOnBounds (double[] c, double z, double tolerance) {
      double z1 = z, zn = Math.pow(z, c.length - 1);
      double sum = c[0] + zn * c[c.length - 1];
      int horizon = c.length;
   
      if (0.0 < tolerance) {
         horizon = 2 + (int)(Math.log(tolerance) / Math.log(Math.abs(z)));
         horizon = (horizon < c.length) ? (horizon) : (c.length);
      }
      zn = zn * zn;
      for (int n = 1; (n < (horizon - 1)); n++) {
         zn = zn / z;
         sum = sum + (z1 + zn) * c[n];
         z1 = z1 * z;
      }
      return(sum / (1.0 - Math.pow(z, 2 * c.length - 2)));
   }

} 


