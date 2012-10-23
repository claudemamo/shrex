/*
This file is part of ShreX.

ShreX is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation; either version 2 of the License, or (at your option) any later version.

ShreX is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.

You should have received a copy of the GNU General Public License along with Foobar; if not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA 
*/
package edu.ogi.cse.xs.common;

import java.util.*;


public class ExtendedPath
{

    /**
     * Public constructor.
     *
     */
    //kind: ELEMENT, ATTRIBUTE, GROUP
    public ExtendedPath( SimplePath sp, int kind, String name )
    {
        m_sp = sp;
        m_kind = kind;
        m_name = name;
    }

     public ExtendedPath( SimplePath sp )
    {
        m_sp = sp;
        m_kind = Constants.ELEMENT;
        m_name = null;
    }


    public SimplePath getSimplePath()
    {
        return m_sp;
    }
    


    public int getKind()
    {
        return m_kind;
    }


    public String getName()
    {
        return m_name;
    }


    /**
     * equals
     */
    public boolean equals(  Object obj )
    {
        if( null == obj ) return false;
        
        if( !( obj instanceof ExtendedPath ) ) return false;
        
        ExtendedPath ep = (ExtendedPath) obj;
    
    
        
        if( ! ( m_sp.equals( ep.getSimplePath() ) ) ) return false;
        
        if( m_kind != ep.getKind() ) return false;
        
        if( m_kind == Constants.ELEMENT ) return true;
        
        if( ! ( m_name.equals( ep.getName() ) ) ) return false;
        
        
        return true;
    }    
    
        

    public int hashCode()
    {
        return toString().hashCode();
    }   
    

    /**
     *  toString()
     */
    public String toString()
    {
        String str = "";
        
        str += m_sp.toString();
        if( m_kind == Constants.ELEMENT)  return str;
        
        
        if( m_kind == Constants.ATTRIBUTE )
        {
            str += Constants.ATTRIBUTE_SEP + m_name;
        }
        
        
        if( m_kind == Constants.GROUP )
        {
            str += Constants.GROUP_SEP + m_name;
        }       
        
        return str.trim();
    }
    

        
    private SimplePath m_sp; 
    private int m_kind;
    private String m_name;
}


