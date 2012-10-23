/*
This file is part of ShreX.

ShreX is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation; either version 2 of the License, or (at your option) any later version.

ShreX is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.

You should have received a copy of the GNU General Public License along with Foobar; if not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA 
*/
package edu.ogi.cse.xs.common;

import java.util.*;


public class IDService
{
    static public IDService getInstance( String docId )
    { 
	IDService idservice = null;

        Object o = s_idserviceTable.get( docId );
        if( null == o )
        {
            idservice = new IDService( docId );
	    s_idserviceTable.put( docId, idservice );
        }
        else
	{
	    idservice = (IDService) o;
	}

        return idservice;
    }
    
        
    
    public IDService( String docId )
    {
        m_id = 0;
        m_docId = docId;
    }

    public IDService( String docId, int initId )
    {
        m_id = initId;
        m_docId = docId;
    }

    
    public String next( String prefix )
    {   
        return m_docId + "_" + prefix + "_" + (++m_id);
    }
    
        
    public String next()
    {
        //return m_docId + "_"  + (++m_id);
        //xxx
        return m_docId + (++m_id);
    }

    //xxx
    public String nextWithoutPrefix()
    {
        return "" + (++m_id);
    }
    
    
    public int currentId()
    {
        return m_id;
    }
    
    public void reset()
    {
        m_id = 0;
    }
    
    private int m_id;
    private String m_docId;

    static private Map s_idserviceTable = new HashMap(2);
    
}
