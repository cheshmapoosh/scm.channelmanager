package ir.daneshrefah.scm.process.model.filter;

import lombok.Data;

@Data
public class Filter {
    private String name;
    private String value;
    private Operation operation;
}
