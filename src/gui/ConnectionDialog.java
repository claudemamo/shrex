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
import javax.swing.tree.*;
import javax.swing.event.*;
import javax.swing.table.*;

import edu.ogi.cse.xs.xsd.*;
import edu.ogi.cse.xs.xsd.xerces.*;
import edu.ogi.cse.xs.database.*;
import edu.ogi.cse.xs.loader.*;
import edu.ogi.cse.xs.mapping.*;

class ConnectionDialog
{
    public ConnectionDialog()
    {}
    

    static public boolean addConnection()
    {
        boolean added = false;
        
        String[] connectOptionNames = { "Add" };
        JPanel connPane =  _createConnectionPanel();
        if( JOptionPane.showOptionDialog( XSGui.getInstance(),
                                          connPane,
                                          "conneciton information",
                                          JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE,
                                          null,
                                          connectOptionNames,
                                          connectOptionNames[0]) == 0 )
        {
            DataBase db = XSGui.getInstance().getDB();
            
            db.addConnection( idField.getText(),
                              driverField.getText(),
                              serverField.getText(),
                              userNameField.getText(),
                              passwordField.getText() );
            
            added = true;
	}
	else
        {
            //do nothing
        }
        
        return added;
    }
    

    static public boolean removeConnection()
    {    
        DataBase db = XSGui.getInstance().getDB();
        java.util.List connections = db.connections();
        
        DBConnection toBeRemoved = XSGui.getInstance().promptForAConnection( connections,
                                                                             "remove a connection" );

        if( null == toBeRemoved ) return false;

        
        db.removeConnection( toBeRemoved.getName() );

        return true;
    }
    
    
    static private JPanel _createConnectionPanel()
    {
 	// Create the labels and text fields.
	JLabel idLabel = new JLabel("Connection ID: ", JLabel.RIGHT);
 	idField = new JTextField("mysql");
        
	userNameLabel = new JLabel("User name: ", JLabel.RIGHT);
 	userNameField = new JTextField("fangdu");
        
	passwordLabel = new JLabel("Password: ", JLabel.RIGHT);
	passwordField = new JTextField("");
        
        serverLabel = new JLabel("Database URL: ", JLabel.RIGHT);
	//serverField = new JTextField("jdbc:db2:xyz");
        serverField = new JTextField("jdbc:mysql://localhost/xs");
        
	driverLabel = new JLabel("Driver: ", JLabel.RIGHT);
	//driverField = new JTextField("COM.ibm.db2.jdbc.app.DB2Driver");
        driverField = new JTextField("com.mysql.jdbc.Driver");

	JPanel connectionPanel = new JPanel(false);
	connectionPanel.setLayout(new BoxLayout(connectionPanel,
						BoxLayout.X_AXIS));

	JPanel namePanel = new JPanel(false);
	namePanel.setLayout(new GridLayout(0, 1));
        namePanel.add(idLabel);
	namePanel.add(userNameLabel);
	namePanel.add(passwordLabel);
	namePanel.add(serverLabel);
	namePanel.add(driverLabel);

	JPanel fieldPanel = new JPanel(false);
	fieldPanel.setLayout(new GridLayout(0, 1));
        fieldPanel.add(idField);
	fieldPanel.add(userNameField);
	fieldPanel.add(passwordField);
	fieldPanel.add(serverField);
        fieldPanel.add(driverField);

	connectionPanel.add(namePanel);
	connectionPanel.add(fieldPanel);

        return connectionPanel;
    }

    
    static private JLabel      idLabel;
    static private JTextField  idField;
    static private JLabel      userNameLabel;
    static private JTextField  userNameField;
    static private JLabel      passwordLabel;
    static private JTextField  passwordField;
    static private JLabel      serverLabel;
    static private JTextField  serverField;
    static private JLabel      driverLabel;
    static private JTextField  driverField;
}

    
