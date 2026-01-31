package transformers

def body = exchange.in.body
def billIdRaw = body['billId']
if (!billIdRaw) {
    throw new IllegalArgumentException("billId not found in JSON")
}
def billId = billIdRaw.toString()
billId = billId.length() > 18
        ? billId[0..17]
        : billId.padRight(18, ' ')

def paymentIdRaw = body['paymentId']
if (!paymentIdRaw) {
    throw new IllegalArgumentException("paymentId not found in JSON")
}
def paymentId = paymentIdRaw.toString()
paymentId = paymentId.length() > 18
        ? paymentId[0..17]
        : paymentId.padRight(18, ' ')

return billId + paymentId