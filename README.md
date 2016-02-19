# akka-http-test
Sample classes that implement a HTTP(S) client / server

To create a self-signed certificate for the HTTPS Server, do the following:

```bash
openssl genrsa -des3 -out server.key 2048
openssl rsa -in server.key -out server.key
openssl req -sha256 -new -key server.key -out server.csr -subj '/CN=localhost'
openssl x509 -req -days 365 -in server.csr -signkey server.key -out server.crt
```

Replace 'localhost' with your domain. To combine the two into a .pem file:

```bash
cat server.crt server.key > cert.pem
```
To create a KeyStore in PKCS12 Format, do:

```
openssl pkcs12 -export -in cert.pem -out mykeystore.pkcs12  -name akka-http-test -noiter -nomaciter
```
## HttpsServer and ConnectionLevelHttpsClient

HttpsServer and ConnectionLevelHttpsClient demonstrate how to write a simple HTTPS server and client. HttpsServer uses a self-signed certificate and ConnectionLevelHttpsClient ignores the certificate and disables hostname verification.

_Warning: do not do this with production code_! But this might be useful e.g. for writung a a test server that provides a REST interface used for testing.
