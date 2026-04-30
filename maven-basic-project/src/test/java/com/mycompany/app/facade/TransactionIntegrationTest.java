package com.mycompany.app.facade;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.github.noconnor.junitperf.JUnitPerfInterceptor;
import com.github.noconnor.junitperf.JUnitPerfTest;
import com.github.noconnor.junitperf.JUnitPerfTestRequirement;
import com.github.noconnor.junitperf.JUnitPerfReportingConfig;
import com.github.noconnor.junitperf.JUnitPerfTestActiveConfig;
import com.github.noconnor.junitperf.reporting.providers.HtmlReportGenerator;
import org.junit.jupiter.api.extension.ExtendWith;

import org.junit.Rule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
//import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
// import org.springframework.test.context.DynamicPropertyRegistry;
// import org.springframework.test.context.DynamicPropertySource;
// import org.testcontainers.containers.PostgreSQLContainer;
// import org.testcontainers.junit.jupiter.Container;
// import org.testcontainers.junit.jupiter.Testcontainers;

import com.mycompany.app.dto.DeudaCreationDTO;
import com.mycompany.app.dto.TransactionCreationDTO;
import com.mycompany.app.model.Group;
import com.mycompany.app.model.Usuario;
import com.mycompany.app.repository.GroupRepository;
import com.mycompany.app.repository.UsuarioRepository;
import org.springframework.test.context.junit4.SpringRunner;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
// @Testcontainers // Uncomment this when Docker network issues are resolved
@AutoConfigureMockMvc(addFilters = false)
@Tag("Layer1")
@ExtendWith(JUnitPerfInterceptor.class)
public class TransactionIntegrationTest {

    /** * DOCKER CONFIGURATION (Currently disabled due to network issues)
     * To reactivate, uncomment the @Testcontainers annotation above, 
     * the imports, and the block below.
     */
    /*
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("expensense_it")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void configureDatasource(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.datasource.driver-class-name", postgres::getDriverClassName);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
        registry.add("spring.jpa.database-platform", () -> "org.hibernate.dialect.PostgreSQLDialect");
    }
    */

    @JUnitPerfTestActiveConfig
        private final static JUnitPerfReportingConfig PERF_CONFIG = JUnitPerfReportingConfig.builder()
        .reportGenerator(new HtmlReportGenerator("target/reports/junitperf_report.html"))
        .build();

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private GroupRepository groupRepository;

    @Test
    @JUnitPerfTest(totalExecutions = 50, threads = 5)
    @JUnitPerfTestRequirement(meanLatency = 400) // Average response time should be under 400ms
    public void performanceTest_CreateTransaction_ShouldPass() {
        executeTransactionFlow();
    }

    @Test
    @JUnitPerfTest(totalExecutions = 50, threads = 5)
    @JUnitPerfTestRequirement(maxLatency = 100)
    public void performanceTest_CreateTransaction_ShouldFailDeliberately() {
        executeTransactionFlow();
    }

        @Test
        public void createGroupTransactionAndDebt_PersistsInTransaccionesAndDeudasTables() {
        executeTransactionFlow();
    }
    @Test
    @JUnitPerfTest(totalExecutions = 40, threads = 4)
    @JUnitPerfTestRequirement(meanLatency = 1000) // El tiempo medio de respuesta debe ser menor a 1000ms
    public void performanceTest_EditTransaction_ShouldPass() {
        executeEditTransactionFlow();
    }

    @Test
    @JUnitPerfTest(totalExecutions = 30, threads = 3)
    @JUnitPerfTestRequirement(meanLatency = 800)
    public void performanceTest_DeleteTransaction_ShouldPass() {
        executeDeleteTransactionFlow();
    }

    @Test
    @JUnitPerfTest(totalExecutions = 30, threads = 3)
    @JUnitPerfTestRequirement(meanLatency = 1000)
    public void performanceTest_PayDebt_ShouldPass() {
        executePayDebtFlow();
    }

    private void executeTransactionFlow() {
        // 1. Arrange: Setup initial users and group
        Usuario creditor = usuarioRepository.save(new Usuario("Creditor", "creditor@mail.com", "pwd", 100.0));
        Usuario debtor = usuarioRepository.save(new Usuario("Debtor", "debtor@mail.com", "pwd", 80.0));

        Group group = new Group("Trip to Bilbao", "Shared expenses");
        group.addMiembro(creditor);
        group.addMiembro(debtor);
        Group savedGroup = groupRepository.save(group);

        TransactionCreationDTO transactionRequest = new TransactionCreationDTO(
                "Dinner",
                30.0,
                "GASTO",
                null,
                savedGroup.getId(),
                creditor.getId(),
                "1");

        // 2. Act: Create a new transaction via REST
        ResponseEntity<String> createTxResponse = restTemplate.postForEntity(
                "/transaction/create",
                transactionRequest,
                String.class);

        // 3. Assert: Verify transaction was created successfully
        assertEquals(HttpStatus.OK, createTxResponse.getStatusCode());

        Integer createdTransactionId = jdbcTemplate.queryForObject(
                "SELECT id FROM transacciones WHERE grupo_id = ? AND concepto = ? ORDER BY id DESC LIMIT 1",
                Integer.class,
                savedGroup.getId(),
                "Dinner");
        assertNotNull(createdTransactionId);

        Integer txCountAfterCreate = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM transacciones WHERE id = ?",
                Integer.class,
                createdTransactionId);
        assertNotNull(txCountAfterCreate);
        assertEquals(1, txCountAfterCreate.intValue());

        // 4. Act: Create a debt associated with the previous transaction
        DeudaCreationDTO deudaRequest = new DeudaCreationDTO();
        deudaRequest.setToken("1");
        deudaRequest.setTransaccionId(createdTransactionId);
        deudaRequest.setDeudorId(debtor.getId());
        deudaRequest.setAcreedorId(creditor.getId());
        deudaRequest.setImporte(15.0);

        ResponseEntity<String> createDebtResponse = restTemplate.postForEntity(
                "/transaction/crear",
                deudaRequest,
                String.class);

        // 5. Assert: Verify debt was correctly persisted and linked
        assertEquals(HttpStatus.OK, createDebtResponse.getStatusCode());

        Integer debtCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM deudas WHERE transaccion_id = ?",
                Integer.class,
                createdTransactionId);
        assertNotNull(debtCount);
        assertEquals(1, debtCount.intValue());

        String deudaEstado = jdbcTemplate.queryForObject(
                "SELECT estado FROM deudas WHERE transaccion_id = ?",
                String.class,
                createdTransactionId);
        assertEquals("PENDIENTE", deudaEstado);
    }
    private void executeEditTransactionFlow() {
        String uniqueSuffix = java.util.UUID.randomUUID().toString().substring(0, 5);
        Usuario creator = usuarioRepository.save(new Usuario("UserEdit" + uniqueSuffix, "edit" + uniqueSuffix + "@mail.com", "pwd", 100.0));
        
        Group group = new Group("Group Edit " + uniqueSuffix, "Edit tests");
        group.addMiembro(creator);
        Group savedGroup = groupRepository.save(group);

        TransactionCreationDTO createReq = new TransactionCreationDTO(
                "Lunch", 20.0, "GASTO", null, savedGroup.getId(), creator.getId(), "1");
        
        restTemplate.postForEntity("/transaction/create", createReq, String.class);
        
        Integer transactionId = jdbcTemplate.queryForObject(
                "SELECT id FROM transacciones WHERE grupo_id = ? ORDER BY id DESC LIMIT 1",
                Integer.class, savedGroup.getId());

        // 2. Act: Editar la transacción
        com.mycompany.app.dto.TransactionEditionDTO editReq = new com.mycompany.app.dto.TransactionEditionDTO();
        editReq.setAccessToken("1");
        editReq.setConcepto("Lunch Updated");
        editReq.setImporteTotal(25.0);
        editReq.setTipoTransaccion("GASTO");
        editReq.setCreadorId(creator.getId());
        editReq.setGrupoId(savedGroup.getId());

        ResponseEntity<String> editResponse = restTemplate.postForEntity(
                "/transaction/edit/" + transactionId,
                editReq,
                String.class);

        // 3. Assert
        assertEquals(HttpStatus.OK, editResponse.getStatusCode());
        String updatedConcept = jdbcTemplate.queryForObject(
                "SELECT concepto FROM transacciones WHERE id = ?",
                String.class, transactionId);
        assertEquals("Lunch Updated", updatedConcept);
    }

    private void executeDeleteTransactionFlow() {
        String uniqueSuffix = java.util.UUID.randomUUID().toString().substring(0, 5);
        Usuario creator = usuarioRepository.save(new Usuario("UserDel" + uniqueSuffix, "del" + uniqueSuffix + "@mail.com", "pwd", 100.0));
        
        TransactionCreationDTO createReq = new TransactionCreationDTO(
                "Taxi", 15.0, "GASTO", null, null, creator.getId(), "1");
        
        restTemplate.postForEntity("/transaction/create", createReq, String.class);
        
        Integer transactionId = jdbcTemplate.queryForObject(
                "SELECT id FROM transacciones WHERE creador_id = ? ORDER BY id DESC LIMIT 1",
                Integer.class, creator.getId());

        com.mycompany.app.dto.TransactionDeletionDTO deleteReq = new com.mycompany.app.dto.TransactionDeletionDTO();
        deleteReq.setAccessToken("1");
        deleteReq.setTransactionId(transactionId);

        ResponseEntity<String> deleteResponse = restTemplate.postForEntity(
                "/transaction/delete",
                deleteReq,
                String.class);

        // 3. Assert
        assertEquals(HttpStatus.OK, deleteResponse.getStatusCode());
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM transacciones WHERE id = ?",
                Integer.class, transactionId);
        assertEquals(0, count.intValue());
    }

    private void executePayDebtFlow() {
        String uniqueSuffix = java.util.UUID.randomUUID().toString().substring(0, 5);
        Usuario creditor = usuarioRepository.save(new Usuario("CreditorPay" + uniqueSuffix, "cpay" + uniqueSuffix + "@mail.com", "pwd", 100.0));
        Usuario debtor = usuarioRepository.save(new Usuario("DebtorPay" + uniqueSuffix, "dpay" + uniqueSuffix + "@mail.com", "pwd", 50.0));

        TransactionCreationDTO txReq = new TransactionCreationDTO(
                "Shared Tickets", 50.0, "GASTO", null, null, creditor.getId(), "1");
        restTemplate.postForEntity("/transaction/create", txReq, String.class);
        
        Integer txId = jdbcTemplate.queryForObject(
                "SELECT id FROM transacciones WHERE creador_id = ? ORDER BY id DESC LIMIT 1",
                Integer.class, creditor.getId());

        DeudaCreationDTO debtReq = new DeudaCreationDTO();
        debtReq.setToken("1");
        debtReq.setTransaccionId(txId);
        debtReq.setDeudorId(debtor.getId());
        debtReq.setAcreedorId(creditor.getId());
        debtReq.setImporte(25.0);
        
        restTemplate.postForEntity("/transaction/crear", debtReq, String.class);
        
        Integer deudaId = jdbcTemplate.queryForObject(
                "SELECT id FROM deudas WHERE transaccion_id = ? ORDER BY id DESC LIMIT 1",
                Integer.class, txId);

        com.mycompany.app.dto.PayDebtDTO payReq = new com.mycompany.app.dto.PayDebtDTO();
        payReq.setToken("1"); 

        ResponseEntity<String> payResponse = restTemplate.postForEntity(
                "/transaction/pay/" + deudaId,
                payReq,
                String.class);

        assertEquals(HttpStatus.OK, payResponse.getStatusCode());
        String estado = jdbcTemplate.queryForObject(
                "SELECT estado FROM deudas WHERE id = ?",
                String.class, deudaId);
        assertEquals("PAGADO", estado); 
    }
}