package br.com.mpps.filehub.domain.model.config;

import br.com.mpps.filehub.domain.exceptions.NotFoundException;
import br.com.mpps.filehub.domain.exceptions.PropertiesReaderException;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.*;

@Getter
@Setter
@NoArgsConstructor
public class Schema {

    private String id;
    private Trigger trigger;
    private Storage middle;
    private Collection<Storage> storages;
    private boolean isTemporaryMiddle;
    private boolean isCacheEnabled;


    public Schema(String id) {
        this.id = id;
        this.storages = new LinkedList<>();
        this.isTemporaryMiddle = false;
        this.isCacheEnabled = false;
    }

    public Schema(String id, Storage storage) {
        this.id = id;
        this.storages = Collections.singletonList(storage);
        this.isTemporaryMiddle = false;
        this.isCacheEnabled = false;
    }

    public Schema(String id, Trigger trigger, Storage middle, Collection<Storage> storages, boolean isCacheEnabled) {
        this.id = id;
        this.trigger = trigger;
        this.middle = middle;
        this.storages = storages;
        this.isCacheEnabled = isCacheEnabled;
        this.isTemporaryMiddle = !storages.contains(middle);
        if(isTemporaryMiddle && isCacheEnabled) {
            throw new PropertiesReaderException("The " + id + " schema is using a temporary storage with cache.");
        }
    }

    public boolean hasTrigger() {
        return trigger != null;
    }

    public void checkIfIsAllowedDirectoryOperations() {
        if(hasTrigger() && !trigger.isAllowDirOperations()) {
            throw new NotFoundException();
        }
    }

    public Storage getFirstUsefulStorage() {
        Storage storage;
        if(middle != null) {
            if(!isTemporaryMiddle && !isCacheEnabled) {
                storage = middle;
            } else {
                Iterator<Storage> storageIterator = storages.iterator();
                storage = storageIterator.next();
                if(storage.equals(middle)) {
                    storage = storageIterator.next();
                }
            }
        } else {
            storage = storages.stream().findFirst().get();
        }
        return storage;
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
