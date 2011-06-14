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

import java.net.*;
import java.io.*;
import javax.swing.*;
import javax.swing.text.JTextComponent;

class CalFormHandler implements CalCons {     

   CalFormItem[] fItems;
   int     formNo;
   int     fItemIndex;
   int     method;
   URL     docBaseURL;
   URL     baseURL;
   String  action;
   String  enctype;
   
   CalFormHandler(CalDoc doc, int attrStart, int attrEnd, int formNo) {

      this.formNo = formNo;
      fItems = new CalFormItem[10];
      fItemIndex = 0;
      method = V_GET;
      String s = null;
      int n;
      docBaseURL = doc.url;
      for (int i=attrStart; i<attrEnd; i++) {
         switch (doc.attrTypes[i]) {
            case A_METHOD : if (doc.attrArgs[i] == V_POST) {
                               method = V_POST;
                            } else if (doc.attrArgs[i] == V_JFORM) {
                               method = V_JFORM;
                            }
                            break;
            case A_ACTION : s = doc.attrStrings[i];
                            break;
            case A_ENCTYPE: enctype = doc.attrStrings[i];
                            break;
                           
         }
      }
      try {
         if (s == null) {
            if (doc.url != null) {
               s = doc.url.getFile();
               if (s != null) {
                  if ((method == V_GET) && ((n = s.indexOf('?')) != -1)) {
                     s = s.substring(0, n);
                  }
                  baseURL = new URL(doc.url.getProtocol(), doc.url.getHost(), doc.url.getPort(), s);
               }
            }
         } else {
            action = s;
            if ((method == V_GET) && ((n = s.indexOf('?')) != -1)) {
               s = s.substring(0, n);
            }
            baseURL = new URL(doc.url, s);
         }
      } catch (MalformedURLException e) {
         baseURL = null;
      }  
   }   


   void addFormItem(CalFormItem fItem) {
   
      if (fItem == null) {
         return;
      }
      if (fItemIndex == fItems.length) {
         CalFormItem[] a = new CalFormItem[2 * fItems.length];
         System.arraycopy(fItems, 0, a, 0, fItems.length);
         fItems = a;
      }
      fItems[fItemIndex++] = fItem;
   }


   synchronized void handleSubmission(CalForm caller, CalViewer viewer) {
   
      if ((baseURL == null) || (fItems == null)) {
         return;
      }
      URL submitURL;
      String s;
      CalFormItem fItem;
      CalForm form;
      CalView view = caller.view;
      StringBuffer sb = new StringBuffer();
      for (int i=0; i<fItemIndex; i++) {
         fItem = fItems[i];
         if ((!fItem.disabled) && (fItem.name != null)) {
            if (fItem.type == V_HIDDEN) {
               if (fItem.initValue != null) {
                  appendAmpersand(sb);
                  sb.append(URLEncoder.encode(fItem.name)).append('=').append(
                                                                     URLEncoder.encode(fItem.initValue));
               }
            } else {
               form = getFormForItem(fItem, view);
               if ((form != null) && (form.comp != null)) { 
                  if ((fItem.tagType == SELECT) && ((fItem.size > 0) || (fItem.multiple))) {
                     getListSelectData(form, sb);
                  } else if ((fItem.type == V_IMAGE) && (form == caller)) {
                     getImageData(form, sb);
                  } else {
                     s = null;
                     switch (fItem.tagType) {
                        case INPUT   : switch (fItem.type) {
                                          case V_PASSWORD   : 
                                          case V_TEXT       : s = getTextComponentData(form);
                                                              break;
                                          case V_CHECKBOX   :
                                          case V_RADIO      : s = getCheckRadioData(form);
                                                              break;
                                          case V_SUBMIT     : if (form == caller) {
                                                                 s = getSubmitButtonData(form);
                                                              }
                                                              break;
                                       }
                                       break;
                        case TEXTAREA: s = getTextComponentData(form); break;
                        case SELECT  : s = getComboSelectData(form);   break;
                        case OBJECT  : s = form.comp.toString();       break;
                        case BUTTON  : if (form == caller) {
                                          s = getSubmitButtonData(form);
                                       }
                                       break;
                     }
                     if (s != null) {
                        appendAmpersand(sb);
                        sb.append(URLEncoder.encode(form.fItem.name)).append('=').append(
                                                                                     URLEncoder.encode(s));
                     }
                  }
               }
            }
         }
      }
      if (sb.length() > 0) {
         if (viewer.pref.handleFormSubmission) {
            if (method == V_GET) {
               try {
                  submitURL = new URL(baseURL.toExternalForm() + '?' + sb.toString());
                  viewer.pane.showDocument(new CalHistoryItem(submitURL,
                                        viewer.name, null, null, null), null, true, HISTORY_NEW, 0);
               } catch (MalformedURLException e) {
                  //no handling
               }
            } else if (method == V_POST) {
               viewer.pane.handleFormPost(new CalHistoryItem(baseURL,
                             viewer.name, null, null, null), sb.toString());
            }
         }
         viewer.pane.formSubmitUpdate(docBaseURL, method, action, enctype, sb.toString());
      } else {
         if (method == V_JFORM) {
            viewer.pane.formSubmitUpdate(docBaseURL, method, action, enctype, null);
         }
      }
   }      
            

   private CalForm getFormForItem(CalFormItem fItem, CalView view) {
   
      if (view.forms != null) {
         for (int i=0; i<view.formIndex; i++) {
            if (view.forms[i].fItem == fItem) {
               return view.forms[i];
            }
         }
      }
      return null;
   }


   private String getTextComponentData(CalForm form) {
   
      JTextComponent tc = null;
      if (form.comp instanceof JTextComponent) {
         tc = (JTextComponent)form.comp;
      } else if (form.comp instanceof JScrollPane) {
         JScrollPane sp = (JScrollPane)form.comp;
         if (sp.getViewport().getView() instanceof JTextComponent) {
            tc = (JTextComponent)sp.getViewport().getView();
         }
      }
      if (tc != null) {
         return tc.getText();
      }
      return null;
   }
   

   private String getCheckRadioData(CalForm form) {
   
      if (form.comp instanceof JToggleButton) {
         JToggleButton tb = (JToggleButton)form.comp;
         if (tb.isSelected()) {
            return ((form.fItem.initValue == null) ? "on" : form.fItem.initValue);
         }
      }
      return null;
   }
   

   private String getSubmitButtonData(CalForm form) {
   
      return ((form.fItem.initValue == null) ? "submit" : form.fItem.initValue);
   }


   private String getComboSelectData(CalForm form) {
   
      String s;
      if (form.comp instanceof JComboBox) {
         JComboBox box = (JComboBox)form.comp;
         int n = box.getSelectedIndex();
         if ((n == -1) || (form.fItem.options[n].initValue == null)) {
            return box.getSelectedItem().toString();
         } else {
            return form.fItem.options[n].initValue;
         }
      }
      return null;
   }
          

   private void getListSelectData(CalForm form, StringBuffer sb) {

      String s;
      if (form.comp instanceof JScrollPane) {
         JScrollPane sp = (JScrollPane)form.comp;
         if (sp.getViewport().getView() instanceof JList) {
            JList list = (JList)(sp.getViewport().getView());
            Object[] obj = list.getSelectedValues();
            if ((obj != null) && (obj.length > 0)) {
               for (int i=0; i<obj.length; i++) {
                  s = obj[i].toString();
                  if (s != null) {
                     appendAmpersand(sb);
                     sb.append(URLEncoder.encode(form.fItem.name)).append('=').append(URLEncoder.encode(s));
                  }
               }
            }
         }
      }
   }


   private void getImageData(CalForm form, StringBuffer sb) {

      String s, s2;
      if ((s = form.fItem.initValue) != null) {
         appendAmpersand(sb);
         sb.append(URLEncoder.encode(form.fItem.name)).append('=').append(URLEncoder.encode(s));
      }
      s = form.fItem.name + ".x";
      s2 = Integer.toString(form.clickX);
      appendAmpersand(sb);
      sb.append(URLEncoder.encode(s)).append('=').append(URLEncoder.encode(s2));
      s = form.fItem.name + ".y";
      s2 = Integer.toString(form.clickY);
      appendAmpersand(sb);
      sb.append(URLEncoder.encode(s)).append('=').append(URLEncoder.encode(s2));
   }

         
   private void appendAmpersand(StringBuffer sb) {
   
      if (sb.length() > 0) {
         sb.append('&');
      }
   }
   
            
   synchronized void handleReset(CalForm caller) {
   
      CalForm form;
      CalView view = caller.view;
      for (int i=0; i<view.formIndex; i++) {
         form = view.forms[i];
         if ((form != null) && (form.fItem.formNo == this.formNo) && (!form.fItem.disabled) &&
                                                                                    (form.comp != null)) {
            switch (form.fItem.tagType) {
               case INPUT   : switch (form.fItem.type) {
                                 case V_PASSWORD   : 
                                 case V_TEXT       : resetTextComponent(form);
                                                     break;
                                 case V_CHECKBOX   :
                                 case V_RADIO      : resetCheckRadio(form);
                                                     break;
                                 }
                                 break;
               case TEXTAREA: resetTextComponent(form);   break;
               case SELECT  : resetSelectComponent(form); break;
                    default : break;
            }
         }
      }
   }      


   private void resetTextComponent(CalForm form) {
   
      JTextComponent tc = null;
      if (form.comp instanceof JTextComponent) {
         tc = (JTextComponent)form.comp;
      } else if (form.comp instanceof JScrollPane) {
         JScrollPane sp = (JScrollPane)form.comp;
         if (sp.getViewport().getView() instanceof JTextComponent) {
            tc = (JTextComponent)sp.getViewport().getView();
         }
      }
      if (tc != null) {
         if (form.fItem.initValue != null) {
            tc.setText(form.fItem.initValue);
         } else {
            tc.setText("");
         }
      }
   }


   private void resetCheckRadio(CalForm form) {
   
      if (form.comp instanceof JToggleButton) {
         JToggleButton tb = (JToggleButton)form.comp;
         tb.setSelected(form.fItem.checked);
      }
   }


   private void resetSelectComponent(CalForm form) {
   
      if (form.comp instanceof JComboBox) {
         JComboBox box = (JComboBox)form.comp;
         for (int i=0; i<form.fItem.optionIndex; i++) {
            if ((form.fItem.options[i] != null) && (form.fItem.options[i].selected)) {
               box.setSelectedIndex(i);
            }
         }
      } else {
         if (form.comp instanceof JScrollPane) {
            JScrollPane sp = (JScrollPane)form.comp;
            if (sp.getViewport().getView() instanceof JList) {
               JList list = (JList)(sp.getViewport().getView());
               int count = 0;
               int[] preselected = new int[form.fItem.optionIndex];      
               for (int i=0; i<form.fItem.optionIndex; i++) {
                  if ((form.fItem.options[i] != null) && (form.fItem.options[i].selected)) {
                     preselected[count++] = i;
                  }
               }
               if (form.fItem.multiple) {
                  if (count > 0) {
                     int[] a = new int[count];
                     System.arraycopy(preselected, 0, a, 0, count);
                     list.setSelectedIndices(a);
                  }
               } else {
                  if (count > 0) {
                     list.setSelectedIndex(preselected[count - 1]);
                  }
               }
            }
         }
      }
   }

}
