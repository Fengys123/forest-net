package com.fnet.out.server.domainCenter;

import io.netty.channel.Channel;

import java.net.UnknownHostException;

/**
 * @author fys
 */
public interface DomainDataService {

    /**
     * ��ʼ��Domain����
     */
    void initData() throws Exception;

    /**
     * �䷢һ������
     */
    DomainInfo issueDomain();

    /**
     * ����һ������
     */
    void recoveryDomainByTransferChannel(Channel channel);

    /**
     * ����������ȡtransfer channel
     */
    Channel getTransferChannelByDomainName(String domainName);
}
