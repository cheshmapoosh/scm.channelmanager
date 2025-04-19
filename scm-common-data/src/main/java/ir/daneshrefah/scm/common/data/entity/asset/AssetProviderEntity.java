package ir.daneshrefah.scm.common.data.entity.asset;

import ir.daneshrefah.scm.common.constant.AssetProviderCode;
import ir.daneshrefah.scm.common.data.converter.AssetProviderCodeConverter;
import ir.daneshrefah.scm.common.data.entity.AbstractEntity;
import jakarta.persistence.*;
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
    @Convert(converter = AssetProviderCodeConverter.class)
    private AssetProviderCode code;
    private boolean active;
    private String abbreviation;
//    @Column(name = "PROVIDER_SERVICE_ID") //TODO NEXT REALISES
//    private String serviceId;

}
