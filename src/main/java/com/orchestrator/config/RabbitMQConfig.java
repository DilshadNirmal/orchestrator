package com.orchestrator.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {
    public static final String EXCHANGE_NAME = "orchestrator.exchange";
    public static final String QUEUE_NAME = "orchestrator.sync.queue";
    public static final String ROUTING_KEY = "sync.#";

    @Bean
    public TopicExchange orchestratorExchange() {
        return new TopicExchange(EXCHANGE_NAME);
    }

    @Bean
    public Queue syncQueue() {
        return QueueBuilder.durable(QUEUE_NAME).build();
    }

    @Bean
    public Binding syncBinding(Queue syncQueue, TopicExchange orchestratorExchange) {
        return BindingBuilder.bind(syncQueue).to(orchestratorExchange).with(ROUTING_KEY);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter());
        return template;
    }
}