/*
This file is part of ShreX.

ShreX is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation; either version 2 of the License, or (at your option) any later version.

ShreX is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.

You should have received a copy of the GNU General Public License along with Foobar; if not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA 
*/
package edu.ogi.cse.xs.relschema;


import java.util.*;

import edu.ogi.cse.xs.common.*;
import edu.ogi.cse.xs.xsd.*;

/** represent a table */
public class Table
{
    public Table( String name, Field[] fields, XSInstance xs )
    {
        m_name = name;
        m_fields = fields;
        m_children = new ArrayList( 3 );
        
        m_parent = null;

        m_primaryKeys = new ArrayList(2);
        m_fkCons = new HashMap(1);
        
        if( null != m_fields )
        {
            for( int i = 0; i < m_fields.length; i++ )
            {
                if( m_fields[i].isPrimaryKey() )
                {
                    m_primaryKeys.add( m_fields[i].getName() );
                }

                if( m_fields[i].isForeignKey() )
                {
                    m_fkCons.put( m_fields[i].getName(),
                                  m_fields[i].getRefTableName() );
                }

                m_fields[i].setTable( this );
            }
        }

        m_xs = xs;
    }


    public XSInstance getAssociatedXSInstance()
    {
        return m_xs;
    }

    
    public String getXsdFileName()
    {
        return m_xs.getFileName();
    }
    
    
    public String getName()
    {
        return m_name;
    }
    

    public Table getParent()
    {
        return m_parent;
    }
    
    public void setParent( Table p )
    {
        m_parent = p;
    }

        
    public Field[] getFields()
    {
        return m_fields;
    }

    public int getNumFields()
    {
	return m_fields.length;
    }

    public Field getField( String fieldName )
    {
        Field result = null;
        
        if( null != m_fields )
        {
            for( int i = 0; i < m_fields.length; i++ )
            {
                if( m_fields[i].getName().equals( fieldName ) )
                {
                    result = m_fields[i];
                    break;
                }
            }
        }
        
        return result;
    }

    
    public List children()
    {
        return m_children;
    }

    
    public void addChild( Table t )
    {
        m_children.add( t );
    }


    public boolean hasCompositePrimaryKey()
    {
        if( m_primaryKeys.size() > 1 ) return true;

        return false;
    }

    
    public String[] getPrimaryKeyFieldNames()
    {
        if( m_primaryKeys.size() <= 0 ) return null;

        String[] result = new String[m_primaryKeys.size()];

        for( int i = 0; i < m_primaryKeys.size(); i++ )
        {
            result[i] = (String) m_primaryKeys.get(i);
        }

        return result;
    }    


    public Map getForeignKeyCons()
    {
        return m_fkCons;
    }


    public boolean hasPidField()
    {
        Field field = getField( Constants.PARENT_TABLE_ID );

        return (null != field);
    }
    
    
    public String toString()
    {
        String str = "";
        str += "==Table: " + getName() + "\n";

        str += "-Fields:\n";
        str += "name" + "\t";
        str += "sqlType" + "\t";
        str += "Type Length" + "\t";
        str += "isNullable" + "\t";
        str += "isPrimaryKey" + "\t";
        str += "isForeignKey" + "\t";
        str += "refTableName" + "\n";
        if( null != m_fields )
        {
            for( int i = 0; i < m_fields.length; i++ )
            {
                str += m_fields[i];
            }
        }

        return str;
    }
    
    
    private String m_name;
    private Field[] m_fields;
    
    private List m_children;
    private Table m_parent;

    private List m_primaryKeys;
    private Map m_fkCons;

    private XSInstance m_xs;
    
}
