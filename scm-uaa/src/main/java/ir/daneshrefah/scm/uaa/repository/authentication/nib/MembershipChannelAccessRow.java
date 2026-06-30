package ir.daneshrefah.scm.uaa.repository.authentication.nib;

public record MembershipChannelAccessRow(
        Integer id,
        Integer sourceChannelId,
        Integer membershipId,
        Integer authenticationMethodId,
        Object maxWithdrawalPerDay,
        Object active,
        Object fromDate,
        Object toDate,
        Object maxPersonalWithdrawalPerDay,
        Object userReason,
        Object reason,
        Object deactivationReason,
        Object favorite
) {
}
