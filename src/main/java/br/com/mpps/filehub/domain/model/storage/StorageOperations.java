package br.com.mpps.filehub.domain.model.storage;

import br.com.mpps.filehub.domain.model.Base64Upload;
import br.com.mpps.filehub.domain.model.FileItem;
import br.com.mpps.filehub.domain.model.FileLocation;
import br.com.mpps.filehub.domain.model.FileMetadata;
import br.com.mpps.filehub.domain.model.config.Storage;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

public interface StorageOperations {

    boolean createDirectory(String directory);

    boolean deleteDirectory(String path, boolean isRecursive);

    List<FileItem> listFiles(String pathDir);

    boolean existsDirectory(String pathDir);


    void upload(FileLocation fileLocation, MultipartFile file, Boolean mkdir);

    void uploadBase64(FileLocation fileLocation, Base64Upload file, Boolean mkdir);

    void upload(String pathDir, MultipartFile[] files, Boolean mkdir);

    void uploadBase64(String pathDir, Base64Upload[] files, Boolean mkdir);


    OutputStream getOutputStreamFromStorage(String path, String filename, Boolean mkdir) throws IOException;

    void transfer(Storage destination, FileLocation fileLocation, Boolean mkdir);

    void transfer(Storage destination, String pathDir, List<String> filenames, Boolean mkdir);


    boolean delete(String path);

    boolean existsFile(String filePath);

    FileMetadata getFileDetails(String filePath);

    InputStream downloadFile(String pathDir) throws IOException;

}
