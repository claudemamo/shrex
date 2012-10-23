/*
This file is part of ShreX.

ShreX is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation; either version 2 of the License, or (at your option) any later version.

ShreX is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.

You should have received a copy of the GNU General Public License along with Foobar; if not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA 
*/
package edu.ogi.cse.xs.query;

public class SQLQuery
{
  
    public SQLQuery()
    {
        m_select = "select";
        m_from = "from";
        m_where = "where";
        m_existsSubQuery = null;
    }


    public String getSelect()
    {
        return m_select;
    }

    
    public String getFrom()
    {
        return m_from;
    }
    

    public String getWhere()
    {
        return m_where;
    }
    
    public void addSelect( String append )
    {
        m_select += append;
    }

    
    public void addFrom( String append )
    {
        m_from += append;
    }
    

    public void addWhere( String append )
    {
        m_where += append;
    }
    

    
    public String getQueryString()
    {
        String str = "";

        String comma = ",", and = "AND";
        
        if( m_from.endsWith( comma ) )
        {
            int idx = m_from.lastIndexOf( comma );
            m_from = m_from.substring( 0, idx );
        }

        
        if( m_where.endsWith( and ) )
        {
            int idx = m_where.lastIndexOf( and );
            m_where = m_where.substring( 0, idx );
        }        
        
        str += m_select + "\n";
        str += m_from + "\n";

        if( !m_where.equals( "where" ) )
        {
            str += m_where + "\n";
        }
        
        return str;
    }

    
    public String toString()
    {
        String str = getQueryString();
        return str;
    }


    public void setParentElementName( String parentElementName )
    {
        m_parentElementName = parentElementName;
    }

    
    public void setIsSelectComplexElement( boolean flag )
    {
        m_isSelectCE = flag;
    }

    
    public void setIsSelectSimpleElement( boolean flag )
    {
        m_isSelectSE = flag;
    }


    public void setIsSelectAttribute( boolean flag )
    {
        m_isSelectAttr = flag;
    }

    public void setElementName( String elemName )
    {
        m_elementName = elemName;
    }
    
        
    public void setAttributeName( String attrName )
    {
        m_attributeName = attrName;
    }
    
    
    public String getParentElementName()
    {
        return m_parentElementName;
    }
    
    
    public boolean isSelectComplexElement()
    {
        return m_isSelectCE;
    }

    
    public boolean isSelectSimpleElement()
    {
        return m_isSelectSE;
    }


    public boolean isSelectAttribute()
    {
        return m_isSelectAttr;
    }

    public String getElementName()
    {
        return m_elementName;
    }
    
        
    public String getAttributeName()
    {
        return m_attributeName;
    }
    
         
    public String getTableName()
    {
        return m_tableName;
    }

            
    public void setTableName( String tableName )
    {
        m_tableName = tableName;
    }

    
    
    
    private String m_select;
    private String m_from;
    private String m_where;

    private SQLQuery m_existsSubQuery;

    private boolean m_isSelectCE;
    private boolean m_isSelectSE;
    private boolean m_isSelectAttr;
    private String m_parentElementName;
    private String m_elementName;
    private String m_attributeName;
    private String m_tableName;
    
}

