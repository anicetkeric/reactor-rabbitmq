package com.bootlab.consumer.listener;

import com.bootlab.consumer.config.AppConstant;
import com.bootlab.consumer.service.InventoryService;
import com.rabbitmq.client.Connection;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.Disposable;
import reactor.core.publisher.Mono;
import reactor.rabbitmq.*;

import java.io.IOException;
import java.time.Duration;
import java.util.Objects;
import java.util.function.BiConsumer;

import static reactor.rabbitmq.ExceptionHandlers.CONNECTION_RECOVERY_PREDICATE;

@RequiredArgsConstructor
@Configuration
public class ConsumerListener {

    private Mono<Connection> connectionMono;

    @PreDestroy
    public void close() throws IOException {
        Objects.requireNonNull(connectionMono.block()).close();
    }

    @Bean
    public Disposable receiveMessage(Receiver receiver, InventoryService inventoryService) {
      final BiConsumer<Receiver.AcknowledgmentContext, Exception> exceptionHandler = new ExceptionHandlers.RetryAcknowledgmentExceptionHandler(
                Duration.ofSeconds(20), Duration.ofMillis(500),
                CONNECTION_RECOVERY_PREDICATE
        );

        return receiver.consumeManualAck(AppConstant.QUEUE, new ConsumeOptions().exceptionHandler(exceptionHandler))
                .subscribe(msg -> {
                    try {
                        inventoryService.saveInventory(msg);
                        msg.ack();
                    } catch (Exception e) {
                       // logger.error("Error while proceed user-message {}", e.getMessage());
                        msg.nack(false);
                    }
                });
    }


}
