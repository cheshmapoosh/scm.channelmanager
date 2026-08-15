package transformers

import org.slf4j.LoggerFactory

def body = exchange.in.body
def accountId = body['accountNo']

//def log = LoggerFactory.getLogger("5kRqGroovyTransformer")
//log.info("5k rq body {}", body)
println("5k rq body {}"+ body)

def nabRequest = [
        "command" : [
                "code"    : "5K",
                "protocol": "ATPS"
        ],
        "data"    : [
                "accountId" : accountId.toString()
        ],
        "request" : [
                "fields": [
                        ["name": "accountId", "length": 18, "required": true],
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
                        ["name": "nationalId", "length": 12],
                        ["name": "name", "length": 35],
                        ["name": "lastName", "length": 60],
                        ["name": "mobile", "length": 13],
                        ["name": "address", "length": 100],
                        ["name": "accountType", "length": 2],
                        ["name": "general", "length": 8],
                        ["name": "descGeneral", "length": 60],
                        ["name": "subsidiary", "length": 8],
                        ["name": "descSubsidiary", "length": 60],
                        ["name": "commerce", "length": 1],
                        ["name": "typeTrans", "length": 10],
                        ["name": "maxInternalAmount", "length": 18],
                        ["name": "maxPayaAmount", "length": 18],
                        ["name": "maxSatnaAmount", "length": 18],
                        ["name": "maxIpAmount", "length": 18],
                        ["name": "expireDate", "length": 8],
                        ["name": "createDate", "length": 8],
                        ["name": "permitServiceId", "length": 10]
                ]
        ]
]

//log.info("5k transformed nab request : {}", nabRequest)
println("5k transformed nab request : {}"+ nabRequest)
return nabRequest