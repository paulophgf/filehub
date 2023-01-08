package br.com.p8projects.filehub.domain.usecase.storage;

import br.com.p8projects.filehub.domain.exceptions.NotFoundException;
import br.com.p8projects.filehub.domain.exceptions.StorageException;
import br.com.p8projects.filehub.domain.exceptions.UploadException;
import br.com.p8projects.filehub.domain.model.*;
import br.com.p8projects.filehub.domain.model.config.FhStorage;
import br.com.p8projects.filehub.domain.model.storage.Base64File;
import br.com.p8projects.filehub.domain.model.storage.filesystem.FileSystemProperties;
import br.com.p8projects.filehub.domain.model.upload.Base64Upload;
import br.com.p8projects.filehub.domain.model.upload.UploadBase64Object;
import br.com.p8projects.filehub.domain.model.upload.UploadMultipartObject;
import org.apache.commons.io.FileUtils;
import org.springframework.util.FileSystemUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

public class FileSystemStorage extends FhStorage<FileSystemProperties> {

    public FileSystemStorage(String id, FileSystemProperties properties) {
        super(id, properties);
    }

    // Directory Operations

    @Override
    public void createBaseDirIfNotExist() {
        File file = new File(properties.getBaseDir());
        if(!file.exists() || (file.exists() && !file.isDirectory())) {
            file.mkdirs();
        }
    }

    @Override
    public boolean createDirectory(String directory) {
        String pathDir = properties.formatDirPath(directory);
        Boolean result = false;
        String filePath = properties.formatFilePath(pathDir);
        File dir = new File(filePath);
        if(!dir.exists() || !dir.isDirectory()) {
            result = dir.mkdirs();
        }
        return result;
    }

    @Override
    public boolean renameDirectory(String path, String name) {
        String pathDir = properties.formatDirPath(path);
        Boolean result = false;
        File file = new File(properties.formatFilePath(pathDir));
        if(file.exists() && file.isDirectory()) {
            Path dirPath = Paths.get(path);
            String newPath = dirPath.getParent() + File.separator + name;
            newPath = properties.getBaseDir() + newPath;
            result = file.renameTo(new File(newPath));
        }
        return result;
    }

    @Override
    public boolean deleteDirectory(String path, boolean isRecursive) {
        String pathDir = properties.formatDirPath(path);
        Boolean result = false;
        File file = new File(properties.formatFilePath(pathDir));
        if(file.exists() && file.isDirectory()) {
            if(isRecursive) {
                result = FileSystemUtils.deleteRecursively(file);
            } else {
                if(file.listFiles() == null || file.listFiles().length == 0) {
                    result = file.delete();
                } else {
                    throw new StorageException("Directory not empty");
                }
            }
        }
        return result;
    }

    @Override
    public List<FileItem> listFiles(String path) {
        String pathDir = properties.formatDirPath(path);
        String filePath = properties.formatFilePath(pathDir);
        File dir = new File(filePath);
        if(!dir.isDirectory()) {
            throw new StorageException(pathDir + " is not a directory");
        }
        List<FileItem> fileItems = new ArrayList<>();
        for(File file : dir.listFiles()) {
            String fileName = file.isDirectory() ? file.getName() + "/" : file.getName();
            String fsPath = (path.startsWith("/") ? pathDir.substring(1) : pathDir) + "/";
            String mimeType = URLConnection.guessContentTypeFromName(file.getName());
            fileItems.add(new FileItem(fsPath, fileName, file.isDirectory(), mimeType, file.length()));
        }
        return fileItems;
    }

    @Override
    public boolean existsDirectory(String pathDir) {
        File file = new File(properties.formatFilePath(pathDir));
        return file.exists() && file.isDirectory();
    }




    // Files Operations

    @Override
    public void upload(UploadMultipartObject uploadMultipartObject) {
        String filePath = properties.formatFilePath(uploadMultipartObject.getPath());
        checkIfFolderExists(uploadMultipartObject.getPath(), uploadMultipartObject.isMkdir());
        for(UploadMultipartObject.FileUploadObject file : uploadMultipartObject.getFiles()) {
            executeUpdateMultipart(filePath, file.getFile(), file.getFilename());
        }
    }

    private void executeUpdateMultipart(String filePath, MultipartFile file, String filename) {
        Path filepath = Paths.get(filePath, filename);
        createFileIfNotExists(filepath);
        int readByteCount;
        byte[] buffer = new byte[4096];

        try(InputStream in = file.getInputStream();
            OutputStream out = new FileOutputStream(filepath.toFile())) {
            while((readByteCount = in.read(buffer)) != -1) {
                out.write(buffer, 0, readByteCount);
            }
        } catch (IOException e) {
            throw new UploadException("Error to upload the file " + filename, e);
        }
    }


    @Override
    public void uploadBase64(UploadBase64Object uploadBase64Object) {
        String filePath = properties.formatFilePath(uploadBase64Object.getPath());
        checkIfFolderExists(uploadBase64Object.getPath(), uploadBase64Object.isMkdir());
        for(Base64Upload file : uploadBase64Object.getFiles()) {
            Base64File base64File = file.getBase64();
            executeUpdateBase64(base64File, filePath, file.getFilename());
        }
    }

    private void executeUpdateBase64(Base64File base64File, String filePath, String filename) {
        byte[] decodedImg = Base64.getDecoder().decode(base64File.getFile().getBytes(StandardCharsets.UTF_8));
        Path destinationFile = Paths.get(filePath, base64File.getFilename());
        try {
            Files.write(destinationFile, decodedImg);
        } catch (IOException e) {
            throw new UploadException("Error to upload the file " + filename, e);
        }
    }


    @Override
    public OutputStream getOutputStreamFromStorage(String pathDir, String filename, Boolean mkdir) throws IOException {
        String filePath = properties.formatFilePath(pathDir);
        checkIfFolderExists(pathDir, mkdir);
        Path filepath = Paths.get(filePath, filename);
        createFileIfNotExists(filepath);
        return new FileOutputStream(filepath.toFile());
    }

    @Override
    public void transfer(FhStorage destination, UploadMultipartObject uploadMultipartObject) {
        String filePath = properties.formatFilePath(uploadMultipartObject.getPath());
        for(UploadMultipartObject.FileUploadObject file : uploadMultipartObject.getFiles()) {
            executeTransfer(destination, uploadMultipartObject.getPath(), filePath, file.getFilename(), uploadMultipartObject.isMkdir());
        }
    }

    private void executeTransfer(FhStorage destination, String pathDir, String filePath, String filename, boolean mkdir) {
        int readByteCount;
        byte[] buffer = new byte[4096];
        try(InputStream in = new FileInputStream(filePath + filename);
            OutputStream out = destination.getOutputStreamFromStorage(pathDir, filename, mkdir)) {
            while((readByteCount = in.read(buffer)) != -1) {
                out.write(buffer, 0, readByteCount);
            }
        } catch (IOException e) {
            throw new RuntimeException("Error to transfer the file " + filename, e);
        }
    }


    @Override
    public boolean existsFile(String path) {
        File file = new File(properties.formatFilePath(path));
        return file.exists() && !file.isDirectory();
    }

    @Override
    public FileMetadata getFileDetails(String filePath) {
        File file = new File(properties.formatFilePath(filePath));
        String contentType = URLConnection.guessContentTypeFromName(file.getName());
        FileMetadata fileMetadata = new FileMetadata();
        fileMetadata.setContentType(contentType);
        fileMetadata.setSize(file.length());
        fileMetadata.setLastModified(file.lastModified());
        return fileMetadata;
    }

    @Override
    public boolean delete(String path) {
        Boolean result = false;
        File file = new File(properties.formatFilePath(path));
        checkIfFileExists(path);
        if(!file.isDirectory()) {
            result = file.delete();
        }
        return result;
    }



    @Override
    public InputStream downloadFile(String filePath) throws IOException {
        File file = new File(properties.formatFilePath(filePath));
        return FileUtils.openInputStream(file);
    }



    private void createFileIfNotExists(Path filepath) {
        try {
            File file = filepath.toFile();
            if (!file.exists()) {
                file.createNewFile();
            }
        } catch (IOException e) {
            throw new RuntimeException("Error to create the file: " + filepath, e);
        }
    }

    private void checkIfFolderExists(String pathDir, Boolean mkdir) {
        if(!existsDirectory(pathDir)) {
            if(!mkdir) {
                throw new StorageException("Directory not found: " + pathDir);
            }
            createDirectory(pathDir);
        }
    }

    private void checkIfFileExists(String path) {
        if(!existsFile(path)) {
            throw new NotFoundException("File not found");
        }
    }

}
