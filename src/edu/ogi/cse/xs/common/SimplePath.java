/*
This file is part of ShreX.

ShreX is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation; either version 2 of the License, or (at your option) any later version.

ShreX is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.

You should have received a copy of the GNU General Public License along with Foobar; if not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA 
*/
package edu.ogi.cse.xs.common;

import java.util.*;


public class SimplePath
{

    /**
     * Public constructor.
     *
     */
    public SimplePath()
    {

        m_components = new Vector( 2 );
        m_size = 0;
    }


    /**
     * Public constructor.
     *
     * @param an array of strings
     *
     */
    public SimplePath( String[] path )
    {
        if( null == path || 0 == path.length )
        {
            m_components = new Vector( 2 );
            m_size = 0;
        }
        else
        {
            m_components = new Vector( path.length );
            for( int i = 0; i < path.length; i++ )
            {
                m_components.add( path[i] );
            }
            m_size = path.length;
        }
    }



    /**
     * Public constructor.
     *
     * @param a vector of strings
     *
     */
    public SimplePath( Vector path )
    {
        m_components = path;
        m_size = path.size();
    }

    

    /**
     * Public constructor.
     *
     * @param spathStr a "_" delimited string, e.g. A_B_C
     *
     */
    public SimplePath( String spathStr )
    {
        m_components = new Vector( 2 );
        m_size = 0;

        StringTokenizer st = new StringTokenizer( spathStr, Constants.ELEMENT_SEP );
        
        String component = null;

        while (st.hasMoreTokens())
        {
            component = st.nextToken();
            this.append( component );
        }
    }
    

    

    /**
     * Method to get  ith component
     *
     * @return ith component
     *
     */
    public String component( int i )
    {
        if( i < 0 || i >= m_size || null == m_components ) return null;

        return (String) m_components.elementAt( i );

    }



    /**
     * Method to get last component of the Simple path
     *
     * @return last component
     *
     */
    public String lastComponent()
    {
        if( null == m_components ) return null;

        return component( m_size - 1 );
    }


    
     /**
     * Method to get size of the Simple path
     *
     * @return length of the simple path
     *
     */

    public int size()
    {
        return m_size;
    }


     /**
     * Method to remove one component from Simple path
     *
     * @return component removed
     *
     */
    public String remove( int i )
    {
        if( i < 0 || i >= m_size || null == m_components ) return null;

        m_size--;
        
        return (String) m_components.remove( i );
    }


    public String removeLastComponent()
    {
        int idx = size() -1;
        return remove( idx );
    }    
        
    
     /**
     * Method to insert one component before ith component to Simple path
     *
     * @param str the content to be inserted
     *
     * @param i the index to be inserted
     *
     */
    public void insert( String str, int i )
    {
        if( i < 0 || i >= m_size || null == m_components ) return;

        m_components.insertElementAt( str, i );

        m_size++;
        
        return;
    }


    /**
     * Method to append one component to Simple path
     *
     * @param the content to be appended
     *
     */

    public void append( String str )
    {
        if( null == m_components )  m_components = new Vector( 2 );

        m_components.add( str );

        m_size++;
        
        return;
    }


    
    /**
     * clone
     *
     * @return the cloned object
     *
     */
    public Object clone()
    {   
        SimplePath copy = new SimplePath();
        
        for( int i = 0; i < size(); i++ )
        {
            copy.append( component(i) );
        }
        
        return copy;
    }

    
    /**
     * equals
     */
    public boolean equals(  Object obj )
    {
        if( null == obj ) return false;
        
        if( !( obj instanceof SimplePath ) ) return false;
        
        SimplePath spath = (SimplePath) obj;
        
        if( size() != spath.size() ) return false;
        
        boolean isEqual = true;
        
        for( int i = 0; i < size(); i++ )
        {
            if( !component(i).equals( spath.component(i) ) )
            {
                isEqual = false;
                break;
            }
        }
        
        return isEqual;
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
        return toString(Constants.ELEMENT_SEP);    
    }

    
    public String toString( String sep )
    {
        String str = "";
        for( int i = 0; i < size(); i++ )
        {
            str += component( i );
            if( i < size() -1 ) str += sep;
        }

        return str.trim();
    }

    
    public String toXPath()
    {
        String str = "";
        for( int i = 0; i < size(); i++ )
        {
            str += "/" + component( i );
        }
        
        return str.trim();
    }


    public boolean prefixedBy( SimplePath sp )
    {
        if( null == sp ) return false;

        if( sp.size() == 0 ) return true;

        if( size() < sp.size() ) return false;

        boolean result = true;

        for( int i = 0; i < sp.size(); i++ )
        {
            if( !sp.component(i).equals( component(i) ) )
            {
                result = false;
                break;
            }
        }

        return result;
    }    

    
    public SimplePath concate( SimplePath sp )
    {   
        if( null == sp ) return (SimplePath) this.clone();
        
        SimplePath result = (SimplePath) this.clone();
        
        for( int k = 0; k < sp.size(); k++ )
        {
            result.append( sp.component(k) );
        }
        
        return result;
    }
    


    public boolean isParentPath( SimplePath sp )
    {
        if( null == sp ) return false;
        
        if( size() >= sp.size() ) return false;

        boolean result = true;
        
        for( int i = 0 ; i < size(); i++ )
        {
            if( !component(i).equals( sp.component(i) ) )
            {
                result = false;
                break;
            }
        }

        return result;
    }
    
        
    private Vector m_components = null; 
    private int m_size = -1;
}


