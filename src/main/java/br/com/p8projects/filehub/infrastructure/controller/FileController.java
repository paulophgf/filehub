package br.com.p8projects.filehub.infrastructure.controller;

import br.com.p8projects.filehub.domain.exceptions.NotFoundException;
import br.com.p8projects.filehub.domain.model.EnumFileHubOperation;
import br.com.p8projects.filehub.domain.model.FileLocation;
import br.com.p8projects.filehub.domain.model.FileMetadata;
import br.com.p8projects.filehub.domain.model.config.Schema;
import br.com.p8projects.filehub.domain.model.upload.Base64Upload;
import br.com.p8projects.filehub.domain.model.upload.UploadBase64Object;
import br.com.p8projects.filehub.domain.model.upload.UploadMultipartObject;
import br.com.p8projects.filehub.domain.usecase.FileManager;
import br.com.p8projects.filehub.domain.usecase.TriggerAuthenticationService;
import br.com.p8projects.filehub.infrastructure.config.StorageResourceReader;
import io.swagger.annotations.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

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


    @ApiOperation(value = "Upload files",
            notes = "It allows to upload files to the path informed. The operation " +
                    "will be executed in each storages of schema informed.")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Operation executed successfully"),
            @ApiResponse(code = 404, message = "Directory not found")
    })
    @PostMapping(value = "/schema/{schema}/upload")
    public ResponseEntity<String> uploadMultipartFiles(HttpServletRequest request,
                                        @ApiParam(value = "Schema name (created at XLM configuration file)", required = true)
                                        @PathVariable("schema") String schemaId,
                                        @ApiParam(value = "Multiple multipart files", required = true)
                                        @RequestParam("files") MultipartFile[] files,
                                        @ApiParam(value = "Array of filenames")
                                        @RequestParam(value = "filenames", required = false) String[] filenames,
                                        @ApiParam(value = "Path separated by slash character \" / \" where the file will be saved", required = true)
                                        @RequestParam("path") String path,
                                        @ApiParam(value = "TRUE: Will create the directory path before upload the file.\n" +
                                               "FALSE: Can do an error if the directory path does not exists", defaultValue = "false")
                                        @RequestParam(value = "mkdir", required = false, defaultValue = "false") Boolean mkdir) {
        Schema schema = StorageResourceReader.getSchema(schemaId);
        UploadMultipartObject uploadMultipartObject = new UploadMultipartObject(schema, path, files, filenames, mkdir);
        triggerAuthenticationService.checkUploadOperation(request, uploadMultipartObject, EnumFileHubOperation.UPLOAD_MULTIPART_FILE);
        fileManager.upload(uploadMultipartObject);
        return ResponseEntity.ok().build();
    }


    @ApiOperation(value = "Upload base64 files",
            notes = "It allows to upload base64 files to the path informed. The operation " +
                    "will be executed in each storages of schema informed.")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Operation executed successfully"),
            @ApiResponse(code = 404, message = "Directory not found")
    })
    @PostMapping(value = "/schema/{schema}/upload64")
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
        UploadBase64Object uploadBase64Object = new UploadBase64Object(schema, path, files, mkdir);
        triggerAuthenticationService.checkUploadOperation(request, uploadBase64Object, EnumFileHubOperation.UPLOAD_BASE64_FILE);
        fileManager.uploadBase64(uploadBase64Object);
        return ResponseEntity.ok().build();
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
                             @RequestParam(value = "attachment", required = false, defaultValue = "false") boolean attachment,
                         HttpServletRequest request, HttpServletResponse response) throws IOException {
        Schema schema = StorageResourceReader.getSchema(schemaId);
        String originalPath = request.getRequestURI().replace("/schema/" + schemaId + "/file/", "/");
        FileLocation fileLocation = new FileLocation(schema, originalPath);
        triggerAuthenticationService.checkFileLocation(request, fileLocation, EnumFileHubOperation.DOWNLOAD_FILE);
        if(!fileManager.existsFile(schema, fileLocation.getPath())) {
            throw new NotFoundException("Not found: " + fileLocation.getPath());
        }
        String contentType = fileManager.getContentType(schema, fileLocation.getPath());
        response.setContentType(contentType);
        if(attachment) {
            response.setHeader("Content-disposition", "attachment");
        }
        fileManager.downloadFile(schema, fileLocation.getPath(), response);
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
        FileLocation fileLocation = new FileLocation(schema, path);
        triggerAuthenticationService.checkFileLocation(request, fileLocation, EnumFileHubOperation.DELETE_FILE);
        fileManager.delete(schema, fileLocation.getPath());
        return ResponseEntity.ok(true);
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
        FileLocation fileLocation = new FileLocation(schema, path);
        triggerAuthenticationService.checkFileLocation(request, fileLocation, EnumFileHubOperation.EXIST_FILE);
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
        FileLocation fileLocation = new FileLocation(schema, path);
        triggerAuthenticationService.checkFileLocation(request, fileLocation, EnumFileHubOperation.DOWNLOAD_FILE);
        FileMetadata fileMetadata = fileManager.getDetails(schema, fileLocation.getPath());
        return ResponseEntity.ok(fileMetadata);
    }

}
