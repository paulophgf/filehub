package br.com.p8projects.filehub.domain.model;

import br.com.p8projects.filehub.domain.model.upload.UploadObject;
import com.google.gson.Gson;
import lombok.Data;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

@Data
public class TriggerRequestBody implements Serializable {

    private String schema;
    private String operation;
    private String path;
    private List<String> filenames;

    public TriggerRequestBody(UploadObject uploadObject, EnumFileHubOperation fileHubOperation) {
        this.schema = uploadObject.getSchema().getId();
        this.operation = fileHubOperation.name();
        this.path = uploadObject.getPath();
        this.filenames = uploadObject.listFilenames();
    }

    public TriggerRequestBody(FileLocation fileLocation, EnumFileHubOperation fileHubOperation) {
        this.schema = fileLocation.getSchema().getId();
        this.operation = fileHubOperation.name();
        this.path = fileLocation.getPath();
        if(fileLocation.getFilename() != null) {
            this.filenames = Collections.singletonList(fileLocation.getFilename());
        }
    }

    public String getBody() {
        return new Gson().toJson(this);
    }

}
