package ir.daneshrefah.scm.common.data.entity.person;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("5")
public class BankPersonEntity extends GeneralLegalPersonEntity {
}
