package com.mycompany.app.facade;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AuthViewController {

    @GetMapping("/web/auth/login")
    public String login() {
        return "login";
    }

    @GetMapping("/web")
    public String hello() {
        return "redirect:/web/auth/login";
    }

    @GetMapping("/web/user/info")
    public String userInfo(){
        return "user-info";
    }
}
