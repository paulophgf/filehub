package br.com.mpps.filehub.infrastructure.controller;

import br.com.mpps.filehub.domain.model.Base64Upload;
import br.com.mpps.filehub.domain.model.config.Schema;
import br.com.mpps.filehub.domain.usecase.TriggerAuthenticationService;
import br.com.mpps.filehub.domain.usecase.FileManager;
import br.com.mpps.filehub.infrastructure.config.StorageReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.FileNotFoundException;
import java.io.IOException;

@RestController
public class FileController {

    private final FileManager fileManager;
    private final TriggerAuthenticationService triggerAuthenticationService;


    @Autowired
    public FileController(FileManager fileManager, TriggerAuthenticationService triggerAuthenticationService) {
        this.fileManager = fileManager;
        this.triggerAuthenticationService = triggerAuthenticationService;
    }


    @PostMapping(value = "/schema/{schema}/upload")
    public ResponseEntity<String> uploadMultipartFiles(HttpServletRequest request,
                                      @PathVariable("schema") String schemaId,
                                      @RequestParam("files") MultipartFile[] files,
                                      @RequestParam("path") String originalPath,
                                      @RequestParam(value = "mkdir", required = false, defaultValue = "false") Boolean mkdir,
                                      @RequestParam(value = "parallel", required = false, defaultValue = "false") Boolean parallel) {
        Schema schema = StorageReader.getStoragesBySchema(schemaId);
        String path = triggerAuthenticationService.getPath(request, schema, originalPath, false);
        fileManager.upload(schema, path, files, mkdir, parallel);
        return ResponseEntity.ok().build();
    }

    @PostMapping(value = "/schema/{schema}/upload64")
    public ResponseEntity<String> uploadBase64Files(HttpServletRequest request,
                                @RequestBody Base64Upload[] files,
                                @PathVariable("schema") String schemaId,
                                @RequestParam("path") String originalPath,
                                @RequestParam(value = "mkdir", required = false, defaultValue = "false") Boolean mkdir) {
        Schema schema = StorageReader.getStoragesBySchema(schemaId);
        String path = triggerAuthenticationService.getPath(request, schema, originalPath, false);
        fileManager.uploadBase64(schema, path, files, mkdir);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping(value = "/schema/{schema}/file")
    public ResponseEntity<Boolean> delete(HttpServletRequest request,
                                          @PathVariable("schema") String schemaId,
                                          @RequestParam("path") String originalPath) {
        Schema schema = StorageReader.getStoragesBySchema(schemaId);
        String path = triggerAuthenticationService.getPath(request, schema, originalPath, false);
        Boolean result = fileManager.delete(schema, path);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/schema/{schema}/file/**")
    public void downloadFile(@PathVariable("schema") String schemaId,
                         HttpServletRequest request, HttpServletResponse response) throws IOException {
        Schema schema = StorageReader.getStoragesBySchema(schemaId);
        String originalPath = request.getRequestURI().replace("/schema/" + schemaId + "/file/", "");
        String path = triggerAuthenticationService.getPath(request, schema, originalPath, false);
        if(!fileManager.existsFile(schema, path)) {
            throw new FileNotFoundException();
        }
        String contentType = fileManager.getContentType(schema, path);
        byte[] byteArray = fileManager.downloadFile(schema, path);
        response.setContentType(contentType);
        response.getOutputStream().write(byteArray);
    }

}
