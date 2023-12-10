package br.ufsm.csi.pila;

import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
public class PilaApplication {

    public static void main(String[] args) {
        SpringApplication.run(PilaApplication.class, args);
    }

}
