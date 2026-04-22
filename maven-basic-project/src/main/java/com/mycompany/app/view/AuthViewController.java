package com.mycompany.app.view;

import com.mycompany.app.dto.CredentialsDTO;
import com.mycompany.app.service.AuthService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

@Controller
public class AuthViewController {

    private final AuthService authService;
    private final RestTemplate restTemplate;

    public AuthViewController(AuthService authService, RestTemplate restTemplate) {
        this.authService = authService;
        this.restTemplate = restTemplate;
    }

    @GetMapping("/web")
    public String root() {
        return "redirect:/web/auth/login";
    }

    @GetMapping("/web/auth/login")
    public String loginForm(@CookieValue(value = "token", required = false) String token) {
        if (token != null && authService.isValidToken(token)) {
            return "redirect:/web/user/profile";
        }
        return "login";
    }

    @PostMapping("/web/auth/login")
    public String doLogin(
            @RequestParam String email,
            @RequestParam String password,
            HttpServletRequest request,
            HttpServletResponse response,
            Model model
    ) {
        String baseUrl = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort();
        try {
            ResponseEntity<String> apiResponse = restTemplate.postForEntity(
                    baseUrl + "/auth/login", new CredentialsDTO(email, password), String.class);
            Cookie cookie = new Cookie("token", apiResponse.getBody());
            cookie.setPath("/");
            response.addCookie(cookie);
            return "redirect:/web/user/profile";
        } catch (Exception e) {
            model.addAttribute("error", "Invalid email or password.");
            return "login";
        }
    }

    @PostMapping("/web/auth/logout")
    public String logout(
            @CookieValue(value = "token", required = false) String token,
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        if (token != null) {
            String baseUrl = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort();
            restTemplate.postForEntity(baseUrl + "/auth/logout?token=" + token, null, Void.class);
        }
        Cookie cookie = new Cookie("token", "");
        cookie.setPath("/");
        cookie.setMaxAge(0);
        response.addCookie(cookie);
        return "redirect:/web/auth/login";
    }

    @GetMapping("/web/user/info")
    public String userInfo(@CookieValue(value = "token", required = false) String token) {
        if (token == null || !authService.isValidToken(token)) {
            return "redirect:/web/auth/login";
        }
        return "redirect:/web/user/profile";
    }
}