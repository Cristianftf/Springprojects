package com.PrototipoManageService.PetuniaPrototipeSpring.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "digital_identities")
@Data
public class DigitalIdentity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String userReferenceId;

    @Column(nullable = false)
    private String encryptedPassword;

    @Column(nullable = false)
    private LocalDateTime expirationDate;

    @Lob
    @Column(nullable = false)
    private byte[] pkcs12File; // Archivo en formato binario

}
