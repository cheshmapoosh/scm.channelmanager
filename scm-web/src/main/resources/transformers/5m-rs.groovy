package transformers

import ir.daneshrefah.scm.common.data.entity.asset.AccountTypeLoader

def bodyRawList = exchange.in.body
def actionCode = exchange.getProperty("actionCode")
if(!bodyRawList||(bodyRawList instanceof String&&bodyRawList.toString().length()==5)){throw new ir.daneshrefah.scm.common.exception.NabError(actionCode, "nab error!");}
def responseList = []
for(def body in bodyRawList){
    if (body.length() < 394) {
        continue
    }

    def accountNo = body[39..56]
    def accountType = body[57..58]
    def accountDesc = body[59..118]
//    def accountBalance = body[119..136]
//    def accountAvailBalance = body[137..154]
    def iBanValue = body[119..148]
    def generallCode = body[149..156]
    def descGenerall = body[157..216]
    def subsidiaryAccount = body[217..224]
    def descSubsidiary = body[225..284]
    def commerce = body[285..285]
    def typeTrans = body[286..295]
    def maxInternallAmount = body[296..313]
    def maxPayaAmount = body[314..331]
    def maxSatnaAmount = body[332..349]
    def maxIpAmount = body[350..367]
    def expireDate = body[368..375]
    def createDate = body[376..383]
    def permitServiceId = body[384..393]
    def remDebitFt = body[394..411]
    def remDebitSatna = body[412..429]
    def remDebitPaya = body[430..447]
    def remDebitPol = body[448..465]

    def privilages = []

    if (typeTrans[0..0] == "1") {
        privilages << [privilage:"XFER_ADD", amount : maxInternallAmount.toString().trim().toLong(), remDebit : remDebitFt.toString().trim().toLong()]
    }
    if (typeTrans[1..1] == "1") {
        privilages << [privilage:"ACH_XFER_ADD", amount :maxPayaAmount.toString().trim().toLong(), remDebit : remDebitSatna.toString().trim().toLong()]
    }
    if (typeTrans[2..2] == "1") {
        privilages << [privilage:"RTGS_XFER_ADD", amount :maxSatnaAmount.toString().trim().toLong(), remDebit : remDebitPaya.toString().trim().toLong()]
    }
    if (typeTrans[3..3] == "1") {
        privilages << [privilage:"IP_XFER_ADD", amount :maxIpAmount.toString().trim().toLong(), remDebit : remDebitPol.toString().trim().toLong()]
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

    def accountTypeName = AccountTypeLoader.accountTypeEntityMap.get(accountType).getName();

    def item = [
            "accountNo":accountNo.toString().trim(),
            "accountType":accountTypeName.toString().trim(),
            "accountDesc":accountDesc.toString().trim(),
//            "accountBalance":accountBalance.toString().trim().toLong(),
//            "accountAvailBalance":accountAvailBalance.toString().trim().toLong(),
            "iBanValue":iBanValue.toString().trim(),
            "generallCode":generallCode.toString().trim(),
            "descGenerall":descGenerall.toString().trim(),
            "subsidiaryAccount":subsidiaryAccount.toString().trim(),
            "descSubsidiary":descSubsidiary.toString().trim(),
            "commerce":commerce.toString().trim(),
            "typeTrans":privilages,
            "expireDate":expireDate.toString().trim(),
            "createDate":createDate.toString().trim(),
            "permitServiceId" : permitServices.toString().trim()
    ]
    responseList << item
}
return responseList