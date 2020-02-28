package com.savor.ads.bean;

import java.io.Serializable;

public class UpgradeInfo implements Serializable {
	/** 是否强制升级 1强制升级  0不强制升级*/
	private int isApkForceUpgrade ;
	private int isRomForceUpgrade ;
	/**是否弱提醒   1提醒   0不提醒 */
	private int isApkPromptUpgrade;
	private int isRomPromptUpgrade;
	/**最新版本号 */
	private String newestApkVersion;
	private String newestRomVersion;
	/**软件下载地址 */
	private String apkUrl;
	private String romUrl;
	private String apk_name;
	private String logo_name;

	/**下载安装包文件的md5值 */
	private String apkMd5;
	private String romMd5;
	//oss桶名字
	private String ossBucketName;
	//oss_path
	private String oss_path;
	//oss地区ID
	private String areaId;

	private String logo_url;
	private String logo_md5;
	private String logo_version;
	//是否是虚拟小平台
	private boolean isVirtual;

	public int getIsApkForceUpgrade() {
		return isApkForceUpgrade;
	}

	public void setIsApkForceUpgrade(int isApkForceUpgrade) {
		this.isApkForceUpgrade = isApkForceUpgrade;
	}

	public int getIsRomForceUpgrade() {
		return isRomForceUpgrade;
	}

	public void setIsRomForceUpgrade(int isRomForceUpgrade) {
		this.isRomForceUpgrade = isRomForceUpgrade;
	}

	public int getIsApkPromptUpgrade() {
		return isApkPromptUpgrade;
	}

	public void setIsApkPromptUpgrade(int isApkPromptUpgrade) {
		this.isApkPromptUpgrade = isApkPromptUpgrade;
	}

	public int getIsRomPromptUpgrade() {
		return isRomPromptUpgrade;
	}

	public void setIsRomPromptUpgrade(int isRomPromptUpgrade) {
		this.isRomPromptUpgrade = isRomPromptUpgrade;
	}

	public String getNewestApkVersion() {
		return newestApkVersion;
	}

	public void setNewestApkVersion(String newestApkVersion) {
		this.newestApkVersion = newestApkVersion;
	}

	public String getNewestRomVersion() {
		return newestRomVersion;
	}

	public void setNewestRomVersion(String newestRomVersion) {
		this.newestRomVersion = newestRomVersion;
	}

	public String getApkUrl() {
		return apkUrl;
	}

	public void setApkUrl(String apkUrl) {
		this.apkUrl = apkUrl;
	}

	public String getRomUrl() {
		return romUrl;
	}

	public void setRomUrl(String romUrl) {
		this.romUrl = romUrl;
	}

	public String getApkMd5() {
		return apkMd5;
	}

	public void setApkMd5(String apkMd5) {
		this.apkMd5 = apkMd5;
	}

	public String getRomMd5() {
		return romMd5;
	}

	public void setRomMd5(String romMd5) {
		this.romMd5 = romMd5;
	}

	public String getOssBucketName() {
		return ossBucketName;
	}

	public void setOssBucketName(String ossBucketName) {
		this.ossBucketName = ossBucketName;
	}

	public String getAreaId() {
		return areaId;
	}

	public void setAreaId(String areaId) {
		this.areaId = areaId;
	}

	public String getLogo_url() {
		return logo_url;
	}

	public void setLogo_url(String logo_url) {
		this.logo_url = logo_url;
	}

	public String getLogo_md5() {
		return logo_md5;
	}

	public void setLogo_md5(String logo_md5) {
		this.logo_md5 = logo_md5;
	}

	public String getApk_name() {
		return apk_name;
	}

	public void setApk_name(String apk_name) {
		this.apk_name = apk_name;
	}

	public String getLogo_name() {
		return logo_name;
	}

	public void setLogo_name(String logo_name) {
		this.logo_name = logo_name;
	}

	public String getLogo_version() {
		return logo_version;
	}

	public void setLogo_version(String logo_version) {
		this.logo_version = logo_version;
	}

	public boolean isVirtual() {
		return isVirtual;
	}

	public void setVirtual(boolean virtual) {
		isVirtual = virtual;
	}

	public String getOss_path() {
		return oss_path;
	}

	public void setOss_path(String oss_path) {
		this.oss_path = oss_path;
	}
}
