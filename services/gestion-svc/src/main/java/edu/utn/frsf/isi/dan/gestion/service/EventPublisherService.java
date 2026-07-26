package edu.utn.frsf.isi.dan.gestion.service;

import edu.utn.frsf.isi.dan.shared.HabitacionEvent;
import lombok.extern.log4j.Log4j2;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

@Service
@Log4j2
public class EventPublisherService {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Value("${rabbitmq.exchange:habitacion.exchange}")
    private String exchange;
    @Value("${rabbitmq.routingkey:habitacion.key}")
    private String routingKey;

    public void publicar(HabitacionEvent evento) {
        try {
            String msgToSend = objectMapper.writeValueAsString(evento);
            log.debug("[RabbitMQ] Enviando mensaje: {}", msgToSend);
            rabbitTemplate.convertAndSend(exchange, routingKey, msgToSend);
        } catch (Exception e) {
            log.error("Error publicando evento en RabbitMQ: {}", e.getMessage(), e);
        }
    }
}
