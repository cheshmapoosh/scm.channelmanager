package ir.daneshrefah.scm.core.entity.constant;

import ir.daneshrefah.scm.common.data.entity.AbstractEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-04-06
 */
@Getter
@Setter
@Entity
@Table(name = "CONSTANT_TABLE")
public class ConstantEntity extends AbstractEntity<Long> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "CONSTANT_TABLE_ID")
    private Long id;
    private String key;
    private String value;

}
