package es.codeurjc.global_mart.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import es.codeurjc.global_mart.repository.OrderRepository;
import es.codeurjc.global_mart.model.User;
import es.codeurjc.global_mart.dto.Orders.OrderDTO;
import es.codeurjc.global_mart.dto.Orders.OrderMapper;
import es.codeurjc.global_mart.dto.User.UserDTO;
import es.codeurjc.global_mart.dto.User.UserMapper;
import es.codeurjc.global_mart.model.Order;
import es.codeurjc.global_mart.repository.ProductRepository;
import es.codeurjc.global_mart.model.Product;
import es.codeurjc.global_mart.repository.UserRepository;

@Service
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired 
    private OrderMapper orderMapper;

    @Autowired UserMapper userMapper;

    public OrderDTO createOrder(UserDTO userDTO) {
        User user = userRepository.findByUsername(userDTO.username()).orElseThrow(() -> new RuntimeException("User not found"));
        
        Order order = new Order(user.getUsername(), user.getTotalPrice(), user, new ArrayList<>(user.getCart()));

        System.out.println();
        user.getHistoricalOrderPrices().add(order.getTotal());
        user.emptyCart();
        user.getOrders().add(order);
        System.out.println("historicalOrderPrices: " + user.getHistoricalOrderPrices() + user.getUsername());

        userRepository.save(user);
        orderRepository.save(order);
        return orderMapper.toOrderDTO(order);
    }

    public List<OrderDTO> findAllOrders() {
        return orderMapper.toOrdersDTO(orderRepository.findAll());
    }

    public OrderDTO findOrderById(Long id) {

        try {
            return orderRepository.findById(id)
                    .map(orderMapper::toOrderDTO)
                    .orElseThrow(() -> new RuntimeException("Order not found with id " + id));
                    
        } catch (Exception e) {
            throw new RuntimeException("Error finding order with id " + id, e);
        }

    }

    public OrderDTO addProductToOrder(Long orderId, Long productId) {
        try {
            Order order = orderRepository.findById(orderId)
                    .orElseThrow(() -> new RuntimeException("Order not found with id " + orderId));
            Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new RuntimeException("Product not found with id " + productId));
            order.addProduct(product);
            Order savedOrder = orderRepository.save(order);
            return orderMapper.toOrderDTO(savedOrder);
        } catch (Exception e) {
            throw new RuntimeException("Error adding product to order with id " + orderId, e);
        }

    }

    public OrderDTO deleteProductFromOrder(Long orderId, Long productId) {
        try {
            Order order = orderRepository.findById(orderId)
                    .orElseThrow(() -> new RuntimeException("Order not found with id " + orderId));
            Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new RuntimeException("Product not found with id " + productId));
            order.deleteProduct(product);
            Order savedOrder = orderRepository.save(order);
            return orderMapper.toOrderDTO(savedOrder);
        } catch (Exception e) {
            throw new RuntimeException("Error deleting product from order with id " + orderId, e);
        }

    }

}
