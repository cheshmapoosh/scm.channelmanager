package ir.daneshrefah.scm.process.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
public class IssuerUser implements Serializable {
    private Long id;
    private String username;
    private String nationality;
    private String nationalCode;
    private String firstName;
    private String lastName;
    private String personType;
}
