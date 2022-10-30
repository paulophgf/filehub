package br.com.mpps.filehub.domain.usecase;

import br.com.mpps.filehub.domain.exceptions.UploadException;
import br.com.mpps.filehub.domain.model.config.Schema;
import br.com.mpps.filehub.domain.model.config.Storage;
import br.com.mpps.filehub.domain.model.Base64Upload;
import br.com.mpps.filehub.domain.model.storage.EnumStorageType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class FileManager {

    public void upload(Schema schema, String path, MultipartFile[] files, Boolean mkdir, Boolean parallel) {
        log.info("UPLOAD USING MULTIPART FILE");
        Collection<Storage> storages = schema.getStorages();
        if(parallel) {
            uploadParallel(storages, path, files, mkdir);
        } else {
            uploadSequential(storages, path, files, mkdir);
        }
    }

    public void uploadBase64(Schema schema, String path, Base64Upload[] files, Boolean mkdir) {
        log.info("UPLOAD USING BASE 64 FILE");
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
        Collection<Storage> storages = schema.getStorages();
        Storage firstStorage = storages.stream().findFirst().get();
        return firstStorage.existsFile(filePath);
    }


    public String getContentType(Schema schema, String filePath) {
        Collection<Storage> storages = schema.getStorages();
        Storage firstStorage = storages.stream().findFirst().get();
        return firstStorage.getContentType(filePath);
    }


    public byte[] downloadFile(Schema schema, String filePath) throws IOException {
        Collection<Storage> storages = schema.getStorages();
        Storage firstStorage = storages.stream().findFirst().get();
        return firstStorage.downloadFile(filePath);
    }



    private void uploadSequential(Collection<Storage> storages, String path, MultipartFile[] files, Boolean mkdir) {
        log.info("SEQUENTIAL");
        Storage firstStorage = storages.stream().findFirst().get();
        if(EnumStorageType.MIDDLE.equals(firstStorage.getType())) {
            log.info("MIDDLE MODE");
            firstStorage.upload(path, files, mkdir);
            List<String> filenames = Arrays.stream(files).map(MultipartFile::getOriginalFilename).collect(Collectors.toList());
            new Thread(() -> {
                storages.remove(firstStorage);
                for (Storage storage : storages) {
                    firstStorage.transfer(storage, path, filenames, mkdir);
                }
            }).start();
        } else {
            for (Storage storage : storages) {
                storage.upload(path, files, mkdir);
            }
        }
    }

    private void uploadParallel(Collection<Storage> storages, String path, MultipartFile[] files, Boolean mkdir) {
        log.info("PARALLEL");
        for(MultipartFile multipartFile : files) {
            int readByteCount;
            byte[] buffer = new byte[4096];
            List<OutputStream> outputStreamList = openOutputStreamList(storages, path, multipartFile, mkdir);
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
