package com.PrototipoManageService.PetuniaPrototipeSpring.Service;

import org.springframework.stereotype.Service;

@Service
public class PdfSignerService {
    
    private final PdfSigner pdfSigner;
    
    public byte[] signPdf(InputStream pdfInputStream, InputStream pkcs12InputStream, String password) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PdfSigner signer = new PdfSigner(pdfInputStream, pkcs12InputStream);
        signer.sign(outputStream);
        return outputStream.toByteArray();
    }
}
