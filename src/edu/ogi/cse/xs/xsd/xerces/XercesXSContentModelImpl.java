/*
This file is part of ShreX.

ShreX is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation; either version 2 of the License, or (at your option) any later version.

ShreX is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.

You should have received a copy of the GNU General Public License along with Foobar; if not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA 
*/
package edu.ogi.cse.xs.xsd.xerces;


import org.apache.xerces.impl.xs.*;
import org.apache.xerces.impl.xs.psvi.*;


import edu.ogi.cse.xs.xsd.*;

import edu.ogi.cse.xs.common.*;


/** represent a complex element's content model */
public class XercesXSContentModelImpl implements XSContentModel
{

    XercesXSContentModelImpl( XSParticle particle, SimplePath elemPath )
    {
        //top group 
        m_topGroup = new XercesXSGroupImpl( particle, elemPath );
    }

    
    public XSGroup getTopGroup()
    {
        return m_topGroup;
    }


    public String toString()
    {
        return getTopGroup().toString();
    }
    
    
    private XercesXSGroupImpl m_topGroup;
    
}

