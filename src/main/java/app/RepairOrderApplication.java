package app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = {"app", "domain", "pricing", "repository", "service"})
@EntityScan(basePackages = {"domain"})
@EnableJpaRepositories(basePackages = {"repository"})
public class RepairOrderApplication {

    public static void main(String[] args) {
        SpringApplication.run(RepairOrderApplication.class, args);
    }
}
