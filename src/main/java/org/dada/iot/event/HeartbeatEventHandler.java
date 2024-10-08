package org.dada.iot.event;

import com.google.gson.Gson;
import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;
import org.dada.iot.protocol.AnswerProtocol;
import org.dada.iot.protocol.HeartbeatProtocol;
import org.dada.iot.session.SessionInfo;
import org.dada.iot.session.SessionManager;
import org.springframework.stereotype.Component;

/**
 * 心跳事件处理
 
 */
@Slf4j
@Component
public class HeartbeatEventHandler implements IotEventHandler{


    /**
     * 会话管理
     */
    private final SessionManager sessionManager;

    public HeartbeatEventHandler(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }

    /**
     * 事件处理
     *
     * @param channel socket channel
     */
    @Override
    public void onEvent(Channel channel, String message) {
        // 检查下客户端是否登录，如果没登录，要踢掉
        // 对于 心跳包自己检查完毕即可，不用通知后端业务
        SessionInfo sessionInfo = sessionManager.getSession(channel);
        if(null == sessionInfo){
            // 没有会话信息，直接踢掉
            channel.close();
        }else if( null == sessionInfo.getDevId() || sessionInfo.getDevId().isEmpty()){
            sessionManager.removeSession(channel);
            channel.close();
        } else {
            HeartbeatProtocol heartbeatProtocol = new Gson().fromJson(message, HeartbeatProtocol.class);

            AnswerProtocol answerProtocol = new AnswerProtocol();
            answerProtocol.setTxNo(heartbeatProtocol.getTxNo());
            answerProtocol.setMsgType(EventType.HEART_BEAT_ANSWER);
            channel.writeAndFlush(answerProtocol);
        }


    }
}
