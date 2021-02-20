package org.sid.app;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.data.rest.core.config.Projection;
import org.springframework.hateoas.PagedModel;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import javax.persistence.*;
import java.util.Collection;
import java.util.Date;


@Entity @Data @AllArgsConstructor @NoArgsConstructor @ToString
class Bill {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long Id;
    private Date BillingDate;
    @JsonProperty(access =JsonProperty.Access.WRITE_ONLY)
    private Long customerId;
    @Transient
    private Customer customer;
    @OneToMany(mappedBy = "bill")
    private Collection<ProductItem> productItems;
}
@Entity @Data @AllArgsConstructor @NoArgsConstructor @ToString
class ProductItem{
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long Id;
    @JsonProperty(access =JsonProperty.Access.WRITE_ONLY)
    private Long productId;
    @Transient
    private Product product;
    private double price;
    private double quatity;
    @ManyToOne @JsonProperty(access =JsonProperty.Access.WRITE_ONLY)
    private Bill bill;
}

@RepositoryRestResource
interface BillRepository extends JpaRepository<Bill,Long>{
}
@RepositoryRestResource
interface ProductItemRepository extends JpaRepository<ProductItem,Long>{
}
@Projection(name = "fullBill", types = Bill.class)
interface fullBill{
    public Long getId();
    public Date getBillingDate();
    public Long getCustomerId();
    public Collection<ProductItem> getProductItems();
}

@Data
class Customer{
    private Long Id;
    private String name;
    private String email;
}

@FeignClient(name = "CUSTOMER-SERVICE")
interface CustomerService{
    @GetMapping("/customers/{id}")
    public Customer findCustomerById(@PathVariable Long id);
}
@Data
class Product{
    private Long id;
    private String name;
    private double price;
}

@FeignClient(name = "INVENTORY-SERVICE")
interface InventoryService{
    @GetMapping("/products/{id}")
    public Product findProductById(@PathVariable Long id);
    @GetMapping("/products")
    public PagedModel<Product> getAllProducts();
}

@RestController
class BillRestController{
    @Autowired
    private BillRepository billRepository;
    @Autowired
    private ProductItemRepository productItemRepository;
    @Autowired
    private CustomerService customerService;
    @Autowired
    private InventoryService inventoryService;


    @GetMapping("/fullBill/{id}")
    public Bill getBill(@PathVariable Long id){
        Bill bill = billRepository.findById(id).get();
        bill.setCustomer(customerService.findCustomerById(bill.getCustomerId()));
        bill.getProductItems().forEach(p ->{
            p.setProduct(inventoryService.findProductById(p.getProductId()));
        });
        return bill;
    }

}

@SpringBootApplication
@EnableFeignClients
public class AppApplication {

    public static void main(String[] args) {
        SpringApplication.run(AppApplication.class, args);
    }
    @Bean
    CommandLineRunner start(BillRepository billRepository,ProductItemRepository productItemRepository,
                            CustomerService customerService,InventoryService inventoryService){
        return args -> {
           Customer c1 = customerService.findCustomerById(1L);
           /*Product p1 = inventoryService.findProductById(1L);
            Product p2 = inventoryService.findProductById(2L);*/
            Bill bill1 = billRepository.save(new Bill(null,new Date(),1L,null,null));
            PagedModel<Product> products = inventoryService.getAllProducts();
            products.getContent().forEach(p ->{
                productItemRepository.save(new ProductItem(null,p.getId(),null,p.getPrice(),21,bill1));
            });





        };
    }

}
