package com.fnet.out.server.handler;

import com.fnet.common.service.Sender;
import com.fnet.common.transfer.protocol.Message;
import com.fnet.common.transfer.protocol.MessageType;
import com.fnet.out.server.sender.TransferCache;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import lombok.extern.slf4j.Slf4j;

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
        int outerChannelId = ctx.channel().hashCode();
        ByteBuf byteBuf = (ByteBuf)msg;

        Message message = new Message(MessageType.TRANSFER_DATA, outerChannelId, byteBuf);
        sender.sendMessageToTransferChannelNoFlush(message);
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        sender.flush(ctx.channel().hashCode());
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        log.debug("A channel disconnect browser!");
        TransferCache.removeOuterChannel(ctx.channel());
    }
}
