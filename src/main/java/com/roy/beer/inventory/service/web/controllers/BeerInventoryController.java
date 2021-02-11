package com.roy.beer.inventory.service.web.controllers;

import com.roy.beer.inventory.service.web.model.BeerInventoryDto;
import com.roy.beer.inventory.service.repositories.BeerInventoryRepository;
import com.roy.beer.inventory.service.web.mappers.BeerInventoryMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@Slf4j
@RequiredArgsConstructor
public class BeerInventoryController {

    private final BeerInventoryRepository beerInventoryRepository;
    private final BeerInventoryMapper beerInventoryMapper;

    @GetMapping("/api/v1/beer/{beerId}/inventory")
    public List<BeerInventoryDto> listBeersById(@PathVariable("beerId")UUID beerId) {
        log.debug("Finding Inventory for beerId: " + beerId);
        return beerInventoryRepository.findAllByBeerId(beerId)
                .stream()
                .map(beerInventory -> beerInventoryMapper.beerInventoryToDto(beerInventory))
                .collect(Collectors.toList());
    }
}
