package br.com.mpps.filehub.domain.usecase;

import br.com.mpps.filehub.domain.exceptions.UploadException;
import br.com.mpps.filehub.domain.model.Base64Upload;
import br.com.mpps.filehub.domain.model.FileLocation;
import br.com.mpps.filehub.domain.model.FileMetadata;
import br.com.mpps.filehub.domain.model.config.Schema;
import br.com.mpps.filehub.domain.model.config.Storage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class FileManager {

    public void upload(Schema schema, FileLocation fileLocation, MultipartFile file, Boolean mkdir) {
        log.info("UPLOAD SINGLE FILE USING MULTIPART FILE");
        uploadSequential(schema, fileLocation, file, mkdir);
    }

    public void upload(Schema schema, String path, MultipartFile[] files, Boolean mkdir, Boolean parallel) {
        log.info("UPLOAD MULTIPLE FILE USING MULTIPART FILE");
        if(parallel) {
            uploadParallel(schema, path, files, mkdir);
        } else {
            uploadSequential(schema, path, files, mkdir);
        }
    }

    public void uploadBase64(Schema schema, FileLocation fileLocation, Base64Upload file, Boolean mkdir) {
        log.info("UPLOAD SINGLE FILE USING BASE 64 FILE");
        Collection<Storage> storages = schema.getStorages();
        for(Storage storage : storages) {
            storage.uploadBase64(fileLocation, file, mkdir);
        }
    }

    public void uploadBase64(Schema schema, String path, Base64Upload[] files, Boolean mkdir) {
        log.info("UPLOAD MULTIPLE FILE USING BASE 64 FILE");
        Collection<Storage> storages = schema.getStorages();
        for(Storage storage : storages) {
            storage.uploadBase64(path, files, mkdir);
        }
    }


    public boolean delete(Schema schema, String path) {
        Boolean result = true;
        Collection<Storage> storages = schema.getStorages();
        for(Storage storage : storages) {
            result = result && storage.delete(path);
        }
        return result;
    }


    public boolean existsFile(Schema schema, String filePath) {
        Storage storage = schema.getFirstUsefulStorage();
        return storage.existsFile(filePath);
    }


    public String getContentType(Schema schema, String filePath) {
        Storage storage = schema.getFirstUsefulStorage();
        return storage.getFileDetails(filePath).getContentType();
    }


    public FileMetadata getDetails(Schema schema, String filePath) {
        Storage storage = schema.getFirstUsefulStorage();
        return storage.getFileDetails(filePath);
    }


    public void downloadFile(Schema schema, String filePath, HttpServletResponse response) throws IOException {
        Storage storage;
        if(schema.isCacheEnabled()) {
            if(schema.getMiddle().existsFile(filePath)) {
                storage = schema.getMiddle();
                copy(storage.downloadFile(filePath), response.getOutputStream());
            } else {
                storage = schema.getFirstUsefulStorage();
                Path path = Paths.get(filePath);
                String fileName = path.getFileName().toString();
                String dirPath = path.getParent().toString();
                OutputStream middleOutputStream = schema.getMiddle().getOutputStreamFromStorage(fileName, dirPath, true);
                copy(storage.downloadFile(filePath), response.getOutputStream(), middleOutputStream);
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



    private void uploadSequential(Schema schema, String path, MultipartFile[] files, Boolean mkdir) {
        log.info("SEQUENTIAL");
        Collection<Storage> storages = schema.getStorages();
        if(schema.getMiddle() != null) {
            log.info("MIDDLE MODE");
            schema.getMiddle().upload(path, files, mkdir);
            List<String> filenames = Arrays.stream(files).map(MultipartFile::getOriginalFilename).collect(Collectors.toList());
            new Thread(() -> {
                storages.remove(schema.getMiddle());
                for (Storage storage : storages) {
                    schema.getMiddle().transfer(storage, path, filenames, mkdir);
                }
            }).start();
        } else {
            for (Storage storage : storages) {
                storage.upload(path, files, mkdir);
            }
        }
    }

    private void uploadSequential(Schema schema, FileLocation fileLocation, MultipartFile file, Boolean mkdir) {
        Collection<Storage> storages = schema.getStorages();
        if(schema.getMiddle() != null) {
            log.info("MIDDLE MODE");
            schema.getMiddle().upload(fileLocation, file, mkdir);
            new Thread(() -> {
                storages.remove(schema.getMiddle());
                for (Storage storage : storages) {
                    schema.getMiddle().transfer(storage, fileLocation, mkdir);
                }
            }).start();
        } else {
            for (Storage storage : storages) {
                storage.upload(fileLocation, file, mkdir);
            }
        }
    }

    private void uploadParallel(Schema schema, String path, MultipartFile[] files, Boolean mkdir) {
        log.info("PARALLEL");
        for(MultipartFile multipartFile : files) {
            int readByteCount;
            byte[] buffer = new byte[4096];
            List<OutputStream> outputStreamList = openOutputStreamList(schema.getStorages(), path, multipartFile, mkdir);
            try(InputStream in = multipartFile.getInputStream()) {
                while((readByteCount = in.read(buffer)) != -1) {
                    for(OutputStream out : outputStreamList) {
                        out.write(buffer, 0, readByteCount);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                closeOutputStreamList(outputStreamList);
            }
        }
    }

    private List<OutputStream> openOutputStreamList(Collection<Storage> storages, String path, MultipartFile file, Boolean mkdir) {
        List<OutputStream> outputStreamList = new ArrayList<>();
        try {
            for (Storage storage : storages) {
                OutputStream outputStream = storage.getOutputStreamFromStorage(path, file.getOriginalFilename(), mkdir);
                outputStreamList.add(outputStream);
            }
        } catch (IOException e) {
            throw new UploadException("Error to open the output stream list in parallel mode", e);
        }
        return outputStreamList;
    }

    private void closeOutputStreamList(List<OutputStream> outputStreamList) {
        for(OutputStream out : outputStreamList) {
            try {
                out.close();
            } catch (IOException e) {
                throw new UploadException("Error to close the output stream list in parallel mode", e);
            }
        }
    }

}
