package org.example.persistence;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;

import java.util.List;

/**
 * Sample repository using:
 *  - Jakarta Persistence 3.1  (EE 10)
 *  - Jakarta Annotation 2.1   (EE 10)
 *
 * Note: @Stateless (jakarta.ejb-api:4.0.1) removed — that version is identical in EE 10 and EE 11,
 * making it version-ambiguous. Replaced by jakarta.mail-api:2.1.3 as the EE 10 marker dependency.
 */
public class ProductRepository {

    @PersistenceContext(unitName = "samplePU")
    private EntityManager entityManager;

    @PostConstruct
    public void init() {
        System.out.println("ProductRepository initialized.");
    }

    @PreDestroy
    public void destroy() {
        System.out.println("ProductRepository destroyed.");
    }

    public void save(Product product) {
        entityManager.persist(product);
    }

    public Product findById(Long id) {
        return entityManager.find(Product.class, id);
    }

    public List<Product> findAll() {
        TypedQuery<Product> query = entityManager.createQuery(
                "SELECT p FROM Product p", Product.class);
        return query.getResultList();
    }

    public void delete(Long id) {
        Product product = findById(id);
        if (product != null) {
            entityManager.remove(product);
        }
    }
}
