package br.com.mpps.filehub.infrastructure.controller;

import br.com.mpps.filehub.domain.exceptions.NotFoundException;
import br.com.mpps.filehub.domain.model.Base64Upload;
import br.com.mpps.filehub.domain.model.FileMetadata;
import br.com.mpps.filehub.domain.model.FileLocation;
import br.com.mpps.filehub.domain.model.config.Schema;
import br.com.mpps.filehub.domain.usecase.FileManager;
import br.com.mpps.filehub.domain.usecase.TriggerAuthenticationService;
import br.com.mpps.filehub.infrastructure.config.StorageResourceReader;
import io.swagger.annotations.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;

@Api(value = "File Operations")
@RestController
public class FileController {

    private final FileManager fileManager;
    private final TriggerAuthenticationService triggerAuthenticationService;


    @Autowired
    public FileController(FileManager fileManager, TriggerAuthenticationService triggerAuthenticationService) {
        this.fileManager = fileManager;
        this.triggerAuthenticationService = triggerAuthenticationService;
    }


    @ApiOperation(value = "Upload single file",
            notes = "It allows to upload a file to the path informed. The operation " +
                    "will be executed in each storages of schema informed.")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Operation executed successfully"),
            @ApiResponse(code = 404, message = "Directory not found")
    })
    @PostMapping(value = "/schema/{schema}/upload")
    public ResponseEntity<String> uploadMultipartFile(HttpServletRequest request,
                                                       @ApiParam(value = "Schema name (created at XLM configuration file)", required = true)
                                                       @PathVariable("schema") String schemaId,
                                                       @ApiParam(value = "Single multipart file", required = true)
                                                       @RequestParam("file") MultipartFile file,
                                                       @ApiParam(value = "Path separated by slash character \" / \" where the file will be saved", required = true)
                                                       @RequestParam("path") String path,
                                                       @ApiParam(value = "Name used to save the file. If filename is not informed, the original filename is used.\n" +
                                                               "Obs.: If TRIGGER function is used, the filename returned from endpoint will be prioritized.")
                                                       @RequestParam(value = "filename", required = false) String filename,
                                                       @ApiParam(value = "TRUE: Will create the directory path before upload the file.\n" +
                                                              "FALSE: Can do an error if the directory path does not exists", defaultValue = "false")
                                                       @RequestParam(value = "mkdir", required = false, defaultValue = "false") Boolean mkdir) {
        Schema schema = StorageResourceReader.getSchema(schemaId);
        FileLocation fileLocation = triggerAuthenticationService.getFileLocation(request, schema, path, false);
        fileLocation.setFilename(filename).setFilename(file.getOriginalFilename());
        fileManager.upload(schema, fileLocation, file, mkdir);
        return ResponseEntity.ok().build();
    }


    @ApiOperation(value = "Upload multiple file",
            notes = "It allows to upload files to the path informed. The operation " +
                    "will be executed in each storages of schema informed.")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Operation executed successfully"),
            @ApiResponse(code = 404, message = "Directory not found")
    })
    @PostMapping(value = "/schema/{schema}/upload/multi")
    public ResponseEntity<String> uploadMultipartFiles(HttpServletRequest request,
                                        @ApiParam(value = "Schema name (created at XLM configuration file)", required = true)
                                        @PathVariable("schema") String schemaId,
                                        @ApiParam(value = "Multiple multipart files", required = true)
                                        @RequestParam("files") MultipartFile[] files,
                                        @ApiParam(value = "Path separated by slash character \" / \" where the file will be saved", required = true)
                                        @RequestParam("path") String path,
                                        @ApiParam(value = "TRUE: Will create the directory path before upload the file.\n" +
                                               "FALSE: Can do an error if the directory path does not exists", defaultValue = "false")
                                        @RequestParam(value = "mkdir", required = false, defaultValue = "false") Boolean mkdir,
                                        @ApiParam(value = "TRUE: Will do the upload at the same time to all schema storages.\n" +
                                               "FALSE: Will do the upload storage by storage once per time", defaultValue = "false")
                                        @RequestParam(value = "parallel", required = false, defaultValue = "false") Boolean parallel) {
        Schema schema = StorageResourceReader.getSchema(schemaId);
        FileLocation fileLocation = triggerAuthenticationService.getFileLocation(request, schema, path, false);
        fileManager.upload(schema, fileLocation.getPath(), files, mkdir, parallel);
        return ResponseEntity.ok().build();
    }


    @ApiOperation(value = "Upload single base64 file",
            notes = "It allows to upload a base64 file to the path informed. The operation " +
                    "will be executed in each storages of schema informed.")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Operation executed successfully"),
            @ApiResponse(code = 404, message = "Directory not found")
    })
    @PostMapping(value = "/schema/{schema}/upload64")
    public ResponseEntity<String> uploadBase64File(HttpServletRequest request,
                                                    @ApiParam(value = "Base64 object", required = true)
                                                    @RequestBody Base64Upload file,
                                                    @ApiParam(value = "Schema name (created at XLM configuration file)", required = true)
                                                    @PathVariable("schema") String schemaId,
                                                    @ApiParam(value = "Path separated by slash character \" / \" where the file will be saved", required = true)
                                                    @RequestParam("path") String path,
                                                    @ApiParam(value = "TRUE: Will create the directory path before upload the file.\n" +
                                                           "FALSE: Can do an error if the directory path does not exists", defaultValue = "false")
                                                    @RequestParam(value = "mkdir", required = false, defaultValue = "false") Boolean mkdir) {
        Schema schema = StorageResourceReader.getSchema(schemaId);
        FileLocation fileLocation = triggerAuthenticationService.getFileLocation(request, schema, path, false);
        fileManager.uploadBase64(schema, fileLocation, file, mkdir);
        fileLocation.setFilename(file.getFilename());
        return ResponseEntity.ok().build();
    }


    @ApiOperation(value = "Upload multiple base64 files",
            notes = "It allows to upload base64 files to the path informed. The operation " +
                    "will be executed in each storages of schema informed.")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Operation executed successfully"),
            @ApiResponse(code = 404, message = "Directory not found")
    })
    @PostMapping(value = "/schema/{schema}/upload64/multi")
    public ResponseEntity<String> uploadBase64Files(HttpServletRequest request,
                                @ApiParam(value = "Base64 object array", required = true)
                                @RequestBody Base64Upload[] files,
                                @ApiParam(value = "Schema name (created at XLM configuration file)", required = true)
                                @PathVariable("schema") String schemaId,
                                @ApiParam(value = "Path separated by slash character \" / \" where the file will be saved", required = true)
                                @RequestParam("path") String path,
                                @ApiParam(value = "TRUE: Will create the directory path before upload the file.\n" +
                                        "FALSE: Can do an error if the directory path does not exists", defaultValue = "false")
                                @RequestParam(value = "mkdir", required = false, defaultValue = "false") Boolean mkdir) {
        Schema schema = StorageResourceReader.getSchema(schemaId);
        FileLocation fileLocation = triggerAuthenticationService.getFileLocation(request, schema, path, false);
        fileManager.uploadBase64(schema, fileLocation.getPath(), files, mkdir);
        return ResponseEntity.ok().build();
    }


    @ApiOperation(value = "Delete file",
            notes = "Delete a file from the all schema storage considering the XML configuration file.")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Operation executed successfully"),
            @ApiResponse(code = 404, message = "File not found"),
            @ApiResponse(code = 403, message = "Unauthorized")
    })
    @DeleteMapping(value = "/schema/{schema}/file")
    public ResponseEntity<Boolean> delete(HttpServletRequest request,
                                 @ApiParam(value = "Schema name (created at XLM configuration file)", required = true)
                                 @PathVariable("schema") String schemaId,
                                 @ApiParam(value = "File path separated by slash character \" / \"", required = true)
                                 @RequestParam("path") String path) {
        Schema schema = StorageResourceReader.getSchema(schemaId);
        FileLocation fileLocation = triggerAuthenticationService.getFileLocation(request, schema, path, false);
        fileManager.delete(schema, fileLocation.getPath());
        return ResponseEntity.ok(true);
    }


    @ApiOperation(value = "Download file",
            notes = "Download a file from the first schema storage considering the XML configuration file.")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Operation executed successfully"),
            @ApiResponse(code = 404, message = "File not found"),
            @ApiResponse(code = 403, message = "Unauthorized")
    })
    @GetMapping("/schema/{schema}/file/**")
    public void downloadFile(@ApiParam(value = "Schema name (created at XLM configuration file)", required = true)
                             @PathVariable("schema") String schemaId,
                             @ApiParam(value = "TRUE: Force the download by a web browser\n" +
                                               "FALSE: Can play or execute the file by a web browser", defaultValue = "false")
                             @RequestParam(value = "download", required = false, defaultValue = "false") boolean download,
                         HttpServletRequest request, HttpServletResponse response) throws IOException {
        Schema schema = StorageResourceReader.getSchema(schemaId);
        String originalPath = request.getRequestURI().replace("/schema/" + schemaId + "/file/", "/");
        FileLocation fileLocation = triggerAuthenticationService.getFileLocation(request, schema, originalPath, true);
        if(!fileManager.existsFile(schema, fileLocation.getPath())) {
            throw new NotFoundException("Not found: " + fileLocation.getPath());
        }
        String contentType = fileManager.getContentType(schema, fileLocation.getPath());
        response.setContentType(contentType);
        if(download) {
            response.setHeader("Content-disposition", "attachment");
        }
        fileManager.downloadFile(schema, fileLocation.getPath(), response);
    }


    @ApiOperation(value = "Check if exists a file",
            notes = "Check if exists a file using the first schema storage considering the XML configuration file.")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Operation executed successfully"),
            @ApiResponse(code = 404, message = "File not found"),
            @ApiResponse(code = 403, message = "Unauthorized")
    })
    @GetMapping(value = "/schema/{schema}/file/exists")
    public ResponseEntity<Boolean> exists(HttpServletRequest request,
                                 @ApiParam(value = "Schema name (created at XLM configuration file)", required = true)
                                 @PathVariable("schema") String schemaId,
                                 @ApiParam(value = "File path separated by slash character \" / \"", required = true)
                                 @RequestParam("path") String path) {
        Schema schema = StorageResourceReader.getSchema(schemaId);
        FileLocation fileLocation = triggerAuthenticationService.getFileLocation(request, schema, path, true);
        return fileManager.existsFile(schema, fileLocation.getPath()) ? ResponseEntity.ok(true) : ResponseEntity.notFound().build();
    }


    @ApiOperation(value = "Show file details",
            notes = "Show file details using the first schema storage considering the XML configuration file.")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Operation executed successfully"),
            @ApiResponse(code = 404, message = "File not found"),
            @ApiResponse(code = 403, message = "Unauthorized")
    })
    @GetMapping(value = "/schema/{schema}/file/details")
    public ResponseEntity<FileMetadata> details(HttpServletRequest request,
                                 @ApiParam(value = "Schema name (created at XLM configuration file)", required = true)
                                 @PathVariable("schema") String schemaId,
                                 @ApiParam(value = "File path separated by slash character \" / \"", required = true)
                                 @RequestParam("path") String path) {
        Schema schema = StorageResourceReader.getSchema(schemaId);
        FileLocation fileLocation = triggerAuthenticationService.getFileLocation(request, schema, path, false);
        FileMetadata fileMetadata = fileManager.getDetails(schema, fileLocation.getPath());
        return ResponseEntity.ok(fileMetadata);
    }

}
