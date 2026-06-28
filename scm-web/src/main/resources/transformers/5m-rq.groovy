package transformers

def body = exchange.in.body
def header = exchange.in.headers
def person = header['person']
println "groovy person $person"
def nationalIdRaw = "0047672064"// person.nationalCode //"0047672064"
if(!nationalIdRaw){throw new IllegalArgumentException("nationalId not found")}
def nationalId=nationalIdRaw.toString()

def nabRequest = [
        "command" : [
                "code"    : "5M",
                "protocol": "ATPS"
        ],
        "data"    : [
                "nationalId" : nationalId
        ],
        "request" : [
                "fields": [
                        ["name": "nationalId", "length": 12, "required": true],
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

return nabRequest