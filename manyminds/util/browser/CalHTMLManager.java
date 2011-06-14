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

import java.util.Hashtable;
import java.util.Enumeration;
import java.net.URL;
import java.net.MalformedURLException;
import java.awt.Image;
import javax.swing.JComponent;


/**
 * A class with static members and methods which primarily controls the caching of data
 * which will be available to all current instances of <code>CalHTMLPane</code>.
 * @see     calpa.html.CalHTMLPane
 */
public class CalHTMLManager {

   static Hashtable documentCache       = new Hashtable();
   static Hashtable dialogCache         = new Hashtable();
   static Hashtable componentCache      = new Hashtable();
   static Hashtable componentArrayCache = new Hashtable();
   static Hashtable imageCache          = new Hashtable();
   static Hashtable imageMapHash        = new Hashtable();
   static String    dummyString         = "http://caldoc/doc";
   static int       dummyCount          = 1;
   static int       docCacheMaxSize     = 8000;
   static int       currDocCacheSize    = 0;;
   static boolean   cacheDocuments      = true;
   static boolean   enableCalFocusManager = true;
   static CalFonts  f                   = new CalFonts();

   private CalHTMLManager() {}      //prevents instantiation    


   static synchronized void addDocument(CalDoc doc) {
   
      int n;
      if ((doc != null) && (doc.linkHash != 0)) {
         CalCDoc doc2;
         int size = 0;
         Integer value = new Integer(doc.linkHash);
         if ((doc2 = (CalCDoc)documentCache.get(value)) != null) {
            documentCache.remove(value);
            currDocCacheSize -= doc2.size;
            doc2 = null;
         }
         if (doc.fileSize >= 0) {
            size = ((doc.fileSize * 9) / 4000) + 2;   // +2 tries to make allowance for doc variables
         } else {
            if (doc.charArray != null) {
               size = ((doc.charArray.length * 4) / 1000) + 2;
            }
         } 
         doc2 = new CalCDoc(doc, System.currentTimeMillis(), size);
         while (currDocCacheSize + size > docCacheMaxSize) {
            if (documentCache.isEmpty()) {
               break;
            } else {
               n = removeOldestDoc();
               if (n == -1) {
                  break;
               }
            }
         }
         if (currDocCacheSize + size <= docCacheMaxSize) {
            currDocCacheSize += size;  
            documentCache.put(value, doc2);
         }
      }
   }
   
   
   static synchronized void removeDocument(URL url) {
   
      Integer value;
      if (url != null) {
         CalCDoc doc;
         int j = getHashCode(url);
         if (j != 0) {
            value = new Integer(j);
            if ((doc = (CalCDoc)documentCache.get(value)) != null) {
               documentCache.remove(value);
               currDocCacheSize -= doc.size;
               doc = null;
            }
         }
      }
   }


   static synchronized CalDoc getDocument(URL url) {
   
      if (url != null) {
         CalCDoc doc;
         int j = getHashCode(url);
         if (j != 0) {
            if ((doc = (CalCDoc)documentCache.get(new Integer(j))) != null) {
               if (url.sameFile(doc.doc.docURL)) {
                  return doc.doc;
               }
            }
         }
      }
      return null;
   }


   static synchronized void addDialogMessage(CalDoc doc, String name) {
   
      if ((doc != null) && (name != null)) {
         documentCache.put(name, doc);
      }
   }


   static synchronized CalDoc getDialogMessage(String name) {
   
      CalDoc doc = null;
      if (name != null) {
         doc = (CalDoc)documentCache.get(name);
      }
      return doc;
   }


   static synchronized void addImageMap(CalImageMap map) {
   
      int maphash;
      String s;
      
      if (map != null) {
         maphash = map.url.hashCode();
         s = map.url.getRef();
         if (s != null) {
            maphash += s.hashCode();
         }
         imageMapHash.put(new Integer(maphash), map);
      }
   }      


   static synchronized CalImageMap getImageMap(int n) {
      
      return (CalImageMap)imageMapHash.get(new Integer(n));
   }
   
   
   static int getHashCode(URL url) {

      int r, j=0;
      char[] a = url.toExternalForm().toCharArray();
      r = a.length;
      for (int k=a.length-1; k>4; k--) {
         if (a[k] == '#') {
            r = k;
            break;
         }
      }               
      for (int k=r, p=0; k>0; k--) {
         j = (j * 37) + a[p++];
      }
      
      return j;
   }      


   /**
   * Adds a JComponent to the Manager's component cache. The component can then be incorporated
   * into an HTML document using the &lt;OBJECT&gt; tag.<BR>
   * The component must have an assigned name (set by calling the
   * <code>AWT Component setName()</code> method).
   * JComponents sent to this method which do not have a name will not be cached by the Manager.
   * @param  component   the JComponent to be added.
   */
   public static synchronized void addUserComponent(JComponent component) {
   
      if ((component != null) && (component.getName() != null)) {
         componentCache.put(component.getName(), component);
      }
   }      


   /**
   * Removes a JComponent from the Manager's component cache.
   * @param  name   the name of the JComponent to be removed.
   */   
   public static synchronized void removeUserComponent(String name) {
      
      if (name != null) {
         componentCache.remove(name);
      }
   }


   /**
   * Adds an array of JComponents to the Manager's component cache.
   * Individual components from this array can then be incorporated
   * into an HTML document using the &lt;OBJECT&gt; tag.<BR>
   * The component array must have an assigned class name.
   * Arrays sent to this method which do not have a class name will not be cached by the Manager.
   * @param  componentArray   the JComponent array to be added.
   * @param  classname        a class identifier for the JComponent array.
   */
   public static synchronized void addUserComponentArray(JComponent[] componentArray, String classname) {
   
      if ((componentArray != null) && (classname != null)) {
         componentArrayCache.put(classname, componentArray);
      }
   }      


   /**
   * Removes a JComponent array from the Manager's component cache.
   * @param  classname   the class identifier of the JComponent array to be removed.
   */   
   public static synchronized void removeUserComponentArray(String classname) {
      
      if (classname != null) {
         componentArrayCache.remove(classname);
      }
   }


   /**
   * Retrieves a JComponent from the Manager's component cache. If the JComponent was added to
   * the cache as part of a component array then the class name of the array must be sent to
   * this method in order to retrieve it.
   * @param   classname   the class identifier of the array the JComponent resides in (may be null).
   * @param   name        the name of the target JComponent.
   * @return  the requested JComponent or <code>null</code> if no JComponent matching the sent
   * parameters can be found
   * @see     calpa.html.CalHTMLManager#addUserComponent
   * @see     calpa.html.CalHTMLManager#addUserComponentArray
    */   
   public static synchronized JComponent getUserComponent(String classname, String name) {
      
      JComponent c = null;
      if (name != null) {
         if (classname == null) {
            c = (JComponent)componentCache.get(name);
         } else {
            JComponent[] c2 = (JComponent[])componentArrayCache.get(classname);
            if (c2 != null) {
               for (int i=0; i<c2.length; i++) {
                  if ((c2[i] != null) && (name.equals(c2[i].getName()))) {
                     c = c2[i];
                     break;
                  }
               }
            }
         }
      }
      
      return c;
   }


   /**
   * Adds an Image to the Manager's image cache. The Image can then be incorporated
   * into an HTML document by naming the image within an &lt;IMG&gt; tag.
   * Incorporating images into documents via the Manager allows the programmer to ensure
   * that a fully loaded image is always available. In addition the image will always be
   * displayed, even if <code>loadImages</code> in <code>CalHTMLPreferences</code> has
   * been disabled. This can be useful when images are being displayed in buttons and other
   * form controls.
   * <P>See the README.TXT file accompanying this documentation for further details.
   * @param   img  the Image to be cached.
   * @param   name a name given to the Image for retrieval purposes.
   * @see     calpa.html.CalHTMLManager#getUserImage
   */
   public static synchronized void addUserImage(Image img, String name) {
   
      if ((img != null) && (name != null)) {
         imageCache.put(name, img);
      }
   }      


   /**
   * Retrieves an Image from the Manager's image cache.
   * @param   name  the name of the target Image.
   * @return  the requested Image or <code>null</code> if no Image matching the sent
   * name can be found
   * @see     calpa.html.CalHTMLManager#addUserImage
   */
   public static synchronized Image getUserImage(String name) {
      
      Image img = null;
      if (name != null) {
         img = (Image)imageCache.get(name);
      }
      return img;
   }


   /**
   * Removes an Image from the Manager's image cache.
   * @param   name  the name of the Image to be removed.
   */
   public static synchronized void removeUserImage(String name) {
      
      if (name != null) {
         imageCache.remove(name);
      }
   }


   static URL getDummyURL() {
   
      URL dummyURL = null;
      try {
         dummyURL = new URL(dummyString + Integer.toString(dummyCount));
         dummyCount++;
      } catch (MalformedURLException e) {
         //no handling
      }
      return dummyURL;
   }


   /**
   * Enables or disables the caching of documents by the Manager. The default setting
   * is true (enabled). 
   * @param   enabled  a flag indicating whether the Manager should cache documents.
   */   
   public static void setCacheDocuments(boolean enabled) {
   
      cacheDocuments = enabled;
   }
   

   /**
   *  Indicates whether document caching is currently enabled. 
   *  @return  a flag indicating whether the Manager is currently able to cache documents.
   */      
   public static boolean isCacheDocumentsEnabled() {
   
      return cacheDocuments;
   }
   
   
   /**
   * Sets the size of the document cache used by the Manager. The size value represents Kilobytes
   * and should be greater than 0. The default value is 8000 (8Mb). Note that this can only be a
   * guide to the Manager as to how many documents to cache.
   * It is possible that the cache size figure may be exceeded
   * due to the difficulty of calculating the memory usage of a fully-parsed HTML document.
   * <P>Once the cache size has been exceeded the Manager will empty the oldest document from the cache
   * and repeat this process until the memory taken up by documents in the cache falls below the cache size.
   * <P>Documents in the Manager's cache do not contain images. These are cached by the JVM and will take
   * up additional memory (sometimes far in excess of the memory taken by documents).
   * <P>Note: Kilobytes have been used in order to allow for the creation of very small cache sizes.
   * A cache size of 50K will be meaningless when dealing with normal HTML documents, but it allows for
   * the caching of mini 'documents' which have been created via Strings rather than URLs.
   * @param   size  the required size in kilobytes of the Manager's document cache.
   */
   public static void setDocumentCacheMaximumSize(int size) {
   
      if (size > 0) {
         docCacheMaxSize = size;
      }
   }


   /**
   * Returns the max size of the document cache used by the Manager.
   * @return the size in kilobytes of the Manager's document cache.
   */
   public static int getDocumentCacheMaximumSize() {
   
      return docCacheMaxSize;
   }


   /**
   * Returns the Manager's estimate of the current memory being taken up by cached documents. The
   * return value represents Kilobytes.
   * <P>The estimate does not include memory which is being taken up by cached document
   * images. These are managed separately by the JVM. Note also that a call to
   * <code>Runtime.getRuntime().freeMemory()</code> will invariably show a memory usage greater
   * than indicated by this method. This is due to the additional memory being used by any
   * <code>CalHTMLPane</code> objects and subsidiary objects related to the current document view
   * they are displaying.
   * @return an estimate of the current memory (in kilobytes) being used by cached documents.
   */
   public static int getDocumentCacheCurrentSize() {
   
      return currDocCacheSize;
   }


   /**
   *Removes the oldest document from the Manager's document cache.
   */
   public static void removeOldestDocumentFromCache() {
   
      removeOldestDoc();
   }


   private static int removeOldestDoc() {

      long time = 0;
      int size = -1;
      Object current, oldest;
      CalCDoc doc;
      if (documentCache != null) {
         current = null;
         oldest  = null;
         for (Enumeration e = documentCache.keys(); e.hasMoreElements(); ) {
            current = e.nextElement();
            doc = (CalCDoc)documentCache.get(current);
            if ((doc != null) && ((time == 0) || (doc.time < time))) {
               oldest = current;
               size = doc.size;
               time = doc.time;
            }
         }
         if (oldest != null) {
            documentCache.remove(oldest);
            currDocCacheSize -= size;
         } else {
            size = -1;
         }
      }
      return size;
   }



   /**
   *Empties the Manager's document cache.
   */
   public static void emptyDocumentCache() {
   
      if (documentCache != null) {
         for (Enumeration e = documentCache.keys(); e.hasMoreElements(); ) {
            documentCache.remove(e.nextElement());
         }
         currDocCacheSize = 0;
      }
   }


   /**
   * Enables or disables the <code>CalFocusManager</code>. When a <code>CalHTMLPane</code> is
   * first instantiated it will replace the incumbent <code>FocusManager</code> with a
   * <code>CalFocusManager</code> unless disabled from doing so by this method.
   * The <code>CalFocusManager</code> should function identically to Swing's
   * <code>DefaultFocusManager</code> outside a <code>CalHTMLPane</code> but extends functionality when a
   * <code>CalHTMLPane</code> has keyboard focus (e.g. it enables tabbing between hyperlinks).<BR>
   * See the README.TXT file accompanying this documentation for more details on why the
   * <code>CalFocusManager</code> has been implemented in this way.
   * <P>(Note that this method will neither install nor uninstall the CalFocusManager.)
   * @param   enabled  a flag indicating whether a CalFocusManager should handle keyboard control within
   * a <code>CalHTMLPane</code>.
   */
   public static void setCalFocusManagerEnabled(boolean enabled) {
   
      enableCalFocusManager = enabled;
   }
   

   /**
   * Indicates whether a CalFocusManager is currently enabled.
   * @return  a flag indicating whether a <code>CalFocusManager</code> should handle keyboard traversal
   * @see     calpa.html.CalHTMLManager#setCalFocusManagerEnabled
   */
   public static boolean isCalFocusManagerEnabled() {
   
      return enableCalFocusManager;
   }

}     
