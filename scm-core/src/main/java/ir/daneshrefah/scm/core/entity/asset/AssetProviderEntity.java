package ir.daneshrefah.scm.core.entity.asset;

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
 * @since 2024-06-22
 */
@Getter
@Setter
@Entity
@Table(name = "CORE_BANKING_SYSTEM")
public class AssetProviderEntity extends AbstractEntity<Integer> {

    @Id
    @Column(name = "CORE_BANKING_SYSTEM_ID")
    private Integer id;
    private String name;
    private String code;
    private boolean active;
    private String abbreviation;
    private String providerServiceId;

}
