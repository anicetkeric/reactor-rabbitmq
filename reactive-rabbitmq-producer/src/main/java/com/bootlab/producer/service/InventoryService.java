package com.bootlab.producer.service;

import com.bootlab.producer.config.AppConstant;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.apache.commons.lang3.SerializationUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.rabbitmq.OutboundMessage;
import reactor.rabbitmq.Sender;
import com.bootlab.producer.model.StockInventory;
import reactor.util.retry.Retry;

import java.time.Duration;

@Slf4j
@RequiredArgsConstructor
@Service
public class InventoryService {

    private final Sender sender;

    private final ObjectMapper mapper;

    /**
     * Method send message to queue with publish confirms
     */
    public Mono<Void> createInventory(StockInventory stockInventory) throws JsonProcessingException {
        String json = mapper.writeValueAsString(stockInventory);
        byte[] inventoryByteArray = SerializationUtils.serialize(json);
        Flux<OutboundMessage> outboundFlux = Flux.just(new OutboundMessage("", AppConstant.QUEUE, inventoryByteArray));


        log.info("Publish message: {}", stockInventory.toString());
        return sender.sendWithPublishConfirms(outboundFlux)
                .subscribeOn(Schedulers.boundedElastic())
                .filter(outboundMessageResult -> !outboundMessageResult.isAck())
                .handle((result, sink) -> sink.error(new Exception("Publish was not acked")))
                .retryWhen(Retry.backoff(2, Duration.ofMillis(100)))
                .then();
    }

}
