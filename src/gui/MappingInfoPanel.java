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
import edu.ogi.cse.xs.common.*;
import edu.ogi.cse.xs.relschema.*;


class MappingInfoPanel extends JPanel
{
    public MappingInfoPanel()
    {
        super();

        setLayout(new BorderLayout());
        
        _init();
    }

    
    private void _init()
    {
        JScrollPane scroller = SwingUtil.createHtmlPane( "image/mappingintro.html" );
        add( scroller, BorderLayout.CENTER );
    }

    
    public void showMappingDetail( SchemaTreeNode node )
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
        XToRMapping xtorMapping = _getMapping();
        
        XSComplexElement ce = node.getComplexElement();
        SimplePath sp = ce.getPath();


        if( ce.isInRecursivePath() )
        {
            String message = sp + " is in a recursive path!";
            JOptionPane.showMessageDialog(XSGui.getInstance(), message);
            
            return;
        }

        
        String headerText = "";
       

        //must mapped to a table
        Table t = xtorMapping.getTable( sp );

        
        if( xtorMapping.edgeMapping( sp ) )
        {
            headerText = sp.toString(":") + " is mapped to an edge table: " + t.getName();
        }
        else if( xtorMapping.mappedToClob(sp) )
        {
            headerText = sp.toString(":") + " is mapped to a CLOB table:" + t.getName();;
        }
        else 
        {
            headerText = sp.toString(":") + " is mapped to table:" + t.getName();
        }

        _paintMappingPanel( headerText, t, null );
    }

    private JTable _createTable( Table t, String highlightedFieldName )
    {
        Field[] fields = t.getFields();
        int numFields = fields.length;
        
        int selectedRow = -1;
        Object[][] data = new Object[numFields][7];

        for( int i = 0; i < numFields; i++ )
        {
            Field field = fields[i];

            if( null != highlightedFieldName && highlightedFieldName.equals( field.getName() ) )
            {
                selectedRow = i;
            }
            
            String[] fieldDescripation = field.toStringArray();

            for( int j = 0; j < 7; j++ )
            {
                data[i][j] = fieldDescripation[j];
            }
        }


        String[] columnNames = { "Field Name", "SQL Type", "SQL Type Length",
                                 "isNullable", "isPrimaryKey",
                                 "isForeignKey", "refTableName" };
        
        
        
        JTable table = new JTable(data, columnNames);

        if( -1 != selectedRow )
        {
            table.setRowSelectionInterval( selectedRow, selectedRow );
        }
        
        
        return table;
    }

    
    private XToRMapping _getMapping()
    {
        SchemaPanel xsdPanel = SchemaPanel.getInstance();
        return xsdPanel.getMapping();
    }

    
    private void _paintMappingPanel( String headerText, Table t, String highlightedFieldName )
    {
        JLabel header = new JLabel( headerText );
        header.setFont(new Font("Serif", Font.PLAIN, 12));
        //header.setForeground(new Color(0xffcc99));
        header.setForeground( Color.blue );
        
        
        JTable table = _createTable( t, highlightedFieldName);
        
        //Create the scroll pane and add the table to it. 
        JScrollPane scrollPane = new JScrollPane(table);
        
        removeAll();
        add( header,BorderLayout.NORTH );
        add( scrollPane, BorderLayout.CENTER );
        
        revalidate();
        repaint();
    }
    
    private void _showSimpleElement( SchemaTreeNode node ) 
    {
        XToRMapping xtorMapping = _getMapping();
            
        XSSimpleElement se = node.getSimpleElement();
        SimplePath sp = se.getPath();
        
        String headerText = "";
        
        Table t = xtorMapping.getTable( sp );

        String highlightedFieldName = null;
        
        if( xtorMapping.mappedToTable(sp) )   //this simple element is outlined
        {    
            headerText = sp.toString(":") + " is mapped to table:" + t.getName();
        }
        else //either inlined or outlined in a group
        { 
            String fieldName = xtorMapping.getFieldName( sp );
            headerText = sp.toString(":") + " is mapped to field " + fieldName
                + " in the table " + t.getName();

            highlightedFieldName = fieldName;
        }        
        
        _paintMappingPanel( headerText, t, highlightedFieldName );
    }

    
    private void _showAttribute( SchemaTreeNode node )
    {
        XToRMapping xtorMapping = _getMapping();
        
        XSAttribute attr = node.getAttribute();
        String attrName = attr.getName();
        SimplePath parentElemPath = attr.getParentElemPath();
        
        String headerText = "";
        String highlightedFieldName = null;
        
        Table t = xtorMapping.getTable( parentElemPath, attrName );

        if( xtorMapping.mappedToTable(parentElemPath, attrName) )   //this attribute is outlined
        {   
            headerText = "Attribute " + attrName + " is mapped to table " + t.getName();
        }
        else
        {
            String fieldName = xtorMapping.getFieldName( parentElemPath, attrName );
            headerText = "Attribute " + attrName + " is mapped to field " + fieldName
                + " in the table " + t.getName();
            highlightedFieldName = fieldName;
        }
        
        _paintMappingPanel( headerText, t, highlightedFieldName);
    }
    
}


        
        
        
        
        
            










