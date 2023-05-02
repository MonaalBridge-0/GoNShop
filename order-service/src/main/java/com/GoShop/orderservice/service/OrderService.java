package com.GoShop.orderservice.service;

import com.GoShop.orderservice.dto.InventoryDto;
import com.GoShop.orderservice.dto.OrderLineItemsDto;
import com.GoShop.orderservice.dto.OrderRequest;
import com.GoShop.orderservice.model.Order;
import com.GoShop.orderservice.model.OrderLineItems;
import com.GoShop.orderservice.respository.OrderRepository;
import lombok.AllArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
@AllArgsConstructor
@Transactional
public class OrderService {
    private final OrderRepository orderRepository;
    private final WebClient webClient;
    private final ApplicationEventPublisher applicationEventPublisher;
    public String placeOrder(OrderRequest orderRequest){
        Order order = new Order();
        order.setOrderNumber(UUID.randomUUID().toString());
        List<OrderLineItems> orderLineItems = orderRequest.getOrderLineItemsDtoList()
                .stream()
                .map(this::mapToDto)
                .toList();
        order.setOrderLineItemsList(orderLineItems);
        List<String> skuCode = order.getOrderLineItemsList().stream().map(OrderLineItems::getSkuCode).toList();

        //Call Inventory Service and place order if product is in stock
        InventoryDto[] inventoryDtoArray = webClient.get()
                    .uri("http://localhost:8082/api/inventory",uriBuilder -> uriBuilder.queryParam("skuCode",skuCode).build())
                    .retrieve()
                    .bodyToMono(InventoryDto[].class)
                    .block();
        boolean allProductsInStock = Arrays.stream(inventoryDtoArray)
                    .allMatch(inventoryDto -> inventoryDto.isInStock());
            if (allProductsInStock) {
                //Save to repository
                orderRepository.save(order);
                return "Order is now Placed";
                //Publish Place order event
            } else {
                throw new IllegalArgumentException("Product is not in Stock, Please try later :) ");
            }
    }

    private OrderLineItems mapToDto(OrderLineItemsDto orderLineItemsDto) {
        OrderLineItems orderLineItems = new OrderLineItems();
        orderLineItems.setPrice(orderLineItemsDto.getPrice());
        orderLineItems.setQuantity(orderLineItemsDto.getQuantity());
        orderLineItems.setSkuCode(orderLineItemsDto.getSkuCode());
        return orderLineItems;
    }
}
