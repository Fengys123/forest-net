package com.fnet.inner.server;

import com.fnet.common.codec.MessageDecoder;
import com.fnet.common.codec.MessageEncoder;
import com.fnet.common.config.Config;
import com.fnet.common.config.cmd.CmdConfigService;
import com.fnet.common.net.TcpServer;
import com.fnet.common.service.Sender;
import com.fnet.common.transfer.Resolver;
import com.fnet.inner.server.handler.KeepAliveHandler;
import com.fnet.inner.server.handler.MonitorOuterServerHandler;
import com.fnet.inner.server.handler.RegisterHandler;
import com.fnet.inner.server.messageResolver.ResolverContext;
import com.fnet.inner.server.service.InnerSender;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import org.apache.commons.cli.*;

import static com.fnet.common.net.TcpServer.*;

public class InnerServerApp {

    public static void main(String[] args) throws InterruptedException, ParseException {

        new CmdConfigService().setInnerServerConfig(args);

        if (Config.isInnerServerConfigComplete()) {

            Sender sender = InnerSender.getInstance();
            Resolver resolver = ResolverContext.getInstance();

            // create a channel to register, when register success, then create other channels
            new TcpServer().startConnect(Config.OUTER_SERVER_ADDRESS , Config.OUTER_SERVER_PORT, CONNECT_OUTER_SERVER_EVENTLOOP_GROUP, new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel ch) {
                    ChannelPipeline pipeline = ch.pipeline();
                    pipeline.addLast("messageEncoder", new MessageEncoder());
                    pipeline.addLast("messageDecoder", new MessageDecoder());
                    pipeline.addLast("idleCheckHandler",  new IdleStateHandler(0, 5, 0));
                    pipeline.addLast("registerHandler", new RegisterHandler());
                    pipeline.addLast("keepAliveHandler", new KeepAliveHandler());
                    pipeline.addLast("monitorOuterServerHandler", new MonitorOuterServerHandler(sender, resolver));
                }
            }, Config.TRANSFER_CHANNEL_NUMBERS);
        }
    }
}
