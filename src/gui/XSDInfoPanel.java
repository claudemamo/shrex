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
import javax.swing.table.*;

import edu.ogi.cse.xs.xsd.*;
import edu.ogi.cse.xs.xsd.xerces.*;
import edu.ogi.cse.xs.loader.*;
import edu.ogi.cse.xs.mapping.*;


class XSDInfoPanel extends JPanel
{
    public XSDInfoPanel()
    {
        super();

        setLayout(new BorderLayout());
        
        _init();
    }

    
    private void _init()
    {
        JScrollPane scroller = SwingUtil.createHtmlPane( "image/xsdintro.html" );
        add( scroller, BorderLayout.CENTER );
    }

    
    public void showSchemaDetail( SchemaTreeNode node )
    {
        if( node.isComplexElement() )
        {
            _showComplexElement( node );
        }
        if( node.isSimpleElement() )
        {
            _showSimpleElement( node );
        }
        if( node.isAttribute() )
        {
            _showAttribute( node );
        }
    }
    
        
    private void _showComplexElement( SchemaTreeNode node ) 
    {
        XSComplexElement ce = node.getComplexElement();
        
        String name = ce.getName();
        String path = ce.getPath().toString(":");
        
        if( ce.isInRecursivePath() )
        {
            String message = path + " is in a recursive path!";
            JOptionPane.showMessageDialog(XSGui.getInstance(), message);
            
            return;
        }
        
        
        String contentModel = "";
        XSContentModel content = ce.getContentModel();
        if( null != content )
        {
            contentModel = content.toString();
        }
        else
        {
            contentModel = ce.getContentType();
        }
        
        
        
        String attrInfo = "";
        java.util.List attrs = ce.attributes();
        for( int i = 0; i < attrs.size() ; i++ )
        {
            XSAttribute attr = (XSAttribute) attrs.get(i);
            attrInfo += attr.getName();
            if( i < (attrs.size() - 1) )
            {
                attrInfo += ", ";
            }
        }
        
        String seInfo = "";
        java.util.List ses = ce.simpleElements();
        for( int i = 0; i < ses.size() ; i++ )
        {
            XSSimpleElement se = (XSSimpleElement) ses.get(i);
            seInfo += se.getName();
            if( i < (ses.size()-1) )
            {
                seInfo += ", ";
            }
        }
        
        String ceInfo = "";
        java.util.List ces = ce.complexElements();
        for( int i = 0; i < ces.size() ; i++ )
        {
            XSComplexElement childce = (XSComplexElement) ces.get(i);
            ceInfo += childce.getName();
            if( i < (ces.size()-1) )
            {
                ceInfo += ", ";
            }
        }
        
        String annotationInfo = "";
        java.util.List annotations = ce.getAnnotationAttributes();
        for( int i = 0; i < annotations.size(); i++ )
        {
            String[] annotationAttr = (String[])annotations.get(i);
            String n = annotationAttr[1];
            String v = annotationAttr[2];
            annotationInfo += n + " = " + v;
            if( i < (annotations.size()-1) )
            {
                annotationInfo += "\n";
            }
        }
        
        Object[][] data =
        {
            { "Name", name },
            { "Path", path },
            { "Content Model", contentModel },
            { "Attributes", attrInfo },
            { "Child Elements of Simple Type", seInfo },
            { "Child Elements of Complex Type", ceInfo },
            { "Annotations", annotationInfo }
        };
        
        
        String[] columnNames = { "Property", "Value" };
        
        JTable table = new JTable(data, columnNames);
        table.setPreferredScrollableViewportSize(new Dimension(500, 70));
        
        _formatTable( table, columnNames );
        
        
        //Create the scroll pane and add the table to it. 
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(SwingUtil.createTitledBorder("Complex Element " + path));
    
        removeAll();
        add(scrollPane, BorderLayout.CENTER);
        
        revalidate();
        repaint();
    }
    
    
    private void _showSimpleElement( SchemaTreeNode node ) 
    {
        XSSimpleElement se = node.getSimpleElement();
        
        String name = se.getName();
        String path = se.getPath().toString(":");

        String datatype = se.getDataType();
        String isOptional = (se.isOptional())?"true":"false";

        Object[][] data =
        {
            { "Name", name },
            { "Path", path },
            { "Data Type", datatype },
            { "Is Optional", isOptional }
        };

        String[] columnNames = { "Property", "Value" };
        
        JTable table = new JTable(data, columnNames);
        table.setPreferredScrollableViewportSize(new Dimension(500, 70));

         _formatTable( table, columnNames );
        
        //Create the scroll pane and add the table to it. 
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(SwingUtil.createTitledBorder("Simple Element " + path));
        
        removeAll();
        add(scrollPane, BorderLayout.CENTER);
        
        revalidate();
        repaint();
    }

    private void _showAttribute( SchemaTreeNode node )
    {
        XSAttribute attr = node.getAttribute();
        
        String name = attr.getName();
        String parentElemPath = attr.getParentElemPath().toString(":");

        String datatype = attr.getDataType();
        String isOptional = (attr.isOptional())?"true":"false";

        
        Object[][] data =
        {
            { "Name", name },
            { "Parent Element Path", parentElemPath },
            { "Data Type", datatype },
            { "Is Optional", isOptional }
        };
        
        String[] columnNames = { "Property", "Value" };
        
        JTable table = new JTable(data, columnNames);
        table.setPreferredScrollableViewportSize(new Dimension(500, 70));

        _formatTable( table, columnNames );
        
        //Create the scroll pane and add the table to it. 
        JScrollPane scrollPane = new JScrollPane(table);
        String title = "Attribute " + name + " of element " + parentElemPath;
        scrollPane.setBorder(SwingUtil.createTitledBorder(title));
        
        removeAll();
        add(scrollPane, BorderLayout.CENTER);
        
        revalidate();
        repaint();
    }

    private void _formatTable( JTable table, String[] columnNames )
    {
        TableColumn col = table.getColumn(columnNames[0]); 
        col.setMinWidth(200);
        col.setMaxWidth(200);
    }
    
    /*
    private void _formatTable( JTable table, String[] columnNames )
    {
        for( int i = 0; i < columnNames.length; i++ )
        {
            TableColumn col = table.getColumn(columnNames[i]);
            
            int hw = _columnHeaderWidth(table, col);   // hw = header width
            int cw = _widestCellInColumn(table, col);  // cw = column width
        
            int width = (hw > cw) ? hw : cw;
            
            col.setMinWidth(width);
            col.setMaxWidth(width);
        }
    }
    

            
    private int _columnHeaderWidth( JTable table, TableColumn col)
    {
        TableCellRenderer renderer = col.getHeaderRenderer();
        
        Component comp = renderer.getTableCellRendererComponent( table, col.getHeaderValue(), 
                                                                 false, false, 0, 0);

        return comp.getPreferredSize().width;
    }

    private int _widestCellInColumn( JTable table, TableColumn col)
    {
        int c = col.getModelIndex(), width=0, maxw=0;

        for(int r = 0; r < table.getRowCount(); ++r)
        {
            TableCellRenderer renderer = table.getCellRenderer(r,c);
            
            Component comp = renderer.getTableCellRendererComponent( table, table.getValueAt(r,c), 
                                                                     false, false, r, c);

            width = comp.getPreferredSize().width;
            maxw = width > maxw ? width : maxw;
        }
        return maxw;
    }
    */
    
}

    

