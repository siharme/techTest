package com.db.dataplatform.techtest.server.component.impl;

import com.db.dataplatform.techtest.server.api.model.DataBody;
import com.db.dataplatform.techtest.server.api.model.DataEnvelope;
import com.db.dataplatform.techtest.server.api.model.DataHeader;
import com.db.dataplatform.techtest.server.persistence.BlockTypeEnum;
import com.db.dataplatform.techtest.server.persistence.model.DataBodyEntity;
import com.db.dataplatform.techtest.server.persistence.model.DataHeaderEntity;
import com.db.dataplatform.techtest.server.service.DataBodyService;
import com.db.dataplatform.techtest.server.component.Server;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.persistence.EntityNotFoundException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;


@Slf4j
@Service
@RequiredArgsConstructor
public class ServerImpl implements Server {

    private final DataBodyService dataBodyServiceImpl;
    private final ModelMapper modelMapper;
    private ObjectMapper objectMapper;

    @Autowired
    RestTemplate restTemplate;

    public static final String URI_PUSHDATALAKE = "http://localhost:8090/hadoopserver/pushbigdata";

    /**
     * @param envelope
     * @return true if there is a match with the client provided checksum.
     */
    @Override
    public boolean saveDataEnvelope(DataEnvelope envelope) {

        // Save to persistence.
        boolean isDataPersist;
        isDataPersist = persist(envelope);
        return isDataPersist;
    }

    @Override
    public List<DataEnvelope> getDataEnvelopes(BlockTypeEnum blockType) {

        // Get DataEnvelopes.
        List<DataEnvelope> dataEnvelopeList = new ArrayList<>();
        List<DataBodyEntity> dataBodyEntities = getDataByBlockType(blockType);
        for (DataBodyEntity dataBodyEntity : dataBodyEntities) {
            DataBody dataBody = new DataBody(dataBodyEntity.getDataBody(), dataBodyEntity.getDataChecksum());
            DataHeader dataHeader = new DataHeader(dataBodyEntity.getDataHeaderEntity().getName(),dataBodyEntity.getDataHeaderEntity().getBlocktype());
            DataEnvelope dataEnvelope = new DataEnvelope(dataHeader, dataBody);
            dataEnvelopeList.add(dataEnvelope);
        }
        if (dataBodyEntities.size()>0){
            log.info("Returning data envelopes successfully");
        } else {
            log.info("No data envelopes present with blockType : {} ", blockType);
        }
        return dataEnvelopeList;
    }

    @Override
    public boolean updateDataEnvelope(String blockName, BlockTypeEnum newBlockType) {

        // Update DataEnvelope.
        try{
            dataBodyServiceImpl.updateDataByBlockName(blockName, newBlockType);
            return true;
        } catch (EntityNotFoundException ex){
            log.info("Data Block with block name {} not found", blockName);
            return false;
        }
    }

    private boolean persist(DataEnvelope envelope) {
        boolean isValidData;
        log.info("Persisting data with attribute name: {}", envelope.getDataHeader().getName());
        DataHeaderEntity dataHeaderEntity = modelMapper.map(envelope.getDataHeader(), DataHeaderEntity.class);

        DataBodyEntity dataBodyEntity = modelMapper.map(envelope.getDataBody(), DataBodyEntity.class);
        dataBodyEntity.setDataHeaderEntity(dataHeaderEntity);

        String calCheckSum = calculateMD5Checksum(envelope.getDataBody().getDataBody().getBytes(StandardCharsets.UTF_8));
        if(calCheckSum.equals(envelope.getDataBody().getDataChecksum())){
            saveData(dataBodyEntity);
            isValidData = true;
        } else {
            isValidData = false;
        }
        return isValidData;
    }

    private void saveData(DataBodyEntity dataBodyEntity) {
        dataBodyServiceImpl.saveDataBody(dataBodyEntity);
    }

    public List<DataBodyEntity> getDataByBlockType(BlockTypeEnum blockType) {
        return dataBodyServiceImpl.getDataByBlockType(blockType);
    }

    private String calculateMD5Checksum(byte[] data) {
        log.info("Calculating md5 checksum for the data");
        return DigestUtils.md5Hex(data);
    }

    public void saveDataEnvelopeToHadoop(DataEnvelope dataEnvelope) {
        log.info("Pushing data {} to {}", dataEnvelope.getDataHeader().getName(), URI_PUSHDATALAKE);

        // Convert DataEnvelope to JSON payload (you may use a library like Jackson)
        String jsonPayload = convertDataEnvelopeToJson(dataEnvelope);

        // Create HttpHeaders for the Hadoop data lake request
        HttpHeaders lakeHeaders = new HttpHeaders();
        lakeHeaders.add("Content-Type", "application/json");

        // Create an HttpEntity with the JSON payload and headers
        HttpEntity<String> lakeRequestEntity = new HttpEntity<>(jsonPayload, lakeHeaders);

        // Make an HTTP POST request to the Hadoop data lake service
        ResponseEntity<Void> lakeResponseEntity = restTemplate.exchange(
                URI_PUSHDATALAKE,
                HttpMethod.POST,
                lakeRequestEntity,
                Void.class
        );

        if (lakeResponseEntity.getStatusCode().is2xxSuccessful()) {
            log.info("Data pushed to Hadoop data lake successfully");
        } else {
            log.error("Data push to Hadoop data lake failed with status code: {}", lakeResponseEntity.getStatusCode());
        }
    }

    private String convertDataEnvelopeToJson(DataEnvelope dataEnvelope){
        objectMapper = new ObjectMapper();
        String jsonPayload = null;
        try {
            jsonPayload = objectMapper.writeValueAsString(dataEnvelope);
        } catch (Exception e) {
            log.error("Error converting DataEnvelope to JSON: {}", e.getMessage());
        }
        return jsonPayload;
    }

}
