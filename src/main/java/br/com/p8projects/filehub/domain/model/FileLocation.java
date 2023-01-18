package br.com.p8projects.filehub.domain.model;

import br.com.p8projects.filehub.domain.model.config.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class FileLocation {

    private Schema schema;
    private String path;
    private String filename;


    public FileLocation(Schema schema, String path) {
        this.schema = schema;
        this.path = path;
    }

    public FileLocation(Schema schema, String path, String filename) {
        this.schema = schema;
        this.path = path;
        setFilename(filename);
    }


    public void setFilename(String filename) {
        if(this.filename == null && (filename != null && !filename.isEmpty())) {
            this.filename = filename;
        }
    }

}
