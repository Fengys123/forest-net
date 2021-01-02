package com.fnet.inner.server.messageResolver;

import com.fnet.common.config.Config;
import com.fnet.common.net.TcpServer;
import com.fnet.common.transfer.protocol.Message;
import com.fnet.common.transfer.protocol.MessageResolver;
import com.fnet.common.transfer.protocol.MessageType;
import com.fnet.inner.server.handler.HelpCloseHandler;
import com.fnet.inner.server.service.ContactOfOuterToInnerChannel;
import com.fnet.inner.server.service.InnerSender;
import com.fnet.inner.server.handler.MonitorRealServerHandler;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.bytes.ByteArrayDecoder;
import io.netty.handler.codec.bytes.ByteArrayEncoder;

public class TransferResolver implements MessageResolver {

    private static EventLoopGroup eventLoopGroup = new NioEventLoopGroup(4);

    @Override
    public void resolve(Message message) throws InterruptedException {
        int outerChannelId;
        Channel innerChannel;

        outerChannelId = message.getOuterChannelId();
        innerChannel = ContactOfOuterToInnerChannel.getInstance().getInnerChannel(outerChannelId);

        if (innerChannel != null) {
            InnerSender.getInstance().sendBytesToRealServer(message);
        } else {
            new TcpServer(){
                @Override
                public void doSomeThingAfterConnectSuccess(Channel channel) {
                    ContactOfOuterToInnerChannel.getInstance().addToMap(message.getOuterChannelId(), channel);
                    InnerSender.getInstance().sendBytesToRealServer(channel, message);
                }
            }.startConnect(Config.REAL_SERVER_ADDRESS, Config.REAL_SERVER_PORT, eventLoopGroup, new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel ch) throws Exception {
                    ch.pipeline().addLast(new ByteArrayDecoder(),
                                          new ByteArrayEncoder(),
                                          new MonitorRealServerHandler(message),
                                          new HelpCloseHandler(eventLoopGroup));
                }
            }, 1);
        }
    }

    @Override
    public boolean isSupport(Message message) {
        return message.getType() == MessageType.TRANSFER_DATA;
    }
}
