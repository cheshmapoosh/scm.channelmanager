package transformers

import ir.daneshrefah.scm.common.model.person.GeneralPerson
import ir.daneshrefah.scm.common.model.person.GeneralRealPerson
import ir.daneshrefah.scm.uaa.common.model.user.User
import ir.daneshrefah.scm.uaa.common.utils.AuthenticationUtils

def body = exchange.in.body;
def header = exchange.in.headers

def f = { value, len -> value = value?.toString() ?: ''; value.length() > len ? value[0..<len] : value.padRight(len, ' ') };

def pageSize = body['pageSize'];
if (pageSize < 0 || pageSize > 400) {
    pageSize = 400;
}
def transType = body['transType']
if (transType == "WITHDRAW") {
    transType = "1";
} else if (transType == "DEPOSIT") {
    transType = "2";
} else {
    transType = "0";
}

def creditDebit = body['creditDebit'];
if (creditDebit == "DEBIT") {
    creditDebit = "1"
} else if (creditDebit == "CREDIT") {
    creditDebit = "2"
} else {
    creditDebit = "0"
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

def sourceCardNo = body['sourceCardNo'];
if (sourceCardNo == null || sourceCardNo.toString().isEmpty() || !(sourceCardNo.toString().length() == 16 || sourceCardNo.toString().length() == 19)) {
    sourceCardNo = "";
}
def destinationCardNo = body['destinationCardNo'];
if (destinationCardNo == null || destinationCardNo.toString().isEmpty() || !(destinationCardNo.toString().length() == 16 || destinationCardNo.toString().length() == 19)) {
    destinationCardNo = "";
}
def iban = body['otherSideIban'];
if (iban == null || iban.toString().isEmpty() || iban.toString().length() != 26) {
    iban = "";
}

def person = AuthenticationUtils.getLoggedInUser().getPerson()

String nationalId = ""
if (person instanceof GeneralPerson) {
    nationalId = ((GeneralRealPerson) person).getNationalCode()
    println("5p nationalId : "+ nationalId)
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
                "accountNo"               : body.accountNo,
                "pageSize"                : pageSize,
                "startDate"               : body.startDate,
                "endDate"                 : body.endDate,
                "transType"               : transType,
                "creditDebit"             : creditDebit,
                "bankIdentificationNumber": bankIdentificationNumber,
                "extCode"                 : extCode,
                "rowNo"                   : body.rowNo,
                "manualDesc"              : body.manualDesc,
                "sourceCardNo"            : sourceCardNo,
                "destinationCardNo"       : destinationCardNo,
                "iban"                    : iban,
                "billId"                  : body.billId,
                "paymentId"               : body.paymentId,
                "referenceCode"           : body.referenceCode,
                "nationalId"              : nationalId,
                "filter"                  : filter
        ],
        "request" : [
                "fields": [
                        ["name": "accountNo", "length": 18, "required": true],
                        ["name": "pageSize", "length": 3, "required": true],
                        ["name": "startDate", "length": 8, "required": false],
                        ["name": "endDate", "length": 8, "required": false],
                        ["name": "transType", "length": 1, "required": false],
                        ["name": "creditDebit", "length": 1, "required": false],
                        ["name": "bankIdentificationNumber", "length": 11, "required": false],
                        ["name": "extCode", "length": 6, "required": false],
                        ["name": "rowNo", "length": 4, "required": true],
                        ["name": "manualDesc", "length": 200, "required": false],
                        ["name": "sourceCardNo", "length": 20, "required": false],
                        ["name": "destinationAccountNo", "length": 18, "required": false],
                        ["name": "iban", "length": 26, "required": false],
                        ["name": "billId", "length": 18, "required": false],
                        ["name": "paymentId", "length": 18, "required": false],
                        ["name": "referenceCode", "length": 30, "required": false],
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
                        ["name": "accountNo", "length": 18],
                        ["name": "transDate", "length": 8],
                        ["name": "refNo", "length": 8],
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
                        ["name": "sourceCardNo", "length": 20],
                        ["name": "destinationAccountNo", "length": 20],
                        ["name": "otherSideIban", "length": 26],
                        ["name": "reference", "length": 30]
                ]
        ]
]

println("5p transformed json : " + nabRequest)

return nabRequest