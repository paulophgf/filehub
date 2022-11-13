package br.com.mpps.filehub.infrastructure.controller;

import br.com.mpps.filehub.domain.exceptions.NotFoundException;
import br.com.mpps.filehub.domain.model.Base64Upload;
import br.com.mpps.filehub.domain.model.FileMetadata;
import br.com.mpps.filehub.domain.model.FileLocation;
import br.com.mpps.filehub.domain.model.config.Schema;
import br.com.mpps.filehub.domain.usecase.FileManager;
import br.com.mpps.filehub.domain.usecase.TriggerAuthenticationService;
import br.com.mpps.filehub.infrastructure.config.StorageReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;

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
    public ResponseEntity<String> uploadMultipartFile(HttpServletRequest request,
                                                       @PathVariable("schema") String schemaId,
                                                       @RequestParam("file") MultipartFile file,
                                                       @RequestParam("path") String originalPath,
                                                       @RequestParam(value = "filename", required = false) String filename,
                                                       @RequestParam(value = "mkdir", required = false, defaultValue = "false") Boolean mkdir) {
        Schema schema = StorageReader.getStoragesBySchema(schemaId);
        FileLocation fileLocation = triggerAuthenticationService.getFileLocation(request, schema, originalPath, false);
        fileLocation.setFilename(filename).setFilename(file.getOriginalFilename());
        fileManager.upload(schema, fileLocation, file, mkdir);
        return ResponseEntity.ok().build();
    }

    @PostMapping(value = "/schema/{schema}/upload/multi")
    public ResponseEntity<String> uploadMultipartFiles(HttpServletRequest request,
                                      @PathVariable("schema") String schemaId,
                                      @RequestParam("files") MultipartFile[] files,
                                      @RequestParam("path") String originalPath,
                                      @RequestParam(value = "mkdir", required = false, defaultValue = "false") Boolean mkdir,
                                      @RequestParam(value = "parallel", required = false, defaultValue = "false") Boolean parallel) {
        Schema schema = StorageReader.getStoragesBySchema(schemaId);
        FileLocation fileLocation = triggerAuthenticationService.getFileLocation(request, schema, originalPath, false);
        fileManager.upload(schema, fileLocation.getPath(), files, mkdir, parallel);
        return ResponseEntity.ok().build();
    }

    @PostMapping(value = "/schema/{schema}/upload64")
    public ResponseEntity<String> uploadBase64File(HttpServletRequest request,
                                                    @RequestBody Base64Upload file,
                                                    @PathVariable("schema") String schemaId,
                                                    @RequestParam("path") String originalPath,
                                                    @RequestParam(value = "filename", required = false) String filename,
                                                    @RequestParam(value = "mkdir", required = false, defaultValue = "false") Boolean mkdir) {
        Schema schema = StorageReader.getStoragesBySchema(schemaId);
        FileLocation fileLocation = triggerAuthenticationService.getFileLocation(request, schema, originalPath, false);
        fileManager.uploadBase64(schema, fileLocation, file, mkdir);
        fileLocation.setFilename(filename);
        return ResponseEntity.ok().build();
    }

    @PostMapping(value = "/schema/{schema}/upload64/multi")
    public ResponseEntity<String> uploadBase64Files(HttpServletRequest request,
                                @RequestBody Base64Upload[] files,
                                @PathVariable("schema") String schemaId,
                                @RequestParam("path") String originalPath,
                                @RequestParam(value = "mkdir", required = false, defaultValue = "false") Boolean mkdir) {
        Schema schema = StorageReader.getStoragesBySchema(schemaId);
        FileLocation fileLocation = triggerAuthenticationService.getFileLocation(request, schema, originalPath, false);
        fileManager.uploadBase64(schema, fileLocation.getPath(), files, mkdir);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping(value = "/schema/{schema}/file")
    public ResponseEntity delete(HttpServletRequest request,
                                 @PathVariable("schema") String schemaId,
                                 @RequestParam("path") String originalPath) {
        Schema schema = StorageReader.getStoragesBySchema(schemaId);
        FileLocation fileLocation = triggerAuthenticationService.getFileLocation(request, schema, originalPath, false);
        fileManager.delete(schema, fileLocation.getPath());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/schema/{schema}/file/**")
    public void downloadFile(@PathVariable("schema") String schemaId,
                             @RequestParam(value = "download", required = false, defaultValue = "false") boolean download,
                         HttpServletRequest request, HttpServletResponse response) throws IOException {
        Schema schema = StorageReader.getStoragesBySchema(schemaId);
        String originalPath = request.getRequestURI().replace("/schema/" + schemaId + "/file/", "/");
        FileLocation fileLocation = triggerAuthenticationService.getFileLocation(request, schema, originalPath, true);
        if(!fileManager.existsFile(schema, fileLocation.getPath())) {
            throw new NotFoundException("Not found: " + fileLocation.getPath());
        }
        String contentType = fileManager.getContentType(schema, fileLocation.getPath());
        InputStream inputStream = fileManager.downloadFile(schema, fileLocation.getPath());
        response.setContentType(contentType);
        if(download) {
            response.setHeader("Content-disposition", "attachment");
        }
        fileManager.copy(inputStream, response.getOutputStream());
    }

    @GetMapping(value = "/schema/{schema}/file/exists")
    public ResponseEntity exists(HttpServletRequest request,
                                 @PathVariable("schema") String schemaId,
                                 @RequestParam("path") String originalPath) {
        Schema schema = StorageReader.getStoragesBySchema(schemaId);
        FileLocation fileLocation = triggerAuthenticationService.getFileLocation(request, schema, originalPath, true);
        return fileManager.existsFile(schema, fileLocation.getPath()) ? ResponseEntity.ok().build() : ResponseEntity.notFound().build();
    }

    @GetMapping(value = "/schema/{schema}/file/details")
    public ResponseEntity details(HttpServletRequest request,
                                 @PathVariable("schema") String schemaId,
                                 @RequestParam("path") String originalPath) {
        Schema schema = StorageReader.getStoragesBySchema(schemaId);
        FileLocation fileLocation = triggerAuthenticationService.getFileLocation(request, schema, originalPath, false);
        FileMetadata fileMetadata = fileManager.getDetails(schema, fileLocation.getPath());
        return ResponseEntity.ok(fileMetadata);
    }

}
