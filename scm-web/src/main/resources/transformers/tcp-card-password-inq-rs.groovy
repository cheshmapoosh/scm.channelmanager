import ir.daneshrefah.scm.common.exception.CardException
import ir.daneshrefah.scm.provider.shetab.iso.util.ISOField
import ir.daneshrefah.scm.provider.shetab.iso.util.MTI
import ir.daneshrefah.scm.provider.shetab.iso.util.ResponseCode

def body = exchange.in.body

if (!body instanceof Map) {
    return
}

def mti = body.get("mti")
println("tcp card password inq rs mti : " + mti)
if (!mti.toString().trim().equals(MTI.AUTHORIZATION_ADVICE_RESPONSE_COMMAND.getCode())) {
    throw new RuntimeException("tcp card inq rs : mti is null")
}

def fields = body.get("fields")
println("tcp card pasword inq rs fields : " + fields)
if (fields == null) {
    throw new RuntimeException("tcp card inq rs : fields is null")
}

println("tcp card password inq rs action code" + fields[ISOField.ACTION_CODE.getPosition().toString()])
if (fields[ISOField.ACTION_CODE.getPosition().toString()] == null || !fields[ISOField.ACTION_CODE.getPosition().toString()].toString().equals(ResponseCode.APPROVED.getCode())) {
    throw new CardException(fields[ISOField.ACTION_CODE.getPosition().toString()].toString(), "tcp card inq rs action code : " + fields[ISOField.ACTION_CODE.getPosition().toString()].toString())
}

//return [
//        "status" : body.actionCode
//]
return [
    "password": null,
    "card": null
]