package br.com.mpps.filehub.domain.model.config;

import br.com.mpps.filehub.domain.model.storage.EnumStorageType;
import br.com.mpps.filehub.domain.model.storage.StorageOperations;
import br.com.mpps.filehub.domain.model.storage.StorageProperties;
import lombok.Getter;
import lombok.Setter;

import java.util.Objects;

@Getter
@Setter
public abstract class Storage<T extends StorageProperties> implements StorageOperations {

    protected String id;
    private EnumStorageType type;
    protected T properties;
    private String autoSchema;

    public Storage(String id, EnumStorageType type, T properties) {
        this.id = id;
        this.type = type;
        this.properties = properties;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Storage storage = (Storage) o;
        return id.equals(storage.id) && type == storage.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, type);
    }

}
