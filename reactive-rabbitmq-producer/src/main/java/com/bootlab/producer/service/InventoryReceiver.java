package com.bootlab.producer.service;


import com.bootlab.producer.config.AppConstant;
import com.bootlab.producer.model.StockInventory;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Delivery;
import jakarta.annotation.PostConstruct;
import org.springframework.amqp.rabbit.connection.Connection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.Disposable;
import reactor.core.publisher.Mono;
import org.apache.commons.lang3.SerializationUtils;

import reactor.rabbitmq.Receiver;

@Service
public class InventoryReceiver {

    /**
     * Name our receiver queue message in RabbitMQ Broker
     */

    @Autowired
    private Receiver receiver;
    @Autowired
    private ObjectMapper mapper;


    // Listen to RabbitMQ as soon as this service is up
    @PostConstruct
    private void init() {
      //  consume();
    }

    // Consume message from the sender queue

    private Disposable consume() {

        return receiver.consumeAutoAck(AppConstant.QUEUE)
                .subscribe(m -> {

                    //1. Deserialize byte to json
                    String json = SerializationUtils.deserialize(m.getBody());

                    StockInventory productOrder;

                    // 2. map json to order object
                    try {

                        productOrder = mapper.readValue(json, StockInventory.class);
                        System.out.println("JSON String deserializer: => "+json.toString());
                        System.out.println("ObjectToString: => "+productOrder.toString());


                        /**
                         * Do your business logic here...
                         */

                    } catch (Exception e) {

                        e.printStackTrace();
                    }

                });

    }



    public void proceedMessage(Delivery message) {

        String json = SerializationUtils.deserialize(message.getBody());


        StockInventory productOrder;

        // 2. map json to order object
        try {

            productOrder = mapper.readValue(json, StockInventory.class);
            System.out.println("JSON String deserializer: => "+json.toString());
            System.out.println("ObjectToString: => "+productOrder.toString());


            /**
             * Do your business logic here...
             */

        } catch (Exception e) {

            e.printStackTrace();
        }
    }

}
