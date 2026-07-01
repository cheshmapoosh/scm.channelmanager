package ir.daneshrefah.scm.core.config;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class SensitiveChannelFilterConfig {
    private boolean enabled = false;
    private List<String> sources = new ArrayList<>();
    private List<String> allowedValues = new ArrayList<>();
}
