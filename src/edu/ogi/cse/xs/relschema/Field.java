/*
This file is part of ShreX.

ShreX is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation; either version 2 of the License, or (at your option) any later version.

ShreX is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.

You should have received a copy of the GNU General Public License along with Foobar; if not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA 
*/
package edu.ogi.cse.xs.relschema;

import java.util.*;

import edu.ogi.cse.xs.common.*;

/** represent a table field */
public class Field
{
public Field( String name,
              int sqlType,
              int len,
              boolean isNullable,
              boolean isPrimaryKey,
              boolean isForeignKey,
              String refTableName )
    {
        m_name = name;
        m_sqlType = sqlType;
        m_len = len;
        m_isNullable = isNullable;
        m_isPrimaryKey = isPrimaryKey;
        m_isForeignKey = isForeignKey;
        m_refTableName = refTableName;

        m_table = null;

        m_otherFieldsInChoiceGroup = new ArrayList( 1 );
    }
    
    public String getName()
    {
        return m_name;
    }
    
    public int getSQLType()
    {
        return m_sqlType;
    }

    public int getTypeLen()
    {
        return m_len;
    }

    
    public boolean isNullable()
    {
        return m_isNullable;
    }
    
    public boolean isPrimaryKey()
    {
        return m_isPrimaryKey;
    }
    
    public boolean isForeignKey()
    {
        return m_isForeignKey;
    }

    public String getRefTableName()
    {
        return m_refTableName;
    }

    
    public void setTable( Table t )
    {
        m_table = t;
    }

     
    public Table getTable()
    {
        return m_table;
    }


    public List getChoiceConstraint()
    {
        return m_otherFieldsInChoiceGroup;
    }

    
    public void addChoiceConstriant( List fieldNames )
    {
        m_otherFieldsInChoiceGroup = fieldNames;
    }
    
    
    public String toString()
    {
        String str = "";
        str += m_name + "\t";
        str += Constants. sqlTypeToString(m_sqlType) + "\t";
        str += m_len + "\t";
        str += m_isNullable + "\t";
        str += m_isPrimaryKey + "\t";
        str += m_isForeignKey + "\t";
        str += m_refTableName + "\n";

        return str;
    }

    public String[] toStringArray()
    {
        return new String[]{    m_name,
                                Constants.sqlTypeToString(m_sqlType),
                                m_len+"",
                                m_isNullable+"",
                                m_isPrimaryKey+"",
                                m_isForeignKey + "",
                                m_refTableName + ""    };
    }
    
        
    
    private String m_name;
    private int m_sqlType;
    private int m_len;
    private boolean m_isNullable;
    private boolean m_isPrimaryKey;
    private boolean m_isForeignKey;
    private String m_refTableName;

    private Table m_table;

    private List m_otherFieldsInChoiceGroup;
}
