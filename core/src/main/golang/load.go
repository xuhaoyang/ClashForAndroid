package main

//#include <jni.h>
import "C"
import "github.com/kr328/clash-for-android/log"
import "github.com/kr328/clash-for-android/profile"

//export Java_com_github_kr328_clash_core_Clash_loadDefault
func Java_com_github_kr328_clash_core_Clash_loadDefault(env *C.JNIEnv, class C.jclass) {
	log.Info("Clash load default")

	profile.LoadDefault()
}
