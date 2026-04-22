package com.mycompany.app.view;

import com.mycompany.app.dto.TransactionCreationDTO;
import com.mycompany.app.model.Group;
import com.mycompany.app.model.Transaction;
import com.mycompany.app.model.Usuario;
import com.mycompany.app.repository.CategoryRepository;
import com.mycompany.app.repository.GroupRepository;
import com.mycompany.app.repository.TransactionRepository;
import com.mycompany.app.repository.UsuarioRepository;
import com.mycompany.app.service.AuthService;
import com.mycompany.app.service.CategoryService;
import com.mycompany.app.service.TransactionService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/web/transaction")
public class TransactionViewController {

    private final AuthService authService;
    private final TransactionService transactionService;
    private final TransactionRepository transactionRepository;
    private final CategoryRepository categoryRepository;
    private final CategoryService categoryService;
    private final GroupRepository groupRepository;
    private final UsuarioRepository usuarioRepository;

    public TransactionViewController(AuthService authService,
                                      TransactionService transactionService,
                                      TransactionRepository transactionRepository,
                                      CategoryRepository categoryRepository,
                                      CategoryService categoryService,
                                      GroupRepository groupRepository,
                                      UsuarioRepository usuarioRepository) {
        this.authService = authService;
        this.transactionService = transactionService;
        this.transactionRepository = transactionRepository;
        this.categoryRepository = categoryRepository;
        this.categoryService = categoryService;
        this.groupRepository = groupRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @GetMapping
    public String transactions(
            @CookieValue(value = "token", required = false) String token,
            @RequestParam(required = false) Boolean showForm,
            @RequestParam(required = false) Integer editId,
            Model model
    ) {
        if (token == null || !authService.isValidToken(token)) return "redirect:/web/auth/login";

        String email = authService.getEmailFromToken(token);
        Usuario user = usuarioRepository.findByEmail(email);
        if (user == null) return "redirect:/web/auth/login";

        loadModelAttributes(user, email, model);

        if (editId != null) {
            transactionRepository.findById(editId).ifPresent(t -> model.addAttribute("editTransaction", t));
            model.addAttribute("showForm", true);
        } else {
            model.addAttribute("showForm", Boolean.TRUE.equals(showForm));
        }

        return "transactions";
    }

    @PostMapping("/create")
    public String createTransaction(
            @CookieValue(value = "token", required = false) String token,
            @RequestParam(required = false) String concepto,
            @RequestParam(required = false) Double importeTotal,
            @RequestParam(required = false) String tipoTransaccion,
            @RequestParam(required = false) Integer categoriaId,
            @RequestParam(required = false) Integer grupoId,
            Model model
    ) {
        if (token == null || !authService.isValidToken(token)) return "redirect:/web/auth/login";

        String email = authService.getEmailFromToken(token);
        Usuario user = usuarioRepository.findByEmail(email);
        if (user == null) return "redirect:/web/auth/login";

        if (concepto == null || concepto.isBlank() || importeTotal == null || importeTotal <= 0
                || tipoTransaccion == null || tipoTransaccion.isBlank()) {
            loadModelAttributes(user, email, model);
            model.addAttribute("error", "Please fill in all required fields.");
            model.addAttribute("showForm", true);
            return "transactions";
        }

        transactionService.createTransaction(new TransactionCreationDTO(
                concepto, importeTotal, tipoTransaccion, categoriaId, grupoId, user.getId(), token));
        return "redirect:/web/transaction";
    }

    @PostMapping("/edit/{id}")
    public String editTransaction(
            @CookieValue(value = "token", required = false) String token,
            @PathVariable Integer id,
            @RequestParam String concepto,
            @RequestParam Double importeTotal,
            @RequestParam String tipoTransaccion,
            @RequestParam(required = false) Integer categoriaId,
            @RequestParam(required = false) Integer grupoId
    ) {
        if (token == null || !authService.isValidToken(token)) return "redirect:/web/auth/login";

        transactionRepository.findById(id).ifPresent(t -> {
            t.setConcepto(concepto);
            t.setImporteTotal(importeTotal);
            t.setTipoTransaccion(tipoTransaccion);
            t.setCategoria(categoriaId != null ? categoryRepository.findById(categoriaId).orElse(null) : null); // NOSONAR
            t.setGrupo(grupoId != null ? groupRepository.findById(grupoId).orElse(null) : null); // NOSONAR
            transactionRepository.save(t);
        });

        return "redirect:/web/transaction";
    }


    //HTML forms do not support DELETE so this function has to be a POST
    @PostMapping("/delete")
    public String deleteTransaction(
            @CookieValue(value = "token", required = false) String token,
            @RequestParam Integer transactionId
    ) {
        if (token == null || !authService.isValidToken(token)) return "redirect:/web/auth/login";
        if (transactionId != null) transactionRepository.deleteById(transactionId);
        return "redirect:/web/transaction";
    }

    private void loadModelAttributes(Usuario user, String email, Model model) {
        List<Group> myGroups = groupRepository.findByMiembrosEmail(email);

        List<Transaction> allTransactions = myGroups.isEmpty()
                ? transactionRepository.findByCreador(user)
                : transactionRepository.findByCreadorOrGrupoIn(user, myGroups);

        Map<Integer, Double> txShares = new HashMap<>();
        for (Transaction t : allTransactions) {
            if (t.getGrupo() != null) {
                Group withMembers = groupRepository.findByIdWithMiembros(t.getGrupo().getId()).orElse(null);
                int numMembers = (withMembers != null && withMembers.getMiembros() != null
                        && !withMembers.getMiembros().isEmpty())
                        ? withMembers.getMiembros().size() : 1;
                txShares.put(t.getId(), Math.round(t.getImporteTotal() / numMembers * 100.0) / 100.0);
            } else {
                txShares.put(t.getId(), t.getImporteTotal());
            }
        }

        model.addAttribute("transactions", allTransactions);
        model.addAttribute("txShares", txShares);
        model.addAttribute("categories", categoryService.getCategoriesByUser(user.getId()));
        model.addAttribute("groups", myGroups);
    }
}