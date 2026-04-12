package transformers

import ir.daneshrefah.scm.utils.date.DateUtils

def b = exchange.in.body;
println("5j body before transform : "+b)
println("permitServiceId : " +  b['permitServiceId'])
println("privileges : " + b['privileges'])
println("customer count : " + b.customerCount)


def f = { value, len -> value = value?.toString() ?: ''; value.length() > len ? value[0..<len] : value.padRight(len, ' ') };

def privilagesRaw = b['privileges'] ?: [];
def getAmount = { prvlg ->
    {
        def objFound = privilagesRaw.find {obj -> obj.privilage.toString().equals(prvlg)}
        objFound ? objFound.amount.toString().trim().toLong() : 0L
    }
}

def privilages = new StringBuilder();
def maxAmounts = new StringBuilder();

//maxInternalAmount
def internalAmount = getAmount('XFER_ADD')
privilages << (internalAmount == 0 ? '0' : '1');
maxAmounts <<  f(internalAmount, 18);

//maxPayaAmount
def payaAmount = getAmount('ACH_XFER_ADD')
privilages << (payaAmount == 0 ? '0' : '1');
maxAmounts <<  f(payaAmount, 18);

////maxSatnaAmount
def satnaAmount = getAmount('RTGS_XFER_ADD')
privilages << (satnaAmount == 0 ? '0' : '1');
maxAmounts <<  f(satnaAmount, 18);

//maxIpAmount
def ipAmount = getAmount('IP_XFER_ADD')
privilages << (ipAmount == 0 ? '0' : '1');
maxAmounts <<  f(ipAmount, 18);

privilages = f(privilages, 10);

def ch = b['permitServiceId'] ?: [];
def permistServiceId = new StringBuilder();
permistServiceId << (ch.contains('atm') ? '1' : '0')
        << (ch.contains('mb') ? '1' : '0')
        << (ch.contains('ib') ? '1' : '0');
permistServiceId = f(permistServiceId, 10);

def customers = b['customers'] ?: [];
def customersStr = new StringBuilder();
if(customers.size() > 9){
    throw new RuntimeException("invalid customer list size");
}
customers.each {customer ->
    customersStr << f(customer, 12);
}

if(customers.size() < 9){
    customersStr << f(' ', 12 * (9-customers.size()));
}

def request = f(b.accountNo, 18) +
        f(b.nationalCode, 10) +
        f(b.cardNo, 20) +
        f(b.customerCount, 1) +
        customersStr.toString() +
        f(b.insDel, 1) +
        privilages +
        maxAmounts +
//        f(b.maxInternalAmount, 18) +
//        f(b.maxPayaAmount, 18) +
//        f(b.maxSatnaAmount, 18) +
//        f(b.maxIpAmount, 18) +
        f(b.expireDate, 8) +
        permistServiceId
return request