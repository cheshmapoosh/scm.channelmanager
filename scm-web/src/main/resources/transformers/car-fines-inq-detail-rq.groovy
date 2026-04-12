package transformers

import groovy.json.JsonOutput
import ir.daneshrefah.scm.common.model.person.GeneralPerson
import ir.daneshrefah.scm.common.model.person.GeneralRealPerson
import ir.daneshrefah.scm.uaa.common.model.user.User
import ir.daneshrefah.scm.uaa.common.utils.AuthenticationUtils

def body = exchange.in.body

def user = (User) AuthenticationUtils.getAuthentication().getPrincipal()
def accessParameter = user.getAccessParameters()

def mobileNumber = accessParameter[0]


def person = AuthenticationUtils.getLoggedInUser().getPerson()

String nationalId = ""

if(person instanceof GeneralPerson){
    nationalId = ((GeneralRealPerson) person).getNationalCode()
}

def request = [
        left         : body?.left ?: "",
        mid          : body?.mid ?: "",
        right        : body?.right ?: "",
        alphabet     : body?.alphabet ?: "",
        mobileNumber : mobileNumber ?: "",
        nationalID   : body?.nationalID ?: ""
]

exchange.in.body = JsonOutput.toJson(request)