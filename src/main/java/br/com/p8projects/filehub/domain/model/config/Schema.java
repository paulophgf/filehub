package br.com.p8projects.filehub.domain.model.config;

import br.com.p8projects.filehub.domain.exceptions.NotFoundException;
import br.com.p8projects.filehub.domain.exceptions.PropertiesReaderException;
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
    private FhStorage middle;
    private Collection<FhStorage> storages;
    private boolean isTemporaryMiddle;
    private boolean isCacheEnabled;
    private boolean isParallelUpload;


    public Schema(String id) {
        this.id = id;
        this.storages = new LinkedList<>();
        this.isTemporaryMiddle = false;
        this.isCacheEnabled = false;
        this.isParallelUpload = false;
    }

    public Schema(String id, FhStorage storage) {
        this.id = id;
        this.storages = Collections.singletonList(storage);
        this.isTemporaryMiddle = false;
        this.isCacheEnabled = false;
        this.isParallelUpload = false;
    }

    public Schema(String id, Trigger trigger, FhStorage middle, Collection<FhStorage> storages,
                  boolean isCacheEnabled, boolean isParallelUpload) {
        this.id = id;
        this.trigger = trigger;
        this.middle = middle;
        this.storages = storages;
        this.isCacheEnabled = isCacheEnabled;
        this.isParallelUpload = isParallelUpload;
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

    public FhStorage getFirstUsefulStorage() {
        FhStorage storage;
        if(middle != null) {
            if(!isTemporaryMiddle && !isCacheEnabled) {
                storage = middle;
            } else {
                Iterator<FhStorage> storageIterator = storages.iterator();
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
