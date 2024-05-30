package com.bootlab.producer.config;

import com.bootlab.producer.service.InventoryReceiver;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import org.springframework.boot.autoconfigure.amqp.RabbitProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.Disposable;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.rabbitmq.*;
import reactor.rabbitmq.ExceptionHandlers.RetryAcknowledgmentExceptionHandler;
import reactor.rabbitmq.ConsumeOptions;
import java.time.Duration;
import java.util.function.BiConsumer;

import static reactor.rabbitmq.ExceptionHandlers.CONNECTION_RECOVERY_PREDICATE;

@Configuration
public class RabbitMQConfig {


    @Bean
    public Mono<Connection> connectionMono(RabbitProperties rabbitProperties) {
        var connectionFactory = new ConnectionFactory();
        connectionFactory.setHost(rabbitProperties.getHost());
        connectionFactory.setPort(rabbitProperties.getPort());
        connectionFactory.setUsername(rabbitProperties.getUsername());
        connectionFactory.setPassword(rabbitProperties.getPassword());
        return Mono.fromCallable(() -> connectionFactory.newConnection("reactor-rabbit")).cache();
    }


    @Bean
    public ReceiverOptions receiverOptions(Mono<Connection> connectionMono) {
        return new ReceiverOptions()
                .connectionMono(connectionMono);
    }

    @Bean
    public Receiver receiver(ReceiverOptions receiverOptions) {
        return RabbitFlux.createReceiver(receiverOptions);
    }

    @Bean
    public SenderOptions senderOptions(Mono<Connection> connectionMono) {
        return new SenderOptions()
                .connectionMono(connectionMono)
                .resourceManagementScheduler(Schedulers.boundedElastic());
    }

    @Bean
    public Sender sender(SenderOptions senderOptions) {
        return RabbitFlux.createSender(senderOptions);
    }

    @Bean
    public ObjectMapper objectMapper() {
        var objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        return objectMapper;
    }



    @Bean
    public Disposable receiveMessage(Receiver receiver, InventoryReceiver messageService) {
      final BiConsumer<Receiver.AcknowledgmentContext, Exception> exceptionHandler = new ExceptionHandlers.RetryAcknowledgmentExceptionHandler(
                Duration.ofSeconds(20), Duration.ofMillis(500),
                CONNECTION_RECOVERY_PREDICATE
        );

        return receiver.consumeManualAck(AppConstant.QUEUE, new ConsumeOptions().exceptionHandler(exceptionHandler))
                .subscribe(msg -> {
                    try {
                        messageService.proceedMessage(msg);
                        msg.ack();
                    } catch (Exception e) {
                       // logger.error("Error while proceed user-message {}", e.getMessage());
                        msg.nack(false);
                    }
                });
    }


}
