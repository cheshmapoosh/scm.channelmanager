package transformers

import ir.daneshrefah.scm.common.model.person.GeneralPerson
import ir.daneshrefah.scm.common.model.person.GeneralRealPerson
import ir.daneshrefah.scm.common.transformerUtil.constant.TransactionType
import ir.daneshrefah.scm.common.transformerUtil.converter.DateAndTimeConverter
import ir.daneshrefah.scm.uaa.common.model.user.User
import ir.daneshrefah.scm.uaa.common.utils.AuthenticationUtils
import org.slf4j.LoggerFactory

//def log = LoggerFactory.getLogger("5pRqGroovyTransformer")

def body = exchange.in.body;
def header = exchange.in.headers

println("5p rq body {}"+ body)
//println("5p rq body {}", body)

def f = { value, len -> value = value?.toString() ?: ''; value.length() > len ? value[0..<len] : value.padRight(len, ' ') };

def pageSize = body['pageSize'];
if (pageSize < 0 || pageSize > 400) {
    pageSize = 400;
}

def creditDebit = body['transferType']
def crdDbt = "0"
switch (creditDebit) {
    case "WITHDRAWAL":
        crdDbt = TransactionType.WITHDRAWAL.getCode()
        break
    case "DEPOSIT":
        crdDbt = TransactionType.DEPOSIT.getCode()
        break
    default:
        crdDbt = TransactionType.WITHDRAWAL_DEPOSIT.getCode()
}

def bankIdentificationNumber;
if (header['acquirerInstitutionID'] != null) {
    bankIdentificationNumber = header['acquirerInstitutionID'];
} else {
    bankIdentificationNumber = "";
}

def extCode = "";
if (header['additionalPrivateData'] != null) {
    def additionalPrivateDataContent = header['additionalPrivateData'];
    def EXTERNAL_STAN_HEADER_TAG = "P13";
    def index = header['additionalPrivateData'].toString().indexOf(EXTERNAL_STAN_HEADER_TAG)
    if (!additionalPrivateDataContent.toString().isEmpty()) {
        if (index >= 0) {
            def fromIndex = index + EXTERNAL_STAN_HEADER_TAG.length();
            def toIndex = index + EXTERNAL_STAN_HEADER_TAG.length() + 3;
            def externalStanLen = additionalPrivateDataContent[fromIndex..toIndex]
            if (!externalStanLen.isEmpty()) {
                def len = externalStanLen.toInteger();
                def endIndex = index + EXTERNAL_STAN_HEADER_TAG.length() + 3 + len;
                extCode = additionalPrivateDataContent[toIndex..endIndex];
            }
        }
    }
}

def sourceCardNumber = body['sourceCardNumber'];
if (sourceCardNumber == null || sourceCardNumber.toString().isEmpty() || !(sourceCardNumber.toString().length() == 16 || sourceCardNumber.toString().length() == 19)) {
    sourceCardNumber = "";
}
def destinationAccountNumber = body['destinationAccountNumber'];
if (destinationAccountNumber == null || destinationAccountNumber.toString().isEmpty() || !(destinationAccountNumber.toString().length() == 16 || destinationAccountNumber.toString().length() == 19)) {
    destinationAccountNumber = "";
}
def iban = body['otherSideIban'];
if (iban == null || iban.toString().isEmpty() || iban.toString().length() != 26) {
    iban = "";
}

def person = AuthenticationUtils.getLoggedInUser().getPerson()

String nationalId = ""
if (person instanceof GeneralPerson) {
    nationalId = ((GeneralRealPerson) person).getNationalCode()
    println("5p nationalId : " + nationalId)
}


def filter = body['filter'];

//def request = f(body.accountNo, 18) +
//        f(pageSize, 3) +
//        f(body.startDate, 8) +
//        f(body.endDate, 8) +
//        f(body.transType, 1) +
//        f(body.creditDebit, 1) +
//        f(body.bankIdentificationNumber, 11) +
//        f(body.extCode, 6) +
//        f(body.rowNo, 4) +
//        f(body.manualDesc, 200) +
//        f(body.sourceCardNo, 20) +
//        f(body.destinationCardNo, 20) +
//        f(body.iban, 26) +
//        f(body.billId, 18) +
//        f(body.paymentId, 18) +
//        f(body.referenceCode, 30) +
//        f(body.nationalId, 10) +
//        f(body.filter, 552)

def nabRequest = [
        "command" : [
                "code"    : "5P",
                "protocol": "ATPS"
        ],
        "data"    : [
                "sourceAccountNumber"     : body.sourceAccountNumber,
                "pageSize"                : pageSize,
                "pageNumber"              : body.pageNumber,
                "startDate"               : DateAndTimeConverter.convertMsToPersianDate(body.startDate),
                "endDate"                 : DateAndTimeConverter.convertMsToPersianDate(body.endDate),
                "transType"               : "0",
                "transferType"            : crdDbt,
                "bankIdentificationNumber": bankIdentificationNumber,
                "extCode"                 : extCode,
                "manualDesc"              : body.manualDesc,
                "sourceCardNumber"        : sourceCardNumber,
                "destinationAccountNumber": destinationAccountNumber,
                "iban"                    : iban,
                "billId"                  : body.billId,
                "paymentId"               : body.paymentId,
                "referenceSequence"       : body.referenceSequence,
                "nationalId"              : nationalId,
                "filter"                  : filter
        ],
        "request" : [
                "fields": [
                        ["name": "sourceAccountNumber", "length": 18, "required": true],
                        ["name": "pageSize", "length": 3, "required": true],
                        ["name": "startDate", "length": 8, "required": false],
                        ["name": "endDate", "length": 8, "required": false],
                        ["name": "transType", "length": 1, "required": false],
                        ["name": "transferType", "length": 1, "required": false],
                        ["name": "bankIdentificationNumber", "length": 11, "required": false],
                        ["name": "extCode", "length": 6, "required": false],
                        ["name": "pageNumber", "length": 4, "required": true],
                        ["name": "manualDesc", "length": 200, "required": false],
                        ["name": "sourceCardNumber", "length": 20, "required": false],
                        ["name": "destinationAccountNumber", "length": 20, "required": false],
                        ["name": "iban", "length": 26, "required": false],
                        ["name": "billId", "length": 18, "required": false],
                        ["name": "paymentId", "length": 18, "required": false],
                        ["name": "referenceSequence", "length": 30, "required": false],
                        ["name": "nationalId", "length": 10, "required": true],
                        ["name": "filter", "length": 552, "required": false],
                ]
        ],
        "response": [
                "fields": [
                        ["name": "command", "length": 2],
                        ["name": "service", "length": 2],
                        ["name": "date", "length": 8],
                        ["name": "time", "length": 6],
                        ["name": "branchNo", "length": 6],
                        ["name": "sourceAccountNumber", "length": 18],
                        ["name": "transDate", "length": 8],
                        ["name": "docNumber", "length": 8],
                        ["name": "refSeq", "length": 10],
                        ["name": "debitCredit", "length": 1],
                        ["name": "transAmount", "length": 18],
                        ["name": "transDesc", "length": 60],
                        ["name": "latinDesc", "length": 50],
                        ["name": "serial", "length": 12],
                        ["name": "refNo", "length": 16],
                        ["name": "bin", "length": 11],
                        ["name": "extCode", "length": 6],
                        ["name": "actTime", "length": 6],
                        ["name": "iban", "length": 26],
                        ["name": "nationalId", "length": 15],
                        ["name": "descManual", "length": 200],
                        ["name": "feeAmount", "length": 18],
                        ["name": "billId", "length": 18],
                        ["name": "paymentId", "length": 18],
                        ["name": "sourceCardAccountNumber", "length": 20],
                        ["name": "destinationAccountNumber", "length": 20],
                        ["name": "otherSideIban", "length": 26],
                        ["name": "referenceSequence", "length": 30]
                ]
        ]
]

println("5p transformed json : {}"+ nabRequest)

return nabRequest