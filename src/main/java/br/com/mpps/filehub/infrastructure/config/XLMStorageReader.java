package br.com.mpps.filehub.infrastructure.config;

import br.com.mpps.filehub.domain.exceptions.PropertiesReaderException;
import br.com.mpps.filehub.domain.model.IgnoreProperty;
import br.com.mpps.filehub.domain.model.config.Schema;
import br.com.mpps.filehub.domain.model.config.Storage;
import br.com.mpps.filehub.domain.model.config.Trigger;
import br.com.mpps.filehub.domain.model.storage.EnumHttpMethod;
import br.com.mpps.filehub.domain.model.storage.EnumStorageType;
import br.com.mpps.filehub.domain.model.storage.EnumTriggerAction;
import br.com.mpps.filehub.domain.model.storage.StorageProperties;
import br.com.mpps.filehub.domain.model.storage.filesystem.FileSystemProperties;
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
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;

public class XLMStorageReader {

    public Map<String, Schema> read(String xmlContent) {
        Map<String, Schema> schemas = null;
        try {
            schemas = buildSchemaMapFromXMLDocument(xmlContent);
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        }
        return schemas;
    }

    private Map<String, Schema> buildSchemaMapFromXMLDocument(String xmlContent)
            throws InstantiationException, IllegalAccessException,
            ParserConfigurationException, IOException, SAXException {
        Document document = createDocument(xmlContent);
        NodeList storageNodes = document.getElementsByTagName("storage");
        Map<String, Storage> storages = readStorages(storageNodes);
        NodeList triggerNodes = document.getElementsByTagName("trigger");
        Map<String, Trigger> triggers = readTriggers(triggerNodes);
        NodeList schemaNodes = document.getElementsByTagName("schema");
        Map<String, Schema> schemas = readSchemas(schemaNodes, storages, triggers, false);
        return schemas;
    }

    private Document createDocument(String xmlContent) throws ParserConfigurationException, IOException, SAXException {
        DocumentBuilderFactory documentFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder documentBuilder = documentFactory.newDocumentBuilder();
        InputSource inputSource = new InputSource(new StringReader(xmlContent));
        return documentBuilder.parse(inputSource);
    }

    private Map<String, Storage> readStorages(NodeList nodes) throws InstantiationException, IllegalAccessException {
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
            if("ALL".equals(storageId)) {
                throw new PropertiesReaderException("ALL is an invalid storage id");
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
            storages.put(storageId, storage);
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
            Trigger trigger = getPropertiesFromTriggerNode(triggerId, triggerAction, triggerElement);
            triggers.put(triggerId, trigger);
        }
        return triggers;
    }

    private Trigger getPropertiesFromTriggerNode(String triggerId, EnumTriggerAction triggerAction, Element triggerElement) {
        Trigger trigger = new Trigger();
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

    private StorageProperties getPropertiesFromStorageNode(EnumStorageType storageType, String storageName, Element storageElement) throws InstantiationException, IllegalAccessException {
        StorageProperties storageProperties = storageType.getPropertiesClass().newInstance();
        Field[] fields = storageType.getPropertiesClass().getDeclaredFields();
        for(Field field : fields) {
            if(!field.isAnnotationPresent(IgnoreProperty.class)) {
                NodeList propertyNode = storageElement.getElementsByTagName(field.getName());
                if (propertyNode.item(0) == null) {
                    throw new PropertiesReaderException("The property " + field.getName() + " not found in the storage " + storageName);
                }
                String propertyValue = propertyNode.item(0).getTextContent();
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

    private Map<String, Schema> readSchemas(NodeList nodes, Map<String, Storage> storages, Map<String, Trigger> triggers, boolean includeDefault) {
        Map<String, Schema> schemas = includeDefault ? createSchemaMapIncludingDefaultStorages(storages) : new LinkedHashMap<>();
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
            if("ALL".equals(schemaName)) {
                throw new PropertiesReaderException("ALL is an invalid schema name");
            }
            if(storages.containsKey(schemaName)) {
                throw new PropertiesReaderException("Schema name is equals a storage id: " + schemaName);
            }
            if(schemas.containsKey(schemaName)) {
                throw new PropertiesReaderException("Duplicated name was found in schema elements: " + schemaName);
            }
            if(!schemaElement.hasChildNodes()) {
                throw new PropertiesReaderException("The " + schemaName + " schema is empty");
            }
            Trigger storageTrigger = getTriggerFromStorage(schemaElement, triggers);
            LinkedList<Storage> storageList = getStoragesFromSchema(schemaElement, storages);
            schemas.put(schemaName, new Schema(schemaName, storageTrigger, storageList));
        }
        return schemas;
    }

    private Map<String, Schema> createSchemaMapIncludingDefaultStorages(Map<String, Storage> storages) {
        Map<String, Schema> schemas = new LinkedHashMap<>();
        Collection<Storage> schemaAll = new LinkedList<>();
        Set<String> storageKeys = storages.keySet();
        for(String key : storageKeys) {
            List<Storage> schemaUniqueStorage = new LinkedList<>();
            schemaAll.add(storages.get(key));
            schemaUniqueStorage.add(storages.get(key));
            schemas.put(key, new Schema(key, schemaUniqueStorage, true));
        }
        schemas.put("ALL", new Schema("ALL", schemaAll, false));
        return schemas;
    }

    private LinkedList<Storage> getStoragesFromSchema(Element schemaElement, Map<String, Storage> storages) {
        LinkedList<Storage> storageList = new LinkedList<>();
        NodeList storagesNodes = schemaElement.getElementsByTagName("storage-id");
        Storage middleStorage = getMiddleStorage(schemaElement, storages);
        for(int i=0; i<storagesNodes.getLength(); i++) {
            String storageId = storagesNodes.item(i).getTextContent();
            Storage storage = storages.get(storageId);
            if(storage == null) {
                throw new PropertiesReaderException("Storage with id = " + storageId + " not found");
            }
            if(storage.equals(middleStorage)) {
                ((FileSystemProperties) middleStorage.getProperties()).setTemporary(false);
            } else {
                if(!storageList.contains(storage)) {
                    storageList.add(storage);
                }
            }
        }
        if(middleStorage != null) {
            middleStorage.setType(EnumStorageType.MIDDLE);
            storageList.addFirst(middleStorage);
        }
        return storageList;
    }

    private Trigger getTriggerFromStorage(Element schemaElement, Map<String, Trigger> triggers) {
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
            Storage storage = storages.get(middleStorageId);
            if(!EnumStorageType.FILE_SYSTEM.equals(storage.getType())) {
                throw new PropertiesReaderException("The attribute temp needs to be FILE_SYSTEM storage type");
            }
            FileSystemProperties propertiesClone = ((FileSystemProperties) storage.getProperties()).clone();
            propertiesClone.setTemporary(true);
            middleStorage = EnumStorageType.FILE_SYSTEM.getStorage(middleStorageId, propertiesClone);
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

}
