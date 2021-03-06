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

import android.content.Context;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.savor.ads.bean.AdInfo;
import com.savor.ads.bean.AdMasterResult;
import com.savor.ads.bean.AdPayloadBean;
import com.savor.ads.bean.AdsMeiSSPResult;
import com.savor.ads.bean.JDmomediaResult;
import com.savor.ads.bean.NettyBalancingResult;
import com.savor.ads.bean.PrizeInfo;
import com.savor.ads.bean.ServerInfo;
import com.savor.ads.bean.TvProgramGiecResponse;
import com.savor.ads.bean.TvProgramResponse;
import com.savor.ads.bean.UpgradeInfo;
import com.savor.ads.utils.ConstantValues;
import com.savor.ads.utils.DesUtils;
import com.savor.ads.utils.LogUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Response;
import tianshu.ui.api.TsUiApiV20171122;
import tianshu.ui.api.ZmtAPI;

/**
 * API 响应结果解析工厂类，所有的API响应结果解析需要在此完成。
 *
 * @author andrew
 * @date 2011-4-22
 */
public class ApiResponseFactory {
    public final static String TAG = "ApiResponseFactory";

    // 当前服务器时间
    private static String webtime = "";
    public static Object getResponse(Context context, AppApi.Action action,
                                     Response response, String key, String other_param) {

        if (action == AppApi.Action.AD_BAIDU_ADS) {
            // 百度聚屏是特殊的类型，需要使用protobuff解析
            TsUiApiV20171122.TsApiResponse tsApiResponse = null;
            try {
                byte[] content = response.body().bytes();
                tsApiResponse = TsUiApiV20171122.TsApiResponse.parseFrom(content);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return tsApiResponse;
        }else if (action == AppApi.Action.AD_ZMENG_ADS){
            ZmtAPI.ZmAdResponse zmAdResponse = null;
            try {
                byte[] content = response.body().bytes();
                zmAdResponse = ZmtAPI.ZmAdResponse.parseFrom(content);
            }catch (Exception e){
                e.printStackTrace();
            }
            return zmAdResponse;
        }

        //转换器
        String requestMethod = "";
        Object result = null;
        boolean isDes = false;
        Session session = Session.get(context);
        String jsonResult = null;
        try {
            jsonResult = response.body().string();

        } catch (IOException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        } finally {
            response.close();
        }
        if (jsonResult == null) {
            return null;
        }

        String header = response.header("des");
        key = response.header("X-SMALL-TYPE");
        if (header != null && Boolean.valueOf(header)) {
            isDes = true;
        }
        if (isDes) {
            jsonResult = DesUtils.decrypt(jsonResult);
        }

        LogUtils.i("jsonResult:" + jsonResult);
        JSONObject rSet;
        JSONObject info = null;
        JSONArray infoArray = null;
        String infoJson = "";
        ResponseErrorMessage error;
        try {
            rSet = new JSONObject(jsonResult);
            if (rSet.has("code")) {
                int code = rSet.getInt("code");
                if (AppApi.HTTP_RESPONSE_STATE_SUCCESS == code||AppApi.HTTP_RESPONSE_ADS_SUCCESS==code) {
                    try {
                        if (AppApi.HTTP_RESPONSE_STATE_SUCCESS == code){
                            info = rSet.getJSONObject("result");
                        }else if(AppApi.HTTP_RESPONSE_ADS_SUCCESS==code){
                            info = rSet.getJSONObject("data");
                        }
                        infoJson = info.toString();
                    } catch (JSONException ex) {
                        try {
                            infoArray = rSet.getJSONArray("result");
                            infoJson = infoArray.toString();
                        } catch (JSONException e) {
                            try {
                                infoJson = rSet.getString("result");
                            } catch (Exception e2) {
                                infoJson = rSet.toString();
                            }

                        }
                    }

                    /**缓存返回数据包*/
//					if(isCache){
//						String serverKey = response.getFirstHeader("key").getValue();
//						String webtimeKey=response.getFirstHeader("webtime").getValue();
//						HttpCacheManager.getInstance(context).saveCacheData(key, serverKey,webtimeKey, infoJson);
//					}
                } else {
                    try {
                        if (rSet.has("msg")) {
                            String msg = rSet.getString("msg");
                            error = new ResponseErrorMessage();
                            error.setCode(code);
                            error.setMessage(msg);
                            error.setJson(jsonResult);
                            return error;
                        }
                    } catch (JSONException ex) {
                        try {
                            String msg = rSet.getString("msg");
                            error = new ResponseErrorMessage();
                            error.setCode(code);
                            error.setMessage(msg);
                            error.setJson(jsonResult);
                            return error;
                        } catch (JSONException e) {
                            try {
                                infoJson = rSet.getString("result");
                            } catch (Exception e2) {
                                LogUtils.d(e.toString());
                            }

                        }
                    }
                }
            }
            result = parseResponse(context,action, infoJson, rSet,key,other_param);
        } catch (Exception e) {
            LogUtils.d(requestMethod + " has other unknown Exception", e);
            e.printStackTrace();
        }

        return result;
    }

    public static Object parseResponse(Context context,AppApi.Action action, String info, JSONObject ret,String key,String other_param) {
        Object result = null;
        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();
		LogUtils.i("info:-->" + info);
        if (info == null) {
            return result;
        }
        switch (action) {
            case SP_GET_UPGRADE_INFO_JSON:
                result = gson.fromJson(info, new TypeToken<UpgradeInfo>() {
                }.getType());
                if (result instanceof UpgradeInfo){
                    UpgradeInfo upgradeInfo = (UpgradeInfo)result;
                    if (ConstantValues.VIRTUAL.equals(key)){
                        upgradeInfo.setVirtual(true);
                    }else{
                        upgradeInfo.setVirtual(false);
                    }
                    result =upgradeInfo;
                }
                break;
            case SP_POST_UPLOAD_PROGRAM_JSON:
                result = info;
                break;
            case SP_POST_UPLOAD_PROGRAM_GIEC_JSON:
                result = info;
                break;
            case CP_GET_SP_IP_JSON:
                result = gson.fromJson(info, new TypeToken<ServerInfo>() {
                }.getType());
                break;
            case SP_GET_TV_MATCH_DATA_FROM_JSON:
                result = gson.fromJson(info, new TypeToken<TvProgramResponse>() {
                }.getType());
                break;
            case SP_GET_TV_MATCH_DATA_FROM_GIEC_JSON:
                result = gson.fromJson(info, new TypeToken<TvProgramGiecResponse>() {
                }.getType());
                break;
            case CP_GET_PRIZE_JSON:
                result = gson.fromJson(info, new TypeToken<PrizeInfo>() {
                }.getType());
                break;
            case PH_NOTIFY_STOP_JSON:
                result = info;
                break;
            case CP_GET_HEARTBEAT_PLAIN:
                result = info;
                break;
            case CP_POST_DEVICE_TOKEN_JSON:
                result = info;
                break;
            case SP_POST_NETSTAT_JSON:
                result = info;
                break;
            case CP_POST_PLAY_LIST_JSON:
                result = info;
                break;
            case CP_POST_DOWNLOAD_LIST_JSON:
                result = info;
                break;
            case CP_POST_SDCARD_STATE_JSON:
                result = info;
                break;
            case CP_POST_FORSCREEN_GETCONFIG_JSON:
                result = info;

                break;
            case CP_GET_UPLOAD_LOG_FILE_JSON:
                try {
                    JSONObject jsonObject = ret;
                    if (jsonObject.has("result")){
                        jsonObject = jsonObject.getJSONObject("result");
                        if (jsonObject.has("log_type")){
                            result = jsonObject.getInt("log_type");
                        }
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
                break;
            case AD_MEI_VIDEO_ADS_JSON:
            case AD_MEI_IMAGE_ADS_JSON:
                try{
                    List<AdsMeiSSPResult> adsMeiSSPResults = new ArrayList<>();
                    JSONArray jsonArray= ret.getJSONArray("ad");
                    for (int i=0;i<jsonArray.length();i++){
                        JSONObject jsonObject = jsonArray.getJSONObject(i);
                        String admnative = jsonObject.getString("admnative");
                        AdsMeiSSPResult adsMeiSSPResult = gson.fromJson(admnative, new TypeToken<AdsMeiSSPResult>() {
                        }.getType());
                        adsMeiSSPResults.add(adsMeiSSPResult);
                    }
                    result = adsMeiSSPResults;
                    LogUtils.d("213");
                }catch (Exception e){
                    e.printStackTrace();
                }
                break;
            case CP_GET_NETTY_BALANCING_FORM:
                try {
                    if (!TextUtils.isEmpty(other_param)&&other_param.equals(ret.getString("req_id"))){
                        result = gson.fromJson(ret.toString(), new TypeToken<NettyBalancingResult>() {}.getType());

                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                break;
            case CP_GET_MINIPROGRAM_PROJECTION_RESOURCE_JSON:
                result = info;
                break;
            case AD_POST_OOHLINK_ADS_JSON:
                result = gson.fromJson(info, new TypeToken<AdInfo>() {
                }.getType());
                break;
            case AD_POST_OOHLINK_REPORT_LOG_JSON:
                break;
            case AD_POST_JDMOMEDIA_ADS_PLAIN:
                result = gson.fromJson(ret.toString(), new TypeToken<JDmomediaResult>() {
                }.getType());
                break;
            case AD_POST_JDMOMEDIA_HEARTBEAT_PLAIN:
                try {
                    JSONObject jsonObject = ret;
                    if (jsonObject.getInt("code")==AppApi.HTTP_RESPONSE_ADS_SUCCESS){
                        Session.get(context).setJDmomediaReport(true);
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
                break;
            case AD_POST_YISHOU_JSON:
                try{
                    JSONObject json = null;
                    if (ret.getInt("code")>=0){
                        JSONArray jsonArray = ret.getJSONArray("payload");
                        if (jsonArray!=null&&jsonArray.length()>0)
                            json = jsonArray.getJSONObject(0);
                        if (json!=null){
                            AdPayloadBean bean = new AdPayloadBean();
                            bean.setShow_time(json.getInt("show-time"));
                            bean.setSlot_id(json.getString("slot-id"));
                            bean.setWidth(json.getInt("width"));
                            bean.setHeight(json.getInt("height"));
                            bean.setFile_size(json.getString("file-size"));
                            bean.setSign(json.getString("sign"));
                            bean.setTrack_url(json.getString("track-url"));
                            bean.setExpire_time(json.getString("expire-time"));
                            bean.setType(json.getString("type"));
                            bean.setUrl(json.getString("url"));
                            result = bean;
                        }
                    }else{
                        ResponseErrorMessage error = new ResponseErrorMessage();
                        error.setCode(ret.getInt("code"));
                        result = error;
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
                break;
            case CP_GET_BOX_TPMEDIAS_JSON:
                try {
                    JSONObject jsonObject = ret.getJSONObject("result");
                    String tpmedia_id = jsonObject.getString("tpmedia_id");
                    Session.get(context).setTpMedias(tpmedia_id);
                }catch (Exception e){
                    e.printStackTrace();
                }
                break;
            case CP_POST_SIMPLE_MINIPROGRAM_FORSCREEN_LOG_JSON:
                result = other_param;
                break;
            case CP_POST_UPDATE_SIMPLE_FORSCREEN_LOG_JSON:
                result = other_param;
                break;
            default:
                break;
        }
        return result;
    }

}