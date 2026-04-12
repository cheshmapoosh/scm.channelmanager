package transformers

def person = exchange.in.headers['person']
println "groovy person $person"
def nationalIdRaw =  "14006284864" //person.nationalCode  //"14006284864"
if(!nationalIdRaw){throw new IllegalArgumentException("nationalId not found")}
def nationalId=nationalIdRaw.toString()
nationalId=nationalId.length()>12?nationalId[0..11]:nationalId.padRight(12,' ')

def subOrganRaw = "1" //person.subOrganizationId
if(!subOrganRaw){throw new IllegalArgumentException("subOrgan not found")}
def subOrgan=subOrganRaw.toString()
subOrgan=subOrgan.length()>8?subOrgan[0..7]:subOrgan.padRight(8,' ')

return nationalId + subOrgan