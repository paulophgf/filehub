package br.com.p8projects.filehub.domain.usecase;

import br.com.p8projects.filehub.domain.exceptions.NotFoundException;
import br.com.p8projects.filehub.domain.model.StorageSynchronize;
import br.com.p8projects.filehub.domain.model.config.Schema;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class SynchronizationService {

    private SynchronizationProgressLocal synchronizationProgress;
    private SynchronizationManager synchronizationManager;


    @Autowired
    public SynchronizationService(SynchronizationProgressLocal synchronizationProgress,
                                  SynchronizationManager synchronizationManager) {
        this.synchronizationProgress = synchronizationProgress;
        this.synchronizationManager = synchronizationManager;
    }


    public UUID start(StorageSynchronize storageSynchronize) {
        UUID key = synchronizationProgress.start(storageSynchronize);
        synchronizationManager.synchronize(storageSynchronize);
        return key;
    }

    public UUID start(Schema target) {
        UUID key = synchronizationProgress.start(target);
        synchronizationManager.synchronize(target);
        return key;
    }

    public Float getStatus(String synchId) {
        UUID key = UUID.fromString(synchId);
        Float status = synchronizationProgress.getStatus(key);
        if(status == null) {
            throw new NotFoundException("Synchronization not found");
        }
        return synchronizationProgress.getStatus(key);
    }

    public void cancel(String synchId) {
        UUID key = UUID.fromString(synchId);
        if(!synchronizationProgress.cancel(key)) {
            throw new NotFoundException("Synchronization not found");
        }
    }

}
