package ir.daneshrefah.scm.core.entity.condition;

import ir.daneshrefah.scm.core.converter.AuthenticationMethodConverter;
import ir.daneshrefah.scm.core.entity.AbstractEntity;
import ir.daneshrefah.scm.uaa.common.type.AuthenticationMethod;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@MappedSuperclass
@Setter
@Getter
public abstract class ConditionBaseEntity<T> extends AbstractEntity<T> {

    @Column(name = "AUTH_METHOD_ID")
    @Convert(converter = AuthenticationMethodConverter.class)
    private AuthenticationMethod authenticationMethod;

    @Column(name = "SEC_AUTH_METHOD_ID")
    @Convert(converter = AuthenticationMethodConverter.class)
    private AuthenticationMethod secondAuthenticationMethod;

    @ManyToOne
    @JoinColumn(name = "CONDITION_ID")
    private ConditionEntity conditionEntity;

    @Column(name = "STATUS")
    private Boolean status;
}
