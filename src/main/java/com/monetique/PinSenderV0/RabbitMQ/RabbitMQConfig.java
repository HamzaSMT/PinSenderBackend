package com.monetique.PinSenderV0.RabbitMQ;

import com.monetique.PinSenderV0.Services.Cardholder.CardholderConsumer;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class RabbitMQConfig {

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(messageConverter());

        rabbitTemplate.setConfirmCallback((correlationData, ack, cause) -> {
            if (ack) {
                System.out.println("Message envoyé avec succès à RabbitMQ.");
            } else {
                System.err.println("Échec d'envoi du message à RabbitMQ : " + cause);
            }
        });

        rabbitTemplate.setMandatory(true);  // S'assurer que les messages non routés sont renvoyés à l'expéditeur
        rabbitTemplate.setReturnsCallback(returned -> {
            System.err.println("Message renvoyé : " + returned.getMessage() + " raison : " + returned.getReplyText());
        });

        return rabbitTemplate;
    }

    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();  // Use Jackson for JSON conversion
    }

    // Declare the queue
    @Bean
    public Queue cardholderQueue() {
        Map<String, Object> args = new HashMap<>(   );
        args.put("x-dead-letter-exchange", "cardholder.dead-letter.exchange");
        args.put("x-dead-letter-routing-key", "cardholder.dlq.routingKey");

        return new Queue("cardholder.queue", true, false, false, args);  // File d'attente avec DLQ
    }

    // Declare the exchange
    @Bean
    public TopicExchange cardholderExchange() {
        return new TopicExchange("cardholder.exchange", true, false); // Durable exchange
    }

    // Bind the queue to the exchange with a routing key
    @Bean
    public Binding cardholderBinding(Queue cardholderQueue, TopicExchange cardholderExchange) {
        return BindingBuilder.bind(cardholderQueue).to(cardholderExchange).with("cardholder.routingKey");
    }

    // Listener container to listen to the queue
    @Bean
    public SimpleMessageListenerContainer messageListenerContainer(ConnectionFactory connectionFactory,
                                                                   MessageListenerAdapter listenerAdapter) {
        SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.setQueueNames("cardholder.queue");
        container.setMessageListener(listenerAdapter);

        container.setConcurrentConsumers(10);
        container.setMaxConcurrentConsumers(20);
        container.setPrefetchCount(50);
        container.setDefaultRequeueRejected(false);
        container.setAcknowledgeMode(AcknowledgeMode.AUTO);

        return container;
    }

    // Listener adapter to use Jackson for message conversion
    @Bean
    public MessageListenerAdapter listenerAdapter(CardholderConsumer consumer) {
        MessageListenerAdapter adapter = new MessageListenerAdapter(consumer, "handleMessage");  // Method to handle messages
        adapter.setMessageConverter(messageConverter());  // Set the JSON message converter here
        return adapter;
    }

    @Bean
    public Queue deadLetterQueue() {
        return new Queue("cardholder.dead-letter.queue", true);  // DLQ durable
    }

    @Bean
    public TopicExchange deadLetterExchange() {
        return new TopicExchange("cardholder.dead-letter.exchange", true, false);  // DLX durable
    }

    @Bean
    public Binding deadLetterBinding(Queue deadLetterQueue, TopicExchange deadLetterExchange) {
        return BindingBuilder.bind(deadLetterQueue).to(deadLetterExchange).with("cardholder.dlq.routingKey");
    }

}