package com.example.orders.service;

import com.example.orders.model.Orders;
import com.example.orders.repositories.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepository orderRepository;
    private final RestTemplate restTemplate;

    public ResponseEntity<Object> addOrder(Orders orders) {

        Long id = orders.getProduct();
        Integer amount = orders.getAmount();
        String productServiceUrl = "http://localhost:8081/api/products/";

        ResponseEntity<Object> responseEntity;
        try {
            responseEntity = restTemplate.getForEntity(productServiceUrl + id, Object.class);
        } catch (HttpClientErrorException.NotFound e) {
            return new ResponseEntity<>("No se encontro el producto", HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>("Error al comunicarse con el servicio de productos", HttpStatus.INTERNAL_SERVER_ERROR);
        }

        if (responseEntity != null && responseEntity.getStatusCode() == HttpStatus.OK) {
            Map<String, Object> productMap = (Map<String, Object>) responseEntity.getBody();

            if (productMap != null && productMap.containsKey("stock")) {
                Number stockNumber = (Number) productMap.get("stock");
                Long stock = stockNumber.longValue();
                System.out.println("Stock: " + stock);

                if (amount <= stock) {
                    Map<String, Object> requestBody = new HashMap<>();
                    requestBody.put("id", id);
                    requestBody.put("amount", amount);
                    productServiceUrl = "http://localhost:8081/api/products/stock";

                    HttpHeaders headers = new HttpHeaders();
                    headers.setContentType(MediaType.APPLICATION_JSON);

                    HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);

                    ResponseEntity<String> responseEntity2;
                    try {
                        responseEntity2 = restTemplate.exchange(
                                productServiceUrl,
                                HttpMethod.PUT,
                                requestEntity,
                                String.class
                        );
                    } catch (Exception e) {
                        return new ResponseEntity<>("Error al actualizar el stock, no se grabara la orden", HttpStatus.INTERNAL_SERVER_ERROR);
                    }

                    if (responseEntity2.getStatusCode() == HttpStatus.OK) {
                        orderRepository.save(orders);
                        return new ResponseEntity<>("Orden creada exitosamente", HttpStatus.OK);
                    } else {
                        return new ResponseEntity<>("Error al actualizar el stock, no se grabara la orden", HttpStatus.BAD_REQUEST);
                    }
                } else {
                    return new ResponseEntity<>("La cantidad ingresada supera el stock disponible", HttpStatus.BAD_REQUEST);
                }
            } else {
                return new ResponseEntity<>("Respuesta del microservicio de productos invalida", HttpStatus.INTERNAL_SERVER_ERROR);
            }
        } else {
            return new ResponseEntity<>("No se encontro el producto", HttpStatus.NOT_FOUND);
        }
    }
}
/*
@Service
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepository orderRepository;
    private final RestTemplate restTemplate;

    public ResponseEntity<Object> addOrder(Orders orders){

        Long id = orders.getProduct();
        Integer amount = orders.getAmount();
        String productServiceUrl = "http://localhost:8081/api/products/";
        //return new ResponseEntity<>("id: "+orders.getId()+" amount: ",HttpStatus.BAD_REQUEST);

        //ResponseEntity<Object> responseEntity = restTemplate.getForEntity(productServiceUrl + id, Object.class);
        ResponseEntity<Object> responseEntity;
        try {
            responseEntity = restTemplate.getForEntity(productServiceUrl + id, Object.class);
        } catch (HttpClientErrorException.NotFound e) {
            return new ResponseEntity<>("No se encontro el producto", HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>("Error al comunicarse con el servicio de productos", HttpStatus.INTERNAL_SERVER_ERROR);
        }

        if (responseEntity.getStatusCode() == HttpStatus.OK) {

            Map<String, Object> productMap = (Map<String, Object>) responseEntity.getBody();

            Number stockNumber = (Number) productMap.get("stock");
            Long stock = stockNumber.longValue();
            //Long stock = ((Integer) productMap.get("stock")).longValue();
            System.out.println("Stock: " + stock);

            if(amount <= stock){

                Map<String, Object> requestBody = new HashMap<>();
                requestBody.put("id", id);
                requestBody.put("amount", amount);
                productServiceUrl = "http://localhost:8081/api/products/stock";

                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);

                HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);

                ResponseEntity<String> responseEntity2;
                try {
                    responseEntity2 = restTemplate.exchange(
                            productServiceUrl,
                            HttpMethod.PUT,
                            requestEntity,
                            String.class
                    );
                } catch (Exception e) {
                    return new ResponseEntity<>("Error al actualizar el stock, no se grabara la orden", HttpStatus.INTERNAL_SERVER_ERROR);
                }

                if (responseEntity2.getStatusCode() == HttpStatus.OK){
                    orderRepository.save(orders);
                    return null;
                }else{
                    return new ResponseEntity<>("Error al actualizar el stock, no se grabara la orden", HttpStatus.BAD_REQUEST);
                }


            }else{
                return new ResponseEntity<>("La cantidad ingresada supera el stock disponible", HttpStatus.BAD_REQUEST);
            }

        } else {
            return new ResponseEntity<>("No se encontro el producto", HttpStatus.NOT_FOUND);
        }


    }
}*/
