package com.locuspark.api.service;

import com.locuspark.api.dto.request.PartnershipRequest;
import com.locuspark.api.dto.response.PartnershipResponse;
import com.locuspark.api.entity.Company;
import com.locuspark.api.entity.Partnership;
import com.locuspark.api.exception.ResourceNotFoundException;
import com.locuspark.api.mapper.PartnershipMapper;
import com.locuspark.api.repository.CompanyRepository;
import com.locuspark.api.repository.PartnershipRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class PartnershipService {

    private final PartnershipRepository partnershipRepository;
    private final CompanyRepository companyRepository;
    private final PartnershipMapper partnershipMapper;

    public PartnershipService(PartnershipRepository partnershipRepository,
                              CompanyRepository companyRepository,
                              PartnershipMapper partnershipMapper) {
        this.partnershipRepository = partnershipRepository;
        this.companyRepository = companyRepository;
        this.partnershipMapper = partnershipMapper;
    }

    @Cacheable(value = "partnerships", key = "#companyId")
    @Transactional(readOnly = true)
    public List<PartnershipResponse> findAllByCompany(UUID companyId) {
        return partnershipRepository.findByCompanyId(companyId)
                .stream()
                .map(partnershipMapper::toResponse)
                .collect(Collectors.toList());
    }

    // 1. Método faltante: Busca por ID validando a Empresa do pátio
    @Transactional(readOnly = true)
    public PartnershipResponse findByIdAndCompany(UUID id, UUID companyId) {
        Partnership partnership = partnershipRepository.findById(id)
                .filter(p -> p.getCompany().getId().equals(companyId))
                .orElseThrow(() -> new ResourceNotFoundException("Convênio não encontrado para esta empresa."));
        return partnershipMapper.toResponse(partnership);
    }

    // 2. Método faltante: Criação de convênio associado à empresa logada
    @CacheEvict(value = "partnerships", key = "#companyId")
    @Transactional
    public PartnershipResponse createPartnership(UUID companyId, PartnershipRequest request) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Empresa não encontrada."));

        Partnership partnership = partnershipMapper.toEntity(request);
        partnership.setCompany(company);

        return partnershipMapper.toResponse(partnershipRepository.save(partnership));
    }

    // 3. Método faltante: Atualização de convênio existente com proteção multi-tenant
    @CacheEvict(value = "partnerships", key = "#companyId")
    @Transactional
    public PartnershipResponse updatePartnership(UUID id, UUID companyId, PartnershipRequest request) {
        Partnership partnership = partnershipRepository.findById(id)
                .filter(p -> p.getCompany().getId().equals(companyId))
                .orElseThrow(() -> new ResourceNotFoundException("Convênio não encontrado para esta empresa."));

        partnershipMapper.updateFromDto(request, partnership);
        return partnershipMapper.toResponse(partnershipRepository.save(partnership));
    }

    // 4. Método faltante: Exclusão física/lógica do convênio do pátio
    @CacheEvict(value = "partnerships", key = "#companyId")
    @Transactional
    public void deletePartnership(UUID id, UUID companyId) {
        Partnership partnership = partnershipRepository.findById(id)
                .filter(p -> p.getCompany().getId().equals(companyId))
                .orElseThrow(() -> new ResourceNotFoundException("Convênio não encontrado ou não pertence a esta empresa."));
        partnershipRepository.delete(partnership);
    }
}