package transformers

def body = exchange.in.body
def actionCode = exchange.getProperty("actionCode")
if (body.trim().length()==5){
    throw new ir.daneshrefah.scm.common.exception.NabError(actionCode, "nab error!");
}
return [
        "actionCode" : actionCode
]