package com.example.reservas;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
//ComponentScan y EntityScan se añaden cuando los beans y otras clases anotadas no son
//subdirectorios del directorio que contiene el archivo entrypoint (este)
@ComponentScan(basePackages = {"com.example.reservas", "infrastructure", "application"})
@EntityScan(basePackages = {"domain.model"})
public class ReservasApplication {

	public static void main(String[] args) {
		SpringApplication.run(ReservasApplication.class, args);
	}

}
