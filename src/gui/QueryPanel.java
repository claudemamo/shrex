/*
This file is part of ShreX.

ShreX is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation; either version 2 of the License, or (at your option) any later version.

ShreX is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.

You should have received a copy of the GNU General Public License along with Foobar; if not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA 
*/
package gui;

import java.io.*;
import java.util.*;
import java.net.*;
import java.sql.*;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.tree.*;
import javax.swing.event.*;

import edu.ogi.cse.xs.xsd.*;
import edu.ogi.cse.xs.xsd.xerces.*;
import edu.ogi.cse.xs.loader.*;
import edu.ogi.cse.xs.mapping.*;
import edu.ogi.cse.xs.common.*;
import edu.ogi.cse.xs.relschema.*;
import edu.ogi.cse.xs.query.*;
import edu.ogi.cse.xs.database.*;



public class QueryPanel extends JSplitPane
{
    public QueryPanel()
    {
        super( JSplitPane.VERTICAL_SPLIT );
        _init();
    }


    public void addDefaultPath( SchemaTreeNode node )
    {
        if( node.isComplexElement() )
        {
            XSComplexElement ce = node.getComplexElement();
            SimplePath sp = ce.getPath();
            m_xpath = sp.toXPath();
        }
        if( node.isSimpleElement() )
        {
            XSSimpleElement se = node.getSimpleElement();
            SimplePath sp = se.getPath();
            m_xpath = sp.toXPath();
        }
        if( node.isAttribute() )
        {
            XSAttribute attr = node.getAttribute();
            String attrName = attr.getName();
            SimplePath parentElemPath = attr.getParentElemPath();
            m_xpath = parentElemPath.toXPath() + "/" + "@" + attrName;
        }
        
        _reDrawPath();
    }
    

    private void _reDrawPath()
    {
        //System.out.println( "redraw path......" );
        setDividerSize(4);
        setDividerLocation(100);
        
        m_comboXPath = _makeComboBox();
        
        m_panelTop = _makeTopPanel();
        setLeftComponent( m_panelTop );
    }

    
    private void _reDrawContent( int selection )
    {
        //System.out.println( "redraw content......" );  
        setDividerSize(4);
        setDividerLocation(100);

        /*
        if( selection == 0 ) //sql
        {
            m_sqlText = _makeJTextArea( m_sql );
            m_sqlPane = _makeScrollPane( m_sqlText );   
        }
        else
        {
            m_resultText = _makeJTextArea( m_result ); 
            m_resultPane = _makeScrollPane( m_resultText );
        }
        
        m_panelContents = _makeContentsPanel();
        m_tab.setSelectedIndex(selection);

        setRightComponent( m_panelContents );
        */

        m_sqlText = _makeJTextArea( m_sql );
        m_resultText = _makeJTextArea( m_result );
        m_sqlPane = _makeScrollPane( m_sqlText );
        m_resultPane = _makeScrollPane( m_resultText );
        m_panelContents = _makeContentsPanel();

        m_tab.setSelectedIndex(selection);
        
        setRightComponent( m_panelContents );
    }
    
        
    private void _init()
    {
        m_translator = new XPathQuery( _getMapping() );   
        
        m_xpathHistory = new ArrayList(10);
        
        for( int i = 0; i < XPATHS.length; i++ )
        {
            m_xpathHistory.add( XPATHS[i] );
        }
        
        m_xpath = "//NAME";
        m_sql = "sql";
        m_result = "result";
        m_sqlQueries = new java.util.ArrayList(1);
        
        //dwaw
        setDividerSize(4);
        setDividerLocation(100);
        
        m_comboXPath = _makeComboBox();
        
        m_buttonTranslate = new JButton( "translate" );
        m_buttonTranslate.addActionListener(new TranslateAction());
        
        m_buttonEvaluate = new JButton( "evaluate" );
        m_buttonEvaluate.addActionListener(new EvaluateAction());       
    
        
        m_sqlText = _makeJTextArea( m_sql );
        m_resultText = _makeJTextArea( m_result );
        m_sqlPane = _makeScrollPane( m_sqlText );
        m_resultPane = _makeScrollPane( m_resultText );
        
        m_panelTop = _makeTopPanel();
       
        m_panelContents = _makeContentsPanel();

       
        setLeftComponent( m_panelTop );
        setRightComponent( m_panelContents );
        
    }
    

    private JComboBox _makeComboBox()
    {
        JComboBox combo = new JComboBox();

        combo.addItem( m_xpath );
        
        for( int i = 0; i < m_xpathHistory.size(); i++ )
        {
            combo.addItem( m_xpathHistory.get(i) );
        }
        combo.setEditable(true);
        return combo;
    }


    private JTextArea _makeJTextArea( String text )
    {
        JTextArea area = new JTextArea();
        area.setEditable(false);
        area.setBackground(Color.white);
        
        area.append( text );

        return area;
    }

    
    private JScrollPane _makeScrollPane(Component component)
    {
        JScrollPane scroll = new JScrollPane(component);
        scroll.setMinimumSize(new Dimension(300, 120));
        scroll.setPreferredSize(new Dimension(300, 120));
        return scroll;
    }

    
    private JPanel _makeTopPanel()
    {
        final double[] weights = { 0.0, 0.8, 0.1 };
        
        _unifyPreferredWidths(new JButton[] { m_buttonEvaluate, m_buttonTranslate });
        
        
        JPanel panel = new JPanel();
        GridBagHelper helper = new GridBagHelper(panel, weights);
        
        helper.addLabel( "XPath" );
        helper.add( m_comboXPath );
        helper.add(_panelize(m_buttonTranslate, bg));
        helper.nextRow();
        helper.addButton( _panelize(m_buttonEvaluate, bg), 2 );

        panel.setBackground(bg);
        return panel; 
        
    }
    
    private JPanel _makeContentsPanel()
    {
        JPanel panelContents = new JPanel();
        panelContents.setLayout(new BorderLayout());
        
        m_tab = new JTabbedPane();
        m_tab.setTabPlacement(JTabbedPane.BOTTOM);
        m_tab.addTab( "SQL",  m_sqlPane );
        m_tab.addTab("Query Result", m_resultPane);
        m_tab.setSelectedIndex(0);
       
        
        panelContents.add(BorderLayout.CENTER, m_tab);
        
        return panelContents;
    }

    
    private void _unifyPreferredWidths(JComponent[] components)
    {/*
        
        int width = 0;
        for (int i=0; i<components.length; ++i)
        {
            if (width < components[i].getPreferredSize().getWidth())
            {
                width = (int)components[i].getPreferredSize().getWidth();
            }
        }
        for (int i=0; i<components.length; ++i)
        {
            Dimension preferred = components[i].getPreferredSize();
            preferred.width = width;
            components[i].setPreferredSize(preferred);
        }
        */ 
    }

    private JPanel _panelize(Component c, Color bg)
    {
        JPanel panel = new JPanel();
        panel.setLayout(new FlowLayout(FlowLayout.CENTER,0,0));
        panel.add(c);
        if (bg!=null)
            panel.setBackground(bg);
        return panel;
    }

    
    private void _addComboUnique(JComboBox combo, String item)
    {
        MutableComboBoxModel model = (MutableComboBoxModel) combo.getModel();
        
        if (!_getComboList(combo).contains(item))
            model.addElement(item);
        // silly combo box gets confused if we insert at the top -- be
        // warned if you reimplement to do most-recent-first ordering
    }

    private java.util.List _getComboList(JComboBox combo)
    {
        ComboBoxModel model = combo.getModel();
        java.util.List list = new ArrayList();
        for (int i = 0; i < model.getSize(); ++i)
        {
            list.add(model.getElementAt(i));
        }
        return Collections.unmodifiableList(list);
    }
    
    
    private XToRMapping _getMapping()
    {
        SchemaPanel xsdPanel = SchemaPanel.getInstance();
        return xsdPanel.getMapping();
    }

    /**********************************
     *        Translate Action        *
     **********************************/  
    class TranslateAction extends AbstractAction
    {
        TranslateAction()
        {
            super("translate");
        }

        public void actionPerformed(ActionEvent e)
        {
            //System.out.println( "trigger translate action." );

            //0. read xpath
            m_xpath = (String) m_comboXPath.getSelectedItem();
            System.out.println( m_xpath );
            
            
            //1. get sql query
            java.util.List sqls = m_translator.translate( m_xpath );
            m_sqlQueries = sqls;
            String sqlstr = "";
            
            for( int j = 0; j < sqls.size(); j++ )
            {
                SQLQuery sql = (SQLQuery) sqls.get(j);
                
                sqlstr += sql.getQueryString() + "\n";
                if( j < sqls.size() -1 )
                {
                    sqlstr += "UNION ALL" + "\n";
                }
            }

            m_sql = sqlstr;

            System.out.println( m_sql );
            
            if(  m_xpathHistory.contains( m_xpath ) == false )
            {   
                m_xpathHistory.add( 0, m_xpath );
            }

            _reDrawContent( 0 );
        }
    }
    
    
    /**********************************
     *        Evaluate Action      *
     **********************************/  
    class EvaluateAction extends AbstractAction
    {
        EvaluateAction()
        {
            super("evaluate");
        }

        public void actionPerformed(ActionEvent e)
        {
            System.out.println( "trigger evaluate action." );    
            
            XSGui xsgui = XSGui.getInstance();

            
            if( null == m_sqlQueries || m_sqlQueries.size() == 0 )
            {
                String message = "No SQL Query to evaluate";
                JOptionPane.showMessageDialog(xsgui, message);  
                return;
            }
            
            DataBase db = xsgui.getDB();
            java.util.List connections = db.connections();
                
            if( null == connections || connections.size() <= 0 )
            {
                String message = "No Database Conneciton is availabe. Please add a conneciton first.";          
                JOptionPane.showMessageDialog(xsgui, message);  
            }
            else
            {
                DBConnection selectedConn
                    = xsgui.promptForAConnection( connections, "Evaluate XML Query" );

                if( null != selectedConn )
                {
                    try
                    {
                        System.out.println( "execute: " + m_sql );
                        
                        ResultSet rs = selectedConn.executeQuery( m_sql );
                        
                        //System.out.println( m_translator.dumpResultSet( rs ) );
                        //rs.beforeFirst();
                        
                        m_result = m_translator.dumpResultSet( rs, m_sqlQueries );
                    }
                    catch( Throwable t )
                    {
                        t.printStackTrace();
                    }
                }    
            }
            
            
            m_tab.setSelectedComponent( m_resultPane );
            
            _reDrawContent( 1 );
        }
    }


   
    static private Color bg = new Color(240,240,240);

    static private String[] XPATHS =
    {
        //Select the title of the first directed show by director 'never silent asymptot'
        "/IMDB/DIRECTOR[NAME='never silent asymptot']/DIRECTED[1]/TITLE",

        //select all movie's title
        "/IMDB/SHOW[MOVIE]/TITLE",

        //select the name of the actors who have played in "blithely fluffy excuse"
        "/IMDB/ACTOR[PLAYED/TITLE='blithely fluffy excuse']/NAME",
        
        //select all the directors, the selection is not an attribute or simple element
        "/IMDB/DIRECTOR",
        
        //select all names
        "/IMDB/*/NAME",

        //select the title, year and character of the show
        //played by actor "quiet Tiresias detect blit"
        "//ACTOR[NAME='quiet Tiresias detect blit']/PLAYED/YEAR",
        
        //select all show's title directed by some director
        "/IMDB/DIRECTOR/DIRECTED/TITLE"
    };
    
       
    
    private JComboBox m_comboXPath;
    private JButton m_buttonEvaluate;
    private JButton m_buttonTranslate;

    private JTabbedPane m_tab;
    private JScrollPane m_sqlPane;    
    private JScrollPane m_resultPane;
    private JTextArea m_sqlText;
    private JTextArea m_resultText;
    
    private JPanel m_panelTop;
    private JPanel m_panelContents;

    private String m_xpath;
    private java.util.List m_xpathHistory;
    
    private String m_sql;
    private String m_result;

    private java.util.List m_sqlQueries;
    
    
    private XPathQuery m_translator;
    
}



