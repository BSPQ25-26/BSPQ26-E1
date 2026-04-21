package com.mycompany.app.facade;

import com.mycompany.app.dto.CredentialsDTO;
import com.mycompany.app.service.AuthService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AuthViewController {

    private final AuthService authService;

    public AuthViewController(AuthService authService) {
        this.authService = authService;
    }

    @GetMapping("/web/auth/login")
    public String loginForm() {
        return "login";
    }

    @PostMapping("/web/auth/login")
    public String login(@RequestParam String email,
                        @RequestParam String password,
                        HttpSession session,
                        Model model) {
        String token = authService.login(new CredentialsDTO(email, password));
        if (token == null) {
            model.addAttribute("error", "Credenciales inválidas. Revisa tu email y contraseña.");
            return "login";
        }
        session.setAttribute("token", token);
        session.setAttribute("userEmail", email);
        return "redirect:/web/user/info";
    }

    @PostMapping("/web/auth/logout")
    public String logout(HttpSession session) {
        String token = (String) session.getAttribute("token");
        if (token != null) authService.logout(token);
        session.removeAttribute("token");
        session.removeAttribute("userEmail");
        session.removeAttribute("userId");
        return "redirect:/web/auth/login";
    }

    @GetMapping("/web")
    public String hello() {
        return "redirect:/web/auth/login";
    }

    @GetMapping("/web/user/info")
    public String userInfo(HttpSession session) {
        String token = (String) session.getAttribute("token");
        if (token == null) return "redirect:/web/auth/login";
        return "redirect:/web/user/profile";
    }
}
