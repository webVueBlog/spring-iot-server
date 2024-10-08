package org.dada.iot.event;

import io.netty.channel.Channel;

/**
 * 协议处理的接口
 
 */
public interface IotEventHandler {

    /**
     * 事件处理
     * @param channel socket channel
     * @param message 数据包
     */
    void onEvent(Channel channel, String message);

}
