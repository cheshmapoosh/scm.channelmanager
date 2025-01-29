package ir.daneshrefah.scm.uaa.service.otp.crypt.model;

import ir.daneshrefah.scm.uaa.service.otp.crypt.EncryptorImpl;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.io.Serializable;

@Configuration
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ConfigurationProperties(prefix = "scm.otp.channel")
public class OtpChannel implements Serializable {
    private String id = "1";
    private String channelCode;
    private String storeKey;
    private String jcaAlgorithm;
    private String provider = EncryptorImpl.DEFAULT_PROVIDER;
    private byte[] keyBytes;
    private String password;
    private String alias;
}
