/*
This file is part of ShreX.

ShreX is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation; either version 2 of the License, or (at your option) any later version.

ShreX is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.

You should have received a copy of the GNU General Public License along with Foobar; if not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA 
*/
package edu.ogi.cse.xs.loader;

import java.io.*;
import java.util.*;

import org.apache.xerces.parsers.SAXParser;
import org.xml.sax.*;
import org.xml.sax.helpers.DefaultHandler;

import edu.ogi.cse.xs.mapping.*;
import edu.ogi.cse.xs.relschema.*;
import edu.ogi.cse.xs.database.*;
import edu.ogi.cse.xs.conf.*;
import edu.ogi.cse.xs.common.*;
import edu.ogi.cse.xs.xsd.*;



/*
  Load one XML document to database
  TBA: deep inline
  TBA: union distribution over complex elements
  */
public class XMLLoader extends DefaultHandler
{
    
    public XMLLoader( XSInstance xs, String xmlfile, DBConnection conn )
    {
        _init( xs, xmlfile, null, conn );
    }
    
        
    public XMLLoader( XSInstance xs, String xmlfile, String documentId, DBConnection conn )
    {
        _init( xs, xmlfile, documentId, conn );
    }

    
    public XMLLoader( XToRMapping xtorMapping , String xmlfile, DBConnection conn )
    {
        _init( xtorMapping, xmlfile, null, conn );
    }

      
    public XMLLoader( XToRMapping xtorMapping , String xmlfile,
                      String documentId, DBConnection conn )
    {
        _init( xtorMapping, xmlfile, documentId, conn );
    }
    
    
    private void _init( XSInstance xs, String xmlfile, String documentId, DBConnection conn )
    {
        RelationalSchemaGenerator rsg = new RelationalSchemaGenerator( xs, conn );
        m_xtorMapping = rsg.process( false );
        //System.out.println( m_xtorMapping );
        
        _initMembers( xmlfile, documentId, conn );
        _setupDataDirectory();
        
    
        if( null == documentId )
        {
            String filename = null;
            StringTokenizer t = new StringTokenizer(m_xmlfile, SEP);
            while (t.hasMoreTokens())
            {
                filename = t.nextToken();
            }

            int idx = filename.indexOf( "." );
            m_documentId = filename.substring( 0, idx );
        }
        else
        {
            m_documentId = documentId;
        }
        
        m_idservice = IDService.getInstance( m_documentId );
    }

    
    private void _init( XToRMapping xtorMapping, String xmlfile, String documentId, DBConnection conn )
    {
        m_xtorMapping = xtorMapping;
        //System.out.println( m_xtorMapping );
        
        _initMembers( xmlfile, documentId, conn );
     
        _setupDataDirectory();
        
        if( null == documentId )
        {
            m_documentId = m_xtorMapping.getXSName();
        }
        else
        {
            m_documentId = documentId;
        }
        
        m_idservice = IDService.getInstance( m_documentId );
    }
    

    private void _initMembers( String xmlfile, String documentId, DBConnection conn )
    {
        m_xmlfile = xmlfile;
        m_conn = conn;
        
        m_conf = Configuration.getInstance();
        
        //It provides a global context
        m_path = new SimplePath();
        
        //for clob and edge processing
        m_mappedToClob = false;
        m_clobroot = null;
        m_clobcontent = "";
        m_clobTable = null;
        
        m_edgeMapping = false;
        m_edgeroot = null;
        m_source = new Stack();
        m_edgeTable = null;

        //for recursion
        m_pidstack = new Stack();
        
        //for union distribution
        m_unsavedData = new HashMap(2);


        //element global information
        m_complexElement = false;
        m_mayAppearInMultiTable = false;

        
        //hold tuples
        m_tabledataMap = new HashMap( 10 );

        m_contextIDNode = null;
        m_intervalIdservice = IDService.getInstance( "_X_X_X_X_X" ); //important to use this name
        m_intv = null;
        if( _interval() )
        {
            IntervalID intvID = new IntervalID( xmlfile );
            intvID.process();
            m_intvIDTable = intvID.getIDTable();
        }
        else
        {
            m_intvIDTable = null;
        }
    }

    
    public void process()
    {
        long start = Stat.tick();
        
        //if store entire doc as a clob, no need to call SAX parser
        if( m_xtorMapping.storeEntireDocAsClob() )
        {
            if( m_conf.verbose() >= Constants.TRACE )
            {
                System.out.println( "Store the entire document as a clob" );
            }
            
            _loadDocAsClob();
        }
        else
        {
            //  create a Xerces SAX parser
            SAXParser parser = new SAXParser();
            
            //set the content handler
            parser.setContentHandler(this);
            
            //  parse the document
            try
            {
                parser.parse(m_xmlfile);
            }
            catch (Throwable t)
            {
                t.printStackTrace();
            }     
            
            
            //dump bulkload command
            if( m_conf.bulkLoading() )
            {
                String cmdpath = m_conn.dumpBulkLoadCmd( m_xtorMapping.getRelationalSchema(), m_datadir );
                
                if( m_conf.doInProcessBulkloading() )
                {
                    m_conn.performBulkloading( cmdpath );
                }
            }
        }
        
        
        //A TableData object may still hold some data, flush them
        //A TableData object holds a file handler, release them now
        Iterator itr = m_tabledataMap.values().iterator();
        while( itr.hasNext() )
        {
            TableData td = (TableData) itr.next();
            //td.flushCurrentRow();
            td.closeFileHandler();
        }
        
    
        //release the connection
        m_conn.close();

        //xxx
        m_idservice.reset();
        m_intervalIdservice.reset();
        
        long stop = Stat.tick();
        Stat.processingTime += stop - start;
    }
    
    
 
    //Document start
    public void startDocument()
    {   
        if( m_conf.verbose() >= Constants.TRACE )
        {
            System.out.println ("Begin to load document " + m_xmlfile );
        }
    }
    
    
    // element start
    public void startElement (String uri, String local, String qName, Attributes atts)
    {
        if( m_conf.verbose() >= Constants.DEBUG )
        {
            System.out.println( "start element " + local );
        }
        
        m_path.append( local );
        
        if( m_conf.verbose() >= Constants.DEBUG )
	{
	    System.out.println( "process " + m_path );
        }
	
	
        //xxx
        if( _interval() )
        {
            String intvId = m_intervalIdservice.nextWithoutPrefix();
            m_intv = (String) m_intvIDTable.get( intvId );
        }
        
        boolean isInRecursivePath = false;
        
        if( !Constants.perf )
        {
            isInRecursivePath = m_xtorMapping.isInRecursivePath( m_path );
        }
        
            
        SimplePath tpath = m_path;
        if( isInRecursivePath )
        {
            tpath = m_xtorMapping.getFirstPath( m_path );
            if( m_conf.verbose() >= Constants.DEBUG )
            {
                System.out.println( "in recursive path. the corresponding path is " + tpath );
            }
        }    
            
       
        m_complexElement = m_xtorMapping.isComplexElement(tpath);
     
        //assume now, the clob is always mapped to a clob column
        //TBA: support clob table
        if( (m_mappedToClob = _maptoclob(m_path)) )
        {   
            if( m_conf.verbose() >= Constants.DEBUG )
            {
                System.out.println( "mapped to clob" );
            }
            
            if( null == m_clobroot )
            {
                m_clobroot = (SimplePath) m_path.clone();
                m_clobTable = m_xtorMapping.getTable( m_path );
            }
                
                
            //add element tag and attrs to clobcontent, may need to consider namespace(TBA)
            m_clobcontent += "<" + local;            
            
            //add attributes to clobcontent
            if( null != atts && atts.getLength() > 0 )
            {
                m_clobcontent += " ";
                
                for ( int i = 0; i < atts.getLength(); i++ )
                { 
                    String attrName  = atts.getLocalName(i);
                    String attrValue = atts.getValue(attrName);
                    m_clobcontent += attrName + "=" + "\""+ attrValue + "\"";
                }
            }
            
            m_clobcontent += ">";  
          
            _readyForCharacters = true;
	    
            return;
        }


        
        if( (m_edgeMapping = _edgeMapping(m_path) ) )
        {
            if( m_conf.verbose() >= Constants.DEBUG )
            {
                System.out.println( "edge mapping" );
            }
            
            if( null == m_edgeroot )
            {
                m_edgeroot = (SimplePath) m_path.clone();
                m_edgeTable = m_xtorMapping.getTable( m_path );
                m_source.push( _getId( m_path.toString() ) );
            }
            else
            {
                if( m_complexElement )
                {
                    String childsource = _getId( m_path.toString() );
                    String source = (String) m_source.peek();
                    _addEdgeTableRow( m_edgeTable, _getPid(), source, _getId( "E" ), local, "false", childsource );
                    m_source.push( childsource );
                } 
            }
            
            if( null != atts && atts.getLength() > 0 )
            {
                for ( int i = 0; i < atts.getLength(); i++ )
                { 
                    String attrName  = atts.getLocalName(i);
                    String attrValue = atts.getValue(attrName);
                    String source = (String) m_source.peek();
                    _addEdgeTableRow( m_edgeTable, _getPid(), source, _getId( "A" ), attrName, "true", attrValue );
                }
            }

            _readyForCharacters = true;
	    
            return;
        }

        
        //now deal with normal case
        
        if( m_complexElement )
        {
            //xxx
            Table cetable = m_xtorMapping.getTable( tpath );

            
            String id = null;
            if( _dewey() )
            {
                id = _getDeweyId( true );
            }
            else if( _interval() )
            {
                id = m_intv;
            }
            else
            {
                id = _getId();
            }
            
            String pid = _getPid();
            m_pidstack.push( id );
            
          
            
            //deal with recursive path
            if( isInRecursivePath )
            {
                _insertField( cetable, Constants.TABLE_ID, id );

                //set right pid field
                
                //R_A_B_A_B_A's pidname is pid_B
                String pidFieldName = null;
                if( m_xtorMapping.mayHaveMultipleParents( tpath )
                    && m_path.size() > tpath.size() )
                {
                    pidFieldName = Constants.PARENT_TABLE_ID + "_"
                        + m_path.component(m_path.size()-2);
                }
                else
                {
                    pidFieldName = Constants.PARENT_TABLE_ID;
                }
                _insertField( cetable, pidFieldName, pid );  
            }
            else
            {
                _setIdFields( cetable, id, pid );
            }
             
            
            //deal with attributes
            if( null != atts && atts.getLength() > 0 )
            {
                for (int i = 0; i < atts.getLength(); i++)
                { 
                    String attrName  = atts.getLocalName(i);
                    String attrValue = atts.getValue(attrName);

                    //xxx
                    String fieldname = m_xtorMapping.getFieldName( tpath, attrName );

                    //xxx
                    if( m_xtorMapping.mappedToTable( tpath, attrName ) )
                    {
                        String attrId = _getId();
                        //xxx
                        Table attrTable = m_xtorMapping.getTable( tpath, attrName );
                        _setIdFields( attrTable, attrId, id );
                        _insertField( attrTable, fieldname, attrValue );
                    }
                    else
                    {
                        _insertField( cetable, fieldname, attrValue );  
                    }
                }
            }
        }
        else  //simple element can not be in recursive path
        {
            //simple element outlined
            //xxx
            if( m_xtorMapping.mappedToTable( tpath ) )
            {
                //do nothing
            }
            else  //not mapped to table, but still could be outlined (inside a outlined group )
            {
                if( !Constants.perf )
                {
                    m_mayAppearInMultiTable = m_xtorMapping.mayAppearInMultiTable( tpath );
                }
                
                if ( m_mayAppearInMultiTable )
                {
                    //no need to do anything
                }
                else
                {   
                    //resolve unsaved data
                    if( m_unsavedData.size() > 0 )
                    {
                        //xxx
                        Table grouptable = m_xtorMapping.getTable( tpath );
                        _resolveUnsavedData( grouptable, m_unsavedData );
                        m_unsavedData.clear();
                    }
                }
            }
        }

	_readyForCharacters = true;
    }
    

    
    //CDATA found
    public void characters(char[] text, int start, int length)
    {
	if( !_readyForCharacters ) return;

        String content = new String(text, start, length);
        content = content.trim();

        if( content.length() <= 0 )
	{
	    _readyForCharacters = false;
	    return;
        }

        if( m_conf.verbose() >= Constants.DEBUG )
	{
	    System.out.println ("See Character Data " + content );
	}

        //mapped to clob
        if( m_mappedToClob )
        {
            m_clobcontent += content;            
          
	    _readyForCharacters = false;
            return;    
        }
        
        
        //edge mapping
        if( m_edgeMapping )
        {
            String attrName = null;
            if( m_complexElement ) //simple or mixed content type
            {
                attrName = Constants.NODEVALUE;
            }
            else
            {
                attrName = m_path.lastComponent();
            }
            String source = (String) m_source.peek();
            _addEdgeTableRow( m_edgeTable, _getPid(), source, _getId( "N" ), attrName, "true", content );
            
	    _readyForCharacters = false;
	    return;
        }

        //normal case
        //xxx ???
        Table t = m_xtorMapping.getTable( m_path );

        
        String fieldname = null;
        if( m_complexElement )
        {
            fieldname = Constants.NODEVALUE;
        }
        else
        {
            //xxx ???
            fieldname = m_xtorMapping.getFieldName( m_path );
        }
        
        //deal with union distribution
        
        if( m_mayAppearInMultiTable )
        {
            m_unsavedData.put( fieldname, content );
        }
        else
        {
            //this is the case that id/pid fields "might" not be filled
            //to be exact, it happens when a group is mapped to a table
            
            TableData td = _getTableData( t );
            if( !td.idSet() )
            {
                //xxx
                String id = null;
                if( _dewey() )
                {
                    id = _getDeweyId( false );
                }
                else if( _interval() )
                {
                    id = m_intv;
                }
                else
                {
                    id = _getId();
                }                
                _insertField( t, Constants.TABLE_ID, id );
            }
            if( t.hasPidField() && !td.pidSet() )
            {
                Table parentTable = t.getParent();

                //System.out.println( "parent table " + parentTable.getName() );
                
                TableData ptd = _getTableData( parentTable );

                //System.out.println( ptd );
                
                String pid = ptd.getCurrentRowId();
                _insertField( t, Constants.PARENT_TABLE_ID, pid );
            }
            
	    _insertField( t, fieldname, content );
            
        }
	_readyForCharacters = false;
    }

    
    //element end
    public void endElement (String uri, String local, String qName)
    {
        if( m_conf.verbose() >= Constants.DEBUG )
        {
	    System.out.println( "end element " + local );
	}

        boolean isInRecursivePath = false;
        if( !Constants.perf )
        {
            isInRecursivePath = m_xtorMapping.isInRecursivePath( m_path );
        }
        
        SimplePath tpath = m_path;
        if( isInRecursivePath )
        {
            tpath = m_xtorMapping.getFirstPath( m_path );
        }    

        //while the  characters() method can use m_complexElement etc directly
        //here need to recomputing the value, otherwise have to put the info in a stack
        //reason is the calling sequence is like start A, characters, start B, characters, end B, end A
        //xxx
        m_complexElement = m_xtorMapping.isComplexElement(tpath);

        
        //mapped to clob
        if( m_mappedToClob )
        {
            m_clobcontent += "</" + local + ">";
            
            if( m_path.equals( m_clobroot ) )  //this is the root of the clob
            {
                String fieldname = Constants.CLOBCOLUMN;
                _insertField( m_clobTable, fieldname, m_clobcontent );
                m_clobroot = null;
                m_clobcontent = "";
                m_mappedToClob = false;
            }

            m_path.removeLastComponent();
            return;
        }
        
        //edge mapping
        if( m_edgeMapping )
        {
            if( m_path.equals( m_edgeroot ) )   
            {
                m_source = new Stack();
                m_edgeroot = null;
                m_edgeMapping = false;
            }
            else
            {
                if(  m_complexElement )
                {   
                    m_source.pop();
                }
            }
            m_path.removeLastComponent();
            return;
        }
        

        if( m_complexElement )
        {
            m_pidstack.pop();

            //xxx
            if( _dewey() )
            {
                m_contextIDNode = m_contextIDNode.getParent();
            }
        }
        

        m_path.removeLastComponent();
    }
    

    // call this at document end
    public void endDocument()
    {
        if( m_conf.verbose() >= Constants.TRACE )
        {
            System.out.println ("Finish loading document " + m_xmlfile );
        }
    }    
    

    
    /***************************************************
     *             private methods                     *
     ***************************************************/
    private void _loadDocAsClob()
    {
        //TBA: good exception handling
        try
        {
            String clobcontent = null;
            ByteArrayOutputStream bos = new ByteArrayOutputStream(512);
            
            FileInputStream fis = new FileInputStream( m_xmlfile );
            int nBytesRead = 0;
            byte[] bytes = new byte[512];
            
            while( (nBytesRead = fis.read(bytes)) != -1 )
            {
                bos.write( bytes, 0, nBytesRead );
            }
            
            fis.close();

            clobcontent = bos.toString();

            SimplePath sp = m_xtorMapping.getTopElementPath();
            Table table =  m_xtorMapping.getTable( sp );
            
            _insertField( table, Constants.CLOBCOLUMN, clobcontent );  
        }
        catch( Throwable t )
        {
            t.printStackTrace();
        }
        
    }
    
    private boolean _maptoclob( SimplePath sp )
    {
        if( Constants.perf ) return false;
        
        boolean result = false;
        
        if( null != m_clobroot )
        {
            if( m_clobroot.isParentPath( sp ) )
            {
                result = true;
            }
        }
        else
        {
            if( m_xtorMapping.mappedToClob( sp ) )
            {
                result = true;
            }
        }

        return result;            
    }

    
    private boolean  _edgeMapping( SimplePath sp )
    {
        if( Constants.perf ) return false;
         
        boolean result = false;
        
        if( null != m_edgeroot )
        {
            if( m_edgeroot.isParentPath( sp ) )
            {
                result = true;
            }
        }
        else
        {
            if( m_xtorMapping.edgeMapping( sp ) )
            {
                result = true;
            }
        }

        return result;            
    }

    //xxx
    private String _getDeweyId( boolean isComplex )
    {
        String id = null;
        
        if( null == m_contextIDNode )
        {
            id = "1";
            m_contextIDNode = new IDNode( 1 );
        }
        else
        {   
            int preId = m_contextIDNode.getLastChildId();
            id = _getPid() + "." + (preId + 1);
            IDNode idnode = new IDNode( preId + 1 );
            m_contextIDNode.addChild( idnode );
            idnode.setParent( m_contextIDNode );

            if( isComplex )
            {
                m_contextIDNode = idnode;
            }
        }
        
        
        return id;
    }
    
    private String _getId( String prefix )
    {
        return m_idservice.next( prefix );
    }

    
    private String _getId()
    {
        return m_idservice.next();
    }

    
    private String _getPid()
    {
        if( m_pidstack.empty() ) return null;
        
        return (String) m_pidstack.peek();
    }
    
    
    private void _setIdFields( Table t, String id, String pid )
    {
        _insertField( t, Constants.TABLE_ID, id );
        if( null != pid )
        {
            _insertField( t, Constants.PARENT_TABLE_ID, pid );
        }
    }

    private TableData _getTableData( Table t )
    {
        TableData tabledata = null;
        
        Object o = m_tabledataMap.get( t.getName() );
        if( null == o )
        {
            tabledata = new TableData( t, m_conn, _getPW(t) );
            m_tabledataMap.put( t.getName(), tabledata );
        }
        else
        {
            tabledata = (TableData) o;
        }
          
        return tabledata;
    }
    
    private void _insertField( Table t, String fieldname, String value )
    {
        TableData tabledata = _getTableData( t );
        tabledata.insertField( fieldname, value );    
    }

    private void  _addEdgeTableRow( Table t, String pid, String source,
                             String ordinal, String attrName, String flag, String value )
    {
        TableData tabledata = _getTableData( t );
        if( null != pid )
        {
            tabledata.insertRow( new String[]{pid, source, ordinal, attrName, flag, value} );    
        }
        else //entire document is mapped to an edge table
        {
            tabledata.insertRow( new String[]{source, ordinal, attrName, flag, value} );    
        }
    }
    

    
    private void _resolveUnsavedData( Table t, Map dataToBeSaved )
    {
        Iterator itr = dataToBeSaved.keySet().iterator();
        while( itr.hasNext() )
        {
            String fieldName = (String) itr.next();
            String fieldValue = (String) dataToBeSaved.get(fieldName);
            
            _insertField( t, fieldName, fieldValue );
        }   
    }
    


    //methods related to data file path
    private void  _setupDataDirectory()
    {
        m_datadir = RelationalSchemaGenerator.getDataDirectory();

        /*       
        //first get sqldir from constant definition
        String xsfilename = m_xtorMapping.getXSInstance().getFileName();
        int idx = xsfilename.indexOf( "." );
        String subdir = xsfilename.substring( 0, idx );
        m_datadir = Constants.LOG + SEP + subdir;        
      
        
        File dataDirectory = new File( m_datadir );

        if( dataDirectory.exists() )
        {
            if( ! dataDirectory.delete() )
            {
                System.out.println( "unable to delete " + m_datadir );
            }
        }
        
        if( !dataDirectory.mkdir() )
        {
            System.out.println( "unable to create " + m_datadir );
        }
        */
    }
    
      
    private String _getDataFilePath( Table t )
    {
        return  m_datadir + SEP + t.getName() + Constants.DATAFILESUFFIX;
    }
    
        
    private PrintWriter _getPW( Table t )
    {
        String datafilepath = _getDataFilePath( t );
        
        File datafile = new File( datafilepath );
        if( datafile.exists() )
        {
            if( !datafile.delete() )
            {
                System.out.println( "unable to delete " + datafilepath ); 
            }
        }

        PrintWriter pw = null;
        
        try
        {
            pw = new PrintWriter(new FileOutputStream(datafilepath, true));
        }
        catch( Throwable e )
        {
            e.printStackTrace();
        }

        return pw;
    }
    

    
    //xxx
    private boolean _dewey()
    {
        if( m_xtorMapping.getIdentitiScheme() == Constants.DEWEY )
            return true;
        
        return false;    
    }

    //xxx
    private boolean _interval()
    {
        if( m_xtorMapping.getIdentitiScheme() == Constants.INTERVAL )
            return true;
        
        return false;    
    }

     
    /***************************************************
     *             class data members                  *
     ***************************************************/
    private String m_xmlfile;
    private String m_documentId;
    
    private XToRMapping  m_xtorMapping;
    private DBConnection m_conn;
    private Configuration m_conf;

    private SimplePath m_path;

    
    //for recursion
    private Stack m_pidstack;
    
    
    //for clob and edge processing
    private boolean m_mappedToClob;
    private SimplePath m_clobroot;
    private String m_clobcontent;
    private Table m_clobTable;
    
    
    private boolean m_edgeMapping;
    private SimplePath m_edgeroot;
    private Stack m_source;
    private Table m_edgeTable;
    
    
    //for union distribution
    private Map m_unsavedData; //key: fieldname; value: fieldValue


    //element global information
    private boolean m_complexElement;
    private boolean m_mayAppearInMultiTable;

    
    //key: table name, value: TableData
    private Map m_tabledataMap;

    private IDService m_idservice;

    private String m_datadir;
    static  private final String SEP = System.getProperty("file.separator");

    //xxx
    private IDNode m_contextIDNode;
    private IDService m_intervalIdservice;
    private String m_intv;
    private Map m_intvIDTable;

    private boolean _readyForCharacters;
}


