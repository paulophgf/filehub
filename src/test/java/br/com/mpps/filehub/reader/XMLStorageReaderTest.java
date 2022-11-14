package br.com.mpps.filehub.reader;

import br.com.mpps.filehub.domain.model.config.Schema;
import br.com.mpps.filehub.infrastructure.config.XMLStorageReader;
import br.com.mpps.filehub.domain.exceptions.PropertiesReaderException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

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

    @Disabled //TODO Check what the problem
    @Test
    @DisplayName("Read Properties XML File (success)")
    void successRead() {
        Map<String, Schema> model = data.createSchemasModel();
        String xmlContent = data.getPropertiesFromXMLFile(XLMPropertiesReaderData.XML_FILE_SUCCESS);
        Map<String, Schema> schemas = XMLStorageReader.read(xmlContent);
        assertEquals(schemas, model);
    }

    @Disabled //TODO Check what the problem
    @Test
    @DisplayName("Read Properties XML File: Schema with middle storage (success)")
    void successReadSchemaWithMiddleStorage() {
        Map<String, Schema> model = data.createSchemasModelWithMiddle(false);
        String xmlContent = data.getPropertiesFromXMLFile(XLMPropertiesReaderData.XML_FILE_SUCCESS_WITH_MIDDLE);
        Map<String, Schema> schemas = XMLStorageReader.read(xmlContent);
        assertEquals(schemas, model);
    }

    @Disabled //TODO Check what the problem
    @Test
    @DisplayName("Read Properties XML File: Schema with temporary middle storage (success)")
    void successReadSchemaWithTemporaryMiddleStorage() {
        Map<String, Schema> model = data.createSchemasModelWithMiddle(true);
        String xmlContent = data.getPropertiesFromXMLFile(XLMPropertiesReaderData.XML_FILE_SUCCESS_WITH_MIDDLE_TEMP);
        Map<String, Schema> schemas = XMLStorageReader.read(xmlContent);
        assertEquals(schemas, model);
    }

    @Disabled //TODO Check what the problem
    @Test
    @DisplayName("Read Properties XML File: Schema with trigger (success)")
    void successReadSchemaWithTrigger() {
        Map<String, Schema> model = data.createSchemasModelWithTrigger();
        String xmlContent = data.getPropertiesFromXMLFile(XLMPropertiesReaderData.XML_FILE_SUCCESS_WITH_TRIGGER);
        Map<String, Schema> schemas = XMLStorageReader.read(xmlContent);
        assertEquals(schemas, model);
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

    @Test
    @DisplayName("Read Properties XML File: Invalid storage's ID (storage)")
    void storageInvalidId() {
        String xmlContent = data.getPropertiesFromXMLFile(XLMPropertiesReaderData.XML_FILE_STORAGE_INVALID_ID);
        Throwable exception = assertThrows(PropertiesReaderException.class,
                () -> XMLStorageReader.read(xmlContent)
        );
        String expectedMessage = "ALL is an invalid storage id";
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
    @DisplayName("Read Properties XML File: Invalid schema's name (schema)")
    void invalidSchemaName() {
        String xmlContent = data.getPropertiesFromXMLFile(XLMPropertiesReaderData.XML_FILE_INVALID_SCHEMA_NAME);
        Throwable exception = assertThrows(PropertiesReaderException.class,
                () -> XMLStorageReader.read(xmlContent)
        );
        String expectedMessage = "ALL is an invalid schema name";
        assertEquals(expectedMessage, exception.getMessage());
    }

    @Test
    @DisplayName("Read Properties XML File: Schema name is equals a storage id (schema)")
    void schemaNameEqualsStorageId() {
        String xmlContent = data.getPropertiesFromXMLFile(XLMPropertiesReaderData.XML_FILE_SCHEMA_NAME_EQUALS_STORAGE_ID);
        Throwable exception = assertThrows(PropertiesReaderException.class,
                () -> XMLStorageReader.read(xmlContent)
        );
        String expectedMessage = "Schema name is equals a storage id: FileSystem-Test";
        assertEquals(expectedMessage, exception.getMessage());
    }

    @Test
    @DisplayName("Read Properties XML File: Schema with wrong type to middle storage (schema)")
    void schemaMiddleWrongType() {
        String xmlContent = data.getPropertiesFromXMLFile(XLMPropertiesReaderData.XML_FILE_SCHEMA_MIDDLE_WRONG_TYPE);
        Throwable exception = assertThrows(PropertiesReaderException.class,
                () -> XMLStorageReader.read(xmlContent)
        );
        String expectedMessage = "The attribute temp needs to be FILE_SYSTEM storage type";
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

}