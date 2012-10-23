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


/*
class LogoPanel extends JPanel
{
    public LogoPanel()
    {
        super();
        setLayout(new BorderLayout()); 
        ImageIcon logo = new ImageIcon("image/xslogo3.jpg");
        JLabel logoLabel = new JLabel(logo);
        add(logoLabel, BorderLayout.CENTER);
        Border etched = BorderFactory.createEtchedBorder( getBackground().darker(),
                                                          getBackground().brighter() );
        setBorder(etched);
        setBackground( Color.white );
    }
}
*/


class LogoPanel extends JPanel
{
    public LogoPanel()
    {
        super();

        try
        {
            JScrollPane scroller = SwingUtil.createHtmlPane( "image/xs.html" );
            setLayout(new BorderLayout());
            add(scroller, BorderLayout.CENTER);
        }
        catch( Throwable t )
        {
            t.printStackTrace();
        }
    }
    
}

