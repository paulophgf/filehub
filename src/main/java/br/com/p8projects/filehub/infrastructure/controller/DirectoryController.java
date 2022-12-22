package br.com.p8projects.filehub.infrastructure.controller;

import br.com.p8projects.filehub.domain.model.FileItem;
import br.com.p8projects.filehub.domain.model.FileLocation;
import br.com.p8projects.filehub.domain.model.config.Schema;
import br.com.p8projects.filehub.domain.usecase.DirectoryManager;
import br.com.p8projects.filehub.domain.usecase.TriggerAuthenticationService;
import br.com.p8projects.filehub.infrastructure.config.StorageResourceReader;
import io.swagger.annotations.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Api(value = "Directory Operations")
@RestController
public class DirectoryController {

    private final DirectoryManager directoryManager;
    private final TriggerAuthenticationService triggerAuthenticationService;


    @Autowired
    public DirectoryController(DirectoryManager directoryManager, TriggerAuthenticationService triggerAuthenticationService) {
        this.directoryManager = directoryManager;
        this.triggerAuthenticationService = triggerAuthenticationService;
    }


    @ApiOperation(value = "It creates a new directory in all schema storages",
            notes = "It allows to create directories using a path. The operation " +
                    "will be executed in each storages of schema informed. " +
                    "If you use the value /home/account/user for the path parameter, " +
                    "then will be created 3 new directories if any of these directories " +
                    "already haven't been created. Created directories won't be created again.")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Operation executed successfully")
    })
    @PostMapping(value = "/schema/{schema}/dir")
    public ResponseEntity<Boolean> create(HttpServletRequest request,
                                          @ApiParam(value = "Schema name (created at XLM configuration file)", required = true)
                                                   @PathVariable("schema") String schemaId,
                                          @ApiParam(value = "Directory path separated by slash character \" / \"", required = true)
                                                   @RequestParam("path") String path) {
        Schema schema = StorageResourceReader.getSchema(schemaId);
        schema.checkIfIsAllowedDirectoryOperations();
        FileLocation fileLocation = triggerAuthenticationService.getFileLocation(request, schema, path, false);
        Boolean result = directoryManager.createDirectory(schema, fileLocation.getPath());
        return ResponseEntity.ok(result);
    }

    @ApiOperation(value = "It renames an directory",
            notes = "It allows to rename directories using a path. The operation " +
                    "will be executed in each storages of schema informed. " +
                    "If you want to rename the path such as /home/account/user to /home/account/new-user, " +
                    "use the follow values to do it: \n" +
                    "path = /home/account/user\n" +
                    "name = new-user")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Operation executed successfully"),
            @ApiResponse(code = 404, message = "Directory not found")
    })
    @PatchMapping(value = "/schema/{schema}/dir/rename")
    public ResponseEntity<Boolean> rename(HttpServletRequest request,
                                          @ApiParam(value = "Schema name (created at XLM configuration file)", required = true)
                                          @PathVariable("schema") String schemaId,
                                          @ApiParam(value = "Directory path separated by slash character \" / \"", required = true)
                                          @RequestParam("path") String path,
                                          @ApiParam(value = "New name to the directory", required = true)
                                          @RequestParam("name") String name) {
        Schema schema = StorageResourceReader.getSchema(schemaId);
        schema.checkIfIsAllowedDirectoryOperations();
        FileLocation fileLocation = triggerAuthenticationService.getFileLocation(request, schema, path, false);
        boolean result = directoryManager.renameDirectory(schema, fileLocation.getPath(), name);
        return ResponseEntity.ok(result);
    }


    @ApiOperation(value = "It deletes a directory in all schema storages",
            notes = "It allows to delete a directory. The operation " +
                    "will be executed in each storages of schema informed. " +
                    "If you use the value /home/account/user for the path parameter, " +
                    "then the directory \"user\" will be deleted.")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Operation executed successfully"),
            @ApiResponse(code = 400, message = "Directory not empty")
    })
    @DeleteMapping(value = "/schema/{schema}/dir")
    public ResponseEntity<Boolean> delete(HttpServletRequest request,
                                          @ApiParam(value = "Schema name (created at XLM configuration file)", required = true)
                                          @PathVariable("schema") String schemaId,
                                          @ApiParam(value = "Directory path separated by slash character \" / \"", required = true)
                                          @RequestParam("path") String path,
                                          @ApiParam(value = "TRUE: Will delete the directory and other directories inside it.\n" +
                                                  "FALSE: Just will delete the directory if it is empty", defaultValue = "false")
                                          @RequestParam(value = "recursively", required = false, defaultValue = "false") Boolean recursively) {
        Schema schema = StorageResourceReader.getSchema(schemaId);
        schema.checkIfIsAllowedDirectoryOperations();
        FileLocation fileLocation = triggerAuthenticationService.getFileLocation(request, schema, path, false);
        Boolean result = directoryManager.deleteDirectory(schema, fileLocation.getPath(), recursively);
        return ResponseEntity.ok(result);
    }


    @ApiOperation(value = "It lists files and directories",
            notes = "It allows to list files and directories inside a directory. The operation " +
                    "will be executed on the first schema storage considering the XML configuration file. " +
                    "If you use the value /home/account/user for the path parameter, " +
                    "then will list everything inside the \"user\" directory.")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Operation executed successfully")
    })
    @GetMapping(value = "/schema/{schema}/dir")
    public ResponseEntity<List<FileItem>> listFiles(HttpServletRequest request,
                                                    @ApiParam(value = "Schema name (created at XLM configuration file)", required = true)
                                                    @PathVariable("schema") String schemaId,
                                                    @ApiParam(value = "Directory path separated by slash character \" / \"", required = true)
                                                    @RequestParam("path") String path) {
        Schema schema = StorageResourceReader.getSchema(schemaId);
        schema.checkIfIsAllowedDirectoryOperations();
        FileLocation fileLocation = triggerAuthenticationService.getFileLocation(request, schema, path, true);
        List<FileItem> result = directoryManager.listFiles(schema, fileLocation.getPath());
        return ResponseEntity.ok(result);
    }


    @ApiOperation(value = "It check if a directory exists or not",
            notes = "It allows to verify if a directory exists or not. " +
                    "It will be executed on the first schema storage considering the XML configuration file. " +
                    "If you use the value /home/account/user for the path parameter, " +
                    "then the \"user\" directory will will be checked. Files won't be considered.")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Operation executed successfully"),
            @ApiResponse(code = 404, message = "Directory not found")
    })
    @GetMapping(value = "/schema/{schema}/dir/exists")
    public ResponseEntity<Boolean> exists(HttpServletRequest request,
                                 @ApiParam(value = "Schema name (created at XLM configuration file)", required = true)
                                 @PathVariable("schema") String schemaId,
                                 @ApiParam(value = "Directory path separated by slash character \" / \"", required = true)
                                 @RequestParam("path") String path) {
        Schema schema = StorageResourceReader.getSchema(schemaId);
        schema.checkIfIsAllowedDirectoryOperations();
        FileLocation fileLocation = triggerAuthenticationService.getFileLocation(request, schema, path, false);
        boolean existsDirectory = directoryManager.existsDirectory(schema, fileLocation.getPath());
        return existsDirectory ? ResponseEntity.ok(true) : ResponseEntity.status(HttpStatus.NOT_FOUND).body(false);
    }

}
