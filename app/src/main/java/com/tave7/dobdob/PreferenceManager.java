package com.tave7.dobdob;

import android.content.Context;
import android.content.SharedPreferences;

public class PreferenceManager {
    private static final String DEFAULT_VALUE_STRING = "";
    private static final boolean DEFAULT_VALUE_BOOLEAN = false;
    private static final int DEFAULT_VALUE_INT = -1;
    private static final long DEFAULT_VALUE_LONG = -1L;
    private static final float DEFAULT_VALUE_FLOAT = -1F;

    private static SharedPreferences getPreferences(Context context) {
        return context.getSharedPreferences("saveFile", context.MODE_PRIVATE);
    }

    public static void setString(Context context, String key, String value){        //String 값 저장
        SharedPreferences prefs = getPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(key, value);
        editor.apply();
    }

    public static void setBoolean(Context context, String key, boolean value){      //Boolean 값 저장
        SharedPreferences prefs = getPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(key, value);
        editor.apply();
    }

    public static void setInt(Context context, String key, int value){              //Int 값 저장
        SharedPreferences prefs = getPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(key, value);
        editor.apply();
    }

    public static void setLong(Context context, String key, long value){            //Long 값 저장
        SharedPreferences prefs = getPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putLong(key, value);
        editor.apply();
    }

    public static void setFloat(Context context, String key, float value){          //Float 값 저장
        SharedPreferences prefs = getPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putFloat(key, value);
        editor.apply();
    }


    public static String getString(Context context, String key){                    //String 값 불러오기
        SharedPreferences prefs = getPreferences(context);
        String value = prefs.getString(key, DEFAULT_VALUE_STRING);
        return value;
    }

    public static boolean getBoolean(Context context, String key){                  //Boolean 값 불러오기
        SharedPreferences prefs = getPreferences(context);
        boolean value = prefs.getBoolean(key, DEFAULT_VALUE_BOOLEAN);
        return value;
    }

    public static int getInt(Context context, String key){                          //Int 값 불러오기
        SharedPreferences prefs = getPreferences(context);
        int value = prefs.getInt(key, DEFAULT_VALUE_INT);
        return value;
    }

    public static long getLong(Context context, String key){                        //Long 값 불러오기
        SharedPreferences prefs = getPreferences(context);
        long value = prefs.getLong(key, DEFAULT_VALUE_LONG);
        return value;
    }

    public static float getFloat(Context context, String key){                      //Float 값 불러오기
        SharedPreferences prefs = getPreferences(context);
        float value = prefs.getFloat(key, DEFAULT_VALUE_FLOAT);
        return value;
    }

    public static void removeKey(Context context, String key){                      //key 값 삭제
        SharedPreferences prefs = getPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.remove(key);
        editor.commit();
    }

    public static void clear(Context context){                                      //모든 저장 데이터 삭제
        SharedPreferences prefs = getPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.clear();
        editor.commit();
    }
}
