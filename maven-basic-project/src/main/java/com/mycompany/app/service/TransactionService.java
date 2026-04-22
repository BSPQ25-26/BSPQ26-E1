package com.mycompany.app.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mycompany.app.dto.DeudaCreationDTO;
import com.mycompany.app.dto.TransactionCreationDTO;
import com.mycompany.app.dto.TransactionDeletionDTO;
import com.mycompany.app.dto.TransactionEditionDTO;
import com.mycompany.app.model.Category;
import com.mycompany.app.model.Deuda;
import com.mycompany.app.model.EstadoDeuda;
import com.mycompany.app.model.Group;
import com.mycompany.app.model.Transaction;
import com.mycompany.app.model.Usuario;
import com.mycompany.app.repository.CategoryRepository;
import com.mycompany.app.repository.DeudaRepository;
import com.mycompany.app.repository.GroupRepository;
import com.mycompany.app.repository.TransactionRepository;
import com.mycompany.app.repository.UsuarioRepository;

@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final CategoryRepository categoryRepository;
    private final UsuarioRepository usuarioRepository;
    private final GroupRepository groupRepository;
    private final DeudaRepository deudaRepository;

    public TransactionService(TransactionRepository transactionRepository,
                              CategoryRepository categoryRepository,
                              UsuarioRepository usuarioRepository,
                              GroupRepository groupRepository,
                              DeudaRepository deudaRepository) {
        this.transactionRepository = transactionRepository;
        this.categoryRepository = categoryRepository;
        this.usuarioRepository = usuarioRepository;
        this.groupRepository = groupRepository;
        this.deudaRepository = deudaRepository;
    }

    @Transactional
    public boolean createTransaction(TransactionCreationDTO dto) {
        try {
            Usuario creador = usuarioRepository.findById(dto.getCreadorId()).orElse(null);
            if (creador == null) return false;

            Category categoria = dto.getCategoriaId() != null
                ? categoryRepository.findById(dto.getCategoriaId()).orElse(null)
                : null;

            Group grupo = dto.getGrupoId() != null
                ? groupRepository.findById(dto.getGrupoId()).orElse(null)
                : null;

            Transaction transaction = new Transaction(
                dto.getConcepto(),
                dto.getImporteTotal(),
                dto.getTipoTransaccion(),
                categoria,
                grupo,
                creador
            );

            transactionRepository.saveAndFlush(transaction);

            applyEffect(dto.getTipoTransaccion(), dto.getImporteTotal(), creador);

            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private void applyEffect(String tipoTransaccion, double importe, Usuario creador) {
        switch (tipoTransaccion) {
            case "INGRESO"      -> creador.addBalance(importe);
            case "GASTO"        -> creador.substractBalance(importe);
            case "LIQUIDACION"  -> creador.substractBalance(importe);
        }
        usuarioRepository.save(creador);
    }


    public boolean deleteTransaction(TransactionDeletionDTO request) {
        try {
            if (transactionRepository.existsById(request.getTransactionId())) {
                transactionRepository.deleteById(request.getTransactionId());
                return true;
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean editTransaction(TransactionEditionDTO request, Integer transactionId) {
        try {
            Transaction transaction = transactionRepository.findById(transactionId).orElse(null);
            if (transaction == null) return false;

            transaction.setConcepto(request.getConcepto());
            transaction.setImporteTotal(request.getImporteTotal());
            transaction.setTipoTransaccion(request.getTipoTransaccion());

            transaction.setGrupo(request.getGrupoId() != null
                ? groupRepository.findById(request.getGrupoId()).orElse(null)
                : null);

            transaction.setCategoria(request.getCategoriaId() != null
                ? categoryRepository.findById(request.getCategoriaId()).orElse(null)
                : null);

            if (request.getCreadorId() != null) {
                usuarioRepository.findById(request.getCreadorId())
                    .ifPresent(transaction::setCreador);
            }

            transactionRepository.saveAndFlush(transaction);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Transactional
    public boolean createDeuda(DeudaCreationDTO dto) {
        try {
            Transaction transaccionOriginal = transactionRepository.findById(dto.getTransaccionId()).orElse(null);
            if (transaccionOriginal == null) return false;

            Usuario deudor = usuarioRepository.findById(dto.getDeudorId()).orElse(null);
            if (deudor == null) return false;

            Usuario acreedor = usuarioRepository.findById(dto.getAcreedorId()).orElse(null);
            if (acreedor == null) return false;

            Deuda deuda = new Deuda();
            deuda.setTransaccionOriginal(transaccionOriginal);
            deuda.setDeudor(deudor);
            deuda.setAcreedor(acreedor);
            deuda.setImporte(dto.getImporte());
            deuda.setEstado(EstadoDeuda.PENDIENTE);

            deudaRepository.save(deuda);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

   @Transactional
    public boolean pagarDeuda(Integer deudaId) {
        try {
            Deuda deuda = deudaRepository.findById(deudaId).orElse(null);
            if (deuda == null || deuda.getEstado() == EstadoDeuda.PAGADO) return false;

            String concepto = "Pago de deuda: " + deuda.getTransaccionOriginal().getConcepto();
            Double importe = deuda.getImporte();

            TransactionCreationDTO pagoDeudor = new TransactionCreationDTO();
            pagoDeudor.setConcepto(concepto);
            pagoDeudor.setImporteTotal(importe);
            pagoDeudor.setTipoTransaccion("LIQUIDACION");
            pagoDeudor.setCreadorId(deuda.getDeudor().getId());

            TransactionCreationDTO ingresoAcreedor = new TransactionCreationDTO();
            ingresoAcreedor.setConcepto(concepto);
            ingresoAcreedor.setImporteTotal(importe);
            ingresoAcreedor.setTipoTransaccion("INGRESO");
            ingresoAcreedor.setCreadorId(deuda.getAcreedor().getId());

            createTransaction(pagoDeudor);
            createTransaction(ingresoAcreedor);

            deuda.setEstado(EstadoDeuda.PAGADO);
            deudaRepository.save(deuda);

            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public List<Transaction> getTransactionsByUserId(Integer userId) {
        return transactionRepository.findByCreadorId(userId);
    }

    public List<Transaction> getTransactionsByGroupId(Integer groupId) {
        return transactionRepository.findByGrupoId(groupId);
    }

    public List<Deuda> getDeudaByUsuarioId(Integer usuarioId) {
    return deudaRepository.findByDeudorId(usuarioId);
    }

    public List<Deuda> getDeudaByTransaccionId(Integer transaccionId) {
        return deudaRepository.findByTransaccionOriginalId(transaccionId);
    }
}