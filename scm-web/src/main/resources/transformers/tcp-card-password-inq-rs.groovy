import ir.daneshrefah.scm.common.exception.CardException
import ir.daneshrefah.scm.provider.shetab.iso.util.ISOField
import ir.daneshrefah.scm.provider.shetab.iso.util.MTI
import ir.daneshrefah.scm.provider.shetab.iso.util.ResponseCode
import org.slf4j.LoggerFactory

def log = LoggerFactory.getLogger("CardPasswordNotificationRsGroovyTransformer")

def body = exchange.in.body

log.info("card password notification rs body: {}", body)

if (!(body instanceof Map)) {
    throw new RuntimeException("tcp card password notification rs : body is not map")
}

def mti = body.get("mti")
log.info("tcp card password notification rs mti: {}", mti)

if (mti == null || !mti.toString().trim().equals(MTI.AUTHORIZATION_ADVICE_RESPONSE_COMMAND.getCode())) {
    throw new RuntimeException("tcp card password notification rs : invalid mti : " + mti)
}

def fields = body.get("fields")
log.info("tcp card password notification rs fields: {}", fields)

if (!(fields instanceof Map)) {
    throw new RuntimeException("tcp card password notification rs : fields is null or not map")
}

def actionCodeKey = ISOField.ACTION_CODE.getPosition().toString()
def dataRecordKey = ISOField.DATA_RECORD.getPosition().toString()

def actionCode = fields[actionCodeKey]
log.info("tcp card password notification rs actionCode: {}", actionCode)

if (actionCode == null) {
    throw new CardException("999", "tcp card password notification rs action code is null")
}

if (!actionCode.toString().equals(ResponseCode.APPROVED.getCode())) {
    throw new CardException(
            actionCode.toString(),
            actionCode.toString(),
            "tcp card password notification rs action code : " + actionCode.toString()
    )
}

def password = fields[dataRecordKey]
log.info("tcp card password notification rs password/dataRecord exists: {}", password != null)

return [
        "password": password == null ? null : password.toString(),
        "card"    : null
]