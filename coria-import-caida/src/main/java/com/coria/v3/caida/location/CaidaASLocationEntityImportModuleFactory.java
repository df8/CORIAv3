package com.coria.v3.caida.location;

import com.coria.v3.parser.ASLocationEntityImportModule;
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
public class CaidaASLocationEntityImportModuleFactory implements ImportModuleFactory<ASLocationEntityImportModule> {
    /**
     * Description in html-views/CaidaImportModuleDescription.html by David Fradin
     */
    @Value("classpath:html-views/CaidaASLocationEntityImportModuleDescription.html")
    private Resource descriptionViewFile;

    private List<ASLocationEntityImportModule> list;

    @Override
    public List<? extends ASLocationEntityImportModule> getList() {
        if (list != null)
            return list;
        list = new ArrayList<>();
        try (InputStreamReader isr = new InputStreamReader(descriptionViewFile.getInputStream(), UTF_8)) {
            list.add(new CaidaASLocationEntityImportModule("caida-as-locations-import-module", "CAIDA AS-Locations Import Module", FileCopyUtils.copyToString(isr)));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }
}
