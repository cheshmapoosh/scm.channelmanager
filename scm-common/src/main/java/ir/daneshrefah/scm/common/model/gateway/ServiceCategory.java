package ir.daneshrefah.scm.common.model.gateway;

import ir.daneshrefah.scm.common.AbstractModel;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ServiceCategory extends AbstractModel<Short> {
    @Size(max = 100)
    private String name;
    @Size(max = 200)
    private String description;
}