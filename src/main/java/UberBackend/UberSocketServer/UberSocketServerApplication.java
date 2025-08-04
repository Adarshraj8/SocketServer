package UberBackend.UberSocketServer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})

@EnableEurekaClient
@EntityScan(basePackages = {
		"UberBackend.UberSocketServer.models",
	    "UberBackend.UberProject_EntityService.models"  // Add this line
	})

public class UberSocketServerApplication {

	public static void main(String[] args) {
		SpringApplication.run(UberSocketServerApplication.class, args);
	}

	@Bean
	public RestTemplate restTemplate() {
		return new RestTemplate();
	}
}
