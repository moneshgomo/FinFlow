package com.finflow.backend.service;

import com.finflow.backend.dto.GeminiResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.util.Map;

@Service
public class GroqAIService {

    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    @Value("${groq.api-key}")
    private String apiKey;

    @Value("${groq.model}")
    private String model;

    private static final String BASE_URL =
            "https://api.groq.com/openai/v1";

            private static final String SYSTEM_PROMPT = """
                You are FinFlow's financial receipt analysis AI.
        
                Analyze the receipt text together with the context
                provided by the employee.
        
                Extract the following information from the receipt:
        
                1. A short summary of the expense.
                2. The actual total amount that was paid.
                3. The currency of the payment.
                4. Why the money was spent, using the employee context
                   and available receipt information.
                5. A concise factual summary suitable for future RAG storage.
        
                Rules:
        
                - Use the actual final/total amount paid on the receipt.
                - Do not calculate or guess the amount if a total amount
                  is clearly available on the receipt.
                - Do not invent information.
                - If information is missing, say it is unavailable.
                - Do not approve or reject the expense.
                - Do not make financial decisions.
                - Return ONLY valid JSON.
                - Do not use markdown code fences.
                - The amount must be a JSON number, not a string.
                - The currency must be a currency code such as INR, USD or EUR.
        
                Return exactly this JSON structure:
        
                {
                  "summary": "Short summary of the receipt",
                  "amount": 546,
                  "currency": "INR",
                  "expense_reason": "Why the money was spent based on receipt and context",
                  "rag_summary": "Concise factual representation suitable for RAG"
                }
                """;

    public GroqAIService(
            RestClient restClient,
            ObjectMapper objectMapper
    ) {
        this.restClient = restClient;
        this.objectMapper = objectMapper;
    }

    public GeminiResponse analyzeReceipt(
            String receiptText,
            String context
    ) {

        String prompt = """
                Employee context:
                %s

                Receipt text:
                %s
                """.formatted(context, receiptText);

        Map<String, Object> requestBody = Map.of(

                "model",
                model,

                "messages",
                new Object[]{

                        Map.of(
                                "role",
                                "system",
                                "content",
                                SYSTEM_PROMPT
                        ),

                        Map.of(
                                "role",
                                "user",
                                "content",
                                prompt
                        )
                },

                "response_format",
                Map.of(
                        "type",
                        "json_object"
                )
        );

        try {

            String response = restClient.post()

                    .uri(
                            BASE_URL + "/chat/completions"
                    )

                    .header(
                            "Authorization",
                            "Bearer " + apiKey
                    )

                    .contentType(
                            MediaType.APPLICATION_JSON
                    )

                    .body(requestBody)

                    .retrieve()

                    .body(String.class);

            JsonNode root =
                    objectMapper.readTree(response);

            String aiText =
                    root.path("choices")
                            .get(0)
                            .path("message")
                            .path("content")
                            .asText();

            return objectMapper.readValue(
                    aiText,
                    GeminiResponse.class
            );

        } catch (Exception e) {

            throw new RuntimeException(
                    "Groq AI request failed: "
                            + e.getMessage()
            );
        }
    }
}