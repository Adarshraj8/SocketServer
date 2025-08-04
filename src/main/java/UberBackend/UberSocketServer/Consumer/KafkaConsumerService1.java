package UberBackend.UberSocketServer.Consumer;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class KafkaConsumerService1 {

	@KafkaListener(topics = "sample-topic",groupId = "my-group-id-test-3")
	public void listen(String message) {
		System.out.println("kafka consumer-1 message from sample topic "+message);
	}
}
