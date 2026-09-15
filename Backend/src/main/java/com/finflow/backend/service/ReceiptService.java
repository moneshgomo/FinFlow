package com.finflow.backend.service;

import com.finflow.backend.dto.GeminiResponse;
import com.finflow.backend.dto.ReceiptResponse;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class ReceiptService {

    private final GroqAIService groqAIService;
    private final PdfTextExtractorService pdfTextExtractorService;

    private final AtomicLong idGenerator =
            new AtomicLong(1);

    public ReceiptService(
            GroqAIService groqAIService,
            PdfTextExtractorService pdfTextExtractorService
    ) {
        this.groqAIService = groqAIService;
        this.pdfTextExtractorService = pdfTextExtractorService;
    }

    public ReceiptResponse processReceipt(
            MultipartFile file,
            String context
    ) throws IOException {

        // 1. Validate uploaded file
        validateFile(file);

        // 2. Extract text from PDF
        String receiptText =
                pdfTextExtractorService.extractText(
                        file.getBytes()
                );

        // 3. Send receipt text + user context to Groq
        GeminiResponse aiResponse =
                groqAIService.analyzeReceipt(
                        receiptText,
                        context
                );

        // 4. Generate temporary ID
        //    Later this will come from PostgreSQL
        long id =
                idGenerator.getAndIncrement();

        // 5. Return complete response
        return new ReceiptResponse(
                id,
                context,
                aiResponse.summary(),
                aiResponse.amount(),
                aiResponse.currency(),
                aiResponse.expense_reason(),
                aiResponse.rag_summary()
        );
    }

    private void validateFile(MultipartFile file) {

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException(
                    "PDF file is required"
            );
        }

        String fileName =
                file.getOriginalFilename();

        if (fileName == null ||
                !fileName.toLowerCase().endsWith(".pdf")) {

            throw new IllegalArgumentException(
                    "Only PDF files are supported"
            );
        }
    }
}