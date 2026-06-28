package transformers

import ir.daneshrefah.scm.common.data.entity.asset.AccountTypeLoader

def nabResponse = exchange.in.body
println("5i nab response : "+ body)

def status = nabResponse.status
def actionCode = status.code
def success = status.success
if (!success){
    throw new ir.daneshrefah.scm.common.exception.NabError(actionCode.asText(), "nab error!");
}
def bodyRawList = nabResponse.records
def responseList = []
for(def body in bodyRawList){
    println("5i response body : " + body);

    def accountNo = body.accountNo
    def accountType = body.accountType
    def accountDesc = body.accountDesc
    def accountBalance = body.accountBalance
    def accountAvailBalance = body.accountAvailBalance
    def blockAmount = body.blockAmount
    def iBanValue = body.iBanValue
    def commerce = body.commerce
    def flagKarpar = body.flagKarpar

    def accountTypeName = AccountTypeLoader.accountTypeEntityMap[accountType]?.name ?: ""

    def item = [
            "accountNo":accountNo,
            "accountType":accountTypeName,
            "accountDesc":accountDesc,
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