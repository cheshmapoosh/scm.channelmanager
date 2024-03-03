package ir.daneshrefah.scm.uaa.service.role;

import lombok.Data;

import java.io.Serializable;

@Data
public class RoleDTO implements Serializable {

    private String name;
    private String code;
    private String abbreviation;
    private Boolean systemRole;
}
