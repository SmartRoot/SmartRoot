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



import ij.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.datatransfer.*;
import java.sql.*;

import javax.swing.table.*;

//import unused.SQLSettingsDialog;
 

/** 
 * This class instantiate the SQL server 
 * 
 */

// %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

class SQLServer extends AbstractTableModel implements ActionListener {


	private static final long serialVersionUID = 1L;
	private Connection con = null;
   private Statement sql = null;
   private boolean sqlConnected = false;
   private String[] varName = {"Image", "Name", "Length", "LPos", "Diameter", "Angle", "Parent", "Branch LPos"};
   private String[] value;
   private int nVar = 8;
   private static Clipboard clipboard;
   
   // Driver for windows user (MS Access database)
   private String jdbcDriverClassNameWin = SR.prefs.get("SQLServer.driver", "sun.jdbc.odbc.JdbcOdbcDriver");
   private String connectionUrlWin = SR.prefs.get("SQLServer.url", "jdbc:odbc:SmartRoot");
   private String connectionUserWin = SR.prefs.get("SQLServer.user", "");
   private String connectionPasswordWin = SR.prefs.get("SQLServer.password", "");
   
   // Driver for Mac and Linux user (MySQL database)
   private String jdbcDriverClassName = SR.prefs.get("SQLServer.driver", "com.mysql.jdbc.Driver");
   private String connectionUrl = SR.prefs.get("SQLServer.url", "jdbc:mysql://localhost/smartroot");
   private String connectionUser = SR.prefs.get("SQLServer.user", "root");
   private String connectionPassword = SR.prefs.get("SQLServer.password", "");
   
//   private SQLSettingsDialog sqlSettingsDialog;
   
   public static final int IMAGE = 0;
   public static final int NAME = 1;
   public static final int LENGTH = 2;
   public static final int LPOS = 3;
   public static final int DIAMETER = 4;
   public static final int ANGLE = 5;
   public static final int PARENT = 6;
   public static final int LPOS_PAR = 7;

   /**
    * Constructor
    */
   public SQLServer() {
      value = new String[nVar];
      clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
//      sqlSettingsDialog = new SQLSettingsDialog(this);
      }
   
   /**
    * Get the default driver depending on the operating system
    * @return
    */
   public String getDefaultDriver() {
	   if(IJ.isWindows()) return ("sun.jdbc.odbc.JdbcOdbcDriver");
	   else return ("com.mysql.jdbc.Driver");
   }
   
   /**
    * Get the default url connection depending on the operating systel
    * @return
    */
   public String getDefaultUrl() {
	   if(IJ.isWindows()) return ("jdbc:odbc:SmartRoot");
	   else return ("jdbc:mysql://localhost/SmartRoot");
   }

   /**
    * Get the Default user depending on the operating system
    * @return
    */
   public String getDefaultUser() {
	   if(IJ.isWindows()) return ("");
	   else return ("root");
   }

   /**
    * Get the default password
    * @return
    */
   public String getDefaultPassword() {
      return ("");
   }

   /**
    * Get the driver depending on the operating system
    * @return
    */
   public String getDriver() {
      if (IJ.isWindows()) return (jdbcDriverClassNameWin);
      else return (jdbcDriverClassName);
   }
   
   /**
    * Get the url depending on the operating system
    * @return
    */
   public String getUrl() {
	   if (IJ.isWindows()) return (connectionUrlWin);
	   else return (connectionUrl);
   }

   /**	
    * Get the user depending on the operating system
    * @return
    */
   public String getUser() {
	   if (IJ.isWindows())  return (connectionUserWin);
	   else return (connectionUser);
   }

   /**
    * Get the user depending on the operating system
    * @return
    */
   public String getPassword() {
	   if (IJ.isWindows()) return (connectionPasswordWin);
	   else return (connectionPassword);
   }

   /**
    * Save the sql preference in the SR prefs.
    * @param driver
    * @param url
    * @param user
    * @param pw
    */
   public void savePrefs(String driver, String url, String user, String pw) {
      SR.prefs.put("SQLServer.driver", driver);
      SR.prefs.put("SQLServer.url", url);
      SR.prefs.put("SQLServer.user", user);
      SR.prefs.put("SQLServer.password", pw);
   }

   /**
    * Start the connection
    */
   public void start() {
	   if(IJ.isWindows()) start(jdbcDriverClassNameWin, connectionUrlWin, connectionUserWin, connectionPasswordWin);
	   else start(jdbcDriverClassName, connectionUrl, connectionUser, connectionPassword);
   }
   
   /**
    * Start the connection
    * @param driver
    * @param url
    * @param user
    * @param pw
    */
   public void start(String driver, String url, String user, String pw) {
      close();
      try {
         Class.forName(driver);
         }
      catch (ClassNotFoundException e) {
         SR.write("The driver " + driver + " was not found.");
         SR.write("You should check your Java installation.");
         SR.write("You will not be able to write to a database.");
         return;
         }
      try {
         con = DriverManager.getConnection(url, user, pw);
         sql = con.createStatement();
         }
      catch (SQLException sqlE) {
         SR.write("The specified database was not found.");
         SR.write("You will not be able to write to a database.");
         SR.write("You will still be able to export to a .csv file.");
         return;
         }
      try {
         sql.executeUpdate("DROP TABLE Export");
         }
      catch (SQLException sqlE) {
         }
      try {
         sql.executeUpdate("CREATE TABLE Export (a CHAR(16), b CHAR(16), c CHAR(16), d CHAR(16), e CHAR(16), f CHAR(16), g CHAR(16), h CHAR(16))");
         }
      catch (SQLException sqlE) {
         SR.write("Table Export could not be created.");
         SR.write(sqlE.getMessage());
         }
      if (IJ.isWindows()){
      jdbcDriverClassNameWin = driver;
      connectionUrlWin = url;
      connectionUserWin = user;
      connectionPasswordWin = pw;
      }
      else {
      jdbcDriverClassName = driver;
      connectionUrl = url;
      connectionUser = user;
      connectionPassword = pw;  
      }
      
      sqlConnected = true;
      SR.write("SQL connection started");
      }  

   /**
    * Close the connection
    */
   public void close() {
      if (con == null) return;
      try {
         con.close();
         }
      catch (SQLException sqlE) {
         SR.write("Datasource could not be closed");
         SR.write(sqlE.getMessage());
         return;
         }
      con = null;
      SR.write("SQL connection closed");
      }
      
   /**
    * Write the data to the database
    */
   public void write() {
      if (sql == null) return;
      String stmt = "INSERT INTO Export VALUES (";
      for (int i = 0; i < nVar; i++) stmt = stmt.concat("'" + value[i] + "', ");
      stmt = stmt.substring(0, stmt.length() - 2);
      stmt = stmt.concat(")");
//SR.write(stmt);
      try {
         sql.executeUpdate(stmt);
         }
      catch (SQLException e) {
         SR.write("Error sending SQL statement");
         SR.write(e.getMessage());
         }
      }

   /**
    * Get the current SQL statement
    * @return
    */
   public Statement getStatement() {
      return sql;
      }

   /**
    * Copy tp clipboard (GL: not surr what this is used for)
    */
   public void copyToSystemClipboard() {
      String content = "";
      for (int i = 0; i < nVar; i++) content = content.concat(value[i] + " \t ");
      content = content.substring(0, content.length() - 2);
      clipboard.setContents(new StringSelection(content), null);
      }

   /**
    * Set values
    * @param v
    */
   public void setValues(String[] v) {
      for (int i = 0; i < v.length; i++) value[i] = v[i];
      fireTableDataChanged();
      }

   /**
    * 
    * @return
    */
   public boolean isConnected() {return sqlConnected; }
   
   /**
    * 
    */
   public int getColumnCount() {return 2; }
   
   /**
    * 
    */
   public int getRowCount() {return nVar; }
   
   /**
    * 
    */
   public String getColumnName(int col) {
      return (col == 0) ? "Variable" : "Value"; 
      }
   
   /**
    * 
    */
   public Object getValueAt(int row, int col) {
      return (col == 0) ? varName[row] : value[row];
      }

   /**
    * 
    */
   public void actionPerformed(ActionEvent e) {
      if (e.getActionCommand() == "SEND_SQL") write();
      else if (e.getActionCommand() == "COPY") copyToSystemClipboard();
      }

//   /**
//    * 
//    */
//   public void changeSettingsDialog() {
//      sqlSettingsDialog.setVisible(true);
//   }
   
  }
