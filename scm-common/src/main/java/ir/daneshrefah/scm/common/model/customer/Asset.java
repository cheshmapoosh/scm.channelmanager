package ir.daneshrefah.scm.common.model.customer;

import ir.daneshrefah.scm.common.BaseModel;
import lombok.Getter;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-01-13
 */
@Getter
public abstract class Asset<T> extends BaseModel<Long> {

    public abstract T getValue();

    public abstract AssetType getType();

}
