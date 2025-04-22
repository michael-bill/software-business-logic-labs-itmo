package ru.aviasales.admin.service.robokassa;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriUtils;
import ru.aviasales.admin.configuration.props.RobokassaProperties;

@Service
@RequiredArgsConstructor
public class RobokassaService {

    private final RobokassaProperties properties;

    public PaymentData preparePayment(String invId, String outSum, String description) throws Exception {
        String signature = generatePaymentSignature(invId, outSum);
        String encodedDesc = encodeDescription(description);

        return new PaymentData(
                properties.getMerchantLogin(),
                outSum,
                invId,
                encodedDesc,
                signature,
                properties.getIsTest()
        );
    }

    public boolean validateResultSignature(String outSum, String invId, String receivedSignature) throws Exception {
        String expectedSignature = generateResultSignature(outSum, invId);
        return expectedSignature.equalsIgnoreCase(receivedSignature);
    }

    public String generatePaymentSignature(String invId, String outSum) throws Exception {
        String data = String.join(":",
                properties.getMerchantLogin(),
                outSum,
                invId,
                properties.getPassword1()
        );
        return md5(data).toUpperCase();
    }

    private String generateResultSignature(String outSum, String invId) throws Exception {
        String data = String.join(":",
                outSum,
                invId,
                properties.getPassword2()
        );
        return md5(data).toUpperCase();
    }

    public String encodeDescription(String description) {
        return UriUtils.encode(description, StandardCharsets.UTF_8);
    }

    private String md5(String input) throws Exception {
        MessageDigest md = MessageDigest.getInstance("MD5");
        byte[] digest = md.digest(input.getBytes());
        return bytesToHex(digest);
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    public static record PaymentData(
            String merchantLogin,
            String outSum,
            String invId,
            String description,
            String signatureValue,
            int isTest
    ) {}
}