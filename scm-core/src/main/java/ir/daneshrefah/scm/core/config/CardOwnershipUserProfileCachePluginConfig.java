package ir.daneshrefah.scm.core.config;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class CardOwnershipUserProfileCachePluginConfig {
    private boolean enabled = true;
    private boolean failIfCardNotFound = true;
    private boolean failIfCardNotOwnedByUser = true;
    private boolean cacheEnabled = true;
    private String cacheName = "USER_PROFILE_CARD_OWNERSHIP_CACHE";
    private List<String> cardNumberSources = new ArrayList<>(List.of(
            "body:card.sourceCardNumber",
            "body:sourceCardNumber",
            "body:cardNumber",
            "header:sourceCardNumber"
    ));
    private String cardNumberHeaderTarget = "sourceCardNumber";
    private boolean putUserProfileInHeader = true;
    private String userProfileHeaderName = "userProfile";
    private boolean putOwnershipResultInHeader = true;
    private String ownershipHeaderName = "cardOwnershipValidated";
}
