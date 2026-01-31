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
        "RQID" : RQID,
        "ledegrBalance" : ledegrBalance,
        "availBalance" : availBalance,
        "iBan" : iBan,
        "nationalCode" : nationalCode,
        "status" : status,
        "paymentStatus" : paymentStatus,
        "batchProcessed" : batchProcessed,
        "followupCode" : followupCode
]