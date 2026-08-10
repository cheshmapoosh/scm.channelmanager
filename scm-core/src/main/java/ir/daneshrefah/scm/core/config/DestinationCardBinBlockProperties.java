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
@ConfigurationProperties(prefix = "scm.plugins.destination-card-bin-block")
public class DestinationCardBinBlockProperties {

    private boolean enabled = true;
    private Set<String> blockedBankBins = Set.of();
    private Error error = new Error();

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * Keeps a normalized immutable lookup set. Empty and malformed entries are ignored so an
     * optional or partially configured plugin does not prevent application startup.
     */
    public void setBlockedBankBins(Collection<String> blockedBankBins) {
        if (blockedBankBins == null) {
            this.blockedBankBins = Set.of();
            return;
        }
        this.blockedBankBins = blockedBankBins.stream()
                .map(String::trim)
                .filter(bin -> bin.matches("[0-9]{6}"))
                .collect(Collectors.toUnmodifiableSet());
    }

    public void setError(Error error) {
        this.error = error == null ? new Error() : error;
    }

    @Getter
    @Setter
    public static class Error {
        private String code = "DESTINATION_CARD_BANK_NOT_ALLOWED";
    }

}