package win.iot4yj;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@EnableEurekaClient
@RestController
public class UserClientApplication {

	public static void main(String[] args) {
		SpringApplication.run(UserClientApplication.class, args);
	}

	@GetMapping("index")
	public String index(){
		return "hello user client";
	}

}

