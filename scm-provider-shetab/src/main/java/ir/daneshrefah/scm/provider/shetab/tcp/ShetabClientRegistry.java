package ir.daneshrefah.scm.provider.shetab.tcp;

import ir.daneshrefah.scm.provider.shetab.config.ShetabResolvedConfig;
import org.jpos.iso.ISOMsg;

public interface ShetabClientRegistry {
    ISOMsg request(ShetabResolvedConfig config, ISOMsg request);
}
