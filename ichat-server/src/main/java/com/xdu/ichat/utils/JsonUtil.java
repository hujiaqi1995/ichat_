package com.xdu.ichat.utils;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.springframework.util.StringUtils;

/**
 * @author hujiaqi
 * @create 2020/6/28
 */
public class JsonUtil {

    public static JsonObject parseString(String jsonString) {
        if (StringUtils.isEmpty(jsonString)) {
            return null;
        }
        JsonElement jsonElement = JsonParser.parseString(jsonString);
        if (jsonElement == null || !jsonElement.isJsonObject()) {
            return null;
        }
        return jsonElement.getAsJsonObject();
    }

    public static String getString(JsonObject jsonObject, String key) {
        if (jsonObject == null || !jsonObject.has(key)) {
            return null;
        }
        return jsonObject.get(key).getAsString();
    }

    public static long getLong(JsonObject jsonObject, String key) {
        if (jsonObject == null || !jsonObject.has(key)) {
            return 0;
        }
        return jsonObject.get(key).getAsLong();
    }

    public static int getInt(JsonObject jsonObject, String key) {
        if (jsonObject == null || !jsonObject.has(key)) {
            return 0;
        }
        return jsonObject.get(key).getAsInt();
    }

    public static JsonObject getJsonObject(JsonObject jsonObject, String key) {
        if (jsonObject == null || !jsonObject.has(key)) {
            return null;
        }
        return jsonObject.get(key).getAsJsonObject();
    }
}
