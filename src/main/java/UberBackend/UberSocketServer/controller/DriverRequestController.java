package UberBackend.UberSocketServer.controller;

import java.util.Optional;

import org.springframework.boot.actuate.endpoint.web.annotation.RestControllerEndpoint;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import UberBackend.UberSocketServer.dto.RideRequestDto;
import UberBackend.UberSocketServer.dto.RideResponseDto;
import UberBackend.UberSocketServer.dto.UpdateBookingRequestDto;
import UberBackend.UberSocketServer.dto.UpdateBookingResponseDto;
import UberBackend.UberSocketServer.producers.KafkaProducerService;

@RestController
@RequestMapping("/api/socket")
public class DriverRequestController {


   private SimpMessagingTemplate messagingTemplate;
   private RestTemplate restTemplate;
    private final KafkaProducerService kafkaProducerService;
    public DriverRequestController(SimpMessagingTemplate messagingTemplate,
    		RestTemplate restTemplate,
    		 KafkaProducerService kafkaProducerService) {
    	this.messagingTemplate=messagingTemplate;
    	
    	this.restTemplate=restTemplate;
    	this.kafkaProducerService=kafkaProducerService;
    }
    
    @GetMapping
    public Boolean help() {
    	kafkaProducerService.publishMessage("sample-topic", "Hello");
		return true;
    	
    }
    
    @PostMapping("/newride")
    public ResponseEntity<Boolean> raiseRideRequest(@RequestBody RideRequestDto requestDto) {
        try {
            sendDriverNewRideRequest(requestDto);
            return ResponseEntity.ok(true);
        } catch (Exception e) {
            e.printStackTrace(); // <--- print root cause
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(false);
        }
    }

    
    public void sendDriverNewRideRequest(RideRequestDto requestDto) {
     
        //ideally request should only go to nearby driver but for simplicity we send it to everyone 
        messagingTemplate.convertAndSend("/topic/rideRequest", requestDto);
    }
    
    @MessageMapping("/rideResponse/{userId}")
    public synchronized void rideResponseHandler(@DestinationVariable String userId, RideResponseDto rideResponseDto) {

        // This driver gets the ride
        System.out.println("Driver " + userId + " accepted ride from passenger " + userId);
        // Notify frontend or update booking in DB
        // e.g., assign driver to booking, change ride status
         UpdateBookingRequestDto requestDto = UpdateBookingRequestDto.builder()
        		 .driverId(Optional.of(Long.parseLong(userId)))
                 .status("SCHEDULED")
                 //.passengerId(Optional.of(passengerId))
        		 .build();
         System.out.println(requestDto.getStatus()+" "+requestDto.getDriverId());
          ResponseEntity<UpdateBookingResponseDto> result = this.restTemplate.postForEntity(
        		    "http://localhost:1005/api/v1/booking/" + rideResponseDto.getBookingId(),
        		    requestDto,
        		    UpdateBookingResponseDto.class
        		);
          kafkaProducerService.publishMessage("sample-topic", "Hello");
          System.out.println(result.getStatusCode());

    }


}
