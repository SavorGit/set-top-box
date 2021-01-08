package com.savor.ads.bean;

import java.io.Serializable;
import java.util.List;

public class MiniProgramProjection implements Serializable{
    /**小程序投屏动作1:呼玛  2：投屏：3 退出投屏 4:投屏多张图片（包括单张）**/
    private int action;
    /**netty请求序列号*/
    private String req_id;
    /**小程序呼出的码**/
    private int code;
    /**微信标示openid**/
    private String openid;
    /**投屏图片url**/
    private String url;
    /**投屏图片名称**/
    private String filename;
    /**投多张图片时总张数**/
    private int img_nums;
    /**投屏添加文字**/
    private String forscreen_char;
    /**操作ID**/
    private String forscreen_id;
    /***********************微信小程序游戏中用到的字段*******************************/
    /**游戏活动ID*/
    private long activity_id;
    /**微信用户头像**/
    private String avatarurl;
    /**游戏邀请码地址**/
    private String gamecode;
    /**欢迎词文字大小*/
    private String wordsize;
    /**欢迎词文字颜色*/
    private String color;
    /**投图片ID*/
    private String img_id;
    private String img_path;
    /**欢迎词图片OSS地址*/
    private String img_oss_addr;
    /**欢迎词音乐ID*/
    private String music_id;
    private String music_path;
    /**欢迎词音乐OSS地址*/
    private String music_oss_addr;
    /**欢迎词字体ID*/
    private String font_id;
    private String font_path;
    /**投视频ID**/
    private String video_id;
    private String goods_id;
    /**微信头像*/
    private String headPic;
    /**微信昵称*/
    private String nickName;
    /**手機號*/
    private String mobile;
    /**職位*/
    private String job;
    /**公司名称*/
    private String company;
    /**二维码地址*/
    private String codeUrl;
    /**小程序餐厅端投屏时长,单位为秒,如果为0就是单次**/
    private int play_times;
    private int resource_type;
    /**文件大小*/
    private long resource_size;
    private String resource_id;
    private List<UserBarrage> userBarrages;
    /**欢迎词指令ID*/
    private int id;
    /**旋转角度*/
    private int rotation;
    /**对于已存在oss的图片，采用一次性传json的方式将本次投屏多张图片一次性传过*/
    private List<ProjectionImg> img_list;
    /**类型 1普通欢迎词 2有服务员信息欢迎词*/
    private int type;
    /**服务人员名称*/
    private String waiterName;
    /**服务人员头像地址*/
    private String waiterIconUrl;

    private String serial_number;
    /**倒计时*/
    private int countdown;
    /**开奖倒计时*/
    private int lottery_countdown;
    /**开奖时间*/
    private String lottery_time;
    /**活动图片*/
    private String partake_img;
    /**活动文件名*/
    private String partake_filename;
    /**活动菜名*/
    private String partake_name;
    /**活动名称*/
    private String activity_name;
    /**满意度层级 1:很糟糕 2:一般般 3:太赞了*/
    private int satisfaction;

    public int getAction() {
        return action;
    }

    public void setAction(int action) {
        this.action = action;
    }

    public String getReq_id() {
        return req_id;
    }

    public void setReq_id(String req_id) {
        this.req_id = req_id;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getOpenid() {
        return openid;
    }

    public void setOpenid(String openid) {
        this.openid = openid;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public int getImg_nums() {
        return img_nums;
    }

    public void setImg_nums(int img_nums) {
        this.img_nums = img_nums;
    }

    public String getForscreen_char() {
        return forscreen_char;
    }

    public void setForscreen_char(String forscreen_char) {
        this.forscreen_char = forscreen_char;
    }

    public String getForscreen_id() {
        return forscreen_id;
    }

    public void setForscreen_id(String forscreen_id) {
        this.forscreen_id = forscreen_id;
    }

    public long getActivity_id() {
        return activity_id;
    }

    public void setActivity_id(long activity_id) {
        this.activity_id = activity_id;
    }

    public String getAvatarurl() {
        return avatarurl;
    }

    public void setAvatarurl(String avatarurl) {
        this.avatarurl = avatarurl;
    }

    public String getGamecode() {
        return gamecode;
    }

    public void setGamecode(String gamecode) {
        this.gamecode = gamecode;
    }

    public String getImg_id() {
        return img_id;
    }

    public void setImg_id(String img_id) {
        this.img_id = img_id;
    }

    public String getVideo_id() {
        return video_id;
    }

    public void setVideo_id(String video_id) {
        this.video_id = video_id;
    }

    public String getGoods_id() {
        return goods_id;
    }

    public void setGoods_id(String goods_id) {
        this.goods_id = goods_id;
    }

    public String getHeadPic() {
        return headPic;
    }

    public void setHeadPic(String headPic) {
        this.headPic = headPic;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getJob() {
        return job;
    }

    public void setJob(String job) {
        this.job = job;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public String getCodeUrl() {
        return codeUrl;
    }

    public void setCodeUrl(String codeUrl) {
        this.codeUrl = codeUrl;
    }

    public int getPlay_times() {
        return play_times;
    }

    public void setPlay_times(int play_times) {
        this.play_times = play_times;
    }

    public int getResource_type() {
        return resource_type;
    }

    public void setResource_type(int resource_type) {
        this.resource_type = resource_type;
    }

    public long getResource_size() {
        return resource_size;
    }

    public void setResource_size(long resource_size) {
        this.resource_size = resource_size;
    }

    public String getResource_id() {
        return resource_id;
    }

    public void setResource_id(String resource_id) {
        this.resource_id = resource_id;
    }

    public List<UserBarrage> getUserBarrages() {
        return userBarrages;
    }

    public void setUserBarrages(List<UserBarrage> userBarrages) {
        this.userBarrages = userBarrages;
    }

    public String getWordsize() {
        return wordsize;
    }

    public void setWordsize(String wordsize) {
        this.wordsize = wordsize;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getImg_oss_addr() {
        return img_oss_addr;
    }

    public void setImg_oss_addr(String img_oss_addr) {
        this.img_oss_addr = img_oss_addr;
    }

    public String getMusic_id() {
        return music_id;
    }

    public void setMusic_id(String music_id) {
        this.music_id = music_id;
    }

    public String getMusic_oss_addr() {
        return music_oss_addr;
    }

    public void setMusic_oss_addr(String music_oss_addr) {
        this.music_oss_addr = music_oss_addr;
    }

    public String getFont_id() {
        return font_id;
    }

    public void setFont_id(String font_id) {
        this.font_id = font_id;
    }

    public String getFont_path() {
        return font_path;
    }

    public void setFont_path(String font_path) {
        this.font_path = font_path;
    }

    public String getImg_path() {
        return img_path;
    }

    public void setImg_path(String img_path) {
        this.img_path = img_path;
    }

    public String getMusic_path() {
        return music_path;
    }

    public void setMusic_path(String music_path) {
        this.music_path = music_path;
    }

    public int getRotation() {
        return rotation;
    }

    public void setRotation(int rotation) {
        this.rotation = rotation;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public List<ProjectionImg> getImg_list() {
        return img_list;
    }

    public void setImg_list(List<ProjectionImg> img_list) {
        this.img_list = img_list;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getWaiterName() {
        return waiterName;
    }

    public void setWaiterName(String waiterName) {
        this.waiterName = waiterName;
    }

    public String getWaiterIconUrl() {
        return waiterIconUrl;
    }

    public void setWaiterIconUrl(String waiterIconUrl) {
        this.waiterIconUrl = waiterIconUrl;
    }

    public String getSerial_number() {
        return serial_number;
    }

    public void setSerial_number(String serial_number) {
        this.serial_number = serial_number;
    }

    public int getCountdown() {
        return countdown;
    }

    public void setCountdown(int countdown) {
        this.countdown = countdown;
    }

    public int getLottery_countdown() {
        return lottery_countdown;
    }

    public void setLottery_countdown(int lottery_countdown) {
        this.lottery_countdown = lottery_countdown;
    }

    public String getLottery_time() {
        return lottery_time;
    }

    public void setLottery_time(String lottery_time) {
        this.lottery_time = lottery_time;
    }

    public String getPartake_img() {
        return partake_img;
    }

    public void setPartake_img(String partake_img) {
        this.partake_img = partake_img;
    }

    public String getPartake_filename() {
        return partake_filename;
    }

    public void setPartake_filename(String partake_filename) {
        this.partake_filename = partake_filename;
    }

    public String getPartake_name() {
        return partake_name;
    }

    public void setPartake_name(String partake_name) {
        this.partake_name = partake_name;
    }

    public String getActivity_name() {
        return activity_name;
    }

    public void setActivity_name(String activity_name) {
        this.activity_name = activity_name;
    }

    public int getSatisfaction() {
        return satisfaction;
    }

    public void setSatisfaction(int satisfaction) {
        this.satisfaction = satisfaction;
    }
}
