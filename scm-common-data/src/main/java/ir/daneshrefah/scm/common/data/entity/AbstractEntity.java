package ir.daneshrefah.scm.common.data.entity;

import jakarta.persistence.MappedSuperclass;

import java.io.Serializable;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-01-10
 */
//@EntityListeners(AuditingEntityListener.class)  //TODO NOT WORKING NOW
@MappedSuperclass
public abstract class AbstractEntity<T> implements Serializable {

    public abstract T getId();

    public abstract void setId(T id);

}
