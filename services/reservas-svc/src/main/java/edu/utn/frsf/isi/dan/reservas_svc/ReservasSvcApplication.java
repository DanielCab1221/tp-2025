package edu.utn.frsf.isi.dan.reservas_svc;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ReservasSvcApplication {

  public static void main(String[] args) {
    SpringApplication.run(ReservasSvcApplication.class, args);
  }
}
