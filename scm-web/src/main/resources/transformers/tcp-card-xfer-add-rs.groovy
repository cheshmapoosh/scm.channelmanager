import ir.daneshrefah.scm.common.data.dto.bank.BankDto
import ir.daneshrefah.scm.common.exception.CardException
import ir.daneshrefah.scm.common.model.message.Message
import ir.daneshrefah.scm.provider.shetab.iso.util.ISOField
import ir.daneshrefah.scm.provider.shetab.iso.util.MTI
import ir.daneshrefah.scm.provider.shetab.iso.util.ResponseCode
import org.slf4j.LoggerFactory

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

def log = LoggerFactory.getLogger("tcp-card-xfer-add-rs")

def safeLog = { msg ->
    try {
        log.info(String.valueOf(msg))
    } catch (Exception ignored) {
        println(String.valueOf(msg))
    }
}

def isBlank = { value ->
    value == null || String.valueOf(value).trim().isEmpty()
}

def unpadZero = { value ->
    if (isBlank(value)) {
        return null
    }

    String text = String.valueOf(value).trim()
    String result = text.replaceFirst("^0+", "")
    return result.isEmpty() ? "0" : result
}

def body = exchange.in.body
def originalBody = exchange.getProperty(Message.ORIGINAL_BODY)
safeLog("card xfer add rs body : " + body)

if (!(body instanceof Map)) {
    throw new RuntimeException("card xfer add rs : body is not map")
}

def mti = body.get("mti")
safeLog("card xfer add rs mti : " + mti)

if (mti == null || !mti.toString().trim().equals(MTI.TRANSFER_RESPONSE_COMMAND.getCode())) {
    throw new RuntimeException("card xfer add rs : invalid mti : " + mti)
}

def fields = body.get("fields")
safeLog("card xfer add rs fields : " + fields)

if (!(fields instanceof Map)) {
    throw new RuntimeException("card xfer add rs : fields is null or not map")
}

def getField = { isoField ->
    def key = String.valueOf(isoField.getPosition())

    if (fields.containsKey(key)) {
        return fields.get(key)
    }

    return null
}

def actionCode = getField(ISOField.ACTION_CODE)
safeLog("card xfer add rs actionCode : " + actionCode)

if (actionCode == null) {
    throw new CardException("999", "card xfer add rs action code is null")
}

if (!actionCode.toString().equals(ResponseCode.APPROVED.getCode())) {
    throw new CardException(
            actionCode.toString(),
            actionCode.toString(),
            "card xfer add rs action code : " + actionCode.toString()
    )
}

def balanceValue = getField(ISOField.ADDITIONAL_AMOUNTS)
def availableBalance = null
def ledgerBalance = null
def depositableAmount = null

if (!isBlank(balanceValue)) {
    String balanceText = String.valueOf(balanceValue)

    def availableRaw = balanceText.length() >= 40 ? balanceText.substring(8, 20) : null
    def ledgerRaw = balanceText.length() >= 40 ? balanceText.substring(28, 40) : null

    try {
        depositableAmount = unpadZero(availableRaw)
    } catch (Exception ignored) {
        depositableAmount = "0"
    }

    try {
        ledgerBalance = unpadZero(ledgerRaw)
    } catch (Exception ignored) {
        ledgerBalance = "0"
    }
}

def localTransactionDateTime = getField(ISOField.LOCAL_TRANSACTION_DATE_TIME)

def date = null
try {
    if (!isBlank(localTransactionDateTime)) {
        date = LocalDateTime
                .parse(String.valueOf(localTransactionDateTime), DateTimeFormatter.ofPattern("yyMMddHHmmss"))
                .atZone(java.time.ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli()
    }
} catch (Exception e) {
    safeLog("card xfer add rs date parse failed : " + localTransactionDateTime)
    date = null
}

def amountValue = getField(ISOField.TRANSACTION_AMOUNT)
def amount = null

try {
    amount = !isBlank(amountValue) ? unpadZero(amountValue) : null
} catch (Exception ignored) {
    amount = null
}

def pan = getField(ISOField.PAN)
def processingCode = getField(ISOField.PROCESSING_CODE)
def rrn = getField(ISOField.RETRIEVAL_REFERENCE_NO)
def authCode = getField(ISOField.AUTHORIZATION_ID_RESPONSE)
def accountNo1 = getField(ISOField.ACCOUNT_NO_1)
def cleanValue = { value ->
    if (value == null) {
        return null
    }

    String text = value.toString().trim()

    if (text.length() >= 2 && text.startsWith('"') && text.endsWith('"')) {
        text = text.substring(1, text.length() - 1).trim()
    }

    return text.isEmpty() ? null : text
}


String cardNo = cleanValue(
        originalBody?.fundTransfer?.destinationCardNumber
)

String firstName = cleanValue(
        originalBody?.fundTransfer?.personName?.firstName
)

String lastName = cleanValue(
        originalBody?.fundTransfer?.personName?.lastName
)

def detection = exchange.context.registry.lookupByName("bankListLoader")
def bankPrefix = cardNo?.length() >= 6
        ? cardNo.substring(0, 6)
        : null

BankDto bank = bankPrefix == null || detection == null
        ? null
        : detection.getBank(bankPrefix)
def result = [
        "fundTransfer"       : [
                "sourceAccountNumber"  : accountNo1 == null ? null : accountNo1.toString(),
                "sourceCardNumber"     : pan == null ? null : pan.toString(),
                "destinationCardNumber": cardNo,
                "amount"               : amount,
                "customerCount"        : 0,
                "followupCode"         : authCode == null ? (rrn == null ? null : rrn.toString()) : authCode.toString(),
                "personName"           : [
                        "firstName": firstName ,
                        "lastName" : lastName
                ],
                "date"                 : date
        ],
        "balance"            : [
                "ledgerBalance"    : ledgerBalance,
                "depositableAmount": depositableAmount
        ],
        "serverResponseCode" : actionCode == null ? null : actionCode.toString(),
        "processCode"        : processingCode == null ? null : processingCode.toString(),
        "destinationBankName": bank?.name?.toString()
]

safeLog("card xfer add rs final result : " + result)

return result