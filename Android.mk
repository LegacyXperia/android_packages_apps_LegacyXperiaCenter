# Copyright (C) 2013 Slimroms http://www.slimroms.net
# Copyright (C) 2014 LegacyXperia Project
#
# This program is free software: you can redistribute it and/or modify
# it under the terms of the GNU General Public License version 2
# as published by the Free Software Foundation.
#
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
# GNU General Public License for more details.
#
# You should have received a copy of the GNU General Public License
# along with this program. If not, see <http://www.gnu.org/licenses/>.

LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)

LOCAL_MODULE_TAGS := optional

LOCAL_SRC_FILES := $(call all-java-files-under, src)

LOCAL_PACKAGE_NAME := LegacyXperiaCenter

LOCAL_PROGUARD_FLAG_FILES := proguard.flags

LOCAL_STATIC_JAVA_LIBRARIES := android-support-v4

include $(BUILD_PACKAGE)
