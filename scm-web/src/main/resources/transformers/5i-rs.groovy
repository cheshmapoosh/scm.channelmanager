package transformers

import ir.daneshrefah.scm.common.data.entity.asset.AccountTypeLoader

def bodyRawList = exchange.in.body
def actionCode = exchange.getProperty("actionCode")
if(!bodyRawList||(bodyRawList instanceof String&&bodyRawList.toString().length()==5)){
    throw new ir.daneshrefah.scm.common.exception.NabError(actionCode, "nab error!");
}
def responseList = []
for(def body in bodyRawList){
    println("5i response body : " + body);
    if (body.length() < 205) {
        continue
    }

    def accountNo = body[39..56]
    def accountType = body[57..58]
    def accountDesc = body[59..118]
    def accountBalance = body[119..136]
    def accountAvailBalance = body[137..154]
    def blockAmount = body[155..172]
    def iBanValue = body[173..202]
    def commerce = body[203..203]
    def flagKarpar = body[204..204]

    def accountTypeName = AccountTypeLoader.accountTypeEntityMap.get(accountType).getName();

    def item = [
            "accountNo":accountNo.toString().trim().toLong().toString(),
            "accountType":accountTypeName.toString().trim(),
            "accountDesc":accountDesc.toString().trim(),
            "accountBalance":accountBalance.toString().trim().toLong(),
            "accountAvailBalance":accountAvailBalance.toString().trim().toLong(),
            "blockAmount":blockAmount.toString().trim().toLong(),
            "iBanValue":iBanValue.toString().trim(),
            "commerce":commerce.toString().trim(),
            "flagKarpar":flagKarpar.toString().trim()
    ]
    responseList << item
}
return responseList