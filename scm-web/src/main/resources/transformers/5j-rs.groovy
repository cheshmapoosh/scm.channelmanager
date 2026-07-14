package transformers

import org.slf4j.LoggerFactory

def body = exchange.in.body
def log = LoggerFactory.getLogger("5jRsGroovyTransformer")
log.info("5j nab response : {}", body)

def status = body.status
def actionCode = status.code
def success = status.success
log.info("5j nab status code : {}", actionCode.asText())
if (!success){
    throw new ir.daneshrefah.scm.common.exception.NabError(actionCode.asText(), "nab error!");
}
return [
        "actionCode" : actionCode
]