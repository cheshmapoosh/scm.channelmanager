package ir.daneshrefah.scm.common.model.service;

import ir.daneshrefah.scm.common.AbstractAuditableModel;
import ir.daneshrefah.scm.common.model.asset.AssetProvider;
import ir.daneshrefah.scm.common.model.terminal.Terminal;
import lombok.Getter;
import lombok.Setter;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-07-28
 */
@Getter
@Setter
public class ProviderTerminalCoding extends AbstractAuditableModel<Integer> {

    private AssetProvider provider;
    private Terminal terminal;
    private String clientCode;
    private String code;

}
