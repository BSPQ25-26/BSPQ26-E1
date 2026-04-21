package com.mycompany.app.view;

import com.mycompany.app.dto.TransactionCreationDTO;
import com.mycompany.app.model.Usuario;
import com.mycompany.app.repository.CategoryRepository;
import com.mycompany.app.repository.GroupRepository;
import com.mycompany.app.repository.TransactionRepository;
import com.mycompany.app.repository.UsuarioRepository;
import com.mycompany.app.service.AuthService;
import com.mycompany.app.service.CategoryService;
import com.mycompany.app.service.TransactionService;

import jakarta.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/web/transaction")
public class TransactionViewController {

    private final CategoryService categoryService;
    private final CategoryRepository categoryRepository;
    private final GroupRepository groupRepository;
    private final TransactionRepository transactionRepository;
    private final UsuarioRepository usuarioRepository;
    private final AuthService authService;
    private final TransactionService transactionService;

    public TransactionViewController(CategoryService categoryService,
                                      CategoryRepository categoryRepository,
                                      GroupRepository groupRepository,
                                      TransactionRepository transactionRepository,
                                      UsuarioRepository usuarioRepository,
                                      AuthService authService,
                                      TransactionService transactionService) {
        this.categoryService = categoryService;
        this.categoryRepository = categoryRepository;
        this.groupRepository = groupRepository;
        this.transactionRepository = transactionRepository;
        this.usuarioRepository = usuarioRepository;
        this.authService = authService;
        this.transactionService = transactionService;
    }

    @GetMapping
    public String transactions(@RequestParam(required = false) String token,
                               @RequestParam(required = false) Boolean showForm,
                               @RequestParam(required = false) Integer editId,
                               HttpSession session, Model model) {
        if (token != null) session.setAttribute("token", token);

        String storedToken = (String) session.getAttribute("token");
        if (storedToken == null || !authService.isValidToken(storedToken)) {
            return "redirect:/web/auth/login";
        }

        String email = authService.getEmailFromToken(storedToken);
        session.setAttribute("userEmail", email);

        Usuario user = usuarioRepository.findByEmail(email);
        if (user == null) return "redirect:/web/auth/login";

        loadModelAttributes(user, email, model);

        if (editId != null) {
            transactionRepository.findById(editId).ifPresent(t ->
                model.addAttribute("editTransaction", t));
            model.addAttribute("showForm", true);
        } else {
            model.addAttribute("showForm", Boolean.TRUE.equals(showForm));
        }

        return "transactions";
    }

    @PostMapping("/create")
    public String createTransaction(
            @RequestParam(required = false) String concepto,
            @RequestParam(required = false) Double importeTotal,
            @RequestParam(required = false) String tipoTransaccion,
            @RequestParam(required = false) Integer categoriaId,
            @RequestParam(required = false) Integer grupoId,
            HttpSession session,
            Model model) {

        String storedToken = (String) session.getAttribute("token");
        String email = (String) session.getAttribute("userEmail");
        if (storedToken == null || email == null) return "redirect:/web/auth/login";

        Usuario user = usuarioRepository.findByEmail(email);
        if (user == null) return "redirect:/web/auth/login";

        if (concepto == null || concepto.isBlank()
                || importeTotal == null || importeTotal <= 0
                || tipoTransaccion == null || tipoTransaccion.isBlank()) {
            loadModelAttributes(user, email, model);
            model.addAttribute("error", "Por favor, rellena todos los campos obligatorios.");
            model.addAttribute("showForm", true);
            return "transactions";
        }

        TransactionCreationDTO dto = new TransactionCreationDTO(
            concepto, importeTotal, tipoTransaccion, categoriaId, grupoId, user.getId(), storedToken
        );
        transactionService.createTransaction(dto);
        return "redirect:/web/transaction";
    }

    @PostMapping("/edit/{id}")
    public String editTransaction(
            @PathVariable Integer id,
            @RequestParam String concepto,
            @RequestParam Double importeTotal,
            @RequestParam String tipoTransaccion,
            @RequestParam(required = false) Integer categoriaId,
            @RequestParam(required = false) Integer grupoId,
            HttpSession session,
            RedirectAttributes ra) {

        if (session.getAttribute("token") == null) return "redirect:/web/auth/login";

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
    public String deleteTransaction(@RequestParam Integer transactionId,
                                    HttpSession session,
                                    RedirectAttributes ra) {
        if (session.getAttribute("token") == null) return "redirect:/web/auth/login";
        if (transactionId != null) transactionRepository.deleteById(transactionId);
        return "redirect:/web/transaction";
    }

    private void loadModelAttributes(Usuario user, String email, Model model) {
        model.addAttribute("listaCategorias", categoryService.getCategoriesByUser(user.getId()));
        model.addAttribute("listaGrupos", groupRepository.findByMiembrosEmail(email));
        model.addAttribute("listaTransacciones", transactionRepository.findByCreador(user));
    }
}
