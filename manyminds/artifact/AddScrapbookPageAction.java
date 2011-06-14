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

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.Iterator;

import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import manyminds.datamodel.Data;
import manyminds.datamodel.DataContext;
import manyminds.datamodel.DataList;
import manyminds.datamodel.ManyMindsDocument;
import manyminds.debug.Level;
import manyminds.debug.Logger;

public class
AddScrapbookPageAction
extends AbstractAction
implements Runnable, DropTargetListener {

    private JFileChooser myChooser = new JFileChooser();
    private File fileToLoad = null;
    private JDialog myDialog = new JDialog();
    private JButton selectFile = new JButton("Choose File");
    private JTextField titleField = new JTextField();
    private JTextArea captionField = new JTextArea(3,50);
    private JLabel fileName = new JLabel("No File Chosen");
    private DataList myPageList = new DataList("scrapbook-tab-group",DataContext.getContext().getSharedData());


    public
    AddScrapbookPageAction() {
        super("Add Page...");
        myChooser.setDialogTitle("Please choose image file");
        JPanel jp = new JPanel();
        jp.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();

        c.weightx = 1.0;
        c.weighty = 0.0;

        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridwidth = GridBagConstraints.RELATIVE;
        jp.add(titleField,c);
        
        c.fill = GridBagConstraints.NONE;
        c.gridwidth = GridBagConstraints.REMAINDER;
        jp.add(new JLabel("Image Title"),c);
        
        c.weighty = 1.0;
        c.fill = GridBagConstraints.BOTH;
        c.gridwidth = GridBagConstraints.RELATIVE;
        captionField.setLineWrap(true);
        captionField.setWrapStyleWord(true);
        jp.add(new JScrollPane(captionField),c);
        
        c.fill = GridBagConstraints.NONE;
        c.gridwidth = GridBagConstraints.REMAINDER;
        jp.add(new JLabel("Image Caption"),c);
        
        c.weighty = 0.0;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridwidth = GridBagConstraints.RELATIVE;
        jp.add(fileName,c);
        
        c.fill = GridBagConstraints.NONE;
        c.gridwidth = GridBagConstraints.REMAINDER;
        selectFile.addActionListener(new ActionListener() {
            public void
            actionPerformed(ActionEvent ae) {
                int i = myChooser.showOpenDialog(null);
                if (i == JFileChooser.APPROVE_OPTION) {
                    fileToLoad = myChooser.getSelectedFile();
                    fileName.setText(fileToLoad.getName());
                }
            }
        });
        
        jp.add(selectFile,c);
        JCheckBox jcb = new JCheckBox("Use Clipboard Data");
        jcb.addActionListener(new ActionListener() {
            public void
            actionPerformed(ActionEvent ae) {
                if (((JCheckBox)ae.getSource()).isSelected()) {
                    fileToLoad = null;
                    fileName.setText("From Clipboard");
                    fileName.setEnabled(false);
                    selectFile.setEnabled(false);
                } else {
                    fileName.setText("");
                    fileName.setEnabled(true);
                    selectFile.setEnabled(true);
                }
            }
        });
        jp.add(jcb,c);
        c.gridwidth = GridBagConstraints.RELATIVE;
        JButton jb = new JButton("Cancel");
        jb.addActionListener(new ActionListener() {
            public void
            actionPerformed(ActionEvent ae) {
                fileToLoad = null;
                fileName.setText("");
                captionField.setText("");
                titleField.setText("");
                myDialog.hide();
            }
        });
        jp.add(jb,c);
        jb = new JButton("Okay");
        jb.addActionListener(new ActionListener() {
            public void
            actionPerformed(ActionEvent ae) {
                if ((!"".equals(captionField.getText())) && (!"".equals(titleField.getText())) && (((fileToLoad != null) || ("From Clipboard".equals(fileName.getText()))))) {
                    (new Thread(AddScrapbookPageAction.this)).start();
                    myDialog.hide();
                } else if ("".equals(captionField.getText())) {
                    Toolkit.getDefaultToolkit().beep();
                    JOptionPane.showMessageDialog(myDialog, "Please enter a caption.", "alert", JOptionPane.ERROR_MESSAGE); 
                } else if ("".equals(titleField.getText())) {
                    Toolkit.getDefaultToolkit().beep();
                    JOptionPane.showMessageDialog(myDialog, "Please enter a title.", "alert", JOptionPane.ERROR_MESSAGE); 
                } else if (fileToLoad == null) {
                    Toolkit.getDefaultToolkit().beep();
                    JOptionPane.showMessageDialog(myDialog, "Please select a file.", "alert", JOptionPane.ERROR_MESSAGE); 
                }
            }
        });
        c.gridwidth = GridBagConstraints.REMAINDER;
        jp.add(jb,c);
        myDialog.setContentPane(jp);
        myDialog.setSize(manyminds.ManyMindsConstants.FULL_WIDTH_HALF_HEIGHT);
    }
    
    public void
    actionPerformed(ActionEvent ae) {
        myDialog.show();
    }
    
    public void
    run() {
        try {
            BufferedInputStream in = null;
            byte[] header = null;
            byte[] data = null;
            if (fileToLoad != null) {
                in = new BufferedInputStream(new FileInputStream(fileToLoad));
                ByteArrayOutputStream dataOut = new ByteArrayOutputStream();
                if (header != null) {
                    dataOut.write(header);
                }
                data = new byte[1024];
                int len;
                while ((len = in.read(data, 0, data.length)) != -1) {
                    dataOut.write(data);
                }
                data = dataOut.toByteArray();
            } else if ("From Clipboard".equals(fileName.getText())) {
                Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                Transferable t = clipboard.getContents(this);
                Image img = (Image)t.getTransferData(DataFlavor.imageFlavor);
                BufferedImage bimg = null;
                if (img instanceof BufferedImage) {
                    bimg = (BufferedImage)img;
                } else {
                    bimg = new BufferedImage(img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_INT_RGB);
                    bimg.getGraphics().drawImage(img,0,0,Color.white,null);
                }
                
                
                /*if (img instanceof apple.awt.CImage) {
                    ((apple.awt.CImage)img).convertToBI();
                    bimg = (BufferedImage)((apple.awt.CImage)img).getConvertedBI();
                }*/
                if (bimg != null) {
                    ByteArrayOutputStream bos = new ByteArrayOutputStream();
                    ImageIO.write((RenderedImage)bimg,"jpeg",bos);
                    data = bos.toByteArray();
                }
            }
            if (data != null) {
                DataContext.getContext().getSharedData().addResource(titleField.getText(),data);
                Data mmd = ManyMindsDocument.newDocument();
                String s = DataContext.getContext().getSharedData().addUniqueID("scrapbook-tab",mmd);
                mmd = DataContext.getContext().getSharedData().getData(s);
                mmd.setValue("("+titleField.getText()+") ("+captionField.getText()+")");
                myPageList.add(s);
            } 
            fileToLoad = null;
            fileName.setText("No File Chosen");
            captionField.setText("");
            titleField.setText("");
        } catch (Throwable t) {
            t.printStackTrace();
            Logger.getLogger("manyminds").log(Level.WARNING,"Error adding scrapbook page",t);
        }
    }

    public void
    dragEnter(DropTargetDragEvent dtde) {
    }
    
    public void
    dragExit(DropTargetEvent dte) {
    }
    
    public void
    dragOver(DropTargetDragEvent dtde) {
    }
    
    public void
    drop(DropTargetDropEvent dtde) {
        DataFlavor[] flavaz = dtde.getCurrentDataFlavors();
        for (int i = 0; i < flavaz.length; ++i) {
            System.err.println(flavaz[i].toString());
        }
        if (dtde.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
            try {
                Object o = dtde.getTransferable().getTransferData(DataFlavor.javaFileListFlavor);
                Iterator it = ((java.util.List)o).iterator();
                while (it.hasNext()) {
                    fileToLoad = (File)it.next();
                    fileName.setText(fileToLoad.getName());
                    myDialog.show();
                }
            } catch (Throwable t) {
                Logger.getLogger("manyminds").log(Level.WARNING,"Error accepting drop",t);
            }
        } else {
        }
    }
    
    public void
    dropActionChanged(DropTargetDragEvent dtde) {
    }
}