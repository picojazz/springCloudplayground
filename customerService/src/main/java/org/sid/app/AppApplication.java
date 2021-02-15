package org.sid.app;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.data.rest.core.config.Projection;
import org.springframework.data.rest.core.config.RepositoryRestConfiguration;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity @Data @NoArgsConstructor @AllArgsConstructor @ToString
class Customer {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String email;
}


@SpringBootApplication
public class AppApplication {

    public static void main(String[] args) {
        SpringApplication.run(AppApplication.class, args);
    }
    @Bean
    CommandLineRunner start (CustomerRepository customerRepository, RepositoryRestConfiguration repositoryRestConfiguration){
        return args -> {
            repositoryRestConfiguration.exposeIdsFor(Customer.class);
            customerRepository.save(new Customer(null,"picojazz","picojazz@gmail.com"));
            customerRepository.save(new Customer(null,"ibrahima","ibrahima@gmail.com"));
            customerRepository.save(new Customer(null,"falilou","falilou@gmail.com"));
            customerRepository.findAll().forEach(System.out::println);
        };
    }

}

@RepositoryRestResource
interface CustomerRepository extends JpaRepository<Customer,Long>{

}
@Projection(name = "name", types = Customer.class)
interface CustomerProjection{
    public Long getId();
    public String getName();

}
