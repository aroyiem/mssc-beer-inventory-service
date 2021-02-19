package com.roy.beer.inventory.service.services;

import com.roy.beer.inventory.service.domain.BeerInventory;
import com.roy.beer.inventory.service.repositories.BeerInventoryRepository;
import com.roy.brewery.model.BeerOrderDetailsDto;
import com.roy.brewery.model.BeerOrderDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@RequiredArgsConstructor
@Slf4j
public class AllocationServiceImpl implements AllocationService {

    private final BeerInventoryRepository beerInventoryRepository;

    @Override
    public Boolean allocateOrder(BeerOrderDto beerOrderDto) {
        log.debug("Allocating OrderId: " + beerOrderDto.getId());

        AtomicInteger totalOrdered = new AtomicInteger();
        AtomicInteger totalAllocated = new AtomicInteger();

        beerOrderDto.getBeerOrderDetails().forEach(beerOrderDetailsDto -> {
            if((beerOrderDetailsDto.getOrderQuantity() !=null ? beerOrderDetailsDto.getOrderQuantity() : 0)
                    - (beerOrderDetailsDto.getQuantityAllocated() != null ? beerOrderDetailsDto.getQuantityAllocated() : 0) > 0) {
                allocateBeerOrderDetails(beerOrderDetailsDto);
            }
            totalOrdered.set(totalOrdered.get() + beerOrderDetailsDto.getOrderQuantity());
            totalAllocated.set(totalAllocated.get() + (beerOrderDetailsDto.getQuantityAllocated() != null ? beerOrderDetailsDto.getQuantityAllocated() : 0));
        });

        log.debug("Total Ordered: " + totalOrdered.get() + " Total Allocated: " + totalAllocated.get());

        return totalOrdered.get() == totalAllocated.get();
    }

    private void allocateBeerOrderDetails(BeerOrderDetailsDto beerOrderDetailsDto) {
        List<BeerInventory> beerInventoryList = beerInventoryRepository.findAllByUpc(beerOrderDetailsDto.getUpc());
        beerInventoryList.forEach(beerInventory -> {
            int inventory = (beerInventory.getQuantityOnHand() == null) ? 0 : beerInventory.getQuantityOnHand();
            int orderQty = (beerOrderDetailsDto.getOrderQuantity() == null) ? 0 : beerOrderDetailsDto.getOrderQuantity();
            int allocatedQty = (beerOrderDetailsDto.getQuantityAllocated() == null) ? 0 : beerOrderDetailsDto.getQuantityAllocated();
            int qtyToAllocate = orderQty - allocatedQty;

            if (inventory >= qtyToAllocate) { // full allocation
                inventory = inventory - qtyToAllocate;
                beerOrderDetailsDto.setQuantityAllocated(orderQty);
                beerInventory.setQuantityOnHand(inventory);

                beerInventoryRepository.save(beerInventory);
            } else if (inventory > 0) { //partial allocation
                beerOrderDetailsDto.setQuantityAllocated(allocatedQty + inventory);
                beerInventory.setQuantityOnHand(0);

            }

            if (beerInventory.getQuantityOnHand() == 0) {
                beerInventoryRepository.delete(beerInventory);
            }
        });
    }

    @Override
    @Transactional
    public void deallocateOrder(BeerOrderDto beerOrderDto) {
        beerOrderDto.getBeerOrderDetails().forEach(beerOrderDetailsDto -> {
            BeerInventory beerInventory = BeerInventory.builder()
                    .beerId(beerOrderDetailsDto.getBeerId())
                    .upc(beerOrderDetailsDto.getUpc())
                    .quantityOnHand(beerOrderDetailsDto.getQuantityAllocated())
                    .build();

            BeerInventory savedInventory = beerInventoryRepository.saveAndFlush(beerInventory);

            log.debug("Saved Inventory for beer upc: " + savedInventory.getUpc() + " inventory id: " + savedInventory.getId());
        });
    }
}
