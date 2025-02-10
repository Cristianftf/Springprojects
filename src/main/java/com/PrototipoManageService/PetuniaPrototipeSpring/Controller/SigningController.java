package com.PrototipoManageService.PetuniaPrototipeSpring.Controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.PrototipoManageService.PetuniaPrototipeSpring.Service.SigningService;
import com.PrototipoManageService.PetuniaPrototipeSpring.dto.SignPdfRequest;

@RestController
@RequestMapping("/api/sign")
public class SigningController {

    private final SigningService signingService;

    public SigningController(SigningService signingService) {
        this.signingService = signingService;
    }

    @PostMapping("/sign-pdf")
    public ResponseEntity<byte[]> signPdf(@RequestBody SignPdfRequest request) {
        try {
            byte[] signedPdf = signingService.signPdf(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(signedPdf);
        } catch (RuntimeException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }
}