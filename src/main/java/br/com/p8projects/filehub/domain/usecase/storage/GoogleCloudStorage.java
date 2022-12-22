package br.com.p8projects.filehub.domain.usecase.storage;

import br.com.p8projects.filehub.domain.exceptions.StorageException;
import br.com.p8projects.filehub.domain.exceptions.UploadException;
import br.com.p8projects.filehub.domain.model.Base64Upload;
import br.com.p8projects.filehub.domain.model.FileItem;
import br.com.p8projects.filehub.domain.model.FileLocation;
import br.com.p8projects.filehub.domain.model.FileMetadata;
import br.com.p8projects.filehub.domain.model.config.FhStorage;
import br.com.p8projects.filehub.domain.model.storage.Base64File;
import br.com.p8projects.filehub.domain.model.storage.EnumStorageType;
import br.com.p8projects.filehub.domain.model.storage.google.GoogleCloudProperties;
import com.google.api.gax.paging.Page;
import com.google.auth.Credentials;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.WriteChannel;
import com.google.cloud.storage.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.Channels;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class GoogleCloudStorage extends FhStorage<GoogleCloudProperties> {


    public GoogleCloudStorage(String id, EnumStorageType type, GoogleCloudProperties properties) {
        super(id, type, properties);
    }

    // Directory Operations

    @Override
    public boolean createDirectory(String directory) {
        Storage googleStorage = getStorage();
        String path = formatDirPath(directory);
        BlobInfo newDirectory = BlobInfo.newBuilder(properties.getBucket(), path).build();
        return googleStorage.create(newDirectory) != null;
    }

    @Override
    public boolean renameDirectory(String path, String name) {
        Storage googleStorage = getStorage();
        String pathDir = formatDirPath(path);

        int lastSlash = pathDir.substring(0, pathDir.length()-1).lastIndexOf("/");
        String newPath = (lastSlash != -1) ? pathDir.substring(0, lastSlash) + name : name;
        newPath += "/";

        Page<Blob> blobs = googleStorage.list(properties.getBucket(), Storage.BlobListOption.currentDirectory(), Storage.BlobListOption.prefix(pathDir));
        Iterable<Blob> blobIterator = blobs.iterateAll();
        Iterator<Blob> iterator = blobIterator.iterator();
        while(iterator.hasNext()) {
            BlobId source = iterator.next().getBlobId();
            String itemName = source.getName().replace(pathDir, "");
            BlobId target = BlobId.of(properties.getBucket(), newPath + itemName);
            Storage.BlobTargetOption precondition = Storage.BlobTargetOption.doesNotExist();
            googleStorage.copy(Storage.CopyRequest.newBuilder().setSource(source).setTarget(target, precondition).build());
            googleStorage.get(source).delete();
        }
        return true;
    }

    @Override
    public boolean deleteDirectory(String path, boolean isRecursive) {
        boolean result = true;
        Storage googleStorage = getStorage();
        String dirPath = formatDirPath(path);
        Page<Blob> blobs = googleStorage.list(properties.getBucket(), Storage.BlobListOption.prefix(dirPath));
        Iterable<Blob> blobIterator = blobs.iterateAll();
        Iterator<Blob> iterator = blobIterator.iterator();
        Blob firstBlob = iterator.hasNext() ? iterator.next() : null;
        if(firstBlob != null) {
            if (!isRecursive && iterator.hasNext()) {
                throw new StorageException("Directory not empty");
            }
            while(iterator.hasNext()) {
                iterator.next().delete();
            }
            result = firstBlob.delete();
        }
        return result;
    }

    @Override
    public List<FileItem> listFiles(String path) {
        Storage googleStorage = getStorage();
        String pathDir = formatDirPath(path);
        Page<Blob> blobs = googleStorage.list(properties.getBucket(), Storage.BlobListOption.prefix(pathDir), Storage.BlobListOption.currentDirectory());
        Iterable<Blob> blobIterator = blobs.iterateAll();
        List<FileItem> list = new ArrayList<>();
        blobIterator.forEach(blob -> {
            String itemPath = blob.getName();
            if("".equals(pathDir) || !pathDir.equals(itemPath)) {
                String itemName = blob.getName().replace(pathDir, "");
                list.add(new FileItem(itemPath, itemName, blob.isDirectory(), blob.getContentType(), blob.getSize()));
            }
        });
        return list;
    }

    @Override
    public boolean existsDirectory(String path) {
        Storage googleStorage = getStorage();
        String pathDir = formatDirPath(path);
        Page<Blob> blobs = googleStorage.list(properties.getBucket(), Storage.BlobListOption.prefix(pathDir), Storage.BlobListOption.currentDirectory());
        return blobs.getValues().iterator().hasNext();
    }


    // Files Operations

    @Override
    public void upload(FileLocation fileLocation, MultipartFile file, Boolean mkdir) {
        Storage googleStorage = getStorage();
        String pathDir = formatDirPath(fileLocation.getPath());
        checkIfFolderExists(googleStorage, pathDir, mkdir);
        executeUploadMultipart(googleStorage, file, pathDir, fileLocation.getFilename());
    }

    @Override
    public void upload(String path, MultipartFile[] files, Boolean mkdir) {
        Storage googleStorage = getStorage();
        String pathDir = formatDirPath(path);
        checkIfFolderExists(googleStorage, pathDir, mkdir);
        for(MultipartFile file : files) {
            executeUploadMultipart(googleStorage, file, pathDir, file.getOriginalFilename());
        }
    }

    private void executeUploadMultipart(Storage googleStorage, MultipartFile file, String pathDir, String filename) {
        try(InputStream in = file.getInputStream()) {
            Storage.BlobTargetOption precondition = Storage.BlobTargetOption.doesNotExist();
            BlobId blobId = BlobId.of(properties.getBucket(), pathDir + filename);
            BlobInfo blobInfo = BlobInfo.newBuilder(blobId).build();
            googleStorage.create(blobInfo, in.readAllBytes(), precondition);
        } catch (IOException e) {
            throw new UploadException("Error to upload the file " + file.getName(), e);
        }
    }


    @Override
    public void uploadBase64(FileLocation fileLocation, Base64Upload file, Boolean mkdir) {
        Storage googleStorage = getStorage();
        String pathDir = formatDirPath(fileLocation.getPath());
        checkIfFolderExists(googleStorage, pathDir, mkdir);
        executeUploadBase64(googleStorage, pathDir, file);
    }

    @Override
    public void uploadBase64(String path, Base64Upload[] files, Boolean mkdir) {
        Storage googleStorage = getStorage();
        String pathDir = formatDirPath(path);
        checkIfFolderExists(googleStorage, pathDir, mkdir);
        for(Base64Upload file : files) {
            executeUploadBase64(googleStorage, pathDir, file);
        }
    }

    private void executeUploadBase64(Storage googleStorage, String path, Base64Upload file) {
        Base64File base64File = file.getBase64();
        byte[] bI = Base64.getDecoder().decode(base64File.getFile().getBytes(StandardCharsets.UTF_8));
        try(InputStream in = new ByteArrayInputStream(bI)) {
            Storage.BlobTargetOption precondition = Storage.BlobTargetOption.doesNotExist();
            BlobId blobId = BlobId.of(properties.getBucket(), path + file.getFilename());
            BlobInfo blobInfo = BlobInfo.newBuilder(blobId).setContentType(base64File.getMimeType()).build();
            googleStorage.create(blobInfo, in.readAllBytes(), precondition);
        } catch (IOException e) {
            throw new UploadException("Error to upload the file " + file.getFilename(), e);
        }
    }


    @Override
    public OutputStream getOutputStreamFromStorage(String path, String filename, Boolean mkdir) {
        Storage googleStorage = getStorage();
        String pathDir = formatDirPath(path);
        checkIfFolderExists(googleStorage, pathDir, mkdir);

        BlobId blobId = BlobId.of(properties.getBucket(), pathDir + filename);
        BlobInfo blobInfo = BlobInfo.newBuilder(blobId).build();

        Blob output = googleStorage.create(blobInfo);
        WriteChannel writeChannel = output.writer();
        return Channels.newOutputStream(writeChannel);
    }

    @Override
    public void transfer(FhStorage destination, FileLocation fileLocation, Boolean mkdir) {
        Storage googleStorage = getStorage();
        String filePath = formatDirPath(fileLocation.getPath());
        executeTransfer(googleStorage, destination, fileLocation.getPath(), filePath, fileLocation.getFilename(), mkdir);
    }

    @Override
    public void transfer(FhStorage destination, String pathDir, List<String> filenames, Boolean mkdir) {
        Storage googleStorage = getStorage();
        String filePath = formatDirPath(pathDir);
        for(String filename : filenames) {
            executeTransfer(googleStorage, destination, pathDir, filePath, filename, mkdir);
        }
    }

    private void executeTransfer(Storage googleStorage, FhStorage destination, String pathDir, String filePath, String filename, boolean mkdir) {
        int readByteCount;
        byte[] buffer = new byte[4096];
        BlobId blobId = BlobId.of(properties.getBucket(), filePath);
        BlobInfo blobInfo = BlobInfo.newBuilder(blobId).build();
        Blob newBlob = googleStorage.create(blobInfo);

        try(InputStream in = Channels.newInputStream(newBlob.reader());
            OutputStream out = destination.getOutputStreamFromStorage(pathDir, filename, mkdir)) {
            while((readByteCount = in.read(buffer)) != -1) {
                out.write(buffer, 0, readByteCount);
            }
        } catch (IOException e) {
            throw new RuntimeException("Error to transfer the file " + filename, e);
        }
    }


    @Override
    public boolean delete(String path) {
        Storage googleStorage = getStorage();
        BlobId blobId = BlobId.of(properties.getBucket(), path);
        return googleStorage.delete(blobId);
    }

    @Override
    public boolean existsFile(String path) {
        Storage googleStorage = getStorage();
        BlobId blobId = BlobId.of(properties.getBucket(), path);
        return googleStorage.get(blobId) != null;
    }

    @Override
    public FileMetadata getFileDetails(String path) {
        Storage googleStorage = getStorage();
        BlobId blobId = BlobId.of(properties.getBucket(), path);
        Blob blob = googleStorage.get(blobId);
        FileMetadata fileMetadata = new FileMetadata();
        fileMetadata.setContentType(blob.getContentType());
        fileMetadata.setSize(blob.getSize());
        fileMetadata.setLastModified(Date.from(blob.getUpdateTimeOffsetDateTime().toInstant()));
        return fileMetadata;
    }

    @Override
    public InputStream downloadFile(String path) {
        Storage googleStorage = getStorage();
        String pathDir = formatDirPath(path);
        BlobId blobId = BlobId.of(properties.getBucket(), pathDir);
        return Channels.newInputStream(googleStorage.get(blobId).reader());
    }



    private Storage getStorage() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, 1);
        Credentials credentials = null;
        try {
            byte[] byteArray = properties.getJsonCredentials().getBytes("UTF-8");
            ByteArrayInputStream inputStream = new ByteArrayInputStream(byteArray);
            credentials = GoogleCredentials.fromStream(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return StorageOptions.newBuilder().setCredentials(credentials).build().getService();
    }

    private String formatDirPath(String path) {
        if(path.startsWith("/")) {
            path = path.substring(1);
        }
        if(!path.isEmpty() && !path.endsWith("/")) {
            path += "/";
        }
        return properties.getBaseDir() + path;
    }

    private void checkIfFolderExists(Storage storage, String path, Boolean mkdir) {
        Page<Blob> blobs = storage.list(properties.getBucket(), Storage.BlobListOption.prefix(path), Storage.BlobListOption.currentDirectory());
        if(!blobs.getValues().iterator().hasNext()) {
            if(!mkdir) {
                throw new StorageException("Directory not found: " + path);
            }
            BlobInfo newDirectory = BlobInfo.newBuilder(properties.getBucket(), path).build();
            storage.create(newDirectory);
        }
    }

}
