package transformers

import groovy.json.JsonSlurper

def rawBody = exchange.in.body
def body = rawBody instanceof String ? new JsonSlurper().parseText(rawBody) : rawBody

def output = [
        totalBill     : body?.totalBill ?: 0,
        lockedByPolice: body?.lockedByPolice ?: false
]

exchange.in.body = output
