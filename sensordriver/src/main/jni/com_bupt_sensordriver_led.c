#include <stdio.h>
#include <stdlib.h>
#include <fcntl.h>
#include <errno.h>
#include <unistd.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <string.h>
#include <stdint.h>
#include <termios.h>
#include <android/log.h>
#include <sys/ioctl.h>
#include "com_bupt_sensordriver_led.h"

#undef  TCSAFLUSH
#define TCSAFLUSH  TCSETSF
#ifndef _TERMIOS_H_
#define _TERMIOS_H_
#endif

static int fd=0;

/*
 * Class:     com_topeet_ledtest_led
 * Method:    Open
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_com_bupt_sensordriver_led_Open
  (JNIEnv *env, jobject obj)
  {
	if(fd<=0)fd = open("/dev/leds", O_RDWR|O_NDELAY|O_NOCTTY);
	if(fd <=0 )__android_log_print(ANDROID_LOG_INFO, "serial", "open /dev/leds Error");
	else __android_log_print(ANDROID_LOG_INFO, "serial", "open /dev/leds Sucess fd=%d",fd);
  }

/*
 * Class:     com_topeet_ledtest_led
 * Method:    Close
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_com_bupt_sensordriver_led_Close
  (JNIEnv *env, jobject obj)
  {
	if(fd > 0)close(fd);
  }

/*
 * Class:     com_topeet_ledtest_led
 * Method:    Ioctl
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_com_bupt_sensordriver_led_Ioctl
  (JNIEnv *env, jobject obj, jint num, jint en)
  {
  	ioctl(fd, en, num);
  }
  


