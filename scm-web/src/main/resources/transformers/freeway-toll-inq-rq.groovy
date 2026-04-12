package transformers

import groovy.json.JsonOutput

def body = exchange.in.body

def alphabetMap = [
        'الف'      : '01',
        'ب'        : '02',
        'پ'        : '03',
        'ت'        : '04',
        'ث'        : '05',
        'ج'        : '06',
        'د'        : '10',
        'ز'        : '13',
        'س'        : '15',
        'ش'        : '16',
        'ص'        : '17',
        'ط'        : '19',
        'ع'        : '21',
        'ف'        : '23',
        'ق'        : '24',
        'ک'        : '25',
        'گ'        : '26',
        'ل'        : '27',
        'م'        : '28',
        'ن'        : '29',
        'و'        : '30',
        'ه'        : '31',
        'ی'        : '32',
        'معلولین'  : '33',
        'تشریفات'  : '34',
        'D'        : '54',
        'S'        : '69'
]


def left     = body?.left?.padLeft(2, '0')
def mid      = body?.mid?.padLeft(3, '0')
def right    = body?.right?.padLeft(2, '0')
def alphabet = body?.alphabet

def alphabetCode = alphabetMap[alphabet] ?: '00'

def plateNumber = "${left}${alphabetCode}${mid}${right}"

def request = [
        plateNumber: plateNumber
]

exchange.in.body = JsonOutput.toJson(request)
