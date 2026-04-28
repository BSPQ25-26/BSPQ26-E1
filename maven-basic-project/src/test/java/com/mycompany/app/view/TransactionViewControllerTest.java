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
class TransactionViewControllerTest {

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
    void listTransactionsNoToken() {
        ResponseEntity<String> response = restTemplate.getForEntity(
                "/web/transaction", String.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().contains("Sign In"));
    }

    @Test
    void listTransactionsWithToken() {
        ResponseEntity<String> loginResponse = restTemplate.postForEntity(
                "/web/auth/login?email=test@test.com&password=test", null, String.class);
        String setCookie = loginResponse.getHeaders().getFirst("Set-Cookie");
        assertNotNull(setCookie);
        String token = setCookie.split(";")[0];

        HttpHeaders headers = new HttpHeaders();
        headers.add("Cookie", token);
        ResponseEntity<String> response = restTemplate.exchange(
                "/web/transaction", HttpMethod.GET, new HttpEntity<>(headers), String.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().contains("Sign In"));
        assertTrue(response.getBody().contains("My Transactions"));
    }

    @Test
    void createTransactionMissingConcepto() {
        ResponseEntity<String> loginResponse = restTemplate.postForEntity(
                "/web/auth/login?email=test@test.com&password=test", null, String.class);
        String setCookie = loginResponse.getHeaders().getFirst("Set-Cookie");
        assertNotNull(setCookie);
        String token = setCookie.split(";")[0];

        HttpHeaders headers = new HttpHeaders();
        headers.add("Cookie", token);
        ResponseEntity<String> response = restTemplate.exchange(
                "/web/transaction/create?concepto=&importeTotal=10.0&tipoTransaccion=GASTO",
                HttpMethod.POST, new HttpEntity<>(headers), String.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().contains("Please fill in all required fields."));
    }

    @Test
    void createTransactionInvalidAmount() {
        ResponseEntity<String> loginResponse = restTemplate.postForEntity(
                "/web/auth/login?email=test@test.com&password=test", null, String.class);
        String setCookie = loginResponse.getHeaders().getFirst("Set-Cookie");
        assertNotNull(setCookie);
        String token = setCookie.split(";")[0];

        HttpHeaders headers = new HttpHeaders();
        headers.add("Cookie", token);
        ResponseEntity<String> response = restTemplate.exchange(
                "/web/transaction/create?concepto=Test&importeTotal=0&tipoTransaccion=GASTO",
                HttpMethod.POST, new HttpEntity<>(headers), String.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().contains("Please fill in all required fields."));
    }

    @Test
    void editWithDebtsNonExistentRedirects() {
        ResponseEntity<String> loginResponse = restTemplate.postForEntity(
                "/web/auth/login?email=test@test.com&password=test", null, String.class);
        String setCookie = loginResponse.getHeaders().getFirst("Set-Cookie");
        assertNotNull(setCookie);
        String token = setCookie.split(";")[0];

        HttpHeaders headers = new HttpHeaders();
        headers.add("Cookie", token);
        ResponseEntity<String> response = restTemplate.exchange(
                "/web/transaction/edit-with-debts/99999?concepto=Test&importeTotal=10.0&tipoTransaccion=GASTO",
                HttpMethod.POST, new HttpEntity<>(headers), String.class);

        // Non-existent transaction is silently ignored and the endpoint redirects normally
        assertEquals(HttpStatus.FOUND, response.getStatusCode());
    }

    @Test
    void createTransactionInvalidTokenRedirects() {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Cookie", "token=invalid_token");

        ResponseEntity<String> response = restTemplate.exchange(
                "/web/transaction/create?concepto=Test&importeTotal=10.0&tipoTransaccion=GASTO",
                HttpMethod.POST, new HttpEntity<>(headers), String.class);

        assertEquals(HttpStatus.FOUND, response.getStatusCode());
    }

    @Test
    void editTransactionNonExistentRedirects() {
        ResponseEntity<String> loginResponse = restTemplate.postForEntity(
                "/web/auth/login?email=test@test.com&password=test", null, String.class);
        String setCookie = loginResponse.getHeaders().getFirst("Set-Cookie");
        assertNotNull(setCookie);
        String token = setCookie.split(";")[0];

        HttpHeaders headers = new HttpHeaders();
        headers.add("Cookie", token);
        ResponseEntity<String> response = restTemplate.exchange(
                "/web/transaction/edit/99999?concepto=Test&importeTotal=10.0&tipoTransaccion=GASTO",
                HttpMethod.POST, new HttpEntity<>(headers), String.class);

        // findById silently skips a missing ID and redirects back to the list
        assertEquals(HttpStatus.FOUND, response.getStatusCode());
    }
}
