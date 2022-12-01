package br.com.mpps.filehub.reader;

import br.com.mpps.filehub.domain.model.config.Schema;
import br.com.mpps.filehub.domain.model.config.Storage;
import br.com.mpps.filehub.domain.model.config.StorageResource;
import br.com.mpps.filehub.domain.model.config.Trigger;
import br.com.mpps.filehub.domain.model.storage.EnumHttpMethod;
import br.com.mpps.filehub.domain.model.storage.EnumTriggerAction;
import br.com.mpps.filehub.domain.model.storage.filesystem.FileSystemProperties;
import br.com.mpps.filehub.domain.model.storage.s3.S3Properties;
import br.com.mpps.filehub.domain.model.storage.EnumStorageType;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class XLMPropertiesReaderData {

    private final static String XML_FILES_DIR = "src/test/resources/config/";

    final static String XML_FILE_SUCCESS = "success/config.xml";
    final static String XML_FILE_SUCCESS_WITH_MIDDLE = "success/config-with-middle.xml";
    final static String XML_FILE_SUCCESS_WITH_MIDDLE_TEMP = "success/config-with-middle-temp.xml";
    final static String XML_FILE_SUCCESS_WITH_TRIGGER = "success/config-with-trigger.xml";
    final static String XML_FILE_SUCCESS_WITH_AUTO_SCHEMA_ON_STORAGE = "success/config-with-auto-schema-storage.xml";
    final static String XML_FILE_SUCCESS_WITH_AUTO_SCHEMA_ON_STORAGES_ELEMENT = "success/config-with-auto-schema-storages-element.xml";
    final static String XML_FILE_SUCCESS_WITH_SCHEMA_CACHE = "success/config-with-schema-cache.xml";
    final static String XML_FILE_SUCCESS_WITH_TRIGGER_DEFAULT = "success/config-trigger-default.xml";
    final static String XML_FILE_SUCCESS_WITH_TRIGGER_NO_DIR = "success/config-trigger-no-dir.xml";

    final static String XML_FILE_STORAGE_MISSING_ID_ATTR = "storage/config-missing-attr-id.xml";
    final static String XML_FILE_STORAGE_MISSING_NAME_ATTR = "storage/config-missing-attr-name.xml";
    final static String XML_FILE_STORAGE_MISSING_TYPE_ATTR = "storage/config-missing-attr-type.xml";
    final static String XML_FILE_STORAGE_MISSING_PROPERTY = "storage/config-missing-property.xml";
    final static String XML_FILE_STORAGE_TYPE_NOT_EXISTS = "storage/config-type-not-found.xml";
    final static String XML_FILE_STORAGE_DUPLICATED_ID = "storage/config-duplicated-id.xml";

    final static String XML_FILE_TRIGGER_MISSING_ID_ATTR = "trigger/config-missing-attr-id.xml";
    final static String XML_FILE_TRIGGER_DUPLICATED_ID = "trigger/config-duplicated-id.xml";
    final static String XML_FILE_TRIGGER_MISSING_PROPERTY = "trigger/config-missing-property.xml";
    final static String XML_FILE_TRIGGER_WRONG_ACTION = "trigger/config-wrong-action.xml";
    final static String XML_FILE_TRIGGER_INVALID_URL = "trigger/config-invalid-url.xml";
    final static String XML_FILE_TRIGGER_INVALID_HEADER = "trigger/config-invalid-header.xml";
    final static String XML_FILE_TRIGGER_ID_DEFAULT = "trigger/config-id-default.xml";
    final static String XML_FILE_TRIGGER_INVALID_DEFAULT_VALUE = "trigger/config-invalid-default-value.xml";
    final static String XML_FILE_TRIGGER_MULTIPLE_DEFAULT = "trigger/config-multiple-default.xml";

    final static String XML_FILE_DUPLICATED_SCHEMA_NAME = "schema/config-duplicated-name.xml";
    final static String XML_FILE_STORAGE_NOT_FOUND = "schema/config-storage-not-found.xml";
    final static String XML_FILE_TRIGGER_NOT_FOUND = "schema/config-trigger-not-found.xml";
    final static String XML_FILE_SUCCESS_WITH_SCHEMA_CACHE_AND_TEMPORARY_MIDDLE = "schema/config-cache-temporary-middle.xml";


    public StorageResource createSchemasModel() {
        S3Properties s3Properties = createS3Properties();
        FileSystemProperties fsProperties = createFsProperties();

        Storage<S3Properties> s3Storage = EnumStorageType.AWS_S3.getStorage("S3-Test", s3Properties);
        Storage<FileSystemProperties> fsStorage = EnumStorageType.FILE_SYSTEM.getStorage("FileSystem-Test", fsProperties);

        Collection<Storage> s3Only = Collections.singletonList(s3Storage);
        Schema s3OnlySchema = new Schema("S3-Only", null, null, s3Only, false);

        Collection<Storage> fsOnly = Collections.singletonList(fsStorage);
        Schema fsOnlySchema = new Schema("FileSystem-Only", null, null, fsOnly, false);

        Collection<Storage> s3Andfs = new LinkedList<>();
        s3Andfs.add(s3Storage);
        s3Andfs.add(fsStorage);
        Schema s3AndFsSchema = new Schema("S3-And-FileSystem", null, null, s3Andfs, false);

        Map<String, Storage> storages = new LinkedHashMap<>();
        storages.put("S3-Test", s3Storage);
        storages.put("FileSystem-Test", fsStorage);

        Map<String, Schema> schemas = new LinkedHashMap<>();
        schemas.put("S3-Only", s3OnlySchema);
        schemas.put("FileSystem-Only", fsOnlySchema);
        schemas.put("S3-And-FileSystem", s3AndFsSchema);

        return new StorageResource(storages, new HashMap<>(), schemas);
    }

    public StorageResource createSchemasModelWithMiddle(Boolean isTemporary, Boolean isCache) {
        S3Properties s3Properties = createS3Properties();
        FileSystemProperties fsProperties = createFsProperties();

        Storage<S3Properties> s3Storage = EnumStorageType.AWS_S3.getStorage("S3-Test", s3Properties);
        Storage<FileSystemProperties> fsStorage = EnumStorageType.FILE_SYSTEM.getStorage("FileSystem-Test", fsProperties);

        Collection<Storage> s3Andfs = new LinkedList<>();
        s3Andfs.add(s3Storage);
        s3Andfs.add(fsStorage);
        Schema s3AndFsSchema = new Schema("S3-And-FileSystem", null, fsStorage, s3Andfs, false);
        s3AndFsSchema.setTemporaryMiddle(isTemporary);
        s3AndFsSchema.setTemporaryMiddle(isCache);

        Map<String, Storage> storages = new LinkedHashMap<>();
        storages.put("S3-Test", s3Storage);
        storages.put("FileSystem-Test", fsStorage);

        Map<String, Schema> schemas = new LinkedHashMap<>();
        schemas.put("S3-And-FileSystem", s3AndFsSchema);

        return new StorageResource(storages, new HashMap<>(), schemas);
    }

    public StorageResource createSchemasModelWithAutoSchemaOnStorage() {
        FileSystemProperties fsProperties = createFsProperties();
        Storage<FileSystemProperties> fsStorage = EnumStorageType.FILE_SYSTEM.getStorage("FileSystem-Test", fsProperties);

        Collection<Storage> fsOnly = Collections.singletonList(fsStorage);
        Schema mySchema = new Schema("mySchema", null, null, fsOnly, false);

        Map<String, Storage> storages = new LinkedHashMap<>();
        storages.put("FileSystem-Test", fsStorage);

        Map<String, Schema> schemas = new LinkedHashMap<>();
        schemas.put("mySchema", mySchema);

        return new StorageResource(storages, new HashMap<>(), schemas);
    }

    public StorageResource createSchemasModelWithAutoSchemaOnStoragesElement() {
        S3Properties s3Properties = createS3Properties();
        FileSystemProperties fsProperties = createFsProperties();

        Storage<S3Properties> s3Storage = EnumStorageType.AWS_S3.getStorage("S3-Test", s3Properties);
        Storage<FileSystemProperties> fsStorage = EnumStorageType.FILE_SYSTEM.getStorage("FileSystem-Test", fsProperties);

        Collection<Storage> allSchema = new LinkedList<>();
        allSchema.add(s3Storage);
        allSchema.add(fsStorage);
        Schema s3AndFsSchema = new Schema("all", null, null, allSchema, false);

        Map<String, Storage> storages = new LinkedHashMap<>();
        storages.put("S3-Test", s3Storage);
        storages.put("FileSystem-Test", fsStorage);

        Map<String, Schema> schemas = new LinkedHashMap<>();
        schemas.put("all", s3AndFsSchema);

        return new StorageResource(storages, new HashMap<>(), schemas);
    }

    public StorageResource createSchemasModelWithTrigger(boolean allowDirOperations) {
        S3Properties s3Properties = createS3Properties();
        FileSystemProperties fsProperties = createFsProperties();

        Storage<S3Properties> s3Storage = EnumStorageType.AWS_S3.getStorage("S3-Test", s3Properties);
        Storage<FileSystemProperties> fsStorage = EnumStorageType.FILE_SYSTEM.getStorage("FileSystem-Test", fsProperties);

        Collection<Storage> s3Only = Collections.singletonList(s3Storage);
        Schema s3OnlySchema = new Schema("S3-Only", null, null, s3Only, false);

        Collection<Storage> fsOnly = Collections.singletonList(fsStorage);
        Schema fsOnlySchema = new Schema("FileSystem-Only", null, null, fsOnly, false);

        Collection<Storage> s3Andfs = new LinkedList<>();
        s3Andfs.add(s3Storage);
        s3Andfs.add(fsStorage);
        Schema s3AndFsSchema = new Schema("S3-And-FileSystem", null, null, s3Andfs, false);

        Trigger trigger = createTrigger("myTrigger");
        trigger.setAllowDirOperations(allowDirOperations);
        s3AndFsSchema.setTrigger(trigger);
        Map<String, Trigger> triggers = new LinkedHashMap<>();
        triggers.put("myTrigger", trigger);

        Map<String, Storage> storages = new LinkedHashMap<>();
        storages.put("S3-Test", s3Storage);
        storages.put("FileSystem-Test", fsStorage);

        Map<String, Schema> schemas = new LinkedHashMap<>();
        schemas.put("S3-Only", s3OnlySchema);
        schemas.put("FileSystem-Only", fsOnlySchema);
        schemas.put("S3-And-FileSystem", s3AndFsSchema);

        return new StorageResource(storages, triggers, schemas);
    }

    public StorageResource createSchemasModelWithTriggerDefault() {
        S3Properties s3Properties = createS3Properties();
        FileSystemProperties fsProperties = createFsProperties();

        Storage<S3Properties> s3Storage = EnumStorageType.AWS_S3.getStorage("S3-Test", s3Properties);
        Storage<FileSystemProperties> fsStorage = EnumStorageType.FILE_SYSTEM.getStorage("FileSystem-Test", fsProperties);

        Trigger defaultTrigger = createTrigger("trigger-default");
        Trigger anotherTrigger = createTrigger("trigger-another");
        anotherTrigger.setAction(EnumTriggerAction.ALL);

        Collection<Storage> s3Only = Collections.singletonList(s3Storage);
        Schema s3OnlySchema = new Schema("S3-Only", defaultTrigger, null, s3Only, false);

        Collection<Storage> fsOnly = Collections.singletonList(fsStorage);
        Schema fsOnlySchema = new Schema("FileSystem-Only", anotherTrigger, null, fsOnly, false);

        Collection<Storage> s3Andfs = new LinkedList<>();
        s3Andfs.add(s3Storage);
        s3Andfs.add(fsStorage);
        Schema s3AndFsSchema = new Schema("S3-And-FileSystem", defaultTrigger, null, s3Andfs, false);


        Map<String, Trigger> triggers = new LinkedHashMap<>();
        triggers.put("trigger-default", defaultTrigger);
        triggers.put("default", defaultTrigger);
        triggers.put("trigger-another", anotherTrigger);

        Map<String, Storage> storages = new LinkedHashMap<>();
        storages.put("S3-Test", s3Storage);
        storages.put("FileSystem-Test", fsStorage);

        Map<String, Schema> schemas = new LinkedHashMap<>();
        schemas.put("S3-Only", s3OnlySchema);
        schemas.put("FileSystem-Only", fsOnlySchema);
        schemas.put("S3-And-FileSystem", s3AndFsSchema);

        return new StorageResource(storages, triggers, schemas);
    }


    String getPropertiesFromXMLFile(String filePath) {
        byte[] encoded = new byte[0];
        try {
            encoded = Files.readAllBytes(Paths.get(XML_FILES_DIR + filePath));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new String(encoded, StandardCharsets.UTF_8);
    }


    private S3Properties createS3Properties() {
        S3Properties s3Properties = new S3Properties();
        s3Properties.setRegion("us-east-2");
        s3Properties.setSecretKeyId("G5FD1G66RDGFGE1");
        s3Properties.setSecretKey("6F51E6f1e651fds1ff161F61fd51s1F");
        s3Properties.setBucket("test");
        return s3Properties;
    }

    private FileSystemProperties createFsProperties() {
        FileSystemProperties fsProperties = new FileSystemProperties();
        fsProperties.setBaseDir("C:\\Users\\user\\filehub");
        return fsProperties;
    }

    private Trigger createTrigger(String id) {
        Trigger trigger = new Trigger();
        trigger.setId(id);
        trigger.setAction(EnumTriggerAction.UPDATE);
        trigger.setUrl("http://localhost:9002/auth");
        trigger.setHeader("Authorization");
        trigger.setHttpMethod(EnumHttpMethod.GET);
        return trigger;
    }

}
