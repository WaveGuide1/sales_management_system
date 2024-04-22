package io.barth.sms.serviceImp;

import io.barth.sms.entity.Client;
import io.barth.sms.entity.Product;
import io.barth.sms.entity.ProductOrder;
import io.barth.sms.repository.ClientRepository;
import io.barth.sms.repository.ProductOrderRepository;
import io.barth.sms.repository.ProductRepository;
import io.barth.sms.service.ProductOrderService;
import io.barth.sms.utilities.ProductOrderBusinessLogic;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class ProductOrderServiceImp implements ProductOrderService {

    private final ProductOrderRepository productOrderRepository;
    private final ClientRepository clientRepository;
    private final ProductRepository productRepository;

    public ProductOrderServiceImp(ProductOrderRepository productOrderRepository,
                                  ClientRepository clientRepository,
                                  ProductRepository productRepository) {
        this.productOrderRepository = productOrderRepository;
        this.clientRepository = clientRepository;
        this.productRepository = productRepository;
    }

    @Override
    @Transactional
    public ProductOrder createProductOrder(Long clientId, Long productId, ProductOrder productOrder) {
        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new EntityNotFoundException("Client with id " + clientId + "not found"));

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new EntityNotFoundException("Product with id " +
                        productId + "not found"));

        // Checking whether there is enough product
        int quantity = ProductOrderBusinessLogic.quantityLogic(
                product.getQuantity(), productOrder.getQuantity()
        );

        product.setQuantity(quantity);
        productRepository.save(product);

        productOrder.setProduct(product);
        client.getProductOrder().add(productOrder);

        ProductOrder newOrder = productOrderRepository.save(productOrder);
        newOrder.setClient(productOrder.getClient());
        return newOrder;

    }

    @Override
    @Transactional
    public ProductOrder updateProductOrder(Long clientId, Long orderId, ProductOrder productOrder) {
        ProductOrder oldProductOrder = productOrderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("No order with id of " + orderId));

        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new EntityNotFoundException("No client with id of " + clientId));

        Product product = productRepository.findById(oldProductOrder.getProduct().getId())
                .orElseThrow(() -> new EntityNotFoundException("No available product"));

        if(client.getProductOrder().isEmpty())
            return null;

        int quantity = ProductOrderBusinessLogic.quantityLogic(
                product.getQuantity(), oldProductOrder.getQuantity(), productOrder.getQuantity()
        );
        product.setQuantity(quantity);
        productRepository.save(product);

        for (ProductOrder item: client.getProductOrder()){
            if(orderId.equals(item.getId())){
                oldProductOrder.setId(orderId);
                oldProductOrder.setQuantity(productOrder.getQuantity());
                break;
            }
        }
        return productOrderRepository.save(oldProductOrder);
    }

    @Override
    @Transactional
    public String confirmOrder(Long clientId, Long orderId) {
        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new EntityNotFoundException("No client with id of " + clientId));

        ProductOrder productOrder = productOrderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("No order with id " + orderId));

        for(ProductOrder clientOrder: client.getProductOrder()){
            if(productOrder.getConfirm()){
                return null;
            }
            else if (clientOrder.getId().equals(orderId)) {
                break;
            } else {
                return null;
            }
        }
        String productName = productOrder.getProduct().getProductName();
        productOrder.setConfirm(true);
        productOrderRepository.save(productOrder);
        return productName;
    }

    @Override
    public String cancelOrder(Long clientId, Long orderId) {

        ProductOrder productOrder = productOrderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("No product with id " + orderId));

        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new EntityNotFoundException("No client with id " + clientId));

        Product product = productRepository.findById(productOrder.getProduct().getId())
                .orElseThrow(() -> new EntityNotFoundException("No available product"));

        for(ProductOrder oldClient: client.getProductOrder()){
            if(oldClient.getId().equals(orderId)){
                int quantity = ProductOrderBusinessLogic.cancellationQuantity(product.getQuantity(),
                        oldClient.getQuantity());
                product.setQuantity(quantity);
                productRepository.save(product);
            } else {
                return null;
            }
        }
        productOrderRepository.deleteById(orderId);
        return "Order has been cancelled";
    }

    @Override
    public List<ProductOrder> getProductOrder(Long clientId) {
        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new EntityNotFoundException("Not a client"));

        if(client.getProductOrder() == null)
            return null;
        return client.getProductOrder();
    }

    @Override
    public Optional<ProductOrder> getProductOrderById(Long clientId, Long orderId) {
        clientRepository.findById(clientId)
                .orElseThrow(() -> new EntityNotFoundException("Not a client"));
        return Optional.ofNullable(productOrderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Client has no such order")));
    }

}
