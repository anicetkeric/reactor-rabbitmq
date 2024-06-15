package com.bootlab.producer.config;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.Connection;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Mono;

import java.util.Objects;

@RequiredArgsConstructor
@Configuration
public class RabbitMQInit {


    private Mono<Connection> connectionMono;

    private final AmqpAdmin amqpAdmin;

    /**
     * Method create exchanges, bindings, queues on start.
     */
    @PostConstruct
    public void init() {

        /* create exchanges */
        amqpAdmin.declareExchange(ExchangeBuilder.directExchange(AppConstant.EXCHANGE).build());

        /* create dlq-queues */
        amqpAdmin.declareQueue(new Queue(AppConstant.QUEUE, false, false, false));


        /* bind queues to exchanges */
        amqpAdmin.declareBinding(BindingBuilder
                .bind(new Queue(AppConstant.QUEUE))
                .to(new DirectExchange(AppConstant.EXCHANGE))
                .with(AppConstant.ROUTING_KEY)
        );
    }


    @PreDestroy
    public void close() {
        Objects.requireNonNull(connectionMono.block()).close();
    }
}
