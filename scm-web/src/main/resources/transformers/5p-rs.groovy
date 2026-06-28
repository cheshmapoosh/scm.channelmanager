import groovy.json.JsonOutput

def nabResponse = exchange.in.body
println("5p nab response : "+ nabResponse)

def status = nabResponse.status
def actionCode = status.code
def success = status.success
if (!success){
    println("nab status code 5p : "+ actionCode.asText())
    throw new ir.daneshrefah.scm.common.exception.NabError(actionCode.asText(), "nab error!");
}

def bodyRawList = nabResponse.records

def responseList = []
for (def body in bodyRawList) {
    def accountNo = body.accountNo
    def transDate = body.transDate
    def transRefNo = body.transRefNo
    def transSeq = body.transSeq
    def creditDebit = body.creditDebit
    def transAmount = body.transAmount
    def transDesc = body.transDesc
    def latinDesc = body.latinDesc
    def serial = body.serial
    def refNo = body.refNo
    def bankIdentificationNumber = body.bankIdentificationNumber
    def extCode = body.extCode
    def actionTime = body.actionTime
    def iban = body.iban
    def nationalId = body.nationalId
    def descManual =body.descManual
    def feeAmount = body.feeAmount
    def billId = body.billId
    def paymentId = body.paymentId
    def sourceCardNo = body.sourceCardNo
    def destinationCardNo = body.destinationCardNo
    def otherSideIban = body.otherSideIban
    def referenceCode = body.referenceCode

    def item = [
            "accountNo":accountNo,
            transDate : transDate,
            "transRefNo":transRefNo,
            "transSeq":transSeq,
            "creditDebit":creditDebit,
            "transAmount":transAmount,
            "transDesc":transDesc,
            "latinDesc":latinDesc,
            "serial":serial,
            "refNo":refNo,
            "bankIdentificationNumber":bankIdentificationNumber,
            "extCode":extCode,
            "actionTime":actionTime,
            "iban":iban,
            "nationalId":nationalId,
            "descManual":descManual,
            "feeAmount":feeAmount,
            "billId":billId,
            "paymentId":paymentId,
            "sourceCardNo":sourceCardNo,
            "destinationCardNo":destinationCardNo,
            "otherSideIban":otherSideIban,
            "referenceCode":referenceCode.toString().trim()
    ]
    responseList << item
}
return JsonOutput.toJson(responseList)