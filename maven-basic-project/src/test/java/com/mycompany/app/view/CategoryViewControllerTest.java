package com.mycompany.app.view;

import com.mycompany.app.model.Usuario;
import com.mycompany.app.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class CategoryViewControllerTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @BeforeEach
    void setUp() {
        if (usuarioRepository.findByEmail("test@test.com") == null) {
            usuarioRepository.save(new Usuario("Test", "test@test.com", "test", 0.0));
        }
    }

    @Test
    void listCategoriesNoToken() {
        ResponseEntity<String> response = restTemplate.getForEntity(
                "/web/categories", String.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().contains("Sign In"));
    }

    @Test
    void showCategoryFormWithToken() {
        ResponseEntity<String> loginResponse = restTemplate.postForEntity(
                "/web/auth/login?email=test@test.com&password=test", null, String.class);
        String setCookie = loginResponse.getHeaders().getFirst("Set-Cookie");
        assertNotNull(setCookie);
        String token = setCookie.split(";")[0];

        HttpHeaders headers = new HttpHeaders();
        headers.add("Cookie", token);
        ResponseEntity<String> response = restTemplate.exchange(
                "/web/categories?showForm=true", HttpMethod.GET, new HttpEntity<>(headers), String.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().contains("New Category"));
        assertTrue(response.getBody().contains("Category Name"));
    }

    @Test
    void createCategoryInvalidTokenRedirects() {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Cookie", "token=invalid_token");

        ResponseEntity<String> response = restTemplate.exchange(
                "/web/categories/create?name=TestCategory", HttpMethod.POST,
                new HttpEntity<>(headers), String.class);

        assertEquals(HttpStatus.FOUND, response.getStatusCode());
    }
}