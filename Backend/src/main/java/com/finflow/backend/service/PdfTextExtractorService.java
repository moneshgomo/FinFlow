package com.finflow.backend.service;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;

@Service
public class PdfTextExtractorService {

    public String extractText(byte[] pdfBytes) {

        try (var document = Loader.loadPDF(pdfBytes)) {

            PDFTextStripper stripper = new PDFTextStripper();

            return stripper.getText(document);

        } catch (Exception e) {

            throw new RuntimeException(
                    "Unable to extract text from PDF."
            );
        }
    }
}