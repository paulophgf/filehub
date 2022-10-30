package br.com.mpps.filehub.domain.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FileItem {

    private String path;
    private String name;
    private Boolean isDirectory;
    private String type;
    private Long size;


    public FileItem(String path, String name, Boolean isDirectory, String type, Long size) {
        this.path = path;
        this.name = name;
        this.isDirectory = isDirectory;
        this.type = isDirectory ? "dir" : type;
        this.size = size;
    }

}
