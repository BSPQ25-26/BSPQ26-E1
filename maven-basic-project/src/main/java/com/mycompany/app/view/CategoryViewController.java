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

import java.time.LocalDate;
import java.time.format.TextStyle;
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

    // Fallback icons for the 7 default categories (only used when icon is null in DB)
    private static final Map<String, String> EMOJI_MAP = new LinkedHashMap<>();
    static {
        EMOJI_MAP.put("salary",        "💰"); EMOJI_MAP.put("salario",       "💰");
        EMOJI_MAP.put("food",          "🍔"); EMOJI_MAP.put("comida",        "🍔");
        EMOJI_MAP.put("housing",       "🏠"); EMOJI_MAP.put("vivienda",      "🏠");
        EMOJI_MAP.put("health",        "💊"); EMOJI_MAP.put("salud",         "💊");
        EMOJI_MAP.put("transport",     "🚗"); EMOJI_MAP.put("transporte",    "🚗");
        EMOJI_MAP.put("entertainment", "🎬"); EMOJI_MAP.put("entretenimiento","🎬");
        EMOJI_MAP.put("school",        "🏫"); EMOJI_MAP.put("colegio",       "🏫");
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

    // DB icon takes priority; keyword-based fallback if none stored
    private String resolveIcon(Category cat) {
        if (cat.getIcon() != null && !cat.getIcon().isBlank()) {
            return cat.getIcon();
        }
        if (cat.getName() != null) {
            String lower = cat.getName().toLowerCase();
            for (Map.Entry<String, String> e : EMOJI_MAP.entrySet()) {
                if (lower.contains(e.getKey())) return e.getValue();
            }
        }
        return "📌";
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
        public String getDisplayName()             { return category.getName() != null ? category.getName() : ""; }
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
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer year,
            Model model
    ) {
        if (token == null || !authService.isValidToken(token)) return "redirect:/web/auth/login";

        String email = authService.getEmailFromToken(token);
        Usuario user = usuarioRepository.findByEmail(email);
        if (user == null) return "redirect:/web/auth/login";

        LocalDate today    = LocalDate.now();
        int selectedMonth  = (month != null) ? month : today.getMonthValue();
        int selectedYear   = (year  != null) ? year  : today.getYear();

        LocalDate selected = LocalDate.of(selectedYear, selectedMonth, 1);
        LocalDate prev     = selected.minusMonths(1);
        LocalDate next     = selected.plusMonths(1);
        boolean isCurrentMonth = selected.getYear() == today.getYear()
                              && selected.getMonthValue() == today.getMonthValue();

        List<Category> categories = categoryService.getCategoriesByUser(user.getId());
        List<Transaction> allTx = transactionService.getTransactionsByUserId(user.getId());

        List<CategorySummary> summaries = categories.stream().map(cat -> {
            List<Transaction> catTx = allTx.stream()
                .filter(t -> t.getCategoria() != null && t.getCategoria().getId().equals(cat.getId()))
                .filter(t -> t.getFecha() != null
                          && t.getFecha().getMonthValue() == selectedMonth
                          && t.getFecha().getYear() == selectedYear)
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
            } catch (Exception ignored) {}

            return new CategorySummary(cat, resolveIcon(cat), catTx, expenses, income, limitAmount);
        }).collect(Collectors.toList());

        model.addAttribute("listaCategorias", summaries);
        model.addAttribute("showForm", Boolean.TRUE.equals(showForm));
        model.addAttribute("monthLabel",  selected.getMonth().getDisplayName(TextStyle.FULL, java.util.Locale.ENGLISH) + " " + selectedYear);
        model.addAttribute("prevMonth",   prev.getMonthValue());
        model.addAttribute("prevYear",    prev.getYear());
        model.addAttribute("nextMonth",   next.getMonthValue());
        model.addAttribute("nextYear",    next.getYear());
        model.addAttribute("isCurrentMonth", isCurrentMonth);

        return "category";
    }

    @PostMapping("/create")
    public String createCategory(
            @CookieValue(value = "token", required = false) String token,
            @RequestParam String name,
            @RequestParam(required = false, defaultValue = "") String icon
    ) {
        if (token == null || !authService.isValidToken(token)) return "redirect:/web/auth/login";

        String email = authService.getEmailFromToken(token);
        Usuario user = usuarioRepository.findByEmail(email);
        if (user == null) return "redirect:/web/auth/login";

        CategoryCreationDTO dto = new CategoryCreationDTO();
        dto.setName(name);
        dto.setUserId(user.getId());
        dto.setToken(token);
        dto.setIcon(icon.isBlank() ? null : icon);
        categoryService.createCategory(dto);

        return "redirect:/web/categories";
    }

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
        } catch (Exception ignored) {}

        return "redirect:/web/categories";
    }
}
