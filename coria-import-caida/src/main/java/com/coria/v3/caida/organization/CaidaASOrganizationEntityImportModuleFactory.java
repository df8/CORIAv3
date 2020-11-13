package com.coria.v3.caida.organization;

import com.coria.v3.parser.ASOrganizationEntityImportModule;
import com.coria.v3.parser.ImportModuleFactory;
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
public class CaidaASOrganizationEntityImportModuleFactory implements ImportModuleFactory<ASOrganizationEntityImportModule> {
    /**
     * Description in html-views/CaidaASOrganizationEntityImportModuleDescription.html by David Fradin
     */
    @Value("classpath:html-views/CaidaASOrganizationEntityImportModuleDescription.html")
    private Resource descriptionViewFile;

    private List<ASOrganizationEntityImportModule> list;

    @Override
    public List<? extends ASOrganizationEntityImportModule> getList() {
        if (list != null)
            return list;
        list = new ArrayList<>();
        try (InputStreamReader isr = new InputStreamReader(descriptionViewFile.getInputStream(), UTF_8)) {
            list.add(new CaidaASOrganizationEntityImportModule("caida-as-organizations-import-module", "CAIDA AS-Organizations Import Module", FileCopyUtils.copyToString(isr)));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }
}
