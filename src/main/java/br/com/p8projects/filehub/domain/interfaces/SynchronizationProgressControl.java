package br.com.p8projects.filehub.domain.interfaces;

import br.com.p8projects.filehub.domain.model.config.Schema;
import br.com.p8projects.filehub.domain.model.config.Storage;

import java.util.UUID;

public interface SynchronizationProgressControl {

    UUID start(Schema schema);

    UUID start(Storage right, Storage left);

    boolean exists(Schema schema);

    boolean exists(Storage right, Storage left);

    void updateStatus(UUID key, Float value);

    Float getStatus(UUID key);

    void cancel(UUID key);

}
