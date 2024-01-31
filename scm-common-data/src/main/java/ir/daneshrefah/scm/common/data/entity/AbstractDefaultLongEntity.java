package ir.daneshrefah.scm.common.data.entity;

import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-01-31
 */
@Getter
@Setter
@MappedSuperclass
public abstract class AbstractDefaultLongEntity extends AbstractDefaultEntity<Long> {

}
