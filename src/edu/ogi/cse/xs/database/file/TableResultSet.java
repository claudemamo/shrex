/*
This file is part of ShreX.

ShreX is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation; either version 2 of the License, or (at your option) any later version.

ShreX is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.

You should have received a copy of the GNU General Public License along with Foobar; if not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA 
*/
package edu.ogi.cse.xs.database.file;



import java.util.*;
import java.sql.*;


public class TableResultSet
{
    public TableResultSet()
    {
        m_fieldNames = new ArrayList(10);
        m_fieldTypes = new ArrayList(10);
        m_rows = new ArrayList(10); //list of list
    }

    public List getFieldNames()
    {
        return m_fieldNames;
    }
    
    public void setFieldNames( List fnames )
    {
        m_fieldNames = fnames;
    }
    
    public List getFieldTypes()
    {
        return m_fieldTypes;
    }
   
    public void setFieldTypes( List ftypes )
    {
        m_fieldTypes = ftypes;
    }

    public List getRows()
    {
        return m_rows;
    } 
        
    public void addRow( List aRow )
    {
        m_rows.add( aRow );
    }
    
    private List m_fieldNames;
    private List m_fieldTypes;
    private List m_rows;
}

