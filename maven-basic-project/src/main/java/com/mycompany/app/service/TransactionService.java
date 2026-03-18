package com.mycompany.app.service;

import org.springframework.stereotype.Service;

import com.mycompany.app.dto.TransactionCreationDTO;
import com.mycompany.app.model.Transaction;
import com.mycompany.app.repository.TransactionRepository;

@Service
public class TransactionService {
    private final TransactionRepository transactionRepository;

    public TransactionService(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    public boolean createTransaction(TransactionCreationDTO transactionCreationDTO){
        try {
            Transaction transaction = new Transaction(
                transactionCreationDTO.getConcepto(),
                transactionCreationDTO.getImporteTotal(),
                transactionCreationDTO.getTipoTransaccion(),
                transactionCreationDTO.getCategoriaId(),
                transactionCreationDTO.getGrupoId(),
                transactionCreationDTO.getCreadorId()
                );

            transactionRepository.saveAndFlush(transaction);

            return true;

        } catch (Exception e) {
            return false;
        }
    }
}
