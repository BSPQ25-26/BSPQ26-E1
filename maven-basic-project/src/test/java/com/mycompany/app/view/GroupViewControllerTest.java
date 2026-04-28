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
class GroupViewControllerTest {

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
    void listGroupsNoToken() {
        ResponseEntity<String> response = restTemplate.getForEntity(
                "/web/groups", String.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().contains("Sign In"));
    }

    @Test
    void groupListShowsMyGroupsHeading() {
        ResponseEntity<String> loginResponse = restTemplate.postForEntity(
                "/web/auth/login?email=test@test.com&password=test", null, String.class);
        String setCookie = loginResponse.getHeaders().getFirst("Set-Cookie");
        assertNotNull(setCookie);
        String token = setCookie.split(";")[0];

        HttpHeaders headers = new HttpHeaders();
        headers.add("Cookie", token);
        ResponseEntity<String> response = restTemplate.exchange(
                "/web/groups", HttpMethod.GET, new HttpEntity<>(headers), String.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().contains("Sign In"));
        assertTrue(response.getBody().contains("My Groups"));
    }

    @Test
    void showGroupFormWithToken() {
        ResponseEntity<String> loginResponse = restTemplate.postForEntity(
                "/web/auth/login?email=test@test.com&password=test", null, String.class);
        String setCookie = loginResponse.getHeaders().getFirst("Set-Cookie");
        assertNotNull(setCookie);
        String token = setCookie.split(";")[0];

        HttpHeaders headers = new HttpHeaders();
        headers.add("Cookie", token);
        ResponseEntity<String> response = restTemplate.exchange(
                "/web/groups?showPanel=true", HttpMethod.GET, new HttpEntity<>(headers), String.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().contains("New Group"));
        assertTrue(response.getBody().contains("Group Name"));
    }

    @Test
    void createGroupInvalidTokenRedirects() {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Cookie", "token=invalid_token");

        ResponseEntity<String> response = restTemplate.exchange(
                "/web/groups/create?nombre=TestGroup", HttpMethod.POST,
                new HttpEntity<>(headers), String.class);

        assertEquals(HttpStatus.FOUND, response.getStatusCode());
    }

    @Test
    void addMemberNonExistentGroupRedirects() {
        ResponseEntity<String> loginResponse = restTemplate.postForEntity(
                "/web/auth/login?email=test@test.com&password=test", null, String.class);
        String setCookie = loginResponse.getHeaders().getFirst("Set-Cookie");
        assertNotNull(setCookie);
        String token = setCookie.split(";")[0];

        HttpHeaders headers = new HttpHeaders();
        headers.add("Cookie", token);
        ResponseEntity<String> response = restTemplate.exchange(
                "/web/groups/addMember?groupId=99999&userEmail=nobody@example.com",
                HttpMethod.POST, new HttpEntity<>(headers), String.class);

        // addMember catches any exception and redirects back to the groups page
        assertEquals(HttpStatus.FOUND, response.getStatusCode());
    }
}