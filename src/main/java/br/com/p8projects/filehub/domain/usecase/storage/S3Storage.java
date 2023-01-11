package br.com.p8projects.filehub.domain.usecase.storage;

import br.com.p8projects.filehub.domain.exceptions.NotFoundException;
import br.com.p8projects.filehub.domain.exceptions.StorageException;
import br.com.p8projects.filehub.domain.exceptions.UploadException;
import br.com.p8projects.filehub.domain.model.*;
import br.com.p8projects.filehub.domain.model.config.FhStorage;
import br.com.p8projects.filehub.domain.model.storage.Base64File;
import br.com.p8projects.filehub.domain.model.storage.s3.S3OutputStream;
import br.com.p8projects.filehub.domain.model.storage.s3.S3Properties;
import br.com.p8projects.filehub.domain.model.upload.Base64Upload;
import br.com.p8projects.filehub.domain.model.upload.UploadBase64Object;
import br.com.p8projects.filehub.domain.model.upload.UploadMultipartObject;
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
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

public class S3Storage extends FhStorage<S3Properties> {

    public S3Storage(String id, S3Properties properties) {
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
        AmazonS3 s3Client = authorizeOnS3();
        String pathDir = properties.formatDirPath(directory);
        return createDirectory(s3Client, pathDir);
    }

    @Override
    public boolean renameDirectory(String path, String name) {
        AmazonS3 s3Client = authorizeOnS3();
        String pathDir = properties.formatDirPath(path);
        checkIfDirectoryExists(s3Client, pathDir);
        Path dirPath = Paths.get(pathDir.substring(0, pathDir.length()-1));
        String newPath = (dirPath.getParent() == null ? "" : dirPath.getParent() + "/") + name;
        newPath = newPath.endsWith("/") ? newPath : newPath + "/";
        if(!pathDir.equals(newPath)) {
            newPath = newPath.replace("\\", "/");
            copy(s3Client, pathDir, newPath);
            deleteDirectory(path, true);
        }
        return true;
    }

    @Override
    public boolean deleteDirectory(String path, boolean isRecursive) {
        AmazonS3 s3Client = authorizeOnS3();
        String pathDir = properties.formatDirPath(path);
        checkIfDirectoryExists(s3Client, pathDir);
        ObjectListing listing = s3Client.listObjects(properties.getBucket(), pathDir);
        if (!isRecursive && listing.getObjectSummaries().size() > 1) {
            throw new StorageException("Directory not empty");
        }
        for (S3ObjectSummary filePath : listing.getObjectSummaries()) {
            s3Client.deleteObject(new DeleteObjectRequest(properties.getBucket(), filePath.getKey()));
        }
        return true;
    }

    @Override
    public List<FileItem> listFiles(String path) {
        AmazonS3 s3Client = authorizeOnS3();
        String pathDir = properties.formatDirPath(path);
        if(!"/".equals(pathDir)) {
            checkIfDirectoryExists(s3Client, pathDir);
        } else {
            pathDir = "";
        }
        List<FileItem> fileItems = new ArrayList<>();
        ListObjectsV2Request request = new ListObjectsV2Request()
                .withBucketName(properties.getBucket()).withDelimiter("/").withPrefix(pathDir);
        ListObjectsV2Result listing = s3Client.listObjectsV2(request);
        List<S3ObjectSummary> dirObjects = listing.getObjectSummaries();
        List<String> filesObjects = listing.getCommonPrefixes();
        String filePath = pathDir.replace(properties.getBaseDir(), "");
        for (String file : filesObjects) {
            String fileName = file.substring(pathDir.length());
            ObjectMetadata metadata = s3Client.getObjectMetadata(properties.getBucket(), file);
            fileItems.add(new FileItem(filePath, fileName, file.endsWith("/"), metadata.getContentType(), metadata.getContentLength()));
        }
        for (S3ObjectSummary file : dirObjects) {
            if (!file.getKey().equals(pathDir)) {
                String fileName = file.getKey().substring(pathDir.length());
                String mimeType = s3Client.getObjectMetadata(properties.getBucket(), file.getKey()).getContentType();
                fileItems.add(new FileItem(filePath, fileName, file.getKey().endsWith("/"), mimeType, file.getSize()));
            }
        }
        return fileItems;
    }

    @Override
    public boolean existsDirectory(String path) {
        AmazonS3 s3Client = authorizeOnS3();
        String pathDir = properties.formatDirPath(path);
        return existDirectory(s3Client, pathDir);
    }


    // Files Operations


    @Override
    public void upload(UploadMultipartObject uploadMultipartObject) {
        AmazonS3 s3Client = authorizeOnS3();
        String pathDir = properties.formatUploadFilePath(uploadMultipartObject.getPath());
        checkIfFolderExists(s3Client, pathDir, uploadMultipartObject.isMkdir());
        for(UploadMultipartObject.FileUploadObject file : uploadMultipartObject.getFiles()) {
            executeUploadMultipart(s3Client, file.getFile(), pathDir, file.getFilename());
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
    public void uploadBase64(UploadBase64Object uploadBase64Object) {
        AmazonS3 s3Client = authorizeOnS3();
        String pathDir = properties.formatUploadFilePath(uploadBase64Object.getPath());
        checkIfFolderExists(s3Client, pathDir, uploadBase64Object.isMkdir());
        for(Base64Upload file : uploadBase64Object.getFiles()) {
            executeUploadBase64(s3Client, pathDir, file, file.getFilename());
        }
    }

    private void executeUploadBase64(AmazonS3 s3Client, String pathDir, Base64Upload file, String filename) {
        Base64File base64File = file.getBase64();
        byte[] bI = Base64.getDecoder().decode(base64File.getFile().getBytes(StandardCharsets.UTF_8));
        InputStream fis = new ByteArrayInputStream(bI);
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType(base64File.getMimeType());
        metadata.setContentLength(bI.length);
        s3Client.putObject(properties.getBucket(), pathDir + filename, fis, metadata);
    }


    @Override
    public OutputStream getOutputStreamFromStorage(String path, String filename, Boolean mkdir) {
        AmazonS3 s3Client = authorizeOnS3();
        String pathDir = properties.formatUploadFilePath(path);
        checkIfFolderExists(s3Client, pathDir, mkdir);
        String keyfile = pathDir + filename;
        return new S3OutputStream(s3Client, properties.getBucket(), keyfile);
    }


    @Override
    public void transfer(FhStorage destination, UploadMultipartObject uploadMultipartObject) {
        AmazonS3 s3Client = authorizeOnS3();
        String filePath = properties.formatUploadFilePath(uploadMultipartObject.getPath());
        for(UploadMultipartObject.FileUploadObject file : uploadMultipartObject.getFiles()) {
            executeTransfer(s3Client, destination, uploadMultipartObject.getPath(), filePath, file.getFilename(), uploadMultipartObject.isMkdir());
        }
    }

    private void executeTransfer(AmazonS3 s3Client, FhStorage destination, String pathDir, String filePath, String filename, boolean mkdir) {
        int readByteCount;
        byte[] buffer = new byte[4096];
        S3Object fileObject = s3Client.getObject(properties.getBucket(), filePath + filename);
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
        String filePath = properties.formatFilePath(path);
        return s3Client.doesObjectExist(properties.getBucket(), filePath);
    }

    @Override
    public FileMetadata getFileDetails(String path) {
        AmazonS3 s3Client = authorizeOnS3();
        String filePath = properties.formatFilePath(path);
        ObjectMetadata metadata = s3Client.getObjectMetadata(properties.getBucket(), filePath);
        FileMetadata fileMetadata = new FileMetadata();
        fileMetadata.setContentType(metadata.getContentType());
        fileMetadata.setSize(metadata.getContentLength());
        fileMetadata.setLastModified(metadata.getLastModified());
        return fileMetadata;
    }

    @Override
    public boolean delete(String path) {
        AmazonS3 s3Client = authorizeOnS3();
        String filePath = properties.formatFilePath(path);
        checkIfFileExists(s3Client, filePath);
        ObjectListing listing = s3Client.listObjects(properties.getBucket(), filePath);
        if (listing.getCommonPrefixes().isEmpty()) {
            s3Client.deleteObject(new DeleteObjectRequest(properties.getBucket(), filePath));
        }
        return true;
    }



    @Override
    public InputStream downloadFile(String path) {
        AmazonS3 s3Client = authorizeOnS3();
        String filePath = properties.formatFilePath(path);
        S3Object fileObject = s3Client.getObject(properties.getBucket(), filePath);
        return fileObject.getObjectContent();
    }


    private AmazonS3 authorizeOnS3() {
        BasicAWSCredentials awsCredentials = new BasicAWSCredentials(properties.getSecretKeyId(), properties.getSecretKey());
        final AmazonS3 s3Client = AmazonS3ClientBuilder.standard()
                .withCredentials(new AWSStaticCredentialsProvider(awsCredentials))
                .withRegion(properties.getRegion())
                .build();
        if (!s3Client.doesBucketExistV2(properties.getBucket())) {
            throw new RuntimeException("AWS S3 Bucket not found: " + properties.getBucket());
        }
        return s3Client;
    }

    private void checkIfDirectoryExists(AmazonS3 s3Client, String pathDir) {
        if(!existDirectory(s3Client, pathDir)) {
            throw new NotFoundException("Directory not found");
        }
    }

    private boolean existDirectory(AmazonS3 s3Client, String pathDir) {
        boolean exists;
        try {
            S3Object object = s3Client.getObject(properties.getBucket(), pathDir);
            exists = object.getKey().endsWith("/");
        } catch (AmazonS3Exception e) {
            if(e.getStatusCode() != 404) {
                e.printStackTrace();
                throw new RuntimeException("Error to find the object in S3");
            }
            exists = false;
        }
        return exists;
    }

    private void checkIfFileExists(AmazonS3 s3Client, String filePath) {
        if(!s3Client.doesObjectExist(properties.getBucket(), filePath)) {
            throw new NotFoundException("File not found");
        }
    }

    private void copy(AmazonS3 s3Client, String pathDir, String newPath) {
        ListObjectsV2Result listing = s3Client.listObjectsV2(properties.getBucket(), pathDir);
        for (S3ObjectSummary filePath : listing.getObjectSummaries()) {
            String newSubPath = filePath.getKey().replace(pathDir, newPath);
            s3Client.copyObject(properties.getBucket(), filePath.getKey(), properties.getBucket(), newSubPath);
        }
    }


    // Remember that in S3 no have folders, just keys in the bucket
    private void checkIfFolderExists(AmazonS3 s3Client, String filepath, Boolean mkdir) {
        if(!"".equals(filepath)) {
            ObjectListing listing = s3Client.listObjects(properties.getBucket(), filepath);
            if (listing.getObjectSummaries().isEmpty()) {
                if (!mkdir) {
                    throw new StorageException("Directory not found: " + filepath);
                }
                createDirectory(s3Client, filepath);
            }
        }
    }

    private boolean createDirectory(AmazonS3 s3Client, String pathDir) {
        if(!s3Client.doesObjectExist(properties.getBucket(), pathDir)) {
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(0);
            InputStream emptyContent = new ByteArrayInputStream(new byte[0]);
            PutObjectRequest putObjectRequest = new PutObjectRequest(properties.getBucket(), pathDir, emptyContent, metadata);
            s3Client.putObject(putObjectRequest);
        }
        return true;
    }

}
