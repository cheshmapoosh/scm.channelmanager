package ir.daneshrefah.scm.core.config;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public  class SensitiveFieldConfig {
    private String name;
    private List sources = new ArrayList<>();
    private List targets = new ArrayList<>();
    private boolean required = false;
    private String validateRegex;
}
