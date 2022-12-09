package br.com.p8projects.filehub.domain.model.storage.filesystem;

import br.com.p8projects.filehub.domain.model.storage.EnumStorageType;
import br.com.p8projects.filehub.domain.model.storage.StorageProperties;
import lombok.Getter;
import lombok.Setter;

import java.util.Objects;

@Getter
@Setter
public class FileSystemProperties implements StorageProperties, Cloneable {

    private String baseDir;

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
            return (FileSystemProperties) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }

}
