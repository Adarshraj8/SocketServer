package UberBackend.UberSocketServer.dto;

import java.util.List;

import UberBackend.UberSocketServer.models.ExactLocation;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

 
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RideRequestDto {

	private Long passengerId;
	
	private ExactLocation startLocation;
	
	private ExactLocation endLocation;
	
	private List<Long> driverIds;
	
	private Long bookingId;
}
