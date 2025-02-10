package com.PrototipoManageService.PetuniaPrototipeSpring.Service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.PrototipoManageService.PetuniaPrototipeSpring.dto.VerifyPdfResponse;

@Service
public class VerifyService {

    public VerifyPdfResponse verifyPdf(MultipartFile file) {
        // Implementa la lógica real de verificación usando PDFBox, BouncyCastle, etc.
        // Aquí se devuelve una respuesta simulada:
        VerifyPdfResponse response = new VerifyPdfResponse();
        response.setSignatures("Simulated signature data");
        response.setVerifiedAt(true);
        response.setValid(true);
        response.setHasWarnings(false);
        return response;
    }
}