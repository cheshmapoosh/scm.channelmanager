package ir.daneshrefah.scm.common;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public abstract class AbstractModel<T>{
    private T id;
}
