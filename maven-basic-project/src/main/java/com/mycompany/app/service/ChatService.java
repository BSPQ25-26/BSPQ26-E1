import org.springframework.stereotype.Service;
import org.springframework.ai.chat.client.ChatClient;

@Service
public class ChatService {

    private final ChatClient chatClient;

    public ChatService(ChatClient.Builder builder) {
        this.chatClient = builder.build();
    }

    public String obtenerConsejosDeAhorro(String resumenGastos) {
        return chatClient.prompt()
                .system("Eres un asesor financiero para jóvenes. Sé breve y un poco divertido.")
                .user("Estos son mis gastos del mes: " + resumenGastos + ". ¿Cómo puedo ahorrar?")
                .call()
                .content();
    }
}