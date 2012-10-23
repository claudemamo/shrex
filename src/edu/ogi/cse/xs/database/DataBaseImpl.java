/*
This file is part of ShreX.

ShreX is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation; either version 2 of the License, or (at your option) any later version.

ShreX is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.

You should have received a copy of the GNU General Public License along with Foobar; if not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA 
*/
package edu.ogi.cse.xs.database;

import java.util.*;

import edu.ogi.cse.xs.database.db2.*;
import edu.ogi.cse.xs.database.mysql.*;
import edu.ogi.cse.xs.database.file.*;
import edu.ogi.cse.xs.conf.*;

//a connection pool
public class DataBaseImpl implements DataBase
{
    public DataBaseImpl()
    {
        m_connections = new HashMap(2);
    
        Configuration conf = Configuration.getInstance();
        String driver = conf.getDriver();
        String url = conf.getDBUrl();
        String user = conf.getDBUser();
        String passwd = conf.getDBPasswd();
       
        setDefaultConnection( driver, url, user, passwd );
    }

    
    public List connections()
    {
        List result = new ArrayList(2);
        
        Iterator itr = m_connections.values().iterator();
        while( itr.hasNext() )
        {
            result.add( itr.next() );
        }
        
        return result;
    }

    
    public DBConnection getDefaultConnection()
    {
        return m_defaultConnection;
    }


    
    public void setDefaultConnection( String driver, String url,
                                      String username, String passwd )
    {
        
        m_defaultConnection = _createConnection( s_DEFAULT, driver, url, username, passwd );
        m_connections.put( s_DEFAULT, m_defaultConnection );
    }


    
    public void addConnection(String connName, String driver, String url,
                              String username, String passwd )
    {
        DBConnection conn =  _createConnection( connName, driver, url, username, passwd );
        m_connections.put( connName, conn );
    }

    
    public void removeConnection( String connName )
    {
        m_connections.remove( connName );
        if( connName.equals(s_DEFAULT) )
        {
            m_defaultConnection = null;
        }
    }


    private DBConnection _createConnection( String connName, String driver, String url,
                                    String username, String passwd )
    {
        DBConnection conn = null;
        
        if( driver.indexOf( "DB2Driver" ) != -1 )
        {
            conn = new DB2Connection( connName, driver, url, username, passwd );
        }
        else if( driver.indexOf( "mysql" ) != -1 )
        {
            conn = new MySQLConnection( connName, driver, url, username, passwd );
        }
        else if( driver.equals( "file" ) )
        {
            conn = new FileConnection( connName, driver, url, username, passwd );
        }
        else
        {
            //throw exception here TBA
        }
        
        /*
          if( driver.indexOf( "OracleDriver" ) != -1 )
        {
            conn = new OracleConneciton( connName, driver, url, username, passwd );
        }
        */
        
        return conn;
    }
    
    static public final String s_DEFAULT = "default";
    private Map m_connections;
    private DBConnection m_defaultConnection;
}
