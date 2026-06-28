package transformers

def body = exchange.in.body
println("5j nab response : "+ body)

def status = body.status
def actionCode = status.code
def success = status.success
if (!success){
    throw new ir.daneshrefah.scm.common.exception.NabError(actionCode.asText(), "nab error!");
}
return [
        "actionCode" : actionCode
]