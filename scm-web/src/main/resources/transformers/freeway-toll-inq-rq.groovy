package transformers

import groovy.json.JsonOutput

def body = exchange.in.body

def request = [
        plateNumber: body?.plateNumber ?: 000000000
]

exchange.in.body = JsonOutput.toJson(request)