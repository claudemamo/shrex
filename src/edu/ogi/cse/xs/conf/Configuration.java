/*
This file is part of ShreX.

ShreX is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation; either version 2 of the License, or (at your option) any later version.

ShreX is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.

You should have received a copy of the GNU General Public License along with Foobar; if not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA 
*/
package edu.ogi.cse.xs.conf;

import java.util.*;

import org.w3c.dom.*;

import edu.ogi.cse.xs.xsd.xerces.XercesUtil;

import edu.ogi.cse.xs.common.*;



//api for MXMConfiguration.xml
public class Configuration
{
    static public Configuration getInstance()
    {
        String conffile = "./conf/mxmconf.xml";
        String confxsd = "./conf/mxmconf.xsd";

        if( null == s_conf )
        {
            s_conf = new Configuration( conffile, confxsd );
        }
        
        return s_conf;
    }

    Configuration( String conffile, String confxsd )
    {
        m_sectionProperties = new HashMap( 5 );
        m_typeLenSpec = new HashMap( 5 );

        //have to choose a parser to parse it, let's default to xerces
        //Document doc = XercesUtil.parse( conffile, confxsd );
        Document doc = XercesUtil.parse( conffile );
        Element top = doc.getDocumentElement();
        
        //sections
        List sections = XercesUtil.getElemsByTagName( top, "section" );
        
        for( int i = 0; i < sections.size(); i++ )
        {
            Element section = (Element) sections.get(i);
            
            String name = section.getAttribute("name");
            
            Object o = m_sectionProperties.get( name );
            Map proptable = null;
            if( null == o )
            {
                proptable = new HashMap(10);
                m_sectionProperties.put( name, proptable );
            }
            else
            {
                proptable = (Map) o;
            }
            
            List props = XercesUtil.getElemsByTagName( section, "property" );
            
            for( int j = 0; j < props.size(); j++ )
            {
                Element prop = (Element) props.get(j);
                String propname = prop.getAttribute( "name" );
                String propvalue = prop.getAttribute( "value" );
                
                proptable.put( propname, propvalue );
            }
        }

        //SQLTypeLenSpec
        List typespec = XercesUtil.getElemsByTagName( top, "SQLTypeLenSpec" );

        if( typespec.size() > 0 )
        {
            Element typespecElem = (Element) typespec.get(0);
            List items = XercesUtil.getElemsByTagName( typespecElem, "item" );
            
            for( int i = 0; i < items.size(); i++ )
            {
                Element item = (Element) items.get(i);

                List sources = XercesUtil.getElemsByTagName( item, "source" );
                Element source = (Element) sources.get(0);
                List lengths = XercesUtil.getElemsByTagName( item, "length" );
                Element length = (Element) lengths.get(0);

                String sourceStr = XercesUtil.getNodeValue( source );
                String lengthStr = XercesUtil.getNodeValue( length );
                lengthStr = lengthStr.trim();
                
                SimplePath sp = new SimplePath( sourceStr );

                m_typeLenSpec.put( sp, new Integer( lengthStr ) );
            }
        }
    }
    
    public String getDBName()
    {
        return getProperty( "DataBase", "dbname" );
        
    }

    
    public String getDriver()
    {
        return getProperty( "DataBase", "driver" );
    }
    
    
    public String getDBUrl()
    {
        return getProperty( "DataBase", "url" );
    }

    public String getUserDataBase()                                             
    {                                                                           
        String url = getDBUrl();                                                
        StringTokenizer st = new StringTokenizer( url, ":" );                   
        st.nextToken();                                                         
        st.nextToken();                                                         
        
        return st.nextToken();                                                  
    }
    
    public String getDBConnstr()
    {
        return getProperty( "DataBase", "connstr" );
    }
    
    
    public String getDBUser()
    {
        return getProperty( "DataBase", "user" );
    }

    
    public String getDBPasswd()
    {
        return getProperty( "DataBase", "passwd" );
    }


    public boolean dumpDBTables()
    {
        String dumpDBTables = getProperty( "DataBase", "dumpDBTables" );

        return dumpDBTables.equals( "true" );
    }

    
    public boolean dumpDBData()
    {
        String dumpDBData = getProperty( "DataBase", "dumpDBData" );

        return dumpDBData.equals( "true" );
    }

    
    public boolean bulkLoading()
    {
        String bulkloading = getProperty( "DataBase", "bulkloading" );

        return bulkloading.equals( "true" );
    }

    
    public boolean doInProcessBulkloading()
    {
        String doInProcessBulkloading = getProperty( "DataBase", "doInProcessBulkloading" );

        return doInProcessBulkloading.equals( "true" );
    }


    

    
    public String getTableDataSeparator()   
    {
        return getProperty( "DataBase", "tableDataSeparator" );
    }

    
    public int getDefaultVarcharLength()   
    {
        String lenstr = getProperty( "DataBase", "defaultVarcharLength" );
        return Integer.parseInt( lenstr );
    }

    
    public int getDefaultNumberLength()   
    {
        String lenstr = getProperty( "DataBase", "defaultNumberLength" );
        return Integer.parseInt( lenstr );
    }

    

    public String getDefaultNameSpace()
    {
        return getProperty( "XMLSchema", "namespace" );
    }


    public String getXMLParser()
    {
        return getProperty( "XMLParser", "parsername" );
    }
    
    
    public String getProperty( String sectionName, String propName )
    {
        Object o = m_sectionProperties.get( sectionName );
        
        if( null == o ) return null;
        
        Map propTable = (Map) o;
        
        o = propTable.get( propName );
        
        if( null == o ) return null;

        return (String) o;
    }

    
    public Map getTypeLenSpec()
    {
        return m_typeLenSpec;
    }


    public int getTypeLen( SimplePath sp )
    {
        Object o = m_typeLenSpec.get( sp );

        if( null == o ) return -1;

        return ((Integer) o).intValue();
    }


    //TBA
    public int verbose()
    {
        //return Constants.TRACE;
        return Constants.SILENT;
    }


    public String toString()
    { 
        String str = "";

        str += "======properties======\n";
        str += "DBName: " + getDBName() + "\n";    
        str += "DBUrl " + getDBUrl() + "\n";
        str += "getDBConnstr " + getDBConnstr() + "\n";
        str += "DBUser " + getDBUser() + "\n";
        str += "DBPasswd " + getDBPasswd() + "\n";
        str += "dumpDBTables " + dumpDBTables()+ "\n";
        str += "dumpDBData " + dumpDBData() + "\n";
        str += "bulkLoading " + bulkLoading() + "\n";
        str += "tableDataSeparator " + getTableDataSeparator() + "\n";
        str += "defaultVarcharLength " + getDefaultVarcharLength() + "\n";
        str += "defaultNumberLength " + getDefaultNumberLength() + "\n";
        str += "defaultNameSpace " + getDefaultNameSpace() + "\n";
        str += "xmlparser " + getXMLParser() + "\n";

        str += "======SQL type lenght specification======\n";
        Map table = getTypeLenSpec();
        Iterator itr = table.keySet().iterator();

        while( itr.hasNext() )
        {
            SimplePath sp = (SimplePath) itr.next();
            int len = getTypeLen(sp);
            str += sp + " type has length " + len + "\n";
        }
        
        return str;
    }
    

    static public void main(String[] args)
    {
        Configuration conf = Configuration.getInstance();
        System.out.println( conf );
    }
    
            
    
    //key: section name, value: a map of property name/value pairs
    private Map m_sectionProperties;

    //key: source, value: length
    private Map m_typeLenSpec;

    static private Configuration s_conf = null;
            
} 
