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

import edu.ogi.cse.xs.xsd.*;
import edu.ogi.cse.xs.xsd.xerces.*;
import edu.ogi.cse.xs.loader.*;
import edu.ogi.cse.xs.mapping.*;


class ViewPanel extends JPanel
{
    public ViewPanel()
    {
        m_logoPanel = null;
        
        setLayout(new BorderLayout());

        m_schemaPanelTable = new HashMap(5);
    }

    public void init()
    {
        showLogoView();
    }

    
    
    public void showLogoView()
    {
        JComponent currentView = _getCurrentView();
        if (currentView instanceof LogoPanel)
            return;
        
        if (m_logoPanel == null)
        {
            m_logoPanel = new LogoPanel();
        }
        removeAll();
        add(m_logoPanel, BorderLayout.CENTER);
        revalidate();
        repaint();
    }

    
    
    
    public void showSchemaView( XToRMapping xtorMapping )
    {
        showSchemaView( xtorMapping, false );
    }

    
    public void showSchemaView( XToRMapping xtorMapping, boolean force )
    {
        String validationMessage = xtorMapping.validateAnnotation();
        if( null != validationMessage )
        {
            JOptionPane.showMessageDialog( XSGui.getInstance(), validationMessage );  
        }

        
        XSInstance xs = xtorMapping.getXSInstance();
        
        //System.out.println( "show schema view: " + xs.getFilePath() );
        
        SchemaPanel xsdpanel = null;
        
        if( force )
        {
            m_schemaPanelTable.remove( xs.getFilePath() );
            xsdpanel = new SchemaPanel( xtorMapping );
            m_schemaPanelTable.put( xs.getFilePath(), xsdpanel );
        }
        else
        {
            Object o = m_schemaPanelTable.get( xs.getFilePath() );
        
            if( null == o )
            {
                xsdpanel = new SchemaPanel( xtorMapping );
                m_schemaPanelTable.put( xs.getFilePath(), xsdpanel );
            }
            else
            {
                xsdpanel = (SchemaPanel) o;
            }
        }
     
        removeAll();
        add( xsdpanel, BorderLayout.CENTER );
        revalidate();
        repaint();
    }

    
    public void showConnectionView()
    {
        ConnectionPanel connpane = new ConnectionPanel();
        removeAll();
        add( connpane, BorderLayout.CENTER );
        revalidate();
        repaint();
    }
    
    public void ShowAddConnectionView()
    {
        boolean added = ConnectionDialog.addConnection();

        //redisplay
        if( added )
        {
            showConnectionView();
        }
    }
    

       
    public void ShowRemoveConnectionView()
    {
        boolean removed = ConnectionDialog.removeConnection();

        //redisplay
        if( removed )
        {   
            showConnectionView();
        }
    }
    
        
    private JComponent _getCurrentView()
    {
        if (getComponentCount() == 1)
            return (JComponent)getComponent(0);
        return null;
    }


    private LogoPanel m_logoPanel;
    private Map m_schemaPanelTable;
    
}

