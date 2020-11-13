package com.coria.v3.resolver;

import com.coria.v3.config.AppContext;
import com.coria.v3.cuda.CudaDeviceInfo;
import com.coria.v3.export.ExportModule;
import com.coria.v3.metrics.MetricAlgorithmImplementation;
import com.coria.v3.metrics.ModuleBase;
import com.coria.v3.model.ImportModuleFilter;
import com.coria.v3.model.ListMetadata;
import com.coria.v3.model.ModuleFilter;
import com.coria.v3.parser.ImportModuleBase;
import graphql.kickstart.tools.GraphQLQueryResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by David Fradin, 2020
 * This resolver handles the GraphQL query requests for MetricAlgorithmImplementation, ImportModule, ExportModule and CudaDevice.
 */
@Component
public class ModulesQueryResolver extends BaseResolver implements GraphQLQueryResolver {
    private final static Logger logger = LoggerFactory.getLogger(ModulesQueryResolver.class);
    private final AppContext appContext;

    @Autowired
    public ModulesQueryResolver(AppContext appContext) {
        this.appContext = appContext;
    }

    private int compareModules(ModuleBase module1, ModuleBase module2, String sortField, String sortOrder) {
        int sortOrderSwitch = sortOrder.equals("ASC") ? 1 : -1;
        int result;
        switch (sortField.toLowerCase()) {
            case "name":
                result = sortOrderSwitch * module1.getName().compareTo(module2.getName());
                if (result == 0)
                    return sortOrderSwitch * module1.getId().compareTo(module2.getId());
                return result;
            case "description":
                return sortOrderSwitch * module1.getDescription().compareTo(module2.getDescription());
            default:
            case "id":
                return sortOrderSwitch * module1.getId().compareTo(module2.getId());
        }
    }

    private <T extends ModuleBase> Page<T> getAll_helper(Collection<T> elements, int page, int perPage, String sortField, String sortOrder, ModuleFilter filter) {
        Stream<T> x = elements.stream();
        if (sortOrder != null && sortField != null)
            x = x.sorted((module1, module2) -> compareModules(module1, module2, sortField, sortOrder));
        if (filter != null && (filter.getQ() != null || filter.getIds() != null))
            x = x.filter(filter::evaluate);
        if (page > 0 && perPage > 0)
            x = x.skip(page * perPage).limit(perPage);
        return new PageImpl<>(x.collect(Collectors.toList()));
    }

    public Page<MetricAlgorithmImplementation> allMetricAlgorithmImplementations(int page, int perPage, String sortField, String sortOrder, ModuleFilter filter) {
        logger.debug("allMetricAlgorithmImplementations(page={}, perPage={}, sortField={}, sortOrder={}, filter={})", page, perPage, sortField, sortOrder, filter);
        return getAll_helper(appContext.getMetricAlgorithmImplementations().values(), page, perPage, sortField, sortOrder, filter);
    }

    public Page<? extends ImportModuleBase> allImportModules(int page, int perPage, String sortField, String sortOrder, ImportModuleFilter filter) {
        logger.debug("allImportModules(page={}, perPage={}, sortField={}, sortOrder={}, filter={})", page, perPage, sortField, sortOrder, filter);
        // Implementation of a typed query. Depending on the provided importResource key in parameter filter, we supply objects from different lists.
        switch (filter.getImportResource()) {
            case "Dataset":
                return getAll_helper(appContext.getDatasetEntityImportModules().values(), page, perPage, sortField, sortOrder, filter);
            case "ASLocation":
                return getAll_helper(appContext.getASLocationEntityImportModules().values(), page, perPage, sortField, sortOrder, filter);
            case "ASOrganization":
                return getAll_helper(appContext.getASOrganizationEntityImportModules().values(), page, perPage, sortField, sortOrder, filter);
            default:
                throw buildException("Unknown import resource: " + filter.getImportResource());
        }
    }

    public Page<ExportModule> allExportModules(int page, int perPage, String sortField, String sortOrder, ModuleFilter filter) {
        logger.debug("allExportModules(page={}, perPage={}, sortField={}, sortOrder={}, filter={})", page, perPage, sortField, sortOrder, filter);
        return getAll_helper(appContext.getExportModules().values(), page, perPage, sortField, sortOrder, filter);
    }

    public Page<CudaDeviceInfo> allCudaDevices(int page, int perPage, String sortField, String sortOrder, ModuleFilter filter) {
        logger.debug("allCudaDevices(page={}, perPage={}, sortField={}, sortOrder={}, filter={})", page, perPage, sortField, sortOrder, filter);
        return getAll_helper(appContext.getCudaDevices().values(), page, perPage, sortField, sortOrder, filter);
    }

    public ListMetadata _allMetricAlgorithmImplementationsMeta(int page, int perPage, String sortField, String sortOrder, ModuleFilter filter) {
        logger.debug("_allMetricAlgorithmImplementationsMeta(page={}, perPage={}, sortField={}, sortOrder={}, filter={})", page, perPage, sortField, sortOrder, filter);
        return new ListMetadata(allMetricAlgorithmImplementations(page, perPage, sortField, sortOrder, filter).getTotalElements());
    }

    public ListMetadata _allImportModulesMeta(int page, int perPage, String sortField, String sortOrder, ImportModuleFilter filter) {
        return new ListMetadata(allImportModules(page, perPage, sortField, sortOrder, filter).getTotalElements());
    }


    public ListMetadata _allExportModulesMeta(int page, int perPage, String sortField, String sortOrder, ModuleFilter filter) {
        return new ListMetadata(allExportModules(page, perPage, sortField, sortOrder, filter).getTotalElements());
    }

    public ListMetadata _allCudaDevicesMeta(int page, int perPage, String sortField, String sortOrder, ModuleFilter filter) {
        return new ListMetadata(allCudaDevices(page, perPage, sortField, sortOrder, filter).getTotalElements());
    }

    public MetricAlgorithmImplementation getMetricAlgorithmImplementation(String id) {
        MetricAlgorithmImplementation m = null;
        try {
            m = appContext.getMetricAlgorithmImplementation(id);
        } catch (Exception ignored) {
        }
        if (m == null)
            throw buildException("Metric algorithm implementation not found");
        return m;
    }

    public ImportModuleBase ImportModule(String id) {
        ImportModuleBase m;
        m = appContext.getDatasetEntityImportModule(id);
        if (m == null)
            m = appContext.getASLocationEntityImportModule(id);
        if (m == null)
            throw buildException("Import module not found");
        return m;
    }

    public ExportModule ExportModule(String id) {
        ExportModule m = appContext.getExportModule(id);
        if (m == null)
            throw buildException("Export module not found");
        return m;
    }

    public CudaDeviceInfo CudaDevice(String id) {
        CudaDeviceInfo m = appContext.getCudaDeviceInfo(id);
        if (m == null)
            throw buildException("Export module not found");
        return m;
    }

}
