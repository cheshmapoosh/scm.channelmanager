def body = exchange.in.body

if (!body instanceof Map) {
    return
}

return [
        "status" : body.actionCode
]