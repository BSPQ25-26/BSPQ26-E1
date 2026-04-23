package com.mycompany.app.view;

import com.mycompany.app.dto.UserCreationDTO;
import com.mycompany.app.model.Deuda;
import com.mycompany.app.model.EstadoDeuda;
import com.mycompany.app.model.Transaction;
import com.mycompany.app.model.Usuario;
import com.mycompany.app.repository.DeudaRepository;
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
import java.util.stream.Collectors;

@Controller
@RequestMapping("/web/user")
public class UserViewController {

    private final AuthService authService;
    private final UsuarioRepository usuarioRepository;
    private final TransactionRepository transactionRepository;
    private final DeudaRepository deudaRepository;
    private final RestTemplate restTemplate;

    public UserViewController(AuthService authService,
                               UsuarioRepository usuarioRepository,
                               TransactionRepository transactionRepository,
                               DeudaRepository deudaRepository,
                               RestTemplate restTemplate) {
        this.authService = authService;
        this.usuarioRepository = usuarioRepository;
        this.transactionRepository = transactionRepository;
        this.deudaRepository = deudaRepository;
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

        // Balance = INGRESO - GASTO - LIQUIDACION from all user's transactions
        List<Transaction> myTransactions = transactionRepository.findByCreador(user);
        double balance = 0.0;
        for (Transaction t : myTransactions) {
            switch (t.getTipoTransaccion()) {
                case "INGRESO"     -> balance += t.getImporteTotal();
                case "GASTO",
                     "LIQUIDACION" -> balance -= t.getImporteTotal();
                default            -> { /* ignore unknown types */ }
            }
        }

        // Debts where user is debtor (PENDIENTE)
        List<Deuda> debtsAsDebtor = deudaRepository.findByDeudorId(user.getId()).stream()
                .filter(d -> d.getEstado() == EstadoDeuda.PENDIENTE)
                .collect(Collectors.toList());

        // Debts where user is creditor (PENDIENTE)
        List<Deuda> debtsAsCreditor = deudaRepository.findByAcreedorId(user.getId()).stream()
                .filter(d -> d.getEstado() == EstadoDeuda.PENDIENTE)
                .collect(Collectors.toList());

        double owes  = debtsAsDebtor.stream().mapToDouble(Deuda::getImporte).sum();
        double owed  = debtsAsCreditor.stream().mapToDouble(Deuda::getImporte).sum();

        model.addAttribute("username", user.getNombre());
        model.addAttribute("email", user.getEmail());
        model.addAttribute("balance", Math.round(balance * 100.0) / 100.0);
        model.addAttribute("owes",    Math.round(owes   * 100.0) / 100.0);
        model.addAttribute("owed",    Math.round(owed   * 100.0) / 100.0);
        model.addAttribute("debtsAsDebtor",   debtsAsDebtor);
        model.addAttribute("debtsAsCreditor", debtsAsCreditor);

        return "user-info";
    }
}
