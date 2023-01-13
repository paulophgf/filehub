package br.com.p8projects.filehub.domain.model;

import lombok.Data;

import java.io.InputStream;

@Data
public class TransferFileObject {

    private String path;
    private String filename;
    private long lenght;
    private String contentType;
    private InputStream inputStream;

}
