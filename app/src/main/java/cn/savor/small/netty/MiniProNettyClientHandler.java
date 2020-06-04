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


import android.content.Context;

import com.google.gson.Gson;
import com.savor.ads.core.Session;
import com.savor.ads.utils.AppUtils;
import com.savor.ads.utils.ConstantValues;
import com.savor.ads.utils.LogFileUtil;
import com.savor.ads.utils.LogUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
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
public class MiniProNettyClientHandler extends SimpleChannelInboundHandler<MessageBean> {
    private MiniProNettyClient.MiniNettyMsgCallback miniCallback;
    private Session session;
    private Context mContext;
    private MiniProNettyClient client;


    public MiniProNettyClientHandler(MiniProNettyClient client,MiniProNettyClient.MiniNettyMsgCallback m, Context context) {
        this.miniCallback = m;
        this.client = client;
        this.mContext = context;
        this.session = Session.get(context);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        LogUtils.i("miniProgram--channelActive ");
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, MessageBean msg) throws Exception {
        LogUtils.i(("mini Client received: " + msg.getCmd()));
        if (msg == null) return;
        /**来自服务端的数据包请求*/
        MessageBean.Action Order = msg.getCmd();
        switch (Order) {
            case HEART_SERVER_TO_CLIENT:
                List<String> contentMsgH = msg.getContent();
                for (String tmp : contentMsgH) {
                    LogUtils.v("miniProgram--SERVER_HEART_RESP： 收到来自服务端的...心跳回应." + tmp + "===>>接收到内容:" + msg.getContent());
                }
                if (!session.isHeartbeatMiniNetty()){
                    if (miniCallback != null) {
                        miniCallback.onMiniConnected();
                    }
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

                    if (ConstantValues.NETTY_MINI_PROGRAM_COMMAND.equals(order)){
                        LogUtils.d("Netty command: contentMsg " + contentMsg);
                        if (miniCallback!=null&&contentMsg.size()>2){
                            miniCallback.onReceiveMiniServerMsg(order,contentMsg.get(2));
                        }
                    }
                }
                ctx.writeAndFlush(response);
                break;
            default:
                break;
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
                LogFileUtil.write("MiniNettyClientHandler READER_IDLE");


            } else if (event.state().equals(IdleState.WRITER_IDLE)) {

                LogUtils.i("WRITER_IDLE");
                LogFileUtil.write("MiniNettyClientHandler WRITER_IDLE");
                try {
                    // 发送心跳消息
                    MessageBean message = new MessageBean();
                    message.setCmd(MessageBean.Action.HEART_CLENT_TO_SERVER);
                    String number = channelId + System.currentTimeMillis();
                    message.setSerialnumber(number);
                    message.setIp(AppUtils.getLocalIPAddress());
                    message.setMac(session.getEthernetMac());
                    InnerBean bean = new InnerBean();
                    bean.setHotelId(session.getBoiteId());
                    bean.setRoomId(session.getRoomId());
                    bean.setSsid(AppUtils.getShowingSSID(mContext));
                    bean.setBoxId(session.getBoxId());
                    ArrayList<String> contList = new ArrayList<>();
                    contList.add("I am a mini Heart Pakage...");
                    contList.add(new Gson().toJson(bean));
                    message.setContent(contList);
                    ChannelFuture future = ctx.writeAndFlush(message);
                    if (!future.isSuccess()) {
                        close(ctx);
                    }
                    LogUtils.v("miniProgram--WRITER_IDLE====" + channelId + "====>>>>心跳包...流水号:" + message.getSerialnumber());
                    LogUtils.v("miniProgram--future.isSuccess()====" + future.isSuccess());
                }catch (Exception e){
                    e.printStackTrace();
                }
            } else if (event.state().equals(IdleState.ALL_IDLE)) {
                //未进行读写
                //客户端发起REQ心跳查询==========
                LogUtils.i("ALL_IDLE");
                close(ctx);
            }

        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        if (miniCallback!=null){
            miniCallback.onMiniCloseIcon();
        }
        Session.get(mContext).setHeartbeatMiniNetty(false);
        LogUtils.i("miniProgram--exceptionCaught客户端出现异常，退出........" + session.getNettyUrl() + ':' + session.getNettyPort());
        close(ctx);
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
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
        super.channelUnregistered(ctx);
        LogUtils.i("miniProgram--channelUnregistered......." + session.getNettyUrl() + ':' + session.getNettyPort());

    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        LogUtils.i("miniProgram--channelInactive......." + session.getNettyUrl() + ':' + session.getNettyPort());Session.get(mContext).setHeartbeatMiniNetty(false);
        if (miniCallback!=null){
            miniCallback.onMiniCloseIcon();
        }
        Session.get(mContext).setHeartbeatMiniNetty(false);
        close(ctx);
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
                    client.start();
                }
            }, 60L, TimeUnit.SECONDS);
        } catch (Exception ex) {
            ex.toString();
        }
    }
}
