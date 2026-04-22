package com.mycompany.app.view;

import com.mycompany.app.dto.CategoryCreationDTO;
import com.mycompany.app.model.Usuario;
import com.mycompany.app.repository.CategoryRepository;
import com.mycompany.app.repository.UsuarioRepository;
import com.mycompany.app.service.AuthService;
import com.mycompany.app.service.CategoryService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/web/categories")
public class CategoryViewController {

    private final CategoryService categoryService;
    private final CategoryRepository categoryRepository;
    private final UsuarioRepository usuarioRepository;
    private final AuthService authService;

    public CategoryViewController(CategoryService categoryService,
                                   CategoryRepository categoryRepository,
                                   UsuarioRepository usuarioRepository,
                                   AuthService authService) {
        this.categoryService = categoryService;
        this.categoryRepository = categoryRepository;
        this.usuarioRepository = usuarioRepository;
        this.authService = authService;
    }

    @GetMapping
    public String category(
            @CookieValue(value = "token", required = false) String token,
            @RequestParam(required = false) Boolean showForm,
            Model model
    ) {
        if (token == null || !authService.isValidToken(token)) return "redirect:/web/auth/login";

        String email = authService.getEmailFromToken(token);
        Usuario user = usuarioRepository.findByEmail(email);
        if (user == null) return "redirect:/web/auth/login";

        model.addAttribute("listaCategorias", categoryService.getCategoriesByUser(user.getId()));
        model.addAttribute("showForm", Boolean.TRUE.equals(showForm));

        return "category";
    }

    @PostMapping("/create")
    public String createCategory(
            @CookieValue(value = "token", required = false) String token,
            @RequestParam String name
    ) {
        if (token == null || !authService.isValidToken(token)) return "redirect:/web/auth/login";

        String email = authService.getEmailFromToken(token);
        Usuario user = usuarioRepository.findByEmail(email);
        if (user == null) return "redirect:/web/auth/login";

        CategoryCreationDTO dto = new CategoryCreationDTO();
        dto.setName(name);
        dto.setUserId(user.getId());
        dto.setToken(token);
        categoryService.createCategory(dto);

        return "redirect:/web/categories";
    }

    //HTML forms do not support DELETE functions so this has to be a POST
    @PostMapping("/delete")
    public String deleteCategory(
            @CookieValue(value = "token", required = false) String token,
            @RequestParam Integer categoryId
    ) {
        if (token == null || !authService.isValidToken(token)) return "redirect:/web/auth/login";
        if (categoryId != null) categoryRepository.deleteById(categoryId);
        return "redirect:/web/categories";
    }
}