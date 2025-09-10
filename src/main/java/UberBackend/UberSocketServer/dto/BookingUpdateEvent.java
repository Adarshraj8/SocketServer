package UberBackend.UberSocketServer.dto;

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
public class BookingUpdateEvent {

	private Long bookingId;
    private UpdateBookingRequestDto requestDto;
}
