package transformers

def body = exchange.in.body

def typeRaw = body?.TYPE
def typeStr = typeRaw.toString().toUpperCase()

def typeMap = [
        'AGGREGATED': '01',
        'DETAILED'  : '01'
]

def type = typeMap.get(typeStr, '01')

return type
