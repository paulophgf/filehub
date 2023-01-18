package br.com.p8projects.filehub.reader;

import br.com.p8projects.filehub.domain.exceptions.PropertiesReaderException;
import br.com.p8projects.filehub.domain.model.config.StorageResource;
import br.com.p8projects.filehub.infrastructure.config.XMLStorageReader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class XMLStorageReaderTest {

    private XMLStorageReader XMLStorageReader;
    private XLMPropertiesReaderData data;


    @BeforeEach
    void setUp() {
        XMLStorageReader = new XMLStorageReader();
        data = new XLMPropertiesReaderData();
    }


    // SUCCESS

    @Test
    @DisplayName("Read Properties XML File (success)")
    void successRead() {
        StorageResource model = data.createSchemasModel();
        String xmlContent = data.getPropertiesFromXMLFile(XLMPropertiesReaderData.XML_FILE_SUCCESS);
        StorageResource schemas = XMLStorageReader.read(xmlContent);
        assertEquals(schemas, model);
    }

    @Test
    @DisplayName("Read Properties XML File: Schema with middle storage (success)")
    void successReadSchemaWithMiddleStorage() {
        StorageResource model = data.createSchemasModelWithMiddle(false, false);
        String xmlContent = data.getPropertiesFromXMLFile(XLMPropertiesReaderData.XML_FILE_SUCCESS_WITH_MIDDLE);
        StorageResource schemas = XMLStorageReader.read(xmlContent);
        assertEquals(schemas, model);
        assertNotNull(schemas.getSchemas().get("S3-And-FileSystem").getMiddle());
        assertFalse(schemas.getSchemas().get("S3-And-FileSystem").isTemporaryMiddle());
    }

    @Test
    @DisplayName("Read Properties XML File: Schema with temporary middle storage (success)")
    void successReadSchemaWithTemporaryMiddleStorage() {
        StorageResource model = data.createSchemasModelWithMiddle(true, false);
        String xmlContent = data.getPropertiesFromXMLFile(XLMPropertiesReaderData.XML_FILE_SUCCESS_WITH_MIDDLE_TEMP);
        StorageResource schemas = XMLStorageReader.read(xmlContent);
        assertEquals(schemas, model);
        assertNotNull(schemas.getSchemas().get("S3-And-FileSystem").getMiddle());
        assertTrue(schemas.getSchemas().get("S3-And-FileSystem").isTemporaryMiddle());
    }

    @Test
    @DisplayName("Read Properties XML File: Auto Schema on storage (success)")
    void successReadSchemaWithAutoSchemaOnStorage() {
        StorageResource model = data.createSchemasModelWithAutoSchemaOnStorage();
        String xmlContent = data.getPropertiesFromXMLFile(XLMPropertiesReaderData.XML_FILE_SUCCESS_WITH_AUTO_SCHEMA_ON_STORAGE);
        StorageResource schemas = XMLStorageReader.read(xmlContent);
        assertEquals(schemas, model);
    }

    @Test
    @DisplayName("Read Properties XML File: Auto Schema on storages (success)")
    void successReadSchemaWithAutoSchemaOnStorages() {
        StorageResource model = data.createSchemasModelWithAutoSchemaOnStoragesElement();
        String xmlContent = data.getPropertiesFromXMLFile(XLMPropertiesReaderData.XML_FILE_SUCCESS_WITH_AUTO_SCHEMA_ON_STORAGES_ELEMENT);
        StorageResource schemas = XMLStorageReader.read(xmlContent);
        assertEquals(schemas, model);
    }

    @Test
    @DisplayName("Read Properties XML File: Schema with cache (success)")
    void successReadSchemaWithCache() {
        StorageResource model = data.createSchemasModelWithMiddle(false, true);
        String xmlContent = data.getPropertiesFromXMLFile(XLMPropertiesReaderData.XML_FILE_SUCCESS_WITH_SCHEMA_CACHE);
        StorageResource schemas = XMLStorageReader.read(xmlContent);
        assertEquals(schemas, model);
        assertTrue(schemas.getSchemas().get("S3-And-FileSystem").isCacheEnabled());
    }

    @Test
    @DisplayName("Read Properties XML File: Schema with trigger (success)")
    void successReadSchemaWithTrigger() {
        StorageResource model = data.createSchemasModelWithTrigger(true);
        String xmlContent = data.getPropertiesFromXMLFile(XLMPropertiesReaderData.XML_FILE_SUCCESS_WITH_TRIGGER);
        StorageResource schemas = XMLStorageReader.read(xmlContent);
        assertEquals(schemas, model);
    }

    @Test
    @DisplayName("Read Properties XML File: Schema with trigger default (success)")
    void successReadSchemaWithTriggerDefault() {
        StorageResource model = data.createSchemasModelWithTriggerDefault();
        String xmlContent = data.getPropertiesFromXMLFile(XLMPropertiesReaderData.XML_FILE_SUCCESS_WITH_TRIGGER_DEFAULT);
        StorageResource schemas = XMLStorageReader.read(xmlContent);
        assertEquals(schemas, model);
        assertNotNull(schemas.getTriggers().get("default"));
    }

    @Test
    @DisplayName("Read Properties XML File: Schema with trigger no directory (success)")
    void successReadSchemaWithTriggerNoDirectory() {
        StorageResource model = data.createSchemasModelWithTrigger(true);
        String xmlContent = data.getPropertiesFromXMLFile(XLMPropertiesReaderData.XML_FILE_SUCCESS_WITH_TRIGGER_NO_DIR);
        StorageResource schemas = XMLStorageReader.read(xmlContent);
        assertEquals(schemas, model);
        assertTrue(schemas.getTriggers().get("myTrigger").isAllowDirOperations());
    }


    // STORAGE

    @Test
    @DisplayName("Read Properties XML File: Missing ID attribute (storage)")
    void storageMissingIdAttribute() {
        String xmlContent = data.getPropertiesFromXMLFile(XLMPropertiesReaderData.XML_FILE_STORAGE_MISSING_ID_ATTR);
        Throwable exception = assertThrows(PropertiesReaderException.class,
                () -> XMLStorageReader.read(xmlContent)
        );
        String expectedMessage = "Attribute 'id' not found in storage element";
        assertEquals(expectedMessage, exception.getMessage());
    }

    @Test
    @DisplayName("Read Properties XML File: Missing name attribute (storage)")
    void storageMissingNameAttribute() {
        String xmlContent = data.getPropertiesFromXMLFile(XLMPropertiesReaderData.XML_FILE_STORAGE_MISSING_NAME_ATTR);
        Throwable exception = assertThrows(PropertiesReaderException.class,
                () -> XMLStorageReader.read(xmlContent)
        );
        String expectedMessage = "Attribute 'name' not found in schema element";
        assertEquals(expectedMessage, exception.getMessage());
    }

    @Test
    @DisplayName("Read Properties XML File: Missing type attribute (storage)")
    void storageMissingTypeAttribute() {
        String xmlContent = data.getPropertiesFromXMLFile(XLMPropertiesReaderData.XML_FILE_STORAGE_MISSING_TYPE_ATTR);
        Throwable exception = assertThrows(PropertiesReaderException.class,
                () -> XMLStorageReader.read(xmlContent)
        );
        String expectedMessage = "Attribute 'type' not found in storage element";
        assertEquals(expectedMessage, exception.getMessage());
    }

    @Test
    @DisplayName("Read Properties XML File: Missing properties (storage)")
    void storageMissingProperties() {
        String xmlContent = data.getPropertiesFromXMLFile(XLMPropertiesReaderData.XML_FILE_STORAGE_MISSING_PROPERTY);
        Throwable exception = assertThrows(PropertiesReaderException.class,
                () -> XMLStorageReader.read(xmlContent)
        );
        String expectedMessage = "The property secretKeyId not found in the storage S3-Test";
        assertEquals(expectedMessage, exception.getMessage());
    }

    @Test
    @DisplayName("Read Properties XML File: Storage type not exists (storage)")
    void storageTypeNotExists() {
        String xmlContent = data.getPropertiesFromXMLFile(XLMPropertiesReaderData.XML_FILE_STORAGE_TYPE_NOT_EXISTS);
        Throwable exception = assertThrows(PropertiesReaderException.class,
                () -> XMLStorageReader.read(xmlContent)
        );
        String expectedMessage = "Storage type ERROR not exists";
        assertEquals(expectedMessage, exception.getMessage());
    }

    @Test
    @DisplayName("Read Properties XML File: Duplicated storage's ID (storage)")
    void storageDuplicatedId() {
        String xmlContent = data.getPropertiesFromXMLFile(XLMPropertiesReaderData.XML_FILE_STORAGE_DUPLICATED_ID);
        Throwable exception = assertThrows(PropertiesReaderException.class,
                () -> XMLStorageReader.read(xmlContent)
        );
        String expectedMessage = "Duplicated id was found in storage elements: S3-Test";
        assertEquals(expectedMessage, exception.getMessage());
    }


    // TRIGGER

    @Test
    @DisplayName("Read Properties XML File: Missing ID attribute (trigger)")
    void triggerMissingIdAttribute() {
        String xmlContent = data.getPropertiesFromXMLFile(XLMPropertiesReaderData.XML_FILE_TRIGGER_MISSING_ID_ATTR);
        Throwable exception = assertThrows(PropertiesReaderException.class,
                () -> XMLStorageReader.read(xmlContent)
        );
        String expectedMessage = "Attribute 'id' not found in trigger element";
        assertEquals(expectedMessage, exception.getMessage());
    }

    @Test
    @DisplayName("Read Properties XML File: Duplicated trigger's ID (trigger)")
    void triggerDuplicatedId() {
        String xmlContent = data.getPropertiesFromXMLFile(XLMPropertiesReaderData.XML_FILE_TRIGGER_DUPLICATED_ID);
        Throwable exception = assertThrows(PropertiesReaderException.class,
                () -> XMLStorageReader.read(xmlContent)
        );
        String expectedMessage = "Duplicated id was found in trigger elements: user-auth";
        assertEquals(expectedMessage, exception.getMessage());
    }

    @Test
    @DisplayName("Read Properties XML File: Missing properties (trigger)")
    void triggerMissingProperties() {
        String xmlContent = data.getPropertiesFromXMLFile(XLMPropertiesReaderData.XML_FILE_TRIGGER_MISSING_PROPERTY);
        Throwable exception = assertThrows(PropertiesReaderException.class,
                () -> XMLStorageReader.read(xmlContent)
        );
        String expectedMessage = "The property url not found in the trigger user-auth";
        assertEquals(expectedMessage, exception.getMessage());
    }

    @Test
    @DisplayName("Read Properties XML File: Trigger action not exists (trigger)")
    void triggerActionNotExists() {
        String xmlContent = data.getPropertiesFromXMLFile(XLMPropertiesReaderData.XML_FILE_TRIGGER_WRONG_ACTION);
        Throwable exception = assertThrows(PropertiesReaderException.class,
                () -> XMLStorageReader.read(xmlContent)
        );
        String expectedMessage = "Trigger action MERGE not exists";
        assertEquals(expectedMessage, exception.getMessage());
    }

    @Test
    @DisplayName("Read Properties XML File: Invalid trigger's URL (trigger)")
    void triggerInvalidURL() {
        String xmlContent = data.getPropertiesFromXMLFile(XLMPropertiesReaderData.XML_FILE_TRIGGER_INVALID_URL);
        Throwable exception = assertThrows(PropertiesReaderException.class,
                () -> XMLStorageReader.read(xmlContent)
        );
        String expectedMessage = "Invalid value to URL property in user-auth trigger element";
        assertEquals(expectedMessage, exception.getMessage());
    }

    @Test
    @DisplayName("Read Properties XML File: Invalid trigger's header (trigger)")
    void triggerInvalidHeader() {
        String xmlContent = data.getPropertiesFromXMLFile(XLMPropertiesReaderData.XML_FILE_TRIGGER_INVALID_HEADER);
        Throwable exception = assertThrows(PropertiesReaderException.class,
                () -> XMLStorageReader.read(xmlContent)
        );
        String expectedMessage = "Invalid value to header property in user-auth trigger element";
        assertEquals(expectedMessage, exception.getMessage());
    }

    @Test
    @DisplayName("Read Properties XML File: Invalid ID - default (trigger)")
    void triggerInvalidId() {
        String xmlContent = data.getPropertiesFromXMLFile(XLMPropertiesReaderData.XML_FILE_TRIGGER_ID_DEFAULT);
        Throwable exception = assertThrows(PropertiesReaderException.class,
                () -> XMLStorageReader.read(xmlContent)
        );
        String expectedMessage = "Invalid value to attribute 'id': keyword 'default' is not allowed";
        assertEquals(expectedMessage, exception.getMessage());
    }

    @Test
    @DisplayName("Read Properties XML File: Invalid default value (trigger)")
    void triggerInvalidDefaultValue() {
        String xmlContent = data.getPropertiesFromXMLFile(XLMPropertiesReaderData.XML_FILE_TRIGGER_INVALID_DEFAULT_VALUE);
        Throwable exception = assertThrows(PropertiesReaderException.class,
                () -> XMLStorageReader.read(xmlContent)
        );
        String expectedMessage = "Invalid value to default attribute in trigger element: trigger-default";
        assertEquals(expectedMessage, exception.getMessage());
    }

    @Test
    @DisplayName("Read Properties XML File: Multiple default (trigger)")
    void triggerMultipleDefault() {
        String xmlContent = data.getPropertiesFromXMLFile(XLMPropertiesReaderData.XML_FILE_TRIGGER_MULTIPLE_DEFAULT);
        Throwable exception = assertThrows(PropertiesReaderException.class,
                () -> XMLStorageReader.read(xmlContent)
        );
        String expectedMessage = "Multiple default triggers found. Only one default trigger is allowed.";
        assertEquals(expectedMessage, exception.getMessage());
    }


    // SCHEMA

    @Test
    @DisplayName("Read Properties XML File: Duplicated schema's name (schema)")
    void duplicatedSchemaName() {
        String xmlContent = data.getPropertiesFromXMLFile(XLMPropertiesReaderData.XML_FILE_DUPLICATED_SCHEMA_NAME);
        Throwable exception = assertThrows(PropertiesReaderException.class,
                () -> XMLStorageReader.read(xmlContent)
        );
        String expectedMessage = "Duplicated name was found in schema elements: S3-Only";
        assertEquals(expectedMessage, exception.getMessage());
    }

    @Test
    @DisplayName("Read Properties XML File: Storage not found (schema)")
    void storageNotFound() {
        String xmlContent = data.getPropertiesFromXMLFile(XLMPropertiesReaderData.XML_FILE_STORAGE_NOT_FOUND);
        Throwable exception = assertThrows(PropertiesReaderException.class,
                () -> XMLStorageReader.read(xmlContent)
        );
        String expectedMessage = "Storage with id = AWS-Test not found";
        assertEquals(expectedMessage, exception.getMessage());
    }

    @Test
    @DisplayName("Read Properties XML File: Trigger not found (schema)")
    void triggerNotFound() {
        String xmlContent = data.getPropertiesFromXMLFile(XLMPropertiesReaderData.XML_FILE_TRIGGER_NOT_FOUND);
        Throwable exception = assertThrows(PropertiesReaderException.class,
                () -> XMLStorageReader.read(xmlContent)
        );
        String expectedMessage = "Trigger with id = other-trigger not found";
        assertEquals(expectedMessage, exception.getMessage());
    }

    @Test
    @DisplayName("Read Properties XML File: Schema with cache and temporary middle (schema)")
    void schemaWithCacheAndTemporaryMiddle() {
        String xmlContent = data.getPropertiesFromXMLFile(XLMPropertiesReaderData.XML_FILE_SUCCESS_WITH_SCHEMA_CACHE_AND_TEMPORARY_MIDDLE);
        Throwable exception = assertThrows(PropertiesReaderException.class,
                () -> XMLStorageReader.read(xmlContent)
        );
        String expectedMessage = "The S3-And-FileSystem schema is using a temporary storage with cache.";
        assertEquals(expectedMessage, exception.getMessage());
    }

}