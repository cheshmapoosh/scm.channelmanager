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
    def blockAmount = body[155..172]
    def iBanValue = body[173..202]
    def commerce = body[203..203]
    def flagKarpar = body[204..204]
    def item = [
            "accountNo":accountNo,
            "accountType":accountType,
            "accountDesc":accountDesc,
            "accountBalance":accountBalance,
            "accountBalance":accountBalance,
            "accountAvailBalance":accountAvailBalance,
            "blockAmount":blockAmount,
            "iBanValue":iBanValue,
            "commerce":commerce,
            "flagKarpar":flagKarpar
    ]
    responseList << item
}
return responseList