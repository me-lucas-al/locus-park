package com.locuspark.api.service;

import com.locuspark.api.dto.request.PricingConfigurationRequest;
import com.locuspark.api.dto.request.TariffConfigurationRequest;
import com.locuspark.api.dto.response.PricingConfigurationResponse;
import com.locuspark.api.dto.response.TariffConfigurationResponse;
import com.locuspark.api.entity.Company;
import com.locuspark.api.entity.PricingConfiguration;
import com.locuspark.api.entity.TariffConfiguration;
import com.locuspark.api.exception.ResourceNotFoundException;
import com.locuspark.api.mapper.PricingConfigurationMapper;
import com.locuspark.api.mapper.TariffConfigurationMapper;
import com.locuspark.api.repository.CompanyRepository;
import com.locuspark.api.repository.PricingConfigurationRepository;
import com.locuspark.api.repository.TariffConfigurationRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
public class ConfigurationService {

    private final TariffConfigurationRepository tariffRepository;
    private final PricingConfigurationRepository pricingRepository;
    private final TariffConfigurationMapper tariffMapper;
    private final PricingConfigurationMapper pricingMapper;
    private final CompanyRepository companyRepository;

    public ConfigurationService(TariffConfigurationRepository tariffRepository,
                                PricingConfigurationRepository pricingRepository,
                                TariffConfigurationMapper tariffMapper,
                                PricingConfigurationMapper pricingMapper, CompanyRepository companyRepository) {
        this.tariffRepository = tariffRepository;
        this.pricingRepository = pricingRepository;
        this.tariffMapper = tariffMapper;
        this.pricingMapper = pricingMapper;
        this.companyRepository = companyRepository;
    }

    @Cacheable(value = "tariffs", key = "#companyId")
    @Transactional(readOnly = true)
    public TariffConfigurationResponse getTariffByCompany(UUID companyId) {
        return tariffRepository.findByCompanyId(companyId)
                .map(tariffMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Configuração tarifária não encontrada para esta empresa."));
    }

    @Cacheable(value = "pricing", key = "#companyId")
    @Transactional(readOnly = true)
    public PricingConfigurationResponse getPricingByCompany(UUID companyId) {
        return pricingRepository.findByCompanyId(companyId)
                .map(pricingMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Configuração de preços não encontrada para esta empresa."));
    }

    // Adicione estes métodos dentro da classe ConfigurationService

    @CacheEvict(value = "tariffs", key = "#companyId")
    @Transactional
    public TariffConfigurationResponse saveOrUpdateTariff(UUID companyId, TariffConfigurationRequest request) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Empresa não encontrada."));

        TariffConfiguration tariff = tariffRepository.findByCompanyId(companyId)
                .orElse(new TariffConfiguration());

        tariff.setCompany(company);
        tariff.setToleranceMinutes(request.toleranceMinutes() != null ? request.toleranceMinutes() : 0);
        tariff.setFirstHourValue(request.firstHourValue() != null ? request.firstHourValue() : BigDecimal.ZERO);
        tariff.setAdditionalFractionValue(request.additionalFractionValue() != null ? request.additionalFractionValue() : BigDecimal.ZERO);
        tariff.setOvernightFee(request.overnightFee() != null ? request.overnightFee() : BigDecimal.ZERO);
        tariff.setLostTicketFee(request.lostTicketFee() != null ? request.lostTicketFee() : BigDecimal.ZERO);

        return tariffMapper.toResponse(tariffRepository.save(tariff));
    }

    @CacheEvict(value = "pricing", key = "#companyId")
    @Transactional
    public PricingConfigurationResponse saveOrUpdatePricing(UUID companyId, PricingConfigurationRequest request) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Empresa não encontrada."));

        PricingConfiguration pricing = pricingRepository.findByCompanyId(companyId)
                .orElse(new PricingConfiguration());

        pricing.setCompany(company);
        pricing.setDailyTriggerHours(request.dailyTriggerHours());
        pricing.setDailyValue(request.dailyValue());
        pricing.setMonthlyBaseValue(request.monthlyBaseValue());

        return pricingMapper.toResponse(pricingRepository.save(pricing));
    }

    @CacheEvict(value = "tariffs", key = "#companyId")
    @Transactional
    public void deleteTariff(UUID companyId) {
        TariffConfiguration tariff = tariffRepository.findByCompanyId(companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Configuração tarifária não encontrada para esta empresa."));
        tariffRepository.delete(tariff);
    }

    @CacheEvict(value = "pricing", key = "#companyId")
    @Transactional
    public void deletePricing(UUID companyId) {
        PricingConfiguration pricing = pricingRepository.findByCompanyId(companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Configuração de preços não encontrada para esta empresa."));
        pricingRepository.delete(pricing);
    }
}