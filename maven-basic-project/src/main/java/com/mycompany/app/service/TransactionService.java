package com.mycompany.app.service;

import org.springframework.stereotype.Service;

import com.mycompany.app.dto.TransactionCreationDTO;
import com.mycompany.app.dto.TransactionDeletionDTO;
import com.mycompany.app.dto.TransactionEditionDTO;
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

    public boolean deleteTransaction(TransactionDeletionDTO request){
        try{
            if(transactionRepository.findById(request.getTransactionId()) != null){
                transactionRepository.deleteById(request.getTransactionId());
                return true;
            }else{
                return false;
            }
        }catch(Exception e){
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
                transaction.setCategoriaId(request.getCategoriaId());
                transaction.setGrupoId(request.getGrupoId());
                transaction.setCreadorId(request.getCreadorId());

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
