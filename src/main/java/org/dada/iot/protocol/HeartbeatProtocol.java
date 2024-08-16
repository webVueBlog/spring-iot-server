package org.dada.iot.protocol;

import org.dada.iot.event.EventType;

/**
 * 心跳包
 
 */
public class HeartbeatProtocol extends ProtocolBase{

    public HeartbeatProtocol(){
        this.msgType = EventType.HEART_BEAT;
        this.txNo = System.currentTimeMillis();
    }
}
