// Declaración del paquete que contiene la clase
package cu.xetid.cav.pdfsigner.exception_handlers;

// Importación de clases necesarias
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

// Anotación para habilitar el logging
@Slf4j
// Anotación para manejar excepciones a nivel global de los controladores REST
@RestControllerAdvice
public class ControllerExceptionHandler {

  // Manejador de excepciones para validación de argumentos de método
  @ExceptionHandler(MethodArgumentNotValidException.class)
  // Define el código de estado HTTP que se devolverá
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public Map<?, ?> handleValidationException(MethodArgumentNotValidException ex) {
    // Obtiene y formatea los errores de validación
    var errors = ex
      .getBindingResult()
      .getAllErrors()
      .stream()
      .map(error ->
        String.format(
          // Formatea el mensaje de error dependiendo si es una opción u otro campo
          error.getObjectName().equals("options") ? "options.%s %s" : "%s %s",
          ((FieldError) error).getField(),
          error.getDefaultMessage()
        )
      )
      .collect(Collectors.toList());
    // Registra el error en el log
    log.error("bad request error: " + ex.getMessage());

    // Retorna un mapa con la información del error
    return Map.ofEntries(
      Map.entry("statusCode", HttpStatus.BAD_REQUEST.value()),
      Map.entry("message", HttpStatus.BAD_REQUEST.getReasonPhrase()),
      Map.entry("error", errors)
    );
  }

  // Manejador global para cualquier otra excepción no manejada
  @ExceptionHandler(Exception.class)
  // Define el código de estado HTTP 500 para errores internos
  @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
  public Map<?, ?> globalExceptionHandler(Exception ex) {
    // Registra el error en el log
    log.error("internal server error: " + ex.getMessage());
    // Retorna un mapa con la información del error
    return Map.ofEntries(
      Map.entry("statusCode", HttpStatus.INTERNAL_SERVER_ERROR.value()),
      Map.entry("message", HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase())
      // Map.entry("error", ex.getMessage())
    );
  }
}