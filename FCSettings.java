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

/* Created on Jun 16, 2009 */

/**  
 * This class contains the parameters for the lateral finding algorithms
 */

public class FCSettings{
   
	static final int N_STEP = 50;
	
	static final float D_MIN = 1;
	static final float D_MAX = 3;
	static final float MAX_ANGLE = 90;
	static final int N_IT = 5;
	
	static final boolean CHECK_N_SIZE = true;
	static final boolean CHECK_R_DIR = true;
	static final boolean CHECK_R_SIZE = true;
	static final boolean DOUBLE_DIR = 	false;
	static final boolean AUTO_FIND = false; 
	static final boolean GLOBAL_CONVEX = true; 
	static final boolean BINARY_IMG = false; 
	
	
	static final double MIN_NODE_SIZE = 0.1;
	static final double MAX_NODE_SIZE = 1.0;
	
	static final double MIN_ROOT_SIZE = 1.0;
	
	static final double MIN_ROOT_DISTANCE = 2.0;

	//--------------------
	
	static int nStep = N_STEP;
	
	static float dMin = D_MIN; 
	static float dMax = D_MAX; 
	static float maxAngle = MAX_ANGLE; 				// Max insertion angle allowed
	static int nIt = N_IT; 							// Number of iterations when searching the laterals
	
	static boolean checkNSize = CHECK_N_SIZE; 		// Check the node size
	static boolean checkRDir = CHECK_R_DIR; 		// Check the root direction
	static boolean checkRSize = CHECK_R_SIZE; 		// Check the root size
	static boolean doubleDir = DOUBLE_DIR; 			// Trace in both directions
	static boolean autoFind = AUTO_FIND; 			// Auto find the laterals
	static boolean globalConvex = GLOBAL_CONVEX; 	// Convexhull includes laterals
	static boolean useBinaryImg = BINARY_IMG; 	// Convexhull includes laterals
	
	static double minNodeSize = MIN_NODE_SIZE; 		// Min diameter of a node
	static double maxNodeSize = MAX_NODE_SIZE; 		// Max diameter of a node
	
	static double minRootSize = MIN_ROOT_SIZE; 		// Min root size 
	
	static double minRootDistance = MIN_ROOT_DISTANCE; // Min root distance from its parent
   
}

