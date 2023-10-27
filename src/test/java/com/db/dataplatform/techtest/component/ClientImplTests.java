package com.db.dataplatform.techtest.component;

import com.db.dataplatform.techtest.client.api.model.DataBody;
import com.db.dataplatform.techtest.client.api.model.DataEnvelope;
import com.db.dataplatform.techtest.client.api.model.DataHeader;
import com.db.dataplatform.techtest.client.component.impl.ClientImpl;
import com.db.dataplatform.techtest.server.persistence.BlockTypeEnum;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;

import static com.db.dataplatform.techtest.Constant.DUMMY_DATA;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
public class ClientImplTests {

    @Mock
    private RestTemplate restTemplate;
    @InjectMocks
    private ClientImpl client = new ClientImpl();

    public static final String URI_PUSHDATA = "http://localhost:8090/dataserver/pushdata";
    public static final String HEADER_NAME = "TSLA-USDGBP-10Y";
    public static final String MD5_CHECKSUM = "cecfd3953783df706878aaec2c22aa70";

    @Test
    public void testGetData() {
        // Define test data
        DataEnvelope[] testEnvelopes = createTestDataEnvelopes();
        ResponseEntity<DataEnvelope[]> responseEntity = new ResponseEntity<>(testEnvelopes, HttpStatus.OK);

        Mockito.when(restTemplate.getForEntity(Mockito.anyString(), Mockito.eq(DataEnvelope[].class)))
                .thenReturn(responseEntity);

        // Call the method under test
        List<DataEnvelope> result = client.getData("BLOCKTYPEA");

        assertEquals(1, result.size());
        assertEquals(Arrays.asList(testEnvelopes), result);
    }

    @Test
    public void testPushData() {
        // Create a DataEnvelope for testing
        DataEnvelope testDataEnvelope = createTestDataEnvelope();

        ResponseEntity<Void> successResponseEntity = new ResponseEntity<>(null, HttpStatus.OK);

        Mockito.when(restTemplate.exchange(Mockito.eq(ClientImpl.URI_PUSHDATA), Mockito.eq(HttpMethod.POST), any(HttpEntity.class), Mockito.eq(Void.class)))
                .thenReturn(successResponseEntity);

        // Call the method under test
        client.pushData(testDataEnvelope);

        verify(restTemplate).exchange(Mockito.eq(ClientImpl.URI_PUSHDATA), Mockito.eq(HttpMethod.POST), any(HttpEntity.class), Mockito.eq(Void.class));
    }

    @Test
    public void testUpdateData() {
        String blockName = "TestBlock";
        String newBlockTypeReceived = "BLOCKTYPEB";
        String uri = "http://localhost:8090/dataserver/update/TestBlock/BLOCKTYPEB";

        BlockTypeEnum newBlockType = BlockTypeEnum.BLOCKTYPEB;
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json");

        HttpEntity<BlockTypeEnum> requestEntity = new HttpEntity<>(newBlockType, headers);
        ResponseEntity<Void> successResponseEntity = new ResponseEntity<>(null, HttpStatus.OK);

        Mockito.when(restTemplate.exchange(Mockito.eq(uri), Mockito.eq(HttpMethod.PUT), Mockito.eq(requestEntity), Mockito.eq(Void.class)))
                .thenReturn(successResponseEntity);

        client.updateData(blockName, newBlockTypeReceived);

        verify(restTemplate).exchange(Mockito.eq(uri), Mockito.eq(HttpMethod.PUT), Mockito.eq(requestEntity), Mockito.eq(Void.class));
    }

    private DataEnvelope createTestDataEnvelope() {
        // Create a list of DataEnvelopes for testing
        DataBody dataBody = new DataBody(DUMMY_DATA, MD5_CHECKSUM);
        DataHeader dataHeader = new DataHeader(HEADER_NAME, BlockTypeEnum.BLOCKTYPEA);
        DataEnvelope dataEnvelope = new DataEnvelope(dataHeader, dataBody);
        return dataEnvelope;
    }

    private DataEnvelope[] createTestDataEnvelopes() {
        // Create an array of DataEnvelopes for testing
        DataBody dataBody = new DataBody(DUMMY_DATA, MD5_CHECKSUM);
        DataHeader dataHeader = new DataHeader(HEADER_NAME, BlockTypeEnum.BLOCKTYPEA);
        DataEnvelope dataEnvelope = new DataEnvelope(dataHeader, dataBody);
        return new DataEnvelope[]{dataEnvelope};
    }
}
