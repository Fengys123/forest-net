package com.fnet.out.server.handler;

import com.fnet.common.config.Config;
import com.fnet.common.handler.AbstractMonitorHandler;
import com.fnet.common.net.TcpServer;
import com.fnet.common.service.AbstractSender;
import com.fnet.common.service.Sender;
import com.fnet.common.service.ThreadPoolUtil;
import com.fnet.common.transfer.AbatractTransfer;
import com.fnet.common.transfer.protocol.MessageResolver;
import com.fnet.out.server.service.AuthService;
import com.fnet.out.server.service.OuterChannelDataService;
import com.fnet.out.server.service.OuterSender;
import com.sun.org.apache.xpath.internal.operations.Mult;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.bytes.ByteArrayDecoder;
import io.netty.handler.codec.bytes.ByteArrayEncoder;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.CompletableFuture;

import static com.fnet.common.net.TcpServer.*;

@Slf4j
@Sharable
public class MonitorInnerServerHandler extends AbstractMonitorHandler {

    private volatile boolean isMonitorBrower = false;

    AuthService authService;

    public MonitorInnerServerHandler(Sender sender, MessageResolver resolver, AuthService authService) {
        super(sender, resolver);
        this.authService = authService;
    }

    @Override
    public void doSomethingAfterAllTransferChannelActive() {
        if (!isMonitorBrower) {
            log.info("Start monitor browser!");
            startMonitorBrowserAsync();
            isMonitorBrower = true;
        }
    }

    /**
     * ���ȫ��tansferChannelʧЧ�Ļ�, ����Ҫ�ͷź������������,�������ע����Ϣ, ���Ҵ�OuterChannelDataService��list��Ϣ��ɾ����Ӧchannel��Ϣ.
     */
    @Override
    public void doSomethingAfterAllTransferChannelInactive() {
        log.info("All transfer channel disconnect, start clean work!");
        OuterChannelDataService.getInstance().clear();
        authService.clearRegisterAuthInfo();
    }

    private void startMonitorBrowserAsync() {
        CompletableFuture.runAsync(()-> {
            try {
                new TcpServer().startMonitor(Config.OUTER_REMOTE_PORT, new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ch.pipeline().addLast(new ByteArrayEncoder(),
                                              new ByteArrayDecoder(),
                                              new MonitorBrowserHandler(sender));
                    }
                }, MONITOR_BROWSER_BOSS_EVENTLOOP_GROUP, MONITOR_BROWSER_WORK_EVENTLOOP_GROUP);
            } catch (InterruptedException e) {
                log.info("monitor browser failed!");
                e.printStackTrace();
            }
        }, ThreadPoolUtil.getCommonExecutor());
    }
}
