package ir.daneshrefah.scm.common.data.entity.person;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("3")
public class CorporatePersonEntity extends GeneralLegalPersonEntity {
}
