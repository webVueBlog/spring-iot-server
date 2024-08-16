package org.dada.iot.event;

import io.netty.channel.Channel;
import lombok.Data;


@Data
public class EventInfo {
    /**
     * 事件类型
     */
    private EventType eventType;

    /**
     *渠道
     */
    private Channel channel;

    /**
     * 协议原文
     */
    private String message;
}
