package transformers

import groovy.json.JsonOutput

def body = exchange.in.body

def request = [
        left         : body?.left ?: "",
        mid          : body?.mid ?: "",
        right        : body?.right ?: "",
        alphabet     : body?.alphabet ?: "",
        mobileNumber : body?.mobileNumber ?: "",
        nationalID   : body?.nationalID ?: ""
]

exchange.in.body = JsonOutput.toJson(request)