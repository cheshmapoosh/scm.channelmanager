package transformers

import ir.daneshrefah.scm.common.exception.CardException

def bodyRaw = exchange.in.body
def body = bodyRaw.body
def errorCode = body.errorCode

def status = body.get("status")
println("rest card inq rs status : " + status)
if (errorCode != null) {
    throw new CardException(errorCode.toString(),body.errorDescription)
}
def statusCode = status as int
if (!(statusCode >= 200 && statusCode < 300)) {
    throw new RuntimeException("Expected 2xx status from HPS card inquiry endpoint, but got " + statusCode)
}

def bodyResponse = body.get("body")
def out = bodyResponse.get("outData")

if (out == null && bodyResponse.get("errorCode") != null) {
    throw new RuntimeException(bodyResponse.get("errorDescription"))
}

println("rest card password inq rs body : " + body)
if (out == null && errorCode != null) {
    throw new RuntimeException(bodyResponse.get("errorDescription"))
}

return [
        "password": null,
        "card": null
]