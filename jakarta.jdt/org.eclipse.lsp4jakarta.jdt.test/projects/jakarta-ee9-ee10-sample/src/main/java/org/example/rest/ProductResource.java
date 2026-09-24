package org.example.rest;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.example.persistence.Product;
import org.example.persistence.ProductRepository;

import java.util.List;

/**
 * Sample REST resource using:
 *  - Jakarta REST (JAX-RS) 3.1   (EE 10)
 *  - Jakarta CDI 4.0             (EE 10)
 *  - Jakarta Bean Validation 3.0 (EE 10)
 *
 * Note: @Inject (jakarta.inject-api:2.0.1) removed — that version is identical across EE 9, EE 10
 * and EE 11, making it version-ambiguous. ProductRepository is instantiated directly instead.
 */
@Path("/products")
@ApplicationScoped
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ProductResource {

    private final ProductRepository productRepository = new ProductRepository();

    @GET
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    @GET
    @Path("/{id}")
    public Response getProduct(@PathParam("id") Long id) {
        Product product = productRepository.findById(id);
        if (product == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.ok(product).build();
    }

    @POST
    public Response createProduct(@Valid Product product) {
        productRepository.save(product);
        return Response.status(Response.Status.CREATED).entity(product).build();
    }

    @DELETE
    @Path("/{id}")
    public Response deleteProduct(@PathParam("id") Long id) {
        productRepository.delete(id);
        return Response.noContent().build();
    }
}
