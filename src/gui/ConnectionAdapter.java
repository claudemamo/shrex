/*
This file is part of ShreX.

ShreX is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation; either version 2 of the License, or (at your option) any later version.

ShreX is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.

You should have received a copy of the GNU General Public License along with Foobar; if not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA 
*/
package gui;

import java.io.*;
import java.util.*;
import java.sql.*;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.tree.*;
import javax.swing.event.*;
import javax.swing.table.*;

import edu.ogi.cse.xs.xsd.*;
import edu.ogi.cse.xs.xsd.xerces.*;
import edu.ogi.cse.xs.database.*;
import edu.ogi.cse.xs.loader.*;
import edu.ogi.cse.xs.mapping.*;


class ConnectionAdapter  extends AbstractTableModel 
{
    public ConnectionAdapter( DBConnection dbconn )
    {
        m_dbconn = dbconn;
    }

    public void fetch( String tableName )
    {
        String query = "select * from " + tableName;
        
        try
        {
            m_resultSet = m_dbconn.executeQuery( query );
            
            m_metaData = m_resultSet.getMetaData();

            int numberOfColumns =  m_metaData.getColumnCount();
            m_columnNames = new String[numberOfColumns];

            
            for(int column = 0; column < numberOfColumns; column++)
            {
                m_columnNames[column] = m_metaData.getColumnLabel(column+1);
            }

            // Get all rows.
            m_rows = new Vector();
            while (m_resultSet.next())
            {
                Vector newRow = new Vector();
                for (int i = 1; i <= getColumnCount(); i++)
                {
	            newRow.addElement(m_resultSet.getObject(i));
                }
                m_rows.addElement(newRow);
            }
            
            fireTableChanged(null); // Tell the listeners a new table has arrived.
        }
        catch (SQLException ex)
        {
            System.out.println(ex);
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
        int type;
        try
        {
            type = m_metaData.getColumnType(column+1);
        }
        catch (SQLException e)
        {
            return super.getColumnClass(column);
        }

        switch(type)
        {
        case Types.CHAR:
        case Types.VARCHAR:
        case Types.LONGVARCHAR:
            return String.class;

        case Types.BIT:
            return Boolean.class;

        case Types.TINYINT:
        case Types.SMALLINT:
        case Types.INTEGER:
            return Integer.class;

        case Types.BIGINT:
            return Long.class;

        case Types.FLOAT:
        case Types.DOUBLE:
            return Double.class;

        case Types.DATE:
            return java.sql.Date.class;

        default:
            return Object.class;
        }
    }

    public boolean isCellEditable(int row, int column)
    {
        try
        {
            return m_metaData.isWritable(column+1);
        }
        catch (SQLException e)
        {
            return false;
        }
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
        Vector row = (Vector)m_rows.elementAt(aRow);
        return row.elementAt(aColumn);
    }

    public String dbRepresentation(int column, Object value)
    {
        int type;

        if (value == null)
        {
            return "null";
        }

        try
        {
            type = m_metaData.getColumnType(column+1);
        }
        catch (SQLException e)
        {
            return value.toString();
        }

        switch(type)
        {
        case Types.INTEGER:
        case Types.DOUBLE:
        case Types.FLOAT:
            return value.toString();
        case Types.BIT:
            return ((Boolean)value).booleanValue() ? "1" : "0";
        case Types.DATE:
            return value.toString(); // This will need some conversion.
        default:
            return "\""+value.toString()+"\"";
        }

    }

    public void setValueAt(Object value, int row, int column)
    {
        try
        {
            String tableName = m_metaData.getTableName(column+1);
            // Some of the drivers seem buggy, tableName should not be null.
            if (tableName == null)
            {
                System.out.println("Table name returned null.");
            }
            String columnName = getColumnName(column);
            String query =
                "update "+tableName+
                " set "+columnName+" = "+dbRepresentation(column, value)+
                " where ";
            // We don't have a model of the schema so we don't know the
            // primary keys or which columns to lock on. To demonstrate
            // that editing is possible, we'll just lock on everything.
            for(int col = 0; col<getColumnCount(); col++)
            {
                String colName = getColumnName(col);
                if (colName.equals(""))
                {
                    continue;
                }
                if (col != 0)
                {
                    query = query + " and ";
                }
                query = query + colName +" = "+
                    dbRepresentation(col, getValueAt(row, col));
            }
            System.out.println(query);
            System.out.println("Not sending update to database");
            // statement.executeQuery(query);
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
        
        Vector dataRow = (Vector)m_rows.elementAt(row);
        dataRow.setElementAt(value, column);
    }


    private DBConnection        m_dbconn;
    private ResultSet           m_resultSet;
    private String[]            m_columnNames = {};
    private Vector		m_rows = new Vector();
    private ResultSetMetaData   m_metaData;
}

    
   

        
