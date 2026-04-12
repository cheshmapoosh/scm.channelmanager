package transformers

import groovy.json.JsonSlurper
def rawBody = exchange.in.body
def body = rawBody instanceof String ? new JsonSlurper().parseText(rawBody) : rawBody

def params = body?.result?.parameters ?: [:]

def output = [

                responseDateTime: body?.responseDateTime ?: "",
                plateNumber     : params?.plateNumber ?: "",
                amount          : params?.amount ?: 0,
                billID          : params?.billID ?: "",
                paymentID       : params?.paymentID ?: "",
                complaintCode   : params?.complaintCode ?: "",
                complaintStatus : params?.complaintStatus ?: ""

]

exchange.in.body = output
