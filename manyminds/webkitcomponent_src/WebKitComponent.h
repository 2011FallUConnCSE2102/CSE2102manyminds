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

/*
 * The files in this package were adapted from Dmitry Markman's WebKitExample
 */

#ifndef _WEBKITCOCOACOMPONENT_H
#define _WEBKITCOCOACOMPONENT_H

#import <JavaVM/jni.h>
#import <AppKit/AppKit.h>
#import <WebKit/WebView.h>
#import <WebKit/WebFrame.h>
#include "JNISig.h"
#include "manyminds_util_browser_WebKitComponent.h"

enum{
	WebKitComponent_storeJavaObject = 1,
	WebKitComponent_loadURL = 2,
        WebKitComponent_goBack = 3,
        WebKitComponent_goForward = 4,
        WebKitComponent_reloadPage = 5,
        WebKitComponent_repaint = 6,
};

// Interface to our custom NSView
@interface WebKitComponent : WebView  {
	JavaVM 			*jvm;
	jobject 		javaOwner;
}
- (id) initWithFrame: (NSRect) frame;
-(void)awtMessage:(jint)messageID message:(jobject)message env:(JNIEnv *)env;
-(void)errorOccurred : (NSString *)desc  host : (NSString *)host;
-(jobject)javaOwner;
-(void)setJavaOwner:(jobject)jOwner;

-(JavaVM *)JVM;
-(void)setJVM:(JavaVM *)vm;

@end

JNIEnv *GetJEnv(JavaVM *vm,bool &wasAttached);

#endif