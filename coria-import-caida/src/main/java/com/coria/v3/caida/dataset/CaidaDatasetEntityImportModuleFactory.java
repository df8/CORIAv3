package com.coria.v3.caida.dataset;

import com.coria.v3.parser.ImportModuleFactory;
import com.coria.v3.parser.DatasetEntityImportModule;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.util.FileCopyUtils;

import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Created by David Fradin, 2020
 */
@Component
public class CaidaDatasetEntityImportModuleFactory implements ImportModuleFactory<DatasetEntityImportModule> {
    /**
     * Technical reference in html-views/CaidaDatasetEntityImportModuleDescription.html - composed by David Fradin, 2020.
     */
    @Value("classpath:html-views/CaidaDatasetEntityImportModuleDescription.html")
    private Resource descriptionViewFile;

    private List<DatasetEntityImportModule> list;

    @Override
    public List<? extends DatasetEntityImportModule> getList() {
        if (list != null)
            return list;
        list = new ArrayList<>();
        try (InputStreamReader isr = new InputStreamReader(descriptionViewFile.getInputStream(), UTF_8)) {
            list.add(new CaidaDatasetEntityImportModule("caida-as-links-import-module", "CAIDA AS-Links Import Module", FileCopyUtils.copyToString(isr)));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }
}
