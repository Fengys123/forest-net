package com.fnet.out.server.domainCenter;

import io.netty.channel.Channel;

/**
 * @author fys
 */
public interface DomainDataService {

    /**
     * ��ʼ��Domain����
     */
    void initData();

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
