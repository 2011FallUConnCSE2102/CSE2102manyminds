all: libWebKitComponent.jnilib

libWebKitComponent.jnilib: WebKitComponent.o
	cc -o libWebKitComponent.jnilib -L/usr/lib/gcc/darwin/3.1 -framework Cocoa -framework JavaVM -framework WebKit -lstdc++ -arch ppc -bundle WebKitComponent.o

WebKitComponent.o: WebKitComponent.m WebKitComponent.h
	cc -c -I/System/Library/Frameworks/JavaVM.framework/Headers -arch ppc -fno-common -fpascal-strings -Os -Wmost -Wno-four-char-constants -g -x objective-c++ WebKitComponent.m