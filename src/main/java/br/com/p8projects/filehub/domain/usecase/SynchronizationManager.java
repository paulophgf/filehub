package br.com.p8projects.filehub.domain.usecase;

import br.com.p8projects.filehub.domain.model.EnumSynchronizationDirection;
import br.com.p8projects.filehub.domain.model.config.Schema;
import br.com.p8projects.filehub.domain.model.config.FhStorage;
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
    public UUID synchronize(FhStorage right, FhStorage left, EnumSynchronizationDirection direction) {
        //TODO
        return UUID.randomUUID();
    }

    @Async
    public UUID synchronize(Schema target) {
        //TODO
        return UUID.randomUUID();
    }

}
