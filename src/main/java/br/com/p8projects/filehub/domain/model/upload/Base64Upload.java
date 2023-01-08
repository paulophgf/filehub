package br.com.p8projects.filehub.domain.model.upload;

import br.com.p8projects.filehub.domain.model.storage.Base64File;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@ApiModel(description = "Base64 Upload Data")
@Data
public class Base64Upload {

    @ApiModelProperty(
            notes = "Name used to save the file. If filename is not informed, the original filename is used."
    )
    private String filename;

    @ApiModelProperty(
            notes = "Base64 string content", required = true
    )
    private String content;

    @JsonIgnore
    public Base64File getBase64() {
        return Base64File.getInstance(filename, content);
    }

}
