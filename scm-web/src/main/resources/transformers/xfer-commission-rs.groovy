package transformers

def body = exchange.in.body
def actionCode = body[0..4]
if (body..toString().trim().length()==5){
    throw new ir.daneshrefah.scm.common.exception.NabError(actionCode, "nab error!");
}

def docNo = body[39..56]
def date = body[57..64]
def amount = body[65..82]
return [
        "docNo":docNo..toString().trim(),
        "date":date..toString().trim(),
        "amount":amount..toString().trim()
]