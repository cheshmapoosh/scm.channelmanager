package ir.daneshrefah.scm.uaa.repository.authentication;

import ir.daneshrefah.scm.common.data.entity.AbstractEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-01-12
 */
@Getter
@Setter
@Entity
@Table(name = "ROLE")
public class RoleEntity extends AbstractEntity<Integer> {

    @Id
    @Column(name = "ROLE_ID")
    private Integer id;
    private String name;
    private String code;
    private String abbreviation;
    private Boolean systemRole;
}
