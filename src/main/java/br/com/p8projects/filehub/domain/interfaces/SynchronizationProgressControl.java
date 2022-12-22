package br.com.p8projects.filehub.domain.interfaces;

import br.com.p8projects.filehub.domain.model.config.Schema;
import br.com.p8projects.filehub.domain.model.config.FhStorage;

import java.util.UUID;

public interface SynchronizationProgressControl {

    UUID start(Schema schema);

    UUID start(FhStorage right, FhStorage left);

    boolean exists(Schema schema);

    boolean exists(FhStorage right, FhStorage left);

    void updateStatus(UUID key, Float value);

    Float getStatus(UUID key);

    void cancel(UUID key);

}
