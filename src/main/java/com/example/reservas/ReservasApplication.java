package com.example.reservas;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@ComponentScan(basePackages = {"com.example.reservas", "infrastructure", "application.service"})
@EntityScan(basePackages = {"infrastructure.adapter.out.persistence.entity"})
@EnableJpaRepositories(basePackages = {"infrastructure.adapter.out.persistence.repository"})
public class ReservasApplication {

	public static void main(String[] args) {
		SpringApplication.run(ReservasApplication.class, args);
	}

}
