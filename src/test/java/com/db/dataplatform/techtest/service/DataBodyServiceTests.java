package com.db.dataplatform.techtest.service;

import com.db.dataplatform.techtest.server.persistence.BlockTypeEnum;
import com.db.dataplatform.techtest.server.persistence.model.DataBodyEntity;
import com.db.dataplatform.techtest.server.persistence.model.DataHeaderEntity;
import com.db.dataplatform.techtest.server.persistence.repository.DataStoreRepository;
import com.db.dataplatform.techtest.server.service.DataBodyService;
import com.db.dataplatform.techtest.server.service.impl.DataBodyServiceImpl;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static com.db.dataplatform.techtest.TestDataHelper.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class DataBodyServiceTests {

    public static final String TEST_NAME_NO_RESULT = "TestNoResult";

    @Mock
    private DataStoreRepository dataStoreRepositoryMock;

    private DataBodyService dataBodyService;
    private DataBodyEntity expectedDataBodyEntity, existingDataBody, updatedDataBody;
    //List<DataBodyEntity> dataBodyEntities;

    private BlockTypeEnum blockType;

    private String blockName = "Test";
    BlockTypeEnum newBlockType = BlockTypeEnum.BLOCKTYPEB;

    @Before
    public void setup() {
        String blockName = "TestBlock";
        DataHeaderEntity testDataHeaderEntity = createTestDataHeaderEntity(Instant.now());
        expectedDataBodyEntity = createTestDataBodyEntity(testDataHeaderEntity);
        blockType = BlockTypeEnum.BLOCKTYPEA;
        existingDataBody = createTestDataBodyEntity1(blockName, BlockTypeEnum.BLOCKTYPEA);
        updatedDataBody = createTestDataBodyEntity1(blockName, newBlockType);
        dataBodyService = new DataBodyServiceImpl(dataStoreRepositoryMock);
    }

    @Test
    public void shouldSaveDataBodyEntityAsExpected(){
        dataBodyService.saveDataBody(expectedDataBodyEntity);

        verify(dataStoreRepositoryMock, times(1))
                .save(eq(expectedDataBodyEntity));
    }

    @Test
    public void shouldGetDataByBlockTypeAsExpected(){
        dataBodyService.getDataByBlockType(blockType);

        verify(dataStoreRepositoryMock, times(1))
                .findByDataHeaderEntityBlocktype(eq(blockType));
    }

    @Test
    public void shouldUpdateDataByBlockNameAsExpected(){

        when(dataStoreRepositoryMock.findByDataHeaderEntityName(blockName)).thenReturn(Optional.of(existingDataBody));
        when(dataStoreRepositoryMock.save(any(DataBodyEntity.class))).thenReturn(updatedDataBody);

        dataBodyService.updateDataByBlockName(blockName, newBlockType);

        verify(dataStoreRepositoryMock).findByDataHeaderEntityName(blockName);
        verify(dataStoreRepositoryMock).save(updatedDataBody);
    }

}
