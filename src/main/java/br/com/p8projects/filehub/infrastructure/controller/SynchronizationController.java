package br.com.p8projects.filehub.infrastructure.controller;

import br.com.p8projects.filehub.domain.model.StorageSynchronize;
import br.com.p8projects.filehub.domain.model.config.FhStorage;
import br.com.p8projects.filehub.domain.model.config.Schema;
import br.com.p8projects.filehub.domain.usecase.SynchronizationService;
import br.com.p8projects.filehub.infrastructure.config.StorageResourceReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import java.util.UUID;

@ApiIgnore
@RestController
public class SynchronizationController {

    private final SynchronizationService synchronizationService;


    @Autowired
    public SynchronizationController(SynchronizationService synchronizationService) {
        this.synchronizationService = synchronizationService;
    }

    @PostMapping(value = "/synch/storage/{storage1}/{storage2}")
    public ResponseEntity<UUID> synchronizeFullStorage(@PathVariable("storage1") String storage1Id,
                                                  @PathVariable("storage2") String storage2Id) {
        FhStorage storage1 = StorageResourceReader.getStorage(storage1Id);
        FhStorage storage2 = StorageResourceReader.getStorage(storage2Id);
        StorageSynchronize storageSynchronize = StorageSynchronize.fullSynchrnozation(storage1, storage2);
        UUID sychKey = synchronizationService.start(storageSynchronize);
        return ResponseEntity.ok(sychKey);
    }

    @PostMapping(value = "/synch/storage/{source}/to/{destination}")
    public ResponseEntity<UUID> synchronizeStorageWithDirection(@PathVariable("source") String sourceId,
                                                               @PathVariable("destination") String destinationId) {
        FhStorage source = StorageResourceReader.getStorage(sourceId);
        FhStorage destination = StorageResourceReader.getStorage(destinationId);
        StorageSynchronize storageSynchronize = StorageSynchronize.sourceToDestination(source, destination);
        UUID sychKey = synchronizationService.start(storageSynchronize);
        return ResponseEntity.ok(sychKey);
    }

    @PostMapping(value = "/synch/schema/{schema}")
    public ResponseEntity<UUID> synchronizeSchema(@PathVariable("schema") String schema) {
        Schema target = StorageResourceReader.getSchema(schema);
        UUID synchId = synchronizationService.start(target);
        return ResponseEntity.ok(synchId);
    }

    @GetMapping(value = "/synch/{synchId}/status")
    public ResponseEntity<Float> getStatus(@PathVariable("synchId") String synchId) {
        Float status = synchronizationService.getStatus(synchId);
        return ResponseEntity.ok(status);
    }

    @DeleteMapping(value = "/synch/{synchId}")
    public ResponseEntity cancelSynchronization(@PathVariable("synchId") String synchId) {
        synchronizationService.cancel(synchId);
        return ResponseEntity.ok().build();
    }

}
