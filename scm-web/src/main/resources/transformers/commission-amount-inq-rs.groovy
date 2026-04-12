package transformers

def body = exchange.in.body.toString()

def feeAmountStr = body[39..56].trim()

def feeAmount = new BigInteger(feeAmountStr)

return [
        "feeAmount": feeAmount
]
