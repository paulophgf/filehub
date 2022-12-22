package br.com.p8projects.filehub.domain.usecase;

import br.com.p8projects.filehub.domain.interfaces.SynchronizationProgressControl;
import br.com.p8projects.filehub.domain.model.config.Schema;
import br.com.p8projects.filehub.domain.model.config.FhStorage;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class SynchronizationProgressLocal implements SynchronizationProgressControl {

    private static Map<UUID, Float> percentageControl = new HashMap<>();
    private static Set<String> synchControl = new HashSet<>();
    private static Set<UUID> cancellationList = new HashSet<>();


    @Override
    public UUID start(Schema schema) {
        UUID key = UUID.randomUUID();
        synchControl.add(schema.getId());
        percentageControl.put(key, 0f);
        return key;
    }

    @Override
    public UUID start(FhStorage right, FhStorage left) {
        UUID key = UUID.randomUUID();
        synchControl.add(right.getId() + left.getId());
        percentageControl.put(key, 0f);
        return key;
    }


    @Override
    public boolean exists(Schema schema) {
        return synchControl.contains(schema.getId());
    }

    @Override
    public boolean exists(FhStorage right, FhStorage left) {
        String key1 = right.getId() + left.getId();
        String key2 = left.getId() + right.getId();
        return synchControl.contains(key1) || synchControl.contains(key2);
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
    public void cancel(UUID key) {
        if(percentageControl.containsKey(key)) {
            cancellationList.add(key);
        }
    }

}
