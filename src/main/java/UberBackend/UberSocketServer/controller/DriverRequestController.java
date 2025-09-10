package UberBackend.UberSocketServer.controller;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

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

import UberBackend.UberSocketServer.dto.BookingUpdateEvent;
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
    // in-memory booking assignment map (could be DB in prod)
    private final Map<Long, Long> bookingAssignments = new ConcurrentHashMap<>();
    public DriverRequestController(SimpMessagingTemplate messagingTemplate,
    		RestTemplate restTemplate,
    		 KafkaProducerService kafkaProducerService) {
    	this.messagingTemplate=messagingTemplate;
    	
    	this.restTemplate=restTemplate;
    	this.kafkaProducerService=kafkaProducerService;
    }
    
//    @GetMapping
//    public Boolean help() {
//    	kafkaProducerService.publishMessage("sample-topic", "Hello");
//		return true;
//    	
//    }
    
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

    
//    public void sendDriverNewRideRequest(RideRequestDto requestDto) {
//     
//        //ideally request should only go to nearby driver but for simplicity we send it to everyone 
//        messagingTemplate.convertAndSend("/topic/rideRequest", requestDto);
//    }
    

public void sendDriverNewRideRequest(RideRequestDto requestDto) {
    if (requestDto.getDriverIds() != null && !requestDto.getDriverIds().isEmpty()) {
        for (Long driverId : requestDto.getDriverIds()) {
            String destination = "/topic/driver/" + driverId + "/rideRequest";
            messagingTemplate.convertAndSend(destination, requestDto);
            System.out.println("📤 WS pushed to " + destination + " -> " + requestDto);
        }
    } else {
        System.out.println("⚠️ No driverIds found in RideRequestDto, nothing sent.");
    }
}
    
//    @MessageMapping("/rideResponse/{userId}")
//    public synchronized void rideResponseHandler(@DestinationVariable String userId, RideResponseDto rideResponseDto) {
//
//        // This driver gets the ride
//        System.out.println("Driver " + userId + " accepted ride from passenger " + userId);
//        // Notify frontend or update booking in DB
//        // e.g., assign driver to booking, change ride status
//         UpdateBookingRequestDto requestDto = UpdateBookingRequestDto.builder()
//        		 .driverId(Optional.of(Long.parseLong(userId)))
//                 .status("SCHEDULED")
//                 //.passengerId(Optional.of(passengerId))
//        		 .build();
//         System.out.println(requestDto.getStatus()+" "+requestDto.getDriverId());
//         // ResponseEntity<UpdateBookingResponseDto> result = this.restTemplate.postForEntity(
//        	//	    "http://localhost:1005/api/v1/booking/" + rideResponseDto.getBookingId(),
//        		//    requestDto,
//        		//    UpdateBookingResponseDto.class
//        		//);
//         BookingUpdateEvent event = new BookingUpdateEvent();
//         event.setBookingId(rideResponseDto.getBookingId());
//         event.setRequestDto(requestDto);
//
//         kafkaProducerService.publishBookingUpdate("booking-update-topic", event);
//       //   System.out.println(result.getStatusCode());
//
//    }

@MessageMapping("/rideResponse/{driverId}")
public synchronized void rideResponseHandler(@DestinationVariable String driverId,
                                             RideResponseDto rideResponseDto) {
    Long bookingId = rideResponseDto.getBookingId();
    Long dId = Long.parseLong(driverId);

    // Check if booking is already taken
    if (bookingAssignments.containsKey(bookingId)) {
        Long assignedDriver = bookingAssignments.get(bookingId);
        System.out.println("❌ Driver " + dId + " tried to accept, but booking " + bookingId +
                           " is already assigned to driver " + assignedDriver);

        // Notify driver that request is no longer available
        messagingTemplate.convertAndSend(
                "/topic/driver/" + dId + "/rideResponse",
                Map.of("status", "REJECTED", "bookingId", bookingId,
                       "message", "Booking already taken by another driver.")
        );
        return;
    }

    // First driver wins
    bookingAssignments.put(bookingId, dId);

    System.out.println("✅ Driver " + dId + " accepted booking " + bookingId);

    UpdateBookingRequestDto requestDto = UpdateBookingRequestDto.builder()
            .driverId(Optional.of(dId))
            .status("SCHEDULED")
            .build();

    // publish booking update via Kafka
    BookingUpdateEvent event = new BookingUpdateEvent();
    event.setBookingId(bookingId);
    event.setRequestDto(requestDto);

    kafkaProducerService.publishBookingUpdate("booking-update-topic", event);

    // Notify assigned driver
    messagingTemplate.convertAndSend(
            "/topic/driver/" + dId + "/rideResponse",
            Map.of("status", "ACCEPTED", "bookingId", bookingId,
                   "message", "You have been assigned this booking.")
    );

    // Notify all other drivers (optional: from your original requestDto driverIds list)
    messagingTemplate.convertAndSend(
            "/topic/rideRequest/updates",
            Map.of("status", "CLOSED", "bookingId", bookingId,
                   "assignedDriverId", dId)
    );
}
}
