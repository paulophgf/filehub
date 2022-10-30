package br.com.mpps.filehub.domain.model.config;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Collection;
import java.util.Objects;

@Getter
@Setter
@NoArgsConstructor
public class Schema {

    private String id;
    private Trigger trigger;
    private Collection<Storage> storages;
    private boolean isStorage;


    public Schema(String id, Collection<Storage> storages, boolean isStorage) {
        this.id = id;
        this.storages = storages;
        this.isStorage = isStorage;
    }

    public Schema(String id, Trigger trigger, Collection<Storage> storages) {
        this.id = id;
        this.trigger = trigger;
        this.storages = storages;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Schema schema = (Schema) o;
        return id.equals(schema.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

}
