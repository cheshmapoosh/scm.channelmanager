package ir.daneshrefah.scm.uaa.common.model.person;

import ir.daneshrefah.scm.uaa.common.model.BaseModel;
import ir.daneshrefah.scm.uaa.common.model.location.City;
import ir.daneshrefah.scm.uaa.common.model.location.Region;
import ir.daneshrefah.scm.uaa.common.type.PersonType;
import lombok.Data;

import java.util.Objects;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-07-19
 */
@Data
public abstract class GeneralPerson extends BaseModel {

    private Boolean active;
    private Loyalty loyalty; // USER.CUSTOMER_TYPE_CODE refer to CUSTOMER_TYPE_CODE Table
    private Region region; // USER.REGION_CODE refer to REGION Table
    private City city; // USER.CITY_CODE refer to CITY Table
    private String username;
    private String email; // USER.email

    public abstract PersonType getType();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GeneralPerson person = (GeneralPerson) o;
        return Objects.equals(active, person.active) &&
                Objects.equals(username, person.username) &&
                Objects.equals(getId(), person.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(active, username,getId());
    }
}
