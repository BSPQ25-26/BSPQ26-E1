package com.mycompany.app.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.mycompany.app.dto.DebtEditionDTO;
import com.mycompany.app.dto.DeudaCreationDTO;
import com.mycompany.app.dto.TransactionCreationDTO;
import com.mycompany.app.dto.TransactionDeletionDTO;
import com.mycompany.app.dto.TransactionEditionDTO;
import com.mycompany.app.dto.TranscactionDebtEditionDTO;
import com.mycompany.app.model.Category;
import com.mycompany.app.model.Deuda;
import com.mycompany.app.model.EstadoDeuda;
import com.mycompany.app.model.Transaction;
import com.mycompany.app.model.Usuario;
import com.mycompany.app.repository.CategoryRepository;
import com.mycompany.app.repository.DeudaRepository;
import com.mycompany.app.repository.GroupRepository;
import com.mycompany.app.repository.TransactionRepository;
import com.mycompany.app.repository.UsuarioRepository;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private GroupRepository groupRepository;

    @Mock
    private DeudaRepository deudaRepository;

    @InjectMocks
    private TransactionService transactionService;

    @Test
    void createTransaction_WithIngreso_UpdatesBalanceAndReturnsTrue() {
        Usuario creator = new Usuario("Ana", "ana@mail.com", "pwd", 50.0);
        creator.setId(1);

        TransactionCreationDTO dto = new TransactionCreationDTO();
        dto.setCreadorId(1);
        dto.setTipoTransaccion("INGRESO");
        dto.setImporteTotal(25.0);
        dto.setConcepto("Salary");

        when(usuarioRepository.findById(1)).thenReturn(Optional.of(creator));

        boolean result = transactionService.createTransaction(dto);

        assertTrue(result);
        assertEquals(75.0, creator.getBalance());
        verify(transactionRepository, times(1)).saveAndFlush(any(Transaction.class));
        verify(usuarioRepository, times(1)).save(creator);
    }

    @Test
    void createTransaction_WhenCreatorDoesNotExist_ReturnsFalse() {
        TransactionCreationDTO dto = new TransactionCreationDTO();
        dto.setCreadorId(99);

        when(usuarioRepository.findById(99)).thenReturn(Optional.empty());

        boolean result = transactionService.createTransaction(dto);

        assertFalse(result);
    }

    @Test
    void deleteTransaction_WhenTransactionExists_ReturnsTrue() {
        TransactionDeletionDTO dto = new TransactionDeletionDTO("token", 10);
        when(transactionRepository.existsById(10)).thenReturn(true);

        boolean result = transactionService.deleteTransaction(dto);

        assertTrue(result);
        verify(transactionRepository, times(1)).deleteById(10);
    }

    @Test
    void editTransaction_WhenNotFound_ReturnsFalse() {
        TransactionEditionDTO dto = new TransactionEditionDTO("token", "concept", 12.0, "GASTO", null, null, null);
        when(transactionRepository.findById(10)).thenReturn(Optional.empty());

        boolean result = transactionService.editTransaction(dto, 10);

        assertFalse(result);
    }

    @Test
    void createDeuda_WithValidData_ReturnsTrue() {
        Transaction original = new Transaction();
        Usuario deudor = new Usuario("Debtor", "d@mail.com", "pwd", 20.0);
        Usuario acreedor = new Usuario("Creditor", "c@mail.com", "pwd", 20.0);

        DeudaCreationDTO dto = new DeudaCreationDTO();
        dto.setTransaccionId(1);
        dto.setDeudorId(2);
        dto.setAcreedorId(3);
        dto.setImporte(8.5);

        when(transactionRepository.findById(1)).thenReturn(Optional.of(original));
        when(usuarioRepository.findById(2)).thenReturn(Optional.of(deudor));
        when(usuarioRepository.findById(3)).thenReturn(Optional.of(acreedor));

        boolean result = transactionService.createDeuda(dto);

        assertTrue(result);
    }

    @Test
    void pagarDeuda_WhenAlreadyPaid_ReturnsFalse() {
        Deuda deuda = new Deuda();
        deuda.setEstado(EstadoDeuda.PAGADO);

        when(deudaRepository.findById(1)).thenReturn(Optional.of(deuda));

        boolean result = transactionService.pagarDeuda(1);

        assertFalse(result);
    }

    @Test
    void pagarDeuda_WhenPending_CreatesPaymentsAndMarksAsPaid() {
        Usuario deudor = new Usuario("Debtor", "d@mail.com", "pwd", 100.0);
        deudor.setId(1);
        Usuario acreedor = new Usuario("Creditor", "c@mail.com", "pwd", 10.0);
        acreedor.setId(2);

        Transaction txOriginal = new Transaction();
        txOriginal.setConcepto("Dinner");

        Deuda deuda = new Deuda();
        deuda.setId(3);
        deuda.setEstado(EstadoDeuda.PENDIENTE);
        deuda.setImporte(15.0);
        deuda.setDeudor(deudor);
        deuda.setAcreedor(acreedor);
        deuda.setTransaccionOriginal(txOriginal);

        when(deudaRepository.findById(3)).thenReturn(Optional.of(deuda));
        when(usuarioRepository.findById(1)).thenReturn(Optional.of(deudor));
        when(usuarioRepository.findById(2)).thenReturn(Optional.of(acreedor));

        boolean result = transactionService.pagarDeuda(3);

        assertTrue(result);
        assertEquals(EstadoDeuda.PAGADO, deuda.getEstado());
        verify(transactionRepository, times(2)).saveAndFlush(any(Transaction.class));
        verify(deudaRepository, times(1)).save(deuda);
    }

    @Test
    void editTransactionWithDeudas_WhenAnyDebtIsPaid_ReturnsFalse() {
        DebtEditionDTO debtEdition = new DebtEditionDTO();
        debtEdition.setId(5);

        TranscactionDebtEditionDTO request = new TranscactionDebtEditionDTO();
        request.setConcepto("Updated");
        request.setImporteTotal(100.0);
        request.setTipoTransaccion("GASTO");
        request.setDeudas(List.of(debtEdition));

        Deuda paidDebt = new Deuda();
        paidDebt.setEstado(EstadoDeuda.PAGADO);

        when(deudaRepository.findById(5)).thenReturn(Optional.of(paidDebt));

        boolean result = transactionService.editTransactionWithDeudas(request, 11);

        assertFalse(result);
    }

    @Test
    void editDeuda_WhenAcreedorDoesNotExist_ReturnsFalse() {
        Deuda deuda = new Deuda();
        deuda.setEstado(EstadoDeuda.PENDIENTE);

        DebtEditionDTO request = new DebtEditionDTO();
        request.setAcreedorId(8);

        when(deudaRepository.findById(1)).thenReturn(Optional.of(deuda));
        when(usuarioRepository.findById(8)).thenReturn(Optional.empty());

        boolean result = transactionService.editDeuda(request, 1);

        assertFalse(result);
    }

    @Test
    void getTransactionsByUserId_DelegatesRepository() {
        List<Transaction> expected = List.of(new Transaction());
        when(transactionRepository.findByCreadorId(5)).thenReturn(expected);

        List<Transaction> result = transactionService.getTransactionsByUserId(5);

        assertEquals(1, result.size());
    }

    @Test
    void editTransaction_WithCategoryAndGroup_AssignsThem() {
        Transaction tx = new Transaction();
        Category category = new Category();

        TransactionEditionDTO dto = new TransactionEditionDTO("token", "Edited", 9.0, "GASTO", 1, 2, null);

        when(transactionRepository.findById(10)).thenReturn(Optional.of(tx));
        when(categoryRepository.findById(1)).thenReturn(Optional.of(category));
        when(groupRepository.findById(2)).thenReturn(Optional.empty());

        boolean result = transactionService.editTransaction(dto, 10);

        assertTrue(result);
        assertEquals("Edited", tx.getConcepto());
        assertEquals(category, tx.getCategoria());
    }
}
