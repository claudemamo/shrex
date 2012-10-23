/*
This file is part of ShreX.

ShreX is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation; either version 2 of the License, or (at your option) any later version.

ShreX is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.

You should have received a copy of the GNU General Public License along with Foobar; if not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA 
*/
//xxx
package edu.ogi.cse.xs.loader;

import java.util.*;


public class IDNode
{
    public IDNode( int id )
    {
        m_parent = null;
        m_children = new ArrayList(10);
        m_id = id;
    }


    public int getId()
    {
        return m_id;
    }
    
    public void setParent( IDNode parent )
    {
        m_parent = parent;
    }

    
    public IDNode getParent()
    {
        return m_parent;
    }

    public void addChild( IDNode child )
    {
        m_children.add( child );
    }


    public IDNode getLastChild()
    {
        int size = m_children.size();

        if(  size == 0 ) return null;

        return (IDNode) m_children.get( size-1 );
    }
    
        
    public int getLastChildId()
    {
        IDNode lastChild = getLastChild();
        if( null == lastChild ) return 0;

        return lastChild.getId();
    }


    public String toString()
    {
        String str = "id: " + m_id + "\tparent: ";
        if( null == m_parent )
        {
            str += "null";
        }
        else
        {
            str += m_parent.getId();
        }
    
        return str;
    }
    
            
    private int m_id;
    private IDNode m_parent;
    private List m_children;
    
}

    
    
