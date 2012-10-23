/*
This file is part of ShreX.

ShreX is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation; either version 2 of the License, or (at your option) any later version.

ShreX is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.

You should have received a copy of the GNU General Public License along with Foobar; if not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA 
*/
package edu.ogi.cse.xs.common;

import java.io.*;

public class Util
{
    //Recursively remove all the files and directories under the given path
    static public void removeRecursively( String path ) throws IOException
    {
        File dir = new File( path );
        
        if( dir.exists() )
        {
            if( dir.isDirectory() )
            {
                File[] files = dir.listFiles();
                for( int i =0; (files != null) && (i<files.length); i++ )
                {
                    if(files[i].isDirectory())
                    {
                        removeRecursively( files[i].getPath() );
                    }
                    else
                    {                        
                        if( !files[i].delete() )
                        {
                            throw new IOException( "unable to remove " + files[i].getPath() );
                        }
                    }
                }
            }
            
            if( !dir.delete() )
            {
                throw new IOException( "unable to remove " + dir.getPath() );
            }
        }
        
    }
    
}


