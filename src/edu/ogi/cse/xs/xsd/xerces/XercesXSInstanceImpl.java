/*
This file is part of ShreX.

ShreX is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation; either version 2 of the License, or (at your option) any later version.

ShreX is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.

You should have received a copy of the GNU General Public License along with Foobar; if not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA 
*/
package edu.ogi.cse.xs.xsd.xerces;

import java.net.*;
import java.util.*;

import org.apache.xerces.impl.xs.*;
import org.apache.xerces.impl.xs.psvi.*;
import org.apache.xerces.impl.dv.*;

import edu.ogi.cse.xs.xsd.*;
import edu.ogi.cse.xs.common.*;


/** represent an XML schema */
public class XercesXSInstanceImpl implements XSInstance
{
    //if topelem name is given, we use it.  Otherwise, pick the first global
    //element as the top element
    public XercesXSInstanceImpl( String xsdpath, String topElemName )
    {
        m_xsdpath = xsdpath;
        
        m_topElem = null;
        m_entityTable = new HashMap(100);
        m_recursivePaths = new ArrayList(1);
        m_edgeMappingComplexElements = new ArrayList(1);
        m_clobMappingComplexElements = new ArrayList(1);
        
        try
        {
            XSModel xs = XercesUtil.build( xsdpath );
            XSNamedMap elemmap = xs.getComponents( XSConstants.ELEMENT_DECLARATION );
            
            XSElementDecl topElem = null;
            if( null == topElemName )
            {
                topElem = (XSElementDecl) elemmap.item(0);
            }
            else
            {
                for( int i = 0; i < elemmap.getLength(); i++ )
                {
                    XSElementDecl telem = (XSElementDecl) elemmap.item(i);
                    if( telem.getName().equals( topElemName ) )
                    {
                        topElem = telem;
                        break;
                    }
                }
            }
            
            //first time traversal
            SimplePath sp = new SimplePath( topElem.getName() );
            m_topElem = new XercesXSComplexElementImpl( topElem, sp );
            
            
            //second traversal
            //build other data structures by traversing the entire schema tree
            _process( m_topElem );
            
        }
        catch( Throwable t )
        {
            t.printStackTrace();
        }
        
    }


    public String getFilePath()
    {
        return m_xsdpath;
    }
    
    public String getFileName()
    {
        String filename = m_xsdpath;
        
        final String SEP = System.getProperty("file.separator");
        StringTokenizer t = new StringTokenizer(m_xsdpath, SEP);
        while (t.hasMoreTokens())
        {
	    filename = t.nextToken();
        }

        return filename;
    }
        
    
    
    public String getName()
    {
        //TBA: return namespace + name
        return m_topElem.getName();
    }

    
    public boolean isComplexElement( SimplePath sp  )
    {   
        if( !_validPath( sp ) ) return false;

        Object o = m_entityTable.get( sp );

        if( o instanceof XercesXSComplexElementImpl ) return true;

        return false;
    }
    
    public boolean isSimpleElement( SimplePath sp )
    {
        if( !_validPath( sp ) ) return false;
        
        Object o = m_entityTable.get( sp );
        
        if( o instanceof XercesXSSimpleElementImpl ) return true;

        return false;
    }
    
   
    public XSComplexElement getComplexElement( SimplePath sp )
    {
        if( isComplexElement(sp) )
            return (XercesXSComplexElementImpl) m_entityTable.get( sp );
        
        return null;
    }
    
    public XSSimpleElement getSimpleElement( SimplePath sp )
    {
        if( isSimpleElement(sp) )
            return (XercesXSSimpleElementImpl) m_entityTable.get( sp );
        
        return null;
    }


    //from here, the entire schema can be traversed
    public XSComplexElement getTopElement()
    {
        return m_topElem;
    }


    public int getIdentityScheme()
    {
        return m_topElem.getIdentityScheme();
    }

    
    public boolean mappedToClob( SimplePath sp ) 
    {
        if( null == sp || isSimpleElement( sp ) ) return false;
        
        XSComplexElement ce = getComplexElement( sp );

        if( null == ce ) return false;
        
        return ce.mappedToClob();
    }

      
    public boolean edgeMapping( SimplePath sp ) 
    {
        if( null == sp || isSimpleElement( sp ) ) return false;

        XSComplexElement ce = getComplexElement( sp );

        if( null == ce ) return false;

        return ce.edgeMapping();
    }


    public SimplePath getClobMappingAncestor(  SimplePath sp )
    {   
        SimplePath result = null;
        for( int i = 0; i < m_clobMappingComplexElements.size(); i++ )
        {
            XSComplexElement ce = (XSComplexElement) m_clobMappingComplexElements.get(i);
            SimplePath cesp = ce.getPath();
            
            if( sp.prefixedBy( cesp ) )
            {
                result = cesp;
                break;
            }
        }
        
        return result;
    }
    
    public SimplePath getEdgeMappingAncestor(  SimplePath sp )
    {
        SimplePath result = null;
        for( int i = 0; i < m_edgeMappingComplexElements.size(); i++ )
        {
            XSComplexElement ce = (XSComplexElement) m_edgeMappingComplexElements.get(i);
            SimplePath cesp = ce.getPath();
            
            if( sp.prefixedBy( cesp ) )
            {
                result = cesp;
                break;
            }
        }

        return result;
    }
    
    public boolean partOfClobMapping(  SimplePath sp ) 
    {
        return null != getClobMappingAncestor( sp );
    }

    
    public boolean partOfEdgeMapping(  SimplePath sp ) 
    {
        return null != getEdgeMappingAncestor( sp );
    }

    //sp is a path of simple element
    public boolean mayAppearInMultiTable( SimplePath sp )
    {
        if( null == sp || isComplexElement( sp ) ) return false;
        
        SimplePath parentPath = (SimplePath) sp.clone();
        parentPath.removeLastComponent();

        XSComplexElement ce = getComplexElement( parentPath );
        
        List distElems = ce.distributedElements();
        boolean result = false;
        
        for( int i = 0; i < distElems.size(); i++ )
        {
            SimplePath distElem = (SimplePath) distElems.get(i);
            if( distElem.equals( sp ) )
            {
                result = true;
                break;
            }
        }

        return result;
    }    

    
    
    public boolean isInRecursivePath( SimplePath sp )
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
    
    public boolean mayHaveMultipleParents( SimplePath sp )
    {
        if( null == sp || isSimpleElement( sp ) ) return false;
        
        XSComplexElement ce = getComplexElement( sp );
        
        if( null == ce ) return false;

        List parentPaths = ce.parentPaths();

        return ( parentPaths.size() > 1 );
    }
    
        
    //is the element unconstrained?
    public boolean isAnyType( SimplePath sp )
    {
        boolean result = false;
        
        Object o = m_entityTable.get( sp );
        
        if( o instanceof XSComplexElement )
        {
            XercesXSComplexElementImpl ce = (XercesXSComplexElementImpl) o;

            if( ce.isAnyType() )
            {
                result = true;
            }
        }

        return result;
    } 
        
        

    public List getElementsOfAnyType()
    {
        List result = new ArrayList(2);
        
        Iterator itr = m_entityTable.keySet().iterator();
        
        while( itr.hasNext() )
        {
            SimplePath sp = (SimplePath) itr.next();
            
            Object o = m_entityTable.get( sp );
            
            if( o instanceof XercesXSComplexElementImpl )
            {
                XercesXSComplexElementImpl ce = (XercesXSComplexElementImpl) o;
                
                if( ce.isAnyType() )
                {
                    result.add( sp );
                }
            }
        }

        return result;
    }

    
    /*************************************************
     *               private methods                 *
     *************************************************/

    //during travasal,
    //1. setup simple path, associated group and accumulated occs for all elements
    //2. bypassing group, setup child elements for complex elements directly
    //3. put all elements in a hashtable
    
    private void _process( XercesXSComplexElementImpl elem )
    {
        
        if( null == elem ) return;


        //System.out.println( "process " + elem.getPath() );
        
        
        //1. set its path
        if( elem.isInRecursivePath() )
        {
            //System.out.println( "in recursive path" );
            
            //find the first appearance of the element,
            //e.g. from A:B:C:D:B to find A:B
            SimplePath sp = elem.getPath();
            int s = sp.size();
            String elemName = sp.component( s-1 );
            SimplePath firstPath = new SimplePath();
            for( int i = 0; i < s -1 ; i++ )
            {
                firstPath.append( sp.component(i) );
                if( elemName.equals( sp.component(i) ) ) break;
            }

            XercesXSComplexElementImpl firstElem
                = (XercesXSComplexElementImpl) getComplexElement( firstPath );

            SimplePath parentPath = (SimplePath) sp.clone();
            parentPath.remove( s-1 );
            firstElem.addParentPath( parentPath );

            m_recursivePaths.add( sp );
            
            return;
        }
        

        //2. traverse elem's content model, and drag all elements directly under it
        _traverseContentModel( elem );
        
        
        //3. put it into m_entityTable
        m_entityTable.put( elem.getPath(), elem );

        
        //4. get its simple elements and put into entity table
        List ses = elem.simpleElements();
        for( int i = 0; i < ses.size(); i++ )
        {
            XercesXSSimpleElementImpl se = (XercesXSSimpleElementImpl) ses.get(i);
            SimplePath sesp = se.getPath();            
            m_entityTable.put( sesp, se );
        }


        //5. get attributes and set parent path
        List attrs = elem.attributes();
        for( int i = 0; i < attrs.size(); i++ )
        {
            XercesXSAttributeImpl attr = (XercesXSAttributeImpl) attrs.get(i);
            attr.setParentElemPath( elem.getPath() );
        }
        
            
        //6. recursive down its complex elements
        List ces = elem.complexElements();
        for( int i = 0; i < ces.size(); i++ )
        {
            XercesXSComplexElementImpl ce = (XercesXSComplexElementImpl) ces.get(i);
            _process( ce );
        }
    }
   

    //1. compute accminoccs, accmaxoccs
    //2. bypassing groups and pull all elements directly
    //3. record inline/outline information: outlined groups and simple elements
    private void _traverseContentModel( XercesXSComplexElementImpl elem )
    {
        //edgeMapping and clobMapping
        if( elem.edgeMapping() )
        {
            m_edgeMappingComplexElements.add( elem );
        }
        if( elem.mappedToClob() )
        {
            m_clobMappingComplexElements.add( elem );
        }
        
        
        XercesXSContentModelImpl content = (XercesXSContentModelImpl) elem.getContentModel();
        
        //this is empty content or simple content
        if( null == content ) return;
        
        XercesXSGroupImpl topGroup = (XercesXSGroupImpl) content.getTopGroup();
        
        int accMinOcc = 1;
        int accMaxOcc = 1;

        if( topGroup.isChoiceGroup() )
        {
            accMinOcc = 0;
        }

        _traverseGroup( elem, topGroup, accMinOcc, accMaxOcc );
    }
  
    //a recursive call
    private void _traverseGroup( XercesXSComplexElementImpl elem, 
                                 XercesXSGroupImpl group,
                                 int accMinOcc, int accMaxOcc )
    {

        //System.out.println( "traverse group: " + group.getName() + "under " + elem.getName() );
        
        //is group mapped to a table
        boolean groupMappedToTable = false;
        if( group.isOutlined() )
        {
            groupMappedToTable = true;
            elem.addOutlinedGroup( group );
        }
 
        //simple elements
        List ses = group.simpleElements();
        for( int i = 0; i < ses.size(); i++ )
        {
            XercesXSSimpleElementImpl se = (XercesXSSimpleElementImpl) ses.get(i);
            
            //set parent group
            se.setParentGroup( group );

            //set inline outline list for the group
            int seMaxOcc = se.getMaxOcc();
            if( seMaxOcc > 1 || seMaxOcc == SchemaSymbols.OCCURRENCE_UNBOUNDED )
            {
                group.addOutlineSimpleElement( se );
            }
            else
            {
                group.addInlineSimpleElement( se );
            }
            
            
            //set accMinOcc and accMaxOccs
            se.setAccMinOcc( accMinOcc );
            se.setAccMaxOcc( accMaxOcc );
            
            elem.addSimpleElement( se );
        }
        

        //complex elements
        List ces = group.complexElements();
        for( int i = 0; i < ces.size(); i++ )
        {
            XercesXSComplexElementImpl ce = (XercesXSComplexElementImpl) ces.get(i);
            
            //set parent group
            ce.setParentGroup( group );
            
            //set accMinOcc and accMaxOccs
            ce.setAccMinOcc( accMinOcc );
            ce.setAccMaxOcc( accMaxOcc );
            
            elem.addComplexElement( ce );
        }
        
        //groups
        List groups = group.groups();
        for( int i = 0; i < groups.size(); i++ )
        {
          XercesXSGroupImpl cg = (XercesXSGroupImpl) groups.get(i);
          
          if( cg.isChoiceGroup() ) 
          {
              accMinOcc = 0;
              accMaxOcc *= cg.getMaxOcc();
          }
          else
          {
              accMinOcc *= cg.getMinOcc();
              accMaxOcc *= cg.getMaxOcc();
          }
        
          _traverseGroup( elem, cg, accMinOcc, accMaxOcc );
        }
    }
  
        
    
    private boolean _validPath( SimplePath sp )
    {
        if( null != m_entityTable.get(sp) ) return true;

        return false;
    }


    public String toString()
    {
        String str = "";
        
        str += "\n\n================================\n";
        str += "schema content\n";
        str += "================================\n\n";
        str += m_topElem;
        
        str += "\n\n\n";

        
        str += "\n\n================================\n";
        str += "entity table\n";
        str += "================================\n\n";
        Iterator itr = m_entityTable.keySet().iterator();
        
        while( itr.hasNext() )
        {
            SimplePath sp = (SimplePath) itr.next();
            str += sp + "\t\t";
            
            if( isComplexElement(sp) )
            {
                str += "Complex Element";
            }
            if( isSimpleElement(sp) )
            {
                str += "Simple Element";
            }
            
            str += "\n";
        }

   
        str += "\n\n================================\n";
        str += "Recursive Paths\n";
        str += "================================\n\n";
        for( int i = 0; i < m_recursivePaths.size(); i++ )
        {
            str += (SimplePath) m_recursivePaths.get(i);
            str += "\n";
        }        

        
        return str;
    }


    public String validateAnnotation()
    {
        List errors = new ArrayList(1);

        XSComplexElement topElem = getTopElement();

        _validateAnnotation( topElem, errors );

        if( errors.size() == 0 ) return null;

        String result = "";
        for( int i = 0; i < errors.size(); i++ )
        {
            result += errors.get(i) + "\n";
        }

        return result;
    }


    private void _validateAnnotation( XSComplexElement ce, List errors )
    {
        //System.out.println( "###Validate annotation: " + ce.getPath() );

        List annotationAttrs = ce.getAnnotationAttributes();
        
	if( null == annotationAttrs ) return;

	String msg = null;
        for( int i = 0; i < annotationAttrs.size(); i++ )
        {
            String[] annotationAttr = (String[])annotationAttrs.get(i);

            String url = annotationAttr[0];
            String name = annotationAttr[1];
            String value = annotationAttr[2];
            
            if( !url.equals( Constants.GLOADERNAMESPACE ) )
            {
                msg = "annotation to " + ce.getPath() + " is ignored.  Its url is "
                    + url + ", not the same as  " + Constants.GLOADERNAMESPACE;
                errors.add( msg );
            }
            
            if( url.equals( Constants.GLOADERNAMESPACE ) && !_isValidAnnotationName( name ) )
            {
                msg = "annotation " + name + " to " + ce.getPath() + " is not recognized.";
                errors.add( msg );  
            }
            
            if( url.equals( Constants.GLOADERNAMESPACE )
                && name.equals( Constants.OUTLINE )
                && !value.equals( "true" ) )
            {
                msg = ce.getPath() + " is an element of complex type.  It could not be inlined.";
                errors.add( msg );  
            }
            
            if( url.equals( Constants.GLOADERNAMESPACE )
                && name.equals( Constants.SQLTYPE ) )
            {
                msg = ce.getPath() + " is an element of complex type.  It could not have an annotation "
                    + Constants.SQLTYPE;
                errors.add( msg );  
            }
            
            if( url.equals( Constants.GLOADERNAMESPACE )
                && name.equals( Constants.FIELDNAME ) )
            {
                msg = ce.getPath() + " is an element of complex type.  It could not have an annotation "
                    + Constants.FIELDNAME;
                errors.add( msg );  
            }

            if( url.equals( Constants.GLOADERNAMESPACE )
                && name.equals( Constants.IDENTITYSCHEME )
                && ce.getPath().size() > 1 )
            {
                msg = ce.getPath() + " is not the top elemenet.  It could not have an annotation "
                    + Constants.IDENTITYSCHEME;
                errors.add( msg );  
            }
        }

        
        //attributes
        List attrs = ce.attributes();
        for( int i = 0; i < attrs.size(); i++ )
        {
            XSAttribute attr = (XSAttribute) attrs.get(i);
            _validateAnnotation( attr, errors );
        }

        //simple elements
        List ses = ce.simpleElements();
        for( int i = 0; i < ses.size(); i++ )
        {
            XSSimpleElement se = (XSSimpleElement) ses.get(i);
            _validateAnnotation( se, errors );
        }

         //complex elements
        List childCes = ce.complexElements();
        for( int i = 0; i < childCes.size(); i++ )
        {
            XSComplexElement childCe = (XSComplexElement) childCes.get(i);
            _validateAnnotation( childCe, errors );
        }    
    }
    
    private boolean _isValidAnnotationName( String name )
    {
        Set names = new HashSet();
        names.add( Constants.GLOADERNAMESPACE );
        names.add( Constants.TABLENAME );
        names.add( Constants.FIELDNAME );
        names.add( Constants.OUTLINE );
        names.add( Constants.SQLTYPE );
        names.add( Constants.MAPTOCLOB );
        names.add( Constants.EDGEMAPPING );
        names.add( Constants.IDENTITYSCHEME );
       
        if( names.contains( name ) ) return true;

        return false;
    }
    

    
    private void _validateAnnotation( XSSimpleElement se, List errors )
    { 
	//System.out.println( "###Validate annotation: " + se.getPath() );
	
        List annotationAttrs = se.getAnnotationAttributes();
        
        if( null == annotationAttrs || annotationAttrs.size() <= 0 ) return;
        
        String msg = null;
        for( int i = 0; i < annotationAttrs.size(); i++ )
        {
            String[] annotationAttr = (String[])annotationAttrs.get(i);

            String url = annotationAttr[0];
            String name = annotationAttr[1];
            String value = annotationAttr[2];
            
            if( !url.equals( Constants.GLOADERNAMESPACE ) )
            {
                msg = "annotation to " + se.getPath() + " is ignored.  Its url is "
                    + url + ", not the same as  " + Constants.GLOADERNAMESPACE;
                errors.add( msg );
            }
            
            if( url.equals( Constants.GLOADERNAMESPACE ) && !_isValidAnnotationName( name ) )
            {
                msg = "annotation " + name + " to " + se.getPath() + " is not recognized.";
                errors.add( msg );  
            }  
         

            if( url.equals( Constants.GLOADERNAMESPACE )
                && name.equals( Constants.IDENTITYSCHEME ) )
            {
                msg = se.getPath() + " is not the top elemenet.  It could not have an annotation "
                    + Constants.IDENTITYSCHEME;
                errors.add( msg );  
            }


	    if( url.equals( Constants.GLOADERNAMESPACE )
		&& (se.getMaxOcc() > 1 || se.getMaxOcc() == -1 )
                && name.equals( Constants.OUTLINE )
                && !value.equals( "true" ) )
	    {
		msg = se.getPath() + " may appear multiple times under its parent.  It could not be inlined.";
                errors.add( msg );  
            }
        }
    }
    
     
     
    private void _validateAnnotation( XSAttribute attr, List errors )
    {
	/*
	System.out.println( "###Validate annotation: " 
			    + attr.getParentElemPath() 
			    + "." + attr.getName() );
	*/

        List annotationAttrs = attr.getAnnotationAttributes();
        
        if( null == annotationAttrs || annotationAttrs.size() <= 0 ) return;
        
        String msg = null;
        for( int i = 0; i < annotationAttrs.size(); i++ )
        {
            String[] annotationAttr = (String[])annotationAttrs.get(i);

            String url = annotationAttr[0];
            String name = annotationAttr[1];
            String value = annotationAttr[2];
            String mypath = attr.getParentElemPath() + "." + attr.getName();
            
            if( !url.equals( Constants.GLOADERNAMESPACE ) )
            {
                msg = "annotation to " + mypath + " is ignored.  Its url is "
                    + url + ", not the same as  " + Constants.GLOADERNAMESPACE;
                errors.add( msg );
            }
            
            if( url.equals( Constants.GLOADERNAMESPACE ) && !_isValidAnnotationName( name ) )
            {
                msg = "annotation " + name + " to " + mypath + " is not recognized.";
                errors.add( msg );  
            }  
         

            if( url.equals( Constants.GLOADERNAMESPACE )
                && name.equals( Constants.IDENTITYSCHEME ) )
            {
                msg = mypath + " is not the top elemenet.  It could not have an annotation "
                    + Constants.IDENTITYSCHEME;
                errors.add( msg );  
            }
        }
    }
    
    

    private String m_xsdpath;
    
    private XercesXSComplexElementImpl m_topElem;

   
    //key: simple path, value: XSComplexElement or XSSimpleElement
    private Map m_entityTable;

    //list of recursive paths
    private List m_recursivePaths;
    

    private List m_edgeMappingComplexElements;
    private List m_clobMappingComplexElements;
    
    
    
    //for testing
    static public void main(String[] args)
    {
        String xsdpath = args[0];
        XercesXSInstanceImpl xs = new XercesXSInstanceImpl( xsdpath, null );

        System.out.println( xs );
    }

}
