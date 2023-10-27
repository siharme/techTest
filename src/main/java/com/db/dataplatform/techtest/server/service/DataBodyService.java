package com.db.dataplatform.techtest.server.service;

import com.db.dataplatform.techtest.server.persistence.BlockTypeEnum;
import com.db.dataplatform.techtest.server.persistence.model.DataBodyEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DataBodyService {
    void saveDataBody(DataBodyEntity dataBody);
    List<DataBodyEntity> getDataByBlockType(BlockTypeEnum blockType);
    void updateDataByBlockName(String blockName, BlockTypeEnum newBlockType);
}
