package org.dada.iot.protocol;

import lombok.Data;



@Data
public class LoginProtocol extends ProtocolBase{

    private String version;

    private String devId;

    private String sign;



}
