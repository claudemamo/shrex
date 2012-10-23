/*
This file is part of ShreX.

ShreX is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation; either version 2 of the License, or (at your option) any later version.

ShreX is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.

You should have received a copy of the GNU General Public License along with Foobar; if not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA 
*/
package edu.ogi.cse.xs.database.file;


import java.io.*;
import java.util.*;
import java.sql.*;

import edu.ogi.cse.xs.relschema.*;
import edu.ogi.cse.xs.database.*;
import edu.ogi.cse.xs.common.*;
import edu.ogi.cse.xs.conf.*;
import edu.ogi.cse.xs.xsd.xerces.*;

import  org.w3c.dom.*;
import  org.apache.xerces.dom.DocumentImpl;
import  org.apache.xerces.dom.DOMImplementationImpl;
import  org.apache.xml.serialize.*;



public class FileConnection implements DBConnection
{
    public FileConnection( String connName, String driver, String url,
                          String username, String passwd )
    {
        m_conf = Configuration.getInstance();
        m_connName = connName;

        /*
        if( null == url || url.equals( "" ) )
        {
            m_dir = "."; //default to the current directory
        }
        else
        {
            m_dir = url;
        }
        */
        m_dir = edu.ogi.cse.xs.mapping.RelationalSchemaGenerator.getDataDirectory();
        
    }
         

    public String getName()
    {
        return m_connName;
    }


    public void close()
    {
    }
    
    
    public void genRelationalSchema( RelationalSchema relschema, String sqlpath )
    { 

        Table root = relschema.getRootTable();
 
        
        try
        {
            //delete original log file
            File sqlfile = new File( sqlpath );
            if( sqlfile.exists() )
            {
                sqlfile.delete();
            }
                
            PrintWriter pw = new PrintWriter(new FileOutputStream(sqlpath, true));

            
            Document doc = _getRelSchemaDocument();
            
            //createAll
            _createAll( root, doc, pw );

            pw.close();
            
            //save finally
            _saveRelSchema( doc );
        }
        catch( Throwable t )
        {
            t.printStackTrace();
        }
    }    
   

    
    private void _createAll( Table t, Document doc, PrintWriter pw )
    {
        _createTable( t, doc, pw );

        List children = t.children();
        for( int i = 0; i < children.size(); i++ )
        {
            Table child = (Table) children.get(i);
            _createAll( child, doc, pw );
        }
    }


    
    private void _createTable( Table t, Document doc, PrintWriter pw )
    {
        //1. dump as an XML document
        try
        {
            Element relSchema = doc.getDocumentElement();
            
            Element table = doc.createElement("table");  
            relSchema.appendChild( table );
            
            table.setAttribute( "name", t.getName() );

            
            Field[] fields = t.getFields();
            

            for( int i = 0; i < fields.length; i++ )
            {
                Field f = fields[i];

                Element field = doc.createElement("field");
                table.appendChild( field );
                
                field.setAttribute( "name", f.getName() );
                field.setAttribute( "sqlType",  Constants.sqlTypeToString(f.getSQLType()) );
                field.setAttribute( "len",  f.getTypeLen() + "" );
                field.setAttribute( "isNullable",  f.isNullable() + "" );
                field.setAttribute( "isPrimaryKey",  f.isPrimaryKey() + "" );
                field.setAttribute( "isForeignKey",  f.isForeignKey() + "" );
                
                String refTableName = f.getRefTableName();

                if( null != refTableName && !refTableName.equals("") )
                {
                    field.setAttribute( "refTableName",  refTableName );
                }
            }
        }
        catch( Throwable e )
        {
            System.out.println( "unable to create table " + t.getName() );
            //e.printStackTrace();
        }


        _logCreateTableStmts( t, pw );
    }


    private void _logCreateTableStmts( Table t, PrintWriter pw )
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
                    sqlStr += " NUM(" + len + ")";
                    break;
                case Constants.SQL_DATE:
                    sqlStr += " DATE";
                    break;
                case Constants.SQL_CLOB:
                    sqlStr += " CLOB";
                    break;
                default:
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
            
           
            pw.println( sqlStr + ";" );
            pw.println();
        }
        catch( Throwable e )
        {
            //System.out.println( "unable to create table " + t.getName() );
            e.printStackTrace();
        }
    }
    


    public void insertRow( String tableName, String[] values, int sqltype[] )
    {              
        try
        {
            String dsep = Constants.TABLEDATASEP;
            String rowValue = "";
            
            for( int i = 0; i < values.length; i++ )
            {                
                if( null == values[i] || values[i].equals( "" ) )
                {
                    rowValue += "NULL";
                }
                else
                {
                    rowValue += values[i];
                }
                
                
                if( i < values.length - 1 )
                {
                    rowValue += dsep;
                }
            }
            
            Document doc = _getTableDataDocument( tableName );
            Element tableData = doc.getDocumentElement();
            Element row = doc.createElement("row");
            tableData.appendChild( row );
            row.appendChild( doc.createTextNode(rowValue) );

            _saveTableData( tableName, doc );
                             
        }
        catch( Throwable t )
        {
            t.printStackTrace();
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
        return null;
    }


    public void performBulkloading( String cmdPath )
    {
        return;   
    }  
  

    
    //get tables inside this db
    public String[] getTableNames()
    {
       
        Vector tableNameVec = new Vector(10);
        
        try
        {
            Document doc = _getRelSchemaDocument();
            Element relSchema = doc.getDocumentElement();
            
            List tables = XercesUtil.getElemsByTagName( relSchema, "table" );
            
            for( int i = 0; i < tables.size(); i++ )
            {
                Element table = (Element) tables.get(i);
                
                String tname = table.getAttribute("name");
            
                tableNameVec.add( tname );
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

        return tableNames;
    }
    
    public ResultSet executeQuery( String query )
    {
        return null;
    }
    
    //the current implementation only get entire table information
    public TableResultSet fetchTable( String tableName )
    {        
        TableResultSet result = new TableResultSet();

        List fnames = new ArrayList(10);
        List ftypes = new ArrayList(10);
        
        try
        {
            Document doc = _getRelSchemaDocument();
            Element relSchema = doc.getDocumentElement();
            
            List tables = XercesUtil.getElemsByTagName( relSchema, "table" );
            
            for( int i = 0; i < tables.size(); i++ )
            {
                Element table = (Element) tables.get(i);
                String tname = table.getAttribute("name");

                if( tname.equals( tableName ) )
                {
                    List fields =  XercesUtil.getElemsByTagName( table, "field" );
                    
                    for( int j = 0; j < fields.size(); j++ )
                    {
                        Element field = (Element) fields.get(j);
                        String fname = field.getAttribute("name");
                        fnames.add( fname );
                        String ftype = field.getAttribute("sqlType");
                        ftypes.add( ftype );
                    }
                }
            }
            
            result.setFieldNames( fnames );
            result.setFieldTypes( ftypes );
            
            doc = _getTableDataDocument( tableName );
            Element tableData = doc.getDocumentElement();

            List rows =  XercesUtil.getElemsByTagName( tableData, "row" );

            //System.out.println( "!!!!" + rows.size() );
            
            for( int i = 0; i < rows.size(); i++ )
            {
                List aRow = new ArrayList(10);
                
                Element row = (Element) rows.get(i);

                String rowVal = XercesUtil.getNodeValue( row );
                
                String dsep = Constants.TABLEDATASEP;

                //System.out.println( "#######" + rowVal );
                
                StringTokenizer st = new StringTokenizer( rowVal, dsep );
                
                while (st.hasMoreTokens())
                {
                    String s = st.nextToken();
                    //System.out.println( "\t" + s );
                    aRow.add( s );
                }
                
                result.addRow( aRow );
            }
            
         }
        catch( Throwable t )
        {
            t.printStackTrace();
        }    

        return result;
    }

    
    private Document _getRelSchemaDocument() throws IOException
    {
        String relSchemaDocPath = m_dir + SEP + "relschema.xml";

        Document doc = null;
        
        File f = new File( relSchemaDocPath );

        if( !f.exists() )
        {
            System.out.println( "Relational Schema document " + relSchemaDocPath
                                + " deos not exist on disk, create one ..." );
            doc= new DocumentImpl();
            Element relSchema = doc.createElement("relSchema");
            doc.appendChild( relSchema );    
        }
        else
        {
            doc = XercesUtil.parse( relSchemaDocPath );
        }
        
        return doc;
    }
    
    private Document _getTableDataDocument( String tableName ) throws IOException
    {
        String tableDataDocPath = m_dir + SEP + tableName + ".xml";
        
        Document doc = null;
        
        File f = new File( tableDataDocPath );
        
        if( !f.exists() )
        {
            System.out.println( "Table data document " + tableDataDocPath
                                + " deos not exist on disk, create one ..." );
            doc= new DocumentImpl();
            Element tableData = doc.createElement("tableData");
            doc.appendChild( tableData );    
        }
        else
        {
            doc = XercesUtil.parse( tableDataDocPath );
        }
        
        return doc;
    }
        
    private void _saveRelSchema( Document doc ) throws Exception
    {
        String relSchemaDocPath = m_dir + SEP + "relschema.xml";
        _saveDocument( relSchemaDocPath, doc );
    }
    
    private void _saveTableData( String tableName, Document doc ) throws Exception
    {
        String tableDataDocPath = m_dir + SEP + tableName + ".xml";
        _saveDocument( tableDataDocPath, doc );
    }

    private void _saveDocument( String path, Document doc ) throws Exception
    {
                
        //_dumpDom( doc );
        
        OutputFormat format  = new OutputFormat( doc );   //Serialize DOM
        format.setIndenting( true );
        
        FileOutputStream fos = new FileOutputStream( path );
        XMLSerializer    serial = new XMLSerializer( fos, format );
        serial.asDOMSerializer(); // As a DOM Serializer
        serial.serialize( doc.getDocumentElement() );
    }


    private void _dumpDom( Document doc )
    {
        Element top = doc.getDocumentElement();
        _dumpNode( top );
    }

    private void _dumpNode( Element e )
    {
        if( null == e ) return;
        
        System.out.println( e.getNodeName() );

        NodeList children = e.getChildNodes(); 

        for( int j = 0; j < children.getLength(); j++ )
        {
            if( children.item(j) instanceof Element )
            {
                Element child = (Element) children.item(j);
                _dumpNode( child );
            }
            if( children.item(j) instanceof Text )
            {
                Text text = (Text) children.item(j);
                System.out.println( text.getData() );
            }
        }
    }            
        
    
    private Configuration m_conf;

    private String m_connName;
    private String m_driver;
    private String m_dir;

    static private final String SEP = System.getProperty("file.separator");
}

