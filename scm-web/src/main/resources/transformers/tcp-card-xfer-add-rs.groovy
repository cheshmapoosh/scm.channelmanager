import java.time.format.DateTimeFormatter

def body = exchange.in.body
def fields = body.get("fields")

def balance = fields["54"]
def availableBalance = null
def ledgerBalance = null
def depositableAmount = null

def unpadZero = { srcStr, pattern ->
    {
        if (!srcStr.isEmpty() && !pattern.isEmpty()) {
            def destStr;
            for (destStr = srcStr; destStr.length() >= pattern.length() && destStr[0..pattern.length() - 1] == pattern; destStr = destStr[pattern.length()..-1]) {
            }

            return destStr;
        } else {
            return srcStr;
        }
    }
}

def createBalance = {
    if (balance.isEmpty()) {
        return null;
    }
    availableBalance = balance.length() >= 40 ? balance[8..19] : null;
    ledgerBalance = balance.length() >= 40 ? balance[28..39] : null;
    try {
        depositableAmount = unpadZero(availableBalance, "0")
    }
    catch (Exception ex) {
        depositableAmount = Double.valueOf(0)
    }
    try {
        ledgerBalance = unpadZero(ledgerBalance, "0")
    }
    catch (Exception ex) {
        ledgerBalance = Double.valueOf(0);
    }
}

createBalance()

def date = fields["12"].format(DateTimeFormatter.ofPattern("yyMMddHHmmss"))
def amount = !fields["4"].isEmpty() ? unpadZero(fields["4"], "0") : null

return [
        "fundTransfer"       : [
                "sourceAccountNumber"  : "308957404",
                "sourceCardNumber"     : fields["2"],
                "destinationCardNumber": "5047061044402697",
                "amount"               : amount,
                "customerCount"        : 0,
                "followupCode"         : "MB07097751214300",
                "personName"           : [
                        "firstName": "میررضا",
                        "lastName" : "موسوی "
                ],
                "date"                 : date
        ],
        "balance"            : [
                "ledgerBalance"    : ledgerBalance,
                "depositableAmount": depositableAmount
        ],
        "serverResponseCode" : fields["39"],
        "processCode"        : fields["3"],
        "destinationBankName": "بانك شهر"
]