import ir.daneshrefah.scm.common.transformerUtil.constant.TransactionType
import ir.daneshrefah.scm.common.transformerUtil.converter.DateAndTimeConverter
import org.slf4j.LoggerFactory

def nabResponse = exchange.in.body

def log = LoggerFactory.getLogger("5pRsGroovyTransformer")
log.info("5p nab response : {}", nabResponse)

def status = nabResponse.status
def actionCode = status.code
def success = status.success
log.info("5p nab status code : {}", actionCode.asText())
if (!success) {
    throw new ir.daneshrefah.scm.common.exception.NabError(actionCode.asText(), "nab error!");
}

def bodyRawList = nabResponse.records

def responseList = []
for (def body in bodyRawList) {
    log.info("5p response body : {}", body);
    def sourceAccountNumber = body.sourceAccountNumber
    def transDate = DateAndTimeConverter.convertPersianDateToMs(body.transDate.asText())
    def docNumber = body.docNumber
    def transSeq = body.refSeq
    def creditDebit = body.debitCredit

    TransactionType transType = TransactionType.getByCode(creditDebit.asText());
    def crdDbtType = ""
    if (transType.equals(TransactionType.WITHDRAWAL)) {
        crdDbtType = "-"
    } else if (transType.equals(TransactionType.DEPOSIT)) {
        crdDbtType = "+"
    }

    def transAmount = body.transAmount == null ? 0 : body.transAmount.asText().trim().toLong()
    def transDesc = body.transDesc
    def latinDesc = body.latinDesc
    def serial = body.serial
    def refNo = body.refNo
    def extCode = body.extCode
    def actionTime = body.actTime
    def iban = body.iban
    def nationalId = body.nationalId
    def descManual = body.descManual
    def feeAmount = body.feeAmount == null ? 0 : body.feeAmount.asText().trim().toLong()
    def billId = body.billId
    def paymentId = body.paymentId
    def sourceCardAccountNumber = body.sourceCardAccountNumber
    def destinationAccountNumber = body.destinationAccountNumber
    def otherSideIban = body.otherSideIban
    def referenceCode = body.referenceSequence

    def item = [
            "sourceAccountNumber"     : sourceAccountNumber,
            "transDate"               : transDate,
            "docNumber"               : docNumber,
            "transSeq"                : transSeq,
            "transType"               : crdDbtType,
            "transAmount"             : transAmount,
            "transDesc"               : transDesc,
            "latinDesc"               : latinDesc,
            "serial"                  : serial,
            "refNo"                   : refNo,
            "extCode"                 : extCode,
            "actionTime"              : actionTime,
            "iban"                    : iban,
            "nationalId"              : nationalId,
            "descManual"              : descManual,
            "feeAmount"               : feeAmount,
            "billId"                  : billId,
            "paymentId"               : paymentId,
            "sourceCardAccountNumber" : sourceCardAccountNumber,
            "destinationAccountNumber": destinationAccountNumber,
            "otherSideIban"           : otherSideIban,
            "referenceSequence"       : referenceCode
    ]
    responseList << item
}
log.info("5p transformed response list : {}", responseList)
return responseList