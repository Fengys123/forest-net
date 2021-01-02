package com.fnet.common.service;

import com.fnet.common.transfer.MultiChannelTransfer;
import com.fnet.common.transfer.SingleChannelTransfer;
import com.fnet.common.transfer.Transfer;
import com.fnet.common.transfer.protocol.Message;
import com.fnet.common.transfer.protocol.MessageType;

public abstract class AbstractSender implements Sender {

    private static final Transfer DEFAULT_TRANSFER = new SingleChannelTransfer();
    private static final Transfer MULTI_TRANSFER = new MultiChannelTransfer();

    public static final Message HEART_BEAT_MESSAGE = new Message(MessageType.HEART_BEAT);

    protected Transfer transfer = DEFAULT_TRANSFER;

    public Transfer getTransfer() {
        return transfer;
    }

    public void setMultiTransfer() {
        this.transfer = MULTI_TRANSFER;
    }

    public void sendMessageToTransferChannel(Message message) {
        transfer.transferData(message);
    }
}
