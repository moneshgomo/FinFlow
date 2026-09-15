package com.finflow.backend.controller;


import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.finflow.backend.dto.ReceiptResponse;
import com.finflow.backend.service.ReceiptService;
import java.io.IOException;

@RestController
@RequestMapping
public class UploadController {

    private final ReceiptService receiptService;

    public UploadController(ReceiptService receiptService) {
        this.receiptService = receiptService;
    }

    @PostMapping(
            value = "/upload",
            consumes = "multipart/form-data"
    )
    public ResponseEntity<ReceiptResponse> uploadReceipt(
            @RequestPart("file") MultipartFile file,
            @RequestPart("context") String context
    ) throws IOException {

        ReceiptResponse response =
                receiptService.processReceipt(file, context);

        return ResponseEntity.ok(response);
    }
}