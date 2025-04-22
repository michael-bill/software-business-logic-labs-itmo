package ru.aviasales.admin.service.robokassa;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring5.SpringTemplateEngine;
import ru.aviasales.admin.configuration.props.RobokassaProperties;

@Service
@RequiredArgsConstructor
public class RobokassaHtmlService {
    private final RobokassaProperties properties;
    private final SpringTemplateEngine templateEngine;
    private final RobokassaService robokassaService;

    public String generatePaymentHtml(String invId, String outSum, String description) throws Exception {
        RobokassaService.PaymentData paymentData = preparePayment(invId, outSum, description);

        Context context = new Context();
        context.setVariable("payment", paymentData);

        return templateEngine.process("robokassa/payment-init", context);
    }

    private RobokassaService.PaymentData preparePayment(String invId, String outSum, String description) throws Exception {
        String signature = robokassaService.generatePaymentSignature(invId, outSum);
        String encodedDesc = robokassaService.encodeDescription(description);

        return new RobokassaService.PaymentData(
                properties.getMerchantLogin(),
                outSum,
                invId,
                encodedDesc,
                signature,
                properties.getIsTest()
        );
    }
}
