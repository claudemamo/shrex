/*
This file is part of ShreX.

ShreX is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation; either version 2 of the License, or (at your option) any later version.

ShreX is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.

You should have received a copy of the GNU General Public License along with Foobar; if not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA 
*/
package edu.ogi.cse.xs.relschema;

import java.util.*;

import edu.ogi.cse.xs.xsd.XSInstance;

/*
  represent relation schema generated from one XML schema
  */
public class RelationalSchema
{
    public RelationalSchema( XSInstance xs )
    {
        m_rootTable = null;
        m_xs = xs;
    }


    public Table getRootTable()
    {
        return m_rootTable;
    }

    public void setRootTable( Table rootTable )
    {
        m_rootTable = rootTable;
    }
    
    public String getXmlschemaname()
    {
        return m_xs.getName();
    }
    
    public XSInstance getXSInstance()
    {
        return m_xs;
    }

    public List tables()
    {
        List result = new ArrayList(10);
        _addTable( m_rootTable, result );

        return result;
    }

    private void _addTable( Table t, List result )
    {
        result.add( t );

        List childTables = t.children();
        for( int i = 0; i < childTables.size(); i++ )
        {
            Table child = (Table) childTables.get(i);

            _addTable( child, result );
        }
    }    
        
    
    private Table m_rootTable;
    private XSInstance m_xs;
    
}
