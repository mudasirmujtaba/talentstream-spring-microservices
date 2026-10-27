package com.jobboard.notifications.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    // Queue names
    public static final String JOB_POSTED_QUEUE = "job.posted.queue";
    public static final String APPLICATION_SUBMITTED_QUEUE = "application.submitted.queue";
    public static final String APPLICATION_STATUS_CHANGED_QUEUE = "application.status.changed.queue";

    // Exchange name
    public static final String JOB_BOARD_EXCHANGE = "job.board.exchange";

    // Routing keys
    public static final String JOB_POSTED_ROUTING_KEY = "job.posted";
    public static final String APPLICATION_SUBMITTED_ROUTING_KEY = "application.submitted";
    public static final String APPLICATION_STATUS_CHANGED_ROUTING_KEY = "application.status.changed";

    // Declare queues
    @Bean
    public Queue jobPostedQueue() {
        return new Queue(JOB_POSTED_QUEUE, true); // durable = true
    }

    @Bean
    public Queue applicationSubmittedQueue() {
        return new Queue(APPLICATION_SUBMITTED_QUEUE, true);
    }

    @Bean
    public Queue applicationStatusChangedQueue() {
        return new Queue(APPLICATION_STATUS_CHANGED_QUEUE, true);
    }

    // Declare exchange
    @Bean
    public TopicExchange jobBoardExchange() {
        return new TopicExchange(JOB_BOARD_EXCHANGE);
    }

    // Bind queues to exchange with routing keys
    @Bean
    public Binding jobPostedBinding() {
        return BindingBuilder
                .bind(jobPostedQueue())
                .to(jobBoardExchange())
                .with(JOB_POSTED_ROUTING_KEY);
    }

    @Bean
    public Binding applicationSubmittedBinding() {
        return BindingBuilder
                .bind(applicationSubmittedQueue())
                .to(jobBoardExchange())
                .with(APPLICATION_SUBMITTED_ROUTING_KEY);
    }

    @Bean
    public Binding applicationStatusChangedBinding() {
        return BindingBuilder
                .bind(applicationStatusChangedQueue())
                .to(jobBoardExchange())
                .with(APPLICATION_STATUS_CHANGED_ROUTING_KEY);
    }

    // Message converter (JSON)
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    // RabbitTemplate with JSON converter
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(jsonMessageConverter());
        return rabbitTemplate;
    }
}