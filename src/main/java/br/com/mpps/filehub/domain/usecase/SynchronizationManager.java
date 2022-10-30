package br.com.mpps.filehub.domain.usecase;

import br.com.mpps.filehub.domain.model.EnumSynchronizationDirection;
import br.com.mpps.filehub.domain.model.config.Schema;
import br.com.mpps.filehub.domain.model.config.Storage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class SynchronizationManager {

    private SynchronizationProgressLocal synchronizationProgress;

    @Autowired
    public SynchronizationManager(SynchronizationProgressLocal synchronizationProgress) {
        this.synchronizationProgress = synchronizationProgress;
    }


    @Async
    public UUID synchronize(Storage right, Storage left, EnumSynchronizationDirection direction) {
        //TODO
        return UUID.randomUUID();
    }

    @Async
    public UUID synchronize(Schema target) {
        //TODO
        return UUID.randomUUID();
    }

}
