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
 package manyminds;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;
import java.awt.Toolkit;

public interface ManyMindsConstants {


    public final static String ARTIFACT_ID = "artifact";
    public final static String AGENT_ID = "agent-panel";
    public final static String AGENT_EDITOR_ID = "agent-editor";
    public final static String SCRAPBOOK_ID = "scrapbook";
    public final static String DATA_TOOL_ID = "data-tool";
    public final static String WEBDISPLAY_ID = "webdisplay";
    public final static String ADVICE_PANEL_ID = "advicepanel";

    public static final String RENAME_BUTTON_TEXT =  "Rename";
    public static final String OK_BUTTON_TEXT = "OK";
    public static final String NEW_BUTTON_TEXT = "New";
    public static final String DELETE_BUTTON_TEXT =  "Delete";
    public static final String CANCEL_BUTTON_TEXT = "Cancel";
    public static final Dimension FULL_WIDTH_FULL_HEIGHT = new Dimension(Toolkit.getDefaultToolkit().getScreenSize().width/2,(int)(Toolkit.getDefaultToolkit().getScreenSize().height * 0.9));
    public static final Dimension HALF_WIDTH_FULL_HEIGHT = new Dimension(FULL_WIDTH_FULL_HEIGHT.width/2,FULL_WIDTH_FULL_HEIGHT.height);
    public static final Dimension HALF_WIDTH_HALF_HEIGHT = new Dimension(FULL_WIDTH_FULL_HEIGHT.width/2,FULL_WIDTH_FULL_HEIGHT.height/2);
    public static final Dimension FULL_WIDTH_HALF_HEIGHT = new Dimension(FULL_WIDTH_FULL_HEIGHT.width,FULL_WIDTH_FULL_HEIGHT.height/2);
    public static final int LEFT_COLUMN_WIDTH = 106;
    public static final Font LARGE_FONT = new Font("SansSerif",Font.PLAIN,14);
    public static final Font SMALL_FONT = new Font("SansSerif",Font.PLAIN,12);
//    public static final Font LARGE_FONT = new Font("Application",Font.PLAIN,14);
//    public static final Font SMALL_FONT = new Font("Application",Font.PLAIN,12);
    public static final Color FOREGROUND_COLOR = Color.black;
    public static final Color BACKGROUND_COLOR = Color.lightGray;
    public static final Insets BUTTON_INSETS = new Insets(0,0,0,0);
    public static final int DEFAULT_FIELD_COLUMNS = 25;
    public static final int DEFAULT_AREA_ROWS = 3;
    public static final String DEFAULT_PANE_TITLE = "Unknown";
    public static final Font PANE_TITLE_FONT = new Font("Dialog", Font.BOLD, 18);
    public static final int RULE_HEIGHT = 3;
    public static final Color RULE_COLOR = FOREGROUND_COLOR;
    public static final int RULE_WEIGHT = 1;
    public static final Color BASE_BACKGROUND = new Color(0x66CCCC);
    public static final Color BASE_SHADED = new Color(0x44AAAA);
    public static final Color BASE_TEXT = new Color(0x000000);
    public static final Color BASE_STROKE = new Color(0x003399);
    public static final Color TEXT_BACKGROUND = new Color(0xFFFF99);
    public static final Color TEXT_TEXT = new Color(0x000000);
    //public static final Color TEXT_TEXT = new Color(0x003399);
    public static final Color TEXT_STROKE = new Color(0xCC9966);
    public static final Color LABEL_TEXT = new Color(0x006666);
    public static final Color RATER_BACKGROUND = new Color(0xFFFF99);
    public static final Color RATER_TEXT = new Color(0x003399);
    public static final Color RATER_STROKE = new Color(0xCC9966);
    public static final Color RATER_TRACK = new Color(0x66CCCC);
    public static final Color RATER_TRACK_STROKE = new Color(0x003399);
    public static final Color TOOLTIP_BACKGROUND = new Color(0x66CCCC);
    public static final Color TOOLTIP_TEXT = new Color(0x000000);
    public static final Color TOOLTIP_STROKE = new Color(0x003399);
    public static final Color HIGHLIGHT_BACKGROUND = new Color(0x66CCCC);
    public static final Color HIGHLIGHT_TEXT = new Color(0x000000);
    public static final Color HIGHLIGHT_STROKE = new Color(0x003399);

    public static final Color QUESTION_COLOR = new Color(0xBB88EE);
    public static final Color HYPOTHESIZE_COLOR = new Color(0xFFCC66);
    public static final Color INVESTIGATE_COLOR = new Color(0x99CC66);
    public static final Color ANALYZE_COLOR = new Color(0xFF6633);
    public static final Color MODEL_COLOR = new Color(0x6699CC);
    public static final Color EVALUATE_COLOR = new Color(0xEE5555);
    
    public static final String TEXT_ENCODING = new String("MacRoman");
}