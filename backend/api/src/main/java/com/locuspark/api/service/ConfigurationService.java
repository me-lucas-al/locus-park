package com.locuspark.api.service;

import com.locuspark.api.dto.request.PricingConfigurationRequest;
import com.locuspark.api.dto.request.TariffConfigurationRequest;
import com.locuspark.api.dto.request.VehicleTypeMultiplierItemRequest;
import com.locuspark.api.dto.request.VehicleTypePricingBatchRequest;
import com.locuspark.api.dto.response.PricingConfigurationResponse;
import com.locuspark.api.dto.response.TariffConfigurationResponse;
import com.locuspark.api.dto.response.VehicleTypeMultiplierResponse;
import com.locuspark.api.entity.Company;
import com.locuspark.api.entity.PricingConfiguration;
import com.locuspark.api.entity.TariffConfiguration;
import com.locuspark.api.entity.VehicleTypePriceMultiplier;
import com.locuspark.api.enums.VehicleType;
import com.locuspark.api.exception.ResourceNotFoundException;
import com.locuspark.api.mapper.PricingConfigurationMapper;
import com.locuspark.api.mapper.TariffConfigurationMapper;
import com.locuspark.api.repository.CompanyRepository;
import com.locuspark.api.repository.PricingConfigurationRepository;
import com.locuspark.api.repository.TariffConfigurationRepository;
import com.locuspark.api.repository.VehicleTypePriceMultiplierRepository;
import com.locuspark.api.service.report.export.format.VehicleTypeLabel;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ConfigurationService {

    private final TariffConfigurationRepository tariffRepository;
    private final PricingConfigurationRepository pricingRepository;
    private final VehicleTypePriceMultiplierRepository vehicleTypePriceMultiplierRepository;
    private final TariffConfigurationMapper tariffMapper;
    private final PricingConfigurationMapper pricingMapper;
    private final CompanyRepository companyRepository;

    public ConfigurationService(TariffConfigurationRepository tariffRepository,
                                PricingConfigurationRepository pricingRepository,
                                VehicleTypePriceMultiplierRepository vehicleTypePriceMultiplierRepository,
                                TariffConfigurationMapper tariffMapper,
                                PricingConfigurationMapper pricingMapper,
                                CompanyRepository companyRepository) {
        this.tariffRepository = tariffRepository;
        this.pricingRepository = pricingRepository;
        this.vehicleTypePriceMultiplierRepository = vehicleTypePriceMultiplierRepository;
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

    @Cacheable(value = "vehiclePricing", key = "#companyId")
    @Transactional(readOnly = true)
    public List<VehicleTypeMultiplierResponse> getVehicleTypePricingByCompany(UUID companyId) {
        companyRepository.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Empresa não encontrada."));

        List<VehicleTypePriceMultiplier> existing = vehicleTypePriceMultiplierRepository.findAllByCompanyId(companyId);
        Map<VehicleType, VehicleTypePriceMultiplier> map = existing.stream()
                .collect(Collectors.toMap(VehicleTypePriceMultiplier::getVehicleType, m -> m));

        return Arrays.stream(VehicleType.values())
                .map(type -> {
                    VehicleTypePriceMultiplier multiplier = map.get(type);
                    BigDecimal value = multiplier != null ? multiplier.getMultiplier() : BigDecimal.ONE.setScale(2, RoundingMode.HALF_UP);
                    UUID id = multiplier != null ? multiplier.getId() : null;
                    return new VehicleTypeMultiplierResponse(id, type, value, VehicleTypeLabel.label(type));
                })
                .toList();
    }

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

    @CacheEvict(value = "vehiclePricing", key = "#companyId")
    @Transactional
    public List<VehicleTypeMultiplierResponse> saveOrUpdateVehicleTypePricing(UUID companyId, VehicleTypePricingBatchRequest request) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Empresa não encontrada."));

        for (VehicleTypeMultiplierItemRequest item : request.multipliers()) {
            VehicleTypePriceMultiplier multiplier = vehicleTypePriceMultiplierRepository
                    .findByCompanyIdAndVehicleType(companyId, item.vehicleType())
                    .orElseGet(() -> VehicleTypePriceMultiplier.builder()
                            .company(company)
                            .vehicleType(item.vehicleType())
                            .build());

            multiplier.setMultiplier(item.multiplier().setScale(2, RoundingMode.HALF_UP));
            vehicleTypePriceMultiplierRepository.save(multiplier);
        }

        return getVehicleTypePricingByCompany(companyId);
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

    @CacheEvict(value = "vehiclePricing", key = "#companyId")
    @Transactional
    public void deleteVehicleTypePricing(UUID companyId) {
        companyRepository.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Empresa não encontrada."));
        vehicleTypePriceMultiplierRepository.deleteAllByCompanyId(companyId);
    }
}