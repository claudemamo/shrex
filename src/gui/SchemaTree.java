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


class SchemaTree extends JTree
{
    public SchemaTree( DefaultTreeModel model )
    { 
        super( model );

        getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);

        putClientProperty("JTree.lineStyle", "Angled");
        
        //Enable tool tips.
        ToolTipManager.sharedInstance().registerComponent(this);
        
        setCellRenderer(new SchemaTreeRenderer());

        
        addTreeExpansionListener(new TreeExpansionListener(){
            
          public void treeCollapsed(TreeExpansionEvent e)
              {
              }
                        
          public void treeExpanded( TreeExpansionEvent e )
              {
                  TreePath path = e.getPath();
                  SchemaTreeNode node = (SchemaTreeNode) path.getLastPathComponent();

                  if (node.isComplexElement())
                  {
                      XSComplexElement ce = node.getComplexElement();
                      
                      if( ce.isInRecursivePath() )
                      {
                          String message = ce.getPath().toString(":")
                              + " is in a recursive path!";
                          JOptionPane.showMessageDialog(XSGui.getInstance(), message);
                          
                          return;
                      }
                  }
                  
                  
                  if( ! node.isExplored())
                  {
                      DefaultTreeModel defaultModel = (DefaultTreeModel) getModel();
                      node.explore();
                      defaultModel.nodeStructureChanged(node);
                  }
              }
        });


        addTreeSelectionListener( new TreeSelectionListener() {
          public void valueChanged(TreeSelectionEvent e)
              {   
                  SchemaTreeNode node = (SchemaTreeNode) getLastSelectedPathComponent();
                  
                  if( node == null ) return;

                  SchemaPanel xsdPanel = SchemaPanel.getInstance();

                  
                  int mode = xsdPanel.getDisplayMode();
                  
                  if( mode == SchemaPanel.SCHEMA )
                  {
                      xsdPanel.showSchemaDetail( node );
                  }
                  else if( mode == SchemaPanel.MAPPING )
                  {
                      xsdPanel.showMappingDetail( node );
                  }
                  else if( mode == SchemaPanel.QUERY )
                  {
                      xsdPanel.addDefaultPath( node );
                  }
                  
                  else if( mode == SchemaPanel.RELATION )
                  {
                      //do nothing
                  }
                  else if( mode == SchemaPanel.NATIVE )
                  {
                      //do nothing
                  }
                  else
                  {
                      //throw exception
                  }
              }
        });
    }    
}




