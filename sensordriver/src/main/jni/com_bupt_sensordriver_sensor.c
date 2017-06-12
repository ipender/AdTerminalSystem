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
#include "com_bupt_sensordriver_sensor.h"

#undef  TCSAFLUSH
#define TCSAFLUSH  TCSETSF
#ifndef _TERMIOS_H_
#define _TERMIOS_H_
#endif

static int fd=0;
static int fd2=0;
/*
 * Class:     com_topeet_myproject_sensor
 * Method:    Open
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_com_bupt_sensordriver_sensor_Open
  (JNIEnv *env, jobject obj)
  {
	if(fd<=0){
		fd = open("/dev/sensor_irq", O_RDWR|O_NDELAY|O_NOCTTY);
	}
	/*
	if(fd2<=0){
		fd2 = open("/dev/leds", O_RDWR|O_NDELAY|O_NOCTTY);
	}*/
	if(fd <=0 )
		__android_log_print(ANDROID_LOG_INFO, "sensor", "open /dev/sensor_irq Error fd=%d",fd);
	else
		__android_log_print(ANDROID_LOG_INFO, "sensor", "open /dev/sensor_irq Sucess fd=%d",fd);
	/*
	if(fd2 <=0 )
			__android_log_print(ANDROID_LOG_INFO, "sensor", "open /dev/leds Error fd=%d",fd2);
		else
			__android_log_print(ANDROID_LOG_INFO, "sensor", "open /dev/leds Sucess fd2=%d",fd2);
	*/
  }

/*
 * Class:     com_topeet_myproject_sensor
 * Method:    Close
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_com_bupt_sensordriver_sensor_Close
  (JNIEnv *env, jobject obj)
  {
	if(fd > 0)close(fd);
	if(fd2 > 0)close(fd2);
  }

/*
 * Class:     com_topeet_myproject_sensor
 * Method:    Ioctl
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_com_bupt_sensordriver_sensor_Ioctl
  (JNIEnv *env, jobject obj, jint num, jint en)
  {
  	//ioctl(fd, en, num);
	//if(fd>0)
	ioctl(fd2, en, num);
  }
/*
 * Class:     com_topeet_myproject_sensor
 * Method:    Read
 * Signature: ()I
 */

JNIEXPORT jintArray JNICALL Java_com_bupt_sensordriver_sensor_Read
//JNIEXPORT jcharArray JNICALL Java_com_bupt_sensordriver_sensor_Read
  (JNIEnv *env, jobject obj)
  {
	//unsigned char buffer[512];
	char buffer[10]={0};
	int BufToJava[10]={0};
	int len = 0;
	int i = 0;

	//printf("sensor_Read test!!!\n"); //�˴���ӡ��Ч
	memset(buffer,0,sizeof(buffer));
	memset(BufToJava,0,sizeof(BufToJava));
	len = read(fd,buffer,6);
	  __android_log_print(ANDROID_LOG_INFO, "sensor", "len = %d",len);
	//len = strlen(buffer);
	  len = 6;
	for(i=0;i<len;i++){
		__android_log_print(ANDROID_LOG_INFO, "sensor", "buffer[%d] = %c",i,buffer[i]);
		BufToJava[i] = buffer[i];
		__android_log_print(ANDROID_LOG_INFO, "sensor", "BufToJava[%d] = %d",i,BufToJava[i]);
	}

	jintArray array = (*env)->NewIntArray(env,len);;
	(*env)->SetIntArrayRegion(env,array,0,len,BufToJava);
	  //char 应用层读完输出乱码
	  //jcharArray array = (*env)->NewCharArray(env,len);;
	  //(*env)->SetCharArrayRegion(env,array,0,len,buffer);

	return array;
  }


