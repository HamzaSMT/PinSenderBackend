package com.monetique.PinSenderV0.RabbitMQ;


import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    // Declare queue
    @Bean
    public Queue myQueue() {
        return new Queue("myQueue", false);
    }

    // Declare exchange
    @Bean
    public DirectExchange myExchange() {
        return new DirectExchange("myExchange");
    }

    // Bind queue to the exchange
    @Bean
    public Binding binding(Queue myQueue, DirectExchange myExchange) {
        return BindingBuilder.bind(myQueue).to(myExchange).with("routingKey");
    }
}