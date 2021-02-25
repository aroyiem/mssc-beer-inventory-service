package com.roy.beer.inventory.service.services.listeners;

import com.roy.beer.inventory.service.config.JMSConfig;
import com.roy.beer.inventory.service.services.AllocationService;
import com.roy.brewery.model.events.AllocateOrderRequest;
import com.roy.brewery.model.events.AllocateOrderResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class AllocationListener {

    private final AllocationService allocationService;
    private final JmsTemplate jmsTemplate;

    @JmsListener(destination = JMSConfig.ALLOCATE_ORDER_QUEUE)
    public void listen(AllocateOrderRequest allocateOrderRequest) {


        AllocateOrderResult.AllocateOrderResultBuilder builder = AllocateOrderResult.builder();

        try {
            Boolean allocationResult = allocationService.allocateOrder(allocateOrderRequest.getBeerOrderDto());

            if(allocationResult) {
                builder.pendingInventory(false);
            } else {
                builder.pendingInventory(true);
            }
            builder.allocationError(false);
        } catch (Exception e) {
            log.error("Allocation failed for Order Id: " + allocateOrderRequest.getBeerOrderDto().getId());
            builder.allocationError(true);
        }

        jmsTemplate.convertAndSend(JMSConfig.ALLOCATE_ORDER_RESPONSE_QUEUE, builder.build());
    }
}
