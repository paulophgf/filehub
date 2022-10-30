package br.com.mpps.filehub.reader;

import br.com.mpps.filehub.domain.model.config.Schema;
import br.com.mpps.filehub.domain.model.config.Storage;
import br.com.mpps.filehub.domain.model.config.Trigger;
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

    final static String XML_FILE_STORAGE_MISSING_ID_ATTR = "storage/config-missing-attr-id.xml";
    final static String XML_FILE_STORAGE_MISSING_NAME_ATTR = "storage/config-missing-attr-name.xml";
    final static String XML_FILE_STORAGE_MISSING_TYPE_ATTR = "storage/config-missing-attr-type.xml";
    final static String XML_FILE_STORAGE_MISSING_PROPERTY = "storage/config-missing-property.xml";
    final static String XML_FILE_STORAGE_TYPE_NOT_EXISTS = "storage/config-type-not-found.xml";
    final static String XML_FILE_STORAGE_DUPLICATED_ID = "storage/config-duplicated-id.xml";
    final static String XML_FILE_STORAGE_INVALID_ID = "storage/config-invalid-id.xml";

    final static String XML_FILE_TRIGGER_MISSING_ID_ATTR = "trigger/config-missing-attr-id.xml";
    final static String XML_FILE_TRIGGER_DUPLICATED_ID = "trigger/config-duplicated-id.xml";
    final static String XML_FILE_TRIGGER_MISSING_PROPERTY = "trigger/config-missing-property.xml";
    final static String XML_FILE_TRIGGER_WRONG_ACTION = "trigger/config-wrong-action.xml";
    final static String XML_FILE_TRIGGER_INVALID_URL = "trigger/config-invalid-url.xml";
    final static String XML_FILE_TRIGGER_INVALID_HEADER = "trigger/config-invalid-header.xml";

    final static String XML_FILE_DUPLICATED_SCHEMA_NAME = "schema/config-duplicated-name.xml";
    final static String XML_FILE_INVALID_SCHEMA_NAME = "schema/config-invalid-name.xml";
    final static String XML_FILE_SCHEMA_NAME_EQUALS_STORAGE_ID = "schema/config-name-equals-storage-id.xml";
    final static String XML_FILE_SCHEMA_MIDDLE_WRONG_TYPE = "schema/config-middle-wrong-type.xml";
    final static String XML_FILE_STORAGE_NOT_FOUND = "schema/config-storage-not-found.xml";
    final static String XML_FILE_TRIGGER_NOT_FOUND = "schema/config-trigger-not-found.xml";



    public Map<String, Schema> createSchemasModel() {
        S3Properties s3Properties = new S3Properties();
        s3Properties.setRegion("us-east-2");
        s3Properties.setSecretKeyId("G5FD1G66RDGFGE1");
        s3Properties.setSecretKey("6F51E6f1e651fds1ff161F61fd51s1F");
        s3Properties.setBucket("test");

        FileSystemProperties fsProperties = new FileSystemProperties();
        fsProperties.setBaseDir("C:\\Users\\user\\filehub");

        Collection<Storage> s3Test = new LinkedList<>();
        s3Test.add(EnumStorageType.AWS_S3.getStorage("S3-Test", s3Properties));
        Schema s3Schema = new Schema("S3-Test", s3Test, true);

        Collection<Storage> fileSystemTest = new LinkedList<>();
        fileSystemTest.add(EnumStorageType.FILE_SYSTEM.getStorage("FileSystem-Test", fsProperties));
        Schema fileSystemSchema = new Schema("FileSystem-Test", fileSystemTest, true);

        Collection<Storage> all = new LinkedList<>();
        all.add(EnumStorageType.AWS_S3.getStorage("S3-Test", s3Properties));
        all.add(EnumStorageType.FILE_SYSTEM.getStorage("FileSystem-Test", fsProperties));
        Schema allSchema = new Schema("ALL", all, false);


        Collection<Storage> s3Only = new LinkedList<>();
        s3Only.add(EnumStorageType.AWS_S3.getStorage("S3-Test", s3Properties));
        Schema s3OnlySchema = new Schema("S3-Only", s3Only, false);

        Collection<Storage> fsOnly = new LinkedList<>();
        fsOnly.add(EnumStorageType.FILE_SYSTEM.getStorage("FileSystem-Test", fsProperties));
        Schema fsOnlySchema = new Schema("FileSystem-Only", fsOnly, false);

        Collection<Storage> s3Andfs = new LinkedList<>();
        s3Andfs.add(EnumStorageType.AWS_S3.getStorage("S3-Test", s3Properties));
        s3Andfs.add(EnumStorageType.FILE_SYSTEM.getStorage("FileSystem-Test", fsProperties));
        Schema s3AndFsSchema = new Schema("S3-And-FileSystem", s3Andfs, false);

        Map<String, Schema> model = new LinkedHashMap<>();
        model.put("S3-Test", s3Schema);
        model.put("FileSystem-Test", fileSystemSchema);
        model.put("ALL", allSchema);
        model.put("S3-Only", s3OnlySchema);
        model.put("FileSystem-Only", fsOnlySchema);
        model.put("S3-And-FileSystem", s3AndFsSchema);
        return model;
    }

    public Map<String, Schema> createSchemasModelWithMiddle(Boolean isTemporary) {
        S3Properties s3Properties = new S3Properties();
        s3Properties.setRegion("us-east-2");
        s3Properties.setSecretKeyId("G5FD1G66RDGFGE1");
        s3Properties.setSecretKey("6F51E6f1e651fds1ff161F61fd51s1F");
        s3Properties.setBucket("test");

        FileSystemProperties fsProperties = new FileSystemProperties();
        fsProperties.setBaseDir("C:\\Users\\user\\filehub");

        Collection<Storage> s3Test = new LinkedList<>();
        s3Test.add(EnumStorageType.AWS_S3.getStorage("S3-Test", s3Properties));
        Schema s3Schema = new Schema("S3-Test", s3Test, true);

        Collection<Storage> fileSystemTest = new LinkedList<>();
        fileSystemTest.add(EnumStorageType.FILE_SYSTEM.getStorage("FileSystem-Test", fsProperties));
        Schema fileSystemSchema = new Schema("FileSystem-Test", fileSystemTest, true);

        Collection<Storage> all = new LinkedList<>();
        all.add(EnumStorageType.AWS_S3.getStorage("S3-Test", s3Properties));
        all.add(EnumStorageType.FILE_SYSTEM.getStorage("FileSystem-Test", fsProperties));
        Schema allSchema = new Schema("ALL", all, false);


        Collection<Storage> s3Only = new LinkedList<>();
        s3Only.add(EnumStorageType.AWS_S3.getStorage("S3-Test", s3Properties));
        Schema s3OnlySchema = new Schema("S3-Only", s3Only, false);

        Collection<Storage> fsOnly = new LinkedList<>();
        fsOnly.add(EnumStorageType.FILE_SYSTEM.getStorage("FileSystem-Test", fsProperties));
        Schema fsOnlySchema = new Schema("FileSystem-Only", fsOnly, false);

        Collection<Storage> s3Andfs = new LinkedList<>();
        fsProperties.setTemporary(isTemporary);
        s3Andfs.add(EnumStorageType.MIDDLE.getStorage("FileSystem-Test", fsProperties));
        s3Andfs.add(EnumStorageType.AWS_S3.getStorage("S3-Test", s3Properties));
        Schema s3AndFsSchema = new Schema("S3-And-FileSystem", s3Andfs, false);

        Map<String, Schema> model = new LinkedHashMap<>();
        model.put("S3-Test", s3Schema);
        model.put("FileSystem-Test", fileSystemSchema);
        model.put("ALL", allSchema);
        model.put("S3-Only", s3OnlySchema);
        model.put("FileSystem-Only", fsOnlySchema);
        model.put("S3-And-FileSystem", s3AndFsSchema);
        return model;
    }

    public Map<String, Schema> createSchemasModelWithTrigger() {
        S3Properties s3Properties = new S3Properties();
        s3Properties.setRegion("us-east-2");
        s3Properties.setSecretKeyId("G5FD1G66RDGFGE1");
        s3Properties.setSecretKey("6F51E6f1e651fds1ff161F61fd51s1F");
        s3Properties.setBucket("test");

        FileSystemProperties fsProperties = new FileSystemProperties();
        fsProperties.setBaseDir("C:\\Users\\user\\filehub");

        Collection<Storage> s3Test = new LinkedList<>();
        s3Test.add(EnumStorageType.AWS_S3.getStorage("S3-Test", s3Properties));
        Schema s3Schema = new Schema("S3-Test", s3Test, true);

        Collection<Storage> fileSystemTest = new LinkedList<>();
        fileSystemTest.add(EnumStorageType.FILE_SYSTEM.getStorage("FileSystem-Test", fsProperties));
        Schema fileSystemSchema = new Schema("FileSystem-Test", fileSystemTest, true);

        Collection<Storage> all = new LinkedList<>();
        all.add(EnumStorageType.AWS_S3.getStorage("S3-Test", s3Properties));
        all.add(EnumStorageType.FILE_SYSTEM.getStorage("FileSystem-Test", fsProperties));
        Schema allSchema = new Schema("ALL", all, false);


        Collection<Storage> s3Only = new LinkedList<>();
        s3Only.add(EnumStorageType.AWS_S3.getStorage("S3-Test", s3Properties));
        Schema s3OnlySchema = new Schema("S3-Only", s3Only, false);

        Collection<Storage> fsOnly = new LinkedList<>();
        fsOnly.add(EnumStorageType.FILE_SYSTEM.getStorage("FileSystem-Test", fsProperties));
        Schema fsOnlySchema = new Schema("FileSystem-Only", fsOnly, false);

        Collection<Storage> s3Andfs = new LinkedList<>();
        s3Andfs.add(EnumStorageType.FILE_SYSTEM.getStorage("FileSystem-Test", fsProperties));
        s3Andfs.add(EnumStorageType.AWS_S3.getStorage("S3-Test", s3Properties));
        Schema s3AndFsSchema = new Schema("S3-And-FileSystem", s3Andfs, false);

        Trigger trigger = new Trigger();
        trigger.setAction(EnumTriggerAction.UPDATE);
        trigger.setUrl("http://localhost:9002/auth");
        trigger.setHeader("Authorization");
        s3AndFsSchema.setTrigger(trigger);

        Map<String, Schema> model = new LinkedHashMap<>();
        model.put("S3-Test", s3Schema);
        model.put("FileSystem-Test", fileSystemSchema);
        model.put("ALL", allSchema);
        model.put("S3-Only", s3OnlySchema);
        model.put("FileSystem-Only", fsOnlySchema);
        model.put("S3-And-FileSystem", s3AndFsSchema);
        return model;
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

}
