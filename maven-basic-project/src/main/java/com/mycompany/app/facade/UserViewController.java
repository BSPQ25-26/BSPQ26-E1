package com.mycompany.app.facade;

import com.mycompany.app.dto.UserCreationDTO;
import com.mycompany.app.dto.UserInfoDTO;
import com.mycompany.app.service.AuthService;
import com.mycompany.app.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/web/user")
public class UserViewController {

    private final UserService userService;
    private final AuthService authService;

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
                model.addAttribute("error", "No se pudo crear el usuario");
                return "user-create";
            }
            model.addAttribute("mensaje", "Usuario creado correctamente");
            return "user-create";
        } catch (Exception e) {
            model.addAttribute("error", "Error interno del servidor");
            return "user-create";
        }
    }

    @GetMapping("/{email}")
    public String getUserInfo(
            @PathVariable String email,
            @RequestParam("token") String token,
            Model model
    ) {
        if (!authService.isValidToken(token)) {
            model.addAttribute("error", "Unauthorized access. Please login.");
            return "error";
        }
        UserInfoDTO usuario = userService.getUserInfo(email);
        if (usuario == null) {
            model.addAttribute("error", "User not found.");
            return "error";
        }
        model.addAttribute("user", usuario);
        return "user-info";
    }
}
