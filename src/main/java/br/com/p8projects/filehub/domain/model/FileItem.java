package br.com.p8projects.filehub.domain.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@ApiModel(description = "File Item")
@Data
public class FileItem {

    @ApiModelProperty(
            notes = "Path of file or directory",
            example = "/account/user/photos"
    )
    private String path;

    @ApiModelProperty(
            notes = "Name of file or directory",
            example = "MyImage.jpeg"
    )
    private String name;

    @ApiModelProperty(
            notes = "TRUE: It is a directory\nFALSE: It is a file"
    )
    private Boolean isDirectory;

    @ApiModelProperty(
            notes = "Item type (based on file extension/content-type).\n" +
                    "When it is a directory the value will be \"dir\"",
            example = "image/jpeg"
    )
    private String type;

    @ApiModelProperty(
            notes = "File size in bytes",
            example = "89866"
    )
    private Long size;


    public FileItem(String path, String name, Boolean isDirectory, String type, Long size) {
        this.path = path;
        this.name = name;
        this.isDirectory = isDirectory;
        this.type = isDirectory ? "dir" : type;
        this.size = size;
    }

}
