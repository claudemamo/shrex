/*
This file is part of ShreX.

ShreX is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation; either version 2 of the License, or (at your option) any later version.

ShreX is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.

You should have received a copy of the GNU General Public License along with Foobar; if not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA 
*/
package edu.ogi.cse.xs.xsd;


import java.util.List;

import edu.ogi.cse.xs.common.SimplePath;

/* represent a model group that is mapped to a table */
public interface XSGroup extends java.io.Serializable
{
    
    //return null if it is an anonymous group
    public String getName();

    
    public SimplePath getElemPath();
   
    
    public boolean isRefGroup();
    
    
    public String getRefGroupName();
    
    
    public boolean isChoiceGroup();
    
    
    public boolean isSequenceGroup();
    
    public boolean isAllGroup();
    
    public int getMinOcc();
    public int getMaxOcc();
    
    public List getAnnotationAttributes();

    public List groups();
    
    public List complexElements();
    
    public List simpleElements();
    public List inlineSimpleElements();
    public List outlineSimpleElements();

    
    public boolean isOutlined();
    
    
    //we only keep the groups that are mapped to a table
    //table name is required to be given by users
    public String getTableName();
        
    
    public String getContent();

    public String toString();
}


