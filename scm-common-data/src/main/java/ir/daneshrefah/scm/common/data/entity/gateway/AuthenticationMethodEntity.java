package ir.daneshrefah.scm.common.data.entity.gateway;

import ir.daneshrefah.scm.common.data.entity.AbstractEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "AUTHENTICATION_METHOD", schema = "REF")
public class AuthenticationMethodEntity extends AbstractEntity<Integer> {
    @Id
    @SequenceGenerator(name = "AUTHENTICATION_METHOD_id_gen", sequenceName = "SQCONSTANTS", allocationSize = 1)
    @Column(name = "AUTHENTICATION_METHOD_ID", nullable = false)
    private Integer id;

    @Column(name = "NAME", length = 100)
    private String name;

    @Column(name = "CODE", length = 3)
    private String code;

    @Column(name = "ABBREVIATION", length = 3)
    private String abbreviation;

}
