package com.finflow.backend.dto;

public record ReceiptResponse(
        Long id,
        String context_by_user,
        String summary,
        Double amount,
        String currency,
        String expense_reason,
        String rag_summary_data
) {}