package es.codeurjc.global_mart.dto.Orders;

import java.util.Collection;
import java.util.List;

import org.mapstruct.Mapper;

import es.codeurjc.global_mart.model.Order;

@Mapper(componentModel = "spring")
public interface OrderMapper {
    OrderDTO toOrderDTO(Order order);

    List<OrderDTO> toOrdersDTO(Collection<Order> orders);
}