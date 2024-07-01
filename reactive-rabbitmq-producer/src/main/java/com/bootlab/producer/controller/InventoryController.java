package com.bootlab.producer.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import com.bootlab.producer.model.StockInventory;
import com.bootlab.producer.service.InventoryService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/inventory")
public class InventoryController {

    private final InventoryService inventoryService;

    @PostMapping
    @ResponseStatus(HttpStatus.ACCEPTED)
    public Mono<Void> createInventory(@RequestBody StockInventory stockInventory) throws JsonProcessingException {
        return inventoryService.createInventory(stockInventory);
    }
}
