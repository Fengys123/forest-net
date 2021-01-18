package com.fnet.inner.server.task;

import com.fnet.common.config.Config;
import com.fnet.common.net.NetService;
import com.fnet.common.service.Sender;
import com.fnet.common.transfer.protocol.Message;
import com.fnet.inner.server.handler.MonitorRealServerHandler;
import com.fnet.inner.server.messageResolver.TransferResolver;
import com.fnet.inner.server.sender.TransferCache;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.bytes.ByteArrayDecoder;
import io.netty.handler.codec.bytes.ByteArrayEncoder;
import lombok.SneakyThrows;

import java.util.concurrent.LinkedBlockingQueue;

public class SendMessageToRealServerTask implements Runnable {

    LinkedBlockingQueue<Message> messageQueue = TransferResolver.MESSAGE_QUEUE;

    Sender sender;
    EventLoopGroup workGroup;
    NetService netService;

    public SendMessageToRealServerTask(Sender sender, NetService netService, EventLoopGroup workGroup) {
        this.sender = sender;
        this.workGroup = workGroup;
        this.netService = netService;
    }

    @SneakyThrows
    @Override
    public void run() {
        while (true) {
            Message message = messageQueue.take();
            int outerChannelId;
            Channel innerChannel;

            outerChannelId = message.getOuterChannelId();
            innerChannel = TransferCache.getInnerChannel(outerChannelId);

            if (innerChannel != null) {
                sender.sendBytesToRealServer(message);
            } else {
                Channel channel =
                        netService.startConnect(Config.REAL_SERVER_ADDRESS, Config.REAL_SERVER_PORT,
                                                                 new ChannelInitializer<SocketChannel>() {
                                                                   @Override
                                                                   protected void initChannel(SocketChannel ch)
                                                                           throws Exception {
                                                                       ch.pipeline().addLast(new ByteArrayDecoder(),
                                                                                             new ByteArrayEncoder(),
                                                                                             new MonitorRealServerHandler(
                                                                                                     message, sender));
                                                                   }
                                                               }, workGroup);
                TransferCache.addToMap(message.getOuterChannelId(), channel);
                sender.sendBytesToRealServer(channel, message);
            }
        }
    }
}
