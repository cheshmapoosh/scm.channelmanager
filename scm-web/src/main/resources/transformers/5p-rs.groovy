import groovy.json.JsonOutput
import ir.daneshrefah.scm.common.transformerUtil.converter.DateAndTimeConverter

def nabResponse = exchange.in.body
println("5p nab response : " + nabResponse)

def status = nabResponse.status
def actionCode = status.code
def success = status.success
if (!success) {
    println("nab status code 5p : " + actionCode.asText())
    throw new ir.daneshrefah.scm.common.exception.NabError(actionCode.asText(), "nab error!");
}

def bodyRawList = nabResponse.records

def responseList = []
for (def body in bodyRawList) {
    println("5p response body : " + body);
    def accountNo = body.accountNo
    def transDate = DateAndTimeConverter.convertPersianDateToMs(body.transDate)
    def transRefNo = body.refNo
    def transSeq = body.refSeq
    def creditDebit = body.debitCredit
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
    def sourceCardNo = body.sourceCardNo
    def destinationAccountNo = body.destinationAccountNo
    def otherSideIban = body.otherSideIban
    def referenceCode = body.reference

    def item = [
            "accountNo"        : accountNo,
            transDate          : transDate,
            "transRefNo"       : transRefNo,
            "transSeq"         : transSeq,
            "creditDebit"      : creditDebit,
            "transAmount"      : transAmount,
            "transDesc"        : transDesc,
            "latinDesc"        : latinDesc,
            "serial"           : serial,
            "refNo"            : refNo,
            "extCode"          : extCode,
            "actionTime"       : actionTime,
            "iban"             : iban,
            "nationalId"       : nationalId,
            "descManual"       : descManual,
            "feeAmount"        : feeAmount,
            "billId"           : billId,
            "paymentId"        : paymentId,
            "sourceCardNo"     : sourceCardNo,
            "destinationAccountNo": destinationAccountNo,
            "otherSideIban"    : otherSideIban,
            "referenceCode"    : referenceCode
    ]
    responseList << item
}
return responseList