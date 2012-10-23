/*
This file is part of ShreX.

ShreX is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation; either version 2 of the License, or (at your option) any later version.

ShreX is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.

You should have received a copy of the GNU General Public License along with Foobar; if not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA 
*/
package testsuite;

import java.sql.*;

public class DB2
{

    static public void main(String[] args)
    {
        try
        {
            String url = "jdbc:db2:xs";
            Class.forName("COM.ibm.db2.jdbc.app.DB2Driver").newInstance();
            Connection conn = DriverManager.getConnection(url,"db2inst1", "Luke@0714");
            System.out.println( "successed in connecting to " + url );

            
            //get tables inside this db
            final String[] TYPES = new String[] {"TABLE"};
            String tableNamePattern = "%";

            
            DatabaseMetaData dbmd = conn.getMetaData();

            ResultSet result = dbmd.getTables( null,
                                               null,
                                               tableNamePattern,
                                               TYPES );
            String mytablename = null;
            
            while (result.next())
            {
                String tableName = result.getString(3).toLowerCase();
                String type = result.getString(4);
                String remarks = result.getString(5);
                
                System.out.println( "name: " + tableName + "\t type: " + type + "\tremarks: " + remarks );


                /*
                ResultSet columns = dbmd.getColumns(null,null,tableName,"%");
                while (columns.next())
                {
                    System.out.println("----------------------");
                    System.out.println("Name: " + columns.getString("COLUMN_NAME"));
                    System.out.println("Type: " + columns.getString("TYPE_NAME"));
                    System.out.println("Size: " + columns.getInt("COLUMN_SIZE"));
                    System.out.println("Digits: " + columns.getInt("DECIMAL_DIGITS"));
                    System.out.println("Defaults: " + columns.getString("COLUMN_DEF"));
                    System.out.println("NULL: " + columns.getString("IS_NULLABLE"));
                }
                */

                mytablename = tableName;
                break;
                
            }

            String sqlstr = "select * from " + mytablename;

            


            
        }
        catch(Throwable t )
        {
            t.printStackTrace();
        }
    }
}

        
