package edu.utn.frsf.isi.dan.reservas_svc;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Además del converter JSON, declara el circuito de dead-letter para {@code habitacion.topic}: los
 * mensajes que {@link edu.utn.frsf.isi.dan.reservas_svc.messaging.GestionMessageListener} rechaza
 * (por ejemplo, un JSON malformado) van a parar acá en vez de perderse para siempre, para poder
 * inspeccionarlos desde la consola de RabbitMQ.
 */
@Configuration
public class RabbitMQConfig {

  public static final String DEAD_LETTER_EXCHANGE = "dan.exchange.dlx";
  public static final String DEAD_LETTER_QUEUE = "habitacion.topic.dlq";

  @Bean
  public MessageConverter jackson2MessageConverter() {
    // Este convertidor usará Jackson para serializar/deserializar objetos a/desde JSON.
    return new Jackson2JsonMessageConverter();
  }

  @Bean
  public FanoutExchange deadLetterExchange() {
    return new FanoutExchange(DEAD_LETTER_EXCHANGE, true, false);
  }

  @Bean
  public Queue deadLetterQueue() {
    return QueueBuilder.durable(DEAD_LETTER_QUEUE).build();
  }

  @Bean
  public Binding deadLetterBinding() {
    return BindingBuilder.bind(deadLetterQueue()).to(deadLetterExchange());
  }
}
