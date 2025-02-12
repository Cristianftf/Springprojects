// Package declaration for the DTO classes
package com.PrototipoManageService.PetuniaPrototipeSpring.dto;

// Import Lombok annotation for automatic getter, setter, equals, hashCode and toString methods
import lombok.Data;

// Lombok annotation to generate boilerplate code
@Data
public class VerifyPdfResponse {
    // Stores the signature information from the PDF
    private String signatures;
    // Indicates when the PDF was verified
    private boolean verifiedAt;
    // Indicates if the PDF is valid
    private boolean isValid;
    // Indicates if the PDF has any warnings
    private boolean hasWarnings;
}