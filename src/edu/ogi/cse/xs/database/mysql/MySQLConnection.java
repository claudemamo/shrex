/*
This file is part of ShreX.

ShreX is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation; either version 2 of the License, or (at your option) any later version.

ShreX is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.

You should have received a copy of the GNU General Public License along with Foobar; if not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA 
*/
package edu.ogi.cse.xs.database.mysql;


import java.io.*;
import java.util.*;

import java.sql.*;

import edu.ogi.cse.xs.relschema.*;
import edu.ogi.cse.xs.database.*;
import edu.ogi.cse.xs.common.*;
import edu.ogi.cse.xs.conf.*;


public class MySQLConnection implements DBConnection
{
    public MySQLConnection( String connName, String driver, String url,
                            String username, String passwd )
    {
        m_pw = null;
        
        m_conn = null;
        m_stmt = null;

        m_conf = Configuration.getInstance();

        m_connName = connName;
        m_driver = driver;
        m_url = url;
        m_username = username;
        m_passwd = passwd;

    }
         

    public String getName()
    {
        return m_connName;
    }


    public void close()
    {
        _stop();
    }
    
    
    public void genRelationalSchema( RelationalSchema relschema, String sqlpath )
    {
        if( null == m_conn )
        {
            _start();
        }

        Table root = relschema.getRootTable();
 
        
        try
        {    
            //delete original log file
            File sqlfile = new File( sqlpath );
            if( sqlfile.exists() )
            {
                sqlfile.delete();
            }
                
            m_pw = new PrintWriter(new FileOutputStream(sqlpath, true));
        
            
            //1. drop all the tables
            Map droppedTables = new HashMap(10);
            _dropAll( root, droppedTables );
            
            //2. createAll
            _createAll( root );
        }
        catch( Throwable t )
        {
            t.printStackTrace();
        }
        finally
        {
            m_pw.close();
            m_pw = null;

            _stop();
        }

    }

    
    
    private void _start()
    {
        try
        {
            Class.forName(m_driver).newInstance();
            
            String connStr = m_url + "?user=" + m_username;
            
            if( null != m_passwd && !m_passwd.equals("") )
            {
                connStr += "&password=" + m_passwd;
            }
            
            //System.out.println( "mysql connection string: " + connStr );       
            
            m_conn = DriverManager.getConnection(connStr);
            
            // Create a Statement
            m_stmt = m_conn.createStatement();
        }
        catch( Throwable t )
        {
            t.printStackTrace();
        }
    }
 

    private void _stop()
    {
        try
        {
            // Close the Statement
            if( null != m_stmt )
            {
                m_stmt.close();
                m_stmt = null;
            }
            
            // Close the connection
            if( m_conn != null )
            {
                m_conn.close();   
                m_conn = null;
            }
            
        }
        catch( Throwable t )
        {
            t.printStackTrace();
        }
    }



    private void _dropAll( Table t, Map droppedTables )
    {
        //System.out.println( "goint to drop " + t.getName() );
     
        
        boolean okToDrop = _okToDrop( t, droppedTables );
        
      
        if( !okToDrop )
        {
            //drop its children
            List children = t.children();
            
            for( int i = 0; i < children.size(); i++ )
            {
                Table child = (Table) children.get(i);
                
                _dropAll( child, droppedTables );
            }
        }
        
        droppedTables.put( t.getName(), t );
        _dropTable( t );
    }
    
      
    private boolean _okToDrop( Table t, Map droppedTables )    
    {        

        
        //ok if all t's children have been dropped
        boolean ok = true;
        
        List children = t.children();
        for( int i = 0; i < children.size(); i++ )
        {
            Table child = (Table) children.get(i);
            Object o = droppedTables.get( child.getName() );
            
            if( null == o )
            {
                ok = false;
                break;
            }
        }

        return ok;
    }
    
    private void _dropTable( Table t )    
    {
        //System.out.println( "drop " + t.getName() );
     
        
        m_pw.println( "DROP TABLE " + t.getName() + ";" );

        String sqlStr = "DROP TABLE " + t.getName();
        try
        {
            m_stmt.executeUpdate( sqlStr );
        }
        catch( Throwable e )
        {
            System.out.println( "unable to drop table: " + t.getName() );
            //e.printStackTrace();
        }
    }

    
    private void _createAll( Table t )
    {
        _createTable( t );

        List children = t.children();
        for( int i = 0; i < children.size(); i++ )
        {
            Table child = (Table) children.get(i);
            _createAll( child );
        }
    }


    private void _createTable( Table t )
    {
        
        try
        {
            
            String sqlStr =  "CREATE TABLE " + t.getName() + "\n";
            sqlStr += "(\n";
            
            Field[] fields = t.getFields();
            
            Map fkTable = new HashMap(1);
            for( int i = 0; i < fields.length; i++ )
            {
                sqlStr +=  "    ";
                
                Field field = fields[i];
                
                sqlStr += field.getName();        
                
                int type = field.getSQLType(); 
                int len = field.getTypeLen();
                
                switch (type)
                {
                case Constants.SQL_STRING:
                    sqlStr += " VARCHAR(" + len + ")";
                    break;
                case Constants.SQL_INT:
                    sqlStr += " INT(" + len + ")";
                    break;
                case Constants.SQL_DATE:
                    sqlStr += " DATE";
                    break;
                case Constants.SQL_CLOB:
                    sqlStr += " TEXT";
                    break;
                default:
                    sqlStr += " VARCHAR(" + len + ")"; //by default
                    break;
                }
                
                if( !field.isNullable() )
                {
                    sqlStr += " NOT NULL";
                }
                
                sqlStr += ",\n";
            }
            
            //primary key definition
            String[] pkeynames = t.getPrimaryKeyFieldNames();
            if( null != pkeynames )
            {
                sqlStr += "    PRIMARY KEY (";
                for( int i = 0; i < pkeynames.length; i++ )
                {
                    sqlStr += pkeynames[i];
                    
                    if( i != pkeynames.length -1 )
                    {
                        sqlStr += ",";
                    }
                }
                sqlStr += ")";
            }
            
            //foreign key definition
            Map fktable = t.getForeignKeyCons();
            if( fktable.size() > 0 )
            {
                sqlStr += ",\n";
                Iterator itr = fktable.keySet().iterator();
                int i = 1;
                while( itr.hasNext() )
                {
                    String fieldName = (String) itr.next();
                    String refTableName = (String) fktable.get( fieldName );
                    sqlStr += "    FOREIGN KEY (" + fieldName + ") REFERENCES " + refTableName;
                    
                    if( i++ < fktable.size() )
                    {
                        sqlStr += ",\n";
                    }
                    else
                    {
                        sqlStr += "\n";
                    }
                }
            }
            else
            {
                sqlStr += "\n";
            }
            
            sqlStr += ")";
            
            
            m_pw.println( sqlStr + ";" );
            m_pw.println();
            
            
            m_stmt.executeUpdate( sqlStr );
            
        }
        catch( Throwable e )
        {
            System.out.println( "unable to create table " + t.getName() );
            e.printStackTrace();
        }
    }

    
    //insert a row using jdbc
    public void insertRow( String tableName, String[] values, int sqltype[] )
    {
        if( null == m_conn )
        {
            _start();
        }
        
                
        try
        {   
            // Select from Supply table
            String sqlStr = "INSERT INTO " + tableName + " VALUES ( \n";
            
            for( int i = 0; i < values.length; i++ )
            {
                sqlStr += "\t";
                
                if( sqltype[i] != Constants.SQL_INT )
                {
                    sqlStr += "'";
                }
                
                sqlStr += values[i];
                
                if( sqltype[i] != Constants.SQL_INT )
                {
                    sqlStr += "'";
                }
                
                
                if( i < values.length -1 )
                {
                    sqlStr +=  ",\n";
                }
                else
                {
                    sqlStr +=  "\n";
                }
            }
            sqlStr +=  ")";

            //System.out.println( sqlStr );
            
            m_stmt.executeUpdate( sqlStr );
        }
        catch( Throwable t )
        {
            //t.printStackTrace();
        }
    }
    

    public void writeRow( String[] values, PrintWriter pw )
    {
        String dsep = Constants.TABLEDATASEP;
        for( int j = 0; j < values.length; j++ )
        {
            if( null != values[j] )
            {
                pw.print( values[j] );
            }
            
            if( j < values.length - 1 )
            {
                pw.print( dsep );
            }
        }
        pw.print( "\n" );
    }
    

    //return cmd file path
    public String dumpBulkLoadCmd( RelationalSchema relschema, String datadir )
    {

        Table root = relschema.getRootTable();
        
        String cmdFileName = Constants.BULKLOADCMDFILENAME;
        String cmdPath = datadir + SEP + cmdFileName;
        
        PrintWriter pw = null;
        try
        {
            File cmdfile = new File( cmdPath );
            if( cmdfile.exists() )
            {
                cmdfile.delete();
            }
            
            pw = new PrintWriter(new FileOutputStream(cmdPath, true));
        }
        catch( Throwable t )
        {
            t.printStackTrace();
        }

        //tokenize the url to get user db
        int lastIdx = m_url.lastIndexOf("/");
        int urlLen = m_url.length();
        String userdb = m_url.substring( lastIdx+1, urlLen );
        
        pw.println( "use " + userdb + ";" );
        _dumpCmd( root, pw, datadir );
        pw.close();

        return cmdPath;
    }


    public void performBulkloading( String cmdPath )
    {
        //TBA
        /* 
        String commandStr = "db2 -tvf " + cmdPath;
        System.out.println( commandStr );
        try
        {    
            Runtime rt = Runtime.getRuntime(); 
            Process p = rt.exec( commandStr );
            StringBuffer buf = new StringBuffer();  
            InputStream in = p.getInputStream();
            BufferedReader reader = new BufferedReader( new InputStreamReader( in ) );
            
            String s = null;
            while( (s = reader.readLine()) != null )
            {
                buf.append( s + "\n" );  
            }
            System.out.println( buf );
        }
        catch( Throwable t )
        {
            t.printStackTrace();
        }
        */
    }
    
    
    private void _dumpCmd( Table t,  PrintWriter pw, String datadir )
    {
        String dataFileName = datadir + SEP + t.getName() + Constants.DATAFILESUFFIX;
        
        //should use "load"
        pw.println( "LOAD DATA LOCAL INFILE \"" + dataFileName + "\" INTO TABLE " + t.getName()
                    + "  FIELDS TERMINATED BY '" + Constants.TABLEDATASEP + "' LINES TERMINATED BY '\\n';" );
        
        List children = t.children();
        for( int i = 0; i < children.size(); i++ )
        {
            Table child = (Table) children.get(i);
            
            _dumpCmd( child, pw, datadir  );
        }
    }
    
    
    //get tables inside this db
    public String[] getTableNames()
    {
        if( null == m_conn )
        {
            _start();
        }

        ResultSet result = null;
        Vector tableNameVec = new Vector(10);
        
        try
        {
            final String[] TYPES = new String[] {"TABLE"};
            String tableNamePattern = "%";
            
            DatabaseMetaData dbmd = m_conn.getMetaData();
            
            result = dbmd.getTables( null,
                                     null,
                                     tableNamePattern,
                                     TYPES );
            
            while (result.next())
            {
                String tableName = result.getString(3);
                tableNameVec.add( tableName );
            }
        }
        catch( Throwable t )
        {
            t.printStackTrace();
        }
        
        
        if( tableNameVec.size() <= 0 ) return null;
        
        String[] tableNames = new String[tableNameVec.size()];
        
        for( int i = 0; i < tableNameVec.size(); i++ )
        {
            tableNames[i] = (String) tableNameVec.elementAt(i);
        }
            
        _stop();

        return tableNames;
    }
    

    
    public ResultSet executeQuery( String query )
    {   
        if( null == m_conn )
        {
            _start();
        }
        
        ResultSet rs = null;
        
        try
        {
            rs = m_stmt.executeQuery(query);
        }
        catch( Throwable t )
        {
            t.printStackTrace();
        }

        return rs;
    }
        
    
    //get Connection etc.
    PrintWriter m_pw;
    
    
    private Connection m_conn;
    private Statement m_stmt;
    
    private Configuration m_conf;

    private String m_connName;
    private String m_driver;
    private String m_url;
    private String m_username;
    private String m_passwd;

    static private final String SEP = System.getProperty("file.separator");
}

