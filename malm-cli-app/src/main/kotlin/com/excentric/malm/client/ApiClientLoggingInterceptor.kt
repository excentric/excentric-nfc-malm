package com.excentric.malm.client

import org.slf4j.LoggerFactory
import org.springframework.http.HttpRequest
import org.springframework.http.client.ClientHttpRequestExecution
import org.springframework.http.client.ClientHttpRequestInterceptor
import org.springframework.http.client.ClientHttpResponse
import org.springframework.stereotype.Component
import java.io.BufferedReader
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets

@Component
class ApiClientLoggingInterceptor : ClientHttpRequestInterceptor {
    private val log = LoggerFactory.getLogger(ApiClientLoggingInterceptor::class.java)

    override fun intercept(
        request: HttpRequest,
        body: ByteArray,
        execution: ClientHttpRequestExecution
    ): ClientHttpResponse {
        logRequest(request, body)
        val response = execution.execute(request, body)
        logResponse(response)
        return response
    }

    private fun logRequest(request: HttpRequest, body: ByteArray) {
        if (log.isDebugEnabled) {
            log.debug("=========================== REQUEST BEGIN ===========================")
            log.debug("URI: {}", request.uri)
            log.debug("Method: {}", request.method)
            log.debug("Headers: {}", request.headers)
            log.debug("Request body: {}", String(body, StandardCharsets.UTF_8))
            log.debug("=========================== REQUEST END ===========================")
        }
    }

    private fun logResponse(response: ClientHttpResponse) {
        if (log.isDebugEnabled) {
            val inputStringBuilder = StringBuilder()
            try {
                BufferedReader(InputStreamReader(response.body, StandardCharsets.UTF_8)).use { bufferedReader ->
                    var line: String?
                    while (bufferedReader.readLine().also { line = it } != null) {
                        inputStringBuilder.append(line)
                    }
                }
                log.debug("=========================== RESPONSE BEGIN ===========================")
                log.debug("Status code: {}", response.statusCode)
                log.debug("Headers: {}", response.headers)
                log.debug("Response body: {}", inputStringBuilder.toString())
                log.debug("=========================== RESPONSE END ===========================")
            } catch (e: Exception) {
                log.error("Error logging response", e)
            }
        }
    }
}
