package com.finflow.backend.dto;

public record GeminiResponse(
        String summary,
        Double amount,
        String currency,
        String expense_reason,
        String rag_summary
) {}