package br.com.mpps.filehub.infrastructure.config;

import br.com.mpps.filehub.domain.exceptions.PropertiesReaderException;
import br.com.mpps.filehub.domain.model.IgnoreProperty;
import br.com.mpps.filehub.domain.model.OptionalProperty;
import br.com.mpps.filehub.domain.model.config.Schema;
import br.com.mpps.filehub.domain.model.config.Storage;
import br.com.mpps.filehub.domain.model.config.StorageResource;
import br.com.mpps.filehub.domain.model.config.Trigger;
import br.com.mpps.filehub.domain.model.storage.EnumHttpMethod;
import br.com.mpps.filehub.domain.model.storage.EnumStorageType;
import br.com.mpps.filehub.domain.model.storage.EnumTriggerAction;
import br.com.mpps.filehub.domain.model.storage.StorageProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.StringReader;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;

public class XMLStorageReader {

    public StorageResource read(String xmlContent) {
        StorageResource resource = null;
        try {
            resource = buildSchemaMapFromXMLDocument(xmlContent);
        } catch (InstantiationException |
                IllegalAccessException |
                ParserConfigurationException |
                IOException |
                SAXException |
                InvocationTargetException |
                NoSuchMethodException e) {
            e.printStackTrace();
        }
        return resource;
    }

    private StorageResource buildSchemaMapFromXMLDocument(String xmlContent)
            throws InstantiationException, IllegalAccessException,
            ParserConfigurationException, IOException, SAXException,
            InvocationTargetException, NoSuchMethodException {
        Document document = createDocument(xmlContent);
        NodeList storagesMainTag = document.getElementsByTagName("storages");
        Schema autoMainSchema = getAutoSchemaFromStoragesTag(storagesMainTag);
        NodeList storageNodes = document.getElementsByTagName("storage");
        Map<String, Storage> storages = readStorages(storageNodes, autoMainSchema);
        NodeList triggerNodes = document.getElementsByTagName("trigger");
        Map<String, Trigger> triggers = readTriggers(triggerNodes);
        NodeList schemaNodes = document.getElementsByTagName("schema");
        Map<String, Schema> schemas = readSchemas(schemaNodes, storages, triggers);
        addDefaultTriggerToSchemasWithoutTrigger(triggers, schemas);
        return new StorageResource(storages, triggers, schemas);
    }

    private void addDefaultTriggerToSchemasWithoutTrigger(Map<String, Trigger> triggers, Map<String, Schema> schemas) {
        Trigger defaultTrigger = triggers.get("default");
        if(defaultTrigger != null) {
            schemas.forEach((key, schema) -> {
                if(!schema.hasTrigger()) {
                    schema.setTrigger(defaultTrigger);
                }
            });
        }
    }

    private Schema getAutoSchemaFromStoragesTag(NodeList storagesMainTag) {
        if(storagesMainTag.getLength() == 0) {
            throw new PropertiesReaderException("Storages main element not defined");
        }
        if(storagesMainTag.getLength() > 1) {
            throw new PropertiesReaderException("Multiple storages main element defined");
        }
        Node storagesNode = storagesMainTag.item(0);
        if(storagesNode.getNodeType() != Node.ELEMENT_NODE) {
            throw new PropertiesReaderException("An invalid format was found in storages element");
        }
        Element storagesElement = (Element) storagesNode;
        String autoSchemaName = storagesElement.getAttribute("generate-schema");
        Schema autoSchema = null;
        if(autoSchemaName != null && !autoSchemaName.isEmpty()) {
            autoSchema = new Schema(autoSchemaName);
        }
        return autoSchema;
    }

    private Document createDocument(String xmlContent) throws ParserConfigurationException, IOException, SAXException {
        DocumentBuilderFactory documentFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder documentBuilder = documentFactory.newDocumentBuilder();
        InputSource inputSource = new InputSource(new StringReader(xmlContent));
        return documentBuilder.parse(inputSource);
    }

    private Map<String, Storage> readStorages(NodeList nodes, Schema autoMainSchema) throws InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        Map<String, Storage> storages = new LinkedHashMap<>();
        for(int i=0; i<nodes.getLength(); i++) {
            Node storageNode = nodes.item(i);
            if(storageNode.getNodeType() != Node.ELEMENT_NODE) {
                throw new PropertiesReaderException("An invalid format was found in storage element");
            }
            Element storageElement = (Element) storageNode;
            String storageId = storageElement.getAttribute("id");
            if(storageId.isEmpty()) {
                throw new PropertiesReaderException("Attribute 'id' not found in storage element");
            }
            if(storages.containsKey(storageId)) {
                throw new PropertiesReaderException("Duplicated id was found in storage elements: " + storageId);
            }
            String typeAttribute = storageElement.getAttribute("type");
            if(typeAttribute.isEmpty()) {
                throw new PropertiesReaderException("Attribute 'type' not found in storage element");
            }
            EnumStorageType storageType = EnumStorageType.get(typeAttribute);
            StorageProperties properties = getPropertiesFromStorageNode(storageType, storageId, storageElement);
            Storage storage = storageType.getStorage(storageId, properties);
            if(storageElement.hasAttribute("generate-schema")) {
                storage.setAutoSchema(storageElement.getAttribute("generate-schema"));
            }
            storages.put(storageId, storage);
            if(autoMainSchema != null) {
                autoMainSchema.getStorages().add(storage);
            }
        }
        return storages;
    }

    private Map<String, Trigger> readTriggers(NodeList nodes) {
        Map<String, Trigger> triggers = new LinkedHashMap<>();
        for(int i=0; i<nodes.getLength(); i++) {
            Node triggerNode = nodes.item(i);
            if(triggerNode.getNodeType() != Node.ELEMENT_NODE) {
                throw new PropertiesReaderException("An invalid format was found in trigger element");
            }
            Element triggerElement = (Element) triggerNode;
            String triggerId = triggerElement.getAttribute("id");
            if("default".equals(triggerId)) {
                throw new PropertiesReaderException("Invalid value to attribute 'id': keyword 'default' is not allowed");
            }
            if(triggerId.isEmpty()) {
                throw new PropertiesReaderException("Attribute 'id' not found in trigger element");
            }
            if(triggers.containsKey(triggerId)) {
                throw new PropertiesReaderException("Duplicated id was found in trigger elements: " + triggerId);
            }
            EnumTriggerAction triggerAction = EnumTriggerAction.ALL;
            String actionAttribute = triggerElement.getAttribute("action");
            if(!actionAttribute.isEmpty()) {
                triggerAction = EnumTriggerAction.get(actionAttribute);
            }
            boolean isTriggerDefault = checkBooleanElement(triggerId, triggerElement, "trigger", "default");
            boolean allowDirOperations = checkBooleanElement(triggerId, triggerElement, "trigger", "no-dir");
            Trigger trigger = getPropertiesFromTriggerNode(triggerId, triggerAction, triggerElement);
            trigger.setAllowDirOperations(allowDirOperations);
            triggers.put(triggerId, trigger);
            if(isTriggerDefault) {
                if(triggers.get("default") != null) {
                    throw new PropertiesReaderException("Multiple default triggers found. Only one default trigger is allowed.");
                }
                triggers.put("default", trigger);
            }
        }
        return triggers;
    }

    private Trigger getPropertiesFromTriggerNode(String triggerId, EnumTriggerAction triggerAction, Element triggerElement) {
        Trigger trigger = new Trigger();
        trigger.setId(triggerId);
        trigger.setAction(triggerAction);
        String url = getSingleProperty(triggerElement, triggerId, "trigger", "url");
        String header = getSingleProperty(triggerElement, triggerId, "trigger", "header");
        String httpMethod = getOptionalProperty(triggerElement, "http-method", "GET");
        EnumHttpMethod triggerHttpMethod = EnumHttpMethod.get(httpMethod);
        urlValidation(triggerId, url);
        headerValidation(triggerId, header);
        trigger.setUrl(url);
        trigger.setHeader(header);
        trigger.setHttpMethod(triggerHttpMethod);
        return trigger;
    }

    private StorageProperties getPropertiesFromStorageNode(EnumStorageType storageType, String storageName, Element storageElement) throws InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        StorageProperties storageProperties = storageType.getPropertiesClass().getDeclaredConstructor().newInstance();
        Field[] fields = storageType.getPropertiesClass().getDeclaredFields();
        for(Field field : fields) {
            if(!field.isAnnotationPresent(IgnoreProperty.class)) {
                boolean isOptionalField = field.isAnnotationPresent(OptionalProperty.class);
                NodeList propertyNode = storageElement.getElementsByTagName(field.getName());
                String propertyValue;
                if (propertyNode.item(0) == null) {
                    if(!isOptionalField) {
                        throw new PropertiesReaderException("The property " + field.getName() + " not found in the storage " + storageName);
                    }
                    propertyValue = field.getDeclaredAnnotation(OptionalProperty.class).defaultValue();
                } else {
                    propertyValue = propertyNode.item(0).getTextContent();
                }
                field.setAccessible(true);
                field.set(storageProperties, convertStringToType(field, propertyValue));
                field.setAccessible(false);
            }
        }
        return storageProperties;
    }

    private Object convertStringToType(Field field, String value) {
        Object result = value;
        try {
            if(!field.getType().equals(String.class)) {
                result = new ObjectMapper().readValue(value, field.getType());
            }
        } catch (IOException e) {
            throw new PropertiesReaderException("Error to convert String to " + field.getType().getName() + " (field: " + field.getName() + ")");
        }
        return result;
    }

    private Map<String, Schema> readSchemas(NodeList nodes, Map<String, Storage> storages, Map<String, Trigger> triggers) {
        Map<String, Schema> schemas = createSchemaMapIncludingAutoGeneratedSchemas(storages);
        for(int i=0; i<nodes.getLength(); i++) {
            Node schemaNode = nodes.item(i);
            if(schemaNode.getNodeType() != Node.ELEMENT_NODE) {
                throw new PropertiesReaderException("An invalid format was found in schema element");
            }
            Element schemaElement = (Element) schemaNode;
            String schemaName = schemaElement.getAttribute("name");
            if(schemaName.isEmpty()) {
                throw new PropertiesReaderException("Attribute 'name' not found in schema element");
            }
            if(schemas.containsKey(schemaName)) {
                throw new PropertiesReaderException("Duplicated name was found in schema elements: " + schemaName);
            }
            if(!schemaElement.hasChildNodes()) {
                throw new PropertiesReaderException("The " + schemaName + " schema is empty");
            }
            Trigger schemaTrigger = getTriggerFromSchema(schemaElement, triggers);
            boolean isEnabledCache = checkBooleanElement(schemaName, schemaElement, "schema", "cache");
            LinkedList<Storage> storageList = getStoragesFromSchema(schemaElement, storages);
            Storage middleStorage = getMiddleStorage(schemaElement, storages);
            schemas.put(schemaName, new Schema(schemaName, schemaTrigger, middleStorage, storageList, isEnabledCache));
        }
        return schemas;
    }

    private Map<String, Schema> createSchemaMapIncludingAutoGeneratedSchemas(Map<String, Storage> storages) {
        Map<String, Schema> schemas = new LinkedHashMap<>();
        Set<String> storageKeys = storages.keySet();
        for(String key : storageKeys) {
            Storage storage = storages.get(key);
            if(storage.getAutoSchema() != null) {
                String schemaName = storage.getAutoSchema();
                schemas.put(schemaName, new Schema(schemaName, storage));
            }
        }
        return schemas;
    }

    private LinkedList<Storage> getStoragesFromSchema(Element schemaElement, Map<String, Storage> storages) {
        LinkedList<Storage> storageList = new LinkedList<>();
        NodeList storagesNodes = schemaElement.getElementsByTagName("storage-id");
        for(int i=0; i<storagesNodes.getLength(); i++) {
            String storageId = storagesNodes.item(i).getTextContent();
            Storage storage = storages.get(storageId);
            if(storage == null) {
                throw new PropertiesReaderException("Storage with id = " + storageId + " not found");
            }
            if(!storageList.contains(storage)) {
                storageList.add(storage);
            }
        }
        return storageList;
    }

    private Trigger getTriggerFromSchema(Element schemaElement, Map<String, Trigger> triggers) {
        Trigger trigger = null;
        String triggerAttribute = schemaElement.getAttribute("trigger");
        if(triggerAttribute != null && !triggerAttribute.isEmpty()) {
            trigger = triggers.get(triggerAttribute);
            if(trigger == null) {
                throw new PropertiesReaderException("Trigger with id = " + triggerAttribute + " not found");
            }
        }
        return trigger;
    }

    private Storage getMiddleStorage(Element schemaElement, Map<String, Storage> storages) {
        Storage middleStorage = null;
        String middleStorageId = schemaElement.getAttribute("middle");
        if(!middleStorageId.isEmpty()) {
            middleStorage = storages.get(middleStorageId);
        }
        return middleStorage;
    }

    private String getSingleProperty(Element triggerElement, String elementId, String elementName, String propertyName) {
        NodeList properties = triggerElement.getElementsByTagName(propertyName);
        if (properties.getLength() > 1) {
            throw new PropertiesReaderException("It was found more than one " + propertyName + " property in " + elementName + " " + elementId);
        }
        if (properties.getLength() == 0) {
            throw new PropertiesReaderException("The property " + propertyName + " not found in the " +  elementName + " " + elementId);
        }
        return properties.item(0).getTextContent();
    }

    private String getOptionalProperty(Element triggerElement, String propertyName, String defaultValue) {
        NodeList properties = triggerElement.getElementsByTagName(propertyName);
        String value = defaultValue;
        if (properties.getLength() == 1) {
            value = properties.item(0).getTextContent();
        }
        return value;
    }

    private void urlValidation(String triggerId, String url) {
        try {
            new URL(url).toURI();
        } catch (URISyntaxException | MalformedURLException e) {
            throw new PropertiesReaderException("Invalid value to URL property in " + triggerId + " trigger element");
        }
    }

    private void headerValidation(String triggerId, String header) {
        if(!header.matches("^[a-zA-Z0-9\\-]+$")) {
            throw new PropertiesReaderException("Invalid value to header property in " + triggerId + " trigger element");
        }
    }

    private boolean checkBooleanElement(String elementName, Element element, String typeElement, String attributeName) {
        boolean isCache = false;
        if(element.hasAttribute(attributeName)) {
            String value = element.getAttribute(attributeName).toLowerCase().trim();
            if(!value.equals("true") && !value.equals("false")) {
                throw new PropertiesReaderException("Invalid value to " + attributeName + " attribute in " + typeElement + " element: " + elementName);
            }
            isCache = "true".equals(value);
        }
        return isCache;
    }

}
