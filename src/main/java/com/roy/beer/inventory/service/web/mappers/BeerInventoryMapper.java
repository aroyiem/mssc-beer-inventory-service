package com.roy.beer.inventory.service.web.mappers;

import com.roy.brewery.model.BeerInventoryDto;
import com.roy.beer.inventory.service.domain.BeerInventory;
import org.mapstruct.Mapper;

@Mapper(uses = {DateMapper.class})
public interface BeerInventoryMapper {

    BeerInventoryDto beerInventoryToDto(BeerInventory beerInventory);
    BeerInventory beerInventoryDtoToBeerInventory(BeerInventoryDto dto);
}
