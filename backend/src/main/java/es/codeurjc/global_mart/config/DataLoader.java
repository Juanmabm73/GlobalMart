package es.codeurjc.global_mart.config;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.hibernate.engine.jdbc.BlobProxy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import es.codeurjc.global_mart.model.Review;
import es.codeurjc.global_mart.service.ProductService;
import es.codeurjc.global_mart.service.UserService;
import jakarta.annotation.PostConstruct;

@Component
public class DataLoader {

        @Autowired
        private ProductService productService;

        @Autowired
        private UserService userService;

        @Autowired
        private PasswordEncoder passwordEncoder;

        @PostConstruct
        public void loadData() throws IOException {

                if (!userService.getAllUsers().isEmpty())
                        return;

                byte[] image1 = new ClassPathResource("static/images/products/diariogreg.jpg")
                                .getInputStream().readAllBytes();
                byte[] image2 = new ClassPathResource("static/images/products/iphone16.jpg")
                                .getInputStream().readAllBytes();
                byte[] image3 = new ClassPathResource("static/images/products/macbook.jpg")
                                .getInputStream().readAllBytes();
                byte[] image4 = new ClassPathResource("static/images/products/at10.jpg")
                                .getInputStream().readAllBytes();
                byte[] image5 = new ClassPathResource("static/images/products/disco.jpg")
                                .getInputStream().readAllBytes();
                byte[] image6 = new ClassPathResource("static/images/products/lavadora_samsung.jpg")
                                .getInputStream().readAllBytes();
                byte[] image7 = new ClassPathResource("static/images/products/tv_55.jpg")
                                .getInputStream().readAllBytes();
                byte[] image8 = new ClassPathResource("static/images/products/conjunto-mesa-sillas.jpg")
                                .getInputStream().readAllBytes();
                byte[] image9 = new ClassPathResource("static/images/products/cortacesped.jpeg")
                                .getInputStream().readAllBytes();
                byte[] image10 = new ClassPathResource("static/images/products/zapatillas.jpg")
                                .getInputStream().readAllBytes();
                byte[] image11 = new ClassPathResource("static/images/products/codigo_davinci.jpg")
                                .getInputStream().readAllBytes();
                byte[] image12 = new ClassPathResource("static/images/products/tablet_galaxy.jpg")
                                .getInputStream().readAllBytes();
                byte[] image13 = new ClassPathResource("static/images/products/smartwatch.jpg")
                                .getInputStream().readAllBytes();
                byte[] image14 = new ClassPathResource("static/images/products/cafetera.jpg")
                                .getInputStream().readAllBytes();
                byte[] image15 = new ClassPathResource("static/images/products/mancuernas.jpg")
                                .getInputStream().readAllBytes();
                byte[] image16 = new ClassPathResource("static/images/products/don_quijote.jpg")
                                .getInputStream().readAllBytes();

                // Create and associate reviews before persisting
                Review review1 = new Review("user1", "Muy bueno", 5);
                Review review2 = new Review("user1", "Muy malo", 1);

                // Create product
                productService.createProduct("Books", "Libro El Quijote", "LaCasaDelLibro", 20.0,
                                "Una versión abreviada de las aventuras de un excéntrico caballero rural y su fiel compañero...",
                                BlobProxy.generateProxy(image16), 10, true, List.of(review1, review2));

                productService.createProduct("Books", "Producto1", "Amazon", 20.0, "Muy chulo",
                                BlobProxy.generateProxy(image1), 10, true, null);
                productService.createProduct("Technology", "Producto2", "eBay", 30.0, "Muy útil",
                                BlobProxy.generateProxy(image2), 10, true, null);
                productService.createProduct("Technology", "Producto3", "comp", 40.0, "Muy práctico", BlobProxy
                                .generateProxy(image3), 10,
                                true, null);
                productService.createProduct("Sports", "Producto4", "Decathlon", 50.0, "Muy resistente",
                                BlobProxy.generateProxy(image4), 10,
                                true, null);
                productService.createProduct("Music", "Producto5", "Zara", 60.0, "Muy elegante",
                                BlobProxy.generateProxy(image5), 10, true, null);
                productService.createProduct("Appliances", "Lavadora Samsung", "ElectroMax", 399.99,
                                "Lavadora de carga frontal con tecnología EcoBubble",
                                BlobProxy.generateProxy(image6), 5, true, null);
                productService.createProduct("Technology", "Televisor LED 55\"", "MediaMarkt", 549.99,
                                "Televisor UHD 4K con Smart TV integrado",
                                BlobProxy.generateProxy(image7), 8, true, null);
                productService.createProduct("Appliances", "Conjunto mesa y sillas", "IKEA", 199.99,
                                "Set de comedor moderno con 4 sillas",
                                BlobProxy.generateProxy(image8), 3, true, null);
                productService.createProduct("Appliances", "Cortacésped eléctrico", "Jardiland", 149.95,
                                "Potente cortacésped con batería recargable",
                                BlobProxy.generateProxy(image9), 7, true, null);
                productService.createProduct("Sports", "Zapatillas deportivas", "Nike", 89.99,
                                "Zapatillas ligeras para running",
                                BlobProxy.generateProxy(image10), 15, true, null);
                productService.createProduct("Books", "El Código Da Vinci", "Casa del Libro", 15.50,
                                "Bestseller mundial de Dan Brown",
                                BlobProxy.generateProxy(image11), 20, true, null);
                productService.createProduct("Technology", "Tablet Galaxy", "Samsung", 299.99,
                                "Tablet con pantalla AMOLED de 10.5\"",
                                BlobProxy.generateProxy(image12), 6, true, null);
                productService.createProduct("Technology", "Smartwatch Fitness", "GadgetWorld", 129.99,
                                "Reloj inteligente con monitorización de actividad física y notificaciones",
                                BlobProxy.generateProxy(image13), 12, true, null);
                productService.createProduct("Appliances", "Cafetera Automática", "HomeAppliances", 89.95,
                                "Cafetera programable con molinillo de granos integrado",
                                BlobProxy.generateProxy(image14), 8, true, null);
                productService.createProduct("Sports", "Mancuernas Ajustables", "FitnessDepot", 199.95,
                                "Juego de mancuernas con pesos intercambiables de 2 a 20 kg",
                                BlobProxy.generateProxy(image15), 4, true, null);

                userService.createUser(null, "User 1", "user1", "user1@gmail.com", passwordEncoder.encode("user1"),
                                Arrays.asList("ADMIN"));

                userService.createUser(null, "comp", "comp", "user1@gmail.com", passwordEncoder.encode("comp"),
                                Arrays.asList("COMPANY"));

                userService.createUser(null, "a", "a", "a@gmail.com", passwordEncoder.encode("a"),
                                Arrays.asList("USER"));

        }

}