package UberBackend.UberSocketServer.dto;

import java.util.Optional;

import UberBackend.UberProject_EntityService.models.BookingStatus;
import UberBackend.UberProject_EntityService.models.Driver;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateBookingResponseDto {

	private Long bookingId;
	private BookingStatus status;
	private Optional<Driver> driver;
}
