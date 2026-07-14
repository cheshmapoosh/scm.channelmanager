package transformers

import ir.daneshrefah.scm.utils.date.DateUtils
import org.slf4j.LoggerFactory

def b = exchange.in.body;

def log = LoggerFactory.getLogger("5jRqGroovyTransformer")
log.info("5j rq body {}", b)


def f = { value, len -> value = value?.toString() ?: ''; value.length() > len ? value[0..<len] : value.padRight(len, ' ') };

def privilagesRaw = b['privileges'] ?: [];
def getAmount = { prvlg ->
    {
        def objFound = privilagesRaw.find { obj -> obj.privilage.toString().equals(prvlg) }
        objFound ? objFound.amount.toString().trim().toLong() : 0L
    }
}

def privilages = new StringBuilder();
def maxAmounts = new StringBuilder();

//maxInternalAmount
def internalAmount = getAmount('XFER_ADD')
privilages << (internalAmount == 0 ? '0' : '1');
maxAmounts << f(internalAmount, 18);

//maxPayaAmount
def payaAmount = getAmount('ACH_XFER_ADD')
privilages << (payaAmount == 0 ? '0' : '1');
maxAmounts << f(payaAmount, 18);

////maxSatnaAmount
def satnaAmount = getAmount('RTGS_XFER_ADD')
privilages << (satnaAmount == 0 ? '0' : '1');
maxAmounts << f(satnaAmount, 18);

//maxIpAmount
def ipAmount = getAmount('IP_XFER_ADD')
privilages << (ipAmount == 0 ? '0' : '1');
maxAmounts << f(ipAmount, 18);

privilages = f(privilages, 10);

def ch = b['permitServiceId'] ?: [];
def permistServiceId = new StringBuilder();
permistServiceId << (ch.contains('atm') ? '1' : '0')
        << (ch.contains('mb') ? '1' : '0')
        << (ch.contains('ib') ? '1' : '0');
permistServiceId = f(permistServiceId, 10);

def customers = b['customers'] ?: [];
def customersStr = new StringBuilder();
if (customers.size() > 9) {
    throw new RuntimeException("invalid customer list size");
}
customers.each { customer ->
    customersStr << f(customer, 12);
}

if (customers.size() < 9) {
    customersStr << f(' ', 12 * (9 - customers.size()));
}

//def request = f(b.accountNo, 18) +
//        f(b.nationalCode, 10) +
//        f(b.cardNo, 20) +
//        f(b.customerCount, 1) +
//        customersStr.toString() +
//        f(b.insDel, 1) +
//        privilages +
//        maxAmounts +
//        f(b.expireDate, 8) +
//        permistServiceId

def isDelete = b.insDel == "2";


def nabRequest = [
        "command" : [
                "code"    : "5J",
                "protocol": "ATPS"
        ],
        "data"    : [
                "accountNo"       : b.accountNo,
                "nationalCode"    : b.nationalCode,
                "cardNo"          : b.cardNo,
                "customerCount"   : b.customerCount,
                "customersStr"    : customersStr,
                "insDel"          : b.insDel,
                "privilages"      : privilages,
                "maxAmounts"      : maxAmounts,
                "expireDate"      : b.expireDate,
                "permistServiceId": permistServiceId
        ],
        "request" : [
                "fields": [
                        ["name": "accountNo", "length": 18, "required": true],
                        ["name": "nationalCode", "length": 10, "required": true],
                        ["name": "cardNo", "length": 20, "required": false],
                        ["name": "customerCount", "length": 1, "required": isDelete ? false : true],
                        ["name": "customersStr", "length": 108, "required": isDelete ? false : true],
                        ["name": "insDel", "length": 1, "required": true],
                        ["name": "privilages", "length": 10, "required": isDelete ? false : true],
                        ["name": "maxAmounts", "length": 72, "required": isDelete ? false : true],
                        ["name": "expireDate", "length": 8, "required": isDelete ? false : true],
                        ["name": "permistServiceId", "length": 10, "required": isDelete ? false : true]
                ]
        ],
        "response": [
                "fields": [
                        ["name": "actionCode", "length": 5]
                ]
        ]
]

log.info("5j transformed nab request : {}", nabRequest)
return nabRequest