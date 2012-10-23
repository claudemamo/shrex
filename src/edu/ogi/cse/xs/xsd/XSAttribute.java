/*
This file is part of ShreX.

ShreX is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation; either version 2 of the License, or (at your option) any later version.

ShreX is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.

You should have received a copy of the GNU General Public License along with Foobar; if not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA 
*/
package edu.ogi.cse.xs.xsd;

import java.util.List;

import edu.ogi.cse.xs.common.SimplePath;

public interface XSAttribute extends java.io.Serializable
{
    public String getName();
   
    public SimplePath getParentElemPath();   

    public List getAnnotationAttributes();
    
    public boolean isListType();
    
    public boolean isUnionType();
    
    public boolean isOptional();

    
    //inlined by default
    public boolean isOutlined();
  
    //if mapped to a field, return its containing table name
    //if outlined, return either annotated table name or derived name
    public String getTableName();
        
         
    //return either annotated name or derived name.
    //If outlined, return null
    public String getFieldName(); 
    
    public int getSQLType();

    //TBA: now just work for VARCHAR and NUMBER
    public int getSQLTypeLen();

    //TBA
    //public String getDefault();

    public String toString();

    public String getDataType();
}
