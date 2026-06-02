package ir.daneshrefah.scm.provider.nab.domain;

import com.fasterxml.jackson.databind.node.ObjectNode;

public record NabHeaderValues(
        String protocol,
        String command,
        String rqUid,
        ObjectNode data
) {
}
