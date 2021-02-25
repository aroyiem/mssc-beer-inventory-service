package com.roy.beer.inventory.service.services.listeners;

import com.roy.beer.inventory.service.config.JMSConfig;
import com.roy.beer.inventory.service.services.AllocationService;
import com.roy.brewery.model.events.DeallocateOrderRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DeallocationListener {

    private final AllocationService allocationService;

    @JmsListener(destination = JMSConfig.DEALLOCATE_ORDER_QUEUE)
    public void listen(DeallocateOrderRequest request) {

        allocationService.deallocateOrder(request.getBeerOrderDto());

    }
}
