package ir.daneshrefah.scm.core.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

@Getter
@Component
@ConfigurationProperties(prefix = "scm.plugins.destination-iban-bank-block")
public class DestinationIbanBankBlockProperties {

    private boolean enabled = true;
    private Set<String> blockedBankCodes = Set.of();
    private Error error = new Error();

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void setBlockedBankCodes(Collection<String> blockedBankCodes) {
        if (blockedBankCodes == null) {
            this.blockedBankCodes = Set.of();
            return;
        }
        this.blockedBankCodes = blockedBankCodes.stream()
                .map(String::trim)
                .filter(bankCode -> bankCode.matches("[0-9]{3}"))
                .collect(Collectors.toUnmodifiableSet());
    }


    public void setError(Error error) {
        this.error = error == null ? new Error() : error;
    }

    @Getter
    @Setter
    public static class Error {
        private String code = "DESTINATION_IBAN_BANK_NOT_ALLOWED";
    }

}