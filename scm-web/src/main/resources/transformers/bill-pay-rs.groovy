package transformers

def body = exchange.in.body

def RQID = body[68..83]
def ledegrBalance = body[32..49]
def availBalance = body[50..67]
def iBan = body[107..132]
def nationalCode = body[133..147]
def status = body[0..4]
def paymentStatus = status == 0 ? "PROCESSED" : "FAILED"
def batchProcessed = body[106..106]
def followupCode = body[90..105]
return [
        "RQID" : RQID.toString()..toString().trim(),
        "ledegrBalance" : ledegrBalance.toString()..toString().trim(),
        "availBalance" : availBalance..toString().trim(),
        "iBan" : iBan..toString().trim(),
        "nationalCode" : nationalCode..toString().trim(),
        "status" : status..toString().trim(),
        "paymentStatus" : paymentStatus..toString().trim(),
        "batchProcessed" : batchProcessed..toString().trim(),
        "followupCode" : followupCode..toString().trim()
]