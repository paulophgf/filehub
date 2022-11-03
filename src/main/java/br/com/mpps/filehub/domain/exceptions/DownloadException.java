package br.com.mpps.filehub.domain.exceptions;

public class DownloadException extends RuntimeException {

    public DownloadException(String message) {
        super(message);
    }

}
