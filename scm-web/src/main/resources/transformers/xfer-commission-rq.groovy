package transformers

def body = exchange.in.body
def header = exchange.in.headers

def xferCommissionType = header['commissionType'].toString()
def XferCommissionUnit = "01"
// def personnelCode = header['person'].username.toString()
def personnelCode = ''
personnelCode = personnelCode == null ? ''.padRight(10, ' ')
        : (personnelCode.length() > 10
        ? personnelCode[0..9]
        : personnelCode.padRight(10, ' '))

// def branchCode = header['person'].branchCode.toString()
def branchCode = ''
branchCode = branchCode == null ? ''.padRight(6, ' ')
        : (branchCode.length() > 6
        ? branchCode[0..5]
        : branchCode.padRight(6, ' '))

def amount = ''.padRight(18, ' ')
def accountNoRaw = body['accountNo']
if (!accountNoRaw) {
    throw new IllegalArgumentException("accountNo not found in JSON")
}
def accountNo = accountNoRaw.toString()
accountNo = accountNo.length() > 18
        ? accountNo[0..17]
        : accountNo.padRight(18, ' ')


return accountNo + xferCommissionType + branchCode + personnelCode + XferCommissionUnit + amount