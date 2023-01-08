package br.com.p8projects.filehub.domain.model.storage.google;

import br.com.p8projects.filehub.domain.model.OptionalProperty;
import br.com.p8projects.filehub.domain.model.storage.EnumStorageType;
import br.com.p8projects.filehub.domain.model.storage.StorageProperties;
import lombok.Getter;
import lombok.Setter;

import java.util.Objects;

@Getter
@Setter
public class GoogleCloudProperties implements StorageProperties {

    private String jsonCredentials;
    private String bucket;
    @OptionalProperty
    private String baseDir;

    @Override
    public EnumStorageType getType() {
        return EnumStorageType.GOOGLE_CLOUD;
    }

    @Override
    public void afterReadProperties(String storageName) {
        checkBaseDir();
    }

    private void checkBaseDir() {
        if(baseDir == null) {
            baseDir = "";
        }
        if(!"".equals(baseDir)) {
            if (baseDir.startsWith("/")) {
                baseDir = baseDir.substring(1);
            }
            if (!baseDir.endsWith("/")) {
                baseDir += "/";
            }
        }
    }

    public String formatDirPath(String path) {
        if(path.startsWith("/")) {
            path = path.substring(1);
        }
        if(!path.isEmpty() && !path.endsWith("/")) {
            path += "/";
        }
        return baseDir + path;
    }

    public String formatFilePath(String path) {
        if(path.startsWith("/")) {
            path = path.substring(1);
        }
        return baseDir + path;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GoogleCloudProperties that = (GoogleCloudProperties) o;
        return jsonCredentials.equals(that.jsonCredentials) && bucket.equals(that.bucket);
    }

    @Override
    public int hashCode() {
        return Objects.hash(jsonCredentials, bucket);
    }

}
