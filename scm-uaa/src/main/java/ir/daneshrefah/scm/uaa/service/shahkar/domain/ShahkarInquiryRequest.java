package ir.daneshrefah.scm.uaa.service.shahkar.domain;

public record ShahkarInquiryRequest(
        String identificaionNo,
        String mobileNo,
        String identificationType
) {}