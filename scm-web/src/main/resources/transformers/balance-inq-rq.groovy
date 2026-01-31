package transformers

def body = exchange.in.body

def accountNoRaw = body['accountNo']
if (!accountNoRaw) {
    throw new IllegalArgumentException("accountNo not found in JSON")
}
def accountNo = accountNoRaw.toString()
accountNo = accountNo.length() > 18
        ? accountNo[0..17]
        : accountNo.padRight(18, ' ')

def rqId = exchange.exchangeId.split("-")[1];
rqId = rqId.length() > 16
        ? rqId[0..15]
        : rqId.padRight(16, ' ')

def interBank = "0"
def bankIdentificationNumber = ''.padRight(11, ' ')
def externalRefNo = ''.padRight(6, ' ')
def termianlId = ''.padRight(8, ' ')
def retrivalRefNo = ''.padRight(12, ' ')
def clientDateTime = ''.padRight(14, ' ')
def outlet = ''.padRight(15, ' ')
def mcc = ''.padRight(4, ' ')
def countryCode = ''.padRight(3, ' ')
def commissionRate = ''.padRight(18, ' ')
def customerId = ''.padRight(12, ' ')

return accountNo + interBank + commissionRate + bankIdentificationNumber + externalRefNo + termianlId + retrivalRefNo + customerId + clientDateTime + outlet + mcc + countryCode