package transformers

def body = exchange.in.body
def errorCode = body.errorCode

def status = body.get("status")
println("rest card inq rs status : " + status)
if (status == null) {
    throw new RuntimeException("REST provider should set HTTP response code")
}
def statusCode = status as int
if (!(statusCode >= 200 && statusCode < 300)) {
    throw new RuntimeException("Expected 2xx status from HPS card inquiry endpoint, but got " + statusCode)
}

def bodyResponse = body.get("body")
def out = bodyResponse.get("outData")

if (out == null && bodyResponse.get("errorCode") != null) {
    throw new RuntimeException(bodyResponse.get("errorDescription"))
}

println("rest card password inq rs body : " + body)
if (out == null && errorCode != null) {
    throw new RuntimeException(bodyResponse.get("errorDescription"))
}

return []