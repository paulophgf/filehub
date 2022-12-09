package br.com.p8projects.filehub.domain.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.text.SimpleDateFormat;
import java.util.Date;

@ApiModel(description = "File Details")
@Data
public class FileMetadata {

    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-hh HH:mm:ss");

    @ApiModelProperty(
            notes = "Content type (based on file extension)",
            example = "image/jpeg"
    )
    private String contentType;

    @ApiModelProperty(
            notes = "File size in bytes",
            example = "89866"
    )
    private Long size;

    @ApiModelProperty(
            notes = "Last file update time (Format: yyyy-MM-hh HH:mm:ss)",
            example = "2022-10-12 14:35:19"
    )
    private String lastModified;


    public void setLastModified(Date date) {
        this.lastModified = sdf.format(date);
    }

    public void setLastModified(long milliseconds) {
        this.lastModified = sdf.format(new Date(milliseconds));
    }

}
