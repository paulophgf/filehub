package br.com.p8projects.filehub.domain.model;

import lombok.Getter;


@Getter
public enum EnumFileHubOperation {

    CREATE_DIRECTORY(false),
    RENAME_DIRECTORY(false),
    DELETE_DIRECTORY(false),
    LIST_FILES(true),
    EXIST_DIRECTORY(true),
    UPLOAD_MULTIPART_FILE(false),
    UPLOAD_BASE64_FILE(false),
    DOWNLOAD_FILE(true),
    DELETE_FILE(false),
    EXIST_FILE(true),
    GET_FILE_DETAILS(true);

    private final boolean isReadOperation;

    EnumFileHubOperation(boolean isReadOperation) {
        this.isReadOperation = isReadOperation;
    }

}
