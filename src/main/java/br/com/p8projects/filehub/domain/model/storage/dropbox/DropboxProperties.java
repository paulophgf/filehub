package br.com.p8projects.filehub.domain.model.storage.dropbox;

import br.com.p8projects.filehub.domain.model.OptionalProperty;
import br.com.p8projects.filehub.domain.model.storage.EnumStorageType;
import br.com.p8projects.filehub.domain.model.storage.StorageProperties;
import lombok.Getter;
import lombok.Setter;

import java.util.Objects;

@Getter
@Setter
public class DropboxProperties implements StorageProperties, Cloneable {

    private String accessToken;
    @OptionalProperty
    private String baseDir;

    @Override
    public EnumStorageType getType() {
        return EnumStorageType.DROPBOX;
    }

    @Override
    public void afterReadProperties(String storageName) {
        // No implementation is necessary
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DropboxProperties that = (DropboxProperties) o;
        return accessToken.equals(that.accessToken) && Objects.equals(baseDir, that.baseDir);
    }

    @Override
    public int hashCode() {
        return Objects.hash(accessToken, baseDir);
    }


    @Override
    public DropboxProperties clone() {
        try {
            return (DropboxProperties) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }

}
