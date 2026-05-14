package com.mycompany.app.view;

import com.mycompany.app.model.Usuario;
import com.mycompany.app.repository.CategoryRepository;
import com.mycompany.app.repository.GroupRepository;
import com.mycompany.app.repository.SavingGoalsRepository;
import com.mycompany.app.repository.TransactionRepository;
import com.mycompany.app.repository.UsuarioRepository;
import com.mycompany.app.service.ChatService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class AcceptanceViewTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @MockBean
    @SuppressWarnings("unused")
    private ChatService chatService;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private SavingGoalsRepository savingGoalsRepository;

    private static final String TEST_EMAIL    = "acceptance@test.com";
    private static final String TEST_PASSWORD = "acceptance";

    // If tests fail this method deletes the data created
    @AfterEach
    @Transactional
    void cleanUp() {
        Usuario user = usuarioRepository.findByEmail(TEST_EMAIL);
        if (user != null) {
            transactionRepository.deleteAll(transactionRepository.findByCreadorId(user.getId()));
            savingGoalsRepository.deleteByUser(user);
            categoryRepository.deleteAll(categoryRepository.findByUserId(user.getId()));
            groupRepository.deleteAll(groupRepository.findByMiembrosEmail(TEST_EMAIL));
            usuarioRepository.delete(user);
        }
    }

    @Test
    void fullUserWorkflow() {
        // Acceptance test for regular user workflow
        // 1. Create user
        ResponseEntity<String> createUserResp = restTemplate.postForEntity(
                "/web/user/create?username=AcceptanceUser&email=" + TEST_EMAIL + "&password=" + TEST_PASSWORD + "&balance=0",
                null, String.class);
        assertEquals(HttpStatus.OK, createUserResp.getStatusCode());
        assertNotNull(createUserResp.getBody());
        assertTrue(createUserResp.getBody().contains("Account created successfully"));

        // 2. Login
        ResponseEntity<String> loginResp = restTemplate.postForEntity(
                "/web/auth/login?email=" + TEST_EMAIL + "&password=" + TEST_PASSWORD,
                null, String.class);
        assertEquals(HttpStatus.FOUND, loginResp.getStatusCode());
        String setCookie = loginResp.getHeaders().getFirst("Set-Cookie");
        assertNotNull(setCookie);
        String tokenCookie = setCookie.split(";")[0];

        HttpHeaders headers = new HttpHeaders();
        headers.add("Cookie", tokenCookie);

        // Check if profile is accesible
        ResponseEntity<String> profileAfterLogin = restTemplate.exchange(
                "/web/user/profile", HttpMethod.GET, new HttpEntity<>(headers), String.class);
        assertEquals(HttpStatus.OK, profileAfterLogin.getStatusCode());
        assertNotNull(profileAfterLogin.getBody());
        assertTrue(profileAfterLogin.getBody().contains(TEST_EMAIL));

        // 3. Create group
        ResponseEntity<String> createGroupResp = restTemplate.exchange(
                "/web/groups/create?nombre=AcceptanceGroup&descripcion=TestGroup",
                HttpMethod.POST, new HttpEntity<>(headers), String.class);
        assertEquals(HttpStatus.FOUND, createGroupResp.getStatusCode());

        ResponseEntity<String> groupsPage = restTemplate.exchange(
                "/web/groups", HttpMethod.GET, new HttpEntity<>(headers), String.class);
        assertEquals(HttpStatus.OK, groupsPage.getStatusCode());
        assertNotNull(groupsPage.getBody());
        assertTrue(groupsPage.getBody().contains("AcceptanceGroup"));

        // Claude code solution for hidden input
        // Extract group ID from the hidden input that follows the group name in the page
        // Rendered HTML: <h3 class="group-name">AcceptanceGroup</h3> ... <input name="groupId" value="X">
        String groupsHtml   = groupsPage.getBody();
        int    gIdStart     = groupsHtml.indexOf("name=\"groupId\" value=\"",
                              groupsHtml.indexOf("AcceptanceGroup")) + "name=\"groupId\" value=\"".length();
        String groupId      = groupsHtml.substring(gIdStart, groupsHtml.indexOf('"', gIdStart));

        // 4. Create expense group
        ResponseEntity<String> createGroupTxResp = restTemplate.exchange(
                "/web/transaction/create?concepto=GroupExpense&importeTotal=50.0&tipoTransaccion=GASTO&grupoId=" + groupId,
                HttpMethod.POST, new HttpEntity<>(headers), String.class);
        assertEquals(HttpStatus.FOUND, createGroupTxResp.getStatusCode());

        ResponseEntity<String> txPageAfterGroup = restTemplate.exchange(
                "/web/transaction", HttpMethod.GET, new HttpEntity<>(headers), String.class);
        assertEquals(HttpStatus.OK, txPageAfterGroup.getStatusCode());
        assertNotNull(txPageAfterGroup.getBody());
        assertTrue(txPageAfterGroup.getBody().contains("GroupExpense"));
        assertTrue(txPageAfterGroup.getBody().contains("AcceptanceGroup"));

        // Extract transaction ID: data-id comes before data-concepto on the same <tr>
        // Rendered HTML: <tr ... data-id="X" data-concepto="GroupExpense" ...>
        String txHtml1      = txPageAfterGroup.getBody();
        int    gtConcepto   = txHtml1.indexOf("data-concepto=\"GroupExpense\"");
        int    gtTrStart    = txHtml1.lastIndexOf("<tr", gtConcepto);
        int    gtIdStart    = txHtml1.indexOf("data-id=\"", gtTrStart) + "data-id=\"".length();
        String groupTxId    = txHtml1.substring(gtIdStart, txHtml1.indexOf('"', gtIdStart));

        // 5. Set a savings goal
        ResponseEntity<String> createGoalResp = restTemplate.exchange(
                "/web/user/objetivo/create?amount=500.0&endDate=2026-12-31",
                HttpMethod.POST, new HttpEntity<>(headers), String.class);
        assertEquals(HttpStatus.FOUND, createGoalResp.getStatusCode());

        ResponseEntity<String> profileWithGoal = restTemplate.exchange(
                "/web/user/profile", HttpMethod.GET, new HttpEntity<>(headers), String.class);
        assertEquals(HttpStatus.OK, profileWithGoal.getStatusCode());
        assertNotNull(profileWithGoal.getBody());
        assertFalse(profileWithGoal.getBody().contains("Sign In"));
        assertTrue(profileWithGoal.getBody().contains("objetivo/delete"));
        assertTrue(profileWithGoal.getBody().contains("500"));

        // 6. Create category
        ResponseEntity<String> createCategoryResp = restTemplate.exchange(
                "/web/categories/create?name=AcceptanceCategory",
                HttpMethod.POST, new HttpEntity<>(headers), String.class);
        assertEquals(HttpStatus.FOUND, createCategoryResp.getStatusCode());

        ResponseEntity<String> categoriesPage = restTemplate.exchange(
                "/web/categories", HttpMethod.GET, new HttpEntity<>(headers), String.class);
        assertEquals(HttpStatus.OK, categoriesPage.getStatusCode());
        assertNotNull(categoriesPage.getBody());
        assertTrue(categoriesPage.getBody().contains("AcceptanceCategory"));

        // Extract category ID from the hidden input that follows the category name in the page
        // Rendered HTML: <h3>AcceptanceCategory</h3> ... <input name="categoryId" value="X">
        String catHtml      = categoriesPage.getBody();
        int    catNamePos   = catHtml.indexOf(">AcceptanceCategory<");
        int    catIdStart   = catHtml.indexOf("name=\"categoryId\" value=\"", catNamePos)
                              + "name=\"categoryId\" value=\"".length();
        String categoryId   = catHtml.substring(catIdStart, catHtml.indexOf('"', catIdStart));

        // 7. Create an expense using that category
        ResponseEntity<String> createCategoryTxResp = restTemplate.exchange(
                "/web/transaction/create?concepto=CategoryExpense&importeTotal=25.0&tipoTransaccion=GASTO&categoriaId=" + categoryId,
                HttpMethod.POST, new HttpEntity<>(headers), String.class);
        assertEquals(HttpStatus.FOUND, createCategoryTxResp.getStatusCode());

        ResponseEntity<String> txPageFull = restTemplate.exchange(
                "/web/transaction", HttpMethod.GET, new HttpEntity<>(headers), String.class);
        assertEquals(HttpStatus.OK, txPageFull.getStatusCode());
        assertNotNull(txPageFull.getBody());
        assertTrue(txPageFull.getBody().contains("GroupExpense"));
        assertTrue(txPageFull.getBody().contains("CategoryExpense"));

        // Extract category transaction ID
        String txHtml2      = txPageFull.getBody();
        int    ctConcepto   = txHtml2.indexOf("data-concepto=\"CategoryExpense\"");
        int    ctTrStart    = txHtml2.lastIndexOf("<tr", ctConcepto);
        int    ctIdStart    = txHtml2.indexOf("data-id=\"", ctTrStart) + "data-id=\"".length();
        String categoryTxId = txHtml2.substring(ctIdStart, txHtml2.indexOf('"', ctIdStart));

        // 8. Delete category expense
        ResponseEntity<String> deleteCategoryTxResp = restTemplate.exchange(
                "/web/transaction/delete?transactionId=" + categoryTxId,
                HttpMethod.POST, new HttpEntity<>(headers), String.class);
        assertEquals(HttpStatus.FOUND, deleteCategoryTxResp.getStatusCode());

        ResponseEntity<String> txAfterCatDelete = restTemplate.exchange(
                "/web/transaction", HttpMethod.GET, new HttpEntity<>(headers), String.class);
        assertNotNull(txAfterCatDelete.getBody());
        assertFalse(txAfterCatDelete.getBody().contains("CategoryExpense"));

        // 9. Delete group expense
        ResponseEntity<String> deleteGroupTxResp = restTemplate.exchange(
                "/web/transaction/delete?transactionId=" + groupTxId,
                HttpMethod.POST, new HttpEntity<>(headers), String.class);
        assertEquals(HttpStatus.FOUND, deleteGroupTxResp.getStatusCode());

        ResponseEntity<String> txAfterGroupDelete = restTemplate.exchange(
                "/web/transaction", HttpMethod.GET, new HttpEntity<>(headers), String.class);
        assertNotNull(txAfterGroupDelete.getBody());
        assertFalse(txAfterGroupDelete.getBody().contains("GroupExpense"));

        // 10. Delete savings goal
        ResponseEntity<String> deleteGoalResp = restTemplate.exchange(
                "/web/user/objetivo/delete",
                HttpMethod.POST, new HttpEntity<>(headers), String.class);
        assertEquals(HttpStatus.FOUND, deleteGoalResp.getStatusCode());

        ResponseEntity<String> profileAfterGoalDelete = restTemplate.exchange(
                "/web/user/profile", HttpMethod.GET, new HttpEntity<>(headers), String.class);
        assertNotNull(profileAfterGoalDelete.getBody());
        assertFalse(profileAfterGoalDelete.getBody().contains("objetivo/delete"));

        // 11. Delete category
        ResponseEntity<String> deleteCategoryResp = restTemplate.exchange(
                "/web/categories/delete?categoryId=" + categoryId,
                HttpMethod.POST, new HttpEntity<>(headers), String.class);
        assertEquals(HttpStatus.FOUND, deleteCategoryResp.getStatusCode());

        ResponseEntity<String> catPageAfterDelete = restTemplate.exchange(
                "/web/categories", HttpMethod.GET, new HttpEntity<>(headers), String.class);
        assertNotNull(catPageAfterDelete.getBody());
        assertFalse(catPageAfterDelete.getBody().contains("AcceptanceCategory"));

        // 12. Delete group
        ResponseEntity<String> deleteGroupResp = restTemplate.exchange(
                "/web/groups/delete?groupId=" + groupId,
                HttpMethod.POST, new HttpEntity<>(headers), String.class);
        assertEquals(HttpStatus.FOUND, deleteGroupResp.getStatusCode());

        ResponseEntity<String> groupsPageAfterDelete = restTemplate.exchange(
                "/web/groups", HttpMethod.GET, new HttpEntity<>(headers), String.class);
        assertNotNull(groupsPageAfterDelete.getBody());
        assertFalse(groupsPageAfterDelete.getBody().contains("AcceptanceGroup"));

        // 13. Logout to clear the in-memory token, then delete the user via repository.
        //     After deletion, attempting to log in again must fail — verifying the user is gone.
        restTemplate.exchange("/web/auth/logout", HttpMethod.POST, new HttpEntity<>(headers), String.class);

        Usuario user = usuarioRepository.findByEmail(TEST_EMAIL);
        assertNotNull(user);
        usuarioRepository.delete(user);

        ResponseEntity<String> loginAfterDelete = restTemplate.postForEntity(
                "/web/auth/login?email=" + TEST_EMAIL + "&password=" + TEST_PASSWORD,
                null, String.class);
        assertEquals(HttpStatus.OK, loginAfterDelete.getStatusCode());
        assertNotNull(loginAfterDelete.getBody());
        assertTrue(loginAfterDelete.getBody().contains("Invalid email or password"));
    }
}