/*
This file is part of ShreX.

ShreX is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation; either version 2 of the License, or (at your option) any later version.

ShreX is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.

You should have received a copy of the GNU General Public License along with Foobar; if not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA 
*/
package testsuite;

import  org.w3c.dom.*;
import org.apache.xerces.dom.ASDOMImplementationImpl;
import org.apache.xerces.dom3.as.*;

import  org.apache.xerces.dom.DocumentImpl;
import  org.apache.xerces.dom.DOMImplementationImpl;
import  org.w3c.dom.Document;
import  org.apache.xml.serialize.OutputFormat;
import  org.apache.xml.serialize.Serializer;
import  org.apache.xml.serialize.SerializerFactory;
import  org.apache.xml.serialize.XMLSerializer;
import  java.io.*;

/**
 * Simple Sample that:
 * - Generate a DOM from Scratch.
 * - Output DOM to a String using Serializer
 */
public class DOMGenerate
{
    static public void main( String[] argv ) {
        try
        {
            Document doc= new DocumentImpl();
            Element root = doc.createElement("person");     // Create Root Element
            Element item = doc.createElement("name");       // Create element
            item.appendChild( doc.createTextNode("Jeff") );
            root.appendChild( item );                       // atach element to Root element
            item = doc.createElement("age");                // Create another Element
            item.appendChild( doc.createTextNode("28" ) );       
            root.appendChild( item );                       // Attach Element to previous element down tree
            item = doc.createElement("height");            
            item.appendChild( doc.createTextNode("1.80" ) );
            root.appendChild( item );                       // Attach another Element - grandaugther
            doc.appendChild( root );                        // Add Root to Document


            OutputFormat    format  = new OutputFormat( doc );   //Serialize DOM
            format.setIndenting( true );
          
            //StringWriter  stringOut = new StringWriter();        //Writer will be a String

            FileOutputStream fos = new FileOutputStream( "test.xml" );
            //XMLSerializer    serial = new XMLSerializer( stringOut, format );
            XMLSerializer    serial = new XMLSerializer( fos, format );
            serial.asDOMSerializer();                            // As a DOM Serializer

            serial.serialize( doc.getDocumentElement() );

            //System.out.println( "STRXML = " + stringOut.toString() ); //Spit out DOM as a String

            //read it back
            // get DOM implementation
            DOMImplementationAS domImpl = (DOMImplementationAS)ASDOMImplementationImpl.getDOMImplementation();
        
            // create a new parser, and set the error handler
            DOMASBuilder parser = domImpl.createDOMASBuilder();  
      
            //parse document
            Document document = parser.parseURI( "test.xml" );
            Element top = doc.getDocumentElement();
            
            System.out.println( top.getTagName() );
            
        }
        catch ( Exception ex )
        {
            ex.printStackTrace();
        }
    }
}

