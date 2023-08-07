package ir.daneshrefah.scm.core.entity.service;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-08-06
 */
@Entity
@DiscriminatorValue("5")
public class ParentServiceEntity extends ServiceEntity {
}
