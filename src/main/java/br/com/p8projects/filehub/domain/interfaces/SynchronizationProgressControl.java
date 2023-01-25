package br.com.p8projects.filehub.domain.interfaces;

import br.com.p8projects.filehub.domain.model.StorageSynchronize;
import br.com.p8projects.filehub.domain.model.config.Schema;

import java.util.UUID;

public interface SynchronizationProgressControl {

    UUID start(Schema schema);

    UUID start(StorageSynchronize storageSynchronize);

    boolean exists(Schema schema);

    boolean exists(StorageSynchronize storageSynchronize);

    void updateStatus(UUID key, Float value);

    Float getStatus(UUID key);

    boolean cancel(UUID key);

}
