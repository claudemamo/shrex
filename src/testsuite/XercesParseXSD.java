/*
This file is part of ShreX.

ShreX is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation; either version 2 of the License, or (at your option) any later version.

ShreX is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.

You should have received a copy of the GNU General Public License along with Foobar; if not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA 
*/
package testsuite;

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

public class XercesParseXSD
{
    
    static public void main(String args[] )
    {
        if( args.length != 1 )
        {
            System.out.println( "usage: java xerces.ParseXSD xsdfilepath" );
            System.exit(1);
        }

        System.out.println( "Going to parse schema " + args[0] );
        

        // get DOM implementation
        DOMImplementationAS domImpl = (DOMImplementationAS)ASDOMImplementationImpl.getDOMImplementation();
        
        // create a new parser
        DOMASBuilder parser = domImpl.createDOMASBuilder();
        
        ASModel asmodel = null;
        
        try
        {
            asmodel = parser.parseASURI(args[0]);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            System.exit(1);
        }

        ASModelImpl model = (ASModelImpl) asmodel;
        Vector models = model.getInternalASModels();
        SchemaGrammar[] grammars = new SchemaGrammar[models.size()];
        for (int i = 0; i < models.size(); i++)
        {
            grammars[i] = ((ASModelImpl)models.elementAt(i)).getGrammar();
        }
        
        XSModel xs = new XSModelImpl(grammars);

        XSNamedMap elemmap = xs.getComponents( XSConstants.ELEMENT_DECLARATION );

        //assume the first top element is what we want
        XSElementDecl topElem = (XSElementDecl) elemmap.item(0);
        
        System.out.println( "Top Element is: " + topElem.getName() );

        echo( topElem.getAnnotationAttrs() );

        XSComplexTypeDefinition ct = (XSComplexTypeDefinition) topElem.getTypeDefinition();
        
        
        //attributes
        XSObjectList attruses = ct.getAttributeUses();
        if( null != attruses )
        {
            for( int i = 0; i < attruses.getLength(); i++ )
            {
                XSAttributeUse attruse = (XSAttributeUse) attruses.item(i);
                XSAttributeDeclaration attr = attruse.getAttrDeclaration();

                System.out.println( "has an attribute: " + attr.getName() );
                echo( attr.getAnnotationAttrs() );
            }
        }


        XSParticle particle = ct.getParticle();

        XSTerm term = particle.getTerm();
        XSModelGroup topGroup = (XSModelGroup) term;

        
        //traverse the group
        XSObjectList objs = topGroup.getParticles();


        for( int i = 0; i < objs.getLength(); i++ )
        {
            XSObject obj = objs.item(i);
            XSParticle p = (XSParticle) obj;
            XSTerm t = p.getTerm();
            
            //we got nested group
            if( t instanceof XSModelGroup  ) 
            {
                System.out.println( "has a group" );
                XSModelGroup g = (XSModelGroup) t;

                echo( g.getAnnotationAttrs() );
            }
            
            if( t instanceof XSElementDeclaration )
            {
                XSElementDeclaration e = (XSElementDeclaration) t;
                XSTypeDefinition et = e.getTypeDefinition();

                System.out.println( "has an element: " + e.getName() );

                                
                if( et.getTypeCategory() ==  XSTypeDefinition.COMPLEX_TYPE )
                {
                    System.out.println( e.getName() + " is complex type." );
                    echo( e.getAnnotationAttrs() );
                }
                else
                {
                    System.out.println( e.getName() + " is simple type." );
                    echo( e.getAnnotationAttrs() );
                }
            }
        }
        
    }


    static public void echo( List annotationAttrs )
    {
        if( annotationAttrs.size() > 0 )
        {
            for( int i = 0; i < annotationAttrs.size(); i++ )
            {
                String[] annotationAttr = (String[])annotationAttrs.get(i);
                
                System.out.println( annotationAttr[0] );
                System.out.println( annotationAttr[1] );
                System.out.println( annotationAttr[2] );
                
            }
        }
    }
    
        
}
