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


/*
  represent a group
  */

public class XercesXSGroupImpl implements XSGroup
{
    
    public XercesXSGroupImpl( XSParticle particle, SimplePath elemPath )
    {
        m_elemPath = elemPath;
        m_annotationAttrs = new ArrayList( 10 );
        m_ses = new ArrayList( 10 );
        m_inlineSes = new ArrayList( 10 );
        m_outlineSes = new ArrayList( 10 );
        m_ces = new ArrayList( 10 );
        m_groups = new ArrayList( 10 );
        
        XSTerm term = particle.getTerm();
        m_group = (XSModelGroup) term;
        
        if( m_group.isRefGroup() )
        {
            m_isRefGroup = true;
            m_refGroupName = m_group.getRefGroupName();
        }
        else
        {
            m_isRefGroup = false;
            m_refGroupName = null;
        }
        
        m_minOcc = particle.getMinOccurs();
        m_maxOcc = particle.getMaxOccurs();
        
        m_isChoiceGroup = XercesUtil.isChoiceGroup( m_group );
        m_isSequenceGroup = XercesUtil.isSequenceGroup( m_group );
        m_isAllGroup = XercesUtil.isAllGroup( m_group );

        
        m_annotationAttrs = m_group.getAnnotationAttrs();

        XercesUtil.echoAnnotation( m_annotationAttrs );
        

        //System.out.println( "traversing a group: " + m_isChoiceGroup + "," +  m_isSequenceGroup  + "," + m_isAllGroup );

        
        //traverse the group
        XSObjectList objs = m_group.getParticles();

        m_content = "(";
        String sep = null;
        if( m_isChoiceGroup ) sep = "|";
        else sep = ",";
        
        for( int i = 0; i < objs.getLength(); i++ )
        {
            XSObject obj = objs.item(i);
            XSParticle p = (XSParticle) obj;
            XSTerm t = p.getTerm();
            
            //we got nested group
            if( t instanceof XSModelGroup  ) 
            {
                //System.out.println( "has a group" );
                
                XercesXSGroupImpl g = new XercesXSGroupImpl( p, m_elemPath );
                m_content += g.getContent();
                m_groups.add( g );
            }
            
            if( t instanceof XSElementDeclaration )
            {
                XSElementDeclaration e = (XSElementDeclaration) t;
                XSTypeDefinition et = e.getTypeDefinition();

                //System.out.println( "has an element: " + e.getName() );

                SimplePath childsp = (SimplePath) m_elemPath.clone();
                childsp.append( e.getName() );
                
                if( et.getTypeCategory() ==  XSTypeDefinition.COMPLEX_TYPE )
                {
                    //System.out.println( e.getName() + " is complex type." );
                    m_ces.add( new XercesXSComplexElementImpl( p, childsp ) );
                }
                else
                {
                    //System.out.println( e.getName() + " is simple type." );
                    XercesXSSimpleElementImpl se = new XercesXSSimpleElementImpl( p, childsp );
                    m_ses.add( se );
                }
                
                m_content += e.getName();
            }
            
            if( i < objs.getLength() - 1 ) m_content += sep;
        }

        m_content += ")";
        
        
        if( getMinOcc() != 1 || getMaxOcc() != 1 )
        {
            String minOccStr = "", maxOccStr = "";
            minOccStr = ( getMinOcc() == 0 )?"0":(minOccStr+getMinOcc());
            maxOccStr = ( getMaxOcc() == SchemaSymbols.OCCURRENCE_UNBOUNDED )?"n":(maxOccStr+getMaxOcc());
            
            m_content += "{" + minOccStr + "," + maxOccStr + "}";
        }
    }

    
    
    //getters: public
    public SimplePath getElemPath()
    {
        return m_elemPath;
    }

    
    public String getName()
    {
        if( isRefGroup() ) return getRefGroupName();

        return null;
    }
    
    public boolean isRefGroup()
    {
        return m_isRefGroup;
    }

    
    public String getRefGroupName()
    {
        return m_refGroupName;
    }

    
    public boolean isChoiceGroup()
    {
        return m_isChoiceGroup;
    }

    
    public boolean isSequenceGroup()
    {
        return m_isSequenceGroup;
    }


    public boolean isAllGroup()
    {
        return m_isAllGroup;
    }


    public int getMinOcc()
    {
        return m_minOcc;
    }

    
    public int getMaxOcc()
    {
        return m_maxOcc;
    }
    

    public List getAnnotationAttributes()
    {
        return m_annotationAttrs;
    }    

    
    public List groups()
    {
        return m_groups;
    }

    public List complexElements()
    {
        return m_ces;
    }


    public List simpleElements()
    {
        return m_ses;
    }

    
    public List inlineSimpleElements()
    {
        return m_inlineSes;
    }
    
       
    public List outlineSimpleElements()
    {
        return m_outlineSes;
    }


    // a couple of important methods:
    public boolean isOutlined()
    {
        return XercesUtil.isOutlined( m_annotationAttrs );  
    }
    

    public String getTableName()
    {
        String tableName = XercesUtil.getTableName( m_annotationAttrs );
        
        if( null == tableName )
        {
            tableName = getRefGroupName();
        }

        //TBA: check error, table name can only be from annotation of ref group
        
        return tableName;
    }
    
    
    public String getContent()
    {
        return m_content;
    }    

    
    public String toString()
    {
        return m_content;
    }    


    //setters
    void addOutlineSimpleElement( XercesXSSimpleElementImpl se )
    {
        m_outlineSes.add( se );
    }
    
    void addInlineSimpleElement( XercesXSSimpleElementImpl se )
    {
        m_inlineSes.add( se );
    }
    
    
    private SimplePath m_elemPath;
    
    private XSModelGroup m_group;
       
    private int m_minOcc;
    private int m_maxOcc;
    
    private boolean m_isChoiceGroup;
    private boolean m_isSequenceGroup;
    private boolean m_isAllGroup;

    private boolean m_isRefGroup;
    private String m_refGroupName;

    private List m_annotationAttrs;
    
    private List m_ses;
    private List m_inlineSes;
    private List m_outlineSes;
    
    private List m_ces;
    private List m_groups;

    private String m_content;    
}


