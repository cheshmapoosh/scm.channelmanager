package ir.daneshrefah.scm.common.data.entity.terminal;

import ir.daneshrefah.scm.common.data.converter.TerminalStatusConverter;
import ir.daneshrefah.scm.common.data.entity.AbstractVersionAbleDefaultEntity;
import ir.daneshrefah.scm.common.model.terminal.TerminalStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "TBL_SCM_TERMINAL")
public class TerminalEntity extends AbstractVersionAbleDefaultEntity<String> {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "TERMINAL_ID")
    private String id;
    private String code;
    private String title;
    @Column(name = "LEGACY_TERMINAL_ID")
    private Long legacyTerminalId;
    @Convert(converter = TerminalStatusConverter.class)
    private TerminalStatus status;
    @Column(name = "SUPPORT_CHECK_AUTH")
    private Boolean supportCheckAuthentication;
    @Column(name = "SUPPORT_CHECK_SECOND_AUTH")
    private Boolean supportCheckSecondAuthentication;
    @Column(name = "SUPPORT_CHECK_SRV_ACCESS")
    private Boolean supportCheckServiceAccess;
    private Boolean supportCheckAssetAccess;
    @Column(name = "SUPPORT_CUSTOMER_INJ")
    private Boolean supportCustomerInjection;

}
