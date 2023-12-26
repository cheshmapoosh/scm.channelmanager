Endpoints ===========================================================

http://127.0.0.1:8000/.well-known/oauth-authorization-server

Client Credential ===================================================

Responsible Filter: OAuth2ClientAuthenticationFilter
Converter: ClientSecretBasicAuthenticationConverter / ClientSecretPostAuthenticationConverter
Token: OAuth2ClientAuthenticationToken
AuthenticationProvider: ClientSecretAuthenticationProvider

Responsible Filter: OAuth2TokenEndpointFilter
Converter: OAuth2ClientCredentialsAuthenticationConverter
Token: OAuth2ClientCredentialsAuthenticationToken
AuthenticationProvider: OAuth2ClientCredentialsAuthenticationProvider

1) ClientSecretBasicAuthenticationConverter
trigger when Authorization Basic header exist

url: http://127.0.0.1:8000/oauth2/token
Authorization header: Basic ...
body: grant_type=client_credentials

2) ClientSecretPostAuthenticationConverter
grant_type = client_credentials & client_id & client_secret should include in querystring or form-data or x-www-form-urlencoded


Authorization Code ==================================================

sample authorization code start url:
1) redirect to uaa server
http://127.0.0.1:8000/oauth2/authorize?response_type=code&client_id=ib&redirect_uri=http://127.0.0.1:8080/authorized&scope=openid
http://127.0.0.1:8000/oauth2/authorize?response_type=code&client_id=ib&redirect_uri=http://127.0.0.1:8080/authorized&scope=openid profile

2) get access token from rest endoint
url: POST:http://127.0.0.1:8000/oauth2/token
Authorization header: Basic ...
body: grant_type=authorization_code & code & redirect_uri

http://127.0.0.1:8000/oauth2/token


First Password ======================================================

url: http://127.0.0.1:8000/oauth2/token
Authorization header: Basic ... data for client
body: grant_type=first_password & username & password & scope(optional)

Responsible Filter: OAuth2TokenEndpointFilter
Converter: FirstPasswordGrantAuthenticationConverter
Token: PreAuthenticationToken
AuthenticationProvider: OAuth2GeneralAuthenticationProvider