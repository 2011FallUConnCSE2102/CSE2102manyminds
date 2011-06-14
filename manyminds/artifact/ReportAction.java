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

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;

import javax.swing.AbstractAction;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import manyminds.datamodel.Data;
import manyminds.datamodel.DataContext;
import manyminds.datamodel.DataList;
import manyminds.debug.Level;
import manyminds.debug.Logger;
import manyminds.util.NameEncoder;
import manyminds.util.StringExploder;

import org.xml.sax.InputSource;

public class
ReportAction
extends AbstractAction
implements Runnable {
    
    private File chosenFile = null;
    private static Logger logger = Logger.getLogger("manyminds");
    
    private String reportHeader = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\" ?>\n<!DOCTYPE report PUBLIC \"manyminds-DTD\" \"manyminds.dtd\">\n";
    private String questionBody = 
            "<document name=\"student-name-0\" />\n"
            + "<image name=\"question.jpg\" source=\"http://localhost:28082/report/HeadingQuestion.jpg\" /> \n"
            + "<document-block>\n"
            + "<font style=\"big\">\n"
            + "<text><![CDATA[Question Brainstorm]]></text>\n"
            + "</font>\n"
            + "<document name=\"question-brainstorm\" />\n"
            + "<font style=\"big\">\n"
            + "<text><![CDATA[Question Prior Knowledge]]></text>\n"
            + "</font>\n"
            + "<document name=\"question-prior-knowledge\" />\n"
            + "<font style=\"big\">\n"
            + "<text><![CDATA[Research Question]]></text>\n"
            + "</font>\n"
            + "<document name=\"question\" />\n"
            + "</document-block>\n";
        
    private String hypothesizeBody =
            "<image name=\"hypothesize.jpg\" source=\"http://localhost:28082/report/HeadingHypothsize.jpg\" />"
            + "<document-block>\n"
            + "<font style=\"big\">\n"
            + "<text><![CDATA[Hypothesis Brainstorm]]></text>\n"
            + "</font>\n"
            + "<document name=\"hypothesize-brainstorm\" />\n"
            + "<font style=\"big\">\n"
            + "<text><![CDATA[Hypotheses]]></text>\n"
            + "</font>\n"
            + "<document name=\"hypothesize\" />\n"
            + "</document-block>\n";
    
    private String investigateBody = 
            "<image name=\"investigate.jpg\" source=\"http://localhost:28082/report/HeadingInvestigate.jpg\" />"
            + "<document-block>\n"
            + "<font style=\"big\">\n"
            + "<text><![CDATA[Logic of the Investigation]]></text>\n"
            + "</font>\n"
            + "<document name=\"investigate-logic\" />\n"
            + "<font style=\"big\">\n"
            + "<text><![CDATA[Investigation Plan]]></text>\n"
            + "</font>\n"
            + "<document name=\"investigate-plan\" />\n"
            + "<font style=\"big\">\n"
            + "<text><![CDATA[Investigation Data]]></text>\n"
            + "</font>\n"
            + "<document name=\"investigate-data\" />\n"
            + "<font style=\"big\">\n"
            + "<text><![CDATA[Investigation Observations and Problems]]></text>\n"
            + "</font>\n"
            + "<document name=\"investigate-observations\" />\n"
            + "</document-block>\n";

    private String analyzeBody = 
            "<image name=\"analyze.jpg\" source=\"http://localhost:28082/report/HeadingAnalyze.jpg\" />"
            + "<document-block>\n"
            + "<font style=\"big\">\n"
            + "<text><![CDATA[Summaries of the Data]]></text>\n"
            + "</font>\n"
            + "<document name=\"analyze-summary\" />\n"
            + "<font style=\"big\">\n"
            + "<text><![CDATA[Findings from the Analysis]]></text>\n"
            + "</font>\n"
            + "<document name=\"analyze-findings\" />\n"
            + "</document-block>\n";

    private String modelBody = 
            "<image name=\"model.jpg\" source=\"http://localhost:28082/report/HeadingModel.jpg\" />"
            + "<document-block>\n"
            + "<font style=\"big\">\n"
            + "<text><![CDATA[Draft of model]]></text>\n"
            + "</font>\n"
            + "<document name=\"model\" />\n"
            + "<font style=\"big\">\n"
            + "<text><![CDATA[Theoretical support for model]]></text>\n"
            + "</font>\n"
            + "<document name=\"model-theoretical-support\" />\n"
            + "<font style=\"big\">\n"
            + "<text><![CDATA[How the Model could be generalized]]></text>\n"
            + "</font>\n"
            + "<document name=\"model-generalizability\" />\n"
            + "</document-block>\n";

    private String evaluateBody = 
            "<image name=\"evaluate.jpg\" source=\"http://localhost:28082/report/HeadingEvaluate.jpg\" />"
            + "<document-block>\n"
            + "<font style=\"big\">\n"
            + "<text><![CDATA[Uses and Limits of The Model]]></text>\n"
            + "</font>\n"
            + "<document name=\"evaluate-model-uses-limits\" />\n"
            + "<font style=\"big\">\n"
            + "<text><![CDATA[Improvements for future research]]></text>\n"
            + "</font>\n"
            + "<document name=\"evaluate-research-improvements\" />\n"
            + "<font style=\"big\">\n"
            + "<text><![CDATA[Ways to Improve Your Research]]></text>\n"
            + "</font>\n"
            + "<document name=\"evaluate-research-improvements\" />\n"
            + "<font style=\"big\">\n"
            + "<text><![CDATA[Future research]]></text>\n"
            + "</font>\n"
            + "<document name=\"evaluate-future-research\" />\n"
            + "</document-block>\n"; 

    private String reportFooter = "</report>\n";

    public ReportAction() {
        super("Create Report");
    }
    
    public void
    run() {
        StringBuffer format = new StringBuffer();
        format.append(reportHeader);
        format.append("<report folder=\"");
        format.append(chosenFile.getAbsolutePath());
        format.append("\">\n");
        format.append(questionBody);
        format.append(hypothesizeBody);
        format.append(investigateBody);
        
/*        DataList dataToolList = new DataList("data-tool-tablist-group",DataContext.getContext().getSharedData());
        LinkedList chartList = new LinkedList();
        for (int i = 0; i < dataToolList.getSize(); ++i) {
            String s = ((Data)dataToolList.getElementAt(i)).getValue();
            java.util.List l = StringExploder.explode(s);
            if (l.get(1).equals("data-table")) {
                format.append("<table name=\"");
                format.append(l.get(2));
                format.append("\" />");
            } else {
                String chartCommand = StringExploder.implodeString(l.subList(1,l.size()));
                chartList.add(chartCommand);
            }
        }*/
        
        format.append(analyzeBody);
        /*
        Iterator it = chartList.iterator();
        int chartNum = 0;
        while (it.hasNext()) {
            format.append("<image source=\"");
            format.append(it.next().toString());
            format.append("\" name=\"chart");
            format.append(Integer.toString(chartNum));
            format.append(".jpg\" />");
            chartNum++;
        }*/
        
        
//        DataList figureList = new DataList("scrapbook-tab-group",DataContext.getContext().getSharedData());
//        int figNum = 1;
//        for (int i = 0; i < figureList.getSize(); ++i) {
//            String s = ((Data)figureList.getElementAt(i)).getValue();
//            java.util.List l = StringExploder.explode(s);
//            String figName = StringExploder.stripParens(l.get(0).toString());
//            String figCaption = StringExploder.stripParens(l.get(1).toString());
//            format.append("<image source =\"");
//            format.append(System.getProperty("manyminds.adviceroot"));
//            format.append("resource/");
//            format.append(NameEncoder.encode(figName));
//            format.append("\" name=\"figure");
//            format.append(Integer.toString(figNum));
//            format.append(".jpg\" />");
//            format.append("<text><![CDATA[<center>Figure "+figNum+": "+figCaption+"</center>]]></text>");
//            figNum++;
//        }
        
        
        format.append(modelBody);
        format.append(evaluateBody);
        format.append(reportFooter);
        try {
            InputSource is = new InputSource(new StringReader(format.toString()));
            is.setSystemId("/-/report.xml");
            ReportGenerator.generateRTFReport(is);
        } catch (IOException ioe) {
            logger.log(Level.WARNING,"Error generating report",ioe);
        }
    }
    
    public void
    actionPerformed(ActionEvent ae) {
        File f = new File(System.getProperty("user.home"));
        File desktop = new File (f,"Desktop");
        if (desktop.exists() && desktop.isDirectory()) {
            f = desktop;
        }
        JFileChooser chooser = new JFileChooser(f);
        int returnVal = chooser.showSaveDialog((Component)ae.getSource());
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            chosenFile = chooser.getSelectedFile();
            if (chosenFile != null) {
                if (!chosenFile.getName().endsWith(".rtf")) {
                    chosenFile = new File(chosenFile.getParentFile(),chosenFile.getName()+".rtf");
                }
                if (chosenFile.exists()) {
                    Object[] options = { "OK", "CANCEL" };
                    if (JOptionPane.showOptionDialog(null, "File Exists! Overwrite?", "Warning", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE, null, null, null) != JOptionPane.YES_OPTION) {
                        chosenFile = null;
                    }
                }
            }
            if (chosenFile != null) {
                new Thread(this).start();
            }
        } else {
            chosenFile = null;
        }
    }
}
    