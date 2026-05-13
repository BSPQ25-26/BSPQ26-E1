package com.mycompany.app.service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import com.mycompany.app.model.Transaction;
import com.mycompany.app.repository.TransactionRepository;

@Service
public class ChatService {

    private final ChatClient chatClient;
    private final TransactionRepository transactionRepository;

    public ChatService(ChatClient.Builder builder, TransactionRepository transactionRepository) {
        this.chatClient = builder.build();
        this.transactionRepository = transactionRepository;
    }

    public String analyzeMonthlyExpenses(Integer userId) {
        String expensesSummary = compileUserExpenses(userId);

        String systemInstructions = "You are a financial advisor for young adults. Be brief and direct. Analyze the provided expenses and return 3 savings tips. IMPORTANT: Respond EXCLUSIVELY in valid JSON format. Do not use Markdown code blocks (```json). The JSON must contain exactly these two keys: 1. 'general_analysis' (string): A short sentence about how you view the expenses. 2. 'tips' (array of strings): A list of 3 specific savings tips.";

        return chatClient.prompt()
                .system(systemInstructions)
                .user("Here are my grouped expenses: " + expensesSummary)
                .call()
                .content();
    }

    private String compileUserExpenses(Integer userId) {
        List<Transaction> userTransactions = transactionRepository.findByCreadorId(userId);
        Map<String, Double> groupedExpenses = userTransactions.stream()
                .filter(t -> t.getTipoTransaccion() != null && t.getTipoTransaccion().equalsIgnoreCase("GASTO"))
                .collect(Collectors.groupingBy(
                        this::getCategoryName, 
                        Collectors.summingDouble(Transaction::getImporteTotal)
                ));

        if (groupedExpenses.isEmpty()) {
            return "The user currently has no registered expenses.";
        }
        return groupedExpenses.entrySet().stream()
                .map(entry -> entry.getKey() + ": " + entry.getValue() + "€")
                .collect(Collectors.joining(", "));
    }

    private String getCategoryName(Transaction t) {
        if (t.getCategoria() != null && t.getCategoria().getName() != null) {
            return t.getCategoria().getName();
        }
        return "Other / Uncategorized";
    }
}