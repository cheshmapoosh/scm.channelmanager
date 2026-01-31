package transformers

import groovy.json.JsonOutput
import ir.daneshrefah.scm.common.model.message.Authentication

def body = exchange.in.body
//def headers = exchange.in.headers

def principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

String mobile = principal.getMobileNumber();
String nationalId = principal.getNationalId();


//def mobile = body?.mobileNumber ?: headers?.get("X-Mobile-Number") ?: ""
//def nationalId = body?.nationalID ?: headers?.get("X-National-Id") ?: ""

def request = [
        left             :  "12",
        mid              : body?.mid ?: "",
        right            : body?.right ?: "",
        alphabet         : body?.alphabet ?: "",
        mobileNumber     : mobile,
        nationalID       : nationalId,
        walletIdentifier : mobile
]

exchange.in.body = JsonOutput.toJson(request)
