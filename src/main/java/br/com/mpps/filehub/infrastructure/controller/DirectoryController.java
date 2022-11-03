package br.com.mpps.filehub.infrastructure.controller;

import br.com.mpps.filehub.domain.model.FileItem;
import br.com.mpps.filehub.domain.model.config.Schema;
import br.com.mpps.filehub.domain.usecase.DirectoryManager;
import br.com.mpps.filehub.domain.usecase.TriggerAuthenticationService;
import br.com.mpps.filehub.infrastructure.config.StorageReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
public class DirectoryController {

    private final DirectoryManager directoryManager;
    private final TriggerAuthenticationService triggerAuthenticationService;


    @Autowired
    public DirectoryController(DirectoryManager directoryManager, TriggerAuthenticationService triggerAuthenticationService) {
        this.directoryManager = directoryManager;
        this.triggerAuthenticationService = triggerAuthenticationService;
    }


    @PostMapping(value = "/schema/{schema}/dir")
    public ResponseEntity<Boolean> createDirectory(HttpServletRequest request,
                                                   @PathVariable("schema") String schemaId,
                                                   @RequestParam("path") String originalPath) {
        Schema schema = StorageReader.getStoragesBySchema(schemaId);
        String path = triggerAuthenticationService.getPath(request, schema, originalPath, false);
        Boolean result = directoryManager.createDirectory(schema, path);
        return ResponseEntity.ok(result);
    }

    @DeleteMapping(value = "/schema/{schema}/dir")
    public ResponseEntity<Boolean> delete(HttpServletRequest request,
                                          @PathVariable("schema") String schemaId,
                                          @RequestParam("path") String originalPath,
                                          @RequestParam(value = "recursively", required = false, defaultValue = "false") Boolean recursively) {
        Schema schema = StorageReader.getStoragesBySchema(schemaId);
        String path = triggerAuthenticationService.getPath(request, schema, originalPath, false);
        Boolean result = directoryManager.deleteDirectory(schema, path, recursively);
        return ResponseEntity.ok(result);
    }

    @GetMapping(value = "/schema/{schema}/dir")
    public ResponseEntity<List<FileItem>> listFiles(HttpServletRequest request,
                                                    @PathVariable("schema") String schemaId,
                                                    @RequestParam("path") String originalPath) {
        Schema schema = StorageReader.getStoragesBySchema(schemaId);
        String path = triggerAuthenticationService.getPath(request, schema, originalPath, false);
        List<FileItem> result = directoryManager.listFiles(schema, path);
        return ResponseEntity.ok(result);
    }

    @GetMapping(value = "/schema/{schema}/dir/exists")
    public ResponseEntity exists(HttpServletRequest request,
                                 @PathVariable("schema") String schemaId,
                                 @RequestParam("path") String originalPath) {
        Schema schema = StorageReader.getStoragesBySchema(schemaId);
        String path = triggerAuthenticationService.getPath(request, schema, originalPath, false);
        return directoryManager.existsDirectory(schema, path) ? ResponseEntity.ok().build() : ResponseEntity.notFound().build();
    }

}
