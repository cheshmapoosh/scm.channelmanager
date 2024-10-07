package ir.daneshrefah.scm.core.entity.service.parameter;

import ir.daneshrefah.scm.common.model.service.parameter.ParameterDatasourceProperty;
import ir.daneshrefah.scm.core.converter.ParameterDatasourcePropertyConverter;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Embeddable
public class ParameterDatasourceEntity  {

    @Convert(converter = ParameterDatasourcePropertyConverter.class)
    @Column(name = "DATA_SRC_PROPERTY")
    private ParameterDatasourceProperty property;
    @Column(name = "DATA_SRC_VALUE")
    private String value;
    @Column(name = "DATA_SRC_LENGTH")
    private Integer length;
    @Column(name = "DATA_SRC_CONVERTOR_CODE")
    private String convertorCode;

}
