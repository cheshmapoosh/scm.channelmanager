Endpoints ===========================================================

http://127.0.0.1:8000/.well-known/oauth-authorization-server

Server Client Credential ===================================================

url: http://127.0.0.1:8000/oauth2/token
http-method:POST

it is possible in two method

1) Basic Method
Basic Authorization Header should be sent.
grant_type=client_credentials


2) Post Method
following parameters should be sent as (form-data/x-www-form-urlencoded/querystring):
client_id=mb
client_secret=<client-secret>
grant_type=client_credentials

401 => Error => 
{
    "error": "invalid_client"
}
200 => OK


Responsible Filter: OAuth2ClientAuthenticationFilter
Converter: ClientSecretBasicAuthenticationConverter / ClientSecretPostAuthenticationConverter
Token: OAuth2ClientAuthenticationToken
AuthenticationProvider: ClientSecretAuthenticationProvider

Responsible Filter: OAuth2TokenEndpointFilter
Converter: OAuth2ClientCredentialsAuthenticationConverter
Token: OAuth2ClientCredentialsAuthenticationToken
AuthenticationProvider: OAuth2ClientCredentialsAuthenticationProvider


Server First Password ======================================================

restUrl: http://127.0.0.1:8000/oauth2/token
http-method:POST
grant_type=first_password

following parameters should be sent:
1) ClientId 
if client  marked as 'requireClientAuthentication', client should send as Authorization Basic header
else should send as 'client_id' parameter
2) username
3) password
4) scope
5) claim_code
6) client_version for clients that marked as 'checkVersion'
7) client_signature for clients that marked as 'checkVersion'
8) register_code for clients that marked as 'checkActivation'


Responsible Filter: OAuth2TokenEndpointFilter
Converter: FirstPasswordGrantAuthenticationConverter
Token: PreAuthenticationToken
AuthenticationProvider: OAuth2GeneralAuthenticationProvider

Server Authorization Code ==================================================

sample authorization code start restUrl:
1) redirect to uaa server
http://127.0.0.1:8000/oauth2/authorize?response_type=code&client_id=ib&redirect_uri=http://127.0.0.1:8080/authorized&scope=openid
http://127.0.0.1:8000/oauth2/authorize?response_type=code&client_id=ib&redirect_uri=http://127.0.0.1:8080/authorized&scope=openid profile

2) get access token from rest endpoint
restUrl: POST:http://127.0.0.1:8000/oauth2/token
Authorization header: Basic ...
body: grant_type=authorization_code & code & redirect_uri

http://127.0.0.1:8000/oauth2/token


UAAClient Basic ===============================================================

this method call service client_credential

request should contain Authorization Basic Header
cache key: ClientAuthenticationToken + MD5(username + password)


UAAClient Bearer ==============================================================

request should contain Authorization Bearer Header
cache key: MD5(token) + :: + terminalCode

this method verify jwt token and create Authentication from it
if jwt contains sessionId claim(sni) it fetch session from sessionCache by


UAAClient Session =============================================================

request should contain Authorization Session Header
request should contain X-SCM-Username Header

cache key: sessionId + :: + terminalCode
