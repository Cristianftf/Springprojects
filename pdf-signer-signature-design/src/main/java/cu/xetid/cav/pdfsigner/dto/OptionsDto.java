package cu.xetid.cav.pdfsigner.dto;

// Importaciones necesarias para validación, anotaciones y lombok
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Null;
import javax.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.lang.Nullable;

// Anotaciones de Lombok para generar getters, setters y constructor sin argumentos
@Data
@NoArgsConstructor
public class OptionsDto {

  // Contraseña del archivo PKCS12, no puede estar vacía y máximo 400 caracteres
  @NotBlank
  @Size(max = 400)
  public String pkcs12FilePassword;

  // Entidad firmante, puede ser nulo y máximo 255 caracteres
  @Size(max = 255)
  @Nullable
  public String signerEntity;

  // Cargo del firmante, puede ser nulo y máximo 255 caracteres
  @Size(max = 255)
  @Nullable
  public String signerJobTitle;

  // Ancho de la firma, puede ser nulo
  @Nullable
  public int signatureWidth;

  // Alto de la firma, puede ser nulo
  @Nullable
  public int signatureHeight;

  // Posición X de la firma en el documento, puede ser nulo
  @Nullable
  public int signaturePositionX;

  // Posición Y de la firma en el documento, puede ser nulo
  @Nullable
  public int signaturePositionY;

  // Descripción de la firma, puede ser nulo
  @Nullable
  public String signatureDescription;

  // Tamaño de la imagen de la firma, puede ser nulo
  @Nullable
  public int signatureImageSize;

  // Posición X relativa de la imagen de la firma, puede ser nulo
  @Nullable
  public int signatureImageRelativePositionX;

  // Posición Y relativa de la imagen de la firma, puede ser nulo
  @Nullable
  public int signatureImageRelativePositionY;

  // Posición X relativa de la descripción de la firma, puede ser nulo
  @Nullable
  public int signatureDescriptionRelativePositionX;

  // Posición Y relativa de la descripción de la firma, puede ser nulo
  @Nullable
  public int signatureDescriptionRelativePositionY;

  // Tamaño de la fuente de la firma, puede ser nulo
  @Nullable
  public int signatureFontSize;
}