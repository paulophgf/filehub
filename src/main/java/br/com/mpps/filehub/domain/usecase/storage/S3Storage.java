package br.com.mpps.filehub.domain.usecase.storage;

import br.com.mpps.filehub.domain.exceptions.DownloadException;
import br.com.mpps.filehub.domain.exceptions.StorageException;
import br.com.mpps.filehub.domain.exceptions.UploadException;
import br.com.mpps.filehub.domain.model.Base64Upload;
import br.com.mpps.filehub.domain.model.FileItem;
import br.com.mpps.filehub.domain.model.FileLocation;
import br.com.mpps.filehub.domain.model.FileMetadata;
import br.com.mpps.filehub.domain.model.config.Storage;
import br.com.mpps.filehub.domain.model.storage.Base64File;
import br.com.mpps.filehub.domain.model.storage.EnumStorageType;
import br.com.mpps.filehub.domain.model.storage.s3.S3OutputStream;
import br.com.mpps.filehub.domain.model.storage.s3.S3Properties;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

public class S3Storage extends Storage<S3Properties> {

    public S3Storage(String id, EnumStorageType type, S3Properties properties) {
        super(id, type, properties);
    }

    // Directory Operations

    @Override
    public boolean createDirectory(String directory) {
        AmazonS3 s3Client = authorizeOnS3();
        String pathDir = S3Storage.formatDirPathToS3(directory);
        if(!s3Client.doesObjectExist(properties.getBucket(), pathDir)) {
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(0);
            InputStream emptyContent = new ByteArrayInputStream(new byte[0]);
            PutObjectRequest putObjectRequest = new PutObjectRequest(properties.getBucket(), pathDir, emptyContent, metadata);
            s3Client.putObject(putObjectRequest);
        }
        return true;
    }

    @Override
    public boolean deleteDirectory(String path, boolean isRecursive) {
        String pathDir = S3Storage.formatDirPathToS3(path);
        AmazonS3 s3Client = authorizeOnS3();
        ObjectListing listing = s3Client.listObjects(properties.getBucket(), pathDir);
        if (!listing.getCommonPrefixes().isEmpty()) {
            if (!isRecursive) {
                throw new StorageException("Directory not empty");
            }
            for (String filePath : listing.getCommonPrefixes()) {
                s3Client.deleteObject(new DeleteObjectRequest(properties.getBucket(), filePath));
            }
        }
        return true;
    }

    @Override
    public List<FileItem> listFiles(String path) {
        String pathDir = S3Storage.formatDirPathToS3(path);
        List<FileItem> fileItems = null;
        AmazonS3 s3Client = authorizeOnS3();
        ObjectListing listing = s3Client.listObjects(properties.getBucket(), pathDir);
        List<S3ObjectSummary> s3Objects = listing.getObjectSummaries();
        if(s3Objects != null && !s3Objects.isEmpty()) {
            fileItems = new ArrayList<>();
            for (S3ObjectSummary file : s3Objects) {
                String fileName = file.getKey().substring(pathDir.length());
                String mimeType = s3Client.getObjectMetadata(properties.getBucket(), file.getKey()).getContentType();
                fileItems.add(new FileItem(pathDir, fileName, file.getKey().endsWith("/"), mimeType, file.getSize()));
            }
        }
        return fileItems;
    }

    @Override
    public boolean existsDirectory(String path) {
        AmazonS3 s3Client = authorizeOnS3();
        String pathDir = S3Storage.formatDirPathToS3(path);
        return s3Client.doesObjectExist(properties.getBucket(), pathDir);
    }


    // Files Operations

    @Override
    public void upload(FileLocation fileLocation, MultipartFile file, Boolean mkdir) {
        AmazonS3 s3Client = authorizeOnS3();
        String pathDir = S3Storage.formatDirPathToS3(fileLocation.getPath());
        checkIfFolderExists(s3Client, pathDir, mkdir);
        executeUploadMultipart(s3Client, file, pathDir, fileLocation.getFilename());
    }

    @Override
    public void upload(String path, MultipartFile[] files, Boolean mkdir) {
        AmazonS3 s3Client = authorizeOnS3();
        String pathDir = S3Storage.formatDirPathToS3(path);
        checkIfFolderExists(s3Client, pathDir, mkdir);
        for(MultipartFile file : files) {
            executeUploadMultipart(s3Client, file, pathDir, file.getOriginalFilename());
        }
    }

    private void executeUploadMultipart(AmazonS3 s3Client, MultipartFile file, String pathDir, String filename) {
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType(file.getContentType());
        metadata.setContentLength(file.getSize());
        try(InputStream in = file.getInputStream()) {
            String keyfile = pathDir + filename;
            PutObjectRequest putObjectRequest = new PutObjectRequest(properties.getBucket(), keyfile, in, metadata);
            s3Client.putObject(putObjectRequest);
        } catch (IOException e) {
            throw new UploadException("Error to upload the file " + file.getName(), e);
        }
    }


    @Override
    public void uploadBase64(FileLocation fileLocation, Base64Upload file, Boolean mkdir) {
        AmazonS3 s3Client = authorizeOnS3();
        String pathDir = S3Storage.formatDirPathToS3(fileLocation.getPath());
        checkIfFolderExists(s3Client, pathDir, mkdir);
        executeUploadBase64(s3Client, file, fileLocation.getFilename());
    }

    @Override
    public void uploadBase64(String path, Base64Upload[] files, Boolean mkdir) {
        AmazonS3 s3Client = authorizeOnS3();
        String pathDir = S3Storage.formatDirPathToS3(path);
        checkIfFolderExists(s3Client, pathDir, mkdir);
        for(Base64Upload file : files) {
            executeUploadBase64(s3Client, file, file.getFilename());
        }
    }

    private void executeUploadBase64(AmazonS3 s3Client, Base64Upload file, String filename) {
        Base64File base64File = file.getBase64();
        byte[] bI = Base64.getDecoder().decode(base64File.getFile().getBytes(StandardCharsets.UTF_8));
        InputStream fis = new ByteArrayInputStream(bI);
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType(base64File.getMimeType());
        metadata.setContentLength(bI.length);
        s3Client.putObject(properties.getBucket(), filename, fis, metadata);
    }


    @Override
    public OutputStream getOutputStreamFromStorage(String path, String filename, Boolean mkdir) {
        AmazonS3 s3Client = authorizeOnS3();
        String pathDir = S3Storage.formatDirPathToS3(path);
        checkIfFolderExists(s3Client, pathDir, mkdir);
        String keyfile = pathDir + filename;
        return new S3OutputStream(s3Client, properties.getBucket(), keyfile);
    }


    @Override
    public void transfer(Storage destination, String pathDir, List<String> filenames, Boolean mkdir) {
        AmazonS3 s3Client = authorizeOnS3();
        if(!s3Client.doesBucketExist(properties.getBucket())) {
            throw new DownloadException("AWS S3 Bucket not found");
        }
        String filePath = formatDirPathToS3(pathDir);
        for(String filename : filenames) {
            executeTransfer(s3Client, destination, pathDir, filePath, filename, mkdir);
        }
    }

    @Override
    public void transfer(Storage destination, FileLocation fileLocation, Boolean mkdir) {
        AmazonS3 s3Client = authorizeOnS3();
        if(!s3Client.doesBucketExist(properties.getBucket())) {
            throw new DownloadException("AWS S3 Bucket not found");
        }
        String filePath = formatDirPathToS3(fileLocation.getPath());
        executeTransfer(s3Client, destination, fileLocation.getPath(), filePath, fileLocation.getFilename(), mkdir);
    }

    private void executeTransfer(AmazonS3 s3Client, Storage destination, String pathDir, String filePath, String filename, boolean mkdir) {
        int readByteCount;
        byte[] buffer = new byte[4096];
        S3Object fileObject = s3Client.getObject(properties.getBucket(), filePath);
        try(InputStream in = fileObject.getObjectContent();
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
        AmazonS3 s3Client = authorizeOnS3();
        String filePath = formatFilePathToS3(path);
        return s3Client.doesObjectExist(properties.getBucket(), filePath);
    }

    @Override
    public FileMetadata getFileDetails(String path) {
        AmazonS3 s3Client = authorizeOnS3();
        String filePath = formatFilePathToS3(path);
        ObjectMetadata metadata = s3Client.getObjectMetadata(properties.getBucket(), filePath);
        FileMetadata fileMetadata = new FileMetadata();
        fileMetadata.setContentType(metadata.getContentType());
        fileMetadata.setSize(metadata.getContentLength());
        fileMetadata.setLastModified(metadata.getLastModified());
        return fileMetadata;
    }

    @Override
    public boolean delete(String path) {
        String filePath = formatFilePathToS3(path);
        AmazonS3 s3Client = authorizeOnS3();
        ObjectListing listing = s3Client.listObjects(properties.getBucket(), filePath);
        if (listing.getCommonPrefixes().isEmpty()) {
            s3Client.deleteObject(new DeleteObjectRequest(properties.getBucket(), filePath));
        }
        return true;
    }



    @Override
    public InputStream downloadFile(String path) {
        AmazonS3 s3Client = authorizeOnS3();
        if(!s3Client.doesBucketExist(properties.getBucket())) {
            throw new DownloadException("AWS S3 Bucket not found");
        }
        String filePath = formatFilePathToS3(path);
        S3Object fileObject = s3Client.getObject(properties.getBucket(), filePath);
        return fileObject.getObjectContent();
    }


    private AmazonS3 authorizeOnS3() {
        BasicAWSCredentials awsCredentials = new BasicAWSCredentials(properties.getSecretKeyId(), properties.getSecretKey());
        final AmazonS3 s3Client = AmazonS3ClientBuilder.standard()
                .withCredentials(new AWSStaticCredentialsProvider(awsCredentials))
                .withRegion(properties.getRegion())
                .build();
        if (!s3Client.doesBucketExist(properties.getBucket())) {
            throw new RuntimeException("O bucket " + properties.getBucket() + " não existe.");
        }
        return s3Client;
    }

    // Remember that in S3 no have folders, just keys in the bucket
    private void checkIfFolderExists(AmazonS3 s3Client, String filepath, Boolean mkdir) {
        ObjectListing listing = s3Client.listObjects(properties.getBucket(), filepath);
        if(listing.getObjectSummaries().isEmpty()) {
            if(!mkdir) {
                throw new StorageException("Directory not found: " + filepath);
            }
            createDirectory(filepath);
        }
    }

    private static String formatDirPathToS3(String path) {
        path = formatFilePathToS3(path);
        if(!path.endsWith("/")) {
            path += "/";
        }
        return path;
    }

    private static String formatFilePathToS3(String path) {
        if(path.startsWith("/")) {
            path = path.substring(1);
        }
        return path;
    }

}
