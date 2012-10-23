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
import edu.ogi.cse.xs.common.*;


public class XS
{

    static public void main(String[] args)
    {
        if( args.length != 2 && args.length != 3  )
        {
            System.out.println( "usage: java XS xsd [topElem] xml" );
            System.exit(1);
        }
        
        String xsdpath = null, topElemName = null, xmlpath = null;
        if( args.length == 2 )
        {
            xsdpath = args[0];
            xmlpath = args[1];
        }
        else
        {
            xsdpath = args[0]; 
            topElemName  = args[1];
            xmlpath = args[2];
        }
       
        
        XercesXSInstanceImpl xs = new XercesXSInstanceImpl( xsdpath, topElemName );

        //System.out.println( xs );



        //just init xtormapping
        RelationalSchemaGenerator rsg = new RelationalSchemaGenerator( xs );
        
        
        
        DataBaseImpl db = new DataBaseImpl();
      

        
        XMLLoader loader = new XMLLoader( xs, xmlpath, db.getDefaultConnection() );

        long start = Stat.tick();

        loader.process();

        long stop = Stat.tick();
        
        Stat.totalTime = stop-start;

        Stat.printStat();
        
        XToRMapping.dumpCallInfo();
    }
    
}




