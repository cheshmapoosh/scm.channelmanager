package ir.daneshrefah.scm.uaa.exception.activation.nib;

import ir.daneshrefah.scm.common.exception.ScmException;

public class NibActivationChannelMappingNotFoundException extends ScmException {
    public NibActivationChannelMappingNotFoundException(String mappingType) {
        super("nib_activation_channel_mapping_not_found", "Required NIB channel mapping was not found: " + mappingType);
    }
}
