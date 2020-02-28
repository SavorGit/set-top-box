package cn.savor.small.netty;

import com.google.gson.annotations.SerializedName;

/**
 * Created by zhanghq on 2017/3/27.
 */

public class InnerBean {
    /**
     * 酒楼ID
     */
    @SerializedName("hotel_id")
    private String hotelId;
    /**
     * 包间ID
     */
    @SerializedName("room_id")
    private String roomId;
    /**
     * 机顶盒ID
     */
    @SerializedName("box_id")
    private String boxId;
    /**
     * SSID
     */
    @SerializedName("ssid")
    private String ssid;
    /**
     * 手机连接码（展示二维码时使用）
     */
    @SerializedName("random_code")
    private String connectCode;

    public String getHotelId() {
        return hotelId;
    }

    public void setHotelId(String hotelId) {
        this.hotelId = hotelId;
    }

    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    public String getSsid() {
        return ssid;
    }

    public void setSsid(String ssid) {
        this.ssid = ssid;
    }
    public String getConnectCode() {
        return connectCode;
    }

    public void setConnectCode(String connectCode) {
        this.connectCode = connectCode;
    }

    public String getBoxId() {
        return boxId;
    }

    public void setBoxId(String boxId) {
        this.boxId = boxId;
    }
}
