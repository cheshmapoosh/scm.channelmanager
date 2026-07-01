package ir.daneshrefah.scm.core.config;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public  class SensitiveDataDecryptConfig {
    private boolean enabled = true;
    private boolean failOnDecryptError = true;
    private SensitiveChannelFilterConfig channelFilter;
    private List<SensitiveFieldConfig> fields = new ArrayList<>();
}
