package org.dada.iot.protocol;

import lombok.Data;
import org.dada.iot.event.EventType;


@Data
public class ProtocolBase {

    /**
     * 消息类型
     */
    protected EventType msgType;

    /**
     * 通讯号
     */
    protected long txNo;
}
