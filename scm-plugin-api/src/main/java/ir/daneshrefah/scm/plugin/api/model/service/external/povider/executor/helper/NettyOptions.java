package ir.daneshrefah.scm.plugin.api.model.service.external.povider.executor.helper;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum NettyOptions {
    RE_USE_CHANNEL  ("reuseChannel"),
    SYNC            ("sync");
    private final String value;
}
