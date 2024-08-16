package org.dada.iot.handler;

import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelConfig;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.socket.DefaultSocketChannelConfig;
import org.dada.iot.session.SessionManager;
import org.springframework.stereotype.Component;

/**
 * 初始化 channel时做的一些事情
 * 线程安全的
 
 */
@Component
@ChannelHandler.Sharable
public class InitChannelHandler extends ChannelInboundHandlerAdapter {

    /**
     * 会话管理
     */
    SessionManager sessionManager;

    /**
     * 构造函数
     * @param sessionManager
     */
    public InitChannelHandler(SessionManager sessionManager){
        this.sessionManager = sessionManager;
    }

    /**
     * 当有客户端注册时调用
     * @param ctx ChannelHandlerContext
     */
    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        ChannelConfig config = ctx.channel().config();
        DefaultSocketChannelConfig socketConfig = (DefaultSocketChannelConfig)config;
        // 此处三个参数决定 延迟情况
        // 连接时间 、往返延迟、 带宽。
        // 这三个参数设置的是权重
        // 因为我的连接会保持住 长连接不会频繁断开，所以 把连接时间权限设置的最低为0
        // 因为我们对往返延迟有一些容忍度，所以 第二参数是1
        // 对于带宽我们会有更大的需求，第三个参数设置为2 这就是目前的权重比
        // 延迟和带宽的性能是互斥的 , 延迟低 , 就意味着很小的包就要发送一次 , 其带宽就低了 , 延迟高了 , 每次积累很多数据才发送 , 其带宽就相应的提高了
        socketConfig.setPerformancePreferences(0,1,2);//setPerformancePreferences表示性能参数的优先级，参数分别是：连接时间、延迟、带宽
        // NioSocketChannel在工作过程中，使用PooledByteBufAllocator来分配内存
        socketConfig.setAllocator(PooledByteBufAllocator.DEFAULT);// 设置缓冲区分配器 setAllocator表示缓冲区分配器，Netty默认使用的是PooledByteBufAllocator
        super.channelRegistered(ctx);

        // 增加会话
        sessionManager.addSession(ctx.channel()); // 添加到会话管理器中
    }
}
