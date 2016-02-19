package com.mthaler.akkahttptest

import javax.net.ssl.{SSLSession, HostnameVerifier}

class AllHostsValid extends HostnameVerifier {
  override def verify(s: String, sslSession: SSLSession): Boolean = true
}
