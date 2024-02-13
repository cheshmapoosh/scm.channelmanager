package ir.daneshrefah.scm.common.data.entity.person;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("6")
public class TaminPersonEntity extends GeneralLegalPersonEntity {
}
