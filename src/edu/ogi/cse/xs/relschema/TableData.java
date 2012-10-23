/*
This file is part of ShreX.

ShreX is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation; either version 2 of the License, or (at your option) any later version.

ShreX is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.

You should have received a copy of the GNU General Public License along with Foobar; if not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA 
*/
package edu.ogi.cse.xs.relschema;

import java.util.*;
import java.io.*;

import edu.ogi.cse.xs.common.*;
import edu.ogi.cse.xs.conf.*;
import edu.ogi.cse.xs.database.*;


//TBA: one file handler is associated with a TableData object
//will change this implemehtation later
public class TableData
{
    
    public TableData( Table t, DBConnection conn, PrintWriter pw )
    {
        m_table = t;
        m_values = new HashMap(10);
        
        m_conn = conn;

        m_filledFieldSet = new HashSet( 10 );

        m_pw = pw;
    }
    
    
    public void insertField( String fieldname, String value )
    {
	/*
        System.out.println( "===================insert field " + fieldname + ": "
                            + value + " to table " + m_table.getName() );
	*/
        
	if( m_filledFieldSet.contains( fieldname ) )
        {
            //flushCurrentRow();  
	    m_values.clear();
	    m_filledFieldSet.clear();
        }
        

        //escape single quote
        String val1 = _escape( value, "'" );
        //escape table separator
        String val2 = _escape( val1, Constants.TABLEDATASEP );
        
        m_values.put( fieldname, val2 );

        _markFilledField( fieldname );

        //System.out.println( toString() );        
	
	if( m_filledFieldSet.size() == m_table.getNumFields() )
	{
	     flushCurrentRow(); 
        }
    }
    

    public void insertRow( String[] values )
    {
        Field[] fields = m_table.getFields();

        if( fields.length != values.length )
        {
            //TBA: throw exception here
        }
        
        for( int i = 0; i < fields.length; i++ ) 
        {
            insertField( fields[i].getName(), values[i] );
        }
    }
        

        
        

    private void _markFilledField( String fieldname )
    {
        m_filledFieldSet.add( fieldname );

        Field field = m_table.getField( fieldname );
        
        List otherFieldsInChoiceGroup = field.getChoiceConstraint();
        
        for( int i = 0; i < otherFieldsInChoiceGroup.size(); i++ ) 
        {
            m_filledFieldSet.add( otherFieldsInChoiceGroup.get( i ) );
        }
    }

    
    public void flushCurrentRow()
    {
        long start = Stat.tick();
        //System.out.println( "flush current row" );
        
        String values[] = _values();

        if( Configuration.getInstance().bulkLoading() )
        {
            //System.out.println( "write to disk" );
            m_conn.writeRow( values, m_pw );
        }
        else
        {
            //System.out.println( "load into db" );
            m_conn.insertRow(m_table.getName(), values, _sqltypes() );
        }
        
        //m_values.clear();

        //m_filledFieldSet.clear();

        long stop = Stat.tick();

        Stat.ioTime += stop - start;
    }

    public void closeFileHandler()
    {
        m_pw.close();
        m_pw = null;
    }


    public boolean idSet()
    {
        return  m_filledFieldSet.contains( Constants.TABLE_ID );
    }

    
    public boolean pidSet()
    {
        return  m_filledFieldSet.contains( Constants.PARENT_TABLE_ID );
    }

    public String getCurrentRowId()
    {
        Object o =  m_values.get( Constants.TABLE_ID );

        if( null == o ) return null;

        return (String) o;
    }
    

     //escape the given character ch
    private String _escape( String value, String ch )
    {
        //XXX
        if( value.indexOf( ch ) == -1 ) return value;
        
        String val1 = "";
        
        StringTokenizer st = new StringTokenizer(value, ch);
        
        int i = 1;
        int totalTokens = st.countTokens();
        
        while ( st.hasMoreTokens() )
        {
            val1 += st.nextToken();
            if( i++ < totalTokens )
            {
                val1 += "''";
            }
        }

        return val1;
    }
   
    
    private String[] _values()
    {
        Field[] fields = m_table.getFields();

        String[] values = new String[fields.length];
        
        for( int i = 0; i < fields.length; i++ )
        {
            values[i] = (String) m_values.get( fields[i].getName() );
        }

        return values;
    }

    
    private int[] _sqltypes()
    {
        Field[] fields = m_table.getFields();
        
        int[] sqltypes = new int[fields.length];
        
        for( int i = 0; i < fields.length; i++ )
        {
            sqltypes[i] = fields[i].getSQLType();
        }
        
        return sqltypes;
    }
    

    public String toString()
    {
        String str = "";
        str += "\n\n================================\n";
        str += "Table " + m_table.getName() + "'s Current Row Values\n";
        str += "================================\n\n";
        
        Iterator itr = m_values.keySet().iterator();
        
        while( itr.hasNext() )
        {
            String fieldName = (String) itr.next();
            String value = (String) m_values.get( fieldName );
            
            str += fieldName + ":\t" + value + "\n";
        }
        
        str += "\n\n================================\n";
        str += "value filled fields\n";
        str += "================================\n\n";

        itr = m_filledFieldSet.iterator();

        while( itr.hasNext() )
        {
            String fieldName = (String) itr.next();
            str += fieldName + " ";
        }
        str += "\n";

        return str;
    }
    
        
    private Map m_values;
    private Table m_table; 
    private PrintWriter m_pw;

    private DBConnection m_conn;
    
    private Set m_filledFieldSet;
    
}
