package timeWizard.bilnut;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class BilnutApplication {

	public static void main(String[] args) {
		SpringApplication.run(BilnutApplication.class, args);
	}

}
