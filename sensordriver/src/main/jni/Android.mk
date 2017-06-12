LOCAL_PATH := $(call my-dir)
include $(CLEAR_VARS)

LOCAL_MODULE    := sensor-driver
LOCAL_SRC_FILES := com_bupt_sensordriver_led.c \
                   com_bupt_sensordriver_adc.c \
                   com_bupt_sensordriver_sensor.c \
                   com_bupt_sensordriver_rfid.c
LOCAL_LDLIBS += -llog
LOCAL_LDLIBS +=-lm
LOCAL_JNI_SHARED_LIBRARIES := libtest
include $(BUILD_SHARED_LIBRARY)