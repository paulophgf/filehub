package br.com.p8projects.filehub.domain.model;

import br.com.p8projects.filehub.domain.model.config.FhStorage;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter(AccessLevel.PROTECTED)
public class StorageSynchronize {

    private FhStorage source;
    private FhStorage destination;
    private boolean isFullSynch;

    private StorageSynchronize(FhStorage source, FhStorage destination) {
        this.source = source;
        this.destination = destination;
        this.isFullSynch = false;
    }

    public static StorageSynchronize sourceToDestination(FhStorage source, FhStorage destination) {
        return new StorageSynchronize(source, destination);
    }

    public static StorageSynchronize fullSynchrnozation(FhStorage storage1, FhStorage storage2) {
        StorageSynchronize storageSynchronize = new StorageSynchronize(storage1, storage2);
        storageSynchronize.setFullSynch(true);
        return storageSynchronize;
    }

}
