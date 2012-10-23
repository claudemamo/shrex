/*
This file is part of ShreX.

ShreX is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation; either version 2 of the License, or (at your option) any later version.

ShreX is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.

You should have received a copy of the GNU General Public License along with Foobar; if not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA 
*/
package gui;

import java.io.*;
import java.util.*;


import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.tree.*;
import javax.swing.event.*;
import javax.swing.table.*;

import edu.ogi.cse.xs.xsd.*;
import edu.ogi.cse.xs.common.*;
import edu.ogi.cse.xs.xsd.xerces.*;
import edu.ogi.cse.xs.database.*;
import edu.ogi.cse.xs.database.file.*;
import edu.ogi.cse.xs.loader.*;
import edu.ogi.cse.xs.mapping.*;


class FileConnectionAdapter  extends AbstractTableModel 
{
    public FileConnectionAdapter( FileConnection filedb )
    {
        m_filedb = filedb;
    }

    public void fetch( String tableName )
    {
        //System.out.println( "fetch table " + tableName );
        
        try
        {
            m_resultSet = m_filedb.fetchTable( tableName );
            
            java.util.List fnames = m_resultSet.getFieldNames();

            int numberOfColumns =  fnames.size();
            m_columnNames = new String[numberOfColumns];

            
            for(int column = 0; column < numberOfColumns; column++)
            {
                //System.out.println( "@@" + (String) fnames.get(column) );   
                m_columnNames[column] = (String) fnames.get(column);
            }

            // Get all rows.
            m_rows = new Vector();

            java.util.List rows = m_resultSet.getRows();
            for (int i = 0; i < rows.size(); i++)
            {
                //System.out.println( "row " + i );
                                    
                java.util.List aRow = (java.util.List) rows.get(i);
                
                Vector newRow = new Vector();

                for (int j = 0; j < aRow.size(); j++)
                {
                    //System.out.println( "column " + j + " with value " + (String) aRow.get(j) );
                    
                    String fieldVal = (String) aRow.get(j);

                    newRow.addElement( fieldVal );
                }

                m_rows.addElement( newRow );
            }
                
            fireTableChanged(null); // Tell the listeners a new table has arrived.
        }
        catch (Throwable t)
        {
            System.out.println(t);
        }
    }

    //////////////////////////////////////////////////////////////////////////
    //
    //             Implementation of the TableModel Interface
    //
    //////////////////////////////////////////////////////////////////////////
    
    // MetaData

    public String getColumnName(int column)
    {
        if (m_columnNames[column] != null)
        {
            return m_columnNames[column];
        }
        else
        {
            return "";
        }
    }

    public Class getColumnClass(int column)
    {
        java.util.List ftypes = m_resultSet.getFieldTypes();
        String type = (String) ftypes.get( column );

        int sqltype = Constants.sqlTypeToInt( type );

        Class c = Object.class;
        
        switch( sqltype )
        {
        case Constants.SQL_STRING:
            c = String.class;
            break;
        case Constants.SQL_INT:
            c = Integer.class;
            break;
        case Constants.SQL_BOOLEAN:
            c = Boolean.class;
            break;
        case Constants.SQL_FLOAT:
            c= Double.class;
            break;
        case Constants.SQL_DATE:
            c= java.sql.Date.class;
            break;
        case Constants.SQL_CLOB:
            c = String.class;
            break;
        default: 
            c = Object.class;
            break;
        }
        
        
        return c;
        
    }

    
    public boolean isCellEditable(int row, int column)
    {
        return false;
    }

    
    public int getColumnCount()
    {
        return m_columnNames.length;
    }

    
    // Data methods

    public int getRowCount()
    {
        return m_rows.size();
    }

    public Object getValueAt(int aRow, int aColumn)
    {
        //System.out.println( "get value at: " + aRow + " " + aColumn );
        Vector row = (Vector)m_rows.elementAt(aRow);
        return row.elementAt(aColumn);
    }

    public String dbRepresentation(int column, Object value)
    {
        return value.toString();

    }

    public void setValueAt(Object value, int row, int column)
    {
        return;
    }


    private FileConnection m_filedb;
    private TableResultSet m_resultSet;
    private String[] m_columnNames = {};
    private Vector m_rows = new Vector();
   
}

    
   

        
