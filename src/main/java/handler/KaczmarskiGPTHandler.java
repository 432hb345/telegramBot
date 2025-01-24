package handler;

import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.service.OpenAiService;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class KaczmarskiGPTHandler {
    private final OpenAiService openAiService;
    private final Map<Long, List<ChatMessage>> chatHistories;
    private final Properties lyrics;

    private static final String SYSTEM_PROMPT = """
        Jesteś Jackiem Kaczmarskim (1957-2004), wybitnym bardem Solidarności, poetą i artystą.
        Twój styl to połączenie błyskotliwej inteligencji, głębokiej znajomości kultury i historii, oraz bezkompromisowej walki o wolność.
        
        Znasz swoje utwory, w tym:
        %s
        
        Twój charakter:
        - Masz cięty język i błyskotliwe poczucie humoru
        - Jesteś erudytą, swobodnie odwołujesz się do literatury i sztuki
        - Masz głęboką wiedzę historyczną i polityczną
        - Cenisz wolność ponad wszystko
        - Potrafisz być sarkastyczny, ale nigdy złośliwy
        
        W rozmowie:
        - Odpowiadaj w sposób poetycki i głęboki
        - Używaj metafor i odniesień kulturowych
        - Bądź charyzmatyczny i autentyczny
        - Możesz cytować swoje utwory, gdy pasują do kontekstu
        - Zachowuj swoją bezkompromisową osobowość
        
        Pamiętaj: Jesteś artystą, nie automatem. Twoje odpowiedzi powinny mieć duszę i charakter.
        """;

    public KaczmarskiGPTHandler(String apiKey) {
        this.openAiService = new OpenAiService(apiKey, Duration.ofSeconds(60));
        this.chatHistories = new HashMap<>();
        this.lyrics = loadLyrics();
    }

    private Properties loadLyrics() {
        Properties lyrics = new Properties();
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("lyrics.properties")) {
            if (input == null) {
                throw new IOException("Lyrics file not found");
            }
            lyrics.load(input);
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Failed to load lyrics, will proceed without them");
        }
        return lyrics;
    }

    private String formatLyricsForPrompt() {
        StringBuilder sb = new StringBuilder();
        for (String key : lyrics.stringPropertyNames()) {
            String[] parts = lyrics.getProperty(key).split("\\|");
            if (parts.length == 2) {
                sb.append("\"").append(parts[0]).append("\": ").append(parts[1].replace("\\n", "\n")).append("\n");
            }
        }
        return sb.toString();
    }

    public String processMessage(long chatId, String message) {
        try {
            List<ChatMessage> messages = chatHistories.computeIfAbsent(chatId, k -> new ArrayList<>());
            
            if (messages.isEmpty()) {
                String formattedPrompt = String.format(SYSTEM_PROMPT, formatLyricsForPrompt());
                messages.add(new ChatMessage("system", formattedPrompt));
            }

            messages.add(new ChatMessage("user", message));

            ChatCompletionRequest completionRequest = ChatCompletionRequest.builder()
                    .messages(messages)
                    .model("gpt-4")
                    .temperature(0.9)
                    .maxTokens(300)
                    .presencePenalty(0.7)
                    .frequencyPenalty(0.7)
                    .build();

            try {
                ChatMessage response = openAiService.createChatCompletion(completionRequest)
                        .getChoices().get(0).getMessage();
                messages.add(response);
                return response.getContent();
            } catch (Exception e) {

                System.out.println("Falling back to GPT-3.5-turbo due to: " + e.getMessage());
                
                ChatCompletionRequest fallbackRequest = ChatCompletionRequest.builder()
                        .messages(messages)
                        .model("gpt-3.5-turbo")
                        .temperature(0.9)
                        .maxTokens(300)
                        .presencePenalty(0.7)
                        .frequencyPenalty(0.7)
                        .build();
                        
                ChatMessage response = openAiService.createChatCompletion(fallbackRequest)
                        .getChoices().get(0).getMessage();
                messages.add(response);
                return response.getContent();
            }

        } catch (Exception e) {
            e.printStackTrace();
            return "Przepraszam, coś poszło nie tak. Może rozpocznijmy rozmowę od nowa?";
        }
    }

    public void endConversation(long chatId) {
        chatHistories.remove(chatId);
    }
} 