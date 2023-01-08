package br.com.p8projects.filehub.domain.usecase.storage;

import br.com.p8projects.filehub.domain.exceptions.DownloadException;
import br.com.p8projects.filehub.domain.exceptions.NotFoundException;
import br.com.p8projects.filehub.domain.exceptions.StorageException;
import br.com.p8projects.filehub.domain.exceptions.UploadException;
import br.com.p8projects.filehub.domain.model.*;
import br.com.p8projects.filehub.domain.model.FileMetadata;
import br.com.p8projects.filehub.domain.model.config.FhStorage;
import br.com.p8projects.filehub.domain.model.storage.Base64File;
import br.com.p8projects.filehub.domain.model.storage.dropbox.DropboxProperties;
import br.com.p8projects.filehub.domain.model.upload.Base64Upload;
import br.com.p8projects.filehub.domain.model.upload.UploadBase64Object;
import br.com.p8projects.filehub.domain.model.upload.UploadMultipartObject;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.dropbox.core.DbxException;
import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.InvalidAccessTokenException;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Slf4j
public class DropboxStorage extends FhStorage<DropboxProperties> {

    private static Map<String, DbxClientV2> dbxClientV2;


    public DropboxStorage(String id, DropboxProperties properties) {
        super(id, properties);
        if(dbxClientV2 == null) {
            dbxClientV2 = new HashMap<>();
        }
    }

    // Directory Operations

    @Override
    public void createBaseDirIfNotExist() {
        if(!"".equals(properties.getBaseDir())) {
            createDirectory("");
        }
    }

    @Override
    public boolean createDirectory(String path) {
        DbxClientV2 client = getClient();
        String pathDir = properties.formatDirPath(path);
        if(!existsDirectory(client, pathDir)) {
            try {
                client.files().createFolderV2(pathDir);
            } catch (InvalidAccessTokenException e) {
                throw new StorageException("Invalid or expired token (Dropbox storage)");
            } catch (DbxException e) {
                e.printStackTrace();
                throw new StorageException("Error to create a directory using Dropbox: " + path);
            }
        }
        return true;
    }

    @Override
    public boolean renameDirectory(String path, String name) {
        DbxClientV2 client = getClient();
        String pathDir = properties.formatDirPath(path);
        try {
            checkIfDirectoryExists(client, pathDir);
            String newPath = pathDir.substring(0, pathDir.lastIndexOf("/")) + "/" + name;
            client.files().moveV2(pathDir, newPath);
        } catch (InvalidAccessTokenException e) {
            throw new StorageException("Invalid or expired token (Dropbox storage)");
        } catch (DbxException e) {
            e.printStackTrace();
            throw new StorageException("Error to rename a directory using Dropbox: " + pathDir);
        }
        return true;
    }

    private void checkIfDirectoryExists(DbxClientV2 client, String pathDir) {
        if (!"".equals(pathDir) && !existsDirectory(client, pathDir)) {
            throw new NotFoundException("Directory not found");
        }
    }

    @Override
    public boolean deleteDirectory(String path, boolean isRecursive) {
        DbxClientV2 client = getClient();
        String pathDir = properties.formatDirPath(path);
        try {
            checkIfDirectoryExists(client, pathDir);
            if (!client.files().listFolderBuilder(pathDir).start().getEntries().isEmpty()) {
                if (!isRecursive) {
                    throw new StorageException("Directory not empty");
                }
            }
            client.files().deleteV2(pathDir);
        } catch (InvalidAccessTokenException e) {
            throw new StorageException("Invalid or expired token (Dropbox storage)");
        } catch (DbxException e) {
            e.printStackTrace();
            throw new StorageException("Error to delete a directory using Dropbox: " + pathDir);
        }
        return true;
    }

    @Override
    public List<FileItem> listFiles(String path) {
        DbxClientV2 client = getClient();
        String pathDir = properties.formatDirPath(path);
        checkIfDirectoryExists(client, pathDir);
        List<FileItem> fileItems = null;
        try {
            ListFolderResult folderResult = client.files().listFolderBuilder(pathDir).withRecursive(false).withIncludeDeleted(false).start();
            if (folderResult.getEntries() != null && !folderResult.getEntries().isEmpty()) {
                fileItems = new ArrayList<>();
                for (Metadata metadata : folderResult.getEntries()) {
                    String fileName = metadata.getName();
                    boolean isDirectory = metadata instanceof FolderMetadata;
                    String mimeType = null;
                    long size = 0L;
                    if (!isDirectory) {
                        com.dropbox.core.v2.files.FileMetadata fileMetadata = (com.dropbox.core.v2.files.FileMetadata) metadata;
                        size = fileMetadata.getSize();
                        mimeType = URLConnection.guessContentTypeFromName(fileMetadata.getName());
                    }
                    fileItems.add(new FileItem(path, fileName, isDirectory, mimeType, size));
                }
            }
        } catch (InvalidAccessTokenException e) {
            throw new StorageException("Invalid or expired token (Dropbox storage)");
        } catch (DbxException e) {
            e.printStackTrace();
            throw new StorageException("Error to list files from directory using Dropbox: " + path);
        }
        return fileItems;
    }

    @Override
    public boolean existsDirectory(String path) {
        DbxClientV2 client = getClient();
        String pathDir = properties.formatDirPath(path);
        return existsDirectory(client, pathDir);
    }


    // Files Operations


    @Override
    public void upload(UploadMultipartObject uploadMultipartObject) {
        DbxClientV2 client = getClient();
        String path = properties.formatDirPath(uploadMultipartObject.getPath());
        checkIfFolderExists(client, path, uploadMultipartObject.isMkdir());
        for(UploadMultipartObject.FileUploadObject file : uploadMultipartObject.getFiles()) {
            executeUploadMultipart(client, file.getFile(), path, file.getFilename());
        }
    }

    private void executeUploadMultipart(DbxClientV2 client, MultipartFile file, String pathDir, String filename) {
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType(file.getContentType());
        metadata.setContentLength(file.getSize());
        try(InputStream in = file.getInputStream()) {
            String keyfile = pathDir.endsWith("/") ? filename : pathDir + "/" + filename;
            client.files().uploadBuilder(keyfile).uploadAndFinish(in);
        } catch (InvalidAccessTokenException e) {
            throw new StorageException("Invalid or expired token (Dropbox storage)");
        } catch (IOException | DbxException e) {
            throw new UploadException("Error to upload the file " + file.getName(), e);
        }
    }


    @Override
    public void uploadBase64(UploadBase64Object uploadBase64Object) {
        DbxClientV2 client = getClient();
        String path = properties.formatDirPath(uploadBase64Object.getPath());
        checkIfFolderExists(client, path, uploadBase64Object.isMkdir());
        for(Base64Upload file : uploadBase64Object.getFiles()) {
            executeUploadBase64(client, file, path, file.getFilename());
        }
    }

    private void executeUploadBase64(DbxClientV2 client, Base64Upload file, String path, String filename) {
        Base64File base64File = file.getBase64();
        byte[] bI = Base64.getDecoder().decode(base64File.getFile().getBytes(StandardCharsets.UTF_8));
        InputStream fis = new ByteArrayInputStream(bI);
        String keyfile = path.endsWith("/") ? filename : path + "/" + filename;
        try {
            client.files().uploadBuilder(keyfile).uploadAndFinish(fis);
        } catch (InvalidAccessTokenException e) {
            throw new StorageException("Invalid or expired token (Dropbox storage)");
        } catch (IOException | DbxException e) {
            throw new UploadException("Error to upload the file " + filename, e);
        }
    }

    @Override
    public OutputStream getOutputStreamFromStorage(String pathDir, String filename, Boolean mkdir) {
        OutputStream outputStream;
        try {
            DbxClientV2 client = getClient();
            String path = properties.formatDirPath(pathDir);
            checkIfFolderExists(client, path, mkdir);
            String keyfile = pathDir.endsWith("/") ? filename : pathDir + "/" + filename;
            outputStream = client.files().uploadBuilder(keyfile).start().getOutputStream();
        } catch (InvalidAccessTokenException e) {
            throw new StorageException("Invalid or expired token (Dropbox storage)");
        } catch (DbxException e) {
            throw new UploadException("Error to get OutputStream from Dropbox", e);
        }
        return outputStream;
    }

    @Override
    public void transfer(FhStorage destination, UploadMultipartObject uploadMultipartObject) {
        DbxClientV2 client = getClient();
        String filePath = properties.formatDirPath(uploadMultipartObject.getPath());
        for(UploadMultipartObject.FileUploadObject file : uploadMultipartObject.getFiles()) {
            executeTransfer(client, destination, uploadMultipartObject.getPath(), filePath, file.getFilename(), uploadMultipartObject.isMkdir());
        }
    }

    private void executeTransfer(DbxClientV2 client, FhStorage destination, String pathDir, String filePath, String filename, boolean mkdir) {
        int readByteCount;
        byte[] buffer = new byte[4096];
        try(InputStream in = client.files().downloadBuilder(filePath).start().getInputStream();
            OutputStream out = destination.getOutputStreamFromStorage(pathDir, filename, mkdir)) {
            while((readByteCount = in.read(buffer)) != -1) {
                out.write(buffer, 0, readByteCount);
            }
        } catch (InvalidAccessTokenException e) {
            throw new StorageException("Invalid or expired token (Dropbox storage)");
        } catch (IOException | DbxException e) {
            throw new RuntimeException("Error to transfer the file " + filename, e);
        }
    }


    @Override
    public boolean delete(String path) {
        DbxClientV2 client = getClient();
        String filePath = properties.formatDirPath(path);
        checkIfFileExists(client, filePath);
        try {
            client.files().deleteV2(filePath);
        } catch (InvalidAccessTokenException e) {
            throw new StorageException("Invalid or expired token (Dropbox storage)");
        } catch (DbxException e) {
            throw new RuntimeException("Error to delete the file " + path, e);
        }
        return true;
    }

    @Override
    public boolean existsFile(String path) {
        DbxClientV2 client = getClient();
        String filePath = properties.formatFilePath(path);
        return existsFile(client, filePath);
    }

    @Override
    public FileMetadata getFileDetails(String path) {
        DbxClientV2 client = getClient();
        String filePath = properties.formatFilePath(path);
        FileMetadata fileMetadata = new FileMetadata();
        try {
            com.dropbox.core.v2.files.FileMetadata metadata = (com.dropbox.core.v2.files.FileMetadata) client.files().getMetadata(filePath);
            fileMetadata.setContentType(URLConnection.guessContentTypeFromName(metadata.getName()));
            fileMetadata.setSize(metadata.getSize());
            fileMetadata.setLastModified(metadata.getClientModified());
        } catch (InvalidAccessTokenException e) {
            throw new StorageException("Invalid or expired token (Dropbox storage)");
        } catch (DbxException e) {
            e.printStackTrace();
        }
        return fileMetadata;
    }

    @Override
    public InputStream downloadFile(String path) {
        InputStream inputStream;
        DbxClientV2 client = getClient();
        String filePath = properties.formatDirPath(path);
        try {
            inputStream = client.files().downloadBuilder(filePath).start().getInputStream();
        } catch (InvalidAccessTokenException e) {
            throw new StorageException("Invalid or expired token (Dropbox storage)");
        } catch (DbxException e) {
            throw new DownloadException("Error to download the file: " + path);
        }
        return inputStream;
    }



    private DbxClientV2 getClient() {
        if(dbxClientV2.get(getId()) == null) {
            DbxRequestConfig config = DbxRequestConfig.newBuilder("dropbox/FileHubApp").build();
            dbxClientV2.put(getId(), new DbxClientV2(config, properties.getAccessToken()));
        }
        return dbxClientV2.get(getId());
    }


    private boolean existsDirectory(DbxClientV2 client, String pathDir) {
        boolean exists = false;
        try {
            Metadata metadata = client.files().getMetadata(pathDir);
            exists = metadata instanceof FolderMetadata;
        } catch (GetMetadataErrorException e) {
            if (e.errorValue.isPath()) {
                LookupError le = e.errorValue.getPathValue();
                exists = !le.isNotFound();
            }
        } catch (InvalidAccessTokenException e) {
            throw new StorageException("Invalid or expired token (Dropbox storage)");
        } catch (DbxException e) {
            throw new StorageException("Error to check if a directory exists (Dropbox storage)");
        }
        return exists;
    }

    private boolean existsFile(DbxClientV2 client, String filePath) {
        boolean exists = false;
        try {
            Metadata metadata = client.files().getMetadata(filePath);
            exists = metadata instanceof com.dropbox.core.v2.files.FileMetadata;
        } catch (GetMetadataErrorException e) {
            if (e.errorValue.isPath()) {
                LookupError le = e.errorValue.getPathValue();
                exists = !le.isNotFound();
            }
        } catch (InvalidAccessTokenException e) {
            throw new StorageException("Invalid or expired token (Dropbox storage)");
        } catch (DbxException e) {
            throw new StorageException("Error to check if a directory exists (Dropbox storage)");
        }
        return exists;
    }

    private void checkIfFolderExists(DbxClientV2 client, String path, Boolean mkdir) {
        if(!"".equals(path)) {
            if (existsDirectory(client, path)) {
                if (!mkdir) {
                    throw new StorageException("Directory not found: " + path);
                }
                createDirectory(path);
            }
        }
    }

    private void checkIfFileExists(DbxClientV2 client, String filepath) {
        if(!existsFile(client, filepath)) {
            throw new NotFoundException("File not found");
        }
    }

}
