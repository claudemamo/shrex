/*
This file is part of ShreX.

ShreX is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation; either version 2 of the License, or (at your option) any later version.

ShreX is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.

You should have received a copy of the GNU General Public License along with Foobar; if not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA 
*/
package edu.ogi.cse.xs.common;



public class Constants
{

    //this is not a constant at all
    static final public boolean bulkloading = false;
    
    static final public String PCO = "PCO";
    static final public String PSCHEMA = "P-Schema";
    
    static final public int ATTRIBUTE = 1;
    static final public int ELEMENT = 2;
    static final public int COMPLEXELEMENT = 3;
    static final public int SIMPLEELEMENT = 4;
    static final public int GROUP = 5;
    static final public String GROUP_SEP = ":";
    static final public String ELEMENT_SEP = "_";
    static final public String ATTRIBUTE_SEP = ".";

    
    static final public String TABLE_ID = "id_";
    static final public String PARENT_TABLE_ID = "pid";
    static final public String CLOBCOLUMN = "clob";
    static final public String SCHEMA_NAME ="schemaName";
    static final public String IDTABLE_NAME = "idtable";
    static final public String  NODEVALUE ="nodevalue";

    
    static final public String SOURCE = "source";
    static final public String ORDINAL = "ordinal";
    static final public String ATTRNAME = "attrname";
    static final public String FLAG = "isValue";
    static final public String VALUE = "value";

    
    //SQL type constant
    static final public int SQL_UNKNOWN = 100;
    static final public int SQL_STRING = 101;
    static final public int SQL_INT = 102;
    static final public int SQL_DATE = 103;
    static final public int SQL_BOOLEAN = 104;
    static final public int SQL_FLOAT = 105;
    static final public int SQL_CLOB = 106;
    
    

    //SQL output path
    static public String LOG = "log";

    static public String BULKLOADCMDFILENAME = "bulkload.cmd";
    
    //Table data  path
    static public String TABLEDIR = "data/tables";
    static public String TABLEDATASEP = ";";
    static public String CTLFILESUFFIX = ".ctl";
    static public String DATAFILESUFFIX = ".dat";

    
    
    //default targetnamespace 
    static public String NAMESPACE = "http://www.cse.ogi.edu/fangdu-indstudy";


    static public String GLOADERNAMESPACE = "http://www.cse.ogi.edu/shrex";
    static public String TABLENAME ="tablename";
    static public String FIELDNAME ="fieldname";
    static public String OUTLINE ="outline";
    static public String SQLTYPE ="sqltype";
    static public String MAPTOCLOB ="maptoclob";
    static public String EDGEMAPPING ="edgemapping";   
    static public String IDENTITYSCHEME ="identityscheme";
    
    
    static public String VARCHAR ="VARCHAR";
    static public String NUMBER ="NUMBER";


    //xxx
    static public String KEY_FK_STR ="KFO";
    static public String DEWEY_STR ="Dewey";
    static public String INTERVAL_STR ="Interval";   
    static final public int KEY_FK = 1000;
    static final public int DEWEY = 1001;
    static final public int INTERVAL = 1002;
    

    //verbose level
    static final public int SILENT = 10000;
    static final public int DEBUG = 10001;
    static final public int TRACE = 10002;


    static public final boolean perf = false;

    
    //sql select type
    static final public int ATTR = 100000;
    static final public int SIMPELEM = 100001;
    static final public int COMPELEM = 100002;

    

    static public String sqlTypeToString( int sqltype )
    {
        String s;
        
        switch( sqltype )
        {
        case SQL_STRING:
            s = "SQL_STRING";
            break;
        case SQL_INT:
            s = "SQL_INT";
            break;
        case SQL_BOOLEAN:
            s = "SQL_BOOLEAN";
            break;
        case SQL_FLOAT:
            s = "SQL_FLOAT";
            break;
        case SQL_DATE:
            s = "SQL_DATE";
            break;
        case SQL_CLOB:
            s = "SQL_CLOB";
            break;
        default: 
            s = "UNKNOWN";
            break;
        }
        
        return s;
    }

    static public int sqlTypeToInt( String s )
    {
        int sqltype = SQL_STRING;

        if( s.equals( "SQL_STRING" ) )
        {
            sqltype =  SQL_STRING;
        }
        else if(  s.equals( "SQL_INT" ) )
        {
            sqltype =  SQL_INT;
        }
        else if(  s.equals( "SQL_FLOAT" ) )
        {
            sqltype =  SQL_FLOAT;
        }
        else if(  s.equals( "SQL_BOOLEAN" ) )
        {
            sqltype =  SQL_BOOLEAN;
        }
        else if(  s.equals( "SQL_DATE" ) )
        {
            sqltype =  SQL_DATE;
        }
        else if(  s.equals( "SQL_CLOB" ) )
        {
            sqltype =  SQL_CLOB;
        }
        else
        {
            
        }
        
        return sqltype;
    }    
    
}

