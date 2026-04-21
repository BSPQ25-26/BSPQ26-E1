package com.mycompany.app.view;

import com.mycompany.app.dto.CategoryCreationDTO;
import com.mycompany.app.model.Usuario;
import com.mycompany.app.repository.CategoryRepository;
import com.mycompany.app.repository.UsuarioRepository;
import com.mycompany.app.service.AuthService;
import com.mycompany.app.service.CategoryService;

import jakarta.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

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
    public String category(@RequestParam(required = false) String token,
                           @RequestParam(required = false) Boolean showForm,
                           HttpSession session, Model model) {
        if (token != null) session.setAttribute("token", token);

        String storedToken = (String) session.getAttribute("token");
        if (storedToken == null || !authService.isValidToken(storedToken)) {
            return "redirect:/web/auth/login";
        }

        String email = authService.getEmailFromToken(storedToken);
        session.setAttribute("userEmail", email);

        Usuario user = usuarioRepository.findByEmail(email);
        if (user == null) return "redirect:/web/auth/login";

        model.addAttribute("listaCategorias", categoryService.getCategoriesByUser(user.getId()));
        model.addAttribute("showForm", Boolean.TRUE.equals(showForm));

        return "category";
    }

    @PostMapping("/create")
    public String createCategory(@RequestParam String name, HttpSession session) {
        String storedToken = (String) session.getAttribute("token");
        String email = (String) session.getAttribute("userEmail");
        if (storedToken == null || email == null) return "redirect:/web/auth/login";

        Usuario user = usuarioRepository.findByEmail(email);
        if (user == null) return "redirect:/web/auth/login";

        CategoryCreationDTO dto = new CategoryCreationDTO();
        dto.setName(name);
        dto.setUserId(user.getId());
        dto.setToken(storedToken);
        categoryService.createCategory(dto);

        return "redirect:/web/categories";
    }

    @PostMapping("/delete")
    public String deleteCategory(@RequestParam Integer categoryId, HttpSession session) {
        if (session.getAttribute("token") == null) return "redirect:/web/auth/login";
        if (categoryId != null) categoryRepository.deleteById(categoryId);
        return "redirect:/web/categories";
    }
}
