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

import java.beans.*;
import java.lang.reflect.*;


class CalObject {

   static Object getObject(CalFormItem fItem) {
   
      Object ob = null;
      
      if (fItem.classid != null) {
         try {
            ClassLoader loader = fItem.getClass().getClassLoader();
            Class c = (loader == null) ? loader.loadClass(fItem.classid) : Class.forName(fItem.classid);
            ob = c.newInstance();
            setParameters(ob, c, fItem.options, fItem.optionIndex);
         } catch (Exception e) {
            //System.err.println("Exception creating object " + e);
            ob = null;
         }
      }

      return ob;
   }


   private static void setParameters(Object ob, Class c, CalFO[] params, int numParams) {
      if (params == null) {
         return;
      }
      BeanInfo info;
      try {
         info = Introspector.getBeanInfo(c);
      } catch (IntrospectionException e) {
         return;
      }
      PropertyDescriptor props[] = info.getPropertyDescriptors();
      for (int i=0; i<numParams; i++) {
         for (int j=0; j<props.length; j++) {
            if ((params[i].name).equalsIgnoreCase(props[j].getName())) {
               Method wm = props[j].getWriteMethod();
               if ((wm != null) && (wm.getParameterTypes().length == 1)) {
                  String[] args = { params[i].initValue };
                  try {
                     wm.invoke(ob, args);
                  } catch (Exception e2) {
                     //System.err.println("Failed to invoke: " + params[i].initValue);
                  }
               }
               break;
            }
         }
      }
   }
   
}

