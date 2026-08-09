package transformers

import ir.daneshrefah.scm.common.model.person.GeneralLegalPerson
import ir.daneshrefah.scm.common.model.person.GeneralPerson
import ir.daneshrefah.scm.common.model.person.GeneralRealPerson
import ir.daneshrefah.scm.uaa.common.utils.AuthenticationUtils
import org.slf4j.LoggerFactory

//def log = LoggerFactory.getLogger("5mRqGroovyTransformer")
def loggedInUser = AuthenticationUtils.getLoggedInUser();
def person = Objects.requireNonNull(loggedInUser).getPerson()
String nationalId = ""
if (person instanceof GeneralRealPerson) {
    println("karpardaz : GeneralRealPerson")
    nationalId = ((GeneralRealPerson) person).getNationalCode()
}else if(person instanceof GeneralLegalPerson)
    nationalId = ((GeneralLegalPerson) person).getNationalId()

//log.info("5m currentt user national code : {}", nationalId)
println("5m currentt user national code : "+ nationalId)
def nationalIdRaw = nationalId //"0047672064"
if (!nationalIdRaw) {
    throw new IllegalArgumentException("nationalId not found")
}

def nabRequest = [
        "command" : [
                "code"    : "5M",
                "protocol": "ATPS"
        ],
        "data"    : [
                "nationalId": nationalId
        ],
        "request" : [
                "fields": [
                        ["name": "nationalId", "length": 12, "required": true],
                ]
        ],
        "response": [
                "fields": [
                        ["name": "command", "length": 2],
                        ["name": "service", "length": 2],
                        ["name": "date", "length": 8],
                        ["name": "time", "length": 6],
                        ["name": "refNo", "length": 16],
                        ["name": "accountNo", "length": 18],
                        ["name": "accountType", "length": 2],
                        ["name": "accountDesc", "length": 60],
                        ["name": "iBanValue", "length": 30],
                        ["name": "generalCode", "length": 8],
                        ["name": "descGeneral", "length": 60],
                        ["name": "subsidiaryAccount", "length": 8],
                        ["name": "descSubsidiary", "length": 60],
                        ["name": "commerce", "length": 1],
                        ["name": "typeTrans", "length": 10],
                        ["name": "maxInternalAmount", "length": 18],
                        ["name": "maxPayaAmount", "length": 18],
                        ["name": "maxSatnaAmount", "length": 18],
                        ["name": "maxIpAmount", "length": 18],
                        ["name": "expireDate", "length": 8],
                        ["name": "createDate", "length": 8],
                        ["name": "permitServiceId", "length": 10],
                        ["name": "remDebitFt", "length": 18],
                        ["name": "remDebitSatna", "length": 18],
                        ["name": "remDebitPaya", "length": 18],
                        ["name": "remDebitPol", "length": 18],
                ]
        ]
]

//log.info("5m transformed nab request : {}", nabRequest)
println("5m transformed nab request : "+ nabRequest)
return nabRequest