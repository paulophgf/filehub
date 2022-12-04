package br.com.mpps.filehub.domain.model.config;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;
import java.util.Objects;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StorageResource {

    private Map<String, Storage> storages;
    private Map<String, Trigger> triggers;
    private Map<String, Schema> schemas;


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StorageResource that = (StorageResource) o;
        return Objects.equals(storages, that.storages) && Objects.equals(triggers, that.triggers) && Objects.equals(schemas, that.schemas);
    }

    @Override
    public int hashCode() {
        return Objects.hash(storages, triggers, schemas);
    }

}
