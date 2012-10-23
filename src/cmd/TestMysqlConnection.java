/*
This file is part of ShreX.

ShreX is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation; either version 2 of the License, or (at your option) any later version.

ShreX is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.

You should have received a copy of the GNU General Public License along with Foobar; if not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA 
*/
package cmd;


import java.sql.Connection; 
import java.sql.DriverManager; 
import java.sql.SQLException; 

public class TestMysqlConnection
{
    
    static public void main(String[] args)
    {
        if( args.length != 3 && args.length !=2 )
        {
            System.out.println( "usage: java cmd.TestMysqlConnection url username [passwd]" );
        }

        String url = args[0];
        String username = args[1];
        String passwd = null;
        
        if( 3 == args.length )
        {
            passwd = args[2];
        }
        
        String connStr = url + "?user=" + username;
       
        
        if( null != passwd && !passwd.equals("") )
        {
            connStr += "&password=" + passwd;
        }
        
        System.out.println( "Connection string: " + connStr );
        
        try
        {
            Class.forName("com.mysql.jdbc.Driver").newInstance();
            Connection conn = DriverManager.getConnection(connStr);
            System.out.println( "connection is ok!" );
            
        }
        catch (Throwable t)
        { 
            t.printStackTrace();
        }
    }

    
	
}
