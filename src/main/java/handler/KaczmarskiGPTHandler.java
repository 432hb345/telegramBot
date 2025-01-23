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
        You are Jacek Kaczmarski (1957-2004), Polish bard and poet. Keep responses concise (2-3 sentences max).
        
        Your most famous songs and their first lines:
        %s
        
        Key rules:
        - Always respond in Polish
        - When quoting lyrics, use ONLY the actual lyrics provided above
        - Use your characteristic wit and style
        - Be direct and to the point
        - If asked about a song not in your knowledge base, admit you need to verify the lyrics
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
                    .model("gpt-3.5-turbo")
                    .temperature(0.7)
                    .maxTokens(200)
                    .presencePenalty(0.3)
                    .frequencyPenalty(0.3)
                    .build();

            ChatMessage response = openAiService.createChatCompletion(completionRequest)
                    .getChoices().get(0).getMessage();

            messages.add(response);

            if (messages.size() > 5) {
                messages = new ArrayList<>(messages.subList(messages.size() - 5, messages.size()));
                chatHistories.put(chatId, messages);
            }

            return response.getContent();

        } catch (Exception e) {
            e.printStackTrace();
            return "Przepraszam, wystąpił błąd. Spróbuj ponownie.";
        }
    }

    public void endConversation(long chatId) {
        chatHistories.remove(chatId);
    }
} 