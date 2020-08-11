package com.savor.ads.log;

import android.content.Context;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;

import com.savor.ads.core.ApiRequestListener;
import com.savor.ads.core.AppApi;
import com.savor.ads.core.Session;
import com.savor.ads.service.HandleMediaDataService;
import com.savor.ads.service.HeartbeatService;
import com.savor.ads.utils.AppUtils;
import com.savor.ads.utils.ConstantValues;
import com.savor.ads.utils.LogFileUtil;
import com.savor.ads.utils.LogUtils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;
import java.util.logging.Handler;

/**
 * 生产日志任务
 */
public class LogProduceService {
	private FileWriter mLogWriter = null;
	private FileWriter mQRCodeLogWriter = null;
	private Context mContext=null;
	private String logTime=null;
	private LogReportUtil logReportUtil = null;
	private Session session;
	private File file;
	private long tempTime;
	//单机版
	private String standalone="standalone";

	private boolean isNewRequestQRcode = false;
	private boolean isNewRequestQRcodeBig = false;
	private boolean isNewRequestQRcodeNew = false;
	private boolean isNewRequestQRcodeCall = false;

	private boolean isNewRequestSQRcode = false;
	private boolean isNewRequestSQRcodeBig = false;
	private boolean isNewRequestSQRcodeNew = false;
	private boolean isNewRequestSQRcodeCall = false;
	private boolean isNewRequestQRcodeExtension = false;
	public LogProduceService (Context context){
		this.mContext = context;
		session = Session.get(context);
		logReportUtil = LogReportUtil.get(mContext);
	}
	android.os.Handler handler=new android.os.Handler(Looper.getMainLooper());
	/**
	 * 1.当卡被拔出的时候停止生产日志
	 * 2、当应用停掉的时候停止生产日志
	 */

	public void run() {
		new Thread() {
			@Override
			public void run() {
				while (true) {
					while (TextUtils.isEmpty(AppUtils.getMainMediaPath())) {
						try {
							Thread.sleep(1000);
						} catch (Exception e) {
							e.printStackTrace();
						}
					}

					// 生成日志文件
					createFile();
					Random random =new Random();
					int index = random.nextInt(200);
					handler.postDelayed(()->downloadMiniProgramIcon(),index*1000);

					while (true) {

                        if (TextUtils.isEmpty(logTime) || !logTime.equals(AppUtils.getCurTime("yyyyMMddHH"))){
                            break;
                        }
                        if (mLogWriter != null) {
                            if (LogReportUtil.getLogNum() > 0) {
                                try {
                                    LogReportParam mparam = logReportUtil.take();
                                    if (mparam != null) {
                                        String log = makeLog(mparam);
                                        LogUtils.i("log:" + log);
                                        try {
                                            mLogWriter.write(log);
                                            mLogWriter.flush();
                                        } catch (Exception e) {
                                            e.printStackTrace();
											if (AppUtils.isMstar()){
												AppApi.reportSDCardState(mContext, apiRequestListener, 1);
											}
                                        }
                                    }
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
                        } else {
							LogFileUtil.write("Log FileWriter is null, will recreate file.");
							createFile();
						}

						try {
							Thread.sleep(5*1000);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}

					closeWriter();
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		}.start();
	}

	/**
	 * 小程序码下载到本地
	 */
	private void downloadMiniProgramIcon(){
	    //20191217:从此版本开始，清除小程序码，以后统一全部使用二维码
		AppUtils.deleteCacheData();

        String box_mac = session.getEthernetMac();

		downloadMiniProgramQRcodeSmall(box_mac);
		isNewRequestQRcode = true;
		//-----------------------------------
		downloadMiniProgramQRcodeBig(box_mac);
		isNewRequestQRcodeBig = true;
		//-----------------------------------
		downloadMiniProgramQRcodeNew(box_mac);
		isNewRequestQRcodeNew = true;
		//-----------------------------------
		downloadMiniProgramQRcodeCall(box_mac);
		isNewRequestQRcodeCall = true;
		//-----------------------------------
		downloadMiniProgramSimpleQRcodeSmall(box_mac);
		isNewRequestSQRcode = true;
		//-----------------------------------
		downloadMiniProgramSimpleQRcodeBig(box_mac);
		isNewRequestSQRcodeBig = true;
		//-----------------------------------
		downloadMiniProgramSimpleQRcodeNew(box_mac);
		isNewRequestSQRcodeNew = true;
		//-----------------------------------
		downloadMiniProgramSimpleQRcodeCall(box_mac);
		isNewRequestSQRcodeCall = true;
		//-----------------------------------
		downloadMiniProgramQRcodeExtension(box_mac);
		isNewRequestQRcodeExtension = true;
	}


	private void closeWriter() {
		if (mLogWriter != null) {
			try {
				mLogWriter.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			mLogWriter = null;
		}
	}

	/**
	 * 获取日志内容
	 */
	private String makeLog(LogReportParam mparam){
		String boxId="";
		String logHour = "";
		String end = "";
		if ("poweron".equals(mparam.getAction())){
			boxId = mparam.getBoxId();
			logHour = mparam.getLogHour();
		}else {
			boxId = session.getEthernetMac();
			logHour = logTime;
		}
		if (file.getName().contains("standalone")){
			end = ",standalone"+"\r\n";
		}else {
			end = "\r\n";
		}
		String ret = mparam.getUUid() + ","
					+ mparam.getHotel_id() + ","
					+ mparam.getRoom_id() + ","
					+ mparam.getTime() + ","
					+ mparam.getAction() + ","
					+ mparam.getType()+ ","
					+ mparam.getMedia_id() + ","
					+ mparam.getMobile_id() + ","
					+ mparam.getApk_version() + ","
					+ mparam.getAdsPeriod() + ","
					+ mparam.getBirthdayPeriod() + ","
					+ mparam.getCustom() + ","
					+ boxId + ","
					+ logHour
					+ end;
		return ret;
	}

	/**
	 * 创建日志
	 */
	private void createFile() {
		try {
			AppUtils.cleanDspOnlineAdsData(mContext);
			String boxMac = session.getEthernetMac();

			File file1 = new File(AppUtils.getMainMediaPath());
			if (!file1.exists()) {
				LogFileUtil.writeKeyLogInfo("createFile() MainMediaPath is not exist!!!");
			}
			String path = AppUtils.getFilePath(AppUtils.StorageFile.log);
			logTime = AppUtils.getCurTime("yyyyMMddHH");
			tempTime = System.currentTimeMillis();
			if (session.isStandalone()){
				mLogWriter = new FileWriter(path + boxMac + "_" + logTime +"_"+standalone+".blog",true);
				file = new File(path + boxMac + "_" + logTime + "_" +standalone +".blog");
			}else {
				mLogWriter = new FileWriter(path + boxMac + "_" + logTime + ".blog",true);
				file = new File(path + boxMac + "_" + logTime + ".blog");
			}

//			String pathCode = AppUtils.getFilePath(mContext,AppUtils.StorageFile.qrcode_log);
//			mQRCodeLogWriter = new FileWriter(pathCode+boxMac+"_"+logTime+".blog",true);
		} catch (Exception e2) {
			e2.printStackTrace();
			LogFileUtil.writeException(e2);
			if (AppUtils.isMstar()){
				AppApi.reportSDCardState(mContext, apiRequestListener, 1);
			}
		}
	}


	ApiRequestListener apiRequestListener = new ApiRequestListener() {
		@Override
		public void onSuccess(AppApi.Action method, Object obj) {
			switch (method){
				case SP_GET_QR_SMALL_JSON:
					if (obj instanceof File){
						File file = (File)obj;
						String pathQRcode = AppUtils.getFilePath(AppUtils.StorageFile.cache) + ConstantValues.MINI_PROGRAM_QRCODE_NAME;
						File tarFile = new File(pathQRcode);
						if (tarFile.exists()) {
							tarFile.delete();
						}
						file.renameTo(tarFile);
					}
					break;
				case SP_GET_QR_BIG_JSON:
					if (obj instanceof File){
						File file = (File)obj;
						String pathQRcode = AppUtils.getFilePath(AppUtils.StorageFile.cache) + ConstantValues.MINI_PROGRAM_QRCODE_BIG_NAME;
						File tarFile = new File(pathQRcode);
						if (tarFile.exists()) {
							tarFile.delete();
						}
						file.renameTo(tarFile);
					}
					break;
				case SP_GET_QR_NEW_JSON:
					if (obj instanceof File){
						File file = (File)obj;
						String pathQRcode = AppUtils.getFilePath(AppUtils.StorageFile.cache) + ConstantValues.MINI_PROGRAM_QRCODE_NEW_NAME;
						File tarFile = new File(pathQRcode);
						if (tarFile.exists()) {
							tarFile.delete();
						}
						file.renameTo(tarFile);
					}
					break;
				case SP_GET_QR_CALL_JSON:
					if (obj instanceof File){
						File file = (File)obj;
						String pathQRcode = AppUtils.getFilePath(AppUtils.StorageFile.cache) + ConstantValues.MINI_PROGRAM_QRCODE_CALL_NAME;
						File tarFile = new File(pathQRcode);
						if (tarFile.exists()) {
							tarFile.delete();
						}
						file.renameTo(tarFile);
					}
					break;
				case SP_GET_QR_SIMPLE_SMALL_JSON:
					if (obj instanceof File){
						File file = (File)obj;
						String pathQRcode = AppUtils.getFilePath(AppUtils.StorageFile.cache) + ConstantValues.MINI_PROGRAM_SQRCODE_NAME;
						File tarFile = new File(pathQRcode);
						if (tarFile.exists()) {
							tarFile.delete();
						}
						file.renameTo(tarFile);
					}
					break;
				case SP_GET_QR_SIMPLE_BIG_JSON:
					if (obj instanceof File){
						File file = (File)obj;
						String pathQRcode = AppUtils.getFilePath(AppUtils.StorageFile.cache) + ConstantValues.MINI_PROGRAM_SQRCODE_BIG_NAME;
						File tarFile = new File(pathQRcode);
						if (tarFile.exists()) {
							tarFile.delete();
						}
						file.renameTo(tarFile);
					}
					break;
				case SP_GET_QR_SIMPLE_NEW_JSON:
					if (obj instanceof File){
						File file = (File)obj;
						String pathQRcode = AppUtils.getFilePath(AppUtils.StorageFile.cache) + ConstantValues.MINI_PROGRAM_SQRCODE_NEW_NAME;
						File tarFile = new File(pathQRcode);
						if (tarFile.exists()) {
							tarFile.delete();
						}
						file.renameTo(tarFile);
					}
					break;
				case SP_GET_QR_SIMPLE_CALL_JSON:
					if (obj instanceof File){
						File file = (File)obj;
						String pathQRcode = AppUtils.getFilePath(AppUtils.StorageFile.cache) + ConstantValues.MINI_PROGRAM_SQRCODE_CALL_NAME;
						File tarFile = new File(pathQRcode);
						if (tarFile.exists()) {
							tarFile.delete();
						}
						file.renameTo(tarFile);
					}
					break;
				case SP_GET_QR_EXTENSION_JSON:
					if (obj instanceof File){
						File file = (File)obj;
						String pathQRcode = AppUtils.getFilePath(AppUtils.StorageFile.cache) + ConstantValues.MINI_PROGRAM_QRCODE_EXTENSIOM_NAME;
						File tarFile = new File(pathQRcode);
						if (tarFile.exists()) {
							tarFile.delete();
						}
						file.renameTo(tarFile);
					}
					break;
			}

		}

		@Override
		public void onError(AppApi.Action method, Object obj) {
			String box_mac = Session.get(mContext).getEthernetMac();
			switch (method){
				case SP_GET_QR_SMALL_JSON:
					if (isNewRequestQRcode){
						downloadMiniProgramQRcodeSmall(box_mac);
						isNewRequestQRcode = false;
					}
					break;
				case SP_GET_QR_BIG_JSON:
					if (isNewRequestQRcodeBig){
						downloadMiniProgramQRcodeBig(box_mac);
						isNewRequestQRcodeBig = false;
					}
					break;
				case SP_GET_QR_NEW_JSON:
					if (isNewRequestQRcodeNew){
						downloadMiniProgramQRcodeNew(box_mac);
						isNewRequestQRcodeNew = false;
					}
					break;
				case SP_GET_QR_CALL_JSON:
					if (isNewRequestQRcodeCall){
						downloadMiniProgramQRcodeCall(box_mac);
						isNewRequestQRcodeCall = false;
					}
					break;
				case SP_GET_QR_SIMPLE_SMALL_JSON:
					if (isNewRequestSQRcode){
						downloadMiniProgramSimpleQRcodeSmall(box_mac);
						isNewRequestSQRcode = false;
					}
					break;
				case SP_GET_QR_SIMPLE_BIG_JSON:
					if (isNewRequestSQRcodeBig){
						downloadMiniProgramSimpleQRcodeBig(box_mac);
						isNewRequestSQRcodeBig = false;
					}
					break;
				case SP_GET_QR_SIMPLE_NEW_JSON:
					if (isNewRequestSQRcodeNew){
						downloadMiniProgramSimpleQRcodeNew(box_mac);
						isNewRequestSQRcodeNew = false;
					}
					break;
				case SP_GET_QR_SIMPLE_CALL_JSON:
					if (isNewRequestSQRcodeCall){
						downloadMiniProgramSimpleQRcodeCall(box_mac);
						isNewRequestSQRcodeCall = false;
					}
					break;
				case SP_GET_QR_EXTENSION_JSON:
					if (isNewRequestQRcodeExtension){
						downloadMiniProgramQRcodeExtension(box_mac);
						isNewRequestQRcodeExtension = false;
					}
					break;
			}
		}

		@Override
		public void onNetworkFailed(AppApi.Action method) {

		}
	};

	/**下载小程序码-二维码小**/
	private void downloadMiniProgramQRcodeSmall(String box_mac){
		String urlQRcode = AppApi.API_URLS.get(AppApi.Action.CP_MINIPROGRAM_DOWNLOAD_QRCODE_JSON)+"?box_mac="+ box_mac+"&type="+ ConstantValues.MINI_PROGRAM_QRCODE_SMALL_TYPE;
		String pathQRcode = AppUtils.getFilePath(AppUtils.StorageFile.cache) + ConstantValues.MINI_PROGRAM_QRCODE_TEMP_NAME;
		File tarFile = new File(pathQRcode);
		if (tarFile.exists()) {
			tarFile.delete();
		}
		AppApi.downloadQRSmallImg(urlQRcode,mContext,apiRequestListener,pathQRcode);
	}
	/**下载小程序码-二维码大**/
	private void downloadMiniProgramQRcodeBig(String box_mac){
		String urlQRcode = AppApi.API_URLS.get(AppApi.Action.CP_MINIPROGRAM_DOWNLOAD_QRCODE_JSON)+"?box_mac="+ box_mac+"&type="+ ConstantValues.MINI_PROGRAM_QRCODE_BIG_TYPE;
		String pathQRcode = AppUtils.getFilePath(AppUtils.StorageFile.cache) + ConstantValues.MINI_PROGRAM_QRCODE_BIG_TEMP_NAME;
		File tarFile = new File(pathQRcode);
		if (tarFile.exists()) {
			tarFile.delete();
		}
		AppApi.downloadQRBigImg(urlQRcode,mContext,apiRequestListener,pathQRcode);
	}
	/**下载小程序码-二维码新节目**/
	private void downloadMiniProgramQRcodeNew(String box_mac){
		String urlQRcode = AppApi.API_URLS.get(AppApi.Action.CP_MINIPROGRAM_DOWNLOAD_QRCODE_JSON)+"?box_mac="+ box_mac+"&type="+ ConstantValues.MINI_PROGRAM_QRCODE_NEW_TYPE;
		String pathQRcode = AppUtils.getFilePath(AppUtils.StorageFile.cache) + ConstantValues.MINI_PROGRAM_QRCODE_NEW_TEMP_NAME;
		File tarFile = new File(pathQRcode);
		if (tarFile.exists()) {
			tarFile.delete();
		}
		AppApi.downloadQRNewImg(urlQRcode,mContext,apiRequestListener,pathQRcode);
	}
	/**下载小程序码-二维码call**/
	private void downloadMiniProgramQRcodeCall(String box_mac){
		String urlQRcode = AppApi.API_URLS.get(AppApi.Action.CP_MINIPROGRAM_DOWNLOAD_QRCODE_JSON)+"?box_mac="+ box_mac+"&type="+ ConstantValues.MINI_PROGRAM_QRCODE_CALL_TYPE;
		String pathQRcode = AppUtils.getFilePath(AppUtils.StorageFile.cache) + ConstantValues.MINI_PROGRAM_QRCODE_CALL_TEMP_NAME;
		File tarFile = new File(pathQRcode);
		if (tarFile.exists()) {
			tarFile.delete();
		}
		AppApi.downloadQRCallImg(urlQRcode,mContext,apiRequestListener,pathQRcode);
	}
	/**下载小程序码-极简二维码小**/
	private void downloadMiniProgramSimpleQRcodeSmall(String box_mac){
		String urlSmall = AppApi.API_URLS.get(AppApi.Action.CP_SIMPLE_MINIPROGRAM_DOWNLOAD_QRCODE_JSON)+"?box_mac="+ box_mac+"&type="+ ConstantValues.MINI_PROGRAM_SQRCODE_SMALL_TYPE;
		String pathSmall = AppUtils.getFilePath(AppUtils.StorageFile.cache) + ConstantValues.MINI_PROGRAM_SQRCODE_TEMP_NAME;
		File tarFile = new File(pathSmall);
		if (tarFile.exists()) {
			tarFile.delete();
		}
		AppApi.downloadQRSimpleSmallImg(urlSmall,mContext,apiRequestListener,pathSmall);
	}
	/**下载小程序码-极简二维码大**/
	private void downloadMiniProgramSimpleQRcodeBig(String box_mac){
		String urlSmall = AppApi.API_URLS.get(AppApi.Action.CP_SIMPLE_MINIPROGRAM_DOWNLOAD_QRCODE_JSON)+"?box_mac="+ box_mac+"&type="+ ConstantValues.MINI_PROGRAM_SQRCODE_BIG_TYPE;
		String pathSmall = AppUtils.getFilePath(AppUtils.StorageFile.cache) + ConstantValues.MINI_PROGRAM_SQRCODE_BIG_TEMP_NAME;
		File tarFile = new File(pathSmall);
		if (tarFile.exists()) {
			tarFile.delete();
		}
		AppApi.downloadQRSimpleBigImg(urlSmall,mContext,apiRequestListener,pathSmall);
	}
	/**下载小程序码-极简二维码新节目**/
	private void downloadMiniProgramSimpleQRcodeNew(String box_mac){
		String urlSmall = AppApi.API_URLS.get(AppApi.Action.CP_SIMPLE_MINIPROGRAM_DOWNLOAD_QRCODE_JSON)+"?box_mac="+ box_mac+"&type="+ ConstantValues.MINI_PROGRAM_SQRCODE_NEW_TYPE;
		String pathSmall = AppUtils.getFilePath(AppUtils.StorageFile.cache) + ConstantValues.MINI_PROGRAM_SQRCODE_NEW_TEMP_NAME;
		File tarFile = new File(pathSmall);
		if (tarFile.exists()) {
			tarFile.delete();
		}
		AppApi.downloadQRSimpleNewImg(urlSmall,mContext,apiRequestListener,pathSmall);
	}
	/**下载小程序码-极简二维码call**/
	private void downloadMiniProgramSimpleQRcodeCall(String box_mac){
		String urlSmall = AppApi.API_URLS.get(AppApi.Action.CP_SIMPLE_MINIPROGRAM_DOWNLOAD_QRCODE_JSON)+"?box_mac="+ box_mac+"&type="+ ConstantValues.MINI_PROGRAM_SQRCODE_CALL_TYPE;
		String pathSmall = AppUtils.getFilePath(AppUtils.StorageFile.cache) + ConstantValues.MINI_PROGRAM_SQRCODE_CALL_TEMP_NAME;
		File tarFile = new File(pathSmall);
		if (tarFile.exists()) {
			tarFile.delete();
		}
		AppApi.downloadQRSimpleCallImg(urlSmall,mContext,apiRequestListener,pathSmall);
	}

	private void downloadMiniProgramQRcodeExtension(String box_mac){
		String urlExtension = AppApi.API_URLS.get(AppApi.Action.CP_MINIPROGRAM_DOWNLOAD_QRCODE_JSON)+"?box_mac="+ box_mac+"&type="+ ConstantValues.MINI_PROGRAM_QRCODE_EXTENSION_TYPE;
		String pathExtension = AppUtils.getFilePath(AppUtils.StorageFile.cache) + ConstantValues.MINI_PROGRAM_QRCODE_EXTENSION_TEMP_NAME;
		File tarFile = new File(pathExtension);
		if (tarFile.exists()) {
			tarFile.delete();
		}
		AppApi.downloadQRCodeExtensionImg(urlExtension,mContext,apiRequestListener,pathExtension);
	}
}
