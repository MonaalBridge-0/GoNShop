package com.GoShop.inventoryservice.service;

import com.GoShop.inventoryservice.dto.InventoryDto;
import com.GoShop.inventoryservice.repository.InventoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryService {
    private final InventoryRepository inventoryRepository;
    @Transactional(readOnly = true)
    @SneakyThrows
    public List<InventoryDto> isInStock(List<String> skuCode){
        log.info("Checking Inventory");
        return inventoryRepository.findBySkuCodeIn(skuCode).stream()
                .map( dto -> InventoryDto.builder()
                        .skuCode(dto.getSkuCode())
                        .isInStock(dto.getQuantity() > 0)
                        .build()
                ).toList();
    }
}
