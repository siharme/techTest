package com.db.dataplatform.techtest.server.service.impl;

import com.db.dataplatform.techtest.server.persistence.BlockTypeEnum;
import com.db.dataplatform.techtest.server.persistence.model.DataBodyEntity;
import com.db.dataplatform.techtest.server.persistence.repository.DataStoreRepository;
import com.db.dataplatform.techtest.server.service.DataBodyService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.persistence.EntityNotFoundException;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class DataBodyServiceImpl implements DataBodyService {

    private final DataStoreRepository dataStoreRepository;

    @Override
    public void saveDataBody(DataBodyEntity dataBody) {
        dataStoreRepository.save(dataBody);
    }

    @Override
    public List<DataBodyEntity> getDataByBlockType(BlockTypeEnum blockType) {
        return dataStoreRepository.findByDataHeaderEntityBlocktype(blockType);
    }

    @Override
    public void updateDataByBlockName(String blockName, BlockTypeEnum newBlockType) {
        Optional<DataBodyEntity> dataBodyEntity = dataStoreRepository.findByDataHeaderEntityName(blockName);

        if (dataBodyEntity.isPresent()){
            DataBodyEntity newDataBodyEntity = dataBodyEntity.get();
            newDataBodyEntity.getDataHeaderEntity().setBlocktype(newBlockType);
            dataStoreRepository.save(newDataBodyEntity);
        } else {
            throw new EntityNotFoundException("Data Block with block name " + blockName + " not found");
        }
    }
}
