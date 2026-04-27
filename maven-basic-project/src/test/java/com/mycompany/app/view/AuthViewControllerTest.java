package com.mycompany.app.view;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class AuthViewControllerTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void loginWrongCreds() {
        ResponseEntity<String> response = restTemplate.postForEntity(
                "/web/auth/login?email=wrong@example.com&password=badpassword",
                null, String.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().contains("Invalid email or password."));
        assertFalse(response.getBody().contains("Current Balance"));
    }

    @Test
    void logoutEndSession() {
        ResponseEntity<String> response = restTemplate.postForEntity(
                "/web/auth/logout", null, String.class);
        assertEquals(HttpStatus.FOUND, response.getStatusCode());
    }

    @Test
    void userInfoNoToken() {
        ResponseEntity<String> response = restTemplate.getForEntity(
                "/web/user/info", String.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().contains("Sign In"));
        assertFalse(response.getBody().contains("Current Balance"));
    }

    @Test
    void loginCorrectCreds() {
        ResponseEntity<String> loginResponse = restTemplate.postForEntity(
                "/web/auth/login?email=test@test.com&password=test", null, String.class);

        assertEquals(HttpStatus.FOUND, loginResponse.getStatusCode());

        String setCookie = loginResponse.getHeaders().getFirst("Set-Cookie");
        assertNotNull(setCookie);
        String token = setCookie.split(";")[0]; // gives "token=abc123"

        HttpHeaders headers = new HttpHeaders();
        headers.add("Cookie", token);
        ResponseEntity<String> profileResponse = restTemplate.exchange(
                "/web/user/profile", HttpMethod.GET, new HttpEntity<>(headers), String.class);

        assertEquals(HttpStatus.OK, profileResponse.getStatusCode());
        assertNotNull(profileResponse.getBody());
        assertTrue(profileResponse.getBody().contains("test@test.com"));
    }
}