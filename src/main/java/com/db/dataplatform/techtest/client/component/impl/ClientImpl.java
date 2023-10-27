package com.db.dataplatform.techtest.client.component.impl;

import com.db.dataplatform.techtest.client.api.model.DataEnvelope;
import com.db.dataplatform.techtest.client.component.Client;
import com.db.dataplatform.techtest.server.persistence.BlockTypeEnum;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriTemplate;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Client code does not require any test coverage
 */

@Service
@Slf4j
@RequiredArgsConstructor
public class ClientImpl implements Client {

    public static final String URI_PUSHDATA = "http://localhost:8090/dataserver/pushdata";
    public static final UriTemplate URI_GETDATA = new UriTemplate("http://localhost:8090/dataserver/data/{blockType}");
    public static final UriTemplate URI_PATCHDATA = new UriTemplate("http://localhost:8090/dataserver/update/{name}/{newBlockType}");

    @Autowired
    RestTemplate restTemplate;

    @Override
    public void pushData(DataEnvelope dataEnvelope) {
        log.info("Pushing data {} to {}", dataEnvelope.getDataHeader().getName(), URI_PUSHDATA);
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json");
        HttpEntity<DataEnvelope> requestEntity = new HttpEntity<>(dataEnvelope, headers);

        ResponseEntity<Void> responseEntity = restTemplate.exchange(URI_PUSHDATA,
                HttpMethod.POST,
                requestEntity,
                Void.class
        );

        if (responseEntity.getStatusCode().is2xxSuccessful()) {
            System.out.println("GET request successful");
        } else {
            System.out.println("GET request failed with status code: " + responseEntity.getStatusCode());
        }
    }

    @Override
    public List<DataEnvelope> getData(String blockType) {
        log.info("Query for data with header block type {}", blockType);
        String uri = URI_GETDATA.expand(blockType).toString();
        ResponseEntity<DataEnvelope[]> response  = restTemplate.getForEntity(uri, DataEnvelope[].class);
        DataEnvelope[] dataEnvelopes = response.getBody();
        List<DataEnvelope> dataEnvelopesList = Arrays.stream(dataEnvelopes).collect(Collectors.toList());
        log.info("Data envelope by blockType {} returned successfully", blockType);
        dataEnvelopesList.forEach(s -> {
            log.info(s.getDataHeader().getName());
        });
        if (response.getStatusCode().is2xxSuccessful()) {
            System.out.println("POST request successful");
        } else {
            System.out.println("POST request failed with status code: " + response.getStatusCode());
        }
        return dataEnvelopesList;
    }

    @Override
    public void updateData(String blockName, String newBlockTypeReceived) {
        log.info("Updating blocktype to {} for block with name {}", newBlockTypeReceived, blockName);
        String uri = URI_PATCHDATA.expand(blockName,newBlockTypeReceived).toString();

        HttpHeaders headers = new HttpHeaders();
        BlockTypeEnum newBlockType = BlockTypeEnum.valueOf(newBlockTypeReceived);
        headers.add("Content-Type", "application/json");
        HttpEntity<BlockTypeEnum> requestEntity = new HttpEntity<>(newBlockType, headers);

        ResponseEntity<Void> responseEntity = restTemplate.exchange(uri,
                HttpMethod.PUT,
                requestEntity,
                Void.class
        );

        if (responseEntity.getStatusCode().is2xxSuccessful()) {
            System.out.println("PUT request successful");
        } else {
            System.out.println("PUT request failed with status code: " + responseEntity.getStatusCode());
        }
    }

}
