package ir.daneshrefah.scm.common.model.gateway;

import ir.daneshrefah.scm.common.Model;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * DTO for {@link ir.daneshrefah.scm.common.model.gateway.route.AuthenticationMethodEntity}
 */
@Getter
@Setter
public class AuthenticationMethod extends Model<Short> {
    @Size(max = 100)
    private String name;
    @Size(max = 3)
    private String code;
    @Size(max = 3)
    private String abbreviation;
}