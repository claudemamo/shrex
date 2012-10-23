/*
This file is part of ShreX.

ShreX is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation; either version 2 of the License, or (at your option) any later version.

ShreX is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.

You should have received a copy of the GNU General Public License along with Foobar; if not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA 
*/
package gui;


import javax.swing.border.*;
import javax.swing.tree.*;
import javax.swing.event.*;
import javax.swing.*;

import java.awt.Component;


import edu.ogi.cse.xs.xsd.*;
import edu.ogi.cse.xs.xsd.xerces.*;
import edu.ogi.cse.xs.database.*;
import edu.ogi.cse.xs.loader.*;
import edu.ogi.cse.xs.mapping.*;



class SchemaTreeRenderer extends DefaultTreeCellRenderer
{
    private ImageIcon m_seIcon;
    private ImageIcon m_attrIcon;

    public SchemaTreeRenderer()
    {
        m_seIcon = new ImageIcon("image/e.gif");
        m_attrIcon = new ImageIcon("image/a.gif");
    }

    public Component getTreeCellRendererComponent( JTree tree,
                                                   Object value,
                                                   boolean sel,
                                                   boolean expanded,
                                                   boolean leaf,
                                                   int row,
                                                   boolean hasFocus)
    {
        super.getTreeCellRendererComponent( tree, value, sel,
                                            expanded, leaf, row,
                                            hasFocus);
        if (leaf )
        {
            if( _isSimpleElement(value) )
            {
                setIcon( m_seIcon );
                setToolTipText( "Simple Element" );
            }
            else
            {
                setIcon( m_attrIcon );
                setToolTipText( "Attribute" );
            }
        }

        return this;
    }

    
    private boolean _isSimpleElement( Object value )
    {
        SchemaTreeNode node = (SchemaTreeNode) value;

        return node.isSimpleElement();
    }
    
}
