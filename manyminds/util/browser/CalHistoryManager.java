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

import java.util.Vector;
import java.awt.Point;
import java.net.URL;

class CalHistoryManager implements CalCons {

   CalViewer viewer;
   Vector    historyVector;
   int       historyPosition;
   int       historyMax;


   CalHistoryManager() {
   
      historyVector   = new Vector();
      historyPosition = -1;
      historyMax      = -1;
   }


   void setViewer(CalViewer viewer) {
   
      this.viewer = viewer;
   }
   
   
   synchronized void addHistoryItem(CalHistoryItem item) {

      if (item == null) {
         return;
      }
      historyPosition++;
      historyMax = historyPosition;
      if (historyPosition >= historyVector.size()) {
         historyVector.addElement(item);
      } else {
         historyVector.setElementAt(item, historyPosition);
      }
      updatePane();
   }


   CalHistoryItem getCurrent() {

      if ((historyPosition >= 0) && (historyPosition < historyVector.size())) {
         return (CalHistoryItem)historyVector.elementAt(historyPosition);
      }
      return null;
   }


   void replaceCurrent(CalHistoryItem item) {

      if ((historyPosition >= 0) && (historyPosition < historyVector.size())) {
         historyVector.setElementAt(item, historyPosition);
      }
   }


   CalHistoryItem getBack() {

      if ((historyVector.size() > 1) && (historyPosition > 0)) {
         historyPosition--;
         updatePane();
         return (CalHistoryItem)historyVector.elementAt(historyPosition);
      }
      return null;
   }
   
   
   CalHistoryItem getForward() {

      if ((historyVector.size() > 1) && (historyPosition < historyMax) &&
                                                           (historyPosition < historyVector.size() - 1)) {
         historyPosition++;
         updatePane();
         return (CalHistoryItem)historyVector.elementAt(historyPosition);
      }
      return null;
   }
   
   
   private void updatePane() {
   
      int n = AT_HISTORY_MIDDLE;
      if (historyPosition == historyMax) {
         n = AT_HISTORY_TOP;
      }
      if (historyPosition == 0) {
         n = n | AT_HISTORY_BOTTOM;
      }
      viewer.pane.historyUpdate(n);
   }


   //private void printCurrentHistory() {
   
   // for (int i=0; i<historyVector.size(); i++) {
   //    CalHTMLHistoryItem h = (CalHTMLHistoryItem)historyVector.elementAt(i);
   //    System.out.println(i +": " + h.docURL + ", " + h.targetFrame + ", " + h.ref + "," + h.viewportPos);
   // }
   //   System.out.println("Current history position = " + historyPosition);
   //}
   
   //void printFrameState(CalHTMLHistoryItem[] state, int level) {
   
   //   for (int i=0; i<state.length; i++) {
   //      if (state[i] != null) {
   //         for (int j=0; j<level; j++) {
   //            System.out.print(" ***** ");
   //         }
   //       System.out.println(i +": " + state[i].docURL + ", " + state[i].targetFrame + ", " + state[i].ref
   //          + "," + state[i].viewportPos);
   //          if (state[i].frameState != null) {
   //             printFrameState(state[i].frameState, level + 1);
   //          }
   //      }
   //   }
   //}
}
