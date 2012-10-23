/*
This file is part of ShreX.

ShreX is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation; either version 2 of the License, or (at your option) any later version.

ShreX is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.

You should have received a copy of the GNU General Public License along with Foobar; if not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA 
*/
package gui;

import java.io.*;
import java.util.*;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;

import edu.ogi.cse.xs.xsd.*;
import edu.ogi.cse.xs.xsd.xerces.*;
import edu.ogi.cse.xs.database.*;
import edu.ogi.cse.xs.loader.*;
import edu.ogi.cse.xs.mapping.*;
import edu.ogi.cse.xs.common.*;
import edu.ogi.cse.xs.conf.*;

public class XSGui extends JFrame
{
    //driver
    static public void main(String args[])
    {    
        XSGui gui = new XSGui(); 
        gui.show();
    }


    
    public XSGui()
    {    
        super("XS: an XML Shredder");

        s_xsFrame = this;
        
        m_db = null;

        m_docLoadingMenuItem = null;
        
        m_viewPanel = new ViewPanel();
       
        JPanel contentPane = new JPanel(); 
        contentPane.setLayout(new BorderLayout());
        contentPane.add(m_viewPanel, BorderLayout.CENTER);
        
        setContentPane(contentPane);

        setBounds(300,300,850,650);
        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

        WindowListener l = new WindowAdapter()
            {
              public void windowClosing(WindowEvent e) {System.exit(0);}
            };
        addWindowListener( l );

        setJMenuBar( _createMenuBar() );
        m_viewPanel.init();
    }
        


    private JMenuBar _createMenuBar()
    {
        JMenuBar menuBar = new JMenuBar();
        JMenuItem mi;
        
        // File Menu
        JMenu file = (JMenu) menuBar.add(new JMenu("File"));
        file.setMnemonic('F');
        
        mi = (JMenuItem) file.add(new JMenuItem("Open Schema"));
        mi.setMnemonic('O');
        mi.addActionListener(new OpenAction());
        
        /*
        mi = (JMenuItem) file.add(new JMenuItem("Save Schema"));
        mi.setMnemonic('S');
        mi.setEnabled(false);
        
        mi = (JMenuItem) file.add(new JMenuItem("Save and reload Schema"));
        mi.setMnemonic('R');
        mi.setEnabled(false);
        */
        mi = (JMenuItem) file.add(new JMenuItem("Close Schema"));
        mi.setMnemonic('C');
        mi.addActionListener(new CloseAction());

        
        file.addSeparator();

        mi = (JMenuItem) file.add(new JMenuItem("Load Table Schema"));
        mi.setMnemonic('M');
        mi.addActionListener(new CreateDBSchemaAction());
        
        file.addSeparator();
        
        mi = (JMenuItem) file.add(new JMenuItem("Load document"));
        m_docLoadingMenuItem = mi;
        mi.setMnemonic('D');
        mi.addActionListener(new LoadDocumentAction());
        mi.setEnabled(false);
        
        
        file.addSeparator();
        
        mi = (JMenuItem) file.add(new JMenuItem("Exit"));
        mi.setMnemonic('x');
        mi.addActionListener(new ActionListener(){
           public void actionPerformed(ActionEvent e) {System.exit(0);}
        } );


        /*
        //edit menu
        JMenu edit = (JMenu) menuBar.add(new JMenu("edit"));
        mi = (JMenuItem) edit.add(new JMenuItem("undo"));
        mi.setEnabled(false);
        
        mi = (JMenuItem) edit.add(new JMenuItem("redo"));
        mi.setEnabled(false); 
        
        mi = (JMenuItem) edit.add(new JMenuItem("copy"));
        mi.setEnabled(false);

        mi = (JMenuItem) edit.add(new JMenuItem("cut"));
        mi.setEnabled(false);
        
        mi = (JMenuItem) edit.add(new JMenuItem("past"));
        mi.setEnabled(false);
        */

        //windows menu
        m_window = (JMenu) menuBar.add(new JMenu("window"));     

        
        // Database Menu
        JMenu db = (JMenu) menuBar.add(new JMenu("Database")); 
        
        //show all connection
        mi = (JMenuItem) db.add(new JMenuItem("connections"));
        mi.addActionListener(new ActionListener(){
           public void actionPerformed(ActionEvent e)
               {
                   m_viewPanel.showConnectionView();
               }
        } );
        
        //add a connection
        mi = (JMenuItem) db.add(new JMenuItem("add a connection"));
        mi.addActionListener(new ActionListener(){
           public void actionPerformed(ActionEvent e)
               {
                   m_viewPanel.ShowAddConnectionView();
               }
        } );
        
        //remove a connection
        mi = (JMenuItem) db.add(new JMenuItem("remove a connection"));
        mi.addActionListener(new ActionListener(){
           public void actionPerformed(ActionEvent e)
               {
                   m_viewPanel.ShowRemoveConnectionView();
               }
        } );


        /*
        //configuration menu
        JMenu conf = (JMenu) menuBar.add(new JMenu("Configuration")); 
        mi = (JMenuItem) conf.add(new JMenuItem("open"));
        mi.setEnabled(false);
        mi = (JMenuItem) conf.add(new JMenuItem("save"));
        mi.setEnabled(false);
        mi = (JMenuItem) conf.add(new JMenuItem("save and reload"));
        mi.setEnabled(false);
        */
        
        //hep menu
        JMenu help = (JMenu) menuBar.add(new JMenu("Help")); 
        mi = (JMenuItem) help.add(new JMenuItem("about"));

        mi.addActionListener(new HelpAction() );
        
        return menuBar;
    }

    
    /**********************************
     *        open an XML Schema      *
     **********************************/  
    class OpenAction extends AbstractAction
     {
         OpenAction()
         {
             super("open");
         }

         public void actionPerformed(ActionEvent e)
         {
             FileDialog fileDialog = new FileDialog(s_xsFrame);
             
             fileDialog.setMode(FileDialog.LOAD);
             fileDialog.show();
             
             String file = fileDialog.getFile();
             
             if (file == null)
             {
                 System.out.println( "no file was chosen" );
                 return;
             }
             
             String directory = fileDialog.getDirectory();
             File f = new File(directory, file);
             if (f.exists())
             {
                 System.out.println( "open file: " + directory + " " + file );
                 s_xsFrame.setTitle( file );
                 JMenuItem xsdmi = (JMenuItem) m_window.add(new JMenuItem(file));
                 xsdmi.addActionListener(new ActionListener(){
                    public void actionPerformed(ActionEvent e)
                        {
                            JMenuItem mi = (JMenuItem) e.getSource();
                            System.out.println( mi.getText() + " is selected.");
                            s_xsFrame.setTitle( mi.getText() );
                            m_activeMapping = (XToRMapping) m_openedSchema.get( mi.getText() );
                            m_viewPanel.showSchemaView( m_activeMapping );
                        }
                 } );
             }
             
             //topelement to be selected later
             String xsdpath = directory + file;

             m_activeMapping = _performMapping( xsdpath );
             
             //System.out.println( "finish mappinging " +  xsdpath );
             
             m_openedSchema.put( file, m_activeMapping );

             m_viewPanel.showSchemaView( m_activeMapping );   
         }
     }

    
    private XToRMapping _performMapping( String xsdpath )
    {
        String topElemName = null;
        if( xsdpath.indexOf( "DCSD" ) != -1 )
        {
            topElemName = "catalog";
        }

	//System.out.println( "topelemname " + topElemName );
        
        XSInstance xs = new XercesXSInstanceImpl( xsdpath, topElemName );
        
        //perform mapping now
        RelationalSchemaGenerator rsg = new RelationalSchemaGenerator( xs );
        
        return rsg.process(false);
    }
    
    
    /**********************************
     *        close the active Schema *
     **********************************/  
    class CloseAction extends AbstractAction
    {
        CloseAction()
        {
            super("close");
        }

        public void actionPerformed(ActionEvent e)
        {
            //1. remove menuitem from m_window menu
            int nmis = m_window.getItemCount();

            if( nmis < 1 ) return;

            String xsdfilename = m_activeMapping.getXSInstance().getFileName();

            int pos = -1;
            for( int i = 0; i < nmis; i++ )
            {
                JMenuItem item = m_window.getItem( i );
                if( item.getText().equals( xsdfilename ) )
                {
                    pos = i;
                    break;
                }
            }
            
            m_window.remove(pos);

            //2. remove schema from opened table
            m_openedSchema.remove( xsdfilename );
            
            //3. pick a schema to display
            nmis = m_window.getItemCount();

            if( nmis > 0 )
            {
                JMenuItem item = m_window.getItem( 0 );
                String text = item.getText();
                
                s_xsFrame.setTitle( text );

                m_activeMapping = (XToRMapping) m_openedSchema.get( text );

                m_viewPanel.showSchemaView( m_activeMapping );
            }
            else //no more opened schema
            {
                s_xsFrame.setTitle( "XS: an XML Shredder" );
                m_viewPanel.init();
            }
         }
    }



    /********************************************
     *        create table schema from mapping  *
     ********************************************/  
    class  CreateDBSchemaAction extends AbstractAction
    {
        CreateDBSchemaAction()
        {
            super("TableSchema");
        }
        
        public void actionPerformed(ActionEvent e)
        {
            if( null == m_activeMapping )
            {
                String message = "There is not an opened schema";          
                JOptionPane.showMessageDialog(s_xsFrame, message);    
            }
            else
            {
                DataBase db = getDB();
                java.util.List connections = db.connections();
                
                if( null == connections || connections.size() <= 0 )
                {
                    String message = "No Database Conneciton is availabe. Please add a conneciton first.";          
                    JOptionPane.showMessageDialog(s_xsFrame, message);  
                }
                else
                {
                    DBConnection selectedConn = promptForAConnection( connections, "Load Table Schema" );

                    if( null != selectedConn )
                    {
                        String sqldir = Constants.LOG;
                        String xsdfilename = m_activeMapping.getXSInstance().getFileName();
                        int idx = xsdfilename.indexOf( "." );
                        String modifiedName = xsdfilename.substring( 0, idx );
                        String sqlpath = sqldir + SEP + "createtable_" + modifiedName + ".sql";
                        
                        selectedConn.genRelationalSchema( m_activeMapping.getRelationalSchema(),sqlpath );
                        
                        m_docLoadingMenuItem.setEnabled( true );
                        
                        
                        String message = "Tables have been created in DB represented by connection "
                            + selectedConn.getName();
                        JOptionPane.showMessageDialog(s_xsFrame, message);    
                    }    
                }
                
            }    
        }
    }
    
    
    public DBConnection promptForAConnection( java.util.List connections, String title )
    {
    
        Object[] possibleConnections = new Object[connections.size()];
        for( int i = 0; i < connections.size(); i++ )
        {
            DBConnection conn = (DBConnection)connections.get(i);
            possibleConnections[i] = conn.getName();   
        }
        
       
        String imagePath = "." + SEP + "image" + SEP + "db.gif";
        ImageIcon icon = new ImageIcon(imagePath);
        
        String s = (String)JOptionPane.showInputDialog( s_xsFrame,
                                                        "Select a connection:",
                                                        title,
                                                        JOptionPane.PLAIN_MESSAGE,
                                                        icon,
                                                        possibleConnections,
                                                        possibleConnections[0] );
        
        DBConnection selectedConn = null;
        
        if ((s != null) && (s.length() > 0))
        {
            for( int i = 0; i < connections.size(); i++ )
            {
                DBConnection conn = (DBConnection)connections.get(i);
                if( conn.getName().equals( s ) )
                {
                    selectedConn = conn;
                    break;
                }
            }
        }

        return selectedConn;
}


    public DataBase getDB()
    {
        if( null == m_db ) m_db = new DataBaseImpl();

        return m_db;
    }
    
        
    /********************************************
     *        load an document                  *
     ********************************************/  
    class LoadDocumentAction extends AbstractAction
    {
        LoadDocumentAction()
        {
            super("LoadDocument");
        }
        
        public void actionPerformed(ActionEvent e)
        {
            if( null == m_activeMapping )
            {
                String message = "There is not an opened schema";          
                JOptionPane.showMessageDialog(s_xsFrame, message);    

                return;
            }   
            
            
            FileDialog fileDialog = new FileDialog(s_xsFrame);
             
            fileDialog.setMode(FileDialog.LOAD);
            fileDialog.show();
            
            String file = fileDialog.getFile();
            
            if (file == null)
            {
                System.out.println( "no file was chosen" );
                return;
            }
            
            String directory = fileDialog.getDirectory();
            File f = new File(directory, file);
            if (f.exists())
            {
                System.out.println( "load file: " + directory + " " + file );

                DataBase db = getDB();
                java.util.List connections = db.connections();
                

                DBConnection selectedConn = promptForAConnection( connections, "Load Document" );

                if( null != selectedConn )
                {
                    XMLLoader loader = new XMLLoader( m_activeMapping , f.getPath(), selectedConn );
                    
                    loader.process();

                    String message = "";
                    if( Configuration.getInstance().bulkLoading() )
                    {
                        message = "XML document has been dumped to disk. Please use bulkloading command to load the data into database.";
                    }
                    else
                    {
                        message = "XML document has been loaded to database, you may view tables from connection menu.";
                    }
                    
                    JOptionPane.showMessageDialog(s_xsFrame, message);
                }     
            }
            
        }
    }

    
     /********************************************
      *        help action                       *
     ********************************************/  
    class HelpAction extends AbstractAction
    {
        HelpAction()
        {
            super("Help");
        }
        
        public void actionPerformed(ActionEvent e)
        {
            if( null == m_helpFrame )
            {
                m_helpFrame = new JFrame();

                JScrollPane scroller = SwingUtil.createHtmlPane( "image/xs.html" );

                JPanel contentPane = new JPanel(); 
                contentPane.setLayout(new BorderLayout());
                contentPane.add(scroller, BorderLayout.CENTER);
                
                m_helpFrame.setContentPane(contentPane);

             
                m_helpFrame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

                WindowListener l = new WindowAdapter()
                    {
                      public void windowClosing(WindowEvent e) {m_helpFrame.setVisible(false);}
                    };
                m_helpFrame.addWindowListener( l );

                m_helpFrame.pack();
                m_helpFrame.setBounds(0,0,850,650);
            }

            m_helpFrame.show();
        }
    }           


            
    public void reLoadCurrentSchema()
    {
        String xsdpath = m_activeMapping.getXSInstance().getFilePath();
        
        m_openedSchema.remove(xsdpath);
        m_activeMapping = _performMapping( xsdpath );

        m_openedSchema.put( xsdpath, m_activeMapping );
        
        m_viewPanel.showSchemaView( m_activeMapping, true );   
    }


    
    public XToRMapping getActiveMapping()
    {
        return m_activeMapping;
    }

        
    static public XSGui getInstance()
    {
        return s_xsFrame;
    }
    

    
    static private XSGui s_xsFrame = null;
    
    private ViewPanel m_viewPanel;

    private JMenu m_window = null;

    private Map m_openedSchema = new HashMap(5);
    private XToRMapping m_activeMapping = null;

    private DataBase m_db;
    
    private JMenuItem m_docLoadingMenuItem;

    static private final String SEP = System.getProperty("file.separator");

    private JFrame m_helpFrame = null;
    
}

