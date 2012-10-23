/*
This file is part of ShreX.

ShreX is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation; either version 2 of the License, or (at your option) any later version.

ShreX is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.

You should have received a copy of the GNU General Public License along with Foobar; if not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA 
*/
package gui;

import java.io.*;
import java.util.*;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.tree.*;
import javax.swing.event.*;

import edu.ogi.cse.xs.xsd.*;
import edu.ogi.cse.xs.xsd.xerces.*;
import edu.ogi.cse.xs.loader.*;
import edu.ogi.cse.xs.mapping.*;


class SchemaTreeNode extends DefaultMutableTreeNode
{
    public SchemaTreeNode( Object o )
    { 
        setUserObject(o); 
    }
    
    public boolean getAllowsChildren()
    {
        return isComplexElement();
    }
    
    public boolean isLeaf()
    {
        return !isComplexElement();
    }

    public XSComplexElement getComplexElement()
    {
        return (XSComplexElement) getUserObject();
    }
    
    public XSSimpleElement getSimpleElement()
    {
        return (XSSimpleElement) getUserObject();
    }
    
    public XSAttribute getAttribute()
    {
        return (XSAttribute) getUserObject();
    }

    public boolean isComplexElement()
    {
        Object o = getUserObject();
        
        return (o instanceof XSComplexElement);
    }

    public boolean isSimpleElement()
    {
        Object o = getUserObject();
        return (o instanceof XSSimpleElement);
    }

    
    public boolean isAttribute()
    {
        Object o = getUserObject();
        return (o instanceof XSAttribute);
    }

    public boolean isExplored()
    {
        return m_explored;
    }


    public String toString()
    {
        String str = null;
        
        Object o = getUserObject();
        if( isComplexElement() )
        {
            XSComplexElement ce = (XSComplexElement) o;
            str = ce.getName();
        }
        if( isSimpleElement() )
        {
            XSSimpleElement se = (XSSimpleElement) o;
            str = se.getName();
        }
        if( isAttribute() )
        {
            XSAttribute attr = (XSAttribute) o;
            str = attr.getName();
        }

        return str;
    }
    
    public void explore()
    {
        if( isLeaf() ) return;

        if( !isExplored() )
        {
            XSComplexElement parent = getComplexElement();

            java.util.List ces = parent.complexElements();

            for(int i = 0; i < ces.size(); i++ ) 
            {
                Object ce =  ces.get(i);
                add( new SchemaTreeNode( ce ) );
            }

            
            java.util.List ses = parent.simpleElements();

            for(int i = 0; i < ses.size(); i++ ) 
            {
                Object se =  ses.get(i);
                add( new SchemaTreeNode( se ) );
            }


            java.util.List attrs = parent.attributes();
            for(int i = 0; i < attrs.size(); i++ ) 
            {
                Object attr =  attrs.get(i);
                add( new SchemaTreeNode( attr ) );
            }
            
            m_explored = true;
        }
    }

    
     private boolean m_explored = false;
}
