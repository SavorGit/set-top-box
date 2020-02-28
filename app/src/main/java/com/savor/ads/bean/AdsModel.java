package com.savor.ads.bean;

public class AdsModel {

	private String playPath = "";
	private String AdsName ="";
	private String AdsId ="";
	private int AdsTrigger= 1;//1:正常循环播放 2：手机推送 3：TV
	private int Time;
	private boolean isPlaying = false;
	private String surfix = "";
	private String md5 = "";
	private int num;

	public AdsModel() {
	}
	
	public void setNum(int arg)
	{
		this.num = arg;
	}
	
	public int getNum() 
	{
		return this.num;
	}
	public void setMd5(String arg) 
	{
		this.md5 = arg;
	}
	
	public String getMd5() 
	{
		return this.md5;
	}
	
	public void setSurfix(String arg)
	{
		this.surfix = arg;
	}
	
	public String getSurfix()
	{
		return this.surfix;
	}
	
	public void setAdsName(String arg)
	{
		this.AdsName = arg;
	}
	public String getAdsName()
	{
		return this.AdsName;
	}
	
	public void setAdsId(String arg)
	{
		this.AdsId = arg;
	}
	public String getAdsId()
	{
		return this.AdsId;
	}
	
	public void setPlayPath(String arg)
	{
		this.playPath = arg;
	}
	public String getPlayPath()
	{
		return this.playPath;
	}
	
	public void setIsplaying(boolean arg)
	{
		isPlaying = arg;
	}
	public boolean getIsplaying(){
		return isPlaying;
	}

	public int getAdsTrigger() {
		return AdsTrigger;
	}
	public void setAdsTrigger(int arg){
		AdsTrigger = arg;
	}
}
