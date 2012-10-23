/*
This file is part of ShreX.

ShreX is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation; either version 2 of the License, or (at your option) any later version.

ShreX is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.

You should have received a copy of the GNU General Public License along with Foobar; if not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA 
*/
package testsuite;

import org.apache.xerces.dom3.DOMConfiguration;

import org.apache.xerces.dom.ASDOMImplementationImpl;
import org.apache.xerces.dom3.as.DOMImplementationAS;
import org.apache.xerces.dom3.as.DOMASBuilder;
import org.apache.xerces.dom3.as.ASModel;
import org.apache.xerces.dom.ASModelImpl;

import org.apache.xerces.impl.xs.psvi.*;
import org.apache.xerces.impl.dv.*;
import org.apache.xerces.impl.xs.*;

import org.w3c.dom.*;

import java.util.*;

public class XercesDom
{
    static final String NAMESPACES_FEATURE_ID = "http://xml.org/sax/features/namespaces";
    static final String VALIDATION_FEATURE_ID = "http://xml.org/sax/features/validation";
    static final String SCHEMA_VALIDATION_FEATURE_ID = "http://apache.org/xml/features/validation/schema";
    static final String SCHEMA_FULL_CHECKING_FEATURE_ID = "http://apache.org/xml/features/validation/schema-full-checking";
    static final boolean DEFAULT_SCHEMA_FULL_CHECKING = false;

    
    static public void main(String args[] )
    {
        if( args.length != 2 )
        {
            System.out.println( "usage: java testsuite.XercesDom xmlfilepath xsdfilepath" );
            System.exit(1);
        }
        
        String xmlfile = args[0];
        String xsdfile  = args[1];
        
        // get DOM implementation
        DOMImplementationAS domImpl = (DOMImplementationAS)ASDOMImplementationImpl.getDOMImplementation();
        
        // create a new parser, and set the error handler
        DOMASBuilder parser = domImpl.createDOMASBuilder();
        DOMConfiguration config = parser.getConfig();
        
        
        boolean schemaFullChecking = DEFAULT_SCHEMA_FULL_CHECKING;
        
        
        // set the features. since we only deal with schema, some features have
        // to be true
        config.setParameter(NAMESPACES_FEATURE_ID, Boolean.TRUE);
        config.setParameter(VALIDATION_FEATURE_ID, Boolean.TRUE);
        
        config.setParameter(SCHEMA_VALIDATION_FEATURE_ID, Boolean.TRUE);
        config.setParameter(SCHEMA_FULL_CHECKING_FEATURE_ID, 
                            (schemaFullChecking)?Boolean.TRUE:Boolean.FALSE);
        
        ASModel asmodel = null;
        Document document  = null;
        
        try
        {
            //parse schema
            asmodel = parser.parseASURI(xsdfile);
            parser.setAbstractSchema(asmodel);
            
            //parse document
            document = parser.parseURI( xmlfile );
        }
        catch (Exception e)
        {
            e.printStackTrace();
            System.exit(1);
        }
        
        System.out.println( document.getDocumentElement().getLocalName() );
    }
}
