/*
This file is part of ShreX.

ShreX is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation; either version 2 of the License, or (at your option) any later version.

ShreX is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.

You should have received a copy of the GNU General Public License along with Foobar; if not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA 
*/
package edu.ogi.cse.xs.xsd.xerces;


import java.util.*;

import org.apache.xerces.impl.xs.*;
import org.apache.xerces.impl.xs.psvi.*;
import org.apache.xerces.impl.dv.*;


import edu.ogi.cse.xs.common.*;
import edu.ogi.cse.xs.conf.*;

import edu.ogi.cse.xs.xsd.*;

public class XercesXSSimpleElementImpl implements XSSimpleElement
{
    public XercesXSSimpleElementImpl( XSParticle particle, SimplePath sp )
    {
        m_elem = (XSElementDeclaration) particle.getTerm();

        m_path = sp;            
        m_parentGroup = null; //set later
        
        //acc relative to its parent (group or element)
        m_minOcc = particle.getMinOccurs();
        m_maxOcc = particle.getMaxOccurs();
        
        
        //acc relative to its parent element, to be set
        m_accMinOcc = m_minOcc;  
        m_accMaxOcc = m_minOcc;
        
        
        m_annotationAttrs = m_elem.getAnnotationAttrs();
        
        XercesUtil.echoAnnotation( m_annotationAttrs );
    }

    //setters: available in packege only
    void setPath( SimplePath path )
    {
        m_path = path;
    }

    
    void setParentGroup( XercesXSGroupImpl parentGroup )
    {
        m_parentGroup = parentGroup;
    }

    
    void setAccMinOcc( int accMinOcc )
    {
        m_accMinOcc = accMinOcc * m_minOcc;
    }
    
     
    void setAccMaxOcc( int accMaxOcc )
    {
        m_accMaxOcc = accMaxOcc * m_maxOcc;
    }


    //getters: all public
    public String getName()
    {
        return m_elem.getName();
    }


    
    public SimplePath getPath()
    {
        return m_path;
    }
    
  
    public XSGroup getParentGroup()
    {
        return m_parentGroup;
    }

    
    public List getAnnotationAttributes()
    {
        return m_annotationAttrs;
    }
    
    
    public int getMinOcc()
    {
        return m_minOcc;
    }

    
    public int getMaxOcc()
    {
        return m_maxOcc;
    }
    

    public int getAccMinOcc()
    {
        return m_accMinOcc;
    }
    
    public int getAccMaxOcc()
    {
        return m_accMaxOcc;
    }
    
     
    public boolean isListType()
    {
        XSTypeDefinition type = m_elem.getTypeDefinition();
        XSSimpleType st = (XSSimpleType) type;
        
        return ( st.getVariety() == XSSimpleTypeDefinition.VARIETY_LIST );
    }

    
    public boolean isUnionType()
    {
        XSTypeDefinition type = m_elem.getTypeDefinition();
        XSSimpleType st = (XSSimpleType) type;
        
        return ( st.getVariety() == XSSimpleTypeDefinition.VARIETY_UNION );
    }

       
       
    public boolean isOptional()
    {
        if( m_accMinOcc > 1e-6 ) return false;

        return true;
    }


    public boolean isOutlined()
    {
        if( getParentGroup().isOutlined() ) return true;
        
        if( getAccMaxOcc() == SchemaSymbols.OCCURRENCE_UNBOUNDED
            || getAccMaxOcc() > 1 )
            return true;

        return XercesUtil.isOutlined( m_annotationAttrs );
    }

    
    public String getTableName()
    {
        String tableName = XercesUtil.getTableName( m_annotationAttrs );
        
        if( null == tableName )
        {
            tableName = m_path.toString();
        }
        
        return tableName;
    }
    

    public String getFieldName()
    {
        String fieldName = XercesUtil.getFieldName( m_annotationAttrs );
        if( null == fieldName )
        {
            fieldName = getName();
        }
        
        return fieldName;
    }   
    
    
    public int getSQLType()
    {
        int result = XercesUtil.getSQLType( m_annotationAttrs );
        
        if( result == Constants.SQL_UNKNOWN )
        {
            if( isListType() || isUnionType() )
            {
                result =  Constants.SQL_STRING;        
            }
            else
            {
                XSTypeDefinition type = m_elem.getTypeDefinition();
                XSSimpleType st = (XSSimpleType) type;
                    
                short basicType = st.getPrimitiveKind();
                
                result =  XercesUtil.convertToSQLType( basicType );
            }
        }

        return result;
    }
    
    
    public int getSQLTypeLen()
    {
        int result = XercesUtil.getTypeLen( m_annotationAttrs );
        
        Configuration conf = Configuration.getInstance();
        
        if( getSQLType() == Constants.SQL_STRING )
        {
            if( -1 == result )
            {
                result = conf.getDefaultVarcharLength();
            }
        }
        if( getSQLType() == Constants.SQL_INT )
        {
            if( -1 == result )
            {
                result = conf.getDefaultNumberLength();
            }
        }
        
        return result;
    }


    public boolean isInChoiceGroup()
    {
        return ( null != m_parentGroup && m_parentGroup.isChoiceGroup() );    
    }

    
    //return: a list of simple elem's names
    public List otherElemsInSameChoicGroup()
    {
        List result = new ArrayList(1);
        if( null != m_parentGroup && m_parentGroup.isChoiceGroup() )
        {
            List ses = m_parentGroup.simpleElements();
            
            for( int i = 0; i < ses.size(); i++ )
            {
                XSSimpleElement tse = (XSSimpleElement) ses.get(i);
                String sename = tse.getName();
                if( !sename.equals( getName() ) )
                {
                    result.add( sename );
                }            
            }
        }

        return result;
    }
    
    //return: a list of field's names
    public List otherFieldsInSameChoicGroup()
    {
        List result = new ArrayList(1);
        if( null != m_parentGroup && m_parentGroup.isChoiceGroup() )
        {
            List ses = m_parentGroup.simpleElements();
            
            for( int i = 0; i < ses.size(); i++ )
            {
                XSSimpleElement tse = (XSSimpleElement) ses.get(i);
                String sename = tse.getName();
                if( !sename.equals( getName() ) )
                {
                    String fieldName = tse.getFieldName();
                    result.add( fieldName );
                }            
            }
        }

        return result;
    }
    
            
   
 
    public String toString()
    {
        String str = "";
        str += "SimpleElement: " + getPath()
            + "\tSQL type: " + Constants. sqlTypeToString( getSQLType() )
            + "\tisOptional: " + isOptional() +"{" + m_accMinOcc + ","
            + m_accMaxOcc + "}"
            + "\toutlined: " + isOutlined() + "\n";
        
        return str;
    }

    
    public String getDataType()
    {
        String result = "";
        
        if( isListType() ) return "list type";
        if( isUnionType() ) return "union type";
        
        
        XSTypeDefinition type = m_elem.getTypeDefinition();
        XSSimpleType st = (XSSimpleType) type;
                    
        short basicType = st.getPrimitiveKind();

        return XercesUtil.xsdTypeToString( basicType );
    }
        
    
    private XSElementDeclaration m_elem;
    private SimplePath m_path;
    private XercesXSGroupImpl m_parentGroup;
    
    private List m_annotationAttrs;
    
    private int m_minOcc;
    private int m_maxOcc;
    private int m_accMinOcc;
    private int m_accMaxOcc;
    
}
