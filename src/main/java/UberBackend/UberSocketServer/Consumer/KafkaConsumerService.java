package UberBackend.UberSocketServer.Consumer;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class KafkaConsumerService {

	@KafkaListener(topics = "sample-topic",groupId = "my-group-id-test")
	public void listen(String message) {
		System.out.println("kafka message from sample topic "+message);
	}
}
