package com.jobboard.jobs.config;

import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    // Exchange name
    public static final String JOB_BOARD_EXCHANGE = "job.board.exchange";

    // Routing keys
    public static final String JOB_POSTED_ROUTING_KEY = "job.posted";
    public static final String APPLICATION_SUBMITTED_ROUTING_KEY = "application.submitted";
    public static final String APPLICATION_STATUS_CHANGED_ROUTING_KEY = "application.status.changed";

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(jsonMessageConverter());
        return rabbitTemplate;
    }
}