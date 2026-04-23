package com.mycompany.app.view;

import com.mycompany.app.dto.DeudaCreationDTO;
import com.mycompany.app.dto.TransactionCreationDTO;
import com.mycompany.app.model.Deuda;
import com.mycompany.app.model.Group;
import com.mycompany.app.model.Transaction;
import com.mycompany.app.model.Usuario;
import com.mycompany.app.repository.CategoryRepository;
import com.mycompany.app.repository.DeudaRepository;
import com.mycompany.app.repository.GroupRepository;
import com.mycompany.app.repository.TransactionRepository;
import com.mycompany.app.repository.UsuarioRepository;
import com.mycompany.app.service.AuthService;
import com.mycompany.app.service.CategoryService;
import com.mycompany.app.service.TransactionService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
    private final DeudaRepository deudaRepository;

    public TransactionViewController(AuthService authService,
                                      TransactionService transactionService,
                                      TransactionRepository transactionRepository,
                                      CategoryRepository categoryRepository,
                                      CategoryService categoryService,
                                      GroupRepository groupRepository,
                                      UsuarioRepository usuarioRepository,
                                      DeudaRepository deudaRepository) {
        this.authService = authService;
        this.transactionService = transactionService;
        this.transactionRepository = transactionRepository;
        this.categoryRepository = categoryRepository;
        this.categoryService = categoryService;
        this.groupRepository = groupRepository;
        this.usuarioRepository = usuarioRepository;
        this.deudaRepository = deudaRepository;
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
            @RequestParam(required = false) Integer pagadorId,
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

        Integer creadorId = (pagadorId != null) ? pagadorId : user.getId();
        transactionService.createTransaction(new TransactionCreationDTO(
                concepto, importeTotal, tipoTransaccion, categoriaId, grupoId, creadorId, token));

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

    @PostMapping("/delete")
    public String deleteTransaction(
            @CookieValue(value = "token", required = false) String token,
            @RequestParam Integer transactionId
    ) {
        if (token == null || !authService.isValidToken(token)) return "redirect:/web/auth/login";
        if (transactionId != null) transactionRepository.deleteById(transactionId);
        return "redirect:/web/transaction";
    }

    @PostMapping("/pay-debt")
    public String payDebt(
            @CookieValue(value = "token", required = false) String token,
            @RequestParam Integer deudaId
    ) {
        if (token == null || !authService.isValidToken(token)) return "redirect:/web/auth/login";
        transactionService.pagarDeuda(deudaId);
        return "redirect:/web/user/profile";
    }

    @PostMapping("/crear-deuda")
    @ResponseBody
    public ResponseEntity<String> crearDeudaWeb(
            @CookieValue(value = "token", required = false) String token,
            @RequestParam Integer transaccionId,
            @RequestParam Integer deudorId,
            @RequestParam Integer acreedorId,
            @RequestParam Double importe
    ) {
        if (token == null || !authService.isValidToken(token)) {
            return ResponseEntity.status(401).body("Unauthorized");
        }
        DeudaCreationDTO dto = new DeudaCreationDTO();
        dto.setTransaccionId(transaccionId);
        dto.setDeudorId(deudorId);
        dto.setAcreedorId(acreedorId);
        dto.setImporte(importe);
        transactionService.createDeuda(dto);
        return ResponseEntity.ok("OK");
    }

    @PostMapping("/repartir/{id}")
    public String repartirGasto(
            @CookieValue(value = "token", required = false) String token,
            @PathVariable Integer id
    ) {
        if (token == null || !authService.isValidToken(token)) return "redirect:/web/auth/login";

        Transaction tx = transactionRepository.findById(id).orElse(null);
        if (tx == null || tx.getGrupo() == null || !"GASTO".equals(tx.getTipoTransaccion())) {
            return "redirect:/web/transaction";
        }

        if (!deudaRepository.findByTransaccionOriginalId(id).isEmpty()) {
            return "redirect:/web/transaction";
        }

        Group withMembers = groupRepository.findByIdWithMiembros(tx.getGrupo().getId()).orElse(null);
        if (withMembers == null || withMembers.getMiembros() == null || withMembers.getMiembros().size() < 2) {
            return "redirect:/web/transaction";
        }

        int numMembers = withMembers.getMiembros().size();
        double share = Math.round(tx.getImporteTotal() / numMembers * 100.0) / 100.0;
        Integer pagadorId = tx.getCreador() != null
                ? tx.getCreador().getId()
                : usuarioRepository.findByEmail(authService.getEmailFromToken(token)).getId();

        for (Usuario member : withMembers.getMiembros()) {
            if (!member.getId().equals(pagadorId)) {
                DeudaCreationDTO dto = new DeudaCreationDTO();
                dto.setTransaccionId(id);
                dto.setDeudorId(member.getId());
                dto.setAcreedorId(pagadorId);
                dto.setImporte(share);
                transactionService.createDeuda(dto);
            }
        }

        return "redirect:/web/transaction";
    }

    private void loadModelAttributes(Usuario user, String email, Model model) {
        // Only user's own transactions, sorted newest first (findByCreadorId avoids loading the full entity)
        List<Transaction> allTransactions = new ArrayList<>(transactionRepository.findByCreadorId(user.getId()));
        allTransactions.sort(Comparator.comparing(Transaction::getFecha,
                Comparator.nullsLast(Comparator.reverseOrder())));

        // Determine which group-GASTO transactions are already split (have debts)
        Set<Integer> txWithDebts = new HashSet<>();
        Map<Integer, List<Deuda>> txDeudas = new HashMap<>();
        for (Transaction t : allTransactions) {
            if (t.getGrupo() != null && "GASTO".equals(t.getTipoTransaccion())
                    && (t.getCreador() == null || t.getCreador().getId().equals(user.getId()))) {
                List<Deuda> debts = deudaRepository.findByTransaccionOriginalId(t.getId());
                if (!debts.isEmpty()) {
                    txWithDebts.add(t.getId());
                    txDeudas.put(t.getId(), debts);
                }
            }
        }

        // Groups the user belongs to (for the create-form dropdown)
        List<Group> myGroups = groupRepository.findByMiembrosEmail(email);

        // Build member map for all user groups in a single pass (eliminates duplicate iteration)
        Map<Integer, Integer> groupMemberCounts = new HashMap<>();
        Map<String, Object> groupMembersData = new HashMap<>();
        for (Group g : myGroups) {
            Group withMembers = groupRepository.findByIdWithMiembros(g.getId()).orElse(null);
            if (withMembers == null || withMembers.getMiembros() == null) continue;
            groupMemberCounts.put(withMembers.getId(), withMembers.getMiembros().size());
            List<Map<String, Object>> members = new ArrayList<>();
            for (Usuario m : withMembers.getMiembros()) {
                Map<String, Object> md = new HashMap<>();
                md.put("id", m.getId());
                md.put("nombre", m.getNombre());
                members.add(md);
            }
            groupMembersData.put(String.valueOf(withMembers.getId()), members);
        }

        model.addAttribute("transactions", allTransactions);
        model.addAttribute("txWithDebts", txWithDebts);
        model.addAttribute("txDeudas", txDeudas);
        model.addAttribute("groupMemberCounts", groupMemberCounts);
        model.addAttribute("groupMembersData", groupMembersData);
        model.addAttribute("categories", categoryService.getCategoriesByUser(user.getId()));
        model.addAttribute("groups", myGroups);
        model.addAttribute("currentUserId", user.getId());
    }
}
