package com.mycompany.app.view;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/web/categories")
public class CategoryViewController {
    @GetMapping()
    public String category(){
        return "category";
    }
}
