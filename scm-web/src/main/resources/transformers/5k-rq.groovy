package transformers

def body = exchange.in.body
def accountIdRaw = body['accountNo']
if(!accountIdRaw){throw new IllegalArgumentException("accountId not found")}
def accountId=accountIdRaw.toString()
accountId=accountId.length()>18?accountId[0..17]:accountId.padRight(18,' ')
return accountId