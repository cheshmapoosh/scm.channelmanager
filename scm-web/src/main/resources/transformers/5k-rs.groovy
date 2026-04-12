package transformers

import ir.daneshrefah.scm.common.data.entity.asset.AccountTypeLoader

def bodyRawList = exchange.in.body
def actionCode = exchange.getProperty("actionCode")
if (!bodyRawList || (bodyRawList instanceof String && bodyRawList.toString().length() == 5)) {
    throw new ir.daneshrefah.scm.common.exception.NabError(actionCode, "nab error!");
}
def responseList = []
for (def body in bodyRawList) {
    println("5k response body : " + body);
    if (body.length() < 506) {
        continue
    }
    def nationalId = body[39..50]
    def name = body[51..85]
    def lastName = body[86..145]
    def mobile = body[146..158]
    def address = body[159..258]
    def accountType = body[259..260]
    def generallCode = body[261..268]
    def descGenerall = body[269..328]
    def subsidiaryCode = body[329..336]
    def descSubsidiary = body[337..396]
    def commerce = body[397..397]
    def typeTrans = body[398..407]
    def maxInternallAmount = body[408..425]
    def maxPayaAmount = body[426..443]
    def maxSatnaAmount = body[444..461]
    def maxIpAmount = body[462..479]
    def expireDate = body[480..487]
    def createDate = body[488..495]
    def permitServiceId = body[496..505]

    def accountTypeName = AccountTypeLoader.accountTypeEntityMap.get(accountType).getName();
    println("accountTypeName ": accountTypeName)

    def privilages = []

    if (typeTrans[0..0] == "1") {
        privilages << [privilage:"XFER_ADD", amount : maxInternallAmount.toString().trim().toLong()]
    }
    if (typeTrans[1..1] == "1") {
        privilages << [privilage:"ACH_XFER_ADD", amount :maxPayaAmount.toString().trim().toLong()]
    }
    if (typeTrans[2..2] == "1") {
        privilages << [privilage:"RTGS_XFER_ADD", amount :maxSatnaAmount.toString().trim().toLong()]
    }
    if (typeTrans[3..3] == "1") {
        privilages << [privilage:"IP_XFER_ADD", amount :maxIpAmount.toString().trim().toLong()]
    }

    def permitServices = []
    if(permitServiceId[0..0] == "1"){
        permitServices << "atm"
    }
    if(permitServiceId[1..1] == "1"){
        permitServices << "ib"
    }
    if(permitServiceId[2..2] == "1"){
        permitServices << "mb"
    }

    def item = [
            "nationalId"        : nationalId.toString().trim(),
            "name"              : name.toString().trim(),
            "lastName"          : lastName.toString().trim(),
            "mobile"            : mobile.toString().trim(),
            "address"           : address.toString().trim(),
            "accountType"       : accountTypeName.toString().trim(),
            "generallCode"      : generallCode.toString().trim(),
            "descGenerall"      : descGenerall.toString().trim(),
            "subsidiaryCode"    : subsidiaryCode.toString().trim(),
            "descSubsidiary"    : descSubsidiary.toString().trim(),
            "commerce"          : commerce.toString().trim(),
            "typeTrans"         : privilages,
            "expireDate"        : expireDate.toString().trim(),
            "createDate"        : createDate.toString().trim(),
            "permitServiceId"   : permitServices.toString().trim()
    ]
    responseList << item
}
return responseList