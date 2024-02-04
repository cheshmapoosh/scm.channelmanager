package ir.daneshrefah.scm.uaa.repository.authentication.client;

import ir.daneshrefah.scm.common.data.entity.AbstractDefaultEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-02-04
 */
@Getter
@Setter
@Entity
@Table(name = "TBL_SUA_SCOPE")
public class ScopeEntity extends AbstractDefaultEntity<Long> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "SCOPE_ID")
    private Long id;
    private String code;
    private String title;

}
