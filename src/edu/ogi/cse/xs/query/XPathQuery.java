/*
This file is part of ShreX.

ShreX is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation; either version 2 of the License, or (at your option) any later version.

ShreX is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.

You should have received a copy of the GNU General Public License along with Foobar; if not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA 
*/
package edu.ogi.cse.xs.query;


import edu.ogi.cse.xs.xsd.xerces.*;
import edu.ogi.cse.xs.xsd.*;
import edu.ogi.cse.xs.database.*;
import edu.ogi.cse.xs.loader.*;
import edu.ogi.cse.xs.mapping.*;
import edu.ogi.cse.xs.common.*;
import edu.ogi.cse.xs.relschema.*;

//xpath parser
import org.apache.commons.jxpath.ri.*;
import org.apache.commons.jxpath.ri.compiler.*;

import java.sql.*;
import java.util.*;

/**
  translate xpath to sql

  open problems:
  1. union all: select different # of columns, e.g. select "//"
  2. order predicate only works with dewey.  Dewey needs to be further improved
  */
public class XPathQuery
{
   
    public XPathQuery( String xsdpath )
    {
        String topElemName  = null;
        
        XSInstance xs = new XercesXSInstanceImpl( xsdpath, topElemName );
        
        //System.out.println( xs );
        
        //just init xtormapping
        RelationalSchemaGenerator rsg = new RelationalSchemaGenerator( xs );
        rsg.process( false );
        
        m_xtor = rsg.getMapping();

        //System.out.println( xtor );

        m_compiler = new TreeCompiler();
    }

    
    public XPathQuery( XToRMapping xtor )
    {
        m_xtor = xtor;

        //System.out.println( xtor );

        m_compiler = new TreeCompiler();
    }
    
        
    public List translate( String xpath )
    {
     
        List sqls = new ArrayList(5);

        LocationPath lpath
            = (LocationPath) Parser.parseExpression( xpath, m_compiler );

        
        if( !lpath.isAbsolute() )
        {
            System.out.println( "The path " + xpath + "is not an absolute path" );
            return null;  //throw exception TBA
        }
        
        SQLQuery query = null;
        
        if( _isWildcardPath( lpath ) )
        {
            System.out.println( "The path contains wild card" );
            List simplePaths = _resolveWildCard( lpath );
            
            if( null != simplePaths && simplePaths.size() > 0 )
            {
                for( int i = 0; i < simplePaths.size(); i++ )
                {
                    LocationPath simplePath = (LocationPath) simplePaths.get(i);
                    query = new SQLQuery();
                    _translateSimplePath( simplePath, query );
                    sqls.add( query );
                }
            }
        }
        else
        {
            query = new SQLQuery();
            _translateSimplePath( lpath, query );
            sqls.add( query );
        }
        
        return sqls;
    }
    
    
    public ResultSet execute( DBConnection conn, String sql ) throws Exception
    {
        return conn.executeQuery( sql );
    }
    
   
    private boolean _isWildcardPath( LocationPath lpath ) 
    {
        boolean result = false;

        if( null == lpath ) return false;
        
        Step steps[] = lpath.getSteps();

        if( null == steps || steps.length == 0 ) return false;
        
        int nsteps = steps.length;
        for (int i = 0; i < nsteps; i++)
        {
            
                
            Step step = steps[i];

            int axis = step.getAxis();
            
            if (axis == org.apache.commons.jxpath.ri.Compiler.AXIS_DESCENDANT_OR_SELF)
            {
                result = true;
                break;
            }
            
            if (axis == org.apache.commons.jxpath.ri.Compiler.AXIS_CHILD)
            {
                NodeTest nodetest = step.getNodeTest();
                NodeNameTest nnt = (NodeNameTest) nodetest;
                String nodeName = nnt.getNodeName().toString();

                if( nodeName.equals( "*" ) )
                {
                    result = true;
                    break;
                }
            }
        }

        return result;
    }
    
    

    private List  _resolveWildCard( LocationPath lpath )
    {
        List result = new ArrayList(10);
        String pattern = _getPattern( lpath );

        System.out.println( "pattern: " + pattern );
        
        List allPaths = m_xtor.allSimplePaths();

        for( int i = 0; i < allPaths.size(); i++ )
        {
            SimplePath sp = (SimplePath) allPaths.get(i);
            String spStr = sp.toXPath();
            
            System.out.println( "##process simplepath: " + spStr );

            boolean matched = false;
            try
            {
                matched = spStr.matches( pattern );
            }
            catch( java.util.regex.PatternSyntaxException pse )
            {
            }
            
            if( matched )
            {
                System.out.println( "##The path " + spStr + " matches pattern " + pattern );
                LocationPath simpleLocationPath = _buildLocationPath( sp, lpath );
                System.out.println( "##matched simple location path is: " + simpleLocationPath );
                result.add( simpleLocationPath );
            }
        }

        return result;
    }

    private String _getPattern( LocationPath lpath )
    {
        if( null == lpath ) return "";
        
        Step steps[] = lpath.getSteps();

        if( null == steps || steps.length == 0 ) return "";

        String str = "";
        
        int nsteps = steps.length;
        for (int i = 0; i < nsteps; i++)
        {       
            Step step = steps[i];

            int axis = step.getAxis();
            
            if (axis == org.apache.commons.jxpath.ri.Compiler.AXIS_DESCENDANT_OR_SELF)
            {
                str += ".*";
            }
            if (axis == org.apache.commons.jxpath.ri.Compiler.AXIS_CHILD)
            {
                NodeTest nodetest = step.getNodeTest();
                NodeNameTest nnt = (NodeNameTest) nodetest;
                String nodeName = nnt.getNodeName().toString();

                if( nodeName.equals( "*" ) )
                {
                    str += ".*";
                }
                else
                {
                    str += "/" + nodeName;
                }
            }
        }

        return str;
    }

    
    private LocationPath  _buildLocationPath( SimplePath sp, LocationPath lpath )
    {
        String pathstr = "";
        
        for( int i = 0; i < sp.size(); i++ )
        {
            String comp = sp.component(i);
            Step step = _getStep( lpath, comp );

            if( null == step )
            {
                pathstr += "/" + comp;
            }
            else
            {
                pathstr += "/"+ step.toString();
            }
        }

        LocationPath result
            = (LocationPath) Parser.parseExpression( pathstr, m_compiler );

        return result;
    }

    
    private Step _getStep( LocationPath lpath, String comp )
    {
        Step steps[] = lpath.getSteps();

        if( null == steps || steps.length == 0 ) return null;

        Step result = null;
        
        int nsteps = steps.length;
        for (int i = 0; i < nsteps; i++)
        {       
            Step step = steps[i];

            int axis = step.getAxis();
            
            if (axis == org.apache.commons.jxpath.ri.Compiler.AXIS_CHILD)
            {
                NodeTest nodetest = step.getNodeTest();
                NodeNameTest nnt = (NodeNameTest) nodetest;
                String nodeName = nnt.getNodeName().toString();

                if( nodeName.equals( comp ) ) 
                {
                    result = step;
                    break;
                }
            }
        }

        return result;
    }
    
        
    private void  _translateSimplePath( LocationPath simplePath,
                                        SQLQuery sql )
    {
        
        SimplePath sp = new SimplePath();

        
        Step steps[] = simplePath.getSteps();
        if (steps != null && steps.length > 0 )
        {
            int nsteps = steps.length;
            String[] tableNames = new String[nsteps];
            String[] tableVarNames = new String[nsteps];
            
            for (int i = 0; i < nsteps; i++)
            {
                //System.out.println( "#########\n" + sql );
                
                Step step = steps[i];
                NodeTest nodetest = step.getNodeTest();
                Expression[] predicates = step.getPredicates();
                
                //System.out.println( "---step---" + i + "\t" + step );
                
                int axis = step.getAxis();
                
                if (axis == org.apache.commons.jxpath.ri.Compiler.AXIS_CHILD)
                {
                    //add to from
                    NodeNameTest nnt = (NodeNameTest) nodetest;
                    String nodeName = nnt.getNodeName().toString();
                    sp.append( nodeName );
                    
                    if( m_xtor.mappedToTable( sp ) )
                    {
                        Table t = m_xtor.getTable( sp );
                        tableNames[i] = t.getName();
                        tableVarNames[i] = tableNames[i] + "1";
                        sql.addFrom( " " + tableNames[i] + " " + tableVarNames[i] + "," );
                        
                        if( i > 0 )
                        {
                            sql.addWhere( " " + tableVarNames[i] + ".pid = "
                                          + tableVarNames[i-1] + "." + IDCOLNAME + " AND" );
                        }
                        
                        //add to where
                        if( null != predicates && predicates.length > 0 )
                        {
                            for( int j = 0; j < predicates.length; j++ )
                            {
                                Expression predicate = predicates[j];
                                
                                if( predicate instanceof Constant )
                                {
                                //order predicate
                                    Constant c = (Constant) predicate;
                                    sql.addWhere( " " + tableVarNames[i] + "."
                                                  + IDCOLNAME + " LIKE '%."
                                                  + c   + "' AND" ); 
                                }
                                else if( predicate instanceof CoreOperationCompare )
                                {
                                //add to where and from
                                    CoreOperationCompare cmpOp = (CoreOperationCompare) predicate;
                                    
                                    Expression[] args = cmpOp.getArguments();
                                    
                                    if( args[0] instanceof LocationPath
                                        && args[1] instanceof Constant )
                                    {
                                        LocationPath lhsPath = (LocationPath) args[0];
                                        Constant rhsConst = (Constant) args[1];
                                        String op = cmpOp.getSymbol();
                                        
                                        _translateBranchingCondition( lhsPath, op, rhsConst.toString(), sp, tableNames[i], sql );
                                    }
                                    else
                                    {
                                        //throw exception
                                    }
                                }
                                else if( predicate instanceof LocationPath )
                                {
                                //translated to "exists"
                                //TBA
                                }
                            }
                        }
                    }
                    else
                    {
                        tableNames[i] = tableNames[i-1];
                        tableVarNames[i] = tableVarNames[i-1];
                    }
                    
                    //add to select
                    if( i == nsteps-1 )
                    {
                        if( m_xtor.mappedToTable( sp ) )
                        {
                            //TBA: reconstruct the whole element using sql
                            sql.addSelect( " " + tableVarNames[i] + "." + IDCOLNAME );
                            sql.setIsSelectComplexElement( true );
                            sql.setElementName( nodeName );
                        }
                        else
                        {
                            sql.addSelect( " " + tableVarNames[i-1] + "." + IDCOLNAME + "," );
                            int s = sp.size();
                            String parentElementName = sp.component( s-2 );
                            String fieldName = m_xtor.getFieldName( sp );
                            sql.setParentElementName( parentElementName );
                            sql.setTableName( tableVarNames[i-1] );
                            sql.setIsSelectSimpleElement( true );
                            sql.setElementName( nodeName );
                            sql.addSelect( " " + tableVarNames[i] + "." + fieldName );
                        }
                    }    
                }
                else if (axis == org.apache.commons.jxpath.ri.Compiler.AXIS_ATTRIBUTE)
                {
                    sql.addSelect( " " + tableVarNames[i-1] + "." + IDCOLNAME + "," );
                    
                    NodeNameTest nnt = (NodeNameTest) nodetest;
                    String attrName = nnt.getNodeName().toString(); 
                    String fieldName = m_xtor.getFieldName( sp, attrName  );
                    sql.addSelect( " " + tableVarNames[i] + "." + fieldName );
                    int s = sp.size();
                    String parentElementName = sp.component( s-2 );
                    sql.setParentElementName( parentElementName );
                    sql.setTableName( tableVarNames[i-1] );
                    sql.setIsSelectAttribute( true );
                    sql.setAttributeName( attrName );
                    sql.setElementName( sp.component( s-1 ) );
                }
                else
                {
                    System.out.println( "Should not see axis: "
                                        + Step.axisToString(axis) );
                    //throw exception
                }
            }
            
        }
    }


    private void _translateBranchingCondition( LocationPath lPath,
                                               String opstr,
                                               String constant,
                                               SimplePath contextPath,
                                               String contextTableName,
                                               SQLQuery sql )
    {        
        SimplePath sp = new SimplePath( contextPath.toString() );
        String contextTableVarName = contextTableName + "1";
        
        Step steps[] = lPath.getSteps();
        if (steps != null && steps.length > 0 )
        {
            int nsteps = steps.length;
            String[] tableNames = new String[nsteps];
            String[] tableVarNames = new String[nsteps];
            
            for (int i = 0; i < nsteps; i++)
            {
                Step step = steps[i];
                NodeTest nodetest = step.getNodeTest();
                Expression[] predicates = step.getPredicates();
                
                //System.out.println( "---step---" + i + "\t" + step );
                
                int axis = step.getAxis();
                
                if (axis == org.apache.commons.jxpath.ri.Compiler.AXIS_CHILD)
                {
                    //add to from
                    NodeNameTest nnt = (NodeNameTest) nodetest;
                    String nodeName = nnt.getNodeName().toString();
                    sp.append( nodeName );

                    if( m_xtor.mappedToTable( sp ) )
                    {
                        Table t = m_xtor.getTable( sp );
                        tableNames[i] = t.getName();
                        tableVarNames[i] = tableNames[i] + "1";
                        sql.addFrom( " " + tableNames[i] + " " + tableVarNames[i] + "," );
                        
                        if( i == 0 )
                        {
                            sql.addWhere( " " + tableVarNames[i] + ".pid = "
                                          + contextTableVarName + "." + IDCOLNAME + " AND" );
                        }
                        
                        if( i > 0 )
                        {
                            sql.addWhere( " " + tableVarNames[i] + ".pid = "
                                          + tableVarNames[i-1] + "." + IDCOLNAME + " AND" );
                        }
                        
                        //add to where
                        if( null != predicates && predicates.length > 0 )
                        {
                            for( int j = 0; j < predicates.length; j++ )
                            {
                                Expression predicate = predicates[j];
                                
                                if( predicate instanceof Constant )
                                {
                                    //order predicate
                                    Constant c = (Constant) predicate;
                                    sql.addWhere( " " + tableVarNames[i] + "." + IDCOLNAME
                                                  + " = " + c   + " AND" ); 
                                }
                                else if( predicate instanceof CoreOperationCompare )
                                {
                                    //add to where and from
                                    CoreOperationCompare cmpOp = (CoreOperationCompare) predicate;
                                    
                                    Expression[] args = cmpOp.getArguments();
                                    
                                    if( args[0] instanceof LocationPath
                                        && args[1] instanceof Constant )
                                    {
                                        LocationPath lhsPath = (LocationPath) args[0];
                                        Constant rhsConst = (Constant) args[1];
                                        String op = cmpOp.getSymbol();
                                        
                                        _translateBranchingCondition( lhsPath, op, rhsConst.toString(), sp, tableVarNames[i], sql );
                                    }
                                    else
                                    {
                                        //throw exception
                                    }
                                }
                                else if( predicate instanceof LocationPath )
                                {
                                //translated to "exists"
                                //TBA
                                }
                            }
                        }
                    }
                    else
                    {
                        if( i > 0 )
                        {
                            tableNames[i] = tableNames[i-1];
                            tableVarNames[i] =   tableVarNames[i-1];
                        }
                        else //i=0
                        {
                            tableNames[i] = contextTableName;
                            tableVarNames[i] =   contextTableVarName;  
                        }
                    }                    

                    if( i == nsteps-1 )
                    {
                        if( m_xtor.mappedToTable( sp ) )
                        {
                            //should not come here
                        }
                        else
                        {
                            String fieldName = m_xtor.getFieldName( sp );
                            sql.addWhere( " " + tableVarNames[i] + "." + fieldName
                                          + opstr + constant + " AND");
                        }
                    }    
                }
                else if (axis == org.apache.commons.jxpath.ri.Compiler.AXIS_ATTRIBUTE)
                {
                    NodeNameTest nnt = (NodeNameTest) nodetest;
                    String attrName = nnt.getNodeName().toString(); 
                    String fieldName = m_xtor.getFieldName( sp, attrName  );
                    sql.addWhere( " " + tableVarNames[i] + "." + fieldName + opstr
                                  + constant  + " AND" );
                }
                else
                {
                    System.out.println( "Should not see axis: "
                                        + Step.axisToString(axis) );
                    //throw exception
                }
            }   
        }
        
    }


    public String  dumpResultSet(ResultSet rs) throws SQLException
    {
        String str = "";
        
        // the order of the rows in a cursor
        // are implementation dependent unless you use the SQL ORDER statement
        ResultSetMetaData meta   = rs.getMetaData();
        int               colmax = meta.getColumnCount();
        int               i;
        Object            o = null;

        // the result set is a cursor into the data.  You can only
        // point to one row at a time
        // assume we are pointing to BEFORE the first row
        // rs.next() points to next row and returns true
        // or false if there is no next row, which breaks the loop
        for (; rs.next(); )
        {
            for (i = 0; i < colmax; ++i)
            {
                o = rs.getObject(i + 1); // Is SQL the first column is indexed with 1 not 0
                //System.out.print(o.toString() + " ");
                if( null == o )
                {
                    str += "";
                }
                else
                {
                    str += o.toString() + " ";
                }
            }
            
            //System.out.println(" ");
            str += " \n";
        }

        return str;
    }

    
    public String dumpResultSet(ResultSet rs, List SQLQueries ) throws SQLException
    {
        String str = "";

        SQLQuery sqlQuery = (SQLQuery) SQLQueries.get(0);

        boolean unionAll = (SQLQueries.size() > 1 )?true:false;
        
        String elemName = sqlQuery.getElementName();
        String attrName = "";
        String parentElemName = "";
        
      
        // the order of the rows in a cursor
        // are implementation dependent unless you use the SQL ORDER statement
        ResultSetMetaData meta   = rs.getMetaData();
        int               colmax = meta.getColumnCount();
        int               i;
        Object            o = null;

        // the result set is a cursor into the data.  You can only
        // point to one row at a time
        // assume we are pointing to BEFORE the first row
        // rs.next() points to next row and returns true
        // or false if there is no next row, which breaks the loop
        for (; rs.next(); )
        {       
            if( sqlQuery.isSelectAttribute() )
            {
                attrName = sqlQuery.getAttributeName();
                String tableName = meta.getTableName(2);
                //SQLQuery tq = _getQueryByTableName( SQLQueries, tableName );
                //parentElemName = tq.getParentElementName(); 
                parentElemName = sqlQuery.getParentElementName(); 
                
                
                String parentId = rs.getObject(1).toString();
                o = rs.getObject(2);
                String attrValue = "";
                if( null != o )
                {
                    attrValue = o.toString();
                }
                else
                {
                    continue;
                }
                
                if( unionAll )
                {
                    str += "<" + "..." + " "
                        + "id" + "=\"" + parentId + "\" "
                        + attrName + "=\"" + attrValue + "\" />"; 
                }
                else
                {
                    str += "<" + parentElemName + " "
                        + "id" + "=\"" + parentId + "\" "
                        + attrName + "=\"" + attrValue + "\" />";
                }
            }
            else if( sqlQuery.isSelectSimpleElement() )
            {
                String tableName = meta.getTableName(2);
                //SQLQuery tq = _getQueryByTableName( SQLQueries, tableName );
                //parentElemName = tq.getParentElementName();
                parentElemName = sqlQuery.getParentElementName();
        
                String parentId = rs.getObject(1).toString();
                o = rs.getObject(2);
                String elemValue = "";
                if( null != o )
                {
                    elemValue = o.toString();
                }
                else
                {
                    continue;
                }
                if( unionAll )
                {
                    str += "<" + "..." + " "
                        + "id" + "=\"" + parentId + "\">  "
                        + "<" + elemName + ">" + elemValue +  "</" + elemName + ">  "
                        + "</" + "..." + ">";
                }
                else
                {
                    str += "<" + parentElemName + " "
                        + "id" + "=\"" + parentId + "\">  "
                        + "<" + elemName + ">" + elemValue +  "</" + elemName + ">  "
                        + "</" + parentElemName + ">";
                }
            }
            else if( sqlQuery.isSelectComplexElement() )
            {
                String id = rs.getObject(1).toString();
                str += "<" + elemName + " " + "id" + "=\""
                    + id + "\" />";
            }
            
                
            str += " \n";
        }
    
        
        return str;
    }

    
    private SQLQuery _getQueryByTableName( List SQLQueries, String tableName )
    {
        SQLQuery result = null;
        
        for( int i = 0; i < SQLQueries.size(); i++ )
        {
            SQLQuery sqlQuery = (SQLQuery) SQLQueries.get(i);
            System.out.println( "---"+sqlQuery.getTableName() );
            System.out.println( "###"+tableName );
            
            if( sqlQuery.getTableName().equals( tableName ) )
            {
                result = sqlQuery;
                break;
            }
        }
        
        return result;
    }       
        

    static public void main(String[] args)
    {
        String xsdpath = "data/imdb/simdb.xsd";
        
        //Select the title of the first directed show by director 'never silent asymptot'
        String xpath1 = "/IMDB/DIRECTOR[NAME='never silent asymptot']/DIRECTED[1]/TITLE";
        
        //select all movie's title
        String xpath2="/IMDB/SHOW[MOVIE]/TITLE";
        
        //select all NAME elements
        String xpath3 = "//NAME";

        //select the name of the actors who have played in "blithely fluffy excuse"
        String xpath4 = "/IMDB/ACTOR[PLAYED/TITLE='blithely fluffy excuse']/NAME";
        
        //select all the directors, the selection is not an attribute or simple element
        String xpath5 = "/IMDB/DIRECTOR";
        
        //select all names
        String xpath6 = "/IMDB/*/NAME";

        //select the title, year and character of the show played by actor "quiet Tiresias detect blit"
        String xpath7 = "//ACTOR[NAME='quiet Tiresias detect blit']/PLAYED/YEAR";

        //select all show's title directed by some director
        String xpath8 = "/IMDB/DIRECTOR/DIRECTED/TITLE";
       
        //String[] xpaths = { xpath1, xpath2, xpath3, xpath4, xpath5, xpath6, xpath7 };
        String[] xpaths = { xpath8, xpath4, xpath1, xpath6, xpath3, xpath7 };

       

        XPathQuery tester = new XPathQuery( xsdpath );
        
        
        for( int i = 0; i < xpaths.length; i++ )
        {
            System.out.println( "==xpath: " + xpaths[i] );
            
            List sqls = tester.translate( xpaths[i] );

            System.out.println( "\n\n==sql: " );

            for( int j = 0; j < sqls.size(); j++ )
            {
                SQLQuery sql = (SQLQuery) sqls.get(j);
                System.out.println( sql.getQueryString() + "\n" );
                
                if( j < sqls.size() -1 )
                {
                    System.out.println( "UNION ALL" );
                }
            }

            echo("\n~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
            echo("\npress <Enter> to continue...\n");
            waitForEnterPressed();
        }
    }


        
    private static void echo(String msg)
    {
        System.out.println(msg);
    }


    private static void waitForEnterPressed()
    {
        try
        {
            System.in.read();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    
    private XToRMapping m_xtor;
    private org.apache.commons.jxpath.ri.Compiler m_compiler;

    static private final String IDCOLNAME = "id_";
    
}



