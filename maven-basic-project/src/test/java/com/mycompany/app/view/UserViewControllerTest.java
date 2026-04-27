package com.mycompany.app.view;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class UserViewControllerTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void createUserFormLoads() {
        ResponseEntity<String> response = restTemplate.getForEntity(
                "/web/user/create", String.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().contains("Sign In"));
    }

    @Test
    void createUserReturnsCreatePage() {
        ResponseEntity<String> response = restTemplate.postForEntity(
                "/web/user/create?username=Test&email=test@test.com&password=test",
                null, String.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().contains("Sign In"));
    }

    @Test
    void profileNoToken() {
        ResponseEntity<String> response = restTemplate.getForEntity(
                "/web/user/profile", String.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().contains("Sign In"));
        assertFalse(response.getBody().contains("Current Balance"));
    }
}
