/*
This file is part of ShreX.

ShreX is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation; either version 2 of the License, or (at your option) any later version.

ShreX is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.

You should have received a copy of the GNU General Public License along with Foobar; if not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA 
*/
package edu.ogi.cse.xs.xsd.xerces;

import org.apache.xerces.dom3.DOMConfiguration;

import org.apache.xerces.dom.ASDOMImplementationImpl;
import org.apache.xerces.dom3.as.DOMImplementationAS;
import org.apache.xerces.dom3.as.ASModel;
import org.apache.xerces.dom3.as.DOMASBuilder;

import org.apache.xerces.dom.ASModelImpl;
import org.apache.xerces.impl.xs.psvi.*;
import org.apache.xerces.impl.dv.*;
import org.apache.xerces.impl.xs.*;

import org.w3c.dom.*;

import java.io.*;
import java.util.*;

import edu.ogi.cse.xs.common.*;

public class XercesUtil
{
    static final String NAMESPACES_FEATURE_ID = "http://xml.org/sax/features/namespaces";
    static final String VALIDATION_FEATURE_ID = "http://xml.org/sax/features/validation";
    static final String SCHEMA_VALIDATION_FEATURE_ID = "http://apache.org/xml/features/validation/schema";
    static final String SCHEMA_FULL_CHECKING_FEATURE_ID = "http://apache.org/xml/features/validation/schema-full-checking";
    static final boolean DEFAULT_SCHEMA_FULL_CHECKING = false;
    


    //parse xml schema
    static public XSModel build( String filename )
    {   
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
        
        try
        {
            
            asmodel = parser.parseASURI(filename);
            parser.setAbstractSchema(asmodel);
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
        
        return xs;
    }
    

    
    //parse xml document with xmlschema validation
    static public Document parse( String xmlfile, String xsdfile )
    {  
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
        
        return document;
    }
    
    static public Document parse( String xmlfile )
    {  
        // get DOM implementation
        DOMImplementationAS domImpl = (DOMImplementationAS)ASDOMImplementationImpl.getDOMImplementation();
        
        // create a new parser, and set the error handler
        DOMASBuilder parser = domImpl.createDOMASBuilder();
        /*
        DOMConfiguration config = parser.getConfig();
        
        
        boolean schemaFullChecking = DEFAULT_SCHEMA_FULL_CHECKING;
        
        
        // set the features. since we only deal with schema, some features have
        // to be true
        config.setParameter(NAMESPACES_FEATURE_ID, Boolean.TRUE);
        config.setParameter(VALIDATION_FEATURE_ID, Boolean.TRUE);
        
        config.setParameter(SCHEMA_VALIDATION_FEATURE_ID, Boolean.TRUE);
        config.setParameter(SCHEMA_FULL_CHECKING_FEATURE_ID, 
                            (schemaFullChecking)?Boolean.TRUE:Boolean.FALSE);
                            */
        Document document  = null;
        
        try
        {  
            //parse document
            document = parser.parseURI( xmlfile );
        }
        catch (Exception e)
        {
            e.printStackTrace();
            System.exit(1);
        }
        
        return document;
    }

    
    
    static public List getElemsByTagName( Element element, String name )
    {
        NodeList allChildren = element.getChildNodes();
        
        List result = new ArrayList( 10 );
        
        int size = allChildren.getLength();
        for( int i = 0 ;i < size; ++i )
        {
            Node next = allChildren.item(i);
            if ( next instanceof Element )
            {
                if ( ((Element)next).getLocalName().equals(name) )
                {
                    result.add(next);
                }
            }
        }
        
        return result;
    }


    //get a list of text node values under a "simple" element
    static public String getNodeValue( Node node )
    {
        String textValue = "";
        
        NodeList nodeList = node.getChildNodes();
        int size = nodeList.getLength();
        
        for( int i=0; i < size; ++i )
        {
            Node child = nodeList.item(i);
            
            if (child instanceof Text)
            {
                textValue += ((Text) child).getData();
            }
        }

        return textValue.trim();
    }
    
    

    //TBA: this type conversion is rough!
    static public int convertToSQLType( short xsdtype )
    {
        int c = Constants.SQL_STRING;
        
        switch( xsdtype )
        {
        case XSSimpleType.PRIMITIVE_STRING:
            c = Constants.SQL_STRING;
            break;
        case XSSimpleType.PRIMITIVE_BOOLEAN :
            c = Constants.SQL_BOOLEAN;
            break;
        case XSSimpleType.PRIMITIVE_DOUBLE:
            c = Constants.SQL_FLOAT;
            break;
        case XSSimpleType.PRIMITIVE_FLOAT :
            c = Constants.SQL_FLOAT;
            break;
        case XSSimpleType.PRIMITIVE_DECIMAL:
            c = Constants.SQL_INT;
            break;   
        case XSSimpleType.PRIMITIVE_DATE :
            c = Constants.SQL_DATE;
            break;
        default: //all the other types will be treated as string
            c = Constants.SQL_STRING;
            break;
        }
        
        return c;
    }

    static public String xsdTypeToString( short xsdtype )
    {
        String c = "unknown";
        
        switch( xsdtype )
        {
        case XSSimpleType.PRIMITIVE_STRING:
            c = "String";
            break;
        case XSSimpleType.PRIMITIVE_BOOLEAN :
            c = "Boolean";
            break;
        case XSSimpleType.PRIMITIVE_DOUBLE:
            c = "Double";
            break;
        case XSSimpleType.PRIMITIVE_FLOAT :
            c = "Float";
            break;
        case XSSimpleType.PRIMITIVE_DECIMAL:
            c = "Decimal";
            break;   
        case XSSimpleType.PRIMITIVE_DATE :
            c = "Date";
            break;
        default: //all the other types will be treated as string
            c = "String";
            break;
        }
        
        return c;
    }
    

    //whether a group is a choice group
    static public boolean isChoiceGroup( XSModelGroup xg )
    {
        if( null == xg ) return false;
        
        boolean result = false;
        
        short order = xg.getCompositor();
        if( order == XSModelGroup.COMPOSITOR_CHOICE  )
        {
            result = true;
        }
        
        return result;
    }

    
    static public boolean isSequenceGroup( XSModelGroup xg )
    {
        if( null == xg ) return false;
        
        boolean result = false;
        
        short order = xg.getCompositor();
        if( order == XSModelGroup.COMPOSITOR_SEQUENCE  )
        {
            result = true;
        }
        
        return result;
    }

       
    static public boolean isAllGroup( XSModelGroup xg )
    {
        if( null == xg ) return false;
        
        boolean result = false;
        
        short order = xg.getCompositor();
        if( order == XSModelGroup.COMPOSITOR_ALL  )
        {
            result = true;
        }
        
        return result;
    }

    
    static public boolean isOutlined( List annotationAttrs )
    {
        if( null == annotationAttrs || annotationAttrs.size() <= 0 ) return false;
        
        boolean result = false;
        
        for( int i = 0; i < annotationAttrs.size(); i++ )
        {
            String[] annotationAttr = (String[])annotationAttrs.get(i);
            
            String url = annotationAttr[0];
            String name = annotationAttr[1];
            String value = annotationAttr[2];
            
            //System.out.println( "isOutlined: name=" + name + ",value=" + value );

            if( url.equals( Constants.GLOADERNAMESPACE )
                && name.equals( Constants.OUTLINE )
                && value.equals( "true" ) )
            {
                result = true;
                break;
            }
        }

        return result;
    }
    
    
    static public String getTableName( List annotationAttrs )
    {
        if( null == annotationAttrs || annotationAttrs.size() <= 0 ) return null;
        
        String tablename = null;
        
        for( int i = 0; i < annotationAttrs.size(); i++ )
        {
            String[] annotationAttr = (String[])annotationAttrs.get(i);

            String url = annotationAttr[0];
            String name = annotationAttr[1];
            String value = annotationAttr[2];
            
            if( url.equals( Constants.GLOADERNAMESPACE )
                && name.equals( Constants.TABLENAME ) )
            {
                tablename = value;
                break;
            }
        }

        return tablename;   
    }

    
    static public String getFieldName( List annotationAttrs )
    {
        if( null == annotationAttrs || annotationAttrs.size() <= 0 ) return null;
        
        String fieldname = null;
        
        for( int i = 0; i < annotationAttrs.size(); i++ )
        {
            String[] annotationAttr = (String[])annotationAttrs.get(i);

            String url = annotationAttr[0];
            String name = annotationAttr[1];
            String value = annotationAttr[2];
            
            if( url.equals( Constants.GLOADERNAMESPACE )
                && name.equals( Constants.FIELDNAME ) )
            {
                fieldname = value;
                break;
            }
        }

        return fieldname;   
    }

    

    static public int getIdentityScheme( List annotationAttrs )
    {
        if( null == annotationAttrs || annotationAttrs.size() <= 0 ) return Constants.KEY_FK;
        
        int result = Constants.KEY_FK;
        
        for( int i = 0; i < annotationAttrs.size(); i++ )
        {
            String[] annotationAttr = (String[])annotationAttrs.get(i);
            String url = annotationAttr[0];
            String name = annotationAttr[1];
            String value = annotationAttr[2];

            if( url.equals( Constants.GLOADERNAMESPACE )
                && name.equals( Constants.IDENTITYSCHEME ) )
            {
                if( value.equals( Constants.DEWEY_STR ) )
                {
                    result = Constants.DEWEY;    
                    break;
                }

		if( value.equals( Constants.INTERVAL_STR ) )
                {
                    result = Constants.INTERVAL;    
                    break;
                }
            }
        }
        
        return result;
    }

    
    static public boolean mappedToClob( List annotationAttrs )
    {
        
        if( null == annotationAttrs || annotationAttrs.size() <= 0 ) return false;
         
        boolean result = false;
        
        for( int i = 0; i < annotationAttrs.size(); i++ )
        {
            String[] annotationAttr = (String[])annotationAttrs.get(i);
            String url = annotationAttr[0];
            String name = annotationAttr[1];
            String value = annotationAttr[2];
            
            //System.out.println( "mappedToClob: name=" + name + ",value=" + value );

            if( url.equals( Constants.GLOADERNAMESPACE )
                && name.equals( Constants.MAPTOCLOB )
                && value.equals( "true" ) )
            {
                result = true;    
                break;
            }
        }
        
        return result;
    }

    
    static public boolean edgeMapping( List annotationAttrs )
    {
        if( null == annotationAttrs || annotationAttrs.size() <= 0 ) return false;
        
        boolean result = false;
        
        for( int i = 0; i < annotationAttrs.size(); i++ )
        {
            String[] annotationAttr = (String[])annotationAttrs.get(i);
            String url = annotationAttr[0];
            String name = annotationAttr[1];
            String value = annotationAttr[2];

            if( url.equals( Constants.GLOADERNAMESPACE )
                && name.equals( Constants.EDGEMAPPING )
                && value.equals( "true" ) )
            {
                result = true;    
                break;
            }
        }
        
        return result;
    }
    
    
    //TBA: deal with more data types
    static public int getSQLType( List annotationAttrs )
    {
        if( null == annotationAttrs || annotationAttrs.size() <= 0 ) return Constants.SQL_UNKNOWN;
        
        int sqltype = Constants.SQL_UNKNOWN;
        
        for( int i = 0; i < annotationAttrs.size(); i++ )
        {
            String[] annotationAttr = (String[])annotationAttrs.get(i);
            
            String url = annotationAttr[0];
            String name = annotationAttr[1];
            String value = annotationAttr[2];

            //System.out.println( "getsqltype: name=" + name + ",value=" + value );
            
            if( url.equals( Constants.GLOADERNAMESPACE )
                && name.equals( Constants.SQLTYPE ) )
            {
                //value is in the format of VARCHAR(128), NUMBER(10) etc.
                //TBA: check input here
                StringTokenizer st = new StringTokenizer(value.trim(), "(");

                String typestr = st.nextToken();
                
                if( typestr.equals( Constants.VARCHAR ) )
                    sqltype = Constants.SQL_STRING;
                if( typestr.equals( Constants.NUMBER ) )
                    sqltype = Constants.SQL_INT;
                break;
            }
        }

        return sqltype;   
    }


    
    //TBA: deal with more data types
    static public int getTypeLen( List annotationAttrs )
    {
        if( null == annotationAttrs || annotationAttrs.size() <= 0 ) return -1;
        
        int sqllen = -1;
        
        for( int i = 0; i < annotationAttrs.size(); i++ )
        {
            String[] annotationAttr = (String[])annotationAttrs.get(i);
            
            String url = annotationAttr[0];
            String name = annotationAttr[1];
            String value = annotationAttr[2];
            
            if( url.equals( Constants.GLOADERNAMESPACE )
                && name.equals( Constants.SQLTYPE ) )
            {
                //value is in the format of VARCHAR(128), NUMBER(10) etc.
                //TBA: check input here
                StringTokenizer st = new StringTokenizer(value.trim(), "(");

                st.nextToken(); //skip the first token
                if( st.hasMoreTokens() )
                {
                    String lenstr = st.nextToken();
                    lenstr = lenstr.trim();
                    lenstr = lenstr.substring( 0, lenstr.length()-1 );  //remove ")"
                    sqllen = Integer.parseInt( lenstr );
                }
                break;
            }
        }

        return sqllen;   
    }


    static public void echoAnnotation( List annotationAttrs )
    {
        /*
        if( null == annotationAttrs )
        {
            System.out.println("annotation is null");
            return;
        }
        
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
        else
        {
            System.out.println("There is no annotation");
        }
        */
    }

    
    static public void main(String args[] )
    {
        if( args.length == 1 )
        {
            XSModel xs = build( args[0] );
        }
        else if( args.length == 2 )
        {
            String xmlfile = args[0];
            String xsdfile = args[1];
            Document doc = parse( xmlfile, xsdfile );
        }
        else
        {
            System.out.println( "no input file is specified" );
        }
    }        

    
}
