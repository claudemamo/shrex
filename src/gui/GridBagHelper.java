/*
This file is part of ShreX.

ShreX is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation; either version 2 of the License, or (at your option) any later version.

ShreX is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.

You should have received a copy of the GNU General Public License along with Foobar; if not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA 
*/
package gui;

import javax.swing.*;
import java.awt.*;

public class GridBagHelper
{
    GridBagLayout gridbag;
    Container container;
    GridBagConstraints c;
    int x = 0;
    int y = 0;
    int labelAlignment = SwingConstants.RIGHT;
    double[] weights;

    public GridBagHelper(Container container, double[] weights)
    {
        this.container = container;
        this.weights = weights;

        gridbag = new GridBagLayout();
        container.setLayout(gridbag);
	
        c = new GridBagConstraints();
        c.insets = new Insets(2,2,2,2);
        c.anchor = GridBagConstraints.EAST;
        c.fill = GridBagConstraints.HORIZONTAL;
    }

    public void add(Component component)
    {
        add(component, 1);
    }

    public void add(Component component, int width)
    {
        c.gridx = x;
        c.gridy = y;
        c.weightx = weights[x];
        c.gridwidth = width;
        gridbag.setConstraints(component, c);
        container.add(component);
        x += width;
    }

    public void addButton(Component component, int start )
    {
        c.gridx = start;
        c.gridy = y;
        c.weightx = 0.1;
        c.gridwidth = 1;
        gridbag.setConstraints(component, c);
        container.add(component);
    }

    
    public void nextRow()
    {
        y++;
        x=0;
    }

    public void addLabel(String label)
    {
        add(new JLabel(label, labelAlignment));
    }

}
    
