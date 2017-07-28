package com.hebta.plato.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * @MessageMapping：需要在此 value 的值前加上WebSocketConfig注册的
 * 		ApplicationDestinationPrefixes（如果有），就构成了整个请求的路径。
 * @SendTo: value 是指服务端将把消息发送到订阅了这个路径的所有客户端
 * 
 * 用法：1. 根据当前配置，客户端使用Stomp，stompClient.send("/frontend/input", {}, obj);
 * 		发送 obj 给服务端，服务端调用 showLog()处理，然后将处理的结果转发给所有通过 
 * 		stompClient.subscribe('/backend/output', function (response) { ... })
 * 		订阅了服务端暴露的接口的客户端。           
 * 		2. 如果服务端需要在运行时，根据需要自行把信息推送给前端，则需要使用
 * 		SimpMessagingTemplate的convertAndSend()主动调用广播端口，也就是@SendTo的值
 * 
 * @author 雷兆金
 */
@RestController
public class PlatoWebSocketController {	
	@MessageMapping("/request") 
	@SendTo("/backend/{userId}/broadcast")  
	public String showLog(@DestinationVariable Long userId, String receivedMsg) {
		return userId + "=====Handled Msg === " + receivedMsg;
	}
	
	//----------服务端自己直接调用，达到主动推送消息给客户端----------
	@Autowired
    private SimpMessagingTemplate template;
	
	@RequestMapping(value = "ws/message", method = RequestMethod.POST)
	public Integer pushMessage(@RequestBody String txtMsg){
		int startIndex = txtMsg.indexOf("] - ");
		int endIndex = txtMsg.indexOf("@");
		if (startIndex < 0 || endIndex < 0){
			return -1;
		}
		String userId = txtMsg.substring(startIndex + 4, endIndex);
		String destination = "/backend/" + userId + "/broadcast";
		template.convertAndSend(destination, txtMsg.substring(endIndex + 1));
		return 1;
	}
}
