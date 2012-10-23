/*
This file is part of ShreX.

ShreX is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation; either version 2 of the License, or (at your option) any later version.

ShreX is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.

You should have received a copy of the GNU General Public License along with Foobar; if not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA 
*/
package edu.ogi.cse.xs.xsd;

import java.util.List;

import edu.ogi.cse.xs.common.SimplePath;

/** represent an XML schema */

public interface XSInstance extends java.io.Serializable
{
    
    public String getFilePath();

    public String getFileName();
    
    
    //return target namespace + top element name
    public String getName();


    //return top element object
    public XSComplexElement getTopElement();

    
    public boolean isComplexElement( SimplePath sp );
    public boolean isSimpleElement( SimplePath sp );

    public XSComplexElement getComplexElement( SimplePath sp );
    public XSSimpleElement getSimpleElement( SimplePath sp );

    
    public int getIdentityScheme();
 
    public boolean mappedToClob( SimplePath sp );
      
    public boolean edgeMapping( SimplePath sp );

    public boolean mayAppearInMultiTable( SimplePath sp );
    
    public boolean isInRecursivePath( SimplePath sp );

    public boolean mayHaveMultipleParents( SimplePath sp );
    
    
    public String toString();
    
    public boolean partOfClobMapping(  SimplePath sp );
    public boolean partOfEdgeMapping(  SimplePath sp );

    public SimplePath getClobMappingAncestor(  SimplePath sp );
    public SimplePath getEdgeMappingAncestor(  SimplePath sp );

    public String validateAnnotation();
    
}

  
  



