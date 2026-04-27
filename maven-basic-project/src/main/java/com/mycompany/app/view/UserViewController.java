package com.mycompany.app.view;

import com.mycompany.app.dto.UserCreationDTO;
import com.mycompany.app.model.Deuda;
import com.mycompany.app.model.EstadoDeuda;
import com.mycompany.app.model.Usuario;
import com.mycompany.app.model.SavingGoals;
import com.mycompany.app.repository.DeudaRepository;
import com.mycompany.app.repository.UsuarioRepository;
import com.mycompany.app.repository.SavingGoalsRepository;
import com.mycompany.app.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/web/user")
public class UserViewController {

    private final AuthService authService;
    private final UsuarioRepository usuarioRepository;
    private final DeudaRepository deudaRepository;
    private final SavingGoalsRepository savingGoalsRepository;
    private final RestTemplate restTemplate;

    public UserViewController(AuthService authService,
                               UsuarioRepository usuarioRepository,
                               DeudaRepository deudaRepository,
                               SavingGoalsRepository savingGoalsRepository,
                               RestTemplate restTemplate) {
        this.authService = authService;
        this.usuarioRepository = usuarioRepository;
        this.deudaRepository = deudaRepository;
        this.savingGoalsRepository = savingGoalsRepository;
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

        // Balance comes directly from the Usuario entity (maintained by TransactionService)
        double balance = user.getBalance() != null ? user.getBalance() : 0.0;

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

        SavingGoals objetivo = savingGoalsRepository.findByUser(user);
        model.addAttribute("objetivo", objetivo);

        return "user-info";
    }
    @PostMapping("/objetivo/create")
    public String createObjetivo(@CookieValue(value = "token", required = false) String token,
                                 @RequestParam Double amount,
                                 @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        if (token == null || !authService.isValidToken(token)) return "redirect:/web/auth/login";
        
        Usuario user = usuarioRepository.findByEmail(authService.getEmailFromToken(token));
        
        SavingGoals existente = savingGoalsRepository.findByUser(user);
        if (existente != null) {
            savingGoalsRepository.delete(existente);
        }
        
        SavingGoals nuevoObjetivo = new SavingGoals(user, amount, LocalDate.now(), endDate);
        savingGoalsRepository.save(nuevoObjetivo);
        
        return "redirect:/web/user/profile";
    }

    @PostMapping("/objetivo/delete")
    public String deleteObjetivo(@CookieValue(value = "token", required = false) String token) {
        if (token == null || !authService.isValidToken(token)) return "redirect:/web/auth/login";
        
        Usuario user = usuarioRepository.findByEmail(authService.getEmailFromToken(token));
        SavingGoals existente = savingGoalsRepository.findByUser(user);
        
        if (existente != null) {
            savingGoalsRepository.delete(existente);
        }
        
        return "redirect:/web/user/profile";
    }
}
