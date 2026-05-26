import groovy.json.JsonOutput

import javax.swing.GroupLayout
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

def body = exchange.in.body
def fundTransfer = body.fundTransfer
//println("tcp rq:" + body)

//def fundTransfer = body.fundTransfer
//println("fundTransfer:" + fundTransfer)
//def amount = fundTransfer.amount
//def date = fundTransfer.date
def srcCard = fundTransfer.sourceCardNumber
def destCard = fundTransfer.destinationCardNumber
def padZeroLeft = { str, length ->
    {
        if (str.isEmpty()) {
            return "";
        }
        while (str.length() < length) {
            str = "0" + str;
        }
        return str;
    }
}
def field48 = "DST" + padZeroLeft(destCard.length() + "", 3) + destCard
println("field48" + field48)

//def srcAcc = fundTransfer.sourceAccountNumber

//def trk2EquivData = body.trk2EquivData
//def cvv2 = trk2EquivData.cvv2
//def cardExpirationYearMonth = trk2EquivData.cardExpirationYearMonth
//def pin = trk2EquivData.pin

def transmissionDateTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("MMddHHmmss"))
def localTransactionDateTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyMMddHHmmss"))
def stan = sprintf("%06d", System.currentTimeMillis() % 1_000_000)
def rrn = sprintf("%012d", System.currentTimeMillis() % 1_000_000_000_000L);

def req = [:]
def field = [:]
def security = [:]

req.put("mti", "1100");

field.put("2", srcCard);
field.put("3", "330000");
field.put("4", "000000000000");
field.put("6", "000000000000");
field.put("7", transmissionDateTime);
field.put("11", stan);
field.put("12", localTransactionDateTime);
field.put("22", "61051061314C");
field.put("24", "113");
field.put("26", "6012");
field.put("32", "589463");
field.put("33", "589463");
field.put("37", rrn);
field.put("41", "67777777");
field.put("42", "   777777777600");
field.put("43", "Refah Bank            Tehran       THRIR010010157171371502184852851");
field.put("48", field48);
field.put("49", "364");
req.put("fields", field)

security.put("expiryDate", "");
security.put("cvv2", "");
security.put("pin", "");
security.put("expiryRequired", false);
security.put("cvv2Required", false);
security.put("pinRequired", false);
security.put("macRequired", false);

req.put("security", security)

println("tcp card inq rq: " + req)
println("tcp card inq rq json: " + JsonOutput.toJson(req))

return JsonOutput.toJson(req)