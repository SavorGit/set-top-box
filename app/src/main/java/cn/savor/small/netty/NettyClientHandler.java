/**
 * Copyright (c) 2016, Stupid Bird and/or its affiliates. All rights reserved.
 * STUPID BIRD PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 *
 * @Project : netty
 * @Package : net.lizhaoweb.netty
 * @author <a href="http://www.lizhaoweb.net">李召(John.Lee)</a>
 * @EMAIL 404644381@qq.com
 * @Time : 13:38
 */
package cn.savor.small.netty;


import android.content.ComponentName;
import android.content.Context;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.jar.savor.box.vo.AdvResponseBean;
import com.jar.savor.box.vo.ResponseT;
import com.jar.savor.box.vo.ResponseT1;
import com.jar.savor.box.vo.SpecialtyResponseBean;
import com.savor.ads.bean.MediaLibBean;
import com.savor.ads.bean.RstrSpecialty;
import com.savor.ads.callback.ProjectOperationListener;
import com.savor.ads.core.AppApi;
import com.savor.ads.core.Session;
import com.savor.ads.database.DBHelper;
import com.savor.ads.utils.AppUtils;
import com.savor.ads.utils.ConstantValues;
import com.savor.ads.utils.GlobalValues;
import com.savor.ads.utils.IPAddressUtils;
import com.savor.ads.utils.LogFileUtil;
import com.savor.ads.utils.LogUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.EventLoop;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;

/**
 * @author bichao</a>
 * @version 1.0.0.0.1
 * @notes Created on 2016年12月05日<br>
 * Revision of last commit:$Revision$<br>
 * Author of last commit:$Author$<br>
 * Date of last commit:$Date$<br>
 */
@ChannelHandler.Sharable
public class NettyClientHandler extends SimpleChannelInboundHandler<MessageBean> {
    private NettyClient.NettyMessageCallback callback;
    private Session session;
    private NettyClient nettyClient;
    private Context mContext;

    public NettyClientHandler(NettyClient client,NettyClient.NettyMessageCallback m, Context context) {
        this.callback = m;
        this.nettyClient = client;
        this.mContext = context;
        this.session = Session.get(context);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        LogUtils.i("Client  Channel Active.................." + NettyClient.host + ':' + NettyClient.port);
        LogFileUtil.write("NettyClientHandler Client Channel Active.................." + NettyClient.host + ':' + NettyClient.port);
        if (callback != null) {
            callback.onConnected();
        }
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, MessageBean msg) throws Exception {
//        LogUtils.i(("Client received: " + ByteBufUtil.hexDump(msg.readBytes(msg.readableBytes())));
        if (msg == null) return;
        /**来自服务端的数据包请求*/
        MessageBean.Action Order = msg.getCmd();
        switch (Order) {
            case HEART_SERVER_TO_CLIENT:
                List<String> contentMsgH = msg.getContent();
                for (String tmp : contentMsgH) {
                    LogUtils.v("SERVER_HEART_RESP： 收到来自服务端的...心跳回应." + tmp + "===>>接收到内容:" + msg.getContent());
                }
                break;
            case SERVER_ORDER_REQ:
                List<String> contentMsg = msg.getContent();

                MessageBean response = new MessageBean();
                response.setCmd(MessageBean.Action.CLIENT_ORDER_RESP);
                response.setSerialnumber(msg.getSerialnumber());
                response.setContent(new ArrayList<String>());

                if (contentMsg != null && contentMsg.size() > 1) {
                    String order = contentMsg.get(0);
                    String params = contentMsg.get(1);
                    response.getContent().add(order);

                    if (ConstantValues.NETTY_SHOW_QRCODE_COMMAND.equals(order)) {
                        // 呼码
                        LogUtils.d("Netty command: show code " + params);
                        String connectCode = "";
                        try {
                            InnerBean bean = new Gson().fromJson(params, new TypeToken<InnerBean>() {
                            }.getType());
                            connectCode = bean.getConnectCode();
                        } catch (JsonSyntaxException e) {
                            e.printStackTrace();
                        }

                        if (callback != null) {
                            callback.onReceiveServerMessage(order, connectCode);
                        }
//                        ArrayList<String> contList = new ArrayList<String>();
//                        String xContent = "我收到了.数据包..回应下";
//                        contList.add(xContent);
//                        response.setContent(contList);
                        response.setIp(IPAddressUtils.getLocalIPAddress());
                        response.setMac(session.getEthernetMac());
                    } else if (ConstantValues.NETTY_STOP_PROJECTION_COMMAND.equals(order)) {
                        // 停止投屏
                        LogUtils.d("Netty command: stop projection " + params);
                        handleStopProjection(params, response);
                    }else if (ConstantValues.NETTY_MINI_PROGRAM_COMMAND.equals(order)){
                        LogUtils.d("Netty command: show mini program " + params);

                    }
                }
                ctx.writeAndFlush(response);
                break;
            default:
                break;
        }
    }

    private void handleStopProjection(String json, MessageBean response) {
        try {
            JsonObject jsonObject = (JsonObject) new JsonParser().parse(json);
            String deviceId = jsonObject.get("deviceId").getAsString();

            ResponseT1 resp = new ResponseT1();
            if (!TextUtils.isEmpty(deviceId) && deviceId.equals(GlobalValues.CURRENT_PROJECT_DEVICE_ID) && GlobalValues.IS_RSTR_PROJECTION) {
                ProjectOperationListener.getInstance(mContext).rstrStop();
                resp.setResult(AppApi.HTTP_RESPONSE_STATE_SUCCESS);
                resp.setInfo("停止成功");

                GlobalValues.IS_RSTR_PROJECTION = false;
                GlobalValues.CURRENT_PROJECT_IMAGE_ID = null;
            } else {
                resp.setResult(ConstantValues.SERVER_RESPONSE_CODE_FAILED);
                resp.setInfo("您的投屏已退出，无需再次停止");
            }

            response.getContent().add(new Gson().toJson(resp));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        super.userEventTriggered(ctx, evt);
        String channelId = ctx.channel().id().toString();
        if (evt instanceof IdleStateEvent) {

            IdleStateEvent event = (IdleStateEvent) evt;

            if (event.state().equals(IdleState.READER_IDLE)) {
                //未进行读操作
                LogUtils.i("READER_IDLE");
                LogFileUtil.write("NettyClientHandler READER_IDLE");


            } else if (event.state().equals(IdleState.WRITER_IDLE)) {

                LogUtils.i("WRITER_IDLE");
                LogFileUtil.write("NettyClientHandler WRITER_IDLE");
                //客户端发起REQ心跳查询==========
                LogUtils.i("ALL_IDLE");
                // 发送心跳消息
                MessageBean message = new MessageBean();
                message.setCmd(MessageBean.Action.HEART_CLENT_TO_SERVER);
                String number = channelId + System.currentTimeMillis();
                message.setSerialnumber(number);
                message.setIp(IPAddressUtils.getLocalIPAddress());
                message.setMac(session.getEthernetMac());
                InnerBean bean = new InnerBean();
                bean.setHotelId(session.getBoiteId());
                bean.setRoomId(session.getRoomId());
                bean.setSsid(AppUtils.getShowingSSID(mContext));
                bean.setBoxId(session.getBoxId());
                ArrayList<String> contList = new ArrayList<String>();
                contList.add("I am a Heart Pakage...");
                contList.add(new Gson().toJson(bean));
                message.setContent(contList);
                ctx.writeAndFlush(message);
                LogUtils.v("客户端向服务端发送====" + channelId + "====>>>>心跳包.....流水号:" + message.getSerialnumber());
                LogFileUtil.write("NettyClientHandler 客户端向服务端发送====" + channelId + "====>>>>心跳包.....流水号:" + message.getSerialnumber());
            } else if (event.state().equals(IdleState.ALL_IDLE)) {
                //未进行读写
                close(ctx);
            }

        }
    }

    public void close(ChannelHandlerContext ctx){
        try {
            Channel cid=ctx.pipeline().channel();
            LogUtils.v("close a client id: "+cid.toString());
            ChannelFuture future=ctx.close();
            boolean is=future.isSuccess();
            LogUtils.v("close be invoke.is success?"+is);

        }catch (Exception ex){
            ex.toString();
        }

    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        LogUtils.i("客户端出现异常，退出........" + NettyClient.host + ':' + NettyClient.port);
        close(ctx);
    }

    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
//        super.channelUnregistered(ctx);
        LogUtils.i("channelUnregistered......." + NettyClient.host + ':' + NettyClient.port);
        LogFileUtil.write("NettyClientHandler channelUnregistered......." + NettyClient.host + ':' + NettyClient.port);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        reconnect(ctx);
        super.channelInactive(ctx);
    }

    public void reconnect(ChannelHandlerContext ctx) {
        try {

            final EventLoop loop = ctx.channel().eventLoop();
            loop.schedule(new Runnable() {
                @Override
                public void run() {
                    LogUtils.i("Reconnecting to: " + session.getNettyUrl() + ':' + session.getNettyPort());
                    nettyClient.start();
                }
            }, 5L, TimeUnit.SECONDS);
        } catch (Exception ex) {
            ex.toString();
        }
    }
}
