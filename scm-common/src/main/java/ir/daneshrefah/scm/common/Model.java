package ir.daneshrefah.scm.common;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;

@Getter
@Setter
public class Model<T>{
    private T id;
}
