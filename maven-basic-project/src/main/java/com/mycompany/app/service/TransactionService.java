package com.mycompany.app.service;

import java.util.Optional;

import org.springframework.stereotype.Service;

import com.mycompany.app.dto.TransactionCreationDTO;
import com.mycompany.app.dto.TransactionDeletionDTO;
import com.mycompany.app.dto.TransactionEditionDTO;
import com.mycompany.app.model.Category;
import com.mycompany.app.model.Transaction;
import com.mycompany.app.model.Usuario;
import com.mycompany.app.repository.CategoryRepository;
import com.mycompany.app.repository.TransactionRepository;
import com.mycompany.app.repository.UsuarioRepository;

@Service
public class TransactionService {
    private final TransactionRepository transactionRepository;
    private final CategoryRepository categoryRepository;
    private final UsuarioRepository usuarioRepository;

    public TransactionService(TransactionRepository transactionRepository, 
                              CategoryRepository categoryRepository, 
                              UsuarioRepository usuarioRepository) {
        this.transactionRepository = transactionRepository;
        this.categoryRepository = categoryRepository;
        this.usuarioRepository = usuarioRepository;
    }

    public boolean createTransaction(TransactionCreationDTO transactionCreationDTO){
        try {
            Optional<Usuario> creadorOpt = usuarioRepository.findById(transactionCreationDTO.getCreadorId());
            if (creadorOpt.isEmpty()) {
                return false;
            }

            Category categoria = null;
            if (transactionCreationDTO.getCategoriaId() != null) {
                categoria = categoryRepository.findById(transactionCreationDTO.getCategoriaId()).orElse(null);
            }

            Transaction transaction = new Transaction(
                transactionCreationDTO.getConcepto(),
                transactionCreationDTO.getImporteTotal(),
                transactionCreationDTO.getTipoTransaccion(),
                categoria,
                transactionCreationDTO.getGrupoId(),
                creadorOpt.get()
            );

            transactionRepository.saveAndFlush(transaction);
            return true;

        } catch (Exception e) {
            return false;
        }
    }

    public boolean deleteTransaction(TransactionDeletionDTO request){
        try {
            if(transactionRepository.existsById(request.getTransactionId())){
                transactionRepository.deleteById(request.getTransactionId());
                return true;
            } else {
                return false;
            }
        } catch(Exception e) {
            return false;
        }
    }

    public boolean editTransaction(TransactionEditionDTO request, Integer transactionId) {
        try {
            Transaction transaction = transactionRepository.findById(transactionId).orElse(null);

            if (transaction != null) {
                transaction.setConcepto(request.getConcepto());
                transaction.setImporteTotal(request.getImporteTotal());
                transaction.setTipoTransaccion(request.getTipoTransaccion());
                transaction.setGrupoId(request.getGrupoId());

                if (request.getCategoriaId() != null) {
                    Category categoria = categoryRepository.findById(request.getCategoriaId()).orElse(null);
                    transaction.setCategoria(categoria);
                } else {
                    transaction.setCategoria(null);
                }

                if (request.getCreadorId() != null) {
                    Usuario creador = usuarioRepository.findById(request.getCreadorId()).orElse(null);
                    if (creador != null) {
                        transaction.setCreador(creador);
                    }
                }

                transactionRepository.saveAndFlush(transaction);
                return true;
            } else {
                return false; 
            }
        } catch (Exception e) {
            return false;
        }
    }
}