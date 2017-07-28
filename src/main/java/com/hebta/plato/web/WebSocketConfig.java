package com.hebta.plato.web;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.AbstractWebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig extends AbstractWebSocketMessageBrokerConfigurer {

	@Override
	public void registerStompEndpoints(StompEndpointRegistry stompEndpointRegistry) {
		stompEndpointRegistry.addEndpoint("/platoEndpoint")  // 客户端连接服务端的端点
			.setAllowedOrigins("*") // 不设置前台连接时报 403 错误
			.withSockJS(); // 开启SockJS支持
	}

	@Override
	public void configureMessageBroker(MessageBrokerRegistry registry) {
		registry.enableSimpleBroker("/backend"); // 客户端订阅地址的前缀
		registry.setApplicationDestinationPrefixes("/frontend"); // 客户端请求服务端的前缀
	}
}
