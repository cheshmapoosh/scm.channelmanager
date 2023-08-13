package ir.daneshrefah.scm.core.entity.authority;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-08-13
 */
@Entity
@DiscriminatorValue("1")
public class TerminalServiceAccessAuthorityEntity extends AuthorityEntity {

    private Boolean targetServiceAccessAllow;

    public Boolean getTargetServiceAccessAllow() {
        return targetServiceAccessAllow;
    }

    public void setTargetServiceAccessAllow(Boolean targetServiceAccessAllow) {
        this.targetServiceAccessAllow = targetServiceAccessAllow;
    }
}
