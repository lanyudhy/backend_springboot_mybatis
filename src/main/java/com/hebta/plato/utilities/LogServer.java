package com.hebta.plato.utilities;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import javax.net.ServerSocketFactory;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

/**
 * 日志可以有多个处理器，比如常见的Console, File, Socket，我们需要把日志自动地通过
 * WebSocket 推送到前端页面，需要两步：
 * 1. 将 java.util.logging.* 或者 log4j 的日志输出到 SocketHandler，如果希望代码实现：
 * 		Handler handler = new SocketHandler("localhost", 5000);
    	logger.addHandler(handler);
                当然也可以直接在 log4j.properties/logging.properties里面配置实现    
 * 2. 启动一个 WebSocket 连接，监听 SocketHandler 输出的 network 出口，一旦有消息
 * 就广播到所有订阅 WebSocket 代理的客户端。
 * -----------------------------------------------------------------------------
 * 简单的做法，在需要把日志推送到 websocket 的方法里面直接加上上面的代码。如果方法
 * 非常多，可以用切面编程实现。
 * 
 * @author 雷兆金
 *
 */
@Component
public class LogServer implements InitializingBean, DisposableBean {
	private Logger logger = LoggerFactory.getLogger(LogServer.class);	
	
	@Autowired
	private RestTemplate restTemp;
	@Autowired
	private Environment env;
	
	@Value("${websocket.port}")  
    private Integer wsPort;

	// 当Spring应用重启时，需要关掉当前的websocket连接释放端口，所以把websocket句柄设置为实例变量
	private ServerSocket serverSocket;
	
	@Override
	public void destroy() {
		logger.info("Shutdown Socket Service.........");
		try {
			serverSocket.close();
		} catch (IOException e) {
			logger.error("There is an exception when close socket::{}", e);
		}
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		logger.info("进入 LogServer.afterPropertiesSet() 启动 Socket 服务");
		
		// 使用 UTF-8发送 POST 请求
		restTemp.getMessageConverters().add(0, new StringHttpMessageConverter(Charset.forName("UTF-8")));
		
		String wsServerUrl = getWebSocketServerUrl();
		// 必须用线程让 socket 不占用主线程去监听端口，否则主程序没办法起来
		new Thread() {
			public void run() {
				ServerSocketFactory serverSocketFactory = ServerSocketFactory.getDefault();
				try { 
					serverSocket = serverSocketFactory.createServerSocket(wsPort);
				} catch (IOException e) {
					logger.error("Unable to create server ::: {}", e);
					System.exit(-1);
				}
				logger.info("LogServer running on port: {}", wsPort);

				while (true) {
					List<String> list = new ArrayList<>();
					try {
						handleSocket(serverSocket, wsServerUrl, list);
					} catch (Exception e) {
						logger.error("Socket 线程被打断，原因是：：", e);
					}
				}
			}
		}.start();
	}

	private String getWebSocketServerUrl() {
		logger.info("进入 LogServer.getWebSocketServerUrl()");
		String port = env.getProperty("server.port");
		String host = "";
		try {
			host = InetAddress.getLocalHost().getHostAddress();
		} catch (UnknownHostException e1) {
			throw new RuntimeException(e1);
		}
		StringBuffer sb = new StringBuffer();
		sb.append("http://").append(host).append(":").append(port).append("/ws/message");
		String websocketRestUrl = sb.toString();
		
		logger.info("websocketRestUrl ----- {}", websocketRestUrl);
		
		return websocketRestUrl;
	}

	private void handleSocket(ServerSocket serverSocket, String url, List<String> list) throws Exception {
		try (Socket socket = serverSocket.accept()) {
			InputStream is = socket.getInputStream();
			BufferedReader br = new BufferedReader(new InputStreamReader(is, "UTF-8"));
			String line = null;
			while ((line = br.readLine()) != null) {
				line = line.trim();
				int contentStartInd = line.indexOf(">") + 1;
				int contentEndInd = line.lastIndexOf("<");
				
				if (line.contains("date")) {
					if (contentEndInd > 0){ 
						list.add(line.substring(line.indexOf("T") + 1, contentEndInd));
					} else {
						list.add(line.substring(line.indexOf("T") + 1));
					}
					
				} else if (line.contains("level")) {
					if (contentEndInd > 0){ 
						list.add("[ " + line.substring(contentStartInd, contentEndInd) + " ]");
					} else {
						list.add("[ " + line.substring(contentStartInd) + " ]");
					}
					
				} else if (line.contains("message")) {
					if (contentEndInd > 0){ 
						list.add(line.substring(contentStartInd, contentEndInd));
					} else {
						list.add(line.substring(contentStartInd));
					}
				}
				if (list.size() == 3) {
					String message = StringUtils.join(list, " - ");
					
					// 用RestTemplate调用封装Socket处理逻辑的REST接口
					restTemp.postForEntity(url, message, Integer.class);
					
					list = new ArrayList<>(); // 一条日志处理完，清空容器准备接收下一条
				}
			}
		}
	}	
}