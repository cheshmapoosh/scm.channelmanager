package ir.daneshrefah.scm.common.error.management;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class ResolverProxyException extends RuntimeException{
    private final Exception targetException;

}
