package ir.daneshrefah.scm.provider.task.entity;

import ir.daneshrefah.scm.common.constant.otp.OtpReason;
import ir.daneshrefah.scm.provider.task.constant.*;
import ir.daneshrefah.scm.provider.task.converter.*;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

@Getter
@Setter
@Entity
@Table(name = "TBL_PRC_PROCESS_TASK_DEFINITIONS")
public class ProcessTaskDefinitionEntity implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "DEFINITION_ID")
    private Long id;

    @Convert(converter = ProcessNameConverter.class)
    @Column(name = "PROCESS_NAME", nullable = false)
    private ProcessNameEnum processName;

    @Column(name = "EXECUTION_METHOD", nullable = false)
    @Convert(converter = ExecutionMethodConvertor.class)
    private ExecutionMethodTypeEnum executionMethodType;

    @Column(name = "DEFINITION_TYPE", nullable = false)
    @Convert(converter = DefinitionTypeConvertor.class)
    private DefinitionTypeEnum definitionType;

    @Column(name = "USER_ACCESS_SECOND_AUTH", nullable = false)
    private boolean userAccessSecondAuth;

    @Column(name = "CONFIRM_USER_ACCSS_SECOND_AUTH", nullable = false)
    private int confirmUserAccessSecondAuth;

    @Column(name = "USER_ALLOWED_CANCEL", nullable = false)
    private int userAllowedCancel;

    @Column(name = "CONFIRM_USER_ALLOWED_CANCEL", nullable = false)
    private int confirmUserAllowedCancel;

    @Column(name = "PROCESS_CODE", nullable = false)
    @Convert(converter = ProcessCodeConverter.class)
    private ProcessCodeEnum processCode;

    @Column(name = "OTP_REASON")
    @Convert(converter = OTPReasonConverter.class)
    private OtpReason otpReason;

    @Column(name = "CREATE_BY", nullable = false)
    private String createBy;

    @Column(name = "UPDATE_BY")
    private String updateBy;

    @Column(name = "CREATE_AT", nullable = false)
    private Date createAt;
}
