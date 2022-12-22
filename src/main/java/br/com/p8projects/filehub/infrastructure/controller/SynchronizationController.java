package br.com.p8projects.filehub.infrastructure.controller;

import br.com.p8projects.filehub.domain.model.EnumSynchronizationDirection;
import br.com.p8projects.filehub.domain.model.config.Schema;
import br.com.p8projects.filehub.domain.model.config.FhStorage;
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

    @PostMapping(value = "/synch/storage/{right}/{left}")
    public ResponseEntity<UUID> synchronizeSchema(@PathVariable("right") String rightId,
                                                  @PathVariable("left") String leftId) {
        FhStorage right = StorageResourceReader.getStorage(rightId);
        FhStorage left = StorageResourceReader.getStorage(leftId);
        UUID synchId = synchronizationService.start(right, left, EnumSynchronizationDirection.BOTH);
        return ResponseEntity.ok(synchId);
    }

    @PostMapping(value = "/synch/storage/{right}/{left}/{direction}")
    public ResponseEntity<UUID> synchronizeSchemaOneDirection(@PathVariable("right") String rightId,
                                                              @PathVariable("left") String leftId,
                                                              @PathVariable("direction") String direction) {
        FhStorage right = StorageResourceReader.getStorage(rightId);
        FhStorage left = StorageResourceReader.getStorage(leftId);
        EnumSynchronizationDirection synchDirection = EnumSynchronizationDirection.getFromString(direction);
        UUID synchId = synchronizationService.start(right, left, synchDirection);
        return ResponseEntity.ok(synchId);
    }

    @PostMapping(value = "/synch/schema/{schema}")
    public ResponseEntity<UUID> synchronizeSchemaOneDirection(@PathVariable("schema") String schema) {
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
