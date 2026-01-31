package transformers

def body = exchange.in.body
def nationalId = body[39..50]
def name = body[51..85]
def lastName = body[86..145]
def mobile = body[146..158]
def address = body[159..218]
def accountType = body[219..220]
def generallCode = body[221..228]
def descGenerall = body[229..288]
def subsidiaryCode = body[289..296]
def descSubsidiary = body[297..356]
def commerce = body[357..357]
def typeTrans = body[358..367]
def maxInternallAmount = body[368..385]
def maxPayaAmount = body[386..403]
def maxSatnaAmount = body[404..421]
def maxIpAmount = body[422..439]
def expireDate = body[440..447]
def createDate = body[448..455]
def permitServiceId = body[456..465]
def privilages = []
if(typeTrans[0..0] == "1"){privilages << "XFER_ADD"}
if(typeTrans[1..1] == "1"){privilages << "ACH_XFER_ADD"}
if(typeTrans[2..2] == "1"){privilages << "RTGS_XFER_ADD"}
if(typeTrans[3..3] == "1"){privilages << "IP_XFER_ADD"}
return [
        "nationalId":nationalId,
        "name":name,
        "lastName":lastName,
        "mobile":mobile,
        "address":address,
        "accountType":accountType,
        "generallCode":generallCode,
        "descGenerall":descGenerall,
        "subsidiaryCode":subsidiaryCode,
        "descSubsidiary":descSubsidiary,
        "commerce":commerce,
        "typeTrans":privilages,
        "maxInternallAmount":maxInternallAmount,
        "maxPayaAmount":maxPayaAmount,
        "maxSatnaAmount":maxSatnaAmount,
        "maxIpAmount":maxIpAmount,
        "expireDate":expireDate,
        "createDate":createDate,
        "permitServiceId" : permitServiceId
]