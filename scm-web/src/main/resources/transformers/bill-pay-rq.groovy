package transformers

def body = exchange.in.body

def accountNoRaw=body['accountNo']
if(!accountNoRaw){throw new IllegalArgumentException("accountNo not found")}
def accountNo = accountNoRaw.toString()
accountNo = accountNo.length()>18?accountNo[0..17]:accountNo.padRight(18, ' ')
def amountRaw = body['amount']
if(!amountRaw){throw new IllegalArgumentException("amount not found")}
def amount = amountRaw.toString()
amount = amount.length()>18?amount[0..17]:amount.padRight(18, ' ')
def billIdRaw = body['billId']
if(!billIdRaw){throw new IllegalArgumentException("billId not found")}
def billId=billIdRaw.toString()
billId=billId.length()>18?billId[0..17]:billId.padRight(18, ' ')
def paymentIdRaw=body['paymentId']
if(!paymentIdRaw){throw new IllegalArgumentException("paymentId not found")}
def paymentId=paymentIdRaw.toString()
paymentId=paymentId.length()>18?paymentId[0..17]:paymentId.padRight(18, ' ')
def billType=""
def customerNoRaw=body['customerNo']
if(!customerNoRaw){throw new IllegalArgumentException("customerNo not found")}
def customerNo=customerNoRaw.toString()
customerNo=customerNo.length()>12?customerNo[0..11]:customerNo.padRight(12, ' ')
def description=''.padRight(200, ' ')
def followupCode=''.padRight(16, ' ')
def depositBill="0"
def customerCount="1"
def totalAmount=amount
def batchSequence='0'.padRight(4, ' ')
def groupSequence="0"
def totalSequence='1'.padRight(4, ' ')
def customerNo2=''.padRight(12, ' ')
def customerNo3=''.padRight(12, ' ')
def customerNo4=''.padRight(12, ' ')
def customerNo5=''.padRight(12, ' ')
def customerNo6=''.padRight(12, ' ')
def customerNo7=''.padRight(12, ' ')
def customerNo8=''.padRight(12, ' ')
def customerNo9=''.padRight(12, ' ')
return accountNo+amount+billId+paymentId+billType+depositBill+groupSequence+followupCode+totalAmount+batchSequence+totalSequence+customerCount+customerNo+customerNo2+customerNo3+customerNo4+customerNo5+customerNo6+customerNo7+customerNo8+customerNo9+description