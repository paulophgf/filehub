package br.com.p8projects.filehub.domain.model.upload;

import br.com.p8projects.filehub.domain.model.config.Schema;
import lombok.Data;

import java.util.Arrays;
import java.util.List;

@Data
public class UploadBase64Object extends UploadObject {
    private List<Base64Upload> files;


    public UploadBase64Object(Schema schema, String path, Base64Upload[] files, boolean mkdir) {
        this.schema = schema;
        this.path = path;
        this.files = Arrays.asList(files);
        this.mkdir = mkdir;
    }

    @Override
    public void setFilename(String filename) {
        if(files.size() == 1 && filename != null && !filename.isEmpty()) {
            this.files.get(0).setFilename(filename);
        }
    }

    @Override
    public List<String> listFilenames() {
        return files.stream().map(Base64Upload::getFilename).toList();
    }

}
