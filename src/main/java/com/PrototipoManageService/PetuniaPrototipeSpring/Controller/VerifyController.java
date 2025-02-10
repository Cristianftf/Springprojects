package com.PrototipoManageService.PetuniaPrototipeSpring.Controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.PrototipoManageService.PetuniaPrototipeSpring.Service.VerifyService;
import com.PrototipoManageService.PetuniaPrototipeSpring.dto.VerifyPdfResponse;

@RestController
@RequestMapping("/api/verify")
public class VerifyController {

    private final VerifyService verifyService;

    public VerifyController(VerifyService verifyService) {
        this.verifyService = verifyService;
    }

    @PostMapping("/verify-pdf")
    public ResponseEntity<VerifyPdfResponse> verifyPdf(@RequestParam("file") MultipartFile file,
                                                       @RequestParam(value = "userCid", required = false) String userCid,
                                                       @RequestParam(value = "userId", required = false) String userId) {
        VerifyPdfResponse response = verifyService.verifyPdf(file);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}