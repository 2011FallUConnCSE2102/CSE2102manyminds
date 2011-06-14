//
//  WebKitComponent.m
//  InquiryIsland
//
//  Created by Eric Eslinger on Mon Jul 14 2003.
//  Copyright (c) 2003 __MyCompanyName__. All rights reserved.
//

#import "WebKitComponent.h"

// Implementation of our custom NSView
@implementation WebKitComponent

- (id) initWithFrame: (NSRect) frame
{
	javaOwner = NULL;
	self = [super initWithFrame:frame frameName:nil groupName:nil];
    [self setResourceLoadDelegate:self];

	return self;
	
}

-(JavaVM *)JVM
{
	return jvm;
}
-(void)setJVM:(JavaVM *)vm
{
	jvm = vm;
}


-(void)setJavaOwner:(jobject)jOwner
{
	javaOwner = jOwner;
}


-(jobject)javaOwner
{
	return javaOwner;
}

-(void)errorOccurred : (NSString *)desc  host : (NSString *)host
{
	if(javaOwner == NULL || [self JVM] == NULL) return;
	bool 	wasAttached = false;
	JNIEnv *env = GetJEnv([self JVM],wasAttached);
	if(env == NULL) return;
	jclass clazz = env->GetObjectClass(javaOwner);
	if(clazz != NULL){
	    jmethodID mID = env->GetMethodID(clazz,"errorOccurred",JRISigMethod(JRISigClass("java/lang/String") JRISigClass("java/lang/String")) JRISigVoid); 
	    if(mID != NULL){
	        jstring jstr1 = env->NewStringUTF([desc cString]);
	        jstring jstr2 = env->NewStringUTF([host cString]);
	        env->CallVoidMethod(javaOwner,mID,jstr1,jstr2);
	    }else{
            fprintf(stderr,"didFailLoadingWithError mID == null\n");
	    }
	}
	if(wasAttached) [self JVM]->DetachCurrentThread();
}


// Messaging api which is sent async from CocoaComponent's sendMessage api
-(void)awtMessage:(jint)messageID message:(jobject)message env:(JNIEnv *)env
{
    switch (messageID) {
    	case WebKitComponent_storeJavaObject:
			[self setJavaOwner:message];
    		break;
    	case WebKitComponent_loadURL:
    	    if(message != nil && env != nil) {
                jboolean isCopy;
                const char *urlString = env->GetStringUTFChars((jstring)message,&isCopy);
                NSURL *url = [NSURL URLWithString : [NSString stringWithCString : urlString]];
                NSURLRequest *request = [NSURLRequest requestWithURL : url];
                WebFrame *webFrame = [self mainFrame];
                [webFrame loadRequest:request];
                //fprintf(stderr,"NSWebKitView_loadURL\n");
                if(isCopy == JNI_TRUE) {
                    env->ReleaseStringUTFChars((jstring)message,urlString);
                }
            }
            break;
    	case WebKitComponent_goBack:
    	    if(message != nil && env != nil) {
                [self goBack];
            }
            break;
    	case WebKitComponent_goForward:
    	    if(message != nil && env != nil) {
                [self goForward];
            }
            break;
    	case WebKitComponent_reloadPage:
    	    if(message != nil && env != nil) {
                [[self mainFrame] reload];
            }
            break;
        default:
            fprintf(stderr,"MyNSWebKitView Error : Unknown Message Received (%d)\n", (int)messageID);
    }
}

- (void) keyDown: (NSEvent *) theEvent
{
	[super keyDown:theEvent];
	printf("keyDown\n");
}

// WebResourceLoadDelegate Methods

-(void)webView:(WebView *)sender resource:(id)identifier didFailLoadingWithError:(NSError *)error fromDataSource:(WebDataSource *)dataSource
{
    // Increment the failed count and update the status message
    NSDictionary *userInfo = [error userInfo];
    if(userInfo != nil){
        id errorURLString = [userInfo objectForKey : NSErrorFailingURLStringKey];
        id localDesc = [userInfo objectForKey : NSLocalizedDescriptionKey];
        
        if([localDesc isKindOfClass : [NSString class]]){
            fprintf(stderr,"localDesc %s\n",[(NSString *)localDesc cString]);
        }
        
        if([errorURLString isKindOfClass : [NSString class]]){
            fprintf(stderr,"errorURLString %s\n",[(NSString *)errorURLString cString]);
        }
    
        //NSString *errorDesc = [error localizedDescription];
        [self errorOccurred : (NSString *)localDesc host : (NSString *)errorURLString];
    }

}


@end


JNIEnv *GetJEnv(JavaVM *vm,bool &wasAttached){
	JNIEnv *env = NULL;
	if(vm == NULL) return env;
	wasAttached = false;

	jint errGetEnv = vm->GetEnv((void **)&env, JNI_VERSION_1_4);
	if(errGetEnv == JNI_ERR) return NULL;
	if(errGetEnv == JNI_EDETACHED){
		vm->AttachCurrentThread((void **)&env,(void *)NULL);
		if(env != NULL) wasAttached = true;
	}else if(errGetEnv != JNI_OK) return NULL;
	return env;
}

JavaVM *JVM = NULL;

JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM *vm, void *reserved)
{
    JVM = vm;
    return JNI_VERSION_1_4;
}

JNIEXPORT void JNICALL JNI_OnUnload(JavaVM *vm, void *reserved)
{
    printf("JNI_OnUnload\n");
}

JNIEXPORT jint JNICALL Java_manyminds_util_browser_WebKitComponent_createNSView
  (JNIEnv *env, jobject nsWebKitViewView)
{
    WebKitComponent *webKitView = nil;
    NS_DURING;
    // Here we create our custom NSView
    webKitView = [[WebKitComponent alloc] initWithFrame : NSMakeRect(0,0,0,0)];
    [webKitView setJVM : JVM];
    NS_HANDLER;
    fprintf(stderr,"ERROR : Failed to create NSOpenGLView\n");
    NS_VALUERETURN(0, jlong);
    NS_ENDHANDLER;

    return (jint) webKitView;
}

