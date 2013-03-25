LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE    := bootmanagerSS
LOCAL_SRC_FILES := bootmanagerSS.c




include $(BUILD_SHARED_LIBRARY)
