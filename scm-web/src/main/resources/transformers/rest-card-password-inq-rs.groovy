package transformers

import ir.daneshrefah.scm.common.exception.CardException

def bodyRaw = exchange.in.body
def body = bodyRaw.body
def errorCode = body.errorCode

def status = body.get("succeed")
println("rest card inq rs status : " + status)
if (errorCode != null) {
    throw new CardException(errorCode.toString(),body.errorDescription)
}

def out = body.get("outData")
println("out clas  : " + out.getClass())

if (out == null && status == false) {
    throw new RuntimeException(body.get("errorDescription"))
}

println("rest card password inq rs body : " + body)


return [
        "password": null,
        "card": null
]