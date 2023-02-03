package br.com.p8projects.filehub.domain.usecase;

import br.com.p8projects.filehub.domain.interfaces.SynchronizationProgressControl;
import br.com.p8projects.filehub.domain.model.StorageSynchronize;
import br.com.p8projects.filehub.domain.model.config.FhStorage;
import br.com.p8projects.filehub.domain.model.config.Schema;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class SynchronizationProgressLocal implements SynchronizationProgressControl {

    private final static Map<UUID, Float> percentageControl = new HashMap<>();
    private final static Set<String> executionControl = new HashSet<>();
    private final static Set<UUID> cancellationList = new HashSet<>();


    @Override
    public UUID start(Schema schema) {
        UUID key = UUID.randomUUID();
        schema.getStorages().forEach(s -> executionControl.add(s.getId()));
        percentageControl.put(key, 0f);
        return key;
    }

    @Override
    public UUID start(StorageSynchronize storageSynchronize) {
        UUID key = UUID.randomUUID();
        executionControl.add(storageSynchronize.getSource().getId());
        executionControl.add(storageSynchronize.getDestination().getId());
        percentageControl.put(key, 0f);
        return key;
    }


    @Override
    public boolean existExecutingStorage(Schema schema) {
        boolean result = false;
        for(FhStorage storage : schema.getStorages()) {
            result = executionControl.contains(storage.getId());
            if(result) {
                break;
            }
        }
        return result;
    }

    @Override
    public boolean existExecutingStorage(StorageSynchronize storageSynchronize) {
        boolean existsSource = executionControl.contains(storageSynchronize.getSource().getId());
        boolean existsDestination = executionControl.contains(storageSynchronize.getDestination().getId());
        return existsSource || existsDestination;
    }


    @Override
    public void updateStatus(UUID key, Float value) {
        percentageControl.put(key, value);
    }

    @Override
    public Float getStatus(UUID key) {
        return percentageControl.get(key);
    }

    @Override
    public boolean cancel(UUID key) {
        boolean existsKey = percentageControl.containsKey(key);
        if(existsKey) {
            cancellationList.add(key);
        }
        return existsKey;
    }

}
