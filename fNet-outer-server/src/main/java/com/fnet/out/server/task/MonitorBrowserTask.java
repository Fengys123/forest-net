package com.fnet.out.server.task;

import com.fnet.common.config.OuterServerConfig;
import com.fnet.common.net.NetService;
import com.fnet.common.service.Sender;
import com.fnet.out.server.domainCenter.DomainDataService;
import com.fnet.out.server.handler.CheckHostHandler;
import com.fnet.out.server.handler.MonitorBrowserHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.traffic.GlobalTrafficShapingHandler;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MonitorBrowserTask implements Runnable {
    Sender sender;
    DomainDataService domainDataService;
    NetService netService;

    OuterServerConfig config;
    EventLoopGroup bossGroup;
    EventLoopGroup workGroup;

    public MonitorBrowserTask(Sender sender, DomainDataService domainDataService, NetService netService, OuterServerConfig config, EventLoopGroup bossGroup, EventLoopGroup workGroup) {
        this.sender = sender;
        this.domainDataService = domainDataService;
        this.netService = netService;
        this.config = config;
        this.bossGroup = bossGroup;
        this.workGroup = workGroup;
    }

    private void startMonitorBrowserAsync() {
        try {
            netService.startMonitor(config.getOspForBrowser(), new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel ch) throws Exception {
                    ChannelPipeline pipeline = ch.pipeline();
                    if (config.getWriteLimit() != 0 || config.getReadLimit() != 0) {
                        pipeline.addLast(new GlobalTrafficShapingHandler(new NioEventLoopGroup(), config.getWriteLimit(), config.getReadLimit(), 1000, 1000));
                    }
                    pipeline.addLast(new CheckHostHandler(domainDataService), new MonitorBrowserHandler(sender));
                }
            }, bossGroup, workGroup);
        } catch (Exception e) {
            log.info("monitor browser failed!");
        }
    }

    @Override
    public void run() {
        startMonitorBrowserAsync();
    }
}
