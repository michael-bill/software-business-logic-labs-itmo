package ru.aviasales.admin.service.core.ad;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import ru.aviasales.admin.jca.JcaRandomServiceClient;
import ru.aviasales.admin.service.robokassa.RobokassaHtmlService;
import ru.aviasales.common.dao.entity.Advertisement;
import ru.aviasales.admin.dao.repository.AdvertisementRepository;
import ru.aviasales.common.dto.response.AdvertisementResp;
import org.springframework.transaction.annotation.Transactional;
import ru.aviasales.admin.exception.EntityNotFoundException;
import ru.aviasales.admin.exception.IllegalOperationException;
import ru.aviasales.admin.service.robokassa.RobokassaService;


import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdvertisementService {

    private final AdvertisementRepository advertisementRepository;
    private final RobokassaService robokassaService;
    private final ModelMapper modelMapper;
    private final RobokassaHtmlService robokassaHtmlService;
    private final JcaRandomServiceClient jcaRandomServiceClient;

    @Value("${advertisement.payment.default-amount:100.00}")
    private String defaultPaymentAmount;


    @Transactional(readOnly = true)
    public AdvertisementResp getAdvertisementById(Long advertisementId) {
        return advertisementRepository.findById(advertisementId)
                .map(ad -> modelMapper.map(ad, AdvertisementResp.class))
                .orElseThrow(() -> new EntityNotFoundException("Рекламное объявление с id " + advertisementId + " не найдено"));
    }

    @Transactional(readOnly = true)
    public Page<AdvertisementResp> getAllAdvertisements(Pageable pageable) {
        return advertisementRepository.findAll(pageable)
                .map(ad -> modelMapper.map(ad, AdvertisementResp.class));
    }

    @Transactional(readOnly = true)
    public Page<AdvertisementResp> findUnpaidAdvertisements(Pageable pageable) {
        Specification<Advertisement> spec = (root, query, cb) -> cb.equal(root.get("payed"), false);
        return advertisementRepository.findAll(spec, pageable)
                .map(ad -> modelMapper.map(ad, AdvertisementResp.class));
    }

    @Transactional
    public String initiatePaymentForAdvertisement(Long advertisementId) {
        log.info("Initiating payment for advertisement ID: {}", advertisementId);
        Advertisement ad = advertisementRepository.findById(advertisementId)
                .orElseThrow(() -> new EntityNotFoundException("Рекламное объявление с id " + advertisementId + " не найдено"));

        if (ad.getPayed()) {
            throw new IllegalOperationException("Рекламное объявление с id " + advertisementId + " уже оплачено.");
        }

        if(ad.getPaymentPage() != null) {
            return ad.getPaymentPage();
        }

        String paymentAmount = defaultPaymentAmount;
        // крутотень
        String invoiceId = jcaRandomServiceClient.getNewInvoiceId();
        String description = "Оплата рекламы: " + ad.getTitle() + " (ID: " + ad.getId() + ")";

        try {
            RobokassaService.PaymentData paymentData = robokassaService.preparePayment(invoiceId, paymentAmount, description);
            String htmlContent = robokassaHtmlService.generatePaymentHtml(paymentData);

            ad.setInvoiceId(invoiceId);
            ad.setPaymentInitiatedAt(LocalDateTime.now());
            ad.setPaymentPage(htmlContent);
            advertisementRepository.save(ad);

            log.info("Payment data prepared for Ad ID {}. Invoice ID: {}", advertisementId, invoiceId);
            return htmlContent;

        } catch (Exception e) {
            log.error("Failed to prepare payment for advertisement ID {}: {}", advertisementId, e.getMessage(), e);
            throw new RuntimeException("Ошибка при подготовке данных для платежа Robokassa", e);
        }
    }

    @Transactional
    public boolean processPaymentCallback(String invId, String outSum, String signature) {
        log.info("Processing payment callback for Invoice ID: {}", invId);
        try {
            if (!robokassaService.validateResultSignature(outSum, invId, signature)) {
                log.warn("Invalid signature received for Invoice ID: {}", invId);
                return false;
            }

            Advertisement ad = advertisementRepository.findByInvoiceId(invId)
                    .orElseThrow(() -> {
                        log.error("Advertisement not found for Invoice ID: {}", invId);
                        return new EntityNotFoundException("Объявление для счета " + invId + " не найдено.");
                    });

            if (ad.getPayed()) {
                log.warn("Received duplicate payment confirmation for already paid Invoice ID: {}", invId);
                return true;
            }

            ad.setPayed(true);
            advertisementRepository.save(ad);
            log.info("Successfully marked advertisement ID {} (Invoice ID: {}) as paid.", ad.getId(), invId);
            return true;

        } catch (EntityNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error processing payment callback for Invoice ID {}: {}", invId, e.getMessage(), e);
            throw new RuntimeException("Internal error processing payment result", e);
        }
    }
}
