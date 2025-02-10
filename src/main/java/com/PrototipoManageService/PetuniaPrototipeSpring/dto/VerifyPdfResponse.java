package com.PrototipoManageService.PetuniaPrototipeSpring.dto;

import lombok.Data;

@Data
public class VerifyPdfResponse {
    private String signatures;
    private boolean verifiedAt;
    private boolean isValid;
    private boolean hasWarnings;
}