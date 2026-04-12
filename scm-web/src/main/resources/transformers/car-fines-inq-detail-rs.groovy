package transformers

import groovy.json.JsonSlurper

def rawBody = exchange.in.body

def body
if (rawBody instanceof String) {
    body = new JsonSlurper().parseText(rawBody)
} else {
    body = new JsonSlurper().parse(rawBody)
}

def parameters = body?.result?.parameters ?: [:]
def details = parameters?.details ?: []

def responseItem = [
        responseDateTime: body?.responseDateTime ?: "",
        plateNumber      : parameters?.plateNumber ?: "",
        totalAmount      : parameters?.totalAmount ?: 0,
        billID           : parameters?.billID ?: "",
        paymentID        : parameters?.paymentID ?: "",
        status           : body?.status ?: 0,
        message          : body?.message ?: "",
        details          : []
]

details.each { d ->
    responseItem.details << [
            amount                   : d?.amount ?: 0,
            billID                   : d?.billID ?: "",
            paymentID                : d?.paymentID ?: "",
            location                 : d?.location ?: "",
            type                     : (d?.type ?: "").replaceAll('[\\r\\n]', ''),
            typeCode                 : d?.typeCode ?: "",
            dateTime                 : d?.dateTime ?: "",
            delivery                 : d?.delivery ?: "",
            serialNumber             : d?.serialNumber ?: "",
            officerIdentificationCode: d?.officerIdentificationCode ?: "",
            uniqueID                 : d?.uniqueID ?: "",
            hasImage                 : d?.hasImage == true
    ]
}

exchange.in.body = responseItem