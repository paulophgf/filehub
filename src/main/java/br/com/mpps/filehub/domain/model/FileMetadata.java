package br.com.mpps.filehub.domain.model;

import lombok.Data;

import java.text.SimpleDateFormat;
import java.util.Date;

@Data
public class FileMetadata {

    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-hh HH:mm:ss");

    private String contentType;
    private Long size;
    private String lastModified;


    public void setLastModified(Date date) {
        this.lastModified = sdf.format(date);
    }

    public void setLastModified(long milliseconds) {
        this.lastModified = sdf.format(new Date(milliseconds));
    }

}
