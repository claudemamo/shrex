/*
This file is part of ShreX.

ShreX is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation; either version 2 of the License, or (at your option) any later version.

ShreX is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.

You should have received a copy of the GNU General Public License along with Foobar; if not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA 
*/
package edu.ogi.cse.xs.mapping;


import java.util.*;


import edu.ogi.cse.xs.xsd.*;
import edu.ogi.cse.xs.relschema.*;
import edu.ogi.cse.xs.common.*;


/*
  holding mapping information from XML to relation
  */
public class XToRMapping
{
        
    public XToRMapping( XSInstance xs )
    {
        m_xs = xs;
        m_tableMapping = new HashMap(10);
        m_fieldMapping = new HashMap(10);

        m_relschema = null;
        m_identityScheme = Constants.KEY_FK;

        if( Constants.perf )
        {
            s_nTimesCalled = new int[10];
            for( int i = 0; i < 10; i++ )
            {
                s_nTimesCalled[i] = 0;
            }

            s_queryTime = 0;
        }
    }


    public String getXSName()
    {
        return m_xs.getName();
    }

    
    public XSInstance getXSInstance()
    {
        return m_xs;
    }
    

    public int getIdentitiScheme()
    {
        return m_identityScheme;
    }
    
    public RelationalSchema getRelationalSchema()
    {
        return m_relschema;
    }
    
    public boolean storeEntireDocAsClob()
    {
        
        if( Constants.perf )
        {
            s_nTimesCalled[0]++;
        }
        long start = Stat.tick();
        
        
        SimplePath sp = new SimplePath();
        sp.append( m_xs.getTopElement().getName() );
        
        boolean result = mappedToClob( sp );

        long stop = Stat.tick();
        Stat.queryTime += stop - start;

        return result;
    }


    public SimplePath getTopElementPath()
    {
        SimplePath sp = new SimplePath();
        sp.append( m_xs.getTopElement().getName() );
        return sp;
    }
    
    
    public boolean isComplexElement( SimplePath sp )
    {
        if( Constants.perf )
        {
            s_nTimesCalled[1]++;
        }
        long start = Stat.tick();
        
        boolean result = m_xs.isComplexElement( sp );

        
        long stop = Stat.tick();
        Stat.queryTime += stop - start;

        return result;
    }


    public Table getTable( SimplePath sp )
    {
        if( Constants.perf )
        {
            s_nTimesCalled[2]++;
        }

        long start = Stat.tick();
        
        SimplePath tpath = sp;

        if( !Constants.perf )
        {
            if( m_xs.partOfClobMapping( sp ) )
            {
                tpath =  m_xs.getClobMappingAncestor(sp);
            }
            
            if( m_xs.partOfEdgeMapping( sp ) )
            {
                tpath =  m_xs.getEdgeMappingAncestor(sp);
            }
        }
        
        
        ExtendedPath ep = new ExtendedPath( tpath );

        Table t =  _getTable( ep );


        long stop = Stat.tick();
        Stat.queryTime += stop - start;
        
        return t;
    } 

    
    public Table getTable( SimplePath sp, String attrName )
    {

        if( Constants.perf )
        {
            s_nTimesCalled[3]++;
        }

        long start = Stat.tick();
       
        
        if( !Constants.perf )
        {
            if( m_xs.partOfClobMapping( sp ) )
            {
                SimplePath tpath =  m_xs.getClobMappingAncestor(sp);
                ExtendedPath ep = new ExtendedPath( tpath );
                return _getTable( ep );
            }
            
            if( m_xs.partOfEdgeMapping( sp ) )
            {
                SimplePath tpath =  m_xs.getEdgeMappingAncestor(sp);
                
                ExtendedPath ep = new ExtendedPath( tpath );
                return _getTable( ep );
            }
        }

        
        ExtendedPath ep = new ExtendedPath( sp, Constants.ATTRIBUTE, attrName );

        Table t =  _getTable( ep );

        long stop = Stat.tick();
        Stat.queryTime += stop - start;

        return t;
    }
    

    private Table _getTable( ExtendedPath ep )
    {    
        Object o = m_tableMapping.get( ep );
        
        if( null != o ) return (Table) o;
        
        //if sp is mapped to field, we found which table it belongs to
        o = m_fieldMapping.get( ep );
        
        if( null == o ) return null;
        
        Field field = (Field) o;
        
        return field.getTable();
    } 

        
    public String getFieldName( SimplePath sp )
    {
        if( Constants.perf )
        {
            s_nTimesCalled[4]++;
        }

        long start = Stat.tick();
       
        
        if( !Constants.perf )
        {
            if( m_xs.partOfClobMapping( sp ) )
            {
                return Constants.CLOBCOLUMN;
            }
            if( m_xs.partOfEdgeMapping( sp ) )
            {
                return Constants.VALUE; //not exactly right TBA
            }
        }
        
        
        ExtendedPath ep = new ExtendedPath( sp );
        
        Object o = m_fieldMapping.get( ep );

        long stop = Stat.tick();
        Stat.queryTime += stop - start;
        
        if( null == o ) return null;

        return ((Field) o).getName();
    }

    
    public String getFieldName( SimplePath sp, String attrName )
    {
        if( Constants.perf )
        {
            s_nTimesCalled[5]++;
        }

        long start = Stat.tick();
        
        if( !Constants.perf )
        {
            if( m_xs.partOfClobMapping( sp ) )
            {
                return Constants.CLOBCOLUMN;
            }
            if( m_xs.partOfEdgeMapping( sp ) )
            {
                return Constants.VALUE; //not exactly right TBA
            }
        }
            
        ExtendedPath ep = new ExtendedPath( sp, Constants.ATTRIBUTE, attrName );

        Object o = m_fieldMapping.get( ep );

        long stop = Stat.tick();
        Stat.queryTime += stop - start;
        
        
        if( null == o ) return null;
        
        return ((Field) o).getName();
    }
    
    
    //sp is a simple path
    public boolean mappedToTable( SimplePath sp )
    {
        if( Constants.perf )
        {
            s_nTimesCalled[6]++;
        }

        long start = Stat.tick();
       
        
        ExtendedPath ep = new ExtendedPath( sp );
        Object o = m_tableMapping.get( ep );
        
        long stop = Stat.tick();
        Stat.queryTime += stop - start;
        
        if( null != o ) return true;
        
        return false;
    }

    
    public boolean mappedToTable( SimplePath sp, String attrName )
    {
        if( Constants.perf )
        {
            s_nTimesCalled[7]++;
        }
        
        long start = Stat.tick();
        
        ExtendedPath ep = new ExtendedPath( sp, Constants.ATTRIBUTE, attrName );
        
        Object o = m_tableMapping.get( ep );
        
        if( null != o ) return true;
       
        return false;
    }


    //sp is a simple path
    public boolean mayAppearInMultiTable( SimplePath sp )
    {
        if( Constants.perf )
        {
            s_nTimesCalled[8]++;
        }
        
        
        return m_xs.mayAppearInMultiTable( sp );
    }


    public boolean mappedToClob( SimplePath sp )
    {
        return m_xs.mappedToClob( sp );
    }

    
    public boolean edgeMapping( SimplePath sp )
    {
      
        return m_xs.edgeMapping( sp ); 
    }


    public boolean isInRecursivePath( SimplePath sp )
    {
        if( Constants.perf )
        {
            s_nTimesCalled[9]++;
        }
        return m_xs.isInRecursivePath( sp );
    }
  

    public SimplePath getFirstPath( SimplePath aRecursivePath )
    {
        int s = aRecursivePath.size();
        String elemName = aRecursivePath.component( s-1 );
        SimplePath firstPath = new SimplePath();
        for( int i = 0; i < s -1 ; i++ )
        {
            firstPath.append( aRecursivePath.component(i) );
            if( elemName.equals( aRecursivePath.component(i) ) ) break;
        }

        return firstPath;
    }
    
    public boolean mayHaveMultipleParents( SimplePath sp )
    {
        return m_xs.mayHaveMultipleParents( sp );
    }
    
    //setters
    void setIdentityScheme( int idscheme )
    {
        m_identityScheme = idscheme;
    }
    
    
    void addTableInfo( SimplePath sp, Table t )
    {
        ExtendedPath ep = new ExtendedPath( sp );
        m_tableMapping.put( ep, t );
    }

    void addTableInfo( SimplePath sp, XSAttribute attr, Table t )
    {
        ExtendedPath ep = new ExtendedPath( sp, Constants.ATTRIBUTE, attr.getName() );
        
        m_tableMapping.put( ep, t );
    }
    
    void addTableInfo( SimplePath sp, XSGroup group, Table t )
    {
        //TBA
    }
    
    void addFieldInfo( SimplePath sp, Field f )
    {
        ExtendedPath ep = new ExtendedPath( sp );
        m_fieldMapping.put( ep, f );
    }

    
    void addFieldInfo( SimplePath sp, XSAttribute attr, Field f )
    {
        ExtendedPath ep = new ExtendedPath( sp, Constants.ATTRIBUTE, attr.getName() );
        m_fieldMapping.put( ep, f );
    }

    void setRelationalSchema( RelationalSchema relschema )
    {
        m_relschema = relschema;
    }
  

    
    public List allSimplePaths()
    {
        List result = new ArrayList(100);
        
        XSComplexElement topCE = m_xs.getTopElement();
      
        _addSimplePath( topCE, result );

        return result;
    }

    
    private void _addSimplePath( XSComplexElement tce, List result )
    {
        if( null == tce ) return;
        
        result.add( tce.getPath() );

        List ses = tce.simpleElements();
        for( int i = 0; i < ses.size(); i++ )
        {
            XSSimpleElement se = (XSSimpleElement) ses.get(i);
            result.add( se.getPath() );
        }
        
        List ces = tce.complexElements();
        for( int i = 0; i < ces.size(); i++ )
        {
            XSComplexElement ce = (XSComplexElement) ces.get(i);
            _addSimplePath( ce, result );
        }
    }

    
    public String toString()
    {
        String str = "";

        str += "=======Table Mapping:=========\n";
        Iterator itr = m_tableMapping.keySet().iterator();
        while( itr.hasNext() )
        {
            ExtendedPath ep = (ExtendedPath) itr.next();
            String tableName = ( (Table) m_tableMapping.get(ep) ).getName();
            
            str += "[" + ep + "] " + tableName + "\n";    
        }
        
        str += "=======Field Mapping:=========\n";
        itr = m_fieldMapping.keySet().iterator();
        while( itr.hasNext() )
        {
            ExtendedPath ep = (ExtendedPath) itr.next();
            String fieldName = ( (Field) m_fieldMapping.get(ep) ).getName();
            
            str += "[" + ep + "] " + fieldName + "\n";    
        }

        str += "\n\n";

        return str;
    }        

        
    //key:sp, value:table
    private Map m_tableMapping;
    //key:sp, value:field
    private Map m_fieldMapping;

    private XSInstance m_xs;
    private RelationalSchema m_relschema;

    private int m_identityScheme;

    static private int[] s_nTimesCalled;
    static public double s_queryTime = 0;



    static public void dumpCallInfo()
    {
        String[] funcnames = {
            "storeEntireDocAsClob",
            "isComplexElement",
            "getTable( SimplePath sp )",
            "getTable( SimplePath sp, String attrName )",
            "getFieldName( SimplePath sp )",
            "getFieldName( SimplePath sp, String attrName )",
            "mappedToTable( SimplePath sp )",
            "mappedToTable( SimplePath sp, String attrName )",
            "mayAppearInMultiTable( SimplePath sp )",
            "isInRecursivePath( SimplePath sp )" 
        };

        for( int i = 0; i < funcnames.length; i++ )
        {
            System.out.println( funcnames[i] + " is called \t" + s_nTimesCalled[i] );
        }
        
    }


    public String validateAnnotation()
    {
        return m_xs.validateAnnotation();
    }
    
}

        
        





