package ir.daneshrefah.scm.common.model.service;

import ir.daneshrefah.scm.common.BaseModel;
import ir.daneshrefah.scm.common.model.asset.AssetProvider;
import ir.daneshrefah.scm.common.model.terminal.Terminal;
import lombok.Data;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-07-28
 */
@Data
public class ProviderTerminalCoding extends BaseModel<Integer> {

    private AssetProvider provider;
    private Terminal terminal;
    private String clientCode;
    private String code;

}
