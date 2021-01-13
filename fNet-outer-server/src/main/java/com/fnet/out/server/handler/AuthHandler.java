package com.fnet.out.server.handler;

import com.fnet.common.service.Sender;
import com.fnet.common.transfer.protocol.Message;
import com.fnet.out.server.domainCenter.DomainDataService;
import com.fnet.out.server.domainCenter.DomainInfo;
import com.fnet.out.server.authCenter.AuthService;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.net.SocketAddress;

@Slf4j
@Sharable
public class AuthHandler extends SimpleChannelInboundHandler<Message> {

    Sender sender;
    AuthService authService;
    DomainDataService domainDataService;

    public AuthHandler(Sender sender, AuthService authService, DomainDataService domainDataService) {
        this.sender = sender;
        this.authService = authService;
        this.domainDataService = domainDataService;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Message msg) throws Exception {
        try {
            Channel channel = ctx.channel();
            SocketAddress socketAddress = ctx.channel().remoteAddress();

            if (socketAddress instanceof InetSocketAddress) {
                InetSocketAddress remoteAddress = (InetSocketAddress)socketAddress;
                DomainInfo domainInfo = null;
                boolean isRegisterSuccess =
                        authService.registerAuth(msg, remoteAddress) && (domainInfo = issueAndSetupDomain(remoteAddress, channel)) != null;
                if (isRegisterSuccess) {
                    sender.sendRegisterResponseMessage(true, domainInfo.getDomainName().getBytes() , channel);
                } else {
                    sender.sendRegisterResponseMessage(false, null, channel);
                    channel.close();
                }
            }
        } finally {
            log.debug("remove auth handler in pipeline!");
            ctx.pipeline().remove(this);
        }
    }

    private DomainInfo issueAndSetupDomain(InetSocketAddress remoteAddress, Channel transferChannel) {
        DomainInfo domainInfo = domainDataService.issueDomain();
        if (domainInfo == null)     return null;
        domainInfo.setAvailable(false);
        domainInfo.setBindClient(true);
        domainInfo.setBindClientIp(remoteAddress.getAddress().getHostAddress());
        domainInfo.setTransferChannel(transferChannel);
        return domainInfo;
    }
}
