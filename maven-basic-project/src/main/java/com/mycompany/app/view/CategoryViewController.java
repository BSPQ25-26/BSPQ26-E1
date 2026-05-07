package com.mycompany.app.view;

import com.mycompany.app.dto.BudgetCreationDTO;
import com.mycompany.app.dto.CategoryCreationDTO;
import com.mycompany.app.dto.CategoryDeletionDTO;
import com.mycompany.app.model.Budget;
import com.mycompany.app.model.Category;
import com.mycompany.app.model.Transaction;
import com.mycompany.app.model.Usuario;
import com.mycompany.app.repository.BudgetRepository;
import com.mycompany.app.repository.UsuarioRepository;
import com.mycompany.app.service.AuthService;
import com.mycompany.app.service.CategoryService;
import com.mycompany.app.service.TransactionService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/web/categories")
public class CategoryViewController {

    private final CategoryService categoryService;
    private final TransactionService transactionService;
    private final BudgetRepository budgetRepository;
    private final UsuarioRepository usuarioRepository;
    private final AuthService authService;

    // Keyword → emoji mapping (checked via String.contains, order matters)
    private static final Map<String, String> EMOJI_MAP = new LinkedHashMap<>();
    static {
        EMOJI_MAP.put("food", "🍔");       EMOJI_MAP.put("comida", "🍔");
        EMOJI_MAP.put("restaurant", "🍽️"); EMOJI_MAP.put("restaurante", "🍽️");
        EMOJI_MAP.put("cafe", "☕");        EMOJI_MAP.put("coffee", "☕");
        EMOJI_MAP.put("groceries", "🛒");  EMOJI_MAP.put("supermercado", "🛒");
        EMOJI_MAP.put("market", "🛒");     EMOJI_MAP.put("mercado", "🛒");
        EMOJI_MAP.put("transport", "🚗");  EMOJI_MAP.put("transporte", "🚗");
        EMOJI_MAP.put("car", "🚗");        EMOJI_MAP.put("coche", "🚗");
        EMOJI_MAP.put("taxi", "🚕");       EMOJI_MAP.put("bus", "🚌");
        EMOJI_MAP.put("train", "🚆");      EMOJI_MAP.put("tren", "🚆");
        EMOJI_MAP.put("flight", "✈️");     EMOJI_MAP.put("vuelo", "✈️");
        EMOJI_MAP.put("health", "💊");     EMOJI_MAP.put("salud", "💊");
        EMOJI_MAP.put("medic", "🏥");      EMOJI_MAP.put("doctor", "🏥");
        EMOJI_MAP.put("farmacia", "💊");   EMOJI_MAP.put("pharmacy", "💊");
        EMOJI_MAP.put("housing", "🏠");    EMOJI_MAP.put("vivienda", "🏠");
        EMOJI_MAP.put("rent", "🏠");       EMOJI_MAP.put("alquiler", "🏠");
        EMOJI_MAP.put("home", "🏠");       EMOJI_MAP.put("hogar", "🏠");
        EMOJI_MAP.put("leisure", "🎮");    EMOJI_MAP.put("ocio", "🎮");
        EMOJI_MAP.put("entertainment", "🎬"); EMOJI_MAP.put("game", "🎮");
        EMOJI_MAP.put("movie", "🎬");      EMOJI_MAP.put("cine", "🎬");
        EMOJI_MAP.put("sport", "⚽");      EMOJI_MAP.put("deporte", "⚽");
        EMOJI_MAP.put("gym", "💪");        EMOJI_MAP.put("gimnasio", "💪");
        EMOJI_MAP.put("fitness", "💪");
        EMOJI_MAP.put("shopping", "🛍️");  EMOJI_MAP.put("compra", "🛍️");
        EMOJI_MAP.put("clothes", "👗");    EMOJI_MAP.put("ropa", "👗");
        EMOJI_MAP.put("tech", "💻");       EMOJI_MAP.put("tecnolog", "💻");
        EMOJI_MAP.put("education", "📚");  EMOJI_MAP.put("educaci", "📚");
        EMOJI_MAP.put("school", "🏫");     EMOJI_MAP.put("estudios", "📚");
        EMOJI_MAP.put("universidad", "🎓"); EMOJI_MAP.put("university", "🎓");
        EMOJI_MAP.put("travel", "✈️");     EMOJI_MAP.put("viaje", "✈️");
        EMOJI_MAP.put("hotel", "🏨");      EMOJI_MAP.put("vacation", "🌴");
        EMOJI_MAP.put("vacaciones", "🌴");
        EMOJI_MAP.put("salary", "💰");     EMOJI_MAP.put("salario", "💰");
        EMOJI_MAP.put("income", "💰");     EMOJI_MAP.put("ingreso", "💰");
        EMOJI_MAP.put("work", "💼");       EMOJI_MAP.put("trabajo", "💼");
        EMOJI_MAP.put("freelance", "💼");
        EMOJI_MAP.put("savings", "🏦");    EMOJI_MAP.put("ahorros", "🏦");
        EMOJI_MAP.put("bank", "🏦");       EMOJI_MAP.put("banco", "🏦");
        EMOJI_MAP.put("insurance", "🛡️"); EMOJI_MAP.put("seguro", "🛡️");
        EMOJI_MAP.put("subscription", "🔄"); EMOJI_MAP.put("suscripci", "🔄");
        EMOJI_MAP.put("streaming", "📺");  EMOJI_MAP.put("netflix", "📺");
        EMOJI_MAP.put("music", "🎵");      EMOJI_MAP.put("musica", "🎵");
        EMOJI_MAP.put("book", "📖");       EMOJI_MAP.put("libro", "📖");
        EMOJI_MAP.put("pet", "🐾");        EMOJI_MAP.put("mascota", "🐾");
        EMOJI_MAP.put("utilities", "💡");  EMOJI_MAP.put("servicios", "💡");
        EMOJI_MAP.put("electricity", "⚡"); EMOJI_MAP.put("electricidad", "⚡");
        EMOJI_MAP.put("internet", "📶");
        EMOJI_MAP.put("phone", "📱");      EMOJI_MAP.put("telef", "📱");
        EMOJI_MAP.put("gift", "🎁");       EMOJI_MAP.put("regalo", "🎁");
        EMOJI_MAP.put("personal", "👤");
        EMOJI_MAP.put("beauty", "💅");     EMOJI_MAP.put("belleza", "💅");
        EMOJI_MAP.put("tax", "📄");        EMOJI_MAP.put("impuesto", "📄");
        EMOJI_MAP.put("other", "📌");      EMOJI_MAP.put("otro", "📌");
        EMOJI_MAP.put("general", "📋");    EMOJI_MAP.put("varios", "📌");
    }

    public CategoryViewController(CategoryService categoryService,
                                   TransactionService transactionService,
                                   BudgetRepository budgetRepository,
                                   UsuarioRepository usuarioRepository,
                                   AuthService authService) {
        this.categoryService = categoryService;
        this.transactionService = transactionService;
        this.budgetRepository = budgetRepository;
        this.usuarioRepository = usuarioRepository;
        this.authService = authService;
    }

    private String getEmoji(String name) {
        if (name == null) return "📂";
        String lower = name.toLowerCase();
        for (Map.Entry<String, String> entry : EMOJI_MAP.entrySet()) {
            if (lower.contains(entry.getKey())) return entry.getValue();
        }
        return "📂";
    }

    // ── View model ────────────────────────────────────────────────────────────

    public static class CategorySummary {
        private final Category category;
        private final String emoji;
        private final List<Transaction> transactions;
        private final double totalExpenses;
        private final double totalIncome;
        private final Double budgetLimit;

        public CategorySummary(Category category, String emoji, List<Transaction> transactions,
                               double totalExpenses, double totalIncome, Double budgetLimit) {
            this.category = category;
            this.emoji = emoji;
            this.transactions = transactions;
            this.totalExpenses = totalExpenses;
            this.totalIncome = totalIncome;
            this.budgetLimit = budgetLimit;
        }

        public Category getCategory()             { return category; }
        public String getEmoji()                   { return emoji; }
        public List<Transaction> getTransactions() { return transactions; }
        public double getTotalExpenses()           { return totalExpenses; }
        public double getTotalIncome()             { return totalIncome; }
        public Double getBudgetLimit()             { return budgetLimit; }

        public double getBudgetPercent() {
            if (budgetLimit == null || budgetLimit <= 0) return 0;
            return Math.min(100.0, Math.round((totalExpenses / budgetLimit) * 1000.0) / 10.0);
        }

        public boolean isOverBudget() {
            return budgetLimit != null && budgetLimit > 0 && totalExpenses > budgetLimit;
        }
    }

    // ── Handlers ──────────────────────────────────────────────────────────────

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

        List<Category> categories = categoryService.getCategoriesByUser(user.getId());
        List<Transaction> allTx = transactionService.getTransactionsByUserId(user.getId());

        List<CategorySummary> summaries = categories.stream().map(cat -> {
            List<Transaction> catTx = allTx.stream()
                .filter(t -> t.getCategoria() != null && t.getCategoria().getId().equals(cat.getId()))
                .sorted(Comparator.comparing(Transaction::getFecha,
                    Comparator.nullsLast(Comparator.reverseOrder())))
                .collect(Collectors.toList());

            double expenses = catTx.stream()
                .filter(t -> "GASTO".equals(t.getTipoTransaccion()))
                .mapToDouble(Transaction::getImporteTotal).sum();

            double income = catTx.stream()
                .filter(t -> "INGRESO".equals(t.getTipoTransaccion()))
                .mapToDouble(Transaction::getImporteTotal).sum();

            Double limitAmount = null;
            try {
                Budget budget = budgetRepository
                    .findByCategoryIdAndUserId(cat.getId(), user.getId()).orElse(null);
                if (budget != null) limitAmount = budget.getLimitAmount();
            } catch (Exception ignored) {
                // budgets table not yet created in this environment
            }

            return new CategorySummary(cat, getEmoji(cat.getName()), catTx,
                expenses, income, limitAmount);
        }).collect(Collectors.toList());

        model.addAttribute("listaCategorias", summaries);
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

    // HTML forms do not support DELETE, so this has to be a POST
    @PostMapping("/delete")
    public String deleteCategory(
            @CookieValue(value = "token", required = false) String token,
            @RequestParam Integer categoryId
    ) {
        if (token == null || !authService.isValidToken(token)) return "redirect:/web/auth/login";
        if (categoryId != null) {
            CategoryDeletionDTO dto = new CategoryDeletionDTO();
            dto.setCategoryId(categoryId);
            dto.setToken(token);
            categoryService.deleteCategory(dto);
        }
        return "redirect:/web/categories";
    }

    @PostMapping("/budget")
    public String setBudget(
            @CookieValue(value = "token", required = false) String token,
            @RequestParam Integer categoryId,
            @RequestParam Double limitAmount
    ) {
        if (token == null || !authService.isValidToken(token)) return "redirect:/web/auth/login";

        String email = authService.getEmailFromToken(token);
        Usuario user = usuarioRepository.findByEmail(email);
        if (user == null) return "redirect:/web/auth/login";

        try {
            BudgetCreationDTO dto = new BudgetCreationDTO();
            dto.setCategoryId(categoryId);
            dto.setUserId(user.getId());
            dto.setLimitAmount(limitAmount);
            dto.setToken(token);
            transactionService.createBudget(dto);
        } catch (Exception ignored) {
            // budgets table not yet created in this environment
        }

        return "redirect:/web/categories";
    }
}