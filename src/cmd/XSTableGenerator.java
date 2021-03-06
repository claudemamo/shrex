/*
This file is part of ShreX.

ShreX is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation; either version 2 of the License, or (at your option) any later version.

ShreX is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.

You should have received a copy of the GNU General Public License along with Foobar; if not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA 
*/
package cmd;

import edu.ogi.cse.xs.xsd.xerces.*;
import edu.ogi.cse.xs.database.*;
import edu.ogi.cse.xs.loader.*;
import edu.ogi.cse.xs.mapping.*;


public class XSTableGenerator
{

    static public void main(String[] args)
    {
        if( args.length != 1 && args.length != 2  )
        {
            System.out.println( "usage: java XSTableGenerator xsdfilepath topElemName" );
            System.exit(1);
        }
        
        String xsdpath = args[0];

        String topElemName = null;
        if( args.length == 2 ) topElemName = args[1];
        
        XercesXSInstanceImpl xs = new XercesXSInstanceImpl( xsdpath, topElemName );
        
        DataBaseImpl db = new DataBaseImpl();
        
        RelationalSchemaGenerator rsg = new RelationalSchemaGenerator( xs, db.getDefaultConnection() );
        
        rsg.process();    
    }
    
}
