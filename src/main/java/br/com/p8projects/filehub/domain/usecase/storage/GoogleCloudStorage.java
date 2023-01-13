package br.com.p8projects.filehub.domain.usecase.storage;

import br.com.p8projects.filehub.domain.exceptions.NotFoundException;
import br.com.p8projects.filehub.domain.exceptions.StorageException;
import br.com.p8projects.filehub.domain.exceptions.UploadException;
import br.com.p8projects.filehub.domain.model.FileItem;
import br.com.p8projects.filehub.domain.model.FileMetadata;
import br.com.p8projects.filehub.domain.model.TransferFileObject;
import br.com.p8projects.filehub.domain.model.config.FhStorage;
import br.com.p8projects.filehub.domain.model.storage.Base64File;
import br.com.p8projects.filehub.domain.model.storage.google.GoogleCloudProperties;
import br.com.p8projects.filehub.domain.model.upload.Base64Upload;
import br.com.p8projects.filehub.domain.model.upload.UploadBase64Object;
import br.com.p8projects.filehub.domain.model.upload.UploadMultipartObject;
import br.com.p8projects.filehub.domain.model.upload.UploadObject;
import br.com.p8projects.filehub.system.FilePathUtils;
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


    public GoogleCloudStorage(String id, GoogleCloudProperties properties) {
        super(id, properties);
    }

    // Directory Operations

    @Override
    public void createBaseDirIfNotExist() {
        if(!"".equals(properties.getBaseDir())) {
            createDirectory("");
        }
    }

    @Override
    public boolean createDirectory(String directory) {
        Storage googleStorage = getStorage();
        String path = properties.formatDirPath(directory);
        if(!existsDirectory(directory)) {
            BlobInfo newDirectory = BlobInfo.newBuilder(properties.getBucket(), path).build();
            googleStorage.create(newDirectory);
        }
        return true;
    }

    @Override
    public boolean renameDirectory(String path, String name) {
        Storage googleStorage = getStorage();
        String pathDir = properties.formatDirPath(path);
        checkIfDirectoryExists(googleStorage, pathDir);
        String newPath = FilePathUtils.getNewPathDirectoryRename(pathDir, name);

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
        String dirPath = properties.formatDirPath(path);
        checkIfDirectoryExists(googleStorage, dirPath);
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
        String pathDir = properties.formatDirPath(path);
        checkIfDirectoryExists(googleStorage, pathDir);
        Page<Blob> blobs = googleStorage.list(properties.getBucket(), Storage.BlobListOption.prefix(pathDir), Storage.BlobListOption.currentDirectory());
        Iterable<Blob> blobIterator = blobs.iterateAll();
        List<FileItem> list = new ArrayList<>();
        blobIterator.forEach(blob -> {
            String itemPath = blob.getName();
            if("".equals(pathDir) || !pathDir.equals(itemPath)) {
                String itemName = blob.getName().replace(pathDir, "");
                itemPath = itemPath.replace(properties.getBaseDir(), "");
                list.add(new FileItem(itemPath, itemName, blob.isDirectory(), blob.getContentType(), blob.getSize()));
            }
        });
        return list;
    }

    @Override
    public boolean existsDirectory(String path) {
        Storage googleStorage = getStorage();
        String pathDir = properties.formatDirPath(path);
        Page<Blob> blobs = googleStorage.list(properties.getBucket(), Storage.BlobListOption.prefix(pathDir), Storage.BlobListOption.currentDirectory());
        return blobs.getValues().iterator().hasNext();
    }

    private void checkIfDirectoryExists(Storage googleStorage, String pathDir) {
        Page<Blob> blobs = googleStorage.list(properties.getBucket(), Storage.BlobListOption.prefix(pathDir), Storage.BlobListOption.currentDirectory());
        if(!blobs.getValues().iterator().hasNext()) {
            throw new NotFoundException("Directory not found");
        }
    }


    // Files Operations


    @Override
    public void upload(UploadMultipartObject uploadMultipartObject) {
        Storage googleStorage = getStorage();
        String pathDir = properties.formatDirPath(uploadMultipartObject.getPath());
        checkIfFolderExists(googleStorage, pathDir, uploadMultipartObject.isMkdir());
        for(UploadMultipartObject.FileUploadObject file : uploadMultipartObject.getFiles()) {
            executeUploadMultipart(googleStorage, file.getFile(), pathDir, file.getFilename());
        }
    }

    private void executeUploadMultipart(Storage googleStorage, MultipartFile file, String pathDir, String filename) {
        try(InputStream in = file.getInputStream()) {
            Storage.BlobTargetOption precondition = Storage.BlobTargetOption.doesNotExist();
            BlobId blobId = BlobId.of(properties.getBucket(), pathDir + filename);
            BlobInfo blobInfo = BlobInfo.newBuilder(blobId).setContentType(file.getContentType()).build();
            googleStorage.create(blobInfo, in.readAllBytes(), precondition);
        } catch (IOException e) {
            throw new UploadException("Error to upload the file " + file.getName(), e);
        }
    }


    @Override
    public void uploadBase64(UploadBase64Object uploadBase64Object) {
        Storage googleStorage = getStorage();
        String pathDir = properties.formatDirPath(uploadBase64Object.getPath());
        checkIfFolderExists(googleStorage, pathDir, uploadBase64Object.isMkdir());
        for(Base64Upload file : uploadBase64Object.getFiles()) {
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
    public OutputStream getOutputStreamFromStorage(UploadObject uploadObject, String filename, String contentType) {
        Storage googleStorage = getStorage();
        String pathDir = properties.formatDirPath(uploadObject.getPath());
        checkIfFolderExists(googleStorage, pathDir, uploadObject.isMkdir());

        BlobId blobId = BlobId.of(properties.getBucket(), pathDir + filename);
        BlobInfo blobInfo = BlobInfo.newBuilder(blobId).setContentType(contentType).build();

        Blob output = googleStorage.create(blobInfo);
        WriteChannel writeChannel = output.writer();
        return Channels.newOutputStream(writeChannel);
    }

    @Override
    public void writeFileInputStream(TransferFileObject transfer, boolean isMkdir) {
        Storage googleStorage = getStorage();
        String pathDir = properties.formatDirPath(transfer.getPath());
        checkIfFolderExists(googleStorage, pathDir, isMkdir);
        Storage.BlobTargetOption precondition = Storage.BlobTargetOption.doesNotExist();
        BlobId blobId = BlobId.of(properties.getBucket(), pathDir + transfer.getFilename());
        BlobInfo blobInfo = BlobInfo.newBuilder(blobId).setContentType(transfer.getContentType()).build();
        try(InputStream in = transfer.getInputStream()) {
            googleStorage.create(blobInfo, in.readAllBytes(), precondition);
        } catch (IOException e) {
            throw new UploadException("Error to upload the file " + transfer.getFilename(), e);
        }
    }

    @Override
    public TransferFileObject getTransferFileObject(String originalPath, String filename) {
        Storage googleStorage = getStorage();
        String path = properties.formatDirPath(originalPath);
        return getTransferFileObject(googleStorage, path, originalPath, filename);
    }

    private TransferFileObject getTransferFileObject(Storage googleStorage, String path, String originalPath, String filename) {
        BlobId blobId = BlobId.of(properties.getBucket(), path + filename);
        Blob newBlob = googleStorage.get(blobId);

        TransferFileObject transferFileObject = new TransferFileObject();
        transferFileObject.setPath(originalPath);
        transferFileObject.setFilename(filename);
        transferFileObject.setLenght(newBlob.getSize());
        transferFileObject.setContentType(newBlob.getContentType());
        transferFileObject.setInputStream(Channels.newInputStream(newBlob.reader()));
        return transferFileObject;
    }

    @Override
    public void transfer(FhStorage destination, UploadMultipartObject uploadMultipartObject) {
        Storage googleStorage = getStorage();
        String filePath = properties.formatDirPath(uploadMultipartObject.getPath());
        for(UploadMultipartObject.FileUploadObject file : uploadMultipartObject.getFiles()) {
            TransferFileObject transferFileObject = getTransferFileObject(googleStorage, filePath, uploadMultipartObject.getPath(), file.getFilename());
            destination.writeFileInputStream(transferFileObject, uploadMultipartObject.isMkdir());
        }
    }


    @Override
    public boolean delete(String path) {
        Storage googleStorage = getStorage();
        String filepath = properties.formatFilePath(path);
        checkIfFileExists(googleStorage, filepath);
        BlobId blobId = BlobId.of(properties.getBucket(), filepath);
        return googleStorage.delete(blobId);
    }

    @Override
    public boolean existsFile(String path) {
        Storage googleStorage = getStorage();
        String filepath = properties.formatFilePath(path);
        BlobId blobId = BlobId.of(properties.getBucket(), filepath);
        return googleStorage.get(blobId) != null;
    }

    @Override
    public FileMetadata getFileDetails(String path) {
        Storage googleStorage = getStorage();
        String filepath = properties.formatFilePath(path);
        BlobId blobId = BlobId.of(properties.getBucket(), filepath);
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
        String filepath = properties.formatFilePath(path);
        BlobId blobId = BlobId.of(properties.getBucket(), filepath);
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

    private void checkIfFolderExists(Storage storage, String path, Boolean mkdir) {
        if(!"".equals(path)) {
            Page<Blob> blobs = storage.list(properties.getBucket(), Storage.BlobListOption.prefix(path), Storage.BlobListOption.currentDirectory());
            if (!blobs.getValues().iterator().hasNext()) {
                if (!mkdir) {
                    throw new StorageException("Directory not found: " + path);
                }
                BlobInfo newDirectory = BlobInfo.newBuilder(properties.getBucket(), path).build();
                storage.create(newDirectory);
            }
        }
    }

    private void checkIfFileExists(Storage googleStorage, String filepath) {
        BlobId blobId = BlobId.of(properties.getBucket(), filepath);
        if(googleStorage.get(blobId) == null) {
            throw new NotFoundException("File not found");
        }
    }

}
