package br.com.mpps.filehub.domain.model;

import br.com.mpps.filehub.domain.model.storage.Base64File;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Base64Upload {

    private String filename;
    private String content;

    public Base64File getBase64() {
        return Base64File.getInstance(filename, content);
    }

}
