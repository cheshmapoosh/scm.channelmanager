package ir.daneshrefah.scm.core.entity.authority;

import ir.daneshrefah.scm.common.model.authentication.AuthenticationMethod;
import ir.daneshrefah.scm.common.model.authority.AuthorityType;
import ir.daneshrefah.scm.common.model.service.ServiceImplementationType;
import ir.daneshrefah.scm.core.converter.AuthenticationMethodConverter;
import ir.daneshrefah.scm.core.converter.AuthorityTypeConverter;
import ir.daneshrefah.scm.core.converter.ServiceImplementationTypeConverter;
import ir.daneshrefah.scm.core.entity.AbstractEntity;
import ir.daneshrefah.scm.core.entity.service.ServiceEntity;
import ir.daneshrefah.scm.core.entity.terminal.ChannelEntity;
import ir.daneshrefah.scm.core.entity.terminal.TerminalEntity;
import jakarta.persistence.*;

@Entity
@Table(name = "TBL_SCM_AUTHORITY")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "AUTHORITY_TYPE_CODE", discriminatorType = DiscriminatorType.INTEGER)
public abstract class AuthorityEntity extends AbstractEntity<String> {

    @Id
    @Column(name = "AUTHORITY_ID")
    private String id;
    @Column(name = "AUTHORITY_TYPE_CODE", insertable = false, updatable = false)
    @Convert(converter = AuthorityTypeConverter.class)
    private AuthorityType authorityType;
    @ManyToOne
    @JoinColumn(name = "SOURCE_TERMINAL_ID")
    private TerminalEntity sourceTerminal;
    @ManyToOne
    @JoinColumn(name = "SOURCE_CHANNEL_ID")
    private ChannelEntity sourceChannel;
    @ManyToOne
    @JoinColumn(name = "SOURCE_SERVICE_ID")
    private ServiceEntity sourceService;
    @Column(name = "SOURCE_AUTHENTICATION_METHOD_CODE", insertable = false, updatable = false)
    @Convert(converter = AuthenticationMethodConverter.class)
    private AuthenticationMethod sourceAuthenticationMethod;
    private String sourceCondition;
    @Column(name = "SOURCE_USER_ID")
    private String sourceUser;
    @Column(name = "SOURCE_MEMBERSHIP_ID")
    private String sourceMembership;

    @Override
    public String getId() {
        return id;
    }

    @Override
    public void setId(String id) {
        this.id = id;
    }

    public TerminalEntity getSourceTerminal() {
        return sourceTerminal;
    }

    public AuthorityType getAuthorityType() {
        return authorityType;
    }

    public void setAuthorityType(AuthorityType authorityType) {
        this.authorityType = authorityType;
    }

    public void setSourceTerminal(TerminalEntity sourceTerminal) {
        this.sourceTerminal = sourceTerminal;
    }

    public ChannelEntity getSourceChannel() {
        return sourceChannel;
    }

    public void setSourceChannel(ChannelEntity sourceChannel) {
        this.sourceChannel = sourceChannel;
    }

    public ServiceEntity getSourceService() {
        return sourceService;
    }

    public void setSourceService(ServiceEntity sourceService) {
        this.sourceService = sourceService;
    }

    public AuthenticationMethod getSourceAuthenticationMethod() {
        return sourceAuthenticationMethod;
    }

    public void setSourceAuthenticationMethod(AuthenticationMethod sourceAuthenticationMethod) {
        this.sourceAuthenticationMethod = sourceAuthenticationMethod;
    }

    public String getSourceCondition() {
        return sourceCondition;
    }

    public void setSourceCondition(String sourceCondition) {
        this.sourceCondition = sourceCondition;
    }

    public String getSourceUser() {
        return sourceUser;
    }

    public void setSourceUser(String sourceUser) {
        this.sourceUser = sourceUser;
    }

    public String getSourceMembership() {
        return sourceMembership;
    }

    public void setSourceMembership(String sourceMembership) {
        this.sourceMembership = sourceMembership;
    }

}
