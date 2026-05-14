package com.mycompany.app.view;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.mycompany.app.model.Usuario;
import com.mycompany.app.repository.UsuarioRepository;
import com.mycompany.app.service.AuthService;
import com.mycompany.app.service.ChatService;

@Controller
@RequestMapping("/web/chat")
public class ChatViewController {

    private final ChatService chatService;
    private final AuthService authService;
    private final UsuarioRepository usuarioRepository;

    public ChatViewController(ChatService chatService,
                               AuthService authService,
                               UsuarioRepository usuarioRepository) {
        this.chatService = chatService;
        this.authService = authService;
        this.usuarioRepository = usuarioRepository;
    }

    @GetMapping("/monthly-advice")
    public String getMonthlyAdvice(
            @CookieValue(value = "token", required = false) String token,
            Model model
    ) {
        if (token == null || !authService.isValidToken(token)) return "redirect:/web/auth/login";

        String email = authService.getEmailFromToken(token);
        Usuario user = usuarioRepository.findByEmail(email);
        if (user == null) return "redirect:/web/auth/login";

        try {
            String advice = chatService.analyzeExpenses(user.getId());
            model.addAttribute("advice", advice);
        } catch (Exception e) {
            model.addAttribute("error", "Could not get AI advice: " + e.getMessage());
        }

        return "chat";
    }
}