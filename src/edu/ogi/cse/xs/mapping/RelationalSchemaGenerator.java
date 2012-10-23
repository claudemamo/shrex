/*
This file is part of ShreX.

ShreX is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation; either version 2 of the License, or (at your option) any later version.

ShreX is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.

You should have received a copy of the GNU General Public License along with Foobar; if not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA 
*/
package edu.ogi.cse.xs.mapping;

import java.util.*;
import java.io.*;

import edu.ogi.cse.xs.xsd.*;
import edu.ogi.cse.xs.relschema.*;
import edu.ogi.cse.xs.database.*;
import edu.ogi.cse.xs.conf.*;
import edu.ogi.cse.xs.common.*;


/*
  This is a class that takes an annotated XML schema as input
  and generate relational schema
  */
public class RelationalSchemaGenerator
{

    public RelationalSchemaGenerator( XSInstance xs )
    {
        m_conn = null;
        
        m_xs = xs;
        m_relschema = new RelationalSchema( m_xs );
        m_xtorMapping = new XToRMapping( xs );
        
        m_conf = Configuration.getInstance();

        m_tables = new HashMap(20);
    }
    
    
    public RelationalSchemaGenerator( XSInstance xs, DBConnection conn )
    {
        m_conn = conn;
        
        m_xs = xs;
        m_relschema = new RelationalSchema( m_xs );
        m_xtorMapping = new XToRMapping( xs );
        
        m_conf = Configuration.getInstance();

        m_tables = new HashMap(20);
    }
    

    public void process()
    {
        boolean genRelSchemaInDB = true;
        process( genRelSchemaInDB );
    }

    public XToRMapping getMapping()
    {
        return m_xtorMapping;
    }

    
    //The method to generate relational schema
    public XToRMapping process( boolean genRelSchemaInDB )
    {
        //process the annotated schema to generate relational schema in-memory info
        XSComplexElement topElem = m_xs.getTopElement();
        
        m_xtorMapping.setIdentityScheme( m_xs.getIdentityScheme() );
        
        _processComplexElement( topElem, null );
        
        if( genRelSchemaInDB )
        {
            //write in-memory relational schema to db, then we done
            if( null == m_conn )
            {
                System.out.println( "Unable to generate relational schema, no connection specified." );
            }
            else
            {
                String sqldir = Constants.LOG;
                String xsdfilename = m_xs.getFileName();
                String sqlpath = sqldir + SEP + "createtable_" + xsdfilename + ".sql";
                
                m_conn.genRelationalSchema( m_relschema, sqlpath );
            }
        }

        m_xtorMapping.setRelationalSchema( m_relschema );
        
        return m_xtorMapping;
    }
        

    
    public void setDBConnection( DBConnection conn )
    {
        m_conn = conn;
    }    

    
    private void _processComplexElement( XSComplexElement ce, Table parentTable )
    {
        //System.out.println( "process " + ce.getPath() );
        
        SimplePath sp = ce.getPath();
        
        //if in recursive path, no table generated
        if( ce.isInRecursivePath() ) return;
        
        //if mapped to clob
        if( m_xs.mappedToClob( sp ) ) 
        {
            //System.out.println( "mapped to clob" );
            
            Table clobTable = _genClobTable( ce, parentTable );
            m_xtorMapping.addTableInfo( sp, clobTable );

            return;
        }
        
        //if mapped to edge
        if( m_xs.edgeMapping( sp ) ) 
        {
            Table edgeTable = _genedgeTable( ce, parentTable );
            m_xtorMapping.addTableInfo( sp, edgeTable );
            
            return;
        }
        
        //if the ce is just a container element, no table generated: TBA
        //if( ce.isContainerElement() ) return;
        
        //generate a table for ce
        Table t = _genTable( ce, parentTable );
        m_xtorMapping.addTableInfo( sp, t );

        //generate tables for all outlined attributes
        List outlineAttributes = ce.outlineAttributes();
        if( null != outlineAttributes )
        {
            for( int i = 0; i < outlineAttributes.size(); i++ )
            {
                XSAttribute attr = (XSAttribute) outlineAttributes.get(i);
                    
                Table attrTable = _genTable( attr, sp, t );
                
                m_xtorMapping.addTableInfo( sp, attr, attrTable );
            }
        }
            

        //generate tables for all outlined simple elements
        List outlineSes = ce.outlineSimpleElements();
        if( null != outlineSes )
        {
            for( int i = 0; i < outlineSes.size(); i++ )
            {
                XSSimpleElement se = (XSSimpleElement) outlineSes.get(i);
                if( !se.getParentGroup().isOutlined() ) //if its parent group is not outlined
                {
                    Table seTable = _genTable( se, t );
                    m_xtorMapping.addTableInfo( se.getPath(), seTable );
                }
                
            }
        }

        
        //generate tables for all outlined groups
        List outlineGroups = ce.outlinedGroups();
        if( null != outlineGroups )
        {
            for( int i = 0; i < outlineGroups.size(); i++ )
            {
                XSGroup group = (XSGroup) outlineGroups.get(i);
                
                Table gTable = _genTable( group, sp, t );
                
                m_xtorMapping.addTableInfo( sp, group, gTable );

                //Generate tables for outlined simple elements
                List groupOutlineSes = group.outlineSimpleElements();
                
                for( int j = 0; j < groupOutlineSes.size(); j++ )
                {
                    XSSimpleElement se = (XSSimpleElement) groupOutlineSes.get(j);
                    Table seTable = _genTable( se, gTable );
                    m_xtorMapping.addTableInfo( se.getPath(), seTable );
                }
            }
        }
    
        
        
        List childCes = ce.complexElements();
        if( null != childCes )
        {
            for( int i = 0; i < childCes.size(); i++ )
            {
                XSComplexElement childce = (XSComplexElement) childCes.get(i);
                //make a recursive call
                _processComplexElement( childce, t );
            }
        }
    }


    
    private Table _genClobTable( XSComplexElement ce, Table parentTable )
    {
        String tableName = ce.getTableName();
        Table table = null;
     
        //clob field
        Field clobField = new Field( Constants.CLOBCOLUMN,
                                     Constants.SQL_CLOB,
                                     -1,
                                     false,
                                     false,
                                     false,
                                     null );
        if( null != parentTable )
        {
            //id field
            Field idField = _getIdField();        
            
            //parent id field: the root table does not have the field
            String parentName = parentTable.getName();
            Field pidField = _getPidField( parentName );
            

            Field[] fields = new Field[]{idField, pidField, clobField};
            
            table = new Table( tableName, fields, m_xs );
            //System.out.println( "create table: \n" + table );
        

            table.setParent( parentTable );
            parentTable.addChild( table );
        }
        else
        {
            Field idField = _getIdField();       
            Field[] fields = new Field[]{idField,clobField};
            table = new Table( tableName, fields, m_xs );
            //System.out.println( "create table: \n" + table );

            m_rootTable = table;
            m_relschema.setRootTable( table );
        }
        
        
        m_tables.put( tableName, table );

        return table;
    }

    
    private Field _getIdField()
    {
        
        Field idField = new Field( Constants.TABLE_ID,
                                   Constants.SQL_STRING,
                                   m_conf.getDefaultVarcharLength(),
                                   false,
                                   true,
                                   false,
                                   null );
        return idField;
    }

    

    private Field _getPidField( String parentName )
    {
        Field parentField = new Field( Constants.PARENT_TABLE_ID,
                                       Constants.SQL_STRING,
                                       m_conf.getDefaultVarcharLength(),
                                       false,
                                       false,
                                       true,
                                       parentName );

        return parentField;
    }
    


    private Table _genedgeTable( XSComplexElement ce, Table parentTable )
    {
        String tableName = ce.getTableName();
        Table table = null;
        
        //2. source field
        Field sourceField = new Field( Constants.SOURCE,
                                       Constants.SQL_STRING,
                                       m_conf.getDefaultVarcharLength(),
                                       false,
                                       true,
                                       false,
                                       null );

        //3. ordianl field
        Field ordinalField = new Field( Constants.ORDINAL,
                                        Constants.SQL_STRING,
                                        m_conf.getDefaultVarcharLength(),
                                        false,
                                        true,
                                        false,
                                        null );

        //4. attrname field
        Field attrnameField = new Field( Constants.ATTRNAME,
                                         Constants.SQL_STRING,
                                         m_conf.getDefaultVarcharLength(),
                                         false,
                                         false,
                                         false,
                                         null );

        //5.flag field: indicate whether it is real value or source pointer
        Field flagField = new Field( Constants.FLAG,
                                     Constants.SQL_STRING,
                                     m_conf.getDefaultVarcharLength(),
                                     false,
                                     false,
                                     false,
                                     null );

        
        //6. value field: string value of source pointer
        Field valueField = new Field( Constants.VALUE,
                                      Constants.SQL_STRING,
                                      m_conf.getDefaultVarcharLength(),
                                      false,
                                      false,
                                      false,
                                      null );
        
        if( null != parentTable )
        {
            String parentName = parentTable.getName();
            //1. parent id
            Field pidField = _getPidField( parentName );
            
            Field[] fields = new Field[]{ pidField,sourceField,ordinalField,
                                              attrnameField,flagField,valueField };
            
            table = new Table( tableName, fields, m_xs );
            //System.out.println( "create table: \n" + table );
            
            table.setParent( parentTable );
            parentTable.addChild( table );
        }
        else
        {
            Field[] fields = new Field[]{ sourceField,ordinalField,
                                              attrnameField,flagField,valueField };
            
            table = new Table( tableName, fields, m_xs );
            //System.out.println( "create table: \n" + table );

            m_rootTable = table;
            m_relschema.setRootTable( table );
        }

        m_tables.put( tableName, table );

        return table;
    }  
    
  

    //help function: generate a table from a complex element
    private Table _genTable( XSComplexElement ce, Table parentTable )
    {
        //maybe user defined
        String tableName = ce.getTableName();
        
        Vector fieldVec = new Vector(8);
        
        //one default field: id 
        Field idField = _getIdField();
        fieldVec.add( idField );
        
        
        //parent ids
        //deal with recursion
        //e.g. R:A:B:A, R:A have two parent paths R and R:A:B
        List parentPaths = ce.parentPaths();
        if( parentPaths.size() > 1 )
        {
            for( int i = 0; i < parentPaths.size(); i++ )
            {
                SimplePath sp = (SimplePath) parentPaths.get(i);
                if( sp.size() > ce.getPath().size() )  //such as R:A:B
                {
                    String anotherPidFieldName = Constants.PARENT_TABLE_ID + "_" + sp.lastComponent();
                    Field anotherPidField = new Field( anotherPidFieldName,
                                                       Constants.SQL_STRING,
                                                       m_conf.getDefaultVarcharLength(),  
                                                       true,
                                                       false,
                                                       false,
                                                       null );
                    fieldVec.add( anotherPidField );
                }
            }

            //the root table does not have the field
            if( null != parentTable )
            {
                String parentName = parentTable.getName();
                Field parentField = new Field( Constants.PARENT_TABLE_ID,
                                               Constants.SQL_STRING,
                                               m_conf.getDefaultVarcharLength(),
                                               true,  //this field is optional
                                               false,
                                               true,
                                               parentName );
                fieldVec.add( parentField );
            }
        }
        else
        {
            //the root table does not have the field
            if( null != parentTable )
            {
                String parentName = parentTable.getName();
                Field parentField = _getPidField( parentName );
                fieldVec.add( parentField );
            }
        }
        
        
        
        Field field = null;
        List attrs = ce.inlineAttributes();
        
        for( int i = 0; i < attrs.size(); i++ )
        {            
            XSAttribute attr = (XSAttribute) attrs.get(i);

            
            field = new Field( attr.getFieldName(),
                               attr.getSQLType(),
                               attr.getSQLTypeLen(),
                               attr.isOptional(),
                               false,
                               false,
                               null );
            fieldVec.add( field );
            
            m_xtorMapping.addFieldInfo( ce.getPath(), attr, field );
        }
         

        //all simple elements
        List ses = ce.inlineSimpleElements();
        for( int i = 0; i < ses.size(); i++ )
        {
            XSSimpleElement se = (XSSimpleElement)  ses.get(i);
            
            field = new Field( se.getFieldName(),
                               se.getSQLType(),
                               se.getSQLTypeLen(),
                               se.isOptional(),
                               false,
                               false,
                               null );
            if( se.isInChoiceGroup() )
            {
                //TBA:note we do not support the case that inside a choice group
                //one se is outlined, but the others are not
                
                field.addChoiceConstriant( se.otherFieldsInSameChoicGroup() );
            }
            
            fieldVec.add( field );
            
            m_xtorMapping.addFieldInfo( se.getPath(), field );
        }

        //nodevalue field
        if( ce.allowNodeValue() )
        {
            field = new Field( Constants.NODEVALUE,
                               Constants.SQL_STRING,
                               m_conf.getDefaultVarcharLength(),
                               true,
                               false,
                               false,
                               null );
            
            fieldVec.add( field );
        }
        
        
        Field[] fields = new Field[fieldVec.size()];
        for( int i = 0; i < fields.length; i++ )
        {
            fields[i] = (Field) fieldVec.elementAt(i);
        }
        
        Table table = new Table( tableName, fields, m_xs );
        //System.out.println( "create table: \n" + table );
        
        if( null == parentTable )
        {
            m_rootTable = table;
            m_relschema.setRootTable( table );
        }
        else
        {
            table.setParent( parentTable );
            parentTable.addChild( table );
        }
        
        m_tables.put( tableName, table );

        return table;
    }
    
  
        
    //generate table for attribute
    private Table _genTable( XSAttribute attr, SimplePath parentPath, Table parentTable )
    {
        //maybe user defined
        String tableName = attr.getTableName();
      
        Vector fieldVec = new Vector(5);
        
        //one default field: id 
        Field idField = new Field( Constants.TABLE_ID,
                                   Constants.SQL_STRING,
                                   m_conf.getDefaultVarcharLength(),
                                   false,
                                   true,
                                   false,
                                   null );
        fieldVec.add( idField );
        
        
        //parent id field: the root table does not have the field
        String parentName = parentTable.getName();
        Field parentField = _getPidField( parentName );
        
        fieldVec.add( parentField );
        
        
        Field field = new Field( attr.getFieldName(),
                                 attr.getSQLType(),
                                 attr.getSQLTypeLen(),
                                 attr.isOptional(),
                                 false,
                                 false,
                                 null );
        fieldVec.add( field );


        m_xtorMapping.addFieldInfo( parentPath, attr, field );

        
        Field[] fields = new Field[fieldVec.size()];
        for( int i = 0; i < fields.length; i++ )
        {
            fields[i] = (Field) fieldVec.elementAt(i);
        }
        
        Table table = new Table( tableName, fields, m_xs );
        //System.out.println( "create table: \n" + table );
        
        table.setParent( parentTable );
        parentTable.addChild( table );
        
        m_tables.put( tableName, table );

        return table;
    }
    


    //generate table for simple element
    private Table _genTable( XSSimpleElement se, Table parentTable )
    {
        //maybe user defined
        String tableName = se.getTableName();
        
        
        //one default field: id 
        Field idField = _getIdField();
                
        //parent id field: the root table does not have the field
        String parentName = parentTable.getName();
        Field pidField = _getPidField( parentName );
        
        Field valueField = new Field( se.getFieldName(),
                                 se.getSQLType(),
                                 se.getSQLTypeLen(),
                                 se.isOptional(),
                                 false,
                                 false,
                                 null );

        m_xtorMapping.addFieldInfo( se.getPath(), valueField );
        
        Field[] fields = new Field[]{idField, pidField, valueField};
         
        Table table = new Table( tableName, fields, m_xs );
        //System.out.println( "create table: \n" + table );
        
        table.setParent( parentTable );
        parentTable.addChild( table );
        
        m_tables.put( tableName, table );

        return table;
    }

    

    private Table _genTable( XSGroup group, SimplePath parentPath, Table parentTable )
    {
        //maybe user defined
        String tableName = group.getTableName();
        
        Vector fieldVec = new Vector(8);
        
        //id field
        Field idField = _getIdField();
        fieldVec.add( idField );

        //parent id field
        String parentName = parentTable.getName();
        Field parentField = _getPidField( parentName );
        fieldVec.add( parentField );
        
        
        Field field = null;     

        //we now assume no nested group and no annotation inside such a group.
        //A general support TBA

        //inline simple elements
        List ses = group.inlineSimpleElements();
        for( int i = 0; i < ses.size(); i++ )
        {
            XSSimpleElement se = (XSSimpleElement)  ses.get(i);
            
            field = new Field( se.getFieldName(),
                               se.getSQLType(),
                               se.getSQLTypeLen(),
                               se.isOptional(),
                               false,
                               false,
                               null );
            //we do not support the case that the outlined group has nested groups, so
            //no need to consider choice constraint
            
            fieldVec.add( field );
            
            m_xtorMapping.addFieldInfo( se.getPath(), field );
        }
        
        
        Field[] fields = new Field[fieldVec.size()];
        for( int i = 0; i < fields.length; i++ )
        {
            fields[i] = (Field) fieldVec.elementAt(i);
        }
        
        Table table = new Table( tableName, fields, m_xs );
        //System.out.println( "create table: \n" + table );
        
       
        table.setParent( parentTable );
        parentTable.addChild( table );
        
        
        m_tables.put( tableName, table );

        return table; 
    }
        
    
    
    private XSInstance m_xs;
    private DBConnection m_conn;
 
    private Table m_rootTable;

    //key: table name, value: Table object
    private Map m_tables;

    private Configuration m_conf;

    private RelationalSchema m_relschema;
    
    static private XToRMapping  m_xtorMapping;

    static private final String SEP = System.getProperty("file.separator");


    
    static public String getDataDirectory()
    {
        if( null == m_datadir )
        {
            //first get sqldir from constant definition
            String xsfilename = m_xtorMapping.getXSInstance().getFileName();
            int idx = xsfilename.indexOf( "." );
            String subdir = xsfilename.substring( 0, idx );
            m_datadir = Constants.LOG + SEP + subdir;        
            
            
            File dataDirectory = new File( m_datadir );
            
            if( dataDirectory.exists() )
            {
                /*
                if( ! dataDirectory.delete() )
                {
                    System.out.println( "unable to delete " + m_datadir );
                }
                */
                try
                {
                    Util.removeRecursively( m_datadir );
                
                }
                catch( Throwable t )
                {
                    System.out.println( "unable to delete " + m_datadir );
                }
            }
            if( !dataDirectory.mkdir() )
            {
                System.out.println( "unable to create " + m_datadir );
            }
        }

        return m_datadir;    
    }

    static private String m_datadir = null;
        

    
    //for testing
    static public void main(String[] args)
    {
        String xsdpath = null, topElemName = null;
        
        if( args.length == 2 )
        {
            xsdpath = args[0];
            topElemName = args[1];
        }
        else if( args.length == 1 )
        {
            xsdpath = args[0];
            topElemName = null;
        }
        else
        {
            System.out.println( "usage: java edu/ogi/cse/xs/mapping/RelationalSchemaGenerator xsdpath [topelemname]" );
        }
        
        edu.ogi.cse.xs.xsd.xerces.XercesXSInstanceImpl xs
            = new edu.ogi.cse.xs.xsd.xerces.XercesXSInstanceImpl( xsdpath, topElemName );

        //System.out.println( xs );
        
        edu.ogi.cse.xs.database.DataBaseImpl db = new edu.ogi.cse.xs.database.DataBaseImpl();
        
        RelationalSchemaGenerator rsg = new RelationalSchemaGenerator( xs, db.getDefaultConnection() );
        
        rsg.process();


        //System.out.println( rsg.getMapping() );
    }
}


