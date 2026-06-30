package ir.daneshrefah.scm.uaa.repository.authentication.nib;

import java.math.BigDecimal;

public record MembershipChannelServiceAccessRow(
        Object archiveNo,
        Integer id,
        Integer membershipChannelAccessId,
        Object maxWithdrawalPerTransaction,
        BigDecimal channelServiceAccessId
) {
}
