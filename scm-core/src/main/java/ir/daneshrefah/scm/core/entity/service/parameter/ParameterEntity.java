package ir.daneshrefah.scm.core.entity.service.parameter;

import ir.daneshrefah.scm.common.data.entity.AbstractDefaultEntity;
import ir.daneshrefah.scm.common.data.entity.AbstractVersionAbleDefaultEntity;
import ir.daneshrefah.scm.common.model.service.parameter.ParameterActionType;
import ir.daneshrefah.scm.common.model.service.parameter.ParameterDatasource;
import ir.daneshrefah.scm.common.model.service.parameter.ParameterType;
import ir.daneshrefah.scm.core.converter.ParameterActionTypeConverter;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-07-14
 */
@Getter
@Setter
@Entity
@Table(name = "TBL_SCM_PARAMETERS")
public class ParameterEntity extends AbstractVersionAbleDefaultEntity<Long> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "PARAMETER_ID")
    private Long id;
    private String name;
    @Embedded
    private ParameterDatasourceEntity datasource;
    @Enumerated(EnumType.STRING)
    private ParameterType type;
    private boolean internal;
    private String tag;
    private boolean required;
    private Integer order;
    @ManyToOne
    @JoinColumn(name = "PARENT_ID")
    private ParameterEntity parent;
    @Convert(converter = ParameterActionTypeConverter.class)
    private ParameterActionType actionType;
    private String defaultValue;

}
