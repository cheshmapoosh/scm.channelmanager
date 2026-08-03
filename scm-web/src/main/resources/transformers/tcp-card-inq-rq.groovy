import groovy.json.JsonOutput
import ir.daneshrefah.scm.provider.shetab.iso.util.CardConstant
import ir.daneshrefah.scm.provider.shetab.iso.util.ISOField
import ir.daneshrefah.scm.provider.shetab.iso.util.MTI
import ir.daneshrefah.scm.provider.shetab.iso.util.ProcessCode
import ir.daneshrefah.scm.utils.string.StringUtils

import javax.swing.GroupLayout
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import org.slf4j.LoggerFactory


def log = LoggerFactory.getLogger("tcp-card-inq-rq")
def body = exchange.in.body
def fundTransfer = body.fundTransfer
def trk2EquivData = body.trk2EquivData

def pin = trk2EquivData == null ? null : trk2EquivData.pin
//log.trace("tcp rq:" + body)

//def fundTransfer = body.fundTransfer
//log.trace("fundTransfer:" + fundTransfer)
//def amount = fundTransfer.amount
//def date = fundTransfer.date
def srcCard = fundTransfer.sourceCardNumber
def destCard = fundTransfer.destinationCardNumber
def padZeroLeft = { str, length ->
    {
        if (str.isEmpty()) {
            return "";
        }
        while (str.length() < length) {
            str = "0" + str;
        }
        return str;
    }
}
def field48 = "DST" + padZeroLeft(destCard.length() + "", 3) + destCard
log.trace("field48: {}", field48)

//def srcAcc = fundTransfer.sourceAccountNumber

//def trk2EquivData = body.trk2EquivData
def cvv2 = trk2EquivData.cvv2
def cardExpirationYearMonth = trk2EquivData.cardExpirationYearMonth
//def pin = trk2EquivData.pin

def transmissionDateTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("MMddHHmmss"))
def localTransactionDateTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyMMddHHmmss"))
def stan = sprintf("%06d", System.currentTimeMillis() % 1_000_000)
def rrn = sprintf("%012d", System.currentTimeMillis() % 1_000_000_000_000L);

def req = [:]
def field = [:]
def security = [:]

req.put("mti", MTI.AUTHORIZATION_ADVICE_REQUEST_COMMAND.getCode());
log.trace("mti card inq rq : {}", MTI.AUTHORIZATION_ADVICE_REQUEST_COMMAND.getCode())

field.put(ISOField.PAN.getPosition(), srcCard);
field.put(ISOField.PROCESSING_CODE.getPosition(), ProcessCode.AUTHORIZATION_ADVICE.getCode());
field.put(ISOField.TRANSACTION_AMOUNT.getPosition(), StringUtils.leftPadZero("0", 12));
field.put(ISOField.TRANSACTION_FEE_AMOUNT.getPosition(), StringUtils.leftPadZero("0", 12));
field.put(ISOField.TRANSMISSON_DATE_TIME.getPosition(), transmissionDateTime);
field.put(ISOField.SYSTEM_TRACE_AUDIT_NUMBER.getPosition(), stan);
field.put(ISOField.LOCAL_TRANSACTION_DATE_TIME.getPosition(), localTransactionDateTime);
field.put(ISOField.POINT_OF_SERVICE_DATA_CODE.getPosition(), CardConstant.DEFAULT_IB_POINT_OF_SERVICE_DATA);
field.put(ISOField.FUNCTION_CODE.getPosition(), CardConstant.XFER_REV_FUNCTION_CODE);
field.put(ISOField.CARD_ACCEPTOR_BUSINESS_CODE.getPosition(), CardConstant.CARD_ACCEPTOR_BUSINESS_CODE);
field.put(ISOField.ACQUIRER_INSTITUTION_ID.getPosition(), CardConstant.DEFAULT_ACQUIRER_INSTITUTION_ID);
field.put(ISOField.FORWARDING_INSTITUTION_ID.getPosition(), destCard[0..5]);
field.put(ISOField.RETRIEVAL_REFERENCE_NO.getPosition(), rrn);
field.put(ISOField.CARD_ACCEPT_TERMINAL_ID.getPosition(), CardConstant.DEFAULT_CARD_ACCEPT_TERMINAL_ID);
field.put(ISOField.CARD_ACCEPT_ID_CODE.getPosition(), CardConstant.DEFAULT_CARD_ACCEPT_ID_CODE);
field.put(ISOField.CARD_ACCEPT_NAME_LOCATION.getPosition(), CardConstant.DEFAULT_CARD_ACCEPT_NAME_LOCATION);
field.put(ISOField.ADDITIONAL_PRIVATE_DATA.getPosition(), field48);
field.put(ISOField.TRANSACTION_CURRENCY_CODE.getPosition(), CardConstant.DEFAULT_CURRENCY_CODE);
//field.put(ISOField.PIN_DATA.getPosition(), pin != null ? CardSystemSecurityUtil.encryptPin(pin, srcCard) : null)
req.put("fields", field)

security.put("expiryDate", cardExpirationYearMonth);
security.put("cvv2", cvv2);
security.put("pin", "9729");
security.put("expiryRequired", false);
security.put("cvv2Required", true);
security.put("pinRequired", true);
security.put("macRequired", false);

req.put("security", security)

log.trace("tcp card inq rq: {}", req)
log.trace("tcp card inq rq json: {}", JsonOutput.toJson(req))

return JsonOutput.toJson(req)