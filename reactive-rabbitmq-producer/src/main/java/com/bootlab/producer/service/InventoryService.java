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
import reactor.rabbitmq.QueueSpecification;
import reactor.rabbitmq.Sender;
import com.bootlab.producer.model.StockInventory;

@Slf4j
@RequiredArgsConstructor
@Service
public class InventoryService {

    private final Sender sender;
    private final ObjectMapper mapper;
/*

    public Mono<Boolean> createInventory(StockInventory stockInventory) throws JsonProcessingException {

        String json = mapper.writeValueAsString(stockInventory);
        byte[] orderByteArray = SerializationUtils.serialize(json);
        Flux<OutboundMessage> outboundFlux = Flux.just( new OutboundMessage(
                "",
                AppConstant.QUEUE, orderByteArray));

        sender.declareQueue(QueueSpecification.queue(AppConstant.QUEUE))
                .thenMany(sender.sendWithPublishConfirms(outboundFlux))
                .doOnError(e -> log.error("send failed with: ", e))
                .subscribeOn(Schedulers.immediate())
                .subscribe(r -> {
                    if (r.isAck()) {
                        log.info("StockInventory sent successfully");
                    }
                });

        return Mono.just(Boolean.TRUE);

    }
*/

    /**
     * Method send message to queue with publish confirms
     */
    public Mono<Boolean> createInventory(StockInventory stockInventory) throws JsonProcessingException {
        String json = mapper.writeValueAsString(stockInventory);
        byte[] orderByteArray = SerializationUtils.serialize(json);
        Flux<OutboundMessage> outboundFlux = Flux.just( new OutboundMessage(
                "",
                AppConstant.QUEUE, orderByteArray));

        sender.sendWithPublishConfirms(outboundFlux)
                .doOnError(t -> log.error("Error while send message to queue {}, {}",  AppConstant.QUEUE, t))
                .subscribe(result -> {
                    if (result.isReturned()) {
                        log.error("Error while send message to queue {}",  AppConstant.QUEUE);
                    }
                });


        return Mono.just(Boolean.TRUE);
    }

}
