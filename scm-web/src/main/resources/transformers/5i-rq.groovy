package transformers
import ir.daneshrefah.scm.common.model.person.GeneralLegalPerson
import ir.daneshrefah.scm.uaa.common.utils.AuthenticationUtils


def loggedInUser = AuthenticationUtils.getLoggedInUser();
def person = Objects.requireNonNull(loggedInUser).getPerson()
String nationalId = ""
String subOrg = ""

if(person instanceof GeneralLegalPerson){
    nationalId = ((GeneralLegalPerson) person).getNationalId()
    subOrg =  ((GeneralLegalPerson) person).getSubOrganizationId()
}

def nationalIdRaw = nationalId//"14006284864"
if (!nationalIdRaw) {
    throw new IllegalArgumentException("nationalId not found")
}

def subOrganRaw = subOrg //"1"
if (!subOrganRaw) {
    throw new IllegalArgumentException("subOrgan not found")
}
def subOrgan = subOrganRaw.toString()

def nabRequest = [
        "command" : [
                "code"    : "5I",
                "protocol": "ATPS"
        ],
        "data"    : [
                "nationalId": nationalId,
                "subOrgan"  : subOrgan
        ],
        "request" : [
                "fields": [
                        ["name": "nationalId", "length": 12, "required": true],
                        ["name": "subOrgan", "length": 8, "required": true]
                ]
        ],
        "response": [
                "fields": [
//                        ["name": "actionCode", "length": 5],
["name": "command", "length": 2],
["name": "service", "length": 2],
["name": "date", "length": 8],
["name": "time", "length": 6],
["name": "refNo", "length": 16],
["name": "accountNo", "length": 18],
["name": "accountType", "length": 2],
["name": "accountDesc", "length": 60],
["name": "accountBalance", "length": 18],
["name": "accountAvailBalance", "length": 18],
["name": "blockAmount", "length": 18],
["name": "iBanValue", "length": 30],
["name": "commerce", "length": 1],
["name": "flagKarpar", "length": 1]
                ]
        ]
]

return nabRequest