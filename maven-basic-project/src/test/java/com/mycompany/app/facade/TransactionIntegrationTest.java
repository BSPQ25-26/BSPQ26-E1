package com.mycompany.app.facade;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
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

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
// @Testcontainers // Uncomment this when Docker network issues are resolved
@AutoConfigureMockMvc(addFilters = false)
@Tag("Layer1")
class TransactionIntegrationTest {

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

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private GroupRepository groupRepository;

    @BeforeEach
    void cleanDatabase() {
        // Disable referential integrity to allow cleaning tables with foreign keys
        jdbcTemplate.execute("SET REFERENTIAL_INTEGRITY FALSE");
        
        jdbcTemplate.execute("TRUNCATE TABLE deudas");
        jdbcTemplate.execute("TRUNCATE TABLE transacciones");
        jdbcTemplate.execute("TRUNCATE TABLE usuario_grupo");
        jdbcTemplate.execute("TRUNCATE TABLE grupo");
        jdbcTemplate.execute("TRUNCATE TABLE categories");
        jdbcTemplate.execute("TRUNCATE TABLE usuario");
        
        // Re-enable referential integrity
        jdbcTemplate.execute("SET REFERENTIAL_INTEGRITY TRUE");
    }

    @Test
    void createGroupTransactionAndDebt_PersistsInTransaccionesAndDeudasTables() {
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

        Integer txCountAfterCreate = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM transacciones WHERE grupo_id = ?",
                Integer.class,
                savedGroup.getId());
        assertNotNull(txCountAfterCreate);
        assertEquals(1, txCountAfterCreate.intValue());

        Integer createdTransactionId = jdbcTemplate.queryForObject(
                "SELECT id FROM transacciones ORDER BY id DESC LIMIT 1",
                Integer.class);
        assertNotNull(createdTransactionId);

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
}