package br.com.mpps.filehub.infrastructure.controller;

import br.com.mpps.filehub.domain.model.EnumSynchronizationDirection;
import br.com.mpps.filehub.domain.model.config.Schema;
import br.com.mpps.filehub.domain.model.config.Storage;
import br.com.mpps.filehub.domain.usecase.SynchronizationService;
import br.com.mpps.filehub.infrastructure.config.StorageReader;
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

    @PostMapping(value = "/synch/storage/{right}/{left}")
    public ResponseEntity<UUID> synchronizeSchema(@PathVariable("right") String rightId,
                                                  @PathVariable("left") String leftId) {
        Storage right = StorageReader.getStorage(rightId);
        Storage left = StorageReader.getStorage(leftId);
        UUID synchId = synchronizationService.start(right, left, EnumSynchronizationDirection.BOTH);
        return ResponseEntity.ok(synchId);
    }

    @PostMapping(value = "/synch/storage/{right}/{left}/{direction}")
    public ResponseEntity<UUID> synchronizeSchemaOneDirection(@PathVariable("right") String rightId,
                                                              @PathVariable("left") String leftId,
                                                              @PathVariable("direction") String direction) {
        Storage right = StorageReader.getStorage(rightId);
        Storage left = StorageReader.getStorage(leftId);
        EnumSynchronizationDirection synchDirection = EnumSynchronizationDirection.getFromString(direction);
        UUID synchId = synchronizationService.start(right, left, synchDirection);
        return ResponseEntity.ok(synchId);
    }

    @PostMapping(value = "/synch/schema/{schema}")
    public ResponseEntity<UUID> synchronizeSchemaOneDirection(@PathVariable("schema") String schema) {
        Schema target = StorageReader.getStoragesBySchema(schema);
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
