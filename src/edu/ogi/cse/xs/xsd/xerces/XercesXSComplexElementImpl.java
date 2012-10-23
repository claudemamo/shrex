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

import edu.ogi.cse.xs.xsd.*;
import edu.ogi.cse.xs.common.*;



public class XercesXSComplexElementImpl implements XSComplexElement
{
    //corresponding to the global top element
    public XercesXSComplexElementImpl( XSElementDecl elem, SimplePath sp )
    {
        m_elem = elem;
        m_parentPaths = new ArrayList(1);  
        _setPath( sp ); 
        
        m_parentGroup = null; //set later

        m_isInRecursivePath = false;
        
        //acc relative to its parent (group or element)
        m_minOcc = 1;  
        m_maxOcc = 1;
        
        //acc relative to its parent element, to be set
        m_accMinOcc = m_minOcc;  
        m_accMaxOcc = m_minOcc;
        
        init();
    }
    
  
    //local element
    public XercesXSComplexElementImpl( XSParticle particle, SimplePath sp )
    {   
        m_elem = (XSElementDeclaration) particle.getTerm();
        m_parentPaths = new ArrayList(1);
        _setPath( sp );     
        
        m_parentGroup = null; //set later  
        
        //acc relative to its parent (group or element)
        m_minOcc = particle.getMinOccurs();
        m_maxOcc = particle.getMaxOccurs();
        
        //acc relative to its parent element, to be setted
        m_accMinOcc = m_minOcc;  
        m_accMaxOcc = m_minOcc;
        

        if( _isInRecursivePath(sp) )
        {
            m_isInRecursivePath = true;
        }
        else
        {
            m_isInRecursivePath = false;
            init();
        }
    }

    
    
    void init()
    {

        //System.out.println( "init " + getName() );
        
        //1. init internal fields
        m_contentModel = null;
        
        m_attrs = new ArrayList( 10 );
        m_inlineAttrs = new ArrayList( 10 );
        m_outlineAttrs = new ArrayList( 10 );

        //to be set
        m_ses = new ArrayList( 10 );
        m_inlineSes = new ArrayList( 10 );
        m_outlineSes = new ArrayList( 10 );

        //to be set
        m_ces = new ArrayList( 10 );
        
        m_outlineGroups = new ArrayList( 10 );


        m_distributedElems = new ArrayList(1);
        
        //2. get its type definition
        XSComplexTypeDefinition ct = (XSComplexTypeDefinition) m_elem.getTypeDefinition();
        
        if( null != ct.getName() && ct.getName().equals( SchemaSymbols.ATTVAL_ANYTYPE  ) )
        {
            m_isAnyType = true;
        }
        else
        {
            m_isAnyType = false;
        }

        m_contentType = ct.getContentType();
        

        
        //attributes
        //TBA: deal with attribute group
        XSObjectList attruses = ct.getAttributeUses();
        if( null != attruses )
        {
            for( int i = 0; i < attruses.getLength(); i++ )
            {
                XSAttributeUse attruse = (XSAttributeUse) attruses.item(i);
                XSAttributeDeclaration attr = attruse.getAttrDeclaration();
                
                XercesXSAttributeImpl xsattr = new XercesXSAttributeImpl( attruse );
                
                m_attrs.add( xsattr );
                
                if( xsattr.isOutlined() )
                {
                    m_outlineAttrs.add( xsattr );
                }
                else
                {
                    m_inlineAttrs.add( xsattr );
                }
                
            }
        }
        
        m_annotationAttrs = m_elem.getAnnotationAttrs();

        XercesUtil.echoAnnotation( m_annotationAttrs );

        //particle is the top group
        XSParticle particle = ct.getParticle();

        if( null != particle )
        {
            m_contentModel = new XercesXSContentModelImpl( particle, m_path );
        }
        else
        {
            //System.out.println( "content type: " + _contentTypeToString( m_contentType ) );
        }
    }
    
    

    //setters: only called from within the package
    void addParentPath( SimplePath parentPath )
    {
        m_parentPaths.add( parentPath );
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

    void addOutlinedGroup( XercesXSGroupImpl group )
    {
        m_outlineGroups.add( group );
    }

    void setSimpleElements( List ses )
    {
      m_ses = ses;
    }

    void addSimpleElement( XercesXSSimpleElementImpl tse )
    { 
        if( null == tse ) return;
        
        //make sure there is no duplication
        boolean exist = false;
        for( int i = 0; i < m_ses.size(); i++ )
        {
            XercesXSSimpleElementImpl se = (XercesXSSimpleElementImpl) m_ses.get(i);
            if( se.getName().equals( tse.getName() ) )
            {
                m_distributedElems.add( tse.getPath() );
                exist = true;
                break;
            }
        }
        
        if( !exist )
        {
            m_ses.add( tse );
            if( tse.isOutlined() ) m_outlineSes.add( tse );
            else m_inlineSes.add( tse );
        }
        
        return;
    }  
          
  
    void setInlineSimpleElements( List inlineSes )
    {
        m_inlineSes = inlineSes;
    }

    void setOutlineSimpleElements( List outlineSes )
    {
        m_outlineSes = outlineSes;
    }

    
    void setComplexElements( List ces )
    {
        m_ces = ces;
    }


    void addComplexElement( XercesXSComplexElementImpl tce )
    { 
        if( null == tce ) return;
        
        //make sure there is no duplication
        boolean exist = false;
        for( int i = 0; i < m_ces.size(); i++ )
        {
            XercesXSComplexElementImpl ce = (XercesXSComplexElementImpl) m_ces.get(i);
            if( ce.getName().equals( tce.getName() ) )
            {
                exist = true;
                break;
            }
        }
        
        if( !exist )
        {
            m_ces.add( tce );
        }
        
        return;
    }     

    
    //getters, all public
    public String getTableName()
    {
        String tableName = XercesUtil.getTableName( m_annotationAttrs );
        
        if( null == tableName )
        {
            tableName = m_path.toString();
        }
        
        return tableName;
    }
    

    public int getIdentityScheme()
    {
        return XercesUtil.getIdentityScheme( m_annotationAttrs );
    }

    
    public String getName()
    {
        return m_elem.getName();
    }


    public SimplePath getPath()
    {
        return m_path;
    }

    
    public List parentPaths()
    {
        return m_parentPaths;
    }
    
    

    public XSGroup getParentGroup()
    {
        return m_parentGroup;
    }


    public XSContentModel getContentModel()
    {
        return m_contentModel;
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
    
     


    public List getAnnotationAttributes()
    {
        return m_annotationAttrs;
    }    
    
    public List attributes()
    {
        return m_attrs;
    }

    
    public List inlineAttributes()
    {
        return m_inlineAttrs;
    }

    
    public List outlineAttributes()
    {
        return m_outlineAttrs;
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

    public List complexElements()
    {
        return m_ces;
    }


    public List outlinedGroups()
    {
        return m_outlineGroups;
    }    
    
    
    public boolean isAnyType()
    {
        return m_isAnyType;
    }


    public boolean isMixedContentModel()
    {
        return (m_contentType == XSComplexTypeDefinition.CONTENTTYPE_MIXED );
    }


    //is the path like A/B/C/A
    public boolean isInRecursivePath()
    {
        return m_isInRecursivePath;
    }
    

    //if this element does not contain any string value, it is just a container
    public boolean isContainerElement()
    {
        XSComplexTypeDefinition ct = (XSComplexTypeDefinition) m_elem.getTypeDefinition();
       
        //if content model is simple or mixed, there might be node value
        return ( attributes().size() <=0 && simpleElements().size() <= 0
                 && ct.getContentType() != XSComplexTypeDefinition.CONTENTTYPE_MIXED
                 && ct.getContentType() != XSComplexTypeDefinition.CONTENTTYPE_SIMPLE );    
    }

    
    public boolean allowNodeValue()
    {
        XSComplexTypeDefinition ct = (XSComplexTypeDefinition) m_elem.getTypeDefinition();
       
        //if content model is simple or mixed, there might be node value
        return ( ct.getContentType() == XSComplexTypeDefinition.CONTENTTYPE_MIXED
                 || ct.getContentType() == XSComplexTypeDefinition.CONTENTTYPE_SIMPLE );    
    }


    //used in union distribution.  When loading such elements, special care needs to be given
    public List distributedElements()
    {
        return m_distributedElems;
    }


    public boolean mappedToClob()
    {
        return XercesUtil.mappedToClob( m_annotationAttrs );
    }

   
    public boolean edgeMapping()
    {
        boolean edgemapping = XercesUtil.edgeMapping( m_annotationAttrs );

        if( !edgemapping )
        {
            if( isAnyType() )
            {
                edgemapping = true;
            }
        }

        return edgemapping;
    }
    
    
    public String toString()
    {
        String str = "";
        
        str += "==ComplexElement: " + getName() + "==\n";

        str += "-path: " + getPath() + "\n";

        if( isInRecursivePath() )
        {
            str += "%%in RECURSIVE path%%\n\n\n";
        }
        else
        {
            str += "-parent paths:";
            for( int i = 0; i < parentPaths().size(); i++ )
            {
                SimplePath sp = (SimplePath) parentPaths().get(i);
                str += "[" + sp + "]";
            }
            str += "\n";
            
            str += "-parent group: " + getParentGroup() + "\n";
            str += "-contentModel: " + getContentModel() + "\n";
            str += "-acc occs: [" + getAccMinOcc() + "," + getAccMaxOcc() + "]" + "\n";
            str += "-isAnyType:" + isAnyType() + "\n";
            str += "-content Type:" + _contentTypeToString( m_contentType )  + "\n";
            str += "-container element: " + isContainerElement() + "\n";
            str += "-allow NodeValue: " + allowNodeValue() + "\n";
            
            str += "-distributed elements: ";
            for( int i = 0; i < m_distributedElems.size(); i++ )
            {
                SimplePath sp = (SimplePath) m_distributedElems.get(i);
                str += "[" + sp + "]";
            }
            str += "\n";
            
            str += "-annotations: ";
            for( int i = 0; i < m_annotationAttrs.size(); i++ )
            {
                String[] annotationAttr = (String[])m_annotationAttrs.get(i);
                str += "[" + annotationAttr[0] + "," + annotationAttr[1]
                    + "," + annotationAttr[2] + "] ";                
            }
            str += "\n";
            
            
            str += "-Attributes: \n";
            for( int i = 0; i < m_attrs.size(); i++ )
            {
                XercesXSAttributeImpl attr = (XercesXSAttributeImpl) m_attrs.get(i);
                str += attr;
            }
            
            str += "\n";
            
            str += "-Inline Attributes: \n";
            for( int i = 0; i < m_inlineAttrs.size(); i++ )
            {
                XercesXSAttributeImpl attr = (XercesXSAttributeImpl) m_inlineAttrs.get(i);
                str += attr;
            }
            
            str += "\n";
            
            str += "-Simple Elements: \n";
            for( int i = 0; i < m_ses.size(); i++ )
            {
                XercesXSSimpleElementImpl se = (XercesXSSimpleElementImpl) m_ses.get(i);
                str += se;
            }
            
            str += "\n";
            
            str += "-Inline Simple Elements: \n";
            for( int i = 0; i < m_inlineSes.size(); i++ )
            {
                XercesXSSimpleElementImpl se = (XercesXSSimpleElementImpl) m_inlineSes.get(i);
                str += se;  
            }
            
            str += "\n";
            
            
            //recursive call:
            str += "-Complex Elements: \n";
            for( int i = 0; i < m_ces.size(); i++ )
            {
                XercesXSComplexElementImpl ce = (XercesXSComplexElementImpl) m_ces.get(i);
                str += ce;
            }
            
            str += "\n\n";
        }
        
        return str;
    }


    //helper: set path and parent path
    private void _setPath( SimplePath path )
    {
        m_path = path;

        if( m_path.size() > 1 )
        {
            SimplePath parentPath = (SimplePath) m_path.clone();
            int s = parentPath.size();
            parentPath.remove( s-1 );
            m_parentPaths.add( parentPath );
        }
    }


         
    private boolean _isInRecursivePath( SimplePath sp )
    {
        boolean result = false;
        int s = sp.size();
        
        String name = sp.component( s-1 );

        for( int i = 0; i < s -1 ; i++ )
        {
            if( name.equals( sp.component(i) ) )
            {
                result = true;
                break;
            }
        }

        return result;
    }
        
    
    
    private String _contentTypeToString( short contentType ) 
    {
        String ctstr = "";
        if( contentType == XSComplexTypeDefinition.CONTENTTYPE_MIXED )
            ctstr = "mixed";
        if( contentType == XSComplexTypeDefinition.CONTENTTYPE_SIMPLE )
            ctstr = "simple";
        if( contentType == XSComplexTypeDefinition.CONTENTTYPE_EMPTY )
            ctstr = "empty";
        if( contentType == XSComplexTypeDefinition.CONTENTTYPE_ELEMENT )
            ctstr = "element";

        return ctstr;
    }

    
    public String getContentType()
    {
        return _contentTypeToString( m_contentType );
    }
    
        
    /***************************************
     *     private data memebers           *
     ***************************************/
    private XSElementDeclaration m_elem;

    private SimplePath m_path; 
    private List m_parentPaths;
    private XercesXSGroupImpl m_parentGroup;

    private boolean m_isInRecursivePath;
    
    private int m_minOcc;
    private int m_maxOcc;
    private int m_accMinOcc;
    private int m_accMaxOcc;


    private XercesXSContentModelImpl m_contentModel;
    
    
    private List m_attrs;
    private List m_inlineAttrs;
    private List m_outlineAttrs;
    
    private List m_ses;
    private List m_inlineSes;
    private List m_outlineSes;
    
    private List m_ces;

    private List m_outlineGroups;

    private List m_annotationAttrs;
    
    //whether the complex element of any type
    private boolean m_isAnyType;

    //is mixed content model?
    private boolean m_mixedContentType;


    private short m_contentType;

    private List m_distributedElems;
    
}
 

