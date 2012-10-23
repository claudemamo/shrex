/*
This file is part of ShreX.

ShreX is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation; either version 2 of the License, or (at your option) any later version.

ShreX is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.

You should have received a copy of the GNU General Public License along with Foobar; if not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA 
*/
package gui;

import java.io.*;
import java.util.*;
import java.net.*;

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
import edu.ogi.cse.xs.loader.*;
import edu.ogi.cse.xs.mapping.*;

class SwingUtil
{
    static public Font defaultFont = new Font("Dialog", Font.PLAIN, 12);
    static public Font boldFont = new Font("Dialog", Font.BOLD, 12);
    static public Font bigFont = new Font("Dialog", Font.PLAIN, 18);
    static public Font bigBoldFont = new Font("Dialog", Font.BOLD, 18);
    static public Font reallyBigFont = new Font("Dialog", Font.PLAIN, 18);
    static public Font reallyBigBoldFont = new Font("Dialog", Font.BOLD, 24);
    
    public SwingUtil()
    {}
    

    static public Border createTitledBorder( String title )
    {   
        Border buttonBorder = new TitledBorder(null, title, 
                                               TitledBorder.LEFT, TitledBorder.TOP,
                                               bigFont);

        Border emptyBorder = new EmptyBorder(5,5,5,5);
        Border compoundBorder = new CompoundBorder( buttonBorder, emptyBorder);

        return compoundBorder;
    }

    static public JScrollPane createHtmlPane( String htmlfilepath )
    {
        JScrollPane scroller = null;
    
        try
        {
            /*
            File htmlfile = new File( htmlfilepath );
            String path = htmlfile.getAbsolutePath();
            String htmlUrlStr = "file://" + path;
            URL url = new URL( htmlUrlStr );
            */

            URL url = createURL( htmlfilepath );
            
            
            JEditorPane editor = new JEditorPane(url);
            editor.setEditable(false);
            
            scroller = new JScrollPane(editor);
        }
        catch( Throwable t )
        {
            t.printStackTrace();
        }
        
        return scroller;
    }

    // Helper method to create a URL from a file name
    static URL createURL(String fileName)
    {
        URL url = null;
        try
        {
            url = new URL(fileName);
        }
        catch (MalformedURLException ex)
        {
            File f = new File(fileName);
            try
            {
                String path = f.getAbsolutePath();
                // This is a bunch of weird code that is required to
                // make a valid URL on the Windows platform, due
                // to inconsistencies in what getAbsolutePath returns.
                String fs = System.getProperty("file.separator");
                if (fs.length() == 1)
                {
                    char sep = fs.charAt(0);
                    if (sep != '/')
                        path = path.replace(sep, '/');
                    if (path.charAt(0) != '/')
                        path = '/' + path;
                }
                path = "file://" + path;
                System.out.println( path );
                url = new URL(path);
            }
            catch (MalformedURLException e)
            {
                System.out.println("Cannot create url for: " + fileName);
                System.exit(0);
            }
        }
        return url;
    }
    
}

