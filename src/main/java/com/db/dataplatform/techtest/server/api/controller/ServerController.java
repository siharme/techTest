package com.db.dataplatform.techtest.server.api.controller;

import com.db.dataplatform.techtest.server.api.model.DataEnvelope;
import com.db.dataplatform.techtest.server.component.Server;
import com.db.dataplatform.techtest.server.persistence.BlockTypeEnum;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.List;

@Slf4j
@Controller
@RequestMapping("/dataserver")
@RequiredArgsConstructor
@Validated
public class ServerController {

    private final Server server;

    @PostMapping(value = "/pushdata", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Boolean> pushData(@Valid @RequestBody DataEnvelope dataEnvelope) throws IOException, NoSuchAlgorithmException {

        log.info("Data envelope received for persistance: {}", dataEnvelope.getDataHeader().getName());
        boolean checksumPass = server.saveDataEnvelope(dataEnvelope);
        if (checksumPass) {
            log.info("Data envelope persisted after Checksums matched. Attribute name: {}", dataEnvelope.getDataHeader().getName());

            //If data persists, save data to hadoop data lake
            server.saveDataEnvelopeToHadoop(dataEnvelope);

        } else {
            log.info("Data not persisted as Checksums do not match for the data");
        }
        return ResponseEntity.ok(checksumPass);
    }
    @GetMapping(value = "/data/{blockType}")
    public ResponseEntity<DataEnvelope[]> getData(@PathVariable BlockTypeEnum blockType) throws IOException, NoSuchAlgorithmException {

        log.info("Data query received for blockType: {}", blockType);
        List<DataEnvelope> list = server.getDataEnvelopes(blockType);
        DataEnvelope[] data = list.toArray(new DataEnvelope[0]);
        return ResponseEntity.ok(data);
    }

    @PutMapping(value = "/update/{name}/{newBlockType}")
    public ResponseEntity<Boolean> updateData(@PathVariable String name, @PathVariable BlockTypeEnum newBlockType) throws IOException, NoSuchAlgorithmException {

        log.info("Update query received for blockName: {}", name);
        boolean isUpdated = server.updateDataEnvelope(name, newBlockType);
        if (isUpdated) {
            log.info("Update done for blockName: {}", name);
        } else {
            log.info("Update not done as {} blockName not present", name);
        }
        return ResponseEntity.ok(isUpdated);
    }

}
