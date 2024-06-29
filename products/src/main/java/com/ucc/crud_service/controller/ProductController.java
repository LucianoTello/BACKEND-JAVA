package com.ucc.crud_service.controller;

import com.ucc.crud_service.model.Product;
import com.ucc.crud_service.service.ProductService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @Value("${jwt.secret}")
    private String secretKey;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<Product> getProducts(){
        return this.productService.getProducts();
    }
/*
    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public Product getoneproduct(@PathVariable Long id){ return this.productService.getoneproduct(id); }*/

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<Product> getoneproduct(@PathVariable Long id) {
        Product product = productService.getoneproduct(id);
        if (product == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } else {
            return new ResponseEntity<>(product, HttpStatus.OK);
        }
    }

/*
    @PostMapping
    @ResponseStatus(HttpStatus.OK)
    public void newProduct(@RequestBody Product product){
        this.productService.addproduct(product);
    }*/
    @PostMapping
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<Object> newProduct(@RequestHeader("Authorization") String authorizationHeader, @Valid @RequestBody Product product, BindingResult bindingResult){

        String jwt = authorizationHeader.substring(7);

        try {
            Claims claims = Jwts.parser().setSigningKey(secretKey.getBytes()).parseClaimsJws(jwt).getBody();
            String username = claims.getSubject();

        } catch (Exception e) {
            return new ResponseEntity<>("Token JWT inválido o expirado", HttpStatus.UNAUTHORIZED);
        }

        if(bindingResult.hasErrors()){

            List <String> errors = bindingResult.getAllErrors().stream()
                    .map(DefaultMessageSourceResolvable::getDefaultMessage)
                    .collect(Collectors.toList());

            return new ResponseEntity<>(errors,HttpStatus.BAD_REQUEST);
        }

        return productService.addproduct(product);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<String> deleteProduct(@RequestHeader("Authorization") @NotNull String authorizationHeader, @PathVariable Long id) {

        //@RequestHeader(value = "Authorization", required = false) String authorizationHeader
        /*if (authorizationHeader == null || authorizationHeader.isEmpty()) {
            return new ResponseEntity<>("No ha ingresado su token", HttpStatus.UNAUTHORIZED);
        }*/

        String jwt = authorizationHeader.substring(7);

        try {
            Claims claims = Jwts.parser().setSigningKey(secretKey.getBytes()).parseClaimsJws(jwt).getBody();
            String username = claims.getSubject();

        } catch (Exception e) {
            return new ResponseEntity<>("Token JWT inválido o expirado", HttpStatus.UNAUTHORIZED);
        }

        try {
            this.productService.deleteproduct(id);
            return ResponseEntity.ok("Producto eliminado exitosamente");
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }

    }

    @PutMapping
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<Object> updateProduct(@RequestHeader("Authorization") @NotNull String authorizationHeader, @RequestBody Product product){

        String jwt = authorizationHeader.substring(7);

        try {
            Claims claims = Jwts.parser()
                    .setSigningKey(secretKey.getBytes())
                    .parseClaimsJws(jwt)
                    .getBody();
            String username = claims.getSubject();

        } catch (Exception e) {
            return new ResponseEntity<>("Token JWT inválido o expirado", HttpStatus.UNAUTHORIZED);
        }

        return productService.updateproduct(product);
        /*try {
            this.productService.updateproduct(product);
            return ResponseEntity.ok("Producto modificado exitosamente");
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }*/
    }

    @PutMapping("/stock")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<Object> updateStock(@RequestBody Map<String, Object> requestBody){

        Long id = ((Number) requestBody.get("id")).longValue();
        Integer amount = (Integer) requestBody.get("amount");

        Product product = productService.getoneproduct(id);
        if(product != null){
            Long stock = product.getStock();
            Long newstock = (stock - amount);

            product.setStock(newstock);
            this.productService.updateproduct(product);
            return new ResponseEntity<>("Stock actualizado con exito!",HttpStatus.OK);
        }else{
            return new ResponseEntity<>("No se pudo actualizar el Stock",HttpStatus.BAD_REQUEST);
        }

    }
}