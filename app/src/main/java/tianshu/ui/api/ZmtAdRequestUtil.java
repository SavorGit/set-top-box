/**
 * 
 */
package tianshu.ui.api;


import android.content.Context;

import com.google.protobuf.ByteString;
import com.savor.ads.bean.NetworkUtils;
import com.savor.ads.core.Session;
import com.savor.ads.utils.AppUtils;
import com.savor.ads.utils.ConstantValues;
import com.savor.ads.utils.DeviceUtils;
import tianshu.ui.api.ZmtAPI.ZmAdRequest;
import tianshu.ui.api.ZmtAPI.ZmAdRequest.Builder;
import tianshu.ui.api.ZmtAPI.Network;
import tianshu.ui.api.ZmtAPI.Network.OperatorType;
import tianshu.ui.api.ZmtAPI.Device;
import tianshu.ui.api.ZmtAPI.AdSlot;
import org.apache.commons.lang3.RandomStringUtils;

/**
 * @author fubaokui
 * @date 2017年10月19日 下午5:13:27
 */
public class ZmtAdRequestUtil {
	private Context context;

	public ZmAdRequest buildAdRequst(Context context) {
		this.context = context;
		Builder requestBuilder = ZmAdRequest.newBuilder();
		requestBuilder.setRequestId(Long.toString(System.currentTimeMillis())
				+ RandomStringUtils.randomAlphanumeric(19)); // [0-9a-zA-Z]{32},保证每次请求唯一
		requestBuilder.setChannelId(ConstantValues.ZMENG_CHANNEL_ID); // 请使用众盟分配的channelId
		requestBuilder.setScreenId(AppUtils.getEthernetMacAddr()); // 测试环境不校验；正式环境必须使用在众盟备案的屏id
		requestBuilder.setToken(ConstantValues.ZMENG_TOKEN); // 请使用众盟分配的token
		System.currentTimeMillis();
		// 必填
		// 构建Device请求参数
		builderDevice(requestBuilder);
		// 必填
		// 构建Network请求参数
		builderNetwork(requestBuilder);
		// 必填
		// 构建Adslot请求参数
		builderAdslot(requestBuilder);

		// 选填
		// 构建GPS请求参数
//		builderGps(requestBuilder);

		return requestBuilder.build();
	}

//	private void builderGps(Builder jPadBuilder) {
//		jPadBuilder.setGps(Gps.newBuilder().setCoordinateType(Gps.CoordinateType.GCJ02)
//				.setLatitude(116.33912).setLongitude(39.99303375).build());
//	}

	private void builderNetwork(Builder jPadBuilder) {
		Network.Builder networkBuilder = Network.newBuilder();
		networkBuilder.setCellularId("")
				.setOperatorType(OperatorType.CHINA_MOBILE)
		 		.setConnectionType(Network.ConnectionType.CELL_4G)
				.setIpv4(NetworkUtils.getIPAddress(true));
		jPadBuilder.setNetwork(networkBuilder.build());
	}

	private void builderDevice(Builder jPadBuilder) {
		Device.Builder builder = Device.newBuilder();
		builder.setUdid(ZmtAPI.UdId.newBuilder()
				.setAndroidId(DeviceUtils.getAndroidID(context))
				.setMac(DeviceUtils.getMacAddress(context))).build();
		builder.setOsType(Device.OsType.ANDROID);
		builder.setDeviceType(Device.DeviceType.OUTDOOR_SCREEN);
		builder.setOsVersion(ZmtAPI.Version.newBuilder().setMajor(4).setMinor(4).setMicro(4).build());
		builder.setVendor(ByteString.copyFromUtf8(Session.get(context).getModel()));
		builder.setModel(ByteString.copyFromUtf8(Session.get(context).getModel()));
		builder.setScreenSize(ZmtAPI.Size.newBuilder().setWidth(1280).setHeight(720).build());
		jPadBuilder.setDevice(builder.build());
	}

	private void builderAdslot(Builder jPadBuilder) {
		AdSlot.Builder builder = AdSlot.newBuilder();
		builder
				.setType(0)
				.setAdslotSize(ZmtAPI.Size.newBuilder().setWidth(1280).setHeight(720));
		jPadBuilder.setAdslot(builder.build());
	}

}
