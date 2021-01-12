package com.fnet.out.server.handler;

import com.fnet.common.service.Sender;
import com.fnet.common.transfer.protocol.Message;
import com.fnet.common.transfer.protocol.MessageType;
import com.fnet.out.server.sender.TransferCache;
import io.netty.channel.*;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

@Slf4j
public class MonitorBrowserHandler extends ChannelInboundHandlerAdapter {

    Sender sender;

    public MonitorBrowserHandler(Sender sender) {
        this.sender = sender;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        log.debug("A channel connect browser!");
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        byte[] bytes;
        int outerChannelId;

        bytes = (byte[])msg;
        outerChannelId = ctx.channel().hashCode();

        Message message = new Message(MessageType.TRANSFER_DATA, outerChannelId, bytes);
        sender.sendMessageToTransferChannel(message);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        log.debug("A channel disconnect browser!");
        TransferCache.removeOuterChannel(ctx.channel());
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
            throws Exception {
        if (cause instanceof IOException) {
            if ("Զ������ǿ�ȹر���һ�����е����ӡ�".equals(cause.getMessage())) {
                log.info("Զ������ǿ�ȹر���һ�����е����ӡ�");
                return;
            }
        }
        ctx.fireExceptionCaught(cause);
    }
}
