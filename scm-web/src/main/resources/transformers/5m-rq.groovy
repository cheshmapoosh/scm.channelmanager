package transformers

def body = exchange.in.body
def header = exchange.in.headers
def person = header['person']
println "groovy person $person"
def nationalIdRaw = "0047672064"// person.nationalCode //"0047672064"
if(!nationalIdRaw){throw new IllegalArgumentException("nationalId not found")}
def nationalId=nationalIdRaw.toString()
nationalId=nationalId.length()>12?nationalId[0..11]:nationalId.padRight(12,' ')
return nationalId