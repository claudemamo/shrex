/*
This file is part of ShreX.

ShreX is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation; either version 2 of the License, or (at your option) any later version.

ShreX is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.

You should have received a copy of the GNU General Public License along with Foobar; if not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA 
*/
package edu.ogi.cse.xs.database;

import java.io.*;

import java.sql.*;

import edu.ogi.cse.xs.relschema.*;

public interface DBConnection
{
    public String getName();
    
    public void close();
    
    //generate table schema in db
    public void genRelationalSchema( RelationalSchema relschema, String sqlpath );

    //write row to a file
    public void writeRow( String[] values, PrintWriter pw );

    //insert row to db directly
    public void insertRow( String tableName, String[] values, int sqltype[] );

    //dump bulk loading cmd to a file
    public String dumpBulkLoadCmd( RelationalSchema relschema, String datadir );

    public void performBulkloading( String cmdPath );

    public String[] getTableNames();

    public ResultSet executeQuery( String query );
}

