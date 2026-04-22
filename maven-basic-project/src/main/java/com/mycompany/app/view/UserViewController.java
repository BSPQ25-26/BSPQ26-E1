package com.mycompany.app.view;

import com.mycompany.app.dto.UserCreationDTO;
import com.mycompany.app.model.Group;
import com.mycompany.app.model.Transaction;
import com.mycompany.app.model.Usuario;
import com.mycompany.app.repository.GroupRepository;
import com.mycompany.app.repository.TransactionRepository;
import com.mycompany.app.repository.UsuarioRepository;
import com.mycompany.app.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Controller
@RequestMapping("/web/user")
public class UserViewController {

    private final AuthService authService;
    private final UsuarioRepository usuarioRepository;
    private final TransactionRepository transactionRepository;
    private final GroupRepository groupRepository;
    private final RestTemplate restTemplate;

    public UserViewController(AuthService authService,
                               UsuarioRepository usuarioRepository,
                               TransactionRepository transactionRepository,
                               GroupRepository groupRepository,
                               RestTemplate restTemplate) {
        this.authService = authService;
        this.usuarioRepository = usuarioRepository;
        this.transactionRepository = transactionRepository;
        this.groupRepository = groupRepository;
        this.restTemplate = restTemplate;
    }

    @GetMapping("/create")
    public String createUserForm(Model model) {
        model.addAttribute("userCreationDTO", new UserCreationDTO());
        return "user-create";
    }

    @PostMapping("/create")
    public String createUser(
            UserCreationDTO userCreationDTO,
            HttpServletRequest request,
            Model model
    ) {
        model.addAttribute("userCreationDTO", userCreationDTO);
        String baseUrl = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort();
        try {
            ResponseEntity<Void> response = restTemplate.postForEntity(baseUrl + "/user/", userCreationDTO, Void.class);
            if (response.getStatusCode() == HttpStatus.CREATED) {
                model.addAttribute("message", "Account created successfully. You can now log in.");
            } else {
                model.addAttribute("error", "An account with that email already exists.");
            }
        } catch (Exception e) {
            model.addAttribute("error", "An internal error occurred. Please try again.");
        }
        return "user-create";
    }

    @GetMapping("/profile")
    public String profile(@CookieValue(value = "token", required = false) String token, Model model) {
        if (token == null || !authService.isValidToken(token)) {
            return "redirect:/web/auth/login";
        }
        return populateProfileModel(token, model);
    }

    private String populateProfileModel(String token, Model model) {
        String email = authService.getEmailFromToken(token);
        Usuario user = usuarioRepository.findByEmail(email);
        if (user == null) return "redirect:/web/auth/login";

        List<Transaction> myTransactions = transactionRepository.findByCreador(user);
        double balance = 0.0;
        for (Transaction t : myTransactions) {
            if ("INCOME".equals(t.getTipoTransaccion())) {
                balance += t.getImporteTotal();
            } else if ("EXPENSE".equals(t.getTipoTransaccion()) && t.getGrupo() == null) {
                balance -= t.getImporteTotal();
            }
        }

        double owed = 0.0;
        double owes = 0.0;
        List<Group> myGroups = groupRepository.findByMiembrosEmail(email);
        for (Group group : myGroups) {
            Group withMembers = groupRepository.findByIdWithMiembros(group.getId()).orElse(null);
            int numMembers = (withMembers != null && withMembers.getMiembros() != null
                    && !withMembers.getMiembros().isEmpty())
                    ? withMembers.getMiembros().size() : 1;

            List<Transaction> groupTx = transactionRepository.findByGrupo(group);
            for (Transaction t : groupTx) {
                if (!"EXPENSE".equals(t.getTipoTransaccion())) continue;
                double share = t.getImporteTotal() / numMembers;
                balance -= share;
                if (numMembers > 1) {
                    if (t.getCreador() != null && t.getCreador().getId().equals(user.getId())) {
                        owed += share * (numMembers - 1);
                    } else {
                        owes += share;
                    }
                }
            }
        }

        model.addAttribute("username", user.getNombre());
        model.addAttribute("email", user.getEmail());
        model.addAttribute("balance", Math.round(balance * 100.0) / 100.0);
        model.addAttribute("owed", Math.round(owed * 100.0) / 100.0);
        model.addAttribute("owes", Math.round(owes * 100.0) / 100.0);

        return "user-info";
    }
}