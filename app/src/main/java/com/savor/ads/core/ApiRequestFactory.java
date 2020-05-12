package com.savor.ads.core;
/*
 * Copyright (C) 2010 mAPPn.Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


import androidx.annotation.NonNull;
import android.text.TextUtils;

import com.savor.ads.utils.AppUtils;
import com.savor.ads.utils.ConstantValues;
import com.savor.ads.utils.LogUtils;
import com.savor.ads.utils.StringUtils;

import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;


/**
 * 这个类是获取API请求内容的工厂方法
 */
public class ApiRequestFactory {
    /**
     * 获取API HTTP 请求内容
     *
     * @param action 请求的API Code
     * @param params 请求参数
     * @throws UnsupportedEncodingException 假如不支持UTF8编码方式会抛出此异常
     */
    public static Object getRequestEntity(AppApi.Action action, Object params,
                                          Session appSession) throws UnsupportedEncodingException, JSONException {
        String nameSpace = action.name();
        if (nameSpace.contains("XML")) {
            /**暂不实现*/
            return null;

        } else if (nameSpace.contains("JSON")) {
            return getJsonRequest(params, appSession);
        } else if (nameSpace.contains("FORM")) {
            return getFormRequestWithoutSign(action, params, appSession);
        } /*else if (nameSpace.contains("NOSIGN")) {
            return getFormRequestWithoutSign(action, params, appSession);
        } */else {
            // 不需要请求内容
            return null;
        }
    }

//    private static StringEntity getFormRequest(AppApi.Action action, Object params,
//                                               Session appSession) throws UnsupportedEncodingException {
//        if (params == null) {
//            return null;
//        }
//        HashMap<String, Object> requestParams;
//        if (params instanceof HashMap) {
//            requestParams = (HashMap<String, Object>) params;
//        } else {
//            return null;
//        }
//        final Iterator<String> keySet = requestParams.keySet().iterator();
//        ArrayList<NameValuePair> pm = new ArrayList<NameValuePair>();
//        try {
//            while (keySet.hasNext()) {
//                final String key = keySet.next();
//                pm.add(new BasicNameValuePair(key, (String) requestParams
//                        .get(key)));
//            }
//
//            /** 应用传递的参数是否需要签名，签名 开始 */
//            String sign = "";
//            String token = "";
//            String username = "";
//            String pwd = "";
//            long timestamp = System.currentTimeMillis() / 1000;
//            try {
//                /**token值为：md5(md5(username|appKey|pwd))*/
////				token = DigestUtils.md5Hex(DigestUtils.md5Hex(username+"|"+AppApi.API_GROUPLEADER_KEY+"|"+pwd));
//                token = appSession.getToken();
//                /** sign：由公私钥和params和time值做的md5值 */
//                String paramJson = ParamsUtils.getJsonParamsString(requestParams);
//                sign = Validator.getSignStr(requestParams, paramJson, timestamp + "");
//            } catch (Exception e) {
//                LogUtils.d("AppService->post():", e);
//            }
//            if (!requestParams.containsKey("sign")) {
//                pm.add(new BasicNameValuePair("sign", sign));
//            }
//            if (!requestParams.containsKey("time")) {
//                pm.add(new BasicNameValuePair("time", timestamp + ""));
//            }
//            if (!requestParams.containsKey("token")) {
//                pm.add(new BasicNameValuePair("token", token));
//            }
//            /** lashou，签名 结束 */
//
//        } catch (Exception e) {
//            e.printStackTrace();
//            return null;
//        }
//        return new UrlEncodedFormEntity(pm, HTTP.UTF_8);
//    }
//
    public static HashMap<String, Object> getFormRequestWithoutSign(AppApi.Action action, Object params,
                                                         Session appSession) throws UnsupportedEncodingException {
        if (params == null) {
            return null;
        }
        HashMap<String, Object> requestParams;
        if (params instanceof HashMap) {
            requestParams = (HashMap<String, Object>) params;
        } else {
            return null;
        }
        final Iterator<String> keySet = requestParams.keySet().iterator();
//        ArrayList<NameValuePair> pm = new ArrayList<>();
        JSONObject jsonObject = new JSONObject();
        try {
            while (keySet.hasNext()) {
                final String key = keySet.next();
//                pm.add(new BasicNameValuePair(key, (String) requestParams
//                        .get(key)));
                jsonObject.put(key,requestParams.get(key));
            }

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return requestParams;
//        return new UrlEncodedFormEntity(pm, HTTP.UTF_8);
    }

    private static JSONObject getJsonRequest(Object params, Session appSession) throws JSONException {
        if (params == null) {
            return new JSONObject();
        }

        HashMap<String, Object> requestParams;
        if (params instanceof HashMap) {
            requestParams = (HashMap<String, Object>) params;
        } else {
            return new JSONObject();
        }

        // add parameter node
        final Iterator<String> keySet = requestParams.keySet().iterator();
        JSONObject jsonParams = new JSONObject();

        try {
            while (keySet.hasNext()) {
                final String key = keySet.next();
                Object val = requestParams.get(key);
                if (val == null) {
                    val = "";
                }
                if (val instanceof String || val instanceof Number || val == null) {
                    jsonParams.accumulate(key, val);
                } else if (val instanceof List<?>) {
                    jsonParams.accumulate(key, getJSONArray((List<?>) val));
                } else {
                    jsonParams.accumulate(key, getJSONObject(val).toString());
                }
            }
            LogUtils.i("请求数据包参数:" + jsonParams.toString());
//            return new StringEntity(DesUtils.encrypt(jsonParams.toString()), HTTP.UTF_8);
            return jsonParams;

        } catch (Exception e) {
            e.printStackTrace();
            return new JSONObject();
        }
    }

    private static JSONArray getJSONArray(List<?> list) {
        JSONArray jArray = new JSONArray();
        for (int i = 0; i < list.size(); i++) {
            Object obj = list.get(i);
            if (obj instanceof String || obj instanceof Number || obj == null) {
                jArray.put(list.get(i));
            } else {
                jArray.put(getJSONObject(obj));
            }
        }
        return jArray;
    }

    @NonNull
    private static JSONObject getJSONObject(Object object) {
        Field[] fields = object.getClass().getDeclaredFields();
        JSONObject jObject = new JSONObject();
        for (Field field : fields) {
            try {
                field.setAccessible(true);
                Object fieldValue = field.get(object);
                if (fieldValue instanceof String || fieldValue instanceof Number || fieldValue == null) {
                    jObject.put(field.getName(), fieldValue);
                } else if(fieldValue instanceof List) {
                    jObject.put(field.getName(), getJSONArray((List<?>) fieldValue));
                } else {
                    jObject.put(field.getName(), getJSONObject(fieldValue));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return jObject;
    }

    public static String getUrlRequest(String requestUrl, AppApi.Action action, Object parameter, Session appSession) {

        if (parameter instanceof Map && ((Map) parameter).size() > 0) {
            requestUrl = requestUrl+"?";
            Map<String, Object> map = (Map<String, Object>) parameter;
            Set<Map.Entry<String, Object>> params = map.entrySet();
            for (Map.Entry<String, Object> param : params) {
                String key = param.getKey();
                Object val = param.getValue();
                requestUrl = requestUrl + key + "=" + val+"&";
            }
        }

        if (requestUrl.endsWith("&")) {
            requestUrl = requestUrl.substring(0, requestUrl.length() - 1);
        }

        return requestUrl;

    }

    /**
     * 易售广告平台获取sign
     * @param map
     * @param timestamp
     * @return
     */
    public static String getYishouSign(Map<String, String> map,long timestamp) {

        final Map<String, Object> parameterMap = new TreeMap<>();

        for (Map.Entry<String, String> entity :map.entrySet())
        { parameterMap.put(entity.getKey(),entity.getValue());
        }

        parameterMap.put("appid", ConstantValues.YISHOU_APPPID);
        parameterMap.put("appkey", ConstantValues.YISHOU_APPPKEY);
        parameterMap.put("sequence", timestamp);
        parameterMap.put("timestamp", timestamp);
        parameterMap.put("version", ConstantValues.YISHOU_API);
        StringBuilder stb = new StringBuilder();
        for (Map.Entry<String, Object> entity : parameterMap.entrySet()){
            stb.append(entity.getKey()).append("=").append(entity.getValue()).append("&");
        }

        String str = stb.toString(); System.out.println(str);

        return AppUtils.getMD5(str.substring(0, str.length() - 1));
    }

    /**
     * 获取sign值
     *
     * @param url
     * @return
     */
    public static String getSignParam(String url, Session appSession) {
        long timestamp = System.currentTimeMillis() / 1000;
        String sign = md5(timestamp + "");
        String token = appSession.getToken();
        token = TextUtils.isEmpty(token) ? "" : token;
        url = url + (TextUtils.isEmpty(token) ? "" : ("/token/" + token)) + "/time/" + timestamp + "/sign/" + sign + "/version/" + appSession.getVersionCode();

        return url;
    }

    public static String md5(String input) {
        try {
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            md5.update(input.getBytes());
            byte[] result = md5.digest();//加密
            return StringUtils.toHexString(result, false);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }
}
