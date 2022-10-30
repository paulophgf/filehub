package br.com.mpps.filehub.domain.model.storage;

import br.com.mpps.filehub.domain.model.config.Storage;
import br.com.mpps.filehub.domain.model.storage.filesystem.FileSystemProperties;
import br.com.mpps.filehub.domain.model.storage.s3.S3Properties;
import br.com.mpps.filehub.domain.exceptions.PropertiesReaderException;
import br.com.mpps.filehub.domain.model.storage.filesystem.FileSystemStorage;
import br.com.mpps.filehub.domain.model.storage.s3.S3Storage;
import lombok.Getter;

@Getter
public enum EnumStorageType {

    MIDDLE(FileSystemProperties.class) {

        @Override
        public Storage getStorage(String id, StorageProperties properties) {
            return new FileSystemStorage(id, MIDDLE, (FileSystemProperties) properties);
        }

    },
    FILE_SYSTEM(FileSystemProperties.class) {

        @Override
        public Storage getStorage(String id, StorageProperties properties) {
            return new FileSystemStorage(id, FILE_SYSTEM, (FileSystemProperties) properties);
        }

    },
    AWS_S3(S3Properties.class) {

        @Override
        public Storage getStorage(String id, StorageProperties properties) {
            return new S3Storage(id, AWS_S3, (S3Properties) properties);
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
            if(EnumStorageType.MIDDLE.equals(type)) {
                throw new IllegalArgumentException();
            }
        } catch (IllegalArgumentException e) {
            throw new PropertiesReaderException("Storage type " + storageType + " not exists");
        }
        return type;
    }

    public abstract Storage getStorage(String id, StorageProperties properties);

}
