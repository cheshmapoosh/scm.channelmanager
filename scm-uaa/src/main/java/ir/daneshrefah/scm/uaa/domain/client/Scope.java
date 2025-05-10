package ir.daneshrefah.scm.uaa.domain.client;

import ir.daneshrefah.scm.common.AuditableModel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-02-04
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Scope extends AuditableModel<Long> {

    private String code;
    private String title;

}
