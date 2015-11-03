package com.catpunch.catpunch.util;

import android.util.Log;

import com.catpunch.catpunch.BuildConfig;

public class LogUtils {
    private static final String APP_TAG = "Catpunch";

    private static final boolean DEBUG_FLAG = true;
    private static final boolean DEBUG_FLAG_CRITICAL = BuildConfig.DEBUG && true; // To double confirm

    public static String d(String tag, Object... msgs) {
        String generatedLog = null;
        if (DEBUG_FLAG) {
            StringBuilder builder = new StringBuilder(getCallerTag(tag));
            for (Object msg : msgs) {
                builder.append(msg);
            }
            generatedLog = builder.toString();
            Log.d(APP_TAG, generatedLog);
        }
        return generatedLog;
    }

    public static String w(String tag, Object... msgs) {
        String generatedLog = null;
        if (DEBUG_FLAG) {
            StringBuilder builder = new StringBuilder(getCallerTag(tag));
            for (Object msg : msgs) {
                builder.append(msg);
            }
            generatedLog = builder.toString();
            Log.w(APP_TAG, generatedLog);
        }
        return generatedLog;
    }

    public static void e(String tag, Object... msgs) {
        if (DEBUG_FLAG) {
            StringBuilder builder = new StringBuilder(getCallerTag(tag));
            for (Object msg : msgs) {
                builder.append(msg);
            }
            Log.e(APP_TAG, builder.toString());
        }
    }

    public static void e(String tag, String msg, Throwable throwable) {
        if (DEBUG_FLAG) {
            StringBuilder builder = new StringBuilder(getCallerTag(tag));
            builder.append(msg);
            Log.e(APP_TAG, builder.toString(), throwable);
        }
    }

    public static String critical(String tag, Object... msgs) {
        String generatedLog = null;
        if (DEBUG_FLAG_CRITICAL) {
            StringBuilder builder = new StringBuilder(getCallerTag(tag));
            for (Object msg : msgs) {
                builder.append(msg);
            }
            generatedLog = builder.toString();
            Log.d(APP_TAG, generatedLog);
        }
        return generatedLog;
    }

    public static boolean logCritical() {
        return DEBUG_FLAG_CRITICAL;
    }

    private static String getCallerTag(String tag) {
        String callerTag = "[" + tag + "] ";
        return callerTag;
    }
}
