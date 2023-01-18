package br.com.p8projects.filehub.domain.model.upload;

import br.com.p8projects.filehub.domain.model.config.Schema;
import lombok.Data;

import java.util.List;

@Data
public abstract class UploadObject {

    protected Schema schema;
    protected String path;
    protected boolean mkdir;

    public abstract void setFilename(String filename);

    public abstract List<String> listFilenames();

}
