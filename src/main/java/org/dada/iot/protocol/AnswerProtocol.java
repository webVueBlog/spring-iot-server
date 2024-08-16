package org.dada.iot.protocol;

import lombok.Data;

/**
 * 应答协议包
 
 */
@Data
public class AnswerProtocol extends ProtocolBase {

    /**
     * 响应结果
     */
    protected String result;
}
