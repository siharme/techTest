package com.db.dataplatform.techtest.server.component;

import com.db.dataplatform.techtest.server.api.model.DataEnvelope;
import com.db.dataplatform.techtest.server.persistence.BlockTypeEnum;
import com.db.dataplatform.techtest.server.persistence.model.DataBodyEntity;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Optional;

public interface Server {
    boolean saveDataEnvelope(DataEnvelope envelope) throws IOException, NoSuchAlgorithmException;

    List<DataEnvelope> getDataEnvelopes(BlockTypeEnum blockType) throws IOException, NoSuchAlgorithmException;

    boolean updateDataEnvelope(String blockName, BlockTypeEnum newBlockType) throws IOException, NoSuchAlgorithmException;

    void saveDataEnvelopeToHadoop(DataEnvelope envelope) throws IOException, NoSuchAlgorithmException;
}
