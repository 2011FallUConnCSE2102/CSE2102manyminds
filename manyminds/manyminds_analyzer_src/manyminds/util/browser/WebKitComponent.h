//
//  WebKitComponent.h
//  InquiryIsland
//
//  Created by Eric Eslinger on Mon Jul 14 2003.
//  Copyright (c) 2003 __MyCompanyName__. All rights reserved.
//

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