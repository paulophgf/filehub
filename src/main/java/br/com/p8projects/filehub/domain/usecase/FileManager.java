package br.com.p8projects.filehub.domain.usecase;

import br.com.p8projects.filehub.domain.model.FileMetadata;
import br.com.p8projects.filehub.domain.model.TransferFileObject;
import br.com.p8projects.filehub.domain.model.config.FhStorage;
import br.com.p8projects.filehub.domain.model.config.Schema;
import br.com.p8projects.filehub.domain.model.upload.UploadBase64Object;
import br.com.p8projects.filehub.domain.model.upload.UploadMultipartObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;

@Slf4j
@Service
public class FileManager {

    public void upload(UploadMultipartObject uploadMultipartObject) {
        log.info("UPLOAD MULTIPLE FILE USING MULTIPART FILE");
        if(uploadMultipartObject.getSchema().isParallelUpload()) {
            //uploadParallel(schema, path, files, mkdir);
        } else {
            uploadSequential(uploadMultipartObject);
        }
    }

    public void uploadBase64(UploadBase64Object uploadBase64Object) {
        log.info("UPLOAD MULTIPLE FILE USING BASE 64 FILE");
        Collection<FhStorage> storages = uploadBase64Object.getSchema().getStorages();
        for(FhStorage storage : storages) {
            storage.uploadBase64(uploadBase64Object);
        }
    }


    public boolean delete(Schema schema, String path) {
        Boolean result = true;
        Collection<FhStorage> storages = schema.getStorages();
        for(FhStorage storage : storages) {
            result = result && storage.delete(path);
        }
        return result;
    }


    public boolean existsFile(Schema schema, String filePath) {
        FhStorage storage = schema.getFirstUsefulStorage();
        return storage.existsFile(filePath);
    }


    public String getContentType(Schema schema, String filePath) {
        FhStorage storage = schema.getFirstUsefulStorage();
        return storage.getFileDetails(filePath).getContentType();
    }


    public FileMetadata getDetails(Schema schema, String filePath) {
        FhStorage storage = schema.getFirstUsefulStorage();
        return storage.getFileDetails(filePath);
    }


    public void downloadFile(Schema schema, String filePath, HttpServletResponse response) throws IOException {
        FhStorage storage;
        if(schema.isCacheEnabled()) {
            FhStorage cacheStorage = schema.getCacheStorage();
            if(cacheStorage.existsFile(filePath)) {
                copy(cacheStorage.downloadFile(filePath), response.getOutputStream());
            } else {
                FhStorage nextStorage = schema.nextStorageForCache();
                Path path = Paths.get(filePath);
                String fileName = path.getFileName().toString();
                String dirPath = path.getParent().toString().replace("\\", "/");
                TransferFileObject transferFileObject = nextStorage.getTransferFileObject(dirPath, fileName);
                cacheStorage.writeFileInputStream(transferFileObject, true);
                copy(nextStorage.downloadFile(filePath), response.getOutputStream());
            }
        } else {
            storage = schema.getFirstUsefulStorage();
            copy(storage.downloadFile(filePath), response.getOutputStream());
        }
    }

    public void copy(InputStream source, OutputStream... target) throws IOException {
        try {
            byte[] buf = new byte[8192];
            int length;
            while ((length = source.read(buf)) != -1) {
                for(OutputStream out : target) {
                    out.write(buf, 0, length);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Download problem");
        } finally {
            source.close();
            for(OutputStream out : target) {
                out.close();
            }
        }
    }



    private void uploadSequential(UploadMultipartObject uploadMultipartObject) {
        log.info("SEQUENTIAL");
        Collection<FhStorage> storages = uploadMultipartObject.getSchema().getStorages();
        if(uploadMultipartObject.getSchema().getMiddle() != null) {
            log.info("MIDDLE MODE");
            FhStorage middle = uploadMultipartObject.getSchema().getMiddle();
            middle.upload(uploadMultipartObject);
            boolean isTemp = !storages.contains(middle);
            new Thread(() -> {
                for (FhStorage storage : storages) {
                    if(!storage.equals(middle)) {
                        middle.transfer(storage, uploadMultipartObject);
                    }
                }
                if(isTemp) {
                    log.info("REMOVING FILES FROM TEMPORARY STORAGE");
                    for(UploadMultipartObject.FileUploadObject uploadObject : uploadMultipartObject.getFiles()) {
                        String filePath = uploadMultipartObject.getPath();
                        filePath += filePath.endsWith("/") ? uploadObject.getFilename() : "/" + uploadObject.getFilename();
                        log.info("Filepath = " + filePath);
                        middle.delete(filePath);
                    }
                }
            }).start();
        } else {
            for (FhStorage storage : storages) {
                storage.upload(uploadMultipartObject);
            }
        }
    }

//    private void uploadParallel(Schema schema, String path, MultipartFile[] files, Boolean mkdir) {
//        log.info("PARALLEL");
//        for(MultipartFile multipartFile : files) {
//            int readByteCount;
//            byte[] buffer = new byte[4096];
//            List<OutputStream> outputStreamList = openOutputStreamList(schema.getStorages(), path, multipartFile, mkdir);
//            try(InputStream in = multipartFile.getInputStream()) {
//                while((readByteCount = in.read(buffer)) != -1) {
//                    for(OutputStream out : outputStreamList) {
//                        out.write(buffer, 0, readByteCount);
//                    }
//                }
//            } catch (IOException e) {
//                e.printStackTrace();
//            } finally {
//                closeOutputStreamList(outputStreamList);
//            }
//        }
//    }

//    private List<OutputStream> openOutputStreamList(Collection<FhStorage> storages, String path, MultipartFile file, Boolean mkdir) {
//        List<OutputStream> outputStreamList = new ArrayList<>();
//        try {
//            for (FhStorage storage : storages) {
//                OutputStream outputStream = storage.getOutputStreamFromStorage(path, file.getOriginalFilename(), mkdir);
//                outputStreamList.add(outputStream);
//            }
//        } catch (IOException e) {
//            throw new UploadException("Error to open the output stream list in parallel mode", e);
//        }
//        return outputStreamList;
//    }

//    private void closeOutputStreamList(List<OutputStream> outputStreamList) {
//        for(OutputStream out : outputStreamList) {
//            try {
//                out.close();
//            } catch (IOException e) {
//                throw new UploadException("Error to close the output stream list in parallel mode", e);
//            }
//        }
//    }

}
