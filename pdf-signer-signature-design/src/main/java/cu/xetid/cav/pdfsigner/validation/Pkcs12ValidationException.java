package cu.xetid.cav.pdfsigner.validation;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Pkcs12ValidationException extends Exception {

  public Pkcs12ValidationException() {}

  public Pkcs12ValidationException(String message) {
    super(message);
    log.error(message);
  }

  public Pkcs12ValidationException(String message, Throwable cause) {
    super(message, cause);
    log.error(message);
    log.error(cause.getMessage());
  }

  public Pkcs12ValidationException(Throwable cause) {
    super(cause);
    log.error(cause.getMessage());
  }

  public Pkcs12ValidationException(
    String message,
    Throwable cause,
    boolean enableSuppression,
    boolean writableStackTrace
  ) {
    super(message, cause, enableSuppression, writableStackTrace);
    log.error(message);
    log.error(cause.getMessage());
  }
}
