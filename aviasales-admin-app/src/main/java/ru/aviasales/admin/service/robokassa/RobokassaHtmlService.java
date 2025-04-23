package ru.aviasales.admin.service.robokassa;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.aviasales.admin.configuration.props.RobokassaProperties;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Service
@RequiredArgsConstructor
public class RobokassaHtmlService {
    private final RobokassaProperties properties;
    private final RobokassaService robokassaService;

    public String generatePaymentHtml(String invId, String outSum, String description) throws Exception {
        RobokassaService.PaymentData paymentData = preparePayment(invId, outSum, description);
        return generatePaymentHtml(paymentData);
    }

    public String generatePaymentHtml(RobokassaService.PaymentData paymentData) {
        if(paymentData == null) return null;

        StringBuilder urlBuilder = new StringBuilder("/pay.html?");

        appendQueryParam(urlBuilder, "MerchantLogin", paymentData.merchantLogin(), false);
        appendQueryParam(urlBuilder, "OutSum", paymentData.outSum(), true);
        appendQueryParam(urlBuilder, "InvoiceID", paymentData.invId(), true);
        appendQueryParam(urlBuilder, "Description", paymentData.description(), true);
        appendQueryParam(urlBuilder, "SignatureValue", paymentData.signatureValue(), true);
        appendQueryParam(urlBuilder, "IsTest", String.valueOf(paymentData.isTest()), true);

        return urlBuilder.toString();
    }

    private void appendQueryParam(StringBuilder builder, String key, String value, boolean prependAmpersand) {
        if (prependAmpersand) {
            builder.append('&');
        }
        builder.append(urlEncode(key));
        builder.append('=');
        builder.append(urlEncode(value != null ? value : ""));
    }

    private String urlEncode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
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
