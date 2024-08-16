package org.dada.iot;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.json.JsonObjectDecoder;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.extern.slf4j.Slf4j;
import org.dada.iot.event.MainEventProducer;
import org.dada.iot.handler.*;
import org.dada.iot.session.SessionManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.concurrent.TimeUnit;

/**
 * 引导程序，同时我们要实现CommandLineRunner接口。
 * 同时实现run方法，会在所有 Spring Beans 都初始化之后，SpringApplication.run() 之前执行
 
 */
@Slf4j
@SpringBootApplication
public class IotApplication implements CommandLineRunner {

	/**
	 * 主事件，负责连接。单一线程就行
	 */
	private final EventLoopGroup bossGroup = new NioEventLoopGroup(1);
	/**
	 * 负责处理业务,不设置线程数时为CPU核心*2.如果运行在容器状态下会不准，建议手动设置
	 */
	private final EventLoopGroup workerGroup = new NioEventLoopGroup(2);
	/**
	 * 服务监听的端口
	 */
	private final static int SERVER_PORT = 4100;

	/**
	 * 状态 handler
	 */
	@Autowired
	StatusPringHandler statusPringHandler;

	/**
	 * 会话管理
	 */
	@Autowired
	SessionManager sessionManager;


	@Autowired
	IdleCheckHandler idleCheckHandler;//表示空闲检测

	@Autowired
	MainEventProducer mainEventProducer;//主事件生产者

	@Autowired
	QuitHandler quitHandler;//退出处理

	@Autowired
	IotProtocolHandler iotProtocolHandler;//协议处理

	@Autowired
	InitChannelHandler initChannelHandler;//初始化通道


	public static void main(String[] args) {
		SpringApplication.run(IotApplication.class, args);
	}

	/**
	 * 实现自定义的run方法
	 * @param args 输入的参数
	 * @throws Exception 抛出异常
	 */
	@Override
	public void run(String... args) throws Exception {
		try{
			ServerBootstrap bootstrap = new ServerBootstrap();
			bootstrap
					.group(bossGroup, workerGroup)
					// 这里还可以支持其他的实现，
					// 比如在Linux下可以用基于EpollServerSocketChannel
					// 在mac下可以使用KQueueServerSocketChannel
					// 在这里我们用比较通用的NioServerSocketChannel实现
					.channel(NioServerSocketChannel.class)
					// option针对boss线程
					// 是否可重复使用地址和端口？docker环境中可以设置为false
					.option(ChannelOption.SO_REUSEADDR,true)//SO_REUSEADDR表示是否可重复使用地址和端口，true表示可以
					// 等待处理的队列大小，
					.option(ChannelOption.SO_BACKLOG, 400)//SO_BACKLOG表示等待处理的队列大小，400表示队列大小为400
					// 接收缓存区
					.option(ChannelOption.SO_RCVBUF,64*1024)//SO_RCVBUF表示接收缓存区大小，64k
					// childOption针对work线程
					.childOption(ChannelOption.TCP_NODELAY, true)//TCP_NODELAY表示是否开启Nagle算法，true表示关闭
					.childOption(ChannelOption.SO_KEEPALIVE, false)//SO_KEEPALIVE表示是否开启TCP心跳机制，false表示关闭
					// 阻塞客户的close函数，尽可能的发送数据
					.childOption(ChannelOption.SO_LINGER,0)//表示关闭时，延迟关闭的时间，0表示立即关闭,SO_LINGER=0 表示关闭时，延迟关闭的时间，0表示立即关闭
					.childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)// 设置缓冲区分配器, ALLOCATOR表示缓冲区分配器，PooledByteBufAllocator.DEFAULT表示使用默认的缓冲区分配器
					.childHandler(new ChannelInitializer<SocketChannel>() {
						@Override
						public void initChannel(SocketChannel ch) {
							ch.pipeline()
									// netty要求ChannelHandler是每个线程一份的，就算指定bean的scope是原型也无效。
									// 这里有三种解决方案
									// 1. 每次都是new的，但把需要依赖spring完成初始化的传参进去
									// 2. 使用一个ApplicationContextHolder工具类，在handler中通过applicationContext.getBean来获取
									// 3. 如果能保证线程安全的情况下 给ChannelHandler增加@Sharable注解
									// 增加一个json解码的
//									.addLast("decoder", new DecoderHandler(sessionManager, mainEventProducer))
									.addLast("initChannel",initChannelHandler)//初始化通道
									.addLast("jsonDecoder", new JsonObjectDecoder())// 增加一个json解码的
									.addLast("iotProtocol", iotProtocolHandler)// 增加一个协议处理
									// 编码器
									.addLast("encoder", new EncoderHandler())// 增加一个json编码的
									// 处理客户端退出时的事件
									.addLast("quit", quitHandler)// 增加一个退出处理
									// 链路 head <=> IdleState <=> decoder <=> idleCheck <=> tail
									// 入站 从head到tail ，出站 从tail到head
									// 增加空闲检查器，规定读写各30秒没操作时触发
									.addLast("IdleState", new IdleStateHandler(30,30,0))//IdleStateHandler 表示空闲检查器，30表示读空闲时间，30表示写空闲时间，0表示读写空闲时间
									//自定义实现的空闲处理
									.addLast("idleCheck", idleCheckHandler);// 自定义实现的空闲处理
						}
					});

			// 为worker组设置一个定时器
			workerGroup.next().scheduleAtFixedRate(statusPringHandler,1, 60, TimeUnit.SECONDS);//定时器，每60秒执行一次,statusPringHandler表示定时器任务 scheduleAtFixedRate表示定时器任务，1表示延迟1秒执行，60表示每60秒执行一次
			// 绑定端口
			ChannelFuture channelFuture = bootstrap.bind(SERVER_PORT).sync();
			log.info("Server start listen port :" + SERVER_PORT);
			channelFuture.channel().closeFuture().sync();// 等待服务端监听端口关闭
		}finally {
			workerGroup.shutdownGracefully();// 在应用结束的时候，我们同时还要结束掉 worker和boos两个事件循环
			bossGroup.shutdownGracefully();// 在应用结束的时候，我们同时还要结束掉 worker和boos两个事件循环
		}

		// 在应用结束的时候，我们同时还要结束掉 worker和boos两个事件循环
		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
				log.info("程序结束，准备结束两个event loop");
				workerGroup.shutdownGracefully();
				bossGroup.shutdownGracefully();
			}
		));

	}
}
