package com.PrototipoManageService.PetuniaPrototipeSpring.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "authorities")
@Data
public class Authority {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Lob
    @Column(nullable = false)
    private byte[] file; // Certificado en binario

    private String subject;
    private String issuer;
    private LocalDateTime expirationDate;

    // Campo opcional para Custom AC (autoridades personalizadas)
    private String userReferenceId;
}
