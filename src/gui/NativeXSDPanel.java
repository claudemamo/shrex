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
import java.beans.*;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.tree.*;
import javax.swing.event.*;
import javax.swing.undo.*;
import javax.swing.text.*;


import edu.ogi.cse.xs.xsd.*;
import edu.ogi.cse.xs.xsd.xerces.*;
import edu.ogi.cse.xs.loader.*;
import edu.ogi.cse.xs.mapping.*;


class NativeXSDPanel extends JPanel
{
    public NativeXSDPanel( XSInstance xs )
    {
        super();

        m_xs = xs;
        
        setBorder(BorderFactory.createEtchedBorder());
	setLayout(new BorderLayout());

        // create the embedded JTextComponent
	m_editor = createEditor();
	m_editor.setFont(new Font("monospaced", Font.PLAIN, 12));
	// Add this as a listener for undoable edits.
	m_editor.getDocument().addUndoableEditListener(m_undoHandler);
	
        installCommandTable();
        
	loadXSDDocument();
        
	JScrollPane scroller = new JScrollPane();
	JViewport port = scroller.getViewport();
	port.add(m_editor);

        
        m_toolbar = createToolbar();
	add( "North", m_toolbar );
	add( "Center", scroller );
    }

    
    protected void loadXSDDocument()
    {
        String xsdpath = m_xs.getFilePath();
        File f = new File( xsdpath );
        if (f.exists())
        {
            Document oldDoc = getEditor().getDocument();
            if(oldDoc != null)
            {
                oldDoc.removeUndoableEditListener(m_undoHandler);
            }
            
            getEditor().setDocument(new PlainDocument());
            
            Document doc = getEditor().getDocument();
            
            try
            {
		Reader in = new FileReader(f);
		char[] buff = new char[4096];
		int nch;
		while ((nch = in.read(buff, 0, buff.length)) != -1)
                {
		    doc.insertString(doc.getLength(), new String(buff, 0, nch), null);
		}
                
		doc.addUndoableEditListener(m_undoHandler);
		resetUndoManager();
	    }
	    catch (Throwable e)
            {
		e.printStackTrace();
	    }
        }
    }
        
        
    protected void installCommandTable()
    {
	m_commands = new Hashtable();

        //application specific actions
        m_commands.put(ACTION_KEYS[SAVE_ACTION], new SaveAction() );
        m_commands.put(ACTION_KEYS[SAVE_AND_RELOAD_ACTION], new SaveAndReloadAction() );
        m_undoAction = new UndoAction();
        m_redoAction = new RedoAction();
        m_commands.put(ACTION_KEYS[UNDO_ACTION], m_undoAction);
        m_commands.put(ACTION_KEYS[REDO_ACTION], m_redoAction);
        
        
        //JTextComponent actions
	Action[] actions = m_editor.getActions();
	for (int i = 0; i < actions.length; i++)
        {
	    Action a = actions[i];
            if( a.getValue(Action.NAME).equals( SWING_COPY_ACTION_NAME ) )
            {
                m_commands.put(ACTION_KEYS[COPY_ACTION] , a);
            }
            if( a.getValue(Action.NAME).equals( SWING_CUT_ACTION_NAME ) )
            {
                m_commands.put(ACTION_KEYS[CUT_ACTION] , a);
            }
            if( a.getValue(Action.NAME).equals( SWING_PASTE_ACTION_NAME ) )
            {
                m_commands.put(ACTION_KEYS[PASTE_ACTION] , a);
            }
	}    
    }

   
    protected JTextComponent createEditor()
    {
	return new JTextArea();
    }

  
    protected JTextComponent getEditor()
    {
	return m_editor;
    }


    
    protected Action getAction( String cmd )
    {
	return (Action) m_commands.get(cmd);
    }
    
    protected Container getToolbar()
    {
	return m_toolbar;
    }

    
    protected void resetUndoManager()
    {
	m_undo.discardAllEdits();
	m_undoAction.update();
	m_redoAction.update();
    }

    private JToolBar createToolbar()
    {
	JToolBar toolbar = new JToolBar();

        toolbar.add( createTool(SAVE_ACTION) );
        toolbar.add( createTool(SAVE_AND_RELOAD_ACTION) );

        toolbar.add(Box.createHorizontalGlue());

        
        toolbar.add( createTool(COPY_ACTION) );
        toolbar.add( createTool(CUT_ACTION) );
        toolbar.add( createTool(PASTE_ACTION) );
        
        toolbar.add(Box.createHorizontalGlue());
        
        toolbar.add( createTool(UNDO_ACTION) );
        toolbar.add( createTool(REDO_ACTION) );
                
	toolbar.add(Box.createHorizontalGlue());

        return toolbar;
    }
    

    protected Component createTool( int action )
    {
        final String SEP = System.getProperty("file.separator");
    
        String actionKey = ACTION_KEYS[action];
        String actionTip = ACTION_TIPS[action];
        
        
        String imagePath = "." + SEP + "image" + SEP + actionKey + ".gif";
	ImageIcon icon = new ImageIcon(imagePath);
        
        JButton b = new JButton( icon ) {
            public float getAlignmentY() { return 0.5f; }
	};
        b.setRequestFocusEnabled(false);
        b.setMargin(new Insets(1,1,1,1));
	
	Action a = getAction(actionKey);
	if (a != null)
        {
	    b.setActionCommand(actionKey);
	    b.addActionListener(a);
	}
        else
        {
	    b.setEnabled(false);
	}


        b.setToolTipText(actionTip);
 
        return b;
    }
    
    
    class UndoHandler implements UndoableEditListener
    {
	/**
	 * Messaged when the Document has created an edit, the edit is
	 * added to <code>undo</code>, an instance of UndoManager.
	 */
        public void undoableEditHappened(UndoableEditEvent e)
        {
	    m_undo.addEdit(e.getEdit());
	    m_undoAction.update();
	    m_redoAction.update();
	}
    }

    

    class SaveAction extends AbstractAction
    {
	SaveAction()
        {
	    super(ACTION_KEYS[SAVE_ACTION]);
	}

        public void actionPerformed(ActionEvent e)
        {
            String xsdpath = m_xs.getFilePath();
	    File f = new File(xsdpath);
            File bakfile = new File( xsdpath + ".bak" );
            
            //System.out.println( getEditor().getText() );
            
	    try
            {
                //1. mv file to bak
                f.renameTo(bakfile);
                
                //2. save the new content to file
		FileOutputStream fstrm = new FileOutputStream(f);
                PrintWriter writer = new PrintWriter(fstrm);
                
                String text = getEditor().getText();
                InputStream is = new ByteArrayInputStream(text.getBytes());
                BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                
                String line = null;
                while ((line = reader.readLine()) != null)
                {
                    writer.println(line);
                }
                writer.close();
                fstrm.close();
	    }
            catch (IOException io)
            {
		io.printStackTrace();
	    }
	}
    }


    
    class SaveAndReloadAction extends AbstractAction
    {
	SaveAndReloadAction()
        {
	    super(ACTION_KEYS[SAVE_AND_RELOAD_ACTION]);
	}

        public void actionPerformed(ActionEvent e)
        {
            String xsdpath = m_xs.getFilePath();
	    File f = new File(xsdpath);
            File bakfile = new File( xsdpath + ".bak" );

            //System.out.println( getEditor().getText() );
            
	    try
            {
                //1. mv file to bak
                f.renameTo(bakfile);

                //2. save the new content to file
		FileOutputStream fstrm = new FileOutputStream(f);
                PrintWriter writer = new PrintWriter(fstrm);
                
                String text = getEditor().getText();
                InputStream is = new ByteArrayInputStream(text.getBytes());
                BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                
                String line = null;
                while ((line = reader.readLine()) != null)
                {
                    writer.println(line);
                }
                writer.close();
                fstrm.close();
	    }
            catch (IOException io)
            {
		io.printStackTrace();
	    }
            

            //reload
            XSGui.getInstance().reLoadCurrentSchema();
	}
    }

    
    
    
    class UndoAction extends AbstractAction
    {
	public UndoAction()
        {
	    super(ACTION_KEYS[UNDO_ACTION]);
	    setEnabled(false);
	}

	public void actionPerformed(ActionEvent e)
        {
	    try
            {
		m_undo.undo();
	    }
            catch (CannotUndoException ex)
            {
		System.out.println("Unable to undo.");
		//ex.printStackTrace();
	    }
	    update();
	    m_redoAction.update();
	}

	protected void update()
        {
	    if(m_undo.canUndo())
            {
		setEnabled(true);
		putValue(Action.NAME, m_undo.getUndoPresentationName());
	    }
	    else
            {
		setEnabled(false);
		putValue(Action.NAME, ACTION_KEYS[UNDO_ACTION]);
	    }
	}
    }

    class RedoAction extends AbstractAction
    {
	public RedoAction()
        {
	    super(ACTION_KEYS[REDO_ACTION]);
	    setEnabled(false);
	}

	public void actionPerformed(ActionEvent e)
        {
	    try
            {
		m_undo.redo();
	    }
            catch (CannotRedoException ex)
            {
		System.out.println("Unable to redo: ");
		//ex.printStackTrace();
	    }
	    update();
	    m_undoAction.update();
	}

	protected void update()
        {
	    if(m_undo.canRedo())
            {
		setEnabled(true);
		putValue(Action.NAME, m_undo.getRedoPresentationName());
	    }
	    else
            {
		setEnabled(false);
		putValue(Action.NAME, ACTION_KEYS[REDO_ACTION]);
	    }
	}
    }

    

    //constants
    static final private int SAVE_ACTION = 0;
    static final private int SAVE_AND_RELOAD_ACTION = 1;
    static final private int COPY_ACTION = 2;
    static final private int CUT_ACTION = 3;
    static final private int PASTE_ACTION = 4;
    static final private int UNDO_ACTION = 5;
    static final private int REDO_ACTION = 6;

    static final int[] ALL_ACTIONS =
    {
        SAVE_ACTION,
        SAVE_AND_RELOAD_ACTION,
        COPY_ACTION ,
        CUT_ACTION,
        PASTE_ACTION,
        UNDO_ACTION,
        REDO_ACTION
    };
    
    
    static final private String[] ACTION_KEYS = 
    {
        "save",
        "reload",
        "copy",
        "cut",
        "paste",
        "undo",
        "redo"
    };
    

    static final private String[] ACTION_TIPS = 
    {
        "save the schema",
        "save and reload the schema",
        "copy",
        "cut",
        "paste",
        "undo the change",
        "redo the change"
    };
    

    
    static final private String SWING_COPY_ACTION_NAME = "copy-to-clipboard";
    static final private String SWING_CUT_ACTION_NAME = "cut-to-clipboard";
    static final private String SWING_PASTE_ACTION_NAME = "paste-from-clipboard";



    //private members
    
    private JTextComponent m_editor;
    private Map m_commands;
    private JToolBar m_toolbar;


    private UndoAction m_undoAction;
    private RedoAction m_redoAction;
    
    //Listener for the edits on the current document.
    protected UndoableEditListener m_undoHandler = new UndoHandler();

    // UndoManager that we add edits to.
    protected UndoManager m_undo = new UndoManager();
    
    
    private XSInstance m_xs;
    
}

    
    

    
    
    

    

