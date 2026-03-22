package com.mycompany.app.service;

import org.springframework.stereotype.Service;

import com.mycompany.app.dto.TransactionCreationDTO;
import com.mycompany.app.dto.TransactionDeletionDTO;
import com.mycompany.app.dto.TransactionEditionDTO;
import com.mycompany.app.model.Group;
import com.mycompany.app.model.Transaction;
import com.mycompany.app.repository.GroupRepository;
import com.mycompany.app.repository.TransactionRepository;

import java.util.Optional;

@Service
public class TransactionService {
    private final TransactionRepository transactionRepository;
    private final GroupRepository groupRepository;

    public TransactionService(TransactionRepository transactionRepository, GroupRepository groupRepository) {
        this.transactionRepository = transactionRepository;
        this.groupRepository = groupRepository;
    }

    public boolean createTransaction(TransactionCreationDTO transactionCreationDTO){
        try {
            // Convert groupId to Group entity if provided
            Group group = null;
            if (transactionCreationDTO.getGrupoId() != null) {
                Optional<Group> groupOpt = groupRepository.findById(transactionCreationDTO.getGrupoId());
                group = groupOpt.orElse(null);
            }

            Transaction transaction = new Transaction(
                transactionCreationDTO.getConcepto(),
                transactionCreationDTO.getImporteTotal(),
                transactionCreationDTO.getTipoTransaccion(),
                transactionCreationDTO.getCategoriaId(),
                group,
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

                // Convert groupId to Group entity if provided
                if (request.getGrupoId() != null) {
                    Optional<Group> groupOpt = groupRepository.findById(request.getGrupoId());
                    transaction.setGroup(groupOpt.orElse(null));
                } else {
                    transaction.setGroup(null);
                }

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
