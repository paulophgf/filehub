package br.com.mpps.filehub.domain.model.storage.filesystem;

import br.com.mpps.filehub.domain.exceptions.DownloadException;
import br.com.mpps.filehub.domain.exceptions.StorageException;
import br.com.mpps.filehub.domain.exceptions.UploadException;
import br.com.mpps.filehub.domain.model.Base64Upload;
import br.com.mpps.filehub.domain.model.FileItem;
import br.com.mpps.filehub.domain.model.config.Storage;
import br.com.mpps.filehub.domain.model.storage.Base64File;
import br.com.mpps.filehub.domain.model.storage.EnumStorageType;
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

public class FileSystemStorage extends Storage<FileSystemProperties> {

    public FileSystemStorage(String id, EnumStorageType type, FileSystemProperties properties) {
        super(id, type, properties);
    }

    // Directory Operations

    @Override
    public boolean createDirectory(String directory) {
        String pathDir = FileSystemStorage.formatDirPathToSF(directory);
        Boolean result = false;
        String filePath = formatFilePath(pathDir);
        File dir = new File(filePath);
        if(!dir.exists() || !dir.isDirectory()) {
            result = dir.mkdirs();
        }
        return result;
    }

    @Override
    public boolean deleteDirectory(String path, boolean isRecursive) {
        String pathDir = FileSystemStorage.formatDirPathToSF(path);
        Boolean result = false;
        File file = new File(formatFilePath(pathDir));
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
        String pathDir = FileSystemStorage.formatDirPathToSF(path);
        String filePath = formatFilePath(pathDir);
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
        File file = new File(formatFilePath(pathDir));
        return file.exists() && file.isDirectory();
    }


    // Files Operations

    @Override
    public void upload(String pathDir, MultipartFile[] files, Boolean mkdir) {
        String filePath = formatFilePath(pathDir);
        checkIfFolderExists(pathDir, mkdir);
        for(MultipartFile file : files) {
            Path filepath = Paths.get(filePath, file.getOriginalFilename());
            createFileIfNotExists(filepath);
            int readByteCount;
            byte[] buffer = new byte[4096];

            try(InputStream in = file.getInputStream();
                OutputStream out = new FileOutputStream(filepath.toFile())) {
                while((readByteCount = in.read(buffer)) != -1) {
                    out.write(buffer, 0, readByteCount);
                }
            } catch (IOException e) {
                throw new UploadException("Error to upload the file " + file.getName(), e);
            }
        }
    }

    @Override
    public void uploadBase64(String pathDir, Base64Upload[] files, Boolean mkdir) {
        String filePath = formatFilePath(pathDir);
        checkIfFolderExists(pathDir, mkdir);
        for(Base64Upload file : files) {
            Base64File base64File = file.getBase64();
            byte[] decodedImg = Base64.getDecoder().decode(base64File.getFile().getBytes(StandardCharsets.UTF_8));
            Path destinationFile = Paths.get(filePath, base64File.getFilename());
            try {
                Files.write(destinationFile, decodedImg);
            } catch (IOException e) {
                throw new UploadException("Error to upload the file " + file.getFilename(), e);
            }
        }
    }

    @Override
    public OutputStream getOutputStreamFromStorage(String pathDir, String filename, Boolean mkdir) throws IOException {
        String filePath = formatFilePath(pathDir);
        checkIfFolderExists(pathDir, mkdir);
        Path filepath = Paths.get(filePath, filename);
        createFileIfNotExists(filepath);
        return new FileOutputStream(filepath.toFile());
    }

    @Override
    public boolean existsFile(String path) {
        File file = new File(formatFilePath(path));
        return file.exists() && !file.isDirectory();
    }

    @Override
    public String getContentType(String filePath) {
        File file = new File(formatFilePath(filePath));
        return URLConnection.guessContentTypeFromName(file.getName());
    }

    @Override
    public boolean delete(String path) {
        Boolean result = false;
        File file = new File(formatFilePath(path));
        if(file.exists()) {
            if(!file.isDirectory()) {
                result = file.delete();
            }
        }
        return result;
    }



    @Override
    public InputStream downloadFile(String filePath) throws IOException {
        File file = new File(formatFilePath(filePath));
        return FileUtils.openInputStream(file);
    }

    @Override
    public void transfer(Storage destination, String pathDir, List<String> filenames, Boolean mkdir) {
        String path = formatFilePath(pathDir);
        for(String filename : filenames) {
            int readByteCount;
            byte[] buffer = new byte[4096];
            try(InputStream in = new FileInputStream(path + filename);
                OutputStream out = destination.getOutputStreamFromStorage(pathDir, filename, mkdir)) {
                while((readByteCount = in.read(buffer)) != -1) {
                    out.write(buffer, 0, readByteCount);
                }
            } catch (IOException e) {
                throw new RuntimeException("Error to transfer the file " + filename, e);
            }
        }
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

    private static String formatDirPathToSF(String path) {
        if(!path.startsWith("/")) {
            path = "/" + path;
        }
        if(path.endsWith("/")) {
            path = path.substring(0, path.length()-1);
        }
        return path;
    }

    private String formatFilePath(String path) {
        return properties.getBaseDir() + path.replace("/", File.separator);
    }

}
