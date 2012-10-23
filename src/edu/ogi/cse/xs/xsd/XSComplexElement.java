/*
This file is part of ShreX.

ShreX is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation; either version 2 of the License, or (at your option) any later version.

ShreX is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.

You should have received a copy of the GNU General Public License along with Foobar; if not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA 
*/
package edu.ogi.cse.xs.xsd;

import java.util.List;

import edu.ogi.cse.xs.common.SimplePath;

/* represent an element of complext type.  A complex element
   is mapped either to a table, or nothing.  In the later case,
   the element just contains simple elements or attributes.
   It is the so called deep inline
   */

public interface XSComplexElement extends java.io.Serializable
{  
    
    //annotation may specify the table name, otherwise
    //derive it from the element's path.  If this complex
    //element is not mapped to a table, return null
    public String getTableName();

    //return local name: e.g A
    public String getName();
    

    //return the path that leads to this element
    public SimplePath getPath();
    

    //case 1 top element:  No parent
    //case 2 non-top element and no recursion: one parent
    //case 3 non-top element and recursion: more than one parent
    //note even though a group can be mapped to a table, but a
    //complex element's parent is still a complex element, not the group.
    //because group does not show a key-foreign key relationship
    //return: a list of parent simple paths
    public List parentPaths();
  

    public XSGroup getParentGroup();
    

    public XSContentModel getContentModel();
    

    public int getMinOcc(); 
    public int getMaxOcc();
    public int getAccMinOcc(); 
    public int getAccMaxOcc();


    public List getAnnotationAttributes();
    
    public List attributes();    
    public List inlineAttributes();    
    public List outlineAttributes();

    public List simpleElements();
    public List inlineSimpleElements();
    public List outlineSimpleElements();

    
    //child elements of complex type
    public List complexElements();
   
    
    //TBA:
    //public List outlineAttrGroups();

    public List outlinedGroups();
    

    //whether the complex element is unconstrained
    public boolean isAnyType();


    //whether the complex element has mixed content model
    public boolean isMixedContentModel();


    //is the path like A/B/C/A
    public boolean isInRecursivePath();
    

    //if this element does not contain any string value, it is just a container
    public boolean isContainerElement();
    
    public boolean allowNodeValue();

    //used in union distribution.  When loading such elements, special care needs to be given
    public List distributedElements();

    public boolean mappedToClob();
    public boolean edgeMapping();
    
    public String toString();

    public String getContentType();
    
    
}


