package ir.daneshrefah.scm.plugin.pichack.provider.dto.common;

import java.io.Serializable;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-04-06
 */
public class ChequeSigner implements Serializable {

    private BenefactorPerson signer;
    private BenefactorPerson signGrantor;
    private LegalStampType legalStamp;

    public BenefactorPerson getSigner() {
        return signer;
    }

    public void setSigner(BenefactorPerson signer) {
        this.signer = signer;
    }

    public BenefactorPerson getSignGrantor() {
        return signGrantor;
    }

    public void setSignGrantor(BenefactorPerson signGrantor) {
        this.signGrantor = signGrantor;
    }

    public LegalStampType getLegalStamp() {
        return legalStamp;
    }

    public void setLegalStamp(LegalStampType legalStamp) {
        this.legalStamp = legalStamp;
    }

}
