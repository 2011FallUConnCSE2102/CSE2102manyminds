/*	File AppletProgressBar.java
 * =============================================================================
 * 
 * A class with static access methods for progress monitoring.  Used at creation
 * time.
 *
 * Author Eric Eslinger
 * Copyright © 2000 University of California
 * All Rights Reserved.
 * 
 * Agenda
 * 
 * History
 * 15 Mar 00	EME	Created
 * 
 * =============================================================================
 */
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
 package manyminds.util.gui;

import java.awt.FlowLayout;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JProgressBar;

public class AppletProgressBar {
	private static JProgressBar progressBar;
	private static JFrame currentFrame;
	private static int stepsPerPhase;
	private static int stepsPerTask;
	private static int currentPhase;
	private static final int maxSteps = 1000;
	
	protected 
	AppletProgressBar() {}
	
	public static void
	startProgressBar(int phases,String action) {
		currentFrame = new JFrame();
		progressBar = new JProgressBar();
		progressBar.setMinimum(1);
		progressBar.setMaximum(maxSteps);
		currentFrame.getContentPane().setLayout(new FlowLayout());
		currentFrame.getContentPane().add(new JLabel(action));
		currentFrame.getContentPane().add(progressBar);
		currentFrame.pack();
		currentFrame.setVisible(true);
		stepsPerPhase = maxSteps/phases;
		currentPhase = -1;
		//System.err.println("created "+phases+" step progress bar");
	}
	
	public static void
	nextPhase(int tasks, String phaseName) {
		if (phaseName != null) {
			progressBar.setString(phaseName);
		}
		stepsPerTask = stepsPerPhase / tasks;
		++currentPhase;
		progressBar.setValue(currentPhase * stepsPerPhase);
		//System.err.println("now on phase "+currentPhase+" which is "+tasks+" long");
	}
	
	public static void
	completedTask() {
		progressBar.setValue(progressBar.getValue() + stepsPerTask);
		//System.err.println("completed task");
	}
	
	public static void
	stopProgressBar() {
//		currentFrame.setVisible(false);
//		currentFrame.dispose();
	}
	
}
