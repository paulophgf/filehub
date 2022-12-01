package br.com.mpps.filehub.system;

import br.com.mpps.filehub.domain.exceptions.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@ControllerAdvice
public class ResponseExceptionHandler {

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<String> entityNotFound(NotFoundException e) {
        ResponseEntity response;
        if(e.getMessage() != null) {
            log.info(e.getMessage());
            response = ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } else {
            response = ResponseEntity.notFound().build();
        }
        return response;
    }

    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ExceptionHandler(TriggerAuthenticationException.class)
    public ResponseEntity<String> triggerAuthenticationException(TriggerAuthenticationException e) {
        log.warn(e.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(UploadException.class)
    public ResponseEntity<String> uploadException(UploadException e) {
        log.error(e.getMessage(), e.getCause());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(DownloadException.class)
    public ResponseEntity<String> downloadException(DownloadException e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(StorageException.class)
    public ResponseEntity<String> storageException(StorageException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<String> runtimeInternalError(RuntimeException e) {
        log.error(e.getMessage(), e.getCause());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
    }

}
