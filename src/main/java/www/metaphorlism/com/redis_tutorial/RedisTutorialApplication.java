package www.metaphorlism.com.redis_tutorial;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableJpaRepositories("www.metaphorlism.com.*")
@ComponentScan(basePackages = { "www.metaphorlism.com.*" })
@EntityScan("www.metaphorlism.com.*")
public class RedisTutorialApplication {

	public static void main(String[] args) {
		SpringApplication.run(RedisTutorialApplication.class, args);
	}

}
