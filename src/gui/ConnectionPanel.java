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
import edu.ogi.cse.xs.database.*;
import edu.ogi.cse.xs.database.file.*;
import edu.ogi.cse.xs.loader.*;
import edu.ogi.cse.xs.mapping.*;


class ConnectionPanel extends JPanel
{
    public ConnectionPanel()
    {
        super();
        setLayout(new BorderLayout());
        
        DataBase db = XSGui.getInstance().getDB();
        m_connections = db.connections();

        if( null == m_connections || m_connections.size() <= 0 )
        {
            JLabel noConnectionLabel = new JLabel("There is no connection defined. ");
            add( noConnectionLabel, BorderLayout.CENTER );
        }
        else
        {
            //left hand side

            //top left: connections
            int nConns = m_connections.size();
            String[] connNames = new String[nConns];
            
            for( int i = 0; i < m_connections.size(); i++ )
            {
                DBConnection conn = (DBConnection) m_connections.get(i);
                connNames[i] = conn.getName();
            }
            
            JList connList = new JList( connNames );
            connList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            connList.setSelectedIndex(0);
            connList.addListSelectionListener(new ConnListSelectionListener());
            m_connListScrollPane = new JScrollPane(connList);

            m_connListScrollPane.setBorder(SwingUtil.createTitledBorder("Connections"));

            

            //bottom left: tables
            m_selectedDBConn = (DBConnection) m_connections.get(0);
            m_tableNames = m_selectedDBConn.getTableNames();

            if( null == m_tableNames )
            {
                JLabel notableLabel = new JLabel( "no table" );
                m_tableListScrollPane = new JScrollPane( notableLabel );
            }
            else
            {    
                JList tableList = new JList( m_tableNames );
                tableList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
                tableList.setSelectedIndex(0);
                tableList.addListSelectionListener(new TableListSelectionListener());
                m_tableListScrollPane = new JScrollPane(tableList);
            }
            
            m_tableListScrollPane.setBorder(SwingUtil.createTitledBorder("Tables"));

            
            //right
	    if( null == m_tableNames ){
		 m_rightScrollPane = new JScrollPane();
	    }
	    else{
		JTable table = _createTable( m_tableNames[0] );
		m_rightScrollPane = new JScrollPane(table);
	    }
            m_rightScrollPane.setBorder(SwingUtil.createTitledBorder("Table Data"));
            
            
            m_leftSplitPane = new JSplitPane( JSplitPane.VERTICAL_SPLIT,
                                              m_connListScrollPane,
                                              m_tableListScrollPane);
            
            m_leftSplitPane.setDividerSize(4);
            m_leftSplitPane.setDividerLocation(200);
            
            
            m_entireSplitPane = new JSplitPane( JSplitPane.HORIZONTAL_SPLIT,
                                                m_leftSplitPane,
                                                m_rightScrollPane );
            
            m_entireSplitPane.setDividerSize(4);
            m_entireSplitPane.setDividerLocation(200);
            
            add( m_entireSplitPane, BorderLayout.CENTER );
        }
        
    }

    
   
    
    private JTable _createTable( String tableName )
    {
        JTable table = null;
        
        if( m_selectedDBConn instanceof FileConnection )
        {
            FileConnection filedb = (FileConnection) m_selectedDBConn;
            
            TableSorter sorter = new TableSorter();
            
            FileConnectionAdapter filedbAdapter = new FileConnectionAdapter( filedb );
            
            //System.out.println( "fetch " + tableName );
            
            filedbAdapter.fetch( tableName );
            
            //System.out.println( "done fetching " + tableName );

            filedb.close();
            
            sorter.setModel(filedbAdapter);
            
            // Create the table
            table = new JTable(sorter);
            
            table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF); 
            
            // Install a mouse listener in the TableHeader as the sorter UI.
            sorter.addMouseListenerToHeaderInTable(table);
        }
        else
        {
            TableSorter sorter = new TableSorter();
            
            ConnectionAdapter connAdapter = new ConnectionAdapter( m_selectedDBConn );
            
            //System.out.println( "fetch " + tableName );
            
            connAdapter.fetch( tableName );
            
            //System.out.println( "done fetching " + tableName );
            
            m_selectedDBConn.close();
            
            sorter.setModel(connAdapter);
            
            // Create the table
            table = new JTable(sorter);
            
            table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF); 
            
            // Install a mouse listener in the TableHeader as the sorter UI.
            sorter.addMouseListenerToHeaderInTable(table);
        }

        return table;
    }
    
            
    class ConnListSelectionListener implements ListSelectionListener
    {
        public ConnListSelectionListener()
        {
        }
        
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
                m_leftSplitPane.remove( m_tableListScrollPane );
                
                int index = theList.getSelectedIndex();
                m_selectedDBConn = (DBConnection) m_connections.get(index);
                m_tableNames = m_selectedDBConn.getTableNames();
                if( null == m_tableNames )
                {
                    JLabel notableLabel = new JLabel( "no table" );
                    m_tableListScrollPane = new JScrollPane( notableLabel );
                }
                else
                {
                    JList tableList = new JList( m_tableNames );
                    tableList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
                    tableList.setSelectedIndex(0);
                    tableList.addListSelectionListener(new TableListSelectionListener());
             
                    m_tableListScrollPane = new JScrollPane(tableList);
                    
                    m_leftSplitPane.setRightComponent( m_tableListScrollPane );
                    
                    m_leftSplitPane.setDividerSize(4);
                    m_leftSplitPane.setDividerLocation(200);
                    
                    m_leftSplitPane.revalidate();

                    //also update the right pane
                    m_entireSplitPane.remove( m_rightScrollPane );
                    String tableName = m_tableNames[0];

                    JTable table = _createTable( tableName );
                
                    m_rightScrollPane = new JScrollPane(table);
                    m_rightScrollPane.setBorder(SwingUtil.createTitledBorder("Table Data"));

                
                    m_entireSplitPane.setRightComponent( m_rightScrollPane );
                
                    m_entireSplitPane.setDividerSize(4);
                    m_entireSplitPane.setDividerLocation(200);
                    
                    m_entireSplitPane.revalidate();
                }

                m_tableListScrollPane.setBorder(SwingUtil.createTitledBorder("Tables"));
                
            }
        
        }
    }
    
    
    class TableListSelectionListener implements ListSelectionListener
    {
        public TableListSelectionListener()
        {
        }
        
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
                m_entireSplitPane.remove( m_rightScrollPane );

                int index = theList.getSelectedIndex();
                String tableName = m_tableNames[index];

                JTable table = _createTable( tableName );
                
                m_rightScrollPane = new JScrollPane(table);
                m_rightScrollPane.setBorder(SwingUtil.createTitledBorder("Table Data"));

                
                m_entireSplitPane.setRightComponent( m_rightScrollPane );
                
                m_entireSplitPane.setDividerSize(4);
                m_entireSplitPane.setDividerLocation(200);
                
                m_entireSplitPane.revalidate();
            }
        }
        
    }
    

 

    
    private JScrollPane m_connListScrollPane;
    private JScrollPane m_tableListScrollPane;
    private JScrollPane m_rightScrollPane;

    private JSplitPane m_leftSplitPane;
    private JSplitPane m_entireSplitPane;
    
    private java.util.List m_connections;
    
    private DBConnection m_selectedDBConn;
    private String[] m_tableNames;  
}


