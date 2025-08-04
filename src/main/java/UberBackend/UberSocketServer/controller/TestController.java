package UberBackend.UberSocketServer.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;

import UberBackend.UberSocketServer.dto.ChatRequest;
import UberBackend.UberSocketServer.dto.ChatResponse;
import UberBackend.UberSocketServer.dto.TestRequest;
import UberBackend.UberSocketServer.dto.TestResponse;


@Controller
public class TestController {

	
    private SimpMessagingTemplate messagingTemplate;
    
    public TestController(SimpMessagingTemplate messagingTemplate) {
    	this.messagingTemplate=messagingTemplate;
    }
	@MessageMapping("/ping")
	@SendTo("/topic/ping")
	public TestResponse pigCheck(TestRequest message) {
		System.out.println("recieved message from client "+message.getData());
		return TestResponse.builder()
                .data("received")
                .build();
	}
	
//	  @Scheduled(fixedDelay = 2000)
//	    public void sendPeriodicMessage() {
//	        String message = "Periodic Message from server " + System.currentTimeMillis();
//	        System.out.println("executed periodic function");
//	        messagingTemplate.convertAndSend("/topic/scheduled", message);
//	    }
	
	@MessageMapping("/chat/{room}")
	@SendTo("/topic/message/{room}")
	public ChatResponse chatMessage(@DestinationVariable String room, ChatRequest request) {
		ChatResponse response =  ChatResponse.builder()
				.name(request.getName())
				.message(request.getMessage())
				.timeStamp(""+System.currentTimeMillis())
				.build();
		return response;
	}
	
	@MessageMapping("/privateChat/{room}/{userId}")
//	@SendTo("/topic/privateMessage/{room}/{userId}")
	public void privateChatMessage(@DestinationVariable String room,@DestinationVariable String userId, ChatRequest request) {
		System.out.println(room+" "+userId);
		ChatResponse response =  ChatResponse.builder()
				.name(request.getName())
				.message(request.getMessage())
				.timeStamp(""+System.currentTimeMillis())
				.build();
		messagingTemplate.convertAndSendToUser(userId,"/queue/privateMessage/" + room, response);
	}
}
