package br.com.p8projects.filehub.domain.model.storage;

import br.com.p8projects.filehub.domain.model.config.FhStorage;
import br.com.p8projects.filehub.domain.model.storage.dropbox.DropboxProperties;
import br.com.p8projects.filehub.domain.model.storage.filesystem.FileSystemProperties;
import br.com.p8projects.filehub.domain.model.storage.google.GoogleCloudProperties;
import br.com.p8projects.filehub.domain.model.storage.s3.S3Properties;
import br.com.p8projects.filehub.domain.exceptions.PropertiesReaderException;
import br.com.p8projects.filehub.domain.usecase.storage.DropboxStorage;
import br.com.p8projects.filehub.domain.usecase.storage.FileSystemStorage;
import br.com.p8projects.filehub.domain.usecase.storage.GoogleCloudStorage;
import br.com.p8projects.filehub.domain.usecase.storage.S3Storage;
import lombok.Getter;

@Getter
public enum EnumStorageType {

    FILE_SYSTEM(FileSystemProperties.class) {

        @Override
        public FhStorage getStorage(String id, StorageProperties properties) {
            return new FileSystemStorage(id, (FileSystemProperties) properties);
        }

    },
    AWS_S3(S3Properties.class) {

        @Override
        public FhStorage getStorage(String id, StorageProperties properties) {
            return new S3Storage(id, (S3Properties) properties);
        }

    },
    GOOGLE_CLOUD(GoogleCloudProperties.class) {

        @Override
        public FhStorage getStorage(String id, StorageProperties properties) {
            return new GoogleCloudStorage(id, (GoogleCloudProperties) properties);
        }

    },
    DROPBOX(DropboxProperties.class) {

        @Override
        public FhStorage getStorage(String id, StorageProperties properties) {
            return new DropboxStorage(id, (DropboxProperties) properties);
        }

    };


    private Class<? extends StorageProperties> propertiesClass;


    EnumStorageType(Class<? extends StorageProperties> propertiesClass) {
        this.propertiesClass = propertiesClass;
    }

    public static EnumStorageType get(String storageType) {
        EnumStorageType type;
        try {
            type = EnumStorageType.valueOf(storageType);
        } catch (IllegalArgumentException e) {
            throw new PropertiesReaderException("Storage type " + storageType + " not exists");
        }
        return type;
    }

    public abstract FhStorage getStorage(String id, StorageProperties properties);

}
