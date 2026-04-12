package transformers

def body = exchange.in.body
def billerCode = text[198..200]
def billerNameE = text[84..144]
def billerNameF = text[24..84]
def amount = text[180..198]
def billId = text[144..162]
def paymentId = text[162..180]

return [
        billerNameF: billerNameF,
        billerNameE: billerNameE,
        billId     : billId,
        paymentId  : paymentId,
        amount     : amount,
        billerCode : billerCode
]
