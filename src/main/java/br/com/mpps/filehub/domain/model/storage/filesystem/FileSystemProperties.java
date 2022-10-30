package br.com.mpps.filehub.domain.model.storage.filesystem;

import br.com.mpps.filehub.domain.model.IgnoreProperty;
import br.com.mpps.filehub.domain.model.storage.StorageProperties;
import br.com.mpps.filehub.domain.model.storage.EnumStorageType;
import lombok.Getter;
import lombok.Setter;

import java.util.Objects;

@Getter
@Setter
public class FileSystemProperties implements StorageProperties, Cloneable {

    private String baseDir;

    @IgnoreProperty
    private Boolean temporary;

    @Override
    public EnumStorageType getType() {
        return EnumStorageType.FILE_SYSTEM;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FileSystemProperties that = (FileSystemProperties) o;
        return baseDir.equals(that.baseDir);
    }

    @Override
    public int hashCode() {
        return Objects.hash(baseDir);
    }

    @Override
    public FileSystemProperties clone() {
        try {
            FileSystemProperties clone = (FileSystemProperties) super.clone();
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }

}
