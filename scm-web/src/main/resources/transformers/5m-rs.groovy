package transformers

def bodyRawList = exchange.in.body
if(!bodyRawList||(bodyRawList instanceof String&&bodyRawList.toString().length()==5)){throw new ir.daneshrefah.scm.common.exception.NabError(bodyRawList.toString(), "nab error!");}
def responseList = []
for(def body in bodyRawList){
    def accountNo = body[39..56]
    def accountType = body[57..58]
    def accountDesc = body[59..118]
    def accountBalance = body[119..136]
    def accountAvailBalance = body[137..154]
    def iBanValue = body[155..184]
    def generallCode = body[185..192]
    def descGenerall = body[193..252]
    def subsidiaryAccount = body[253..260]
    def descSubsidiary = body[261..320]
    def commerce = body[321..321]
    def typeTrans = body[322..331]
    def maxInternallAmount = body[332..349]
    def maxPayaAmount = body[350..367]
    def maxSatnaAmount = body[368..385]
    def maxIpAmount = body[386..403]
    def expireDate = body[404..411]
    def createDate = body[412..419]
    def permitServiceId = body[420..429]
    def privilages = []
    if(typeTrans[0..0] == "1"){privilages << "XFER_ADD"}
    if(typeTrans[1..1] == "1"){privilages << "ACH_XFER_ADD"}
    if(typeTrans[2..2] == "1"){privilages << "RTGS_XFER_ADD"}
    if(typeTrans[3..3] == "1"){privilages << "IP_XFER_ADD"}
    def item = [
            "accountNo":accountNo,
            "accountType":accountType,
            "accountDesc":accountDesc,
            "accountBalance":accountBalance,
            "accountAvailBalance":accountAvailBalance,
            "iBanValue":iBanValue,
            "generallCode":generallCode,
            "descGenerall":descGenerall,
            "subsidiaryAccount":subsidiaryAccount,
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
    responseList << item
}
return responseList