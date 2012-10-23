/*
This file is part of ShreX.

ShreX is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation; either version 2 of the License, or (at your option) any later version.

ShreX is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.

You should have received a copy of the GNU General Public License along with Foobar; if not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA 
*/
//xxx
package edu.ogi.cse.xs.loader;

import java.io.*;
import java.util.*;

import org.apache.xerces.parsers.SAXParser;
import org.xml.sax.*;
import org.xml.sax.helpers.DefaultHandler;

import edu.ogi.cse.xs.common.*;

public class IntervalID extends DefaultHandler
{
    
    public IntervalID( String xmlfile )
    {
        m_xmlfile = xmlfile;
        m_idtable = new HashMap(100);
        m_idservice = IDService.getInstance( "_X_X_X_X_X" );
        m_count = 0;
        m_idstack =  new Stack();
    }

    
    public Map getIDTable()
    {
        return m_idtable;
    }

    
    public void process()
    {
        //  create a Xerces SAX parser
        SAXParser parser = new SAXParser();
        
        //set the content handler
        parser.setContentHandler(this);
        
        //  parse the document
        try
        {
            parser.parse(m_xmlfile);
        }
        catch (Throwable t)
        {
            t.printStackTrace();
        }

        m_idservice.reset();

        
        //System.out.println( m_idtable );
        
    }


     //Document start
    public void startDocument()
    {
        //System.out.println ("Begin to load document " + m_xmlfile );
    }
    
    
    // element start
    public void startElement (String uri, String local, String qName, Attributes atts)
    {
        //System.out.println( "start element " + local );
        
        String id = m_idservice.nextWithoutPrefix();
        m_idstack.push( id );
        
        m_count++;
        
        String intervalStr = "[" + m_count + " ";
        
        m_idtable.put( id, intervalStr );
    }


    public void characters(char[] text, int start, int length)
    {
        String content = new String(text, start, length);
        content = content.trim();
        
        //System.out.println ("See Character Data " + content );
        
        m_count += 2;
    }
    

    public void endElement (String uri, String local, String qName)
    {
        //System.out.println( "end element " + local );

        String id = (String) m_idstack.pop();
        String intervalStr = (String) m_idtable.get( id );

        m_count++;
        
        intervalStr += m_count + "]";

        m_idtable.put( id, intervalStr );
    }
    
            
    public void endDocument()
    {
        //System.out.println ("Finish loading document " + m_xmlfile );
    }

    private String m_xmlfile;
    private Map m_idtable;
    private IDService m_idservice;
    private int m_count;
    private Stack m_idstack;
}
