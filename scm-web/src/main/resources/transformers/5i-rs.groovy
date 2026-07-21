package transformers

import ir.daneshrefah.scm.common.data.entity.asset.AccountTypeLoader
import org.slf4j.LoggerFactory

def nabResponse = exchange.in.body
def log = LoggerFactory.getLogger("5iRsGroovyTransformer")
log.info("5i nab response : {}", nabResponse)

def status = nabResponse.status
def actionCode = status.code
def success = status.success
log.info("5i nab status code : {}", actionCode.asText())
if (!success) {
    throw new ir.daneshrefah.scm.common.exception.NabError(actionCode.asText(), "nab error!");
}
def bodyRawList = nabResponse.records
def responseList = []
for (def body in bodyRawList) {
    log.info("5i response body : {}", body);

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
            "accountNo"          : accountNo,
            "accountType"        : accountTypeName,
            "accountDesc"        : accountDesc,
            "accountBalance"     : accountBalance,
            "accountAvailBalance": accountAvailBalance,
            "blockAmount"        : blockAmount,
            "iBanValue"          : iBanValue,
            "commerce"           : commerce,
            "flagKarpar"         : flagKarpar
    ]
    responseList << item
}

log.info("5i nab response : {}", responseList)
return responseList