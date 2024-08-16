package org.dada.iot.event;

import com.google.gson.Gson;
import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;
import org.dada.iot.protocol.AnswerProtocol;
import org.dada.iot.protocol.LoginProtocol;
import org.dada.iot.session.SessionInfo;
import org.dada.iot.session.SessionManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * 登录事件
 
 */
@Slf4j
@Component
public class LoginEventHandler implements IotEventHandler {



    /**
     * 会话管理
     */
    @Autowired
    SessionManager sessionManager;

    /**
     * 密钥
     */
    @Value("${netty.key}")
    private String key;

    /**
     * 事件处理
     * @param channel socket channel
     * @param message 数据包
     */
    @Override
    public void onEvent(Channel channel, String message) {
        // TODO:验签，验签不通过的直接踢掉

        // 处理业务
        LoginProtocol loginProtocol = new Gson().fromJson(message, LoginProtocol.class);

        // 通过设备 Id 获取旧连接
        Channel oldChannel = sessionManager.getChannel(loginProtocol.getDevId());
        if(null != oldChannel){
            // 已经存在 旧设备，准备先踢出旧设备
            SessionInfo oldSessionInfo= sessionManager.getSession(oldChannel);
            oldChannel.close();
            sessionManager.removeSession(oldChannel);
        }

        sessionManager.setSession(channel, loginProtocol.getDevId(), loginProtocol.getVersion());
        sessionManager.addChannel(loginProtocol.getDevId(), channel);
        // TODO:本地处理完后需要通过 rabbitMQ 一类的网络队列向后通知有新客户端上线事件。
        //  同时需要考虑当有多份前置服务端时下行消息要保证送到正确的MQ队列

        // 向客户端发送应答数据包
        AnswerProtocol answerProtocol = new AnswerProtocol();
        answerProtocol.setMsgType(EventType.CLIENT_REGISTER_ANSWER);
        answerProtocol.setTxNo(loginProtocol.getTxNo());
        channel.writeAndFlush(answerProtocol);



    }
}
