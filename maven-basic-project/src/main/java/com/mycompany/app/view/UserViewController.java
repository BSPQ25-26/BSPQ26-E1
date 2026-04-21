package com.mycompany.app.view;

import java.util.List;

import com.mycompany.app.dto.UserCreationDTO;
import com.mycompany.app.model.Group;
import com.mycompany.app.model.Transaction;
import com.mycompany.app.model.Usuario;
import com.mycompany.app.repository.GroupRepository;
import com.mycompany.app.repository.TransactionRepository;
import com.mycompany.app.repository.UsuarioRepository;
import com.mycompany.app.service.AuthService;
import com.mycompany.app.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/web/user")
public class UserViewController {

    private final UserService userService;
    private final AuthService authService;

    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private TransactionRepository transactionRepository;
    @Autowired private GroupRepository groupRepository;

    public UserViewController(UserService userService, AuthService authService) {
        this.userService = userService;
        this.authService = authService;
    }

    @GetMapping("/create")
    public String createUserForm() {
        return "user-create";
    }

    @PostMapping("/create")
    public String createUser(UserCreationDTO userCreationDTO, Model model) {
        try {
            if (!userService.createUser(userCreationDTO)) {
                model.addAttribute("error", "User not found");
                return "user-create";
            }
            model.addAttribute("message", "User found correctly");
            return "user-create";
        } catch (Exception e) {
            model.addAttribute("error", "Internal server error");
            return "user-create";
        }
    }

    @GetMapping("/profile")
    public String profile(HttpSession session, Model model) {
        String token = (String) session.getAttribute("token");
        if (token == null || !authService.isValidToken(token)) {
            return "redirect:/web/auth/login";
        }
        return populateProfileModel(token, session, model);
    }

    @GetMapping("/init")
    public String init(@RequestParam String token, HttpSession session, Model model) {
        session.setAttribute("token", token);
        if (!authService.isValidToken(token)) return "redirect:/web/auth/login";
        return populateProfileModel(token, session, model);
    }

    private String populateProfileModel(String token, HttpSession session, Model model) {
        String email = authService.getEmailFromToken(token);
        Usuario user = usuarioRepository.findByEmail(email);
        if (user == null) return "redirect:/web/auth/login";

        session.setAttribute("userEmail", email);

        List<Transaction> myTransactions = transactionRepository.findByCreador(user);
        double balance = 0.0;
        for (Transaction t : myTransactions) {
            if ("INGRESO".equals(t.getTipoTransaccion())) {
                balance += t.getImporteTotal();
            } else if ("GASTO".equals(t.getTipoTransaccion()) && t.getGrupo() == null) {
                balance -= t.getImporteTotal();
            }
        }

        double queMeDeben = 0.0;
        double queDebo = 0.0;
        List<Group> myGroups = groupRepository.findByMiembrosEmail(email);
        for (Group grupo : myGroups) {
            Group withMembers = groupRepository.findByIdWithMiembros(grupo.getId()).orElse(null);
            int numMembers = (withMembers != null && withMembers.getMiembros() != null
                    && !withMembers.getMiembros().isEmpty())
                    ? withMembers.getMiembros().size() : 1;

            List<Transaction> groupTx = transactionRepository.findByGrupo(grupo);
            for (Transaction t : groupTx) {
                if (!"GASTO".equals(t.getTipoTransaccion())) continue;
                double share = t.getImporteTotal() / numMembers;
                balance -= share;
                if (numMembers > 1) {
                    if (t.getCreador() != null && t.getCreador().getId().equals(user.getId())) {
                        queMeDeben += share * (numMembers - 1);
                    } else {
                        queDebo += share;
                    }
                }
            }
        }

        model.addAttribute("username", user.getNombre());
        model.addAttribute("email", user.getEmail());
        model.addAttribute("balance", Math.round(balance * 100.0) / 100.0);
        model.addAttribute("queMeDeben", Math.round(queMeDeben * 100.0) / 100.0);
        model.addAttribute("queDebo", Math.round(queDebo * 100.0) / 100.0);

        return "user-info";
    }
}
