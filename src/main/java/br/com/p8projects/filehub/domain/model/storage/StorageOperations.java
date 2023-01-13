package br.com.p8projects.filehub.domain.model.storage;

import br.com.p8projects.filehub.domain.model.*;
import br.com.p8projects.filehub.domain.model.config.FhStorage;
import br.com.p8projects.filehub.domain.model.upload.UploadBase64Object;
import br.com.p8projects.filehub.domain.model.upload.UploadMultipartObject;
import br.com.p8projects.filehub.domain.model.upload.UploadObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

public interface StorageOperations {

    void createBaseDirIfNotExist();

    boolean createDirectory(String directory);

    boolean renameDirectory(String path, String name);

    boolean deleteDirectory(String path, boolean isRecursive);

    List<FileItem> listFiles(String pathDir);

    boolean existsDirectory(String pathDir);


    void upload(UploadMultipartObject uploadMultipartObject);

    void uploadBase64(UploadBase64Object uploadBase64Object);


    OutputStream getOutputStreamFromStorage(UploadObject uploadObject, String filename, String contentType) throws IOException;

    void writeFileInputStream(TransferFileObject transfer, boolean isMkdir);

    TransferFileObject getTransferFileObject(String path, String filename);

    void transfer(FhStorage destination, UploadMultipartObject uploadMultipartObject);


    boolean delete(String path);

    boolean existsFile(String filePath);

    FileMetadata getFileDetails(String filePath);

    InputStream downloadFile(String pathDir) throws IOException;

}
