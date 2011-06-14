/*================================================================================

                        Freeware License Agreement
                        ==========================

 This software is provided 'as-is', without any express or implied warranty.
 In no event will the authors be held liable for any damages arising from the
 use of this software.

 Permission is granted to anyone to use this software for any purpose, including
 commercial applications, and to alter it and redistribute it freely, subject to
 the following restrictions:

       1. The origin of this software must not be misrepresented; you must not
          claim that you wrote the original software. If you use this software
          in a product, an acknowledgment in the product documentation would be
          appreciated but is not required.

       2. Altered source versions must be plainly marked as such, and must not
          be misrepresented as being the original software.

       3. This notice may not be removed or altered from any source distribution.


 Author(s):
      Andrew Moulden 1998 (original copyright holder)

 Original package: CalHTMLPane html renderer, part of the calpa package

==================================================================================*/

package manyminds.util.browser;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.net.URL;
import java.net.MalformedURLException;

class CalNavBar implements CalCons {

   JPanel      topPanel;
   JPanel      menuPanel;
   JPanel      buttonPanel;
   CalHTMLPane pane;
   JButton     backButton;
   JButton     forwardButton;
   JButton     stopButton;
   JButton     reloadButton;
   JLabel      urlLabel;
   JLabel      statusLabel;
   JTextField  tf;
   int         mpHeight;
   NavListener nl;
   
   CalNavBar(CalHTMLPane pane, CalHTMLPreferences pref) {
   
      this.pane = pane;
      menuPanel = new JPanel();
      menuPanel.setLayout(new BorderLayout());
      Font font = new Font("Dialog", Font.BOLD, 11);
      buttonPanel    = new JPanel(new FlowLayout());
      topPanel       = new JPanel(new BorderLayout());
      backButton     = new JButton("Back");
      forwardButton  = new JButton("Forward");
      stopButton     = new JButton("Stop");
      reloadButton   = new JButton("Reload");
      Font font2 = new Font("SansSerif", Font.PLAIN, 11);
      Font font3 = new Font("SansSerif", Font.PLAIN, 12);
      Insets ins = new Insets(0, 4, 0, 4);
      backButton.setFont(font2);
      forwardButton.setFont(font2);
      stopButton.setFont(font2);
      reloadButton.setFont(font2);
      backButton.setMargin(ins);
      forwardButton.setMargin(ins);
      stopButton.setMargin(ins);
      reloadButton.setMargin(ins);
      //backButton.setEnabled(false);
      //forwardButton.setEnabled(false);
      stopButton.setEnabled(false);
      buttonPanel.add(backButton);
      buttonPanel.add(forwardButton);
      buttonPanel.add(stopButton);
      buttonPanel.add(reloadButton);
      tf = new JTextField();
      tf.setMargin(CalCons.TEXT_INSETS);
      tf.setOpaque(true);
      tf.setFont(font2);
      urlLabel = new JLabel("URL:");
      urlLabel.setForeground(Color.black);
      urlLabel.setFont(font);
      urlLabel.setBorder(BorderFactory.createEmptyBorder(0,0,0,3));
      statusLabel = new JLabel("Status");
      statusLabel.setForeground(Color.black);
      statusLabel.setFont(font2);
      statusLabel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createCompoundBorder(
          BorderFactory.createEmptyBorder(0, 8, 0, 0),
            BorderFactory.createLineBorder(Color.black, 1)),
              BorderFactory.createEmptyBorder(1,2,1,2)));
      statusLabel.setPreferredSize(new Dimension(105, statusLabel.getPreferredSize().height));
      statusLabel.setHorizontalAlignment(SwingConstants.CENTER);
      topPanel.add("West", urlLabel);
      topPanel.add("Center", tf);
      topPanel.add("East", statusLabel);
      int h = buttonPanel.getPreferredSize().height;
      int h2 = topPanel.getPreferredSize().height;
      h -= h2;
      if (h > 0) {
         h = (h + 1) / 2;
      } else {
         h = 0;
      }   
      topPanel.setBorder(BorderFactory.createEmptyBorder(h, 4, h, 8));
      menuPanel.add("Center", topPanel);
      menuPanel.add("West", buttonPanel);
      mpHeight = menuPanel.getPreferredSize().height;
      nl = new NavListener();
      backButton.addActionListener(nl);
      forwardButton.addActionListener(nl);
      reloadButton.addActionListener(nl);
      stopButton.addActionListener(nl);
      tf.addActionListener(nl);
   }

   
   private class NavListener implements ActionListener {

      public void actionPerformed(ActionEvent e) {

          String s, s2;
          
          if (e.getSource() == tf) {
             s = tf.getText();
             if (s != null) {
                s = s.trim();
                if (s == null) {
                   return;
                }
                URL url = null;
                try {
                   url = new URL(s);
                } catch (MalformedURLException e1) {
                   url = null;
                   try {
                      if (s.length() > 3) {
                         s2 = s.substring(0, 3);
                         if (("www").equalsIgnoreCase(s2)) {
                            s2 = "http://" + s;
                         } else {
                            s2 = s.substring(s.length() - 3);
                            if (("com").equalsIgnoreCase(s2)) {
                               s2 = "http://www." + s;
                            } else {
                               s2 = "http://www." + s + ".com";
                            }
                         }
                      } else {
                         s2 = "http://www." + s + ".com";
                      }     
                      url = new URL(s2);
                   } catch (MalformedURLException e2) {
                      url = null;
                      try {
                         s2 = "http://www." + s + ".com";
                         url = new URL(s2);
                      } catch (MalformedURLException e3) {
                         url = null;
                         try {
                            if ((pane.viewer.doc != null) && (pane.viewer.doc.url != null)) {
                               url = new URL(pane.viewer.doc.url, s);
                            }
                         } catch (MalformedURLException e4) {
                            url = null;
                         }
                      }
                   }
                }
                if (url != null) {
                   pane.showHTMLDocument(url);
                } else {
                   System.out.println("Invalid URL I'm afraid");
                   //SHOW ERROR DIALOG
                }
             }
          } else {
             s = e.getActionCommand();
             if (("Reload").equals(s)) {
                pane.reloadDocument();
             } else if (("Back").equals(s)) {
                pane.goBack();
             } else if (("Forward").equals(s)) {
                pane.goForward();
             } else if (("Stop").equals(s)) {
                pane.stopAll();
             }
          }
       }
    }

}      
      
