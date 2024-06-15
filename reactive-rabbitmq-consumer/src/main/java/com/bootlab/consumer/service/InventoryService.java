package com.bootlab.consumer.service;

import com.bootlab.consumer.model.StockInventory;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Delivery;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.SerializationUtils;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class InventoryService {

    private final ObjectMapper mapper;

    public void saveInventory(Delivery message) {

        String json = SerializationUtils.deserialize(message.getBody());
        StockInventory productOrder;
        // 2. map json to order object
        try {
            productOrder = mapper.readValue(json, StockInventory.class);
            System.out.println("JSON String deserializer: => "+json.toString());
            System.out.println("ObjectToString: => "+productOrder.toString());

        } catch (Exception e) {

            e.printStackTrace();
        }
    }

}
