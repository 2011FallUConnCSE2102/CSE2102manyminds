/*  Copyright (C) 1998-2002 Regents of the University of California
 *  This file is part of ManyMinds.
 *
 *  ManyMinds is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *  
 *  ManyMinds is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License
 *  along with ManyMinds; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
 package manyminds.artifact;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.ScrollPane;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.InputStream;

import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.DefaultListModel;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import manyminds.datamodel.Data;
import manyminds.datamodel.DataContext;
import manyminds.datamodel.DataList;
import manyminds.util.StringExploder;

public class
Scrapbook
extends JPanel {
    private JLabel myImage;
    private DataList myImageList;
    private AddScrapbookPageAction myAddAction;
    private String myName;
    private ScrollPane canvasScroller;
    private JTextArea myCaption;
    private JLabel myImageLabel;
    private JList myImageSelector;

    private class
    ImageData {
        private String myName;
        private String myCaption;
        private byte[] data;
        
        public
        ImageData(String name, String caption) {
            myName = name;
            myCaption = caption;
        }
        
        public String
        getName() {
            return myName;
        }
        
        public byte[]
        getData() {
            if (data == null) {
                data = DataContext.getContext().getSharedData().getResource(myName);
            }
            return data;
        }
        
        public String
        getCaption() {
            return myCaption;
        }
        
        public String
        toString() {
            return getName();
        }
    }

    static class PlusIcon implements Icon {
        Color baseColor = Color.gray, lineColor = Color.white;
        
        PlusIcon() {
        }
        
        public void paintIcon( Component c, Graphics g, int x, int y ) {
            g.translate( x, y );
            g.setColor(baseColor); 
            g.fillOval( 0, 0, 9, 9 );
            g.setColor(lineColor);
            g.drawLine( 1, 4, 7, 4 );
            g.drawLine( 4, 1, 4, 7 );
            g.translate( -x, -y );
        }

        public int getIconWidth() { return 9; }

        public int getIconHeight() { return 9; }
    
    }

    static class MinusIcon implements Icon {
        Color baseColor = Color.gray, lineColor = Color.white;
        
        MinusIcon() {
        }
        
        public void paintIcon( Component c, Graphics g, int x, int y ) {
            g.translate( x, y );
            g.setColor(baseColor); 
            g.fillOval( 0, 0, 9, 9 );
            g.setColor(lineColor);
            g.drawLine( 1, 4, 7, 4 );
            g.translate( -x, -y );
        }

        public int getIconWidth() { return 9; }

        public int getIconHeight() { return 9; }
    
    }

    public
    Scrapbook() {
        setLayout(new GridBagLayout());

        myImage = new JLabel();

        setLayout(new BorderLayout());
        myCaption = new JTextArea();
        myCaption.setLineWrap(true);
        myCaption.setWrapStyleWord(true);
        myCaption.setEnabled(false);
        add(myCaption,BorderLayout.NORTH);
        canvasScroller = new ScrollPane();
        canvasScroller.add(myImage);
        add(canvasScroller,BorderLayout.CENTER);
        myAddAction = new AddScrapbookPageAction();
        new java.awt.dnd.DropTarget(this,myAddAction);
        new java.awt.dnd.DropTarget(this,myAddAction);
        new java.awt.dnd.DropTarget(myCaption,myAddAction);
        new java.awt.dnd.DropTarget(myImageLabel,myAddAction);
        JPanel selectionPanel = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.gridwidth = GridBagConstraints.RELATIVE;
        c.weightx = 1.0;
        c.weighty = 0.0;
        c.fill = GridBagConstraints.HORIZONTAL;
        
        JButton plusButton = new JButton("Add to Scrapbook",new PlusIcon());
        plusButton.addActionListener(myAddAction);
        selectionPanel.add(plusButton,c);
        
        JButton minusButton = new JButton("Remove from Scrapbook",new MinusIcon());
        minusButton.addActionListener(getRemoveAction());
        c.gridwidth = GridBagConstraints.REMAINDER;
        selectionPanel.add(minusButton,c);
        
        myImageList = new DataList("scrapbook-tab-group",DataContext.getContext().getSharedData());
        myImageSelector = new JList(new DefaultListModel());
        
        myImageList.addListDataListener(new ListDataListener() {
            public void
            intervalAdded(ListDataEvent e) {
                for (int i = e.getIndex0(); i <= e.getIndex1(); ++i) {
                    String s = ((Data)myImageList.getElementAt(i)).getValue();
                    java.util.List parsed = StringExploder.explode(s);
                    if (parsed.size() > 0) {
                        String name = StringExploder.stripParens(parsed.get(0).toString());
                        String caption = StringExploder.stripParens(parsed.get(1).toString());
                        ((DefaultListModel)myImageSelector.getModel()).add(i,new ImageData(name,caption));
                    }
                }
            }
            
            public void
            intervalRemoved(ListDataEvent e) {
                for (int i = e.getIndex0(); i <= e.getIndex1(); ++i) {
                    ((DefaultListModel)myImageSelector.getModel()).remove(i);
                }
            }
            
            public void
            contentsChanged(ListDataEvent e) {
                ((DefaultListModel)myImageSelector.getModel()).clear();
                for (int i = 0; i < myImageList.getSize(); ++i) {
                    String s = ((Data)myImageList.getElementAt(i)).getValue();
                    java.util.List parsed = StringExploder.explode(s);
                    String name = StringExploder.stripParens(parsed.get(0).toString());
                    String caption = StringExploder.stripParens(parsed.get(1).toString());
                    ((DefaultListModel)myImageSelector.getModel()).add(i,new ImageData(name,caption));
                }
            }

        });
            
        
        myImageSelector.addListSelectionListener(new ListSelectionListener() {
            public void
            valueChanged(ListSelectionEvent e) {
                ImageData d = (ImageData)myImageSelector.getSelectedValue();
                setImage(d);
            }
        });
        
        /*myImageSelector.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                int index = myImageSelector.locationToIndex(e.getPoint());
                if ((index >= 0) && (index < myImageSelector.getModel().getSize())) {
                    ImageData d = (ImageData)myImageSelector.getModel().getElementAt(index);
                    setImage(d);
                }
            }
        });*/
                    
        
        for (int i = 0; i < myImageList.getSize(); ++i) {
            String s = ((Data)myImageList.getElementAt(i)).getValue();
            java.util.List parsed = StringExploder.explode(s);
            String name = StringExploder.stripParens(parsed.get(0).toString());
            String caption = StringExploder.stripParens(parsed.get(1).toString());
            ((DefaultListModel)myImageSelector.getModel()).add(i,new ImageData(name,caption));
        }
        
        c.weighty = 1.0;
        c.fill = GridBagConstraints.BOTH;
        selectionPanel.add(new JScrollPane(myImageSelector),c);
        add(selectionPanel,BorderLayout.EAST);
    }
        
    private void
    addImage(int i, String name, String caption) {
        ((DefaultListModel)myImageSelector.getModel()).add(i,new ImageData(name,caption));
    }
    
    private void
    removeImage(int i) {	
        ((DefaultListModel)myImageSelector.getModel()).remove(i);
    }
        
    private void
    setImage(final ImageData d) {
        SwingUtilities.invokeLater(new Runnable() {
            public void
            run() {
                if (d != null) {
                    byte[] data = d.getData();
                    if (data != null) {
                        try {
                            InputStream is = new ByteArrayInputStream(data);
                            BufferedImage im = ImageIO.read(is);
                            if (im == null) {
                                myImage.setIcon(null);
                            } else {
                                myImage.setIcon(new ImageIcon(im));
                            }
                        } catch (Throwable t) {
                            t.printStackTrace();
                        }
                    }
                    myCaption.setText(d.getCaption());
                } else {
                    myCaption.setText("");
                    myImage.setIcon(null);
                }
            }
        });
    }

    public Action
    getAddAction() {
        return myAddAction;
    }

    public Action
    getRemoveAction() {
        return new AbstractAction("Remove Page...") {
            public void
            actionPerformed(ActionEvent ae) {
                int i = myImageSelector.getSelectedIndex();
                if (i < 0) {
                    i = myImageSelector.getModel().getSize() - 1;
                }
                if ((i >= 0) 
                    && (i < myImageSelector.getModel().getSize())
                    && (JOptionPane.showConfirmDialog(Scrapbook.this,"Are you sure?","Are you sure?",JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION)) {
                        DataContext.getContext().getSharedData().removeResource(myImageSelector.getSelectedValue().toString());
                        myImageList.remove(i);
                }
            }
        };
    }
    
    
    
}