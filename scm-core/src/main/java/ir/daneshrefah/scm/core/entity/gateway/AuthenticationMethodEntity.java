package ir.daneshrefah.scm.core.entity.gateway;

import ir.daneshrefah.scm.common.data.entity.AbstractEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "AUTHENTICATION_METHOD", schema = "REF")
public class AuthenticationMethodEntity extends AbstractEntity<Short> {
    @Id
    @SequenceGenerator(name = "AUTHENTICATION_METHOD_id_gen", sequenceName = "SQCONSTANTS", allocationSize = 1)
    @Column(name = "AUTHENTICATION_METHOD_ID", nullable = false)
    private Short id;

    @Size(max = 100)
    @Convert(disableConversion = true)
    @Column(name = "NAME", length = 100)
    private String name;

    @Size(max = 3)
    @Convert(disableConversion = true)
    @Column(name = "CODE", length = 3)
    private String code;

    @Size(max = 3)
    @Convert(disableConversion = true)
    @Column(name = "ABBREVIATION", length = 3)
    private String abbreviation;

}