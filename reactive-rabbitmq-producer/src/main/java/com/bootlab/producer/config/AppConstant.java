package com.bootlab.producer.config;

import lombok.experimental.UtilityClass;

@UtilityClass
public class AppConstant {

    public static final String EXCHANGE = "inventory.create";
    public static final String QUEUE = "queue.inventory.create";
    public static final String ROUTING_KEY = "inventory";
}
