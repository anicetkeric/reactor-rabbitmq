package com.bootlab.consumer.listener;

import com.bootlab.consumer.config.AppConstant;
import com.bootlab.consumer.service.InventoryService;
import com.rabbitmq.client.Connection;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.Disposable;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.rabbitmq.*;

import java.io.IOException;
import java.time.Duration;
import java.util.Objects;


@Slf4j
@RequiredArgsConstructor
@Component
public class ConsumerListener {

    private final Mono<Connection> connectionMono;

    private final InventoryService inventoryService;

    private final Receiver receiver;

    @PreDestroy
    public void close() throws IOException {
        Objects.requireNonNull(connectionMono.block()).close();
    }

    @PostConstruct
    public Disposable receiveMessage() {
        return receiver.consumeManualAck(AppConstant.QUEUE, consumeOptions())
                .publishOn(Schedulers.parallel())
                .filter(delivery -> Objects.nonNull(delivery.getBody()))
                .flatMap(this::consumer)
                .subscribe();
    }

    private Mono<?> consumer(AcknowledgableDelivery acknowledgableDelivery) {
        return inventoryService.saveInventory(acknowledgableDelivery)
                .doOnSuccess(consume -> {
                    if (Boolean.TRUE.equals(consume)) {
                        acknowledgableDelivery.ack();
                    } else {
                        acknowledgableDelivery.nack(false);
                    }
                })
                .onErrorResume(throwable -> {
                    log.error(">> Exception Error => ", throwable);
                    return Mono.fromRunnable(() -> acknowledgableDelivery.nack(false));
                });
    }

    private ConsumeOptions consumeOptions() {
        return new ConsumeOptions().exceptionHandler(
                new ExceptionHandlers.RetryAcknowledgmentExceptionHandler(
                        Duration.ofSeconds(20),
                        Duration.ofMillis(500),
                        ExceptionHandlers.CONNECTION_RECOVERY_PREDICATE
                )
        );
    }
}
