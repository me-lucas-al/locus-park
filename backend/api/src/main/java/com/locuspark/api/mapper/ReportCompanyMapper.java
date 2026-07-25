package com.locuspark.api.mapper;

import com.locuspark.api.dto.response.report.ReportCompanyResponse;
import com.locuspark.api.entity.Company;
import com.locuspark.api.types.Cnpj;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ReportCompanyMapper {

    ReportCompanyResponse toResponse(Company company);

    default String map(Cnpj cnpj) {
        return cnpj != null ? cnpj.getValue() : null;
    }
}
