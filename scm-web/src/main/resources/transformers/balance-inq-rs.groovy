package transformers

def body = exchange.in.body.toString()

def branchNo       = body[23..28].trim()
def accountNo      = body[29..46].trim()
def ledgerBalStr   = body[47..64].trim()
def ledgerBalance  = new BigInteger(ledgerBalStr)
def accountStatus  = body[65..66].trim()
def availBalStr    = body[67..84].trim()
def availBalance   = new BigInteger(availBalStr)
def accountDesc    = body[85..144].trim()
def bin            = body[161..171].trim()
def externalRefNo  = body[172..177].trim()
def iban           = body[178..203].trim()
def nationalCode   = body[204..215].trim()

return [
        branchNo      : branchNo,
        accountNo     : accountNo,
        ledgerBalance : ledgerBalance,
        accountStatus : accountStatus,
        availBalance  : availBalance,
        accountDesc   : accountDesc,
        bin           : bin,
        externalRefNo : externalRefNo,
        iban          : iban,
        nationalCode  : nationalCode
]