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


class SchemaPanel extends JPanel
{
    public SchemaPanel( XToRMapping xtorMapping )
    {
        super();

        s_xsdPanel = this;
        
        m_xtorMapping = xtorMapping;
        m_xs = m_xtorMapping.getXSInstance();
        
        setLayout(new BorderLayout());

        m_xsdtree = _buildSchemaTree();

        
        //Create the scroll pane and add the tree to it. 
        JScrollPane treePane = new JScrollPane(m_xsdtree);
        treePane.setBorder(SwingUtil.createTitledBorder("Schema Tree"));

        
        //create a tabbed pane in right
        m_tabbedPane = new JTabbedPane();
        
        
        m_xsdinfoPane = new XSDInfoPanel();
        m_tabbedPane.addTab("XML Schema", m_xsdinfoPane );
        m_tabbedPane.setSelectedIndex(0);
        
        m_mappinginfoPane = new MappingInfoPanel();
        m_tabbedPane.addTab("About Mappings", m_mappinginfoPane );
        
        RelationInfoPanel relationInfoPane = new RelationInfoPanel();
        m_tabbedPane.addTab("Relational Schema", relationInfoPane );

        NativeXSDPanel nativeXSDPanel = new NativeXSDPanel(m_xs);
        m_tabbedPane.addTab("Mapping Editor", nativeXSDPanel );

        
        m_queryPanel = new QueryPanel();
        m_tabbedPane.addTab("Query", m_queryPanel );
        
        
        JPanel rightPane = new JPanel();
        rightPane.setLayout(new GridLayout(1, 1)); 
        rightPane.add(m_tabbedPane);
        
	JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, 
                                              treePane, rightPane );    
       
        splitPane.setDividerSize(4);
        splitPane.setDividerLocation(200);
        
        add(splitPane, BorderLayout.CENTER);        
        
    }

    
    SchemaTree _buildSchemaTree()
    {
        //Create the root
        XSComplexElement topElem = m_xs.getTopElement();
        SchemaTreeNode root = new SchemaTreeNode( topElem );
        
        root.explore();
        DefaultTreeModel model =  new DefaultTreeModel( root );
        
        SchemaTree xsdTree = new SchemaTree( model );

        return xsdTree;
    }
    

    public int getDisplayMode()
    {   
        return m_tabbedPane.getSelectedIndex();
    }


    public void showSchemaDetail( SchemaTreeNode node )
    {
        m_xsdinfoPane.showSchemaDetail( node );
    }
    
    public void showMappingDetail( SchemaTreeNode node )
    {
        m_mappinginfoPane.showMappingDetail( node );   
    }


    public void addDefaultPath( SchemaTreeNode node )
    {
        m_queryPanel.addDefaultPath( node );
    }
    
    
    static public SchemaPanel getInstance()
    {
        return s_xsdPanel;
    }

    
    public XToRMapping getMapping()
    {
        return m_xtorMapping;
    }

    
    //refer to myself
    static public SchemaPanel s_xsdPanel;
    
    private XSInstance m_xs;
    private XToRMapping m_xtorMapping;
    private SchemaTree m_xsdtree;
    private JTabbedPane m_tabbedPane;

    
    private XSDInfoPanel m_xsdinfoPane;
    private MappingInfoPanel m_mappinginfoPane;
    private QueryPanel m_queryPanel;
    
    
    static public final int SCHEMA = 0;
    static public final int MAPPING = 1;
    static public final int RELATION = 2;
    static public final int NATIVE = 3;
    static public final int QUERY = 4;
}


