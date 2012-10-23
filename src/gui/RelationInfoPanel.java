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


class RelationInfoPanel extends JSplitPane implements ListSelectionListener
{
    public RelationInfoPanel()
    {
        super( JSplitPane.VERTICAL_SPLIT );
        setDividerSize(4);
        setDividerLocation(200);
        
        SchemaPanel xsdPanel = SchemaPanel.getInstance();
        RelationalSchema rs = xsdPanel.getMapping().getRelationalSchema();

        
        //list
        java.util.List tables = rs.tables();
        m_tableArray = tables.toArray();
        
        String[] tableNames = new String[m_tableArray.length];
        for( int i = 0; i < m_tableArray.length; i++ )
        {
            Table t = (Table) m_tableArray[i];
            tableNames[i] = t.getName();
        }
        
        
        JList list = new JList( tableNames );
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.setSelectedIndex(0);
        list.addListSelectionListener(this);
        JScrollPane listScrollPane = new JScrollPane(list);

        setLeftComponent( listScrollPane );
        
        
        //table
        Table firstTable = (Table) m_tableArray[0];
        JTable jt = _createTable( firstTable );
        m_tableScrollPane = new JScrollPane(jt);
        
        setRightComponent( m_tableScrollPane );
    }

    private JTable _createTable( Table t )
    {
        Field[] fields = t.getFields();
        int numFields = fields.length;
        
        
        Object[][] data = new Object[numFields][7];

        for( int i = 0; i < numFields; i++ )
        {
            Field field = fields[i];

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

        return table;
    }

    //listener method impl
    public void valueChanged( ListSelectionEvent e )
    {
        if (e.getValueIsAdjusting())
            return;

        JList theList = (JList) e.getSource();

        if ( theList.isSelectionEmpty() )
        {
            return;
        }
        else
        {
            int index = theList.getSelectedIndex();
            
            Table t = (Table) m_tableArray[index];
            JTable jt = _createTable( t );
            
            remove( m_tableScrollPane );
            
            m_tableScrollPane = new JScrollPane(jt);

            setDividerSize(4);
            setDividerLocation(200);
            setRightComponent( m_tableScrollPane );
            
            revalidate();
        }
    }

    
    private Object[] m_tableArray;
    private JScrollPane m_tableScrollPane;
}
