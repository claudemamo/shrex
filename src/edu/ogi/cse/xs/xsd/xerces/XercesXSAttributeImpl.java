/*
This file is part of ShreX.

ShreX is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation; either version 2 of the License, or (at your option) any later version.

ShreX is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.

You should have received a copy of the GNU General Public License along with Foobar; if not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA 
*/
package edu.ogi.cse.xs.xsd.xerces;


import java.util.List;

import org.apache.xerces.impl.xs.psvi.*;
import org.apache.xerces.impl.dv.*;


import edu.ogi.cse.xs.common.*;
import edu.ogi.cse.xs.conf.*;

import edu.ogi.cse.xs.xsd.*;

public class XercesXSAttributeImpl implements XSAttribute
{
    public XercesXSAttributeImpl( XSAttributeUse attruse )
    {
        m_attruse = attruse;
        m_attr = m_attruse.getAttrDeclaration();
        
        m_annotationAttrs = m_attr.getAnnotationAttrs();
        
        XercesUtil.echoAnnotation( m_annotationAttrs );
        
        m_parentElemPath = null; //to be set
    }


    //setter: package scope
    void setParentElemPath( SimplePath parentElemPath )
    {
        m_parentElemPath = parentElemPath;
    }


    //getters: all public
    public String getName(  )
    {
        return m_attr.getName();
    }

    
    public SimplePath getParentElemPath()
    {
        return m_parentElemPath;
    }
    

    public List getAnnotationAttributes()
    {
        return m_annotationAttrs;
    }    

    
    public boolean isListType()
    {
        XSSimpleTypeDefinition attrType = m_attr.getTypeDefinition();
        return ( attrType.getVariety() == XSSimpleTypeDefinition.VARIETY_LIST );
    }

    
    public boolean isUnionType()
    {
        XSSimpleTypeDefinition attrType = m_attr.getTypeDefinition();
        return ( attrType.getVariety() == XSSimpleTypeDefinition.VARIETY_UNION  );
    }

       
    public boolean isOptional()
    {
        return (! m_attruse.getRequired() );
    }
    
    
    public boolean isOutlined()
    {
        return XercesUtil.isOutlined( m_annotationAttrs );
    }

    
    public String getTableName()
    {
        String tableName = XercesUtil.getTableName( m_annotationAttrs );
        if( null == tableName )
        {
            tableName = getParentElemPath() + "." + getName();
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
                XSSimpleTypeDefinition attrType = m_attr.getTypeDefinition();
                XSSimpleType st = (XSSimpleType) attrType;
            
                short basicType = st.getPrimitiveKind();
                
                result = XercesUtil.convertToSQLType( basicType );
            }    
        }
        

        return result;
    }

    

    //TBA: now just work for VARCHAR and NUMBER
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
    

    public String toString()
    {
        String str = "";
        str += "Attribute: " + getName()
            + "\tSQL type: " + Constants. sqlTypeToString( getSQLType() )
            + "\tisOptional: " + isOptional()
            + "\toutlined: " + isOutlined() + "\n";
        
        return str;
    }
    

    public String getDataType()
    {
        String result = "";
        
        if( isListType() ) return "list type";
        if( isUnionType() ) return "union type";
        
        XSSimpleTypeDefinition attrType = m_attr.getTypeDefinition();
        XSSimpleType st = (XSSimpleType) attrType;
        short basicType = st.getPrimitiveKind();
        
        return XercesUtil.xsdTypeToString( basicType );
    }
        
    
    private XSAttributeUse m_attruse;
    private XSAttributeDeclaration m_attr;
    
    
    private List m_annotationAttrs;
    
    private SimplePath m_parentElemPath;
}
