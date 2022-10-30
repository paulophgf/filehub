package br.com.mpps.filehub.domain.exceptions;

public class UploadException extends RuntimeException {

    public UploadException(String message, Throwable cause) {
        super(message, cause);
    }

}
