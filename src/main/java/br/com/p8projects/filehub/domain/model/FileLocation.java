package br.com.p8projects.filehub.domain.model;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class FileLocation {

    private String path;
    private String filename;


    public FileLocation(String path) {
        this.path = path;
    }


    public void setFilename(String filename) {
        if(this.filename == null && (filename != null && !filename.isEmpty())) {
            this.filename = filename;
        }
    }

}
