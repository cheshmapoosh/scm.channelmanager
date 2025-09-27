package ir.daneshrefah.scm.common.dto.definition;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import ir.daneshrefah.scm.common.model.definition.DefinitionDetail;
import ir.daneshrefah.scm.common.model.definition.DefinitionRequestDeserializer;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonDeserialize(using = DefinitionRequestDeserializer.class)
public class DefinitionDetailRequest{
    private String id;
    private List<? extends DefinitionDetail> detail;
}