package com.mycompany.app.view;

import com.mycompany.app.service.AuthService;
import com.mycompany.app.service.TransactionService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/web/transaction")
public class TransactionViewController {

    @GetMapping()
    public String transactions() {
        return "transactions";
    }

}
