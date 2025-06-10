package ir.daneshrefah.scm.common;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public abstract class AbstractModel<T> implements Serializable {
    private T id;
}
