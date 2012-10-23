/*
This file is part of ShreX.

ShreX is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation; either version 2 of the License, or (at your option) any later version.

ShreX is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.

You should have received a copy of the GNU General Public License along with Foobar; if not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA 
*/
package edu.ogi.cse.xs.common;



public class Stat
{
    static public double parsingTime = 0;
    static public double queryTime = 0;
    static public double ioTime = 0;
    
    static public double processingTime = parsingTime + queryTime + ioTime;
    
    static public double loadingTime = 0;
   
    static public double totalTime = 0;
    
    
    static public long tick()
    {
        return System.currentTimeMillis();
    }


    static public void printStat()
    {
        System.out.println( "parsing time:\t" +( processingTime - queryTime - ioTime) );
        System.out.println( "query time:\t" + queryTime );
        System.out.println( "io time:\t" + ioTime );
        System.out.println( "loading time:\t" + loadingTime );
        
        //totalTime = parsingTime + processingTime + loadingTime + ioTime;
        
        
        System.out.println( "totaltime:\t" + totalTime );
    }    
        
}


