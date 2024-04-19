package io.barth.sms.controllers;

import io.barth.sms.entity.ProductOrder;
import io.barth.sms.serviceImp.ProductOrderServiceImp;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("api/v1/orders")
public class ProductOrderController {

    private final ProductOrderServiceImp productOrderServiceImp;

    public ProductOrderController(ProductOrderServiceImp productOrderServiceImp) {
        this.productOrderServiceImp = productOrderServiceImp;
    }

    @GetMapping("/")
    public List<ProductOrder> getAllOrderItems() {
        return productOrderServiceImp.getProductOrder();
    }

    @PostMapping("/client/{clientId}/product/{productId}")
    public ResponseEntity<ProductOrder> createOrderItem(@PathVariable Long clientId,
                                                        @PathVariable Long productId,
                                                        @RequestBody ProductOrder order) {
        try {
            ProductOrder createdOrder = productOrderServiceImp.createProductOrder(clientId, productId, order);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdOrder);
        } catch (Exception ex){
            return ResponseEntity.internalServerError().build();
        }
    }

    @PutMapping("/client/{clientId}/order/{orderId}")
    public ResponseEntity<ProductOrder> updateOrder(@PathVariable Long clientId,
                                                    @PathVariable Long orderId,
                                                    @RequestBody ProductOrder order) {

        try {
            ProductOrder updatedOrder = productOrderServiceImp.updateProductOrder(clientId, orderId, order);
            return ResponseEntity.ok(updatedOrder);
        } catch (Exception ex){
            return new ResponseEntity<>(HttpStatus.BAD_GATEWAY);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductOrder> getOrderItemById(@PathVariable Long id) {
        Optional<ProductOrder> order = productOrderServiceImp.getProductOrderById(id);
        return order.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOrderItem(@PathVariable Long id) {
        productOrderServiceImp.deleteProductOrder(id);
        return ResponseEntity.noContent().build();
    }
}
